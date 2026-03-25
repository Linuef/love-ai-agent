package com.yupi.yuaiagent.config;

import com.yupi.yuaiagent.chatMemory.FileBasedChatMemory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class AiConfig {

    @Bean
    public MessageWindowChatMemory MessageWindowChatMemory(){
        return MessageWindowChatMemory.builder()
                .maxMessages(10)
                .build();
    }
    @Bean
    public FileBasedChatMemory fileBasedChatMemory(){
        String fileDir = System.getProperty("user.dir") + "/chatMemory";
        return new FileBasedChatMemory(fileDir);
    }

}
