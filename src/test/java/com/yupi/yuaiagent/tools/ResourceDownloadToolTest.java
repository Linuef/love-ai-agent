package com.yupi.yuaiagent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class ResourceDownloadToolTest {
    @Test
    void download() {
        ResourceDownloadTool tool = new ResourceDownloadTool();
        tool.downloadResource("https://www.codefather.cn/logo.png", "logo.png");
    }

}