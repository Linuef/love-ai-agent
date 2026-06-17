下面的类是智能体的核心架构。

这个智能体的架构参考的是**[OpenManus](https://github.com/FoundationAgents/OpenManus)**的架构

# BaseAgent

智能体基类，定义基本信息和多步骤执行流程。

一些难理解的设计，我下载下面了，其余的都好理解，不行就去问AI：

## nextStepPrompt变量的作用

用户在输入提示词后，比如"请帮我查看今天的天气"，接着这个nextStepPrompt提示词就会引导AI分析该用什么工具，然后返回所需工具以及对应参数的信息，用于工具调用。

nextStepPrompt示例：

```java
String NEXT_STEP_PROMPT = """
Based on user needs, proactively select the most appropriate tool or combination of tools. For complex tasks, you can break down the problem and use different tools step by step to solve it. After using each tool, clearly explain the execution results and suggest the next steps.

If you want to stop the interaction at any point, use the `terminate` tool/function call.
""";
```



## messageList上下文

由于我不想让Spring AI托管工具调用全过程，我想控制下面的过程：

拿到AI返回的要调用的工具和对应参数的信息，交给Spring AI去执行工具调用，然后我拿到response后，加入上下文，拼接成字符串返回给AI。

因此，在上面的过程中，上下文需要由我自己控制，比如：

1. 用户输入提示词后，我把用户提示词加入上下文，然后执行agent_loop，开始循环
2. 我把nextStepPrompt作为用户提示词加入上下文，引导AI输出所需工具和对应参数
3. 如果输出显示不需要调用工具，就把刚才拿到的助手消息assistantMessage加入上下文
4. 如果输出显示需要调用工具，就执行act方法，在act方法里，会把原本的上下文，第三步拿到的助手消息以及执行工具获得的结果都加入上下文，然后把工具调用的结果拼接成字符串返回给AI



# ReActAgent

实现思考和行动两个步骤的智能体，继承自BaseAgent。

由于ReActAgent继承自BaseAgent，因此在加上@Data注解后，还要补充一个@EqualsAndHashCode(callSuper = true)注解，原因：

BaseAgent有一个属性是name。我现在要比较两个ReActAgent是否相等，在比较了ReActAgent内部的属性后，如果没加@EqualsAndHashCode(callSuper = true)注解，就不会比较name属性，如果两个对象的name属性不同，就会产生矛盾。

ReActAgent主要重写了step方法，并定义了think和act方法。



# ToolCallAgent

虽然ToolCallAgent的代码比较多，但有很大部分代码都是为了调试使用，代码逻辑很简单。

实现工具调用能力的智能体，继承自ReActAgent，重写了think和act方法。

## think和act方法中地prompt变量

在think和act方法中，都需要构造提示词：

```java
Prompt prompt = new Prompt(messageList, chatOptions);
```

在think方法中，这个提示词给AI后，AI就会给出所需工具和对应参数。

在act方法中，toolCallingManager通过执行executeToolCalls(prompt, toolCallChatResponse)方法，执行工具调用。

| 参数                     | 来源                                                | 核心职责                 |                           存储内容                           |
| :----------------------- | --------------------------------------------------- | ------------------------ | :----------------------------------------------------------: |
| **toolCallChatResponse** | `think()` 中 LLM 的返回结果                         | **下达工具调用指令**     |      1. 模型文本回答2. 工具名称、调用参数、工具调用 ID       |
| **prompt**               | 你手动封装的 `new Prompt(messageList, chatOptions)` | **提供执行环境与上下文** | 1. 全量对话历史 `messageList`2. 所有可用工具 `ToolCallback`3. 模型 / 工具配置 `ChatOptions`4. 工具运行时上下文 `ToolContext` |

## act方法中的conversationHistory()方法

得到conversationHistory变量后，就把这个变量地值赋值给上下文。

```txt
toolCallingManager在执行executeToolCalls(prompt, toolCallChatResponse)方法时，会自动地构造conversationHistory变量，然后把conversationHistory变量赋值给toolExecutionResult，如下图，这是在DefaultToolCallingManager(实现了toolCallingManager)中实现地方法：
```

![image-20260617150708163](C:\Users\20496\AppData\Roaming\Typora\typora-user-images\image-20260617150708163.png)

![image-20260617150829314](C:\Users\20496\AppData\Roaming\Typora\typora-user-images\image-20260617150829314.png)

## 禁止Spring AI托管工具调用

在BaseAgent类中说过，我想自己控制工具调用地一部分过程，不想全部交给Spring AI。

下面是有关代码：

```
//禁用 Spring AI 内置的工具调用机制，自己维护选项和消息上下文
private final ChatOptions chatOptions;



public ToolCallAgent(ToolCallback[] availableTools) {
    super();
    this.availableTools = availableTools;
    this.toolCallingManager = ToolCallingManager.builder().build();
    //禁用 Spring AI 内置的工具调用机制，自己维护选项和消息上下文
    this.chatOptions = DashScopeChatOptions.builder()
            .internalToolExecutionEnabled(false)
            .build();
}
```

## nextStepPrompt

这个变量地作用已经在BaseAgent类说过了。