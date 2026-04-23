package com.yupi.yuaiagent.rag;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;
import org.springframework.ai.rag.preretrieval.query.expansion.QueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.retrieval.join.ConcatenationDocumentJoiner;
import org.springframework.ai.rag.retrieval.join.DocumentJoiner;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.Assert;
import reactor.core.scheduler.Scheduler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class MyRetrievalAugmentationAdvisor implements BaseAdvisor {
    private static final String DOCUMENT_CONTEXT = "rag_document_context";

    private final List<QueryTransformer> queryTransformers;

    @Nullable
    private final QueryExpander queryExpander;

    private final DocumentJoiner documentJoiner;

    private final DocumentRetriever documentRetriever;

    private final QueryAugmenter queryAugmenter;

    private final TaskExecutor taskExecutor;

    private final Scheduler scheduler;

    private final int order;


    private MyRetrievalAugmentationAdvisor(@Nullable List<QueryTransformer> queryTransformers,
            @Nullable QueryExpander queryExpander, @Nullable DocumentJoiner documentJoiner,
            DocumentRetriever documentRetriever, @Nullable QueryAugmenter queryAugmenter,
            @Nullable TaskExecutor taskExecutor, @Nullable Scheduler scheduler,
            @Nullable Integer order) {
        Assert.notNull(documentRetriever, "DocumentRetriever must not be null");
        Assert.noNullElements(queryTransformers, "QueryTransformers cannot contain null elements");
        this.documentRetriever = documentRetriever;
        this.queryTransformers = queryTransformers != null ? queryTransformers : List.of();
        this.queryExpander = queryExpander;
        this.documentJoiner = documentJoiner != null ? documentJoiner : new ConcatenationDocumentJoiner();
        this.queryAugmenter = queryAugmenter != null ? queryAugmenter : ContextualQueryAugmenter.builder().build();
        this.taskExecutor = taskExecutor != null ? taskExecutor : buildDefaultTaskExecutor();
        this.scheduler = scheduler != null ? scheduler : BaseAdvisor.DEFAULT_SCHEDULER;
        this.order = order != null ? order : 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest,@Nullable AdvisorChain advisorChain) {
        HashMap<String, Object> context = new HashMap<>(chatClientRequest.context());

        Query originalQuery = Query.builder()
                .text(chatClientRequest.prompt().getUserMessage().getText())
                .history(chatClientRequest.prompt().getInstructions())
                .context(context)
                .build();

        Query transformedQuery = originalQuery;
        for (var queryTransformer : this.queryTransformers) {
            transformedQuery = queryTransformer.apply(transformedQuery);
        }

        List<Query> expandedQuery = this.queryExpander != null ? this.queryExpander.apply(transformedQuery)
                : List.of(transformedQuery);

        Map<Query, List<List<Document>>> documentsForQuery = expandedQuery.stream()
                .map(query -> CompletableFuture.supplyAsync(() -> getDocumentsForQuery(query)))
                .toList()
                .stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> List.of(entry.getValue())));

        List<Document> documents = documentJoiner.join(documentsForQuery);

        context.put(DOCUMENT_CONTEXT, documents);

        Query augmentedQuery = this.queryAugmenter.augment(originalQuery, documents);
        //这里创建了一个新的chatClientRequest
        return chatClientRequest.mutate()
                //保留之前的系统消息等一系列消息，仅仅替换增强后的用户消息
                .prompt(chatClientRequest.prompt().augmentUserMessage(augmentedQuery.text()))
                .context(context)
                .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse,@Nullable AdvisorChain advisorChain) {
        ChatResponse.Builder chatResponseBuilder;
        if(chatClientResponse.chatResponse()==null){
            chatResponseBuilder = ChatResponse.builder();
        }
        else{
            chatResponseBuilder = ChatResponse.builder().from(chatClientResponse.chatResponse());
        }
        chatResponseBuilder.metadata(DOCUMENT_CONTEXT, chatClientResponse.context().get(DOCUMENT_CONTEXT));
        return chatClientResponse.mutate()
                .chatResponse(chatResponseBuilder.build())
                .context(chatClientResponse.context())
                .build();
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public Scheduler getScheduler() {
        return this.scheduler;
    }

    private Map.Entry<Query, List<Document>> getDocumentsForQuery(Query query){
        List<Document> documents = this.documentRetriever.retrieve(query);
        return Map.entry(query, documents);
    }

    private static TaskExecutor buildDefaultTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix("ai-advisor-");
        taskExecutor.setCorePoolSize(4);
        taskExecutor.setMaxPoolSize(16);
        taskExecutor.setTaskDecorator(new ContextPropagatingTaskDecorator());
        taskExecutor.initialize();
        return taskExecutor;
    }

    public static class Builder {
        private List<QueryTransformer> queryTransformers;

        private QueryExpander queryExpander;

        private DocumentRetriever documentRetriever;

        private DocumentJoiner documentJoiner;


        private QueryAugmenter queryAugmenter;

        private TaskExecutor taskExecutor;

        private Scheduler scheduler;

        private Integer order;

        public Builder queryTransformers(List<QueryTransformer> queryTransformers) {
            this.queryTransformers = queryTransformers;
            return this;
        }

        public Builder queryExpander(QueryExpander queryExpander) {
            this.queryExpander = queryExpander;
            return this;
        }

        public Builder documentRetriever(DocumentRetriever documentRetriever) {
            this.documentRetriever = documentRetriever;
            return this;
        }

        public Builder documentJoiner(DocumentJoiner documentJoiner) {
            this.documentJoiner = documentJoiner;
            return this;
        }


        public Builder queryAugmenter(QueryAugmenter queryAugmenter) {
            this.queryAugmenter = queryAugmenter;
            return this;
        }

        public Builder taskExecutor(TaskExecutor taskExecutor) {
            this.taskExecutor = taskExecutor;
            return this;
        }

        public Builder scheduler(Scheduler scheduler) {
            this.scheduler = scheduler;
            return this;
        }

        public Builder order(Integer order) {
            this.order = order;
            return this;
        }

        public MyRetrievalAugmentationAdvisor build() {
            return new MyRetrievalAugmentationAdvisor(queryTransformers, queryExpander,
                    documentJoiner, documentRetriever,
                    queryAugmenter, taskExecutor,
                    scheduler, order);
        }

    }

}
