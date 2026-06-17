这个包下的工具类用于根据提示词搜索图片



# ImageSearchTool

根据提示词搜索图片。

图片网站： [Pexels]([Free Image and Video API – Pexels](https://www.pexels.com/api/))，在这个网站申请api密钥后，根据项目文档写出调用这个api的工具类。

我把项目文档传给ai后，ai给我写出来了代码。

在写完这个工具类后，要在类YuImageSearchMcpServerApplication中注册为MCP服务，才能被MCP客户端调用。



新增了一项功能：在从Pexels网站中获得图片链接中，只获取前num个链接，这个num通过环境变量来传递，也就是说，你需要在

mcp-server.json文件中定义：

![image-20260612170157768](C:\Users\20496\AppData\Roaming\Typora\typora-user-images\image-20260612170157768.png)

上图的num的意思是，只获取前两个链接。

## 参数传递机制

在 std⁠io 传输模式下可‌以通过环境变量传递参数，比如传递 A‎PI Key：

```json
{
  "mcpServers": {
    "amap-maps": {
      "command": "npx",
      "args": [
        "-y",
        "@amap/amap-maps-mcp-server"
      ],
      "env": {
        "AMAP_MAPS_API_KEY": "你的 API Key"
      }
    }
}
```



怎么在 MCP 服务中获取到定义好的环境变量呢？

让我们来看下 ⁠Java MCP Clie‌nt 的源码，发现建立连接时客户端传递的环境变量会被‎设置到服务器进程的环境变量‌中（可能存在一定的安全风险）：

![image-20260612170410163](C:\Users\20496\AppData\Roaming\Typora\typora-user-images\image-20260612170410163.png)

在 MCP 服务端可以通过 `System.getenv()` 获取环境变量。让我们来测试一下，随便添加一个变量：

![image-20260612170426219](C:\Users\20496\AppData\Roaming\Typora\typora-user-images\image-20260612170426219.png)

修改 MCP 服务端的代码，获取到环境变量的值。注意不能直接通过 `System.out.println` 来输出环境变量，因为 stdio 使用标准输入输出流进行通信，自己输出的内容会干扰通信。

![image-20260612170458434](C:\Users\20496\AppData\Roaming\Typora\typora-user-images\image-20260612170458434.png)

运行 MCP 客户端，发现获取环境变量的值成功：

![image-20260612170510522](C:\Users\20496\AppData\Roaming\Typora\typora-user-images\image-20260612170510522.png)
