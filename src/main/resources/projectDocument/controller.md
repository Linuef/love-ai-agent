# AiController

把AI的接口暴露给前端，让接口服务化。



## doChatWithLoveAppSync

方法名有个sync(同步)，顾名思义，当AI生成好内容后，才会把内容返回给前端。

当遇到OpenManus这张有agent_loop设计模式的多轮循环智能体时，等待时间较久。



## doChatWithLoveAppSse

流式输出方法的第一种，在注解@GetMapping中加个参数：

```java
@GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
```

 **1.设置 HTTP 响应头 `Content-Type: text/event-stream`**

告诉浏览器 / 客户端：当前响应**不是普通文本 / JSON**，而是**长连接流式 SSE 数据**，需要按流模式持续接收。

**2.告知 Spring WebFlux 流式输出策略**

配合 `Flux` 时，Spring 会**不缓存完整响应**，有数据就立刻分段推送给客户端，实现**流式推送**，而非等全部数据生成后一次性返回。



## doChatWithLoveAppServerSentEvent

流式输出方法的第二种。

与第一种方法不同的是，这个方法没有在注解@GetMapping中加参数：produces = MediaType.TEXT_EVENT_STREAM_VALUE)，而是修改了返回值，把**Flux<String>**改为**Flux<ServerSentEvent<String>>**

Spring **自动识别**这个返回值**Flux<ServerSentEvent<String>>**并追加 `Content-Type: text/event-stream`，无需手动写 `produces`；



## doChatWithLoveAppSeeEmitter

**核心订阅推送逻辑**

```java
loveApp.doChatByStream(message,chatId)
    .subscribe(
        // onNext：每收到一块数据
        chunk->{
            try {
                sseEmitter.send(chunk);
            } catch (IOException e) {
                sseEmitter.completeWithError(e);
            }
        },
        // onError：流出现异常
        sseEmitter::completeWithError,
        // onComplete：流正常结束
        sseEmitter::complete
    );
```

**解释核心逻辑**

`loveApp.doChatByStream(...)` 返回 **Flux**（响应式流），AI 会分段吐出文本 `chunk`。

`subscribe` 订阅流，三个回调对应：**正常数据、异常、流结束**。

1. **onNext（分片推送）**

   - 拿到每一段 AI 文本 `chunk`，调用 `sseEmitter.send(chunk)` 实时推给前端。
   - `IOException`：常见场景：**前端关闭页面、主动断开连接、网络中断**。捕获后调用 `completeWithError`，标记连接异常终止。

2. **onError（流异常）**

   AI 调用、业务代码报错时，执行 `completeWithError(e)`，关闭连接并标记异常。

3. **onComplete（流正常结束）**

   AI 所有内容推送完毕，执行 `complete()`，**正常关闭 SSE 长连接**。

**return sseEmitter**

将 `SseEmitter` 交给 Spring MVC 框架：

- 框架不会立刻关闭 HTTP 响应；
- 持有长连接，等待后续 `send` 推送数据；
- 直至主动 `complete`、异常、超时才断开连接。



## doChatWithManus

异步输出Manus智能体输出的内容



## 流式输出三种方法

| 实现方式                 | 技术栈              | 特点                                                  |
| ------------------------ | ------------------- | ----------------------------------------------------- |
| Flux<String> + produces  | WebFlux             | 响应式流，原始文本                                    |
| Flux<ServerSentEvent<T>> | WebFlux             | 自动拼接标准 SSE 格式                                 |
| SseEmitter               | Spring MVC(Servlet) | 传统长连接，手动控制推送 / 关闭，老项目首选，比较灵活 |



## 三种调试方法

**1.通过 Swagger 接口文档来测‌试接口功能**

**2.用CURL 工具进行测试**

git有个git bash功能，模拟了Linux环境，可以用git bash来运行CURL命令。

在浏⁠览器 F12 控制‌台中，可以直接选中网络请求来复制 C‎URL 命令，非常‌便于测试：

![curl工具](E:\截屏的图片保存在这里了\typora-user-images\Snipaste_2026-06-23_16-23-57.png)

**3.IDEA 自带的 HTTP Client 工具进‎行测试**

![1625](E:\截屏的图片保存在这里了\typora-user-images\Snipaste_2026-06-23_16-25-10.png)





## 跨域问题

**先搞懂：什么是**跨域**（同源策略）**

**1. 同源策略（浏览器安全规则）**

**同源** = 三个部分**全部相同**：

```
协议 + 域名(IP) + 端口号
```

浏览器默认**只允许当前页面 访问「同源」的接口**。

如果任意一个不一样，就判定为 **跨域**，浏览器会拦截请求，这就是**跨域问题**。

**同源判断规则（举例）**

假设当前前端页面地址：`http://localhost:8080/index.html`

|         目标接口地址         | 是否跨域 |                             原因                             |
| :--------------------------: | :------: | :----------------------------------------------------------: |
| `http://localhost:8080/xxx`  |  ❶ 同源  |                    协议、域名、端口全一致                    |
| `http://localhost:8081/xxx`  |  ❷ 跨域  |                         **端口不同**                         |
| `https://localhost:8080/xxx` |  ❸ 跨域  |                 **协议不同**（http / https）                 |
| `http://127.0.0.1:8080/xxx`  |  ❹ 跨域  | **域名不同**（[localhost](https://link.wtturl.cn/?target=https%3A%2F%2Flocalhost&scene=im&aid=497858&lang=zh) ≠ 127.0.0.1） |
|   `http://a.com:8080/xxx`    |  ❺ 跨域  |                         域名完全不同                         |

> 重点：**跨域是浏览器行为**，不是后端限制。
>
> - 后端之间互相调用接口、Postman/Java 代码请求：**不会跨域**
> - 只有「浏览器里的 JS 发起请求」才会触发跨域拦截

**2. 为什么浏览器要搞跨域？（安全目的）**

防止 **恶意网站窃取你当前网站的 Cookie、登录态、隐私数据**。

举例：

你登录了网银 `bank.com`，再点开一个钓鱼网站 `hack.com`。

如果没有跨域限制，`hack.com` 的 JS 就能直接调用 `bank.com` 接口、偷你的账户信息。

**同源策略就是浏览器的一道安全锁。**