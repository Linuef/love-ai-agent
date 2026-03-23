package com.yupi.yuaiagent.demo.invoke;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Spring AI 框架调用 AI大模型
 */
/*@Component*/
public class SpringAiAiInvoke implements CommandLineRunner {
    @Resource
    public ChatModel dashscopeChatModel;
    /*MessageChatMemoryAdvisor*/


    

    @Override
    public void run(String... args) throws Exception {
        AssistantMessage assistantMessage = dashscopeChatModel.call(new Prompt("怎么亲嘴"))
                .getResult()
                .getOutput();
        System.out.println(assistantMessage.getText());
    }
}
