package com.yupi.yuaiagent.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import com.yupi.yuaiagent.chatMemory.FileBasedChatMemory;
import com.yupi.yuaiagent.rag.LoveAppDocumentLoader;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.util.List;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

@Configuration
public class AiConfig {
    /**
     * 基于消息窗口的聊天记忆，最多保留10条消息
     * @return
     */
    @Bean
    public MessageWindowChatMemory MessageWindowChatMemory(){
        return MessageWindowChatMemory.builder()
                .maxMessages(10)
                .build();
    }

    /**
     * 基于文件的聊天记忆，把聊天记忆保存到本地文件中
     * @return
     */
    @Bean
    public FileBasedChatMemory fileBasedChatMemory(){
        String fileDir = System.getProperty("user.dir") + "/tmp/chatMemory";
        return new FileBasedChatMemory(fileDir);
    }
    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    /**
     * 基于向量的存储，把文档转换为向量，用于相似度搜索（基于本地知识库）
     * @param dashscopeEmbeddingModel
     * @return
     */
    @Bean
    VectorStore loveAppVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel)
                .build();

        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
        simpleVectorStore.add(documents);
        return simpleVectorStore;
    }

    @Value("${spring.ai.dashscope.api-key}")
    private String dashScopeApiKey;

    /**
     * 实现了一个检索增强拦截器，结合了阿里云百炼平台的知识库进行检索增强（基于云知识库服务）
     * @return
     */
    @Bean
    public Advisor loveAppRagCloudAdvisor() {
        DashScopeApi dashScopeApi = DashScopeApi.builder().apiKey(dashScopeApiKey).build();
        // String KNOWLEDGE_INDEX = "恋爱大师";//进行恋爱问题的问答的知识库
        final String KNOWLEDGE_INDEX = "恋爱对象人员信息";//进行恋爱对象人员信息的问答的知识库
        DocumentRetriever documentRetriever = new DashScopeDocumentRetriever(dashScopeApi,
                DashScopeDocumentRetrieverOptions.builder()
                        .withIndexName(KNOWLEDGE_INDEX)
                        .build());
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .build();
    }

    /**
     * 基于PostgreSQL的向量存储，把文档转换为向量，用于相似度搜索（基于PostgreSQL数据库）(手动整合)
     * @param jdbcTemplate
     * @param dashscopeEmbeddingModel
     * @return
     */
    /*@Bean
    public VectorStore pgvectorVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel dashscopeEmbeddingModel) {
        VectorStore vectorStore = PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
                .dimensions(1024)
                .distanceType(COSINE_DISTANCE)
                .indexType(HNSW)
                .initializeSchema(true)
                .schemaName("public")
                .vectorTableName("vector_store")
                .maxDocumentBatchSize(10000)
                .build();
        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();

        vectorStore.add(documents);
        return vectorStore;
    }*/


}
