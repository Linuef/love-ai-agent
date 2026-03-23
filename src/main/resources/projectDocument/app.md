# AI应用的核心原理

## LoveApp

LoveApp类里有两个方法，一个是构造方法，一个是doChat方法

### 构造方法

用构造器注入的方式，注入了两个bean，一个是ChatModel dashscopeChatModel，这个不用自己实现，spring提供了默认实现，另一个是ChatMemory chatMemory，这个我自己实现了，在config包下的配置类AiConfig下，实现了一个bean，MessageWindowChatMemory，这个类的构造方法用private修饰了，但内置了build方法和Builder类，Builder类里的maxMessages()方法可以指定最大存储的对话轮数,ChatClient类的advisors方法可以指定会话id

### doChat方法

这个方法需要注意的是ChatClient类的advisors方法，ChatMemory类没有RETHIEVE_SIZE_KEY这个常量，因此可以通过其他方法（MessageWindowChatMemory）来实现。

功能：通过chatClient，来与AI进行交互

