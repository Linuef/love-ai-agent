package com.yupi.yuaiagent.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenChatModel;

/**
 * langchain4j框架调用 AI
 */
public class LangChainAiInvoke {
    public static void main(String[] args) {
        QwenChatModel qwenChatModel = QwenChatModel.builder()
                .apiKey(TestApiKey.API_KEY)
                .modelName("qwen-plus")
                .build();
        String answer = qwenChatModel.chat("我是程序员鱼皮，这是codefather.cn的原创项目 AI超级智能体");
        System.out.println(answer);
    }
}
