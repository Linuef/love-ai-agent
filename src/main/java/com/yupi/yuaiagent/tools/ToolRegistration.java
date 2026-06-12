package com.yupi.yuaiagent.tools;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolRegistration {
    @Value("${search-api.api-key}")
    private String searchApiKey;

    @Bean
    public ToolCallback[] allTools() {
        //操作文件的工具类
        FileOperationTool fileOperationTool = new FileOperationTool();
        //网页搜索的工具类
        WebSearchTool webSearchTool = new WebSearchTool(searchApiKey);
        //抓取网页的工具类
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        //下载资源到本地的工具类
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        //执行终端命令的工具类
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        //生成PDF的工具类
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        //获取当前时间
        GetCurrentTimeTool getCurrentTimeTool = new GetCurrentTimeTool();

        return ToolCallbacks.from(
                fileOperationTool,
                webSearchTool,
                webScrapingTool,
                resourceDownloadTool,
                terminalOperationTool,
                pdfGenerationTool,
                getCurrentTimeTool
        );
    }
}
