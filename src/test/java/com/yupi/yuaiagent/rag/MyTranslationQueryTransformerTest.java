package com.yupi.yuaiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.rag.Query;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class MyTranslationQueryTransformerTest {
    @Resource
    MyTranslationQueryTransformer myTranslationQueryTransformer;

    @Test
    void translate() {
        String message = "I like Li Mengting";
        String result = myTranslationQueryTransformer.translate(message,"en","zh");
        System.out.println(result);
    }
}