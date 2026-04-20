package com.yupi.yuaiagent.rag;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;

public class LoveAppContextualQueryAugmenterFactory {
    /**
     * 创建上下文查询增强器实例
     * @return
     */
    public static ContextualQueryAugmenter createInstance() {
        PromptTemplate emptyContextPromptTemplate = PromptTemplate.builder()
                .template("""
                        你应该输出下面的内容：
                        抱歉，我只能回答恋爱相关的问题，别的没办法帮到您哦，
                        有问题可以联系编程导航客服 https://codefather.cn
                        """)
                .build();
        ContextualQueryAugmenter contextualQueryAugmenter = ContextualQueryAugmenter.builder()
                .allowEmptyContext(false)
                .emptyContextPromptTemplate(emptyContextPromptTemplate)
                .build();
        return contextualQueryAugmenter;
    }
}
