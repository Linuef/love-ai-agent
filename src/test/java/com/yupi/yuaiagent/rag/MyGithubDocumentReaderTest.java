package com.yupi.yuaiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GitHub;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@Slf4j
@SpringBootTest
class MyGithubDocumentReaderTest {
    @Value("${myapp.github.token}")
    private String githubToken;
    @Test
    void read() {
        MyGithubDocumentReader myGithubDocumentReader = MyGithubDocumentReader.builder()
                .githubToken(githubToken)
                .owner("alibaba")
                .repo("spring-ai-alibaba")
                .branch("main")
                .path("README.md")
                .build();
        List<Document> documents = myGithubDocumentReader.read();
        for (Document document : documents) {
            System.out.println("文档内容: " + document.getText());
            System.out.println("文档元数据: " + document.getMetadata());
        }
    }
}