package com.yupi.yuaiagent.demo.invoke;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
/**
 * HTTP 调用 AI
 */
public class HttpAiInvoke {
    public static void main(String[] args) {
        // 替换为你自己的 API Key
        String apiKey = TestApiKey.API_KEY;
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("请设置 DASHSCOPE_API_KEY 环境变量");
        }

        // 构建请求体 JSON
        JSONObject requestBody = new JSONObject();
        requestBody.set("model", "qwen3-max");

        // messages 数组
        JSONObject message = new JSONObject();
        message.set("role", "user");
        message.set("content", "你是谁？");

        JSONObject input = new JSONObject();
        input.set("messages", new Object[]{message});
        requestBody.set("input", input);

        // parameters
        JSONObject parameters = new JSONObject();
        parameters.set("enable_thinking", true);
        parameters.set("result_format", "message");
        requestBody.set("parameters", parameters);




        // 发送 POST 请求
        HttpResponse response = HttpRequest.post("https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(requestBody.toString())
                .timeout(30000) // 超时 30 秒
                .execute();

        // 打印响应
        if (response.isOk()) {
            System.out.println("响应成功:");
            System.out.println(response.body());
        } else {
            System.err.println("请求失败，状态码: " + response.getStatus());
            System.err.println("错误信息: " + response.body());
        }
    }
}
