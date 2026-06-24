下面的类是智能体的核心架构。

这个智能体的架构参考的是**[OpenManus](https://github.com/FoundationAgents/OpenManus)**的架构

# BaseAgent

智能体基类，定义基本信息和多步骤执行流程。

一些难理解的设计，我写在下面了，其余的都好理解，不行就去问AI。

## runStream方法

基于run方法改造的方法，可以流式输出Manux智能体在循环中生成的信息。

**幂等性**：**方法 / 接口重复执行多次，和执行一次的效果一致，不会产生副作用、报错或数据错乱**。

**一、功能**

基于 **Spring SseEmitter + ** 实现**SSE 长连接流式推送**，异步分步执行智能体任务，实时把每一步结果推送给前端。

**二、核心组件说明**

1. **SseEmitter**
   - 创建长连接，超时 `300000ms(5分钟)`，超时框架自动关闭连接。
   - 负责向前端分段发送数据、主动关闭连接。
2. **CompletableFuture.runAsync**
   - 异步执行业务，**不阻塞 Tomcat 请求线程**，提升接口并发能力。
3. **状态机 AgentState**
   - 控制智能体状态（空闲 / 运行 / 结束 / 异常），**防止并发重复调用**。

**三、执行流程**

1. 初始化 SSE 发射器，注册**超时、连接结束**两个回调。
2. 异步线程内做前置校验：状态、入参非空。
3. 标记状态为运行中，记录用户提问上下文。
4. 循环分步执行智能体逻辑，每执行一步就通过 SSE 推送给前端。
5. 执行完毕 / 达到最大步数，正常关闭 SSE。
6. 出现异常：标记错误状态，推送错误信息并关闭连接。
7. `finally` 统一清理临时资源。

**四、回调说明**

1. **onTimeout**

   连接 5 分钟超时触发：修正状态、清理资源、打印警告日志。

2. **onCompletion**

   连接所有关闭场景都会触发（正常结束、异常、超时、前端断连），兜底修正状态 + 资源清理。

**五、特点 & 注意点**

- 异步执行，请求线程快速释放，适合耗时流式任务。
- 多层异常捕获，保证 SSE 连接正常收尾。
- 多处调用 `cleanup()`，需保证该方法**幂等**，避免重复清理报错。

**六、try-catch块**

在CompletableFuture.runAsync()中，最外层包裹一个try-catch块，捕获代码里没有捕获的异常，并终止http长连接。

在核心代码处包裹一层try-catch块，用于设置智能体状态为ERROR，并向前端推送，然后终止http长连接，由于这里用emitter.send方法推送，所以又包裹一层try-catch块，用于防止这里也出错，无法终止连接。

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