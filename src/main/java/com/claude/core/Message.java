package com.claude.core;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 消息模型
 * 
 * 对应 TS 源码: types.ts 中的 Message 类型
 */
@Data
public class Message {
    
    // ========== 基础字段 ==========
    private String id;              // 消息唯一ID
    private String role;            // user | assistant | system | tool
    private String content;         // 消息内容
    private LocalDateTime timestamp; // 时间戳
    
    // ========== 工具调用相关 (对应 TS 中的 tool_use) ==========
    private List<ToolCall> toolCalls;     // AI 调用的工具
    private ToolResult toolResult;        // 工具执行结果
    
    // ========== 元数据 (对应 TS 中的各种 metadata) ==========
    private Map<String, Object> metadata;
    private String model;           // 使用的模型
    private Integer tokenCount;     // token 数量
    private Boolean isError;        // 是否是错误消息
    
    /**
     * 工具调用
     * 
     * 对应 TS 源码: types.ts 中的 ToolCall
     */
    @Data
    public static class ToolCall {
        private String id;           // 工具调用ID
        private String name;         // 工具名称 (如 "ReadFile", "Bash")
        private Map<String, Object> input;  // 工具参数
        
        // 辅助方法
        public String getPath() { return (String) input.get("path"); }
        public String getCommand() { return (String) input.get("command"); }
        public String getPattern() { return (String) input.get("pattern"); }
    }
    
    /**
     * 工具执行结果
     * 
     * 对应 TS 源码: types.ts 中的 ToolResult
     */
    @Data
    public static class ToolResult {
        private String toolCallId;   // 对应的工具调用ID
        private String content;      // 执行结果内容
        private Boolean isError;     // 是否出错
        private String error;         // 错误信息
        
        public static ToolResult success(String toolCallId, String content) {
            ToolResult result = new ToolResult();
            result.setToolCallId(toolCallId);
            result.setContent(content);
            result.setIsError(false);
            return result;
        }
        
        public static ToolResult error(String toolCallId, String error) {
            ToolResult result = new ToolResult();
            result.setToolCallId(toolCallId);
            result.setError(error);
            result.setIsError(true);
            return result;
        }
    }
    
    // ========== 静态工厂方法 ==========
    
    public static Message user(String content) {
        Message msg = new Message();
        msg.setId(java.util.UUID.randomUUID().toString());
        msg.setRole("user");
        msg.setContent(content);
        msg.setTimestamp(LocalDateTime.now());
        return msg;
    }
    
    public static Message assistant(String content) {
        Message msg = new Message();
        msg.setId(java.util.UUID.randomUUID().toString());
        msg.setRole("assistant");
        msg.setContent(content);
        msg.setTimestamp(LocalDateTime.now());
        return msg;
    }
    
    public static Message system(String content) {
        Message msg = new Message();
        msg.setId(java.util.UUID.randomUUID().toString());
        msg.setRole("system");
        msg.setContent(content);
        msg.setTimestamp(LocalDateTime.now());
        return msg;
    }
    
    public static Message tool(String toolCallId, String content) {
        Message msg = new Message();
        msg.setId(java.util.UUID.randomUUID().toString());
        msg.setRole("tool");
        msg.setContent(content);
        msg.setTimestamp(LocalDateTime.now());
        return msg;
    }
}