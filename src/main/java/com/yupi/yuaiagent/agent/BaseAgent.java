package com.yupi.yuaiagent.agent;

import cn.hutool.core.util.StrUtil;
import com.yupi.yuaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.StringUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 智能体基类，定义基本信息和多步骤执行流程
 */
@Data
@Slf4j
public abstract class BaseAgent {
    //核心属性
    private String name;


    //提示词
    private String systemPrompt;
    private String nextStepPrompt;

    //代理状态
    private AgentState state = AgentState.IDLE;

    //执行步骤控制
    private int maxSteps = 10;
    private int currentStep = 0;

    //LLM大模型
    private ChatClient chatClient;

    //Memory 记忆（需要自主维护上下文）
    private List<Message> messageList = new ArrayList<>();

    /**
     * 运行代理
     * @param userPrompt 用户输入的提示词
     * @return 代理执行结果的字符串
     */
    public String run(String userPrompt) {
        //1.基础校验
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Cannot run agent from state: " + this.state);
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }

        //2.执行，更改状态
        this.state = AgentState.RUNNING;
        //记录消息上下文
        messageList.add(new UserMessage(userPrompt));
        //保存结果列表

        List<String> results = new ArrayList<>();
        try {
            //执行循环
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("Executing step " + stepNumber + "/" + maxSteps);
                //单步执行
                String stepResult = step();
                String result = "Step " + stepNumber + ": " + stepResult;
                results.add(result);
            }
            //检查是否超出步骤限制
            if (currentStep >= maxSteps) {
                state = AgentState.FINISHED;
                results.add("Terminated: Reached max steps (" + maxSteps + ")");
            }
            return String.join("\n", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Error executing agent", e);
            return "执行错误" + e.getMessage();
        } finally {
            //3.清理资源
            this.cleanup();
        }
    }
    /**
     * 运行代理(异步SSE流式输出)
     * 基于SseEmitter实现长连接，异步分步执行智能体逻辑并实时推送结果到前端
     * @param userPrompt 用户输入的提示词
     * @return SseEmitter SSE长连接对象，用于持续推送流式数据
     */
    public SseEmitter runStream(String userPrompt) {
        // 创建SSE发射器，设置超时时间：300000毫秒 = 5分钟，超时后框架自动关闭连接
        SseEmitter emitter = new SseEmitter(300000L);

        // 异步执行智能体核心逻辑，避免阻塞Tomcat请求线程
        CompletableFuture.runAsync(() -> {
            try {
                // ====================== 1. 前置基础校验 ======================
                // 校验代理状态：非空闲状态禁止重复执行
                if (this.state != AgentState.IDLE) {
                    emitter.send("错误：无法从状态运行代理: " + this.state);
                    emitter.complete();
                    return;
                }
                // 校验入参：禁止空提示词
                if (StrUtil.isBlank(userPrompt)) {
                    emitter.send("错误：不能使用空提示词运行代理");
                    emitter.complete();
                    return;
                }

                // ====================== 2. 初始化执行状态 ======================
                // 修改代理状态为运行中，防止并发重复调用
                this.state = AgentState.RUNNING;
                // 保存用户提问至消息上下文，用于会话记录
                messageList.add(new UserMessage(userPrompt));

                try {
                    // ====================== 3. 循环分步执行智能体 ======================
                    // 循环执行任务：未达到最大步数 且 代理未主动结束
                    for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                        int stepNumber = i + 1;
                        currentStep = stepNumber;
                        log.info("Executing step " + stepNumber + "/" + maxSteps);

                        // 执行单步智能体业务逻辑
                        String stepResult = step();
                        String result = "Step " + stepNumber + ": " + stepResult;
                        // 将单步结果实时推送给前端
                        emitter.send(result);
                    }

                    // 判定执行终止原因：达到最大执行步数
                    if (currentStep >= maxSteps) {
                        state = AgentState.FINISHED;
                        emitter.send("执行结束: 达到最大步骤 (" + maxSteps + ")");
                    }
                    // 所有步骤执行完毕，正常关闭SSE连接
                    emitter.complete();

                } catch (Exception e) {
                    // 捕获业务执行异常，标记代理为错误状态
                    state = AgentState.ERROR;
                    log.error("执行智能体失败", e);
                    try {
                        // 向前端推送异常信息并关闭连接
                        emitter.send("执行错误" + e.getMessage());
                        emitter.complete();
                    } catch (IOException ex) {
                        // 推送失败时，以异常状态终止SSE连接
                        emitter.completeWithError(ex);
                    }
                } finally {
                    // ====================== 4. 统一资源清理 ======================
                    // 无论正常/异常，执行完毕都清理临时资源
                    this.cleanup();
                }
            } catch (IOException e) {
                // 兜底异常：SSE消息发送失败时，以异常终止连接
                emitter.completeWithError(e);
            }
        });

        // SSE连接超时回调：连接超过5分钟无交互时触发
        emitter.onTimeout(() -> {
            this.state = AgentState.FINISHED;
            this.cleanup();
            log.warn("SSE connection timed out");
        });

        // SSE连接结束回调：正常关闭、异常关闭、超时、前端断连都会触发
        emitter.onCompletion(() -> {
            // 兜底修正状态：连接断开时若仍处于运行中，强制标记为已结束
            if (this.state == AgentState.RUNNING) {
                this.state = AgentState.FINISHED;
            }
            // 最终兜底资源清理
            this.cleanup();
            log.info("SSE connection completed");
        });

        // 返回SSE发射器，由Spring维持长连接
        return emitter;
    }



    /**
     * 定义单个步骤
     * @return
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void cleanup() {

    }
}
