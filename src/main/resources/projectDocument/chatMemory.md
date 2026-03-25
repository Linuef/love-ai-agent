# 自定义ChatMemory

## 阅读源码

我阅读了MesageWindowChatMemory的源码，发现操作能保存多少轮会话记录的方法是process方法，实现原理很简单：

```
Set<Message> memoryMessagesSet = new HashSet<>(memoryMessages);
boolean hasNewSystemMessage = newMessages.stream()
    .filter(SystemMessage.class::isInstance)
    .anyMatch(message -> !memoryMessagesSet.contains(message));
    
这一段源码的功能，是查看新的会话记录中，是否有新的不同于旧的系统消息
```
 


```
memoryMessages.stream()
    .filter(message -> !(hasNewSystemMessage && message instanceof SystemMessage))
    .forEach(processedMessages::add);
    在这段源码中，当新的会话记录有新的不同于旧的系统消息时，删去旧的系统消息，只保留新的系统消息和旧的用户消息
```





```
int messagesToRemove = processedMessages.size() - this.maxMessages;

List<Message> trimmedMessages = new ArrayList<>();
int removed = 0;
for (Message message : processedMessages) {
    if (message instanceof SystemMessage || removed >= messagesToRemove) {
       trimmedMessages.add(message);
    }
    else {
       removed++;
    }
}

return trimmedMessages;

当新消息和旧消息整合到一起后，如果大于最大会话数，就需要删除消息，特殊的是，系统消息不在“待删除队列”
```



```
Spring AI对MessageWindowChatMemory的实现做了优化，其中，增删查的功能，被整合到哦ChatMemoryRepository类中，当需要调用增删查功能时，调用ChatMemoryRepository的api就行
```



## FileBasedChatMemory

在这个ChatMemory中，ChatMemoryRepository类的功能被getConversationFile方法替代，会话被持久化到本地文件中。

序列化用的是Kryo序列化库[EsotericSoftware/kryo: Java binary serialization and cloning: fast, efficient, automatic](https://github.com/EsotericSoftware/kryo)

这个类写到config包下的配置类中，被注册为bean

```：
getConversationFile：得到本地会话文件的File类
saveConversation： 持久化会话到本地文件
getOrCreateConversation：得到本地会话文件
```

```java
static {
    kryo.setRegistrationRequired(false);
    kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
}
这段代码用于灵活的在kryo注册类，我不了解具体原理
```



```
public FileBasedChatMemory(String dir) {
    this.BASE_DIR = dir;
    File baseDir = new File(BASE_DIR);
    if(!baseDir.exists()){
        baseDir.mkdirs();
    }
}
baseDir.mkdirs();的作用是：如果baseDir的路径不存在，就递归的创建路径
```