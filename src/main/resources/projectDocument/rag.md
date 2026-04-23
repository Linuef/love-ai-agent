# RAG包下的类

## LoveAppDocumentLoader

这个类主要功能是，读取本地文档，然后把这些文档切片并返回。

1.ResourcePatternResolver接口实现了getResources方法，返回Resources[],用于读取资源文件

2.这个类中，for循环里的代码参考了spring ai的文档：[ETL Pipeline :: Spring AI Reference](https://docs.spring.io/spring-ai/reference/api/etl-pipeline.html#_documentreaders)，spring ai提供了ETL（文档的读取，转换和存储）的一系列功能，其中就有读取各种文件格式（JSON，Text，Markdown）的功能。

3.String status = fileName.substring(fileName.length()-6,fileName.length()-4);   这行代码通过截取文件名来定义文档的元信息

## LoveAppContextualQueryAugmenterFactory

这是一个工厂，创建上下文查询增强器实例，参考官方文档：[ContextualQueryAugmenter]([Retrieval Augmented Generation :: Spring AI Reference](https://docs.spring.io/spring-ai/reference/api/retrieval-augmented-generation.html#_contextualqueryaugmenter))

核心方法是createInstance，创建了空上下文模板emptyContextPromptTemplate，当不允许空上下文且查不到文档时，就会应用这个模板

## QueryRewriter

核心方法是DoQueryRewrite，使用大语言模型对用户的原始查询进行改写，使其更加清晰和详细



## MyKeywordEnricher

核心方法是enrichDocuments，对传入的文档列表增加元信息，参考官方文档：[MyKeywordEnricher]([ETL Pipeline :: Spring AI Reference](https://docs.spring.io/spring-ai/reference/api/etl-pipeline.html#_keywordmetadataenricher))



## LoveAppRagCustomAdvisorFactory

这是一个工厂，创建RetrievalAugmentationAdvisor，可根据自身需要设定过滤条件，相似度阈值，最终的文档数量，向量数据库。

指定了查询增强器ContextualQueryAugmenter。

参考官方文档：[RetrievalAugmentationAdvisor]([Retrieval Augmented Generation :: Spring AI Reference](https://docs.spring.io/spring-ai/reference/api/retrieval-augmented-generation.html#_retrievalaugmentationadvisor))



## MyGithubDocumentReader

这个类的功能是读取github仓库里的文件，用于RAG，用alibaba提供的[GithubDocumentReader]([Document Reader 使用指南 | Spring AI Alibaba](https://java2ai.com/integration/rag/document-readers#支持的扩展实现))实现，[maven中央仓库的地址]([Maven Repository: com.alibaba.cloud.ai » spring-ai-alibaba-starter-document-reader-github](https://mvnrepository.com/artifact/com.alibaba.cloud.ai/spring-ai-alibaba-starter-document-reader-github))



解析器parser用的是阿里云的[TikaDocumentParser]([Document Parser 使用指南 | Spring AI Alibaba](https://java2ai.com/integration/rag/document-parsers#示例-5与-document-reader-配合使用))  ,  [maven中央仓库的地址]([Maven Repository: com.alibaba.cloud.ai » spring-ai-alibaba-starter-document-parser-tika](https://mvnrepository.com/artifact/com.alibaba.cloud.ai/spring-ai-alibaba-starter-document-parser-tika))



你需要传入的参数是：你的github账号的token，仓库的所有者owner，仓库的名字repo，仓库的分支branch，仓库的路径path，然后开始调用方法。

注意：你传入的github账号的token，在MyGithubDocumentReader的内部类Builder中，当调用build()方法后，会根据githubToken自动创建Github类，因此你无需手动创建Github类，只需传入githubToken，如下图：

![image-20260421175357610](C:\Users\20496\AppData\Roaming\Typora\typora-user-images\image-20260421175357610.png)

如果你的path最终指向的是一个文件，那么调用read方法，如果指向的是一个文件夹，那么调用readList方法。

注意：GHContent是原始数据，很复杂，可以是目录，也可以是单纯的文件，在GithubResource类中经过递归方法scanDirectory，把目录里的文件创建成一个个的GithubResource，GithubResource是单个文件的资源。





## MyTranslationQueryTransformer

调用腾讯云的机器翻译api创建了一个可以翻译语言的类，具体过程我也不太了解，参考[官方文档]([SDK 中心 Java_腾讯云](https://cloud.tencent.com/document/sdk/Java))。

我当时是把官方文档复制给ai，然后让ai搜索腾讯云机器翻译的api，自动给我补全我写的代码。

自定义 Q⁠ueryTransforme‌r 查询转换器（其实我也没实现QueryTransformer接口，我是可以实现的，但我觉得太麻烦了，遇到具体情况再说吧），利用第三方腾讯云翻译 API 代替 Spr‎ing AI 内置的基于大模‌型的翻译工具，从而降低成本。

## MyRetrievalAugmentationAdvisor

这个类是我看了RetrievalAugmentationAdvisor类的源码后，自己比着源码抄过来的，虽然是抄过来的，但我也加深了对于RAG的理解。

源码的核心方法是before()方法。

成员变量scheduler在before()和after()方法中没用到，只是重写了他的返回方法。

RetrievalAugmentationAdvisor类很灵活，支持传入QueryTransformer,QueryExpander,QueryAugmenter,DocumentPostProcessor,DocumentJoiner,DocumentRetriever（DocumentPostProcessor我没学过，因此没写进我的代码）

以上那些组件在before()方法中都有应用。





此外，在检索文档时，用了异步线程的方法，同时开启多个线程去检索文档，提高检索的效率，代码在下面：

```java
// 3. Get similar documents for each query.
Map<Query, List<List<Document>>> documentsForQuery = expandedQueries.stream()
    .map(query -> CompletableFuture.supplyAsync(() -> getDocumentsForQuery(query), this.taskExecutor))
    .toList()
    .stream()
    .map(CompletableFuture::join)
    .collect(Collectors.toMap(Map.Entry::getKey, entry -> List.of(entry.getValue())));

/*
假设stream流有三个元素1，2，3
元素1先到达终止方法，到达终止方法后，中间操作才开始执行（这就是stream流的惰性），元素2和3以此类推。
因此对于上面的代码，要想实现并行检索，必须在两个.map方法间加个终止方法，不然就会变成串行检索，也就是说：
必须把“启动任务”和“等待结果”拆分成两个独立的流操作（通过中间落地到 List 实现），这样才能实现先全部发射，再统一回收

*/
```
