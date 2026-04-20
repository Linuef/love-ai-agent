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



