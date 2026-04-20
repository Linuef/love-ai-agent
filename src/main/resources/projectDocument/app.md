# AI应用的核心原理

## LoveApp

### 构造方法

用构造器注入的方式，注入了两个bean，一个是ChatModel dashscopeChatModel，这个不用自己实现，spring提供了默认实现，另一个是ChatMemory chatMemory，这个我自己实现了，在config包下的配置类AiConfig下，实现了一个bean，MessageWindowChatMemory，这个类的构造方法用private修饰了，但内置了build方法和Builder类，Builder类里的maxMessages()方法可以指定最大存储的对话轮数,ChatClient类的advisors方法可以指定会话id

### doChat方法

这个方法需要注意的是ChatClient类的advisors方法，ChatMemory类没有RETHIEVE_SIZE_KEY这个常量，因此可以通过其他方法（MessageWindowChatMemory）来实现。

功能：通过chatClient，来与AI进行交互

### doChatReport方法

这个方法用了Spring AI的特性：结构化输出

生成恋爱报告，代码与doChat方法差不多，主要增加了以下代码：

1. 修改了系统预设，往系统预设里增加了这句话："每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表，建议列表中每个建议占一行"，以便更好的结构化输出

2. 在LoveApp类创建了一个record类，LoveReport，在doChatReport方法中，运用ChatClient的 链式编程：“.entity(LoveReport.class)”，让AI输出恋爱报告

### doChatWithRag方法
这个方法用了Spring AI的特性：RAG问答拦截器,实现方式与doChat方法差不多，主要是拦截器的实现不同，拦截器的实现类是loveAppRagCloudAdvisor类，loveAppVectorStore类，pgvectorVectorStore类，
一个是基于云知识库服务(阿里云)的RAG问答拦截器，一个是基于本地知识库的向量存储，一个是基于pgvector数据库的向量存储。

用QueryRewriter类的doQueryRewrite方法对传入查询进行了重写。
