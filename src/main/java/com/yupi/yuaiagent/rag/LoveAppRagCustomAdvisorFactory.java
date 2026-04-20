package com.yupi.yuaiagent.rag;

import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Component;

/**
 * 自定义RAG拦截器工厂
 * 用于创建自定义的RAG拦截器，根据指定的状态筛选文档
 */
@Component
public class LoveAppRagCustomAdvisorFactory {
    /**
     * 创建自定义的RAG顾问
     * @param vectorStore
     * @param status
     * @return
     */
    public static Advisor create(VectorStore vectorStore, String status) {
        // 筛选状态为指定状态的文档
        Filter.Expression filterExpression = new FilterExpressionBuilder()
                .eq("status", status)
                .build();
        // 创建文档检索器
        DocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.7)
                .filterExpression(filterExpression)
                .topK(3)
                .build();
        // 创建RAG顾问
        RetrievalAugmentationAdvisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .queryAugmenter(LoveAppContextualQueryAugmenterFactory.createInstance())
                .build();
        return retrievalAugmentationAdvisor;
    }
}
