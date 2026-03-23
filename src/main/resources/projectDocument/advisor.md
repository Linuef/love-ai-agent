# 自定义拦截器

## MyLoggerAdvisor

我把spring中SimpleLoggerAdvisor的源码粘贴到了MyLoggerAdvisor中，然后做了 一些修改，把日志级别修改为info，把源码中的logger变量用注解@Slf4j代替



## ReReadingAdvisor

我阅读了spring的官方文档，文档中给出了ReReadingAdvisor的例子，文档链接为：[Advisors API :: Spring AI Reference](https://docs.spring.io/spring-ai/reference/api/advisors.html#_implementing_an_advisor)，ReReadingAdvisor实现了BaseAdvisor接口，BaseAdvisor实现了流式接口和非流式接口

这个拦截器的实现我有的地方没看懂

