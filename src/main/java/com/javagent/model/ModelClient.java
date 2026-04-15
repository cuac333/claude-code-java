package com.javagent.model;

import com.javagent.core.Config;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 模型客户端 - 基于Java 11+ HttpClient实现
 * 支持Mock模式和真实API调用
 */
public class ModelClient {
    
    private final Config config;
    private final HttpClient httpClient;
    private boolean mockMode;
    
    public ModelClient(Config config) {
        this.config = config;
        this.mockMode = config.isMockMode();
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    }
    
    /**
     * 创建模型客户端实例
     */
    public static ModelClient create(Config config) {
        return new ModelClient(config);
    }
    
    /**
     * 发送对话请求
     * 伪代码实现：展示请求构建和响应解析流程
     */
    public AIResponse chat(String systemPrompt, List<Message> messages) {
        if (mockMode) {
            return mockChat(systemPrompt, messages);
        }
        
        try {
            // 1. 构建请求体
            String requestBody = buildRequestBody(systemPrompt, messages);
            
            // 2. 创建HTTP请求
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.getBaseUrl() + "/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + config.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
            
            // 3. 发送请求
            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
            
            // 4. 解析响应
            if (response.statusCode() == 200) {
                return parseResponse(response.body());
            } else {
                return AIResponse.error("API error: " + response.statusCode() + " - " + response.body());
            }
            
        } catch (Exception e) {
            return AIResponse.error("Request failed: " + e.getMessage());
        }
    }
    
    /**
     * Mock模式实现
     */
    private AIResponse mockChat(String systemPrompt, List<Message> messages) {
        // 获取最后一条用户消息
        String lastUserMessage = messages.stream()
            .filter(Message::isUser)
            .reduce((first, second) -> second)
            .map(Message::content)
            .orElse("");
        
        // 简单的关键词响应
        String response = generateMockResponse(lastUserMessage);
        
        return new AIResponse(response, null, "mock-model", 0, false);
    }
    
    private String generateMockResponse(String input) {
        String lowerInput = input.toLowerCase();
        
        if (lowerInput.contains("hello") || lowerInput.contains("hi")) {
            return "Hello! I'm JavaAgent CLI. How can I help you today?";
        }
        
        if (lowerInput.contains("help")) {
            return "I can help you with:\n" +
                   "- Reading files (use /tools to see available tools)\n" +
                   "- Answering questions\n" +
                   "- Executing approved commands\n" +
                   "\nType /help for CLI commands.";
        }
        
        if (lowerInput.contains("tool") || lowerInput.contains("read")) {
            return "I have access to several tools including:\n" +
                   "- ReadFile: Read file contents\n" +
                   "- GrepTool: Search file contents\n" +
                   "- BashTool: Execute shell commands (requires approval)\n" +
                   "\nWould you like me to use one of these tools?";
        }
        
        // 默认响应
        return "[Mock Mode] You said: \"" + input + "\"\n\n" +
               "This is a mock response. Switch to real mode with '/mode real' and configure API key.";
    }
    
    /**
     * 构建请求体（伪代码）
     */
    private String buildRequestBody(String systemPrompt, List<Message> messages) {
        // 伪代码：实际实现需要JSON序列化
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"model\":\"gpt-4\",");
        sb.append("\"messages\":[");
        
        // 添加系统消息
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            sb.append("{\"role\":\"system\",\"content\":\"").append(escapeJson(systemPrompt)).append("\"},");
        }
        
        // 添加对话消息
        for (int i = 0; i < messages.size(); i++) {
            Message msg = messages.get(i);
            sb.append("{\"role\":\"").append(msg.role()).append("\",\"content\":\"");
            sb.append(escapeJson(msg.content())).append("\"}");
            if (i < messages.size() - 1) {
                sb.append(",");
            }
        }
        
        sb.append("],");
        sb.append("\"max_tokens\":2000");
        sb.append("}");
        
        return sb.toString();
    }
    
    /**
     * 解析响应（伪代码）
     */
    private AIResponse parseResponse(String responseBody) {
        // 伪代码：实际实现需要JSON解析
        // 简化实现：直接返回错误，因为需要完整的JSON解析
        return AIResponse.error("Real API mode not fully implemented. Use mock mode.");
    }
    
    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    // ========== 配置方法 ==========
    
    public void setMockMode(boolean mockMode) {
        this.mockMode = mockMode;
    }
    
    public boolean isMockMode() {
        return mockMode;
    }
    
    // ========== AI响应记录 ==========
    
    public record AIResponse(
        String content,
        List<Message.ToolCall> toolCalls,
        String model,
        int tokensUsed,
        boolean isError
    ) {
        public boolean hasToolCalls() {
            return toolCalls != null && !toolCalls.isEmpty();
        }
        
        public static AIResponse error(String message) {
            return new AIResponse(message, null, "error", 0, true);
        }
    }
}
