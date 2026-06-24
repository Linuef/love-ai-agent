package com.yupi.yuaiagent.tools;

import cn.hutool.core.lang.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class PDFGenerationToolTest {
    @Test
    void generatePDF() {
        PDFGenerationTool tool = new PDFGenerationTool();
        String result = tool.generatePDF("test.pdf", "你好");
        Assert.notNull(result);
    }
}