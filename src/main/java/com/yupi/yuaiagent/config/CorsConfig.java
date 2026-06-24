package com.yupi.yuaiagent.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
/**
 * 全局跨域配置类
 * 解决前后端分离场景下浏览器CORS跨域拦截问题，对项目所有接口生效
 * CORS：Cross-Origin Resource Sharing 跨域资源共享
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 全局注册跨域映射规则
     * @param registry 跨域注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 匹配项目下所有接口路径，全局应用跨域规则
        registry.addMapping("/**")
                // 允许跨域请求携带Cookie、会话凭证、登录信息
                .allowCredentials(true)
                // 允许所有来源域名访问接口，兼容凭证传递（Spring推荐写法）
                .allowedOriginPatterns("*")
                // 允许的HTTP请求方法，包含OPTIONS预检请求
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许前端携带所有自定义请求头（如Token、Authorization）
                .allowedHeaders("*")
                // 向后端暴露所有响应头，允许前端JS读取自定义响应头
                .exposedHeaders("*");
    }
}
