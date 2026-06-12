package com.yupi.yuaiagent.config;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class AiConfigTest {
    @Resource
    VectorStore pgVectorVectorStore;


    @Test
    void pgvectorVectorStore() {
        List<Document> documents = List.of(
                new Document("鱼皮喜欢在编程导航学编程", Map.of("meta1", "meta1")),
                new Document("编程导航  codefather.com"),
                new Document("鱼皮是个比较帅气的人", Map.of("meta2", "meta2")));

        pgVectorVectorStore.add(documents);

        List<Document> results = pgVectorVectorStore.similaritySearch(SearchRequest.builder().query("怎么学编程").topK(3).build());
        Assertions.assertNotNull(results);
    }

    @Test
    void testSupplier() {
        Supplier<String> supplier = () -> "hello world";
        System.out.println(supplier.get());
        Assertions.assertEquals("hello world", supplier.get());
    }
}