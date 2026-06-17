这个包下的类，是专门用于”工具调用“的类。



# FileOperationTool

这个类的作用，是读取项目根目录下的tmp目录里的内容，以及往里面写入内容





# WebSearchTool

这个类的作用是“联网搜索”，用的api是：[SearchApi](https://www.searchapi.io/) 

搜索引擎是baidu      

代码是把接口文档发给ai，让ai给我写的

在application-local.yml中配置了这个api的api-key

![image-20260608133217537](C:\Users\20496\AppData\Roaming\Typora\typora-user-images\image-20260608133217537.png)







# TerminalOperationTool

这个类的作用是操作终端。



这行代码：ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);的“cmd.exe”表明，操作的是Windows操作系统的命令，如果输入了Linux系统的命令(比如 ls)，会出错。



用StringBuilder拼接执行结果的原因是，在读取执行结果时，是一行一行的读，需要拼接很多次数据。







# WebScrapingToolTest

这个类的作用是抓取网页内容

引入了Jsoup库，用来抓取网页内容：

```xml
<!-- 抓取网页内容 -->
<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
    <version>1.19.1</version>
</dependency>
```



# ResourceDownloadTool

这个类的作用是把网络资源下载到本地

用的是hutool包下的HttpUtil工具类



# PDFGenerationTool

用给定资源生成PDF





因为 **PdfWriter、PdfDocument、Document** 都是**需要手动关闭的资源**。

如果不关闭，会导致：

- 文件一直被占用
- 内存泄漏
- 下次无法覆盖写入

**Java 规定：必须安全关闭的资源，必须放在 try () 里**



这个代码的结构是：

```java
// 外层大 try（捕获所有异常）
try {
        // 内层 try（只负责自动关闭资源，不处理异常）
        try (资源) {
            操作...
        }
} catch (IOException e) {
        // 所有异常都在这里捕获
    }

/*
1. 里面的 try () {} 叫【try-with-resources】
2. 它是 Java 7+ 的语法糖，自带自动关闭功能，不需要写 catch！
3. 它的作用：自动关流，防止资源泄漏
*/
```





# ToolRegistration

1. 工厂模式：allTools() 方法作为一个工厂方法，负责创建和配置多个工具实例，然后将它们包装成统一的数组返回。这符合工厂模式的核心思想 - 集中创建对象并隐藏创建细节。
2. 依赖注入模式：通过 `@Value` 注解注入配置值，以及将创建好的工具通过 Spring 容器注入到需要它们的组件中。
3. 注册模式：该类作为一个中央注册点，集中管理和注册所有可用的工具，使它们能够被系统其他部分统一访问。
4. 适配器模式的应用：ToolCallbacks.from 方法可以看作是一种适配器，它将各种不同的工具类转换为统一的 ToolCallback 数组，使系统能够以一致的方式处理它们。

有了这个注⁠册类，如果需要添加‌或移除工具，只需修改这一个类即可，更利‎于维护。



# GetCurrentTimeTool

获取当前时间的工具类



# TerminateTool

终止工具，作用是让自主规划智能体能够合理的中断。
