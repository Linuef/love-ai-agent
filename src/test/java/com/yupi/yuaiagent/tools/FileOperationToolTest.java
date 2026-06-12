package com.yupi.yuaiagent.tools;

import cn.hutool.core.lang.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class FileOperationToolTest {
    @Test
    void read(){
        FileOperationTool fileOperationTool = new FileOperationTool();
        String content = fileOperationTool.readFile("以撒的结合.txt");
        Assert.notNull(content, "readFile error");
        System.out.println(content);
    }
    @Test
    void write(){
        FileOperationTool fileOperationTool = new FileOperationTool();
        String result = fileOperationTool.writeFile("以撒的结合.txt", "https://isaac.huijiwiki.com/wiki/%E9%A6%96%E9%A1%B5");
        Assert.notNull(result, "writeFile error");
    }

}