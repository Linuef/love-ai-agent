package com.yupi.yuaiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class MyKeywordEnricherTest {
    @Resource
    ChatModel dashscopeChatModel;
    @Test
    void ecrichD(){
        ChatModel chatModel = dashscopeChatModel;
                KeywordMetadataEnricher enricher = KeywordMetadataEnricher.builder(chatModel)
                .keywordCount(3)
                .build();

        Document doc = new Document("i like play bastetball,ping-pong,run");

        List<Document> enrichedDocs = enricher.apply(List.of(doc));

        Document enrichedDoc = enrichedDocs.get(0);
        String keywords = (String) enrichedDoc.getMetadata().get("excerpt_keywords");
        System.out.println("Extracted keywords: " + keywords);
    }

}