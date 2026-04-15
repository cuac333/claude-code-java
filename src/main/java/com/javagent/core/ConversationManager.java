package com.javagent.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.javagent.model.Message;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 会话管理器 - 管理最后一次会话的保存和加载
 * 简化实现：只保存最后一次会话到单个JSON文件
 */
public class ConversationManager {
    
    private final Config config;
    private final ObjectMapper objectMapper;
    
    // 当前会话数据
    private List<Message> currentMessages = new ArrayList<>();
    private LocalDateTime sessionStartTime;
    private String lastResponse = "";
    
    public ConversationManager(Config config) {
        this.config = config;
        this.objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        this.sessionStartTime = LocalDateTime.now();
    }
    
    /**
     * 添加用户消息
     */
    public void addUserMessage(String content) {
        Message message = Message.user(content);
        currentMessages.add(message);
    }
    
    /**
     * 添加助手消息
     */
    public void addAssistantMessage(String content) {
        Message message = Message.assistant(content);
        currentMessages.add(message);
        this.lastResponse = content;
    }
    
    /**
     * 添加工具结果消息
     */
    public void addToolResult(Message toolMessage) {
        currentMessages.add(toolMessage);
    }
    
    /**
     * 获取当前上下文（用于发送给模型）
     */
    public List<Message> getCurrentContext() {
        return new ArrayList<>(currentMessages);
    }
    
    /**
     * 获取最后一条响应
     */
    public String getLastResponse() {
        return lastResponse;
    }
    
    /**
     * 清空当前会话
     */
    public void clearCurrentSession() {
        currentMessages.clear();
        sessionStartTime = LocalDateTime.now();
        lastResponse = "";
    }
    
    /**
     * 保存当前会话到JSON文件
     */
    public void saveCurrentSession() {
        try {
            SessionData sessionData = new SessionData(
                sessionStartTime,
                LocalDateTime.now(),
                currentMessages,
                lastResponse
            );
            
            Path sessionPath = config.getLastSessionPath();
            
            // 确保父目录存在
            Path parent = sessionPath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            
            objectMapper.writeValue(sessionPath.toFile(), sessionData);
            
        } catch (IOException e) {
            System.err.println("Failed to save session: " + e.getMessage());
        }
    }
    
    /**
     * 加载最后一次会话
     */
    public void loadLastSession() {
        try {
            Path sessionPath = config.getLastSessionPath();
            
            if (!Files.exists(sessionPath)) {
                System.out.println("No previous session found");
                return;
            }
            
            SessionData sessionData = objectMapper.readValue(
                sessionPath.toFile(), 
                new TypeReference<SessionData>() {}
            );
            
            this.currentMessages = sessionData.messages != null ? sessionData.messages : new ArrayList<>();
            this.sessionStartTime = sessionData.startTime != null ? sessionData.startTime : LocalDateTime.now();
            this.lastResponse = sessionData.lastResponse != null ? sessionData.lastResponse : "";
            
            System.out.println("Loaded session with " + currentMessages.size() + " messages");
            
        } catch (IOException e) {
            System.err.println("Failed to load session: " + e.getMessage());
            // 初始化空会话
            clearCurrentSession();
        }
    }
    
    /**
     * 检查是否有保存的会话
     */
    public boolean hasSavedSession() {
        return Files.exists(config.getLastSessionPath());
    }
    
    /**
     * 获取会话统计信息
     */
    public String getSessionStats() {
        long userCount = currentMessages.stream()
            .filter(m -> "user".equals(m.role()))
            .count();
        long assistantCount = currentMessages.stream()
            .filter(m -> "assistant".equals(m.role()))
            .count();
        long toolCount = currentMessages.stream()
            .filter(m -> "tool".equals(m.role()))
            .count();
        
        return String.format(
            "Session: %d messages (user: %d, assistant: %d, tool: %d)",
            currentMessages.size(), userCount, assistantCount, toolCount
        );
    }
    
    // ========== 内部数据类 ==========
    
    private static class SessionData {
        public LocalDateTime startTime;
        public LocalDateTime lastUpdated;
        public List<Message> messages;
        public String lastResponse;
        
        public SessionData() {}
        
        public SessionData(LocalDateTime startTime, LocalDateTime lastUpdated, 
                          List<Message> messages, String lastResponse) {
            this.startTime = startTime;
            this.lastUpdated = lastUpdated;
            this.messages = messages;
            this.lastResponse = lastResponse;
        }
    }
}
