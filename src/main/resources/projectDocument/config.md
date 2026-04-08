# 配置类

这个类目前的主要功能是把我自定义的类注册为bean，然后自动注入。

### ChatMemory类型
MessageWindowChatMemory类，FileBasedChatMemory类
### loveAppVectorStore类
基于本地知识库的向量存储，通过loveAppDocumentLoader类读取文档并切片，然后存入向量数据库
### loveAppRagCloudAdvisor类
基于云知识库服务的RAG问答拦截器， spring ai提供了DocumentRetriever接口，
spring ai albaba用DashScopeDocumentRetriever类实现了这个接口，用于从阿里云百炼平台的知识库中检索文档。
