package com.javagent.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * 配置管理类
 * 基于Java标准库的配置管理，支持属性文件持久化
 */
public class Config {
    
    private static final String CONFIG_FILE = "config.properties";
    private static final String LAST_SESSION_FILE = "last_session.json";
    
    private final Properties properties = new Properties();
    private final Path configPath;
    
    // 配置项常量
    private static final String KEY_MOCK_MODE = "agent.mock_mode";
    private static final String KEY_API_KEY = "agent.api_key";
    private static final String KEY_BASE_URL = "agent.base_url";
    private static final String KEY_AUTO_SAVE = "agent.auto_save";
    private static final String KEY_MAX_TOKENS = "agent.max_tokens";
    private static final String KEY_APPROVAL_REQUIRED = "agent.approval_required";
    
    // 默认配置值
    private static final String DEFAULT_MOCK_MODE = "true";
    private static final String DEFAULT_API_KEY = "";
    private static final String DEFAULT_BASE_URL = "https://api.example.com/v1";
    private static final String DEFAULT_AUTO_SAVE = "true";
    private static final String DEFAULT_MAX_TOKENS = "200000";
    private static final String DEFAULT_APPROVAL_REQUIRED = "true";
    
    private Config(Path configPath) {
        this.configPath = configPath;
        loadProperties();
    }
    
    /**
     * 加载配置
     */
    public static Config load() {
        Path configPath;
        
        // 1. 首先尝试当前目录
        Path currentDir = Paths.get(System.getProperty("user.dir"));
        Path currentConfig = currentDir.resolve(CONFIG_FILE);
        
        // 2. 然后尝试用户home目录
        Path homeDir = Paths.get(System.getProperty("user.home"));
        Path homeConfig = homeDir.resolve(".javaagent").resolve(CONFIG_FILE);
        
        // 3. 确定使用哪个配置文件
        if (Files.exists(currentConfig)) {
            configPath = currentConfig;
        } else if (Files.exists(homeConfig)) {
            configPath = homeConfig;
        } else {
            // 都不存在，创建默认配置在用户目录
            configPath = homeConfig;
        }
        
        return new Config(configPath);
    }
    
    private void loadProperties() {
        try {
            if (Files.exists(configPath)) {
                try (FileInputStream fis = new FileInputStream(configPath.toFile())) {
                    properties.load(fis);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load config: " + e.getMessage());
        }
        
        // 确保必要配置项存在
        ensureDefaultConfig();
    }
    
    /**
     * 保存配置
     */
    public void save() {
        try {
            // 确保父目录存在
            Path parent = configPath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            
            try (FileOutputStream fos = new FileOutputStream(configPath.toFile())) {
                properties.store(fos, "JavaAgent CLI Configuration");
            }
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }
    
    private void ensureDefaultConfig() {
        if (!properties.containsKey(KEY_MOCK_MODE)) {
            properties.setProperty(KEY_MOCK_MODE, DEFAULT_MOCK_MODE);
        }
        if (!properties.containsKey(KEY_API_KEY)) {
            properties.setProperty(KEY_API_KEY, DEFAULT_API_KEY);
        }
        if (!properties.containsKey(KEY_BASE_URL)) {
            properties.setProperty(KEY_BASE_URL, DEFAULT_BASE_URL);
        }
        if (!properties.containsKey(KEY_AUTO_SAVE)) {
            properties.setProperty(KEY_AUTO_SAVE, DEFAULT_AUTO_SAVE);
        }
        if (!properties.containsKey(KEY_MAX_TOKENS)) {
            properties.setProperty(KEY_MAX_TOKENS, DEFAULT_MAX_TOKENS);
        }
        if (!properties.containsKey(KEY_APPROVAL_REQUIRED)) {
            properties.setProperty(KEY_APPROVAL_REQUIRED, DEFAULT_APPROVAL_REQUIRED);
        }
    }
    
    // ========== 配置获取方法 ==========
    
    public boolean isMockMode() {
        return Boolean.parseBoolean(properties.getProperty(KEY_MOCK_MODE, DEFAULT_MOCK_MODE));
    }
    
    public void setMockMode(boolean mockMode) {
        properties.setProperty(KEY_MOCK_MODE, String.valueOf(mockMode));
        save();
    }
    
    public String getApiKey() {
        return properties.getProperty(KEY_API_KEY, DEFAULT_API_KEY);
    }
    
    public void setApiKey(String apiKey) {
        properties.setProperty(KEY_API_KEY, apiKey != null ? apiKey : "");
        save();
    }
    
    public String getBaseUrl() {
        return properties.getProperty(KEY_BASE_URL, DEFAULT_BASE_URL);
    }
    
    public void setBaseUrl(String baseUrl) {
        properties.setProperty(KEY_BASE_URL, baseUrl != null ? baseUrl : DEFAULT_BASE_URL);
        save();
    }
    
    public boolean shouldAutoSave() {
        return Boolean.parseBoolean(properties.getProperty(KEY_AUTO_SAVE, DEFAULT_AUTO_SAVE));
    }
    
    public void setAutoSave(boolean autoSave) {
        properties.setProperty(KEY_AUTO_SAVE, String.valueOf(autoSave));
        save();
    }
    
    public int getMaxTokens() {
        try {
            return Integer.parseInt(properties.getProperty(KEY_MAX_TOKENS, DEFAULT_MAX_TOKENS));
        } catch (NumberFormatException e) {
            return Integer.parseInt(DEFAULT_MAX_TOKENS);
        }
    }
    
    public void setMaxTokens(int maxTokens) {
        properties.setProperty(KEY_MAX_TOKENS, String.valueOf(maxTokens));
        save();
    }
    
    public boolean isApprovalRequired() {
        return Boolean.parseBoolean(properties.getProperty(KEY_APPROVAL_REQUIRED, DEFAULT_APPROVAL_REQUIRED));
    }
    
    public void setApprovalRequired(boolean approvalRequired) {
        properties.setProperty(KEY_APPROVAL_REQUIRED, String.valueOf(approvalRequired));
        save();
    }
    
    // ========== 会话文件路径方法 ==========
    
    public Path getLastSessionPath() {
        // 优先当前目录，否则用户目录
        Path currentDir = Paths.get(System.getProperty("user.dir"));
        Path currentSession = currentDir.resolve(LAST_SESSION_FILE);
        
        if (Files.exists(currentDir) && Files.isWritable(currentDir)) {
            return currentSession;
        }
        
        // 使用用户目录
        Path homeDir = Paths.get(System.getProperty("user.home"));
        return homeDir.resolve(".javaagent").resolve(LAST_SESSION_FILE);
    }
    
    // ========== 环境检测方法 ==========
    
    public String getRuntimeInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Java Version: ").append(System.getProperty("java.version")).append("\n");
        info.append("OS: ").append(System.getProperty("os.name")).append(" ").append(System.getProperty("os.version")).append("\n");
        info.append("User Home: ").append(System.getProperty("user.home")).append("\n");
        info.append("Working Dir: ").append(System.getProperty("user.dir")).append("\n");
        info.append("Config File: ").append(configPath.toString());
        return info.toString();
    }
}