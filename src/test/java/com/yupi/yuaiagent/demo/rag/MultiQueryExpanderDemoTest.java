package com.yupi.yuaiagent.demo.rag;

import com.alibaba.cloud.ai.document.TextDocumentParser;
import com.alibaba.cloud.ai.parser.tika.TikaDocumentParser;
import com.alibaba.cloud.ai.reader.github.GitHubDocumentReader;
import com.alibaba.cloud.ai.reader.github.GitHubResource;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GitHub;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import javax.net.ssl.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.http.HttpClient;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class MultiQueryExpanderDemoTest {
    @Resource
    private MultiQueryExpanderDemo multiQueryExpanderDemo;
    
    private SSLContext originalSSLContext;
    private SSLSocketFactory originalSSLSocketFactory;
    private HostnameVerifier originalHostnameVerifier;
    private String originalSystemProperty;
    
    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {
        originalSSLContext = SSLContext.getDefault();
        originalSSLSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
        originalHostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
        originalSystemProperty = System.getProperty("jdk.internal.httpclient.disableHostnameVerification");
    }
    
    @AfterEach
    void tearDown() {
        try {
            if (originalSSLContext != null) {
                SSLContext.setDefault(originalSSLContext);
            }
            if (originalSSLSocketFactory != null) {
                HttpsURLConnection.setDefaultSSLSocketFactory(originalSSLSocketFactory);
            }
            if (originalHostnameVerifier != null) {
                HttpsURLConnection.setDefaultHostnameVerifier(originalHostnameVerifier);
            }
            if (originalSystemProperty != null) {
                System.setProperty("jdk.internal.httpclient.disableHostnameVerification", originalSystemProperty);
            } else {
                System.clearProperty("jdk.internal.httpclient.disableHostnameVerification");
            }
        } catch (Exception e) {
            System.err.println("Failed to restore SSL configuration: " + e.getMessage());
        }
    }
    
    @Test
    void expand() {
        List<Query> queries = multiQueryExpanderDemo.expand("谁是程序员鱼皮啊？");
        System.out.println(queries);
    }



    @Value("${myapp.github.token}")
    private String githubToken;
    @Test
    void testGithubDocumentReader() throws IOException {
        // 创建 Tika Parser
        TikaDocumentParser tikaDocumentParser = new TikaDocumentParser();
        String path = "spring-ai-alibaba-graph-core/src/main/java/com/alibaba/cloud/ai/graph/action/AsyncCommandAction.java";
        GitHub gitHub = GitHub.connectUsingOAuth(githubToken);
        GitHubResource gitHubResource = GitHubResource.builder()
                .gitHub(gitHub)
                .owner("alibaba")
                .repo("spring-ai-alibaba")
                .branch("main")
                .path("README.md")
                .build();
        TextDocumentParser textDocumentParser = new TextDocumentParser();
        GitHubDocumentReader reader = new GitHubDocumentReader(gitHubResource,tikaDocumentParser);
        List<Document> documents = reader.read();
        for (Document document : documents) {
            System.out.println("文档内容: " + document.getText());
            System.out.println("文档元数据: " + document.getMetadata());
        }
    }

    /**
     * 这个方法的核心目的是禁用 Java 应用程序中的 SSL/TLS 证书验证,
     * 让程序可以连接到使用自签名证书、过期证书或无效证书的 HTTPS 服务器。
     */
    private static void installAllTrustManager() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
            };

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            
            SSLContext.setDefault(sc);
            
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
            
            System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to install all-trust manager", e);
        }
    }
}