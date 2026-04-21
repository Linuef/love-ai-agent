package com.yupi.yuaiagent.rag;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.tmt.v20180321.TmtClient;
import com.tencentcloudapi.tmt.v20180321.models.TextTranslateRequest;
import com.tencentcloudapi.tmt.v20180321.models.TextTranslateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Slf4j
@Component
public class MyTranslationQueryTransformer  {
    @Value("${tencentcloud.secret-id}")
    private String secretId;
    @Value("${tencentcloud.secret-key}")
    private String secretKey;

    public String translate(String text,String sourceLanguage,String targetLanguage) {
        try {
            //实例化一个认证对象
            // 注意：实际生产中建议使用 DefaultCredentialsProvider 或环境变量，避免硬编码
            Credential cred = new Credential(secretId, secretKey);

            //实例化一个http选项 (可选，没有特殊需求可以跳过)
            HttpProfile httpProfile = new HttpProfile();
            // 设置地域，TMT 支持的地域如 ap-shanghai, ap-guangzhou 等
            httpProfile.setEndpoint("tmt.tencentcloudapi.com");

            //实例化一个client选项
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            // 设置签名方法，TMT 通常支持 HmacSHA256 或 TC3-HMAC-SHA256
            clientProfile.setSignMethod("HmacSHA256");

            // 实例化要请求产品的client对象
            // 注意：第二个参数是地域，必须与 endpoint 对应
            TmtClient client = new TmtClient(cred, "ap-shanghai", clientProfile);

            //实例化一个请求对象
            TextTranslateRequest req = new TextTranslateRequest();

            //填充请求参数
            req.setSourceText(text); // 待翻译文本
            req.setSource(sourceLanguage);                // 源语言
            req.setTarget(targetLanguage);                // 目标语言
            req.setProjectId(0L);                // 项目ID，默认为0

            //通过client对象调用对应接口
            TextTranslateResponse resp = client.TextTranslate(req);
            // 如果需要看完整JSON返回包
            // System.out.println(TextTranslateResponse.toJsonString(resp));
            // 输出结果
            return resp.getTargetText();


        } catch (TencentCloudSDKException e) {
            log.error("调用失败: " + e.toString(),e);
        }

        return "翻译失败" + text;
    }
}
