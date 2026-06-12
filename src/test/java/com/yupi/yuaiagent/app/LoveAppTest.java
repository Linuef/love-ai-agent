package com.yupi.yuaiagent.app;

import cn.hutool.core.lang.Assert;
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
        String answer = loveApp.doChatWithRag("我婚后关系不太好，有什么解决办法吗", chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void df(){
        float x = 16777216.0f;
        float y = 0.5f;
        System.out.println(x+y);
    }

    @Test
    void doChatWithTools() {
        //测试联网搜索问题的答案 完成
//        testMessage("周末想带女朋友去上海约会，推荐几个适合情侣的小众打卡地？(请使用联网搜索功能)");

        //测试网页抓取：恋爱案例分析 完成
//        testMessage("最近和对象吵架了，看看编程导航网站（codefather.cn）的其他情侣是怎么解决矛盾的？(使用网页抓取功能)");

        //测试资源下载：图片下载  完成
//        testMessage("直接下载一张适合做手机壁纸的星空情侣图片为文件");

        //测试终端操作：执行代码
//        testMessage("执行 Python3 脚本来生成数据分析报告");

        //测试文件操作：保存用户档案 完成
//        testMessage("保存我的恋爱档案为文件(使用FileOperationTool,档案内容为空也要保存)");

        //测试PDF生成  完成
//        testMessage("生成一份‘七夕约会计划’PDF，包含餐厅预订、活动流程和礼物清单");

        testMessage("我想获取当前时间");
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = loveApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithMcp() {
        String chatId = UUID.randomUUID().toString();

        /*String message = "我的另一半居住在上海静安区，请帮我找到 5 公里内合适的约会地点";
        String answer =  loveApp.doChatWithMcp(message, chatId);*/

        String message = "帮我找个哄另一半开心的图片";
        String answer =  loveApp.doChatWithMcp(message, chatId);
        Assert.notNull(answer);
    }
}