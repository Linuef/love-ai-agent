# RAG核心功能

## LoveAppDocumentLoader

这个类主要功能是，读取本地文档，然后把这些文档切片并返回。

这个类中，for循环里的代码参考了spring ai的文档：[ETL Pipeline :: Spring AI Reference](https://docs.spring.io/spring-ai/reference/api/etl-pipeline.html#_documentreaders)，spring ai提供了ETL（文档的读取，转换和存储）的一系列功能，其中就有读取各种文件格式（JSON，Text，Markdown）的功能。