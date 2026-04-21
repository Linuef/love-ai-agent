package com.yupi.yuaiagent.app;

import cn.hutool.core.lang.UUID;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class LoveAppTest {
    @Resource
    private LoveApp loveApp;

    @Test
    void doChat() {
        String chatId = UUID.randomUUID().toString();

        String message = "你好，我是程序员鱼皮";
        String answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);

        message = "我想让另一半（编程导航）更爱我";
        answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);

        message = "我的另一半叫什么来着？我刚跟你说过，给我回忆一下";

        answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatReport() {
        String chatId = UUID.randomUUID().toString();
        LoveApp.LoveReport loveReport = loveApp.doChatReport("你好，我是程序员鱼皮,我想让另一半（编程导航）更爱我,请给我一些建议", chatId);
        Assertions.assertNotNull(loveReport);
        Assertions.assertNotNull(loveReport.title());
        Assertions.assertNotNull(loveReport.suggestions());
    }

    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String answer = loveApp.doChatWithRag("My relationship after marriage is not very good. Is there any way to solve this problem", chatId);
        Assertions.assertNotNull(answer);
    }
}