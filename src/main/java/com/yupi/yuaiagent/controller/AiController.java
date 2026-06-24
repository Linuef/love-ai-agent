package com.yupi.yuaiagent.controller;

import com.yupi.yuaiagent.agent.YuManus;
import com.yupi.yuaiagent.app.LoveApp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/ai")
public class AiController {
    @Resource
    private LoveApp loveApp;

    @Resource
    private ToolCallback[] alltools;

    @Resource
    private ChatModel dashscopeChatModel;

    /**
     * 同步输出AI回复
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping("/love_app/chat/sync")
    public String doChatWithLoveAppSync(String message,String chatId) {
        return loveApp.doChat(message,chatId);
    }

    /**
     * 异步输出AI回复的第一种方法
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSse(String message, String chatId) {
        return loveApp.doChatByStream(message,chatId);
    }
    /**
     * 异步输出AI回复的第二种方法
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping( "/love_app/chat/server_sent_event")
    public Flux<ServerSentEvent<String>> doChatWithLoveAppServerSentEvent(String message, String chatId) {
        return loveApp.doChatByStream(message,chatId)
                .map(chunk->ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }
    /**
     * 异步输出AI回复的第三种方法
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping("/love_app/chat/sse/emitter")
    public SseEmitter doChatWithLoveAppSseEmitter(String message, String chatId) {
        SseEmitter sseEmitter = new SseEmitter(180000L);
        loveApp.doChatByStream(message,chatId)
                .subscribe(
                        // onNext：每收到一块数据
                        chunk->{
                            try {
                                sseEmitter.send(chunk);
                            } catch (IOException e) {
                                sseEmitter.completeWithError(e);
                            }
                        },
                        // onError：流出现异常
                        sseEmitter::completeWithError,
                        // onComplete：流正常结束
                        sseEmitter::complete
                );
        return sseEmitter;
    }

    /**
     * 用YuManus智能体异步输出AI的回复
     * @param message
     * @return
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        YuManus yManus = new YuManus(alltools,dashscopeChatModel);
        return yManus.runStream(message);
    }
}
