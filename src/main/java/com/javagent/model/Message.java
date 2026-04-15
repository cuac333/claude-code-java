package com.javagent.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 消息模型 - 使用Java Record实现不可变消息
 * 支持用户、助手、工具角色
 */
public record Message(
    String id,
    String role,           // user | assistant | tool | system
    String content,
    LocalDateTime timestamp,
    List<ToolCall> toolCalls,    // AI调用的工具（仅assistant消息）
    ToolResult toolResult,       // 工具执行结果（仅tool消息）
    Map<String, Object> metadata
) {
    
    // ========== 静态工厂方法 ==========
    
    public static Message user(String content) {
        return new Message(
            UUID.randomUUID().toString(),
            "user",
            content,
            LocalDateTime.now(),
            null,
            null,
            null
        );
    }
    
    public static Message assistant(String content) {
        return new Message(
            UUID.randomUUID().toString(),
            "assistant",
            content,
            LocalDateTime.now(),
            null,
            null,
            null
        );
    }
    
    public static Message assistantWithToolCalls(String content, List<ToolCall> toolCalls) {
        return new Message(
            UUID.randomUUID().toString(),
            "assistant",
            content,
            LocalDateTime.now(),
            toolCalls,
            null,
            null
        );
    }
    
    public static Message tool(String toolCallId, String content, boolean isError) {
        return new Message(
            UUID.randomUUID().toString(),
            "tool",
            content,
            LocalDateTime.now(),
            null,
            new ToolResult(toolCallId, content, isError),
            null
        );
    }
    
    public static Message system(String content) {
        return new Message(
            UUID.randomUUID().toString(),
            "system",
            content,
            LocalDateTime.now(),
            null,
            null,
            null
        );
    }
    
    // ========== 便捷方法 ==========
    
    public boolean isUser() {
        return "user".equals(role);
    }
    
    public boolean isAssistant() {
        return "assistant".equals(role);
    }
    
    public boolean isTool() {
        return "tool".equals(role);
    }
    
    public boolean isSystem() {
        return "system".equals(role);
    }
    
    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }
    
    // ========== 内部记录类 ==========
    
    /**
     * 工具调用记录
     */
    public record ToolCall(
        String id,
        String name,
        Map<String, Object> input
    ) {
        public String getPath() {
            return input != null ? (String) input.get("path") : null;
        }
        
        public String getCommand() {
            return input != null ? (String) input.get("command") : null;
        }
        
        public String getPattern() {
            return input != null ? (String) input.get("pattern") : null;
        }
    }
    
    /**
     * 工具结果记录
     */
    public record ToolResult(
        String toolCallId,
        String content,
        boolean isError
    ) {
        public static ToolResult success(String toolCallId, String content) {
            return new ToolResult(toolCallId, content, false);
        }
        
        public static ToolResult error(String toolCallId, String error) {
            return new ToolResult(toolCallId, error, true);
        }
    }
}
