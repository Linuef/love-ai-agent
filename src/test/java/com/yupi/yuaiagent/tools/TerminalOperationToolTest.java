package com.yupi.yuaiagent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class TerminalOperationToolTest {
    @Test
    void executeCommand() {
        TerminalOperationTool tool = new TerminalOperationTool();
        String result = tool.executeTerminalCommand("ls");
        System.out.println(result);
    }

}