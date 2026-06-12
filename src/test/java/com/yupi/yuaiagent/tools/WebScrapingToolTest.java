package com.yupi.yuaiagent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class WebScrapingToolTest {
    @Test
    void scrape() {
        WebScrapingTool tool = new WebScrapingTool();
        String result = tool.scrapeWebPage("https://www.codefather.cn");
        System.out.println(result);
    }
}