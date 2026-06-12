package com.yupi.yuimagesearchmcpserver.tools;

import cn.hutool.core.lang.Assert;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class ImageSearchToolTest {
    @Resource
    private ImageSearchTool imageSearchTool;

    @Test
    void searchImage() {
        String searchImage = imageSearchTool.searchImage("小猫");
        Assert.notNull(searchImage, "searchImage is null");
    }
}