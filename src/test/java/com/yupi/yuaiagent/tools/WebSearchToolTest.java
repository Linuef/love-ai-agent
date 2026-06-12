package com.yupi.yuaiagent.tools;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class WebSearchToolTest {
    @Value("${search-api.api-key}")
    String apiKey;

    @Test
    void search() {
        WebSearchTool tool = new WebSearchTool(apiKey);
        String result = tool.searchWeb("以撒的结合wiki：https://isaac.huijiwiki.com/wiki/%E9%A6%96%E9%A1%B5");
        System.out.println(result);
    }

}