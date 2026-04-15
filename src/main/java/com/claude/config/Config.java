package com.claude.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 配置类
 * 
 * 对应 TS 源码: utils/config.ts, cliArgs.ts
 */
@Configuration
@ConfigurationProperties(prefix = "claude")
public class Config {
    
    // ========== 模型配置 ==========
    private ModelConfig model = new ModelConfig();
    
    // ========== 历史配置 ==========
    private HistoryConfig history = new HistoryConfig();
    
    // ========== 上下文配置 ==========
    private ContextConfig context = new ContextConfig();
    
    // Getters and Setters
    public ModelConfig getModel() { return model; }
    public void setModel(ModelConfig model) { this.model = model; }
    
    public HistoryConfig getHistory() { return history; }
    public void setHistory(HistoryConfig history) { this.history = history; }
    
    public ContextConfig getContext() { return context; }
    public void setContext(ContextConfig context) { this.context = context; }
    
    /**
     * 模型配置
     */
    public static class ModelConfig {
        private String mode = "mock";  // mock | real
        private String apiKey;
        private String baseUrl = "https://api.claude.ai";
        
        // 对应 TS: services/api/claude.ts
        // 对应 TS: utils/betas.ts
        
        public String getMode() { return mode; }
        public void setMode(String mode) { this.mode = mode; }
        
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    }
    
    /**
     * 历史配置
     */
    public static class HistoryConfig {
        private String path = "~/.claude-code-java/history";
        private int maxSize = 100;
        
        // 对应 TS: history.ts
        
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        
        public int getMaxSize() { return maxSize; }
        public void setMaxSize(int maxSize) { this.maxSize = maxSize; }
    }
    
    /**
     * 上下文配置
     */
    public static class ContextConfig {
        private int maxTokens = 200000;
        
        // 对应 TS: utils/context.ts
        
        public int getMaxTokens() { return maxTokens; }
        public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }
    }
}
