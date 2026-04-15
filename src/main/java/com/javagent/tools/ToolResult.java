package com.javagent.tools;

import java.util.Map;

/**
 * 工具执行结果
 */
public record ToolResult(
    boolean success,
    String content,
    String error,
    Map<String, Object> metadata
) {
    
    public static ToolResult success(String content) {
        return new ToolResult(true, content, null, null);
    }
    
    public static ToolResult success(String content, Map<String, Object> metadata) {
        return new ToolResult(true, content, null, metadata);
    }
    
    public static ToolResult error(String error) {
        return new ToolResult(false, null, error, null);
    }
    
    public static ToolResult error(String error, Map<String, Object> metadata) {
        return new ToolResult(false, null, error, metadata);
    }
    
    public boolean isError() {
        return !success;
    }
}
