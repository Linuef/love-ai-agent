package com.yupi.yuaiagent.demo.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class TranslateInstanceTest {
    @Resource
    TranslateInstance translateInstance;

    @Test
    void translate() {
        translateInstance.translate("hello","en","zh");
    }
}