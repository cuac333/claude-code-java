package com.javagent.tools;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * 文件读取工具
 */
public class ReadFileTool implements Tool {
    
    private static final int MAX_LINES = 1000;
    
    @Override
    public String getName() {
        return "ReadFile";
    }
    
    @Override
    public String getDescription() {
        return "Read file contents with optional line range";
    }
    
    @Override
    public String[] getAliases() {
        return new String[]{"read", "cat", "type"};
    }
    
    @Override
    public ToolResult execute(Map<String, Object> input) {
        try {
            String path = (String) input.get("path");
            if (path == null || path.isEmpty()) {
                return ToolResult.error("Path is required");
            }
            
            Path filePath = Paths.get(path);
            
            // 检查文件是否存在
            if (!Files.exists(filePath)) {
                return ToolResult.error("File not found: " + path);
            }
            
            // 检查是否为常规文件
            if (!Files.isRegularFile(filePath)) {
                return ToolResult.error("Not a regular file: " + path);
            }
            
            // 读取文件
            List<String> allLines = Files.readAllLines(filePath);
            
            // 获取行范围参数
            int offset = getInt(input, "offset", 0);
            int limit = getInt(input, "limit", MAX_LINES);
            
            // 应用行范围
            int start = Math.max(0, Math.min(offset, allLines.size()));
            int end = Math.min(start + limit, allLines.size());
            List<String> selectedLines = allLines.subList(start, end);
            
            // 构建结果
            StringBuilder content = new StringBuilder();
            content.append("File: ").append(path).append("\n");
            content.append("Lines: ").append(start + 1).append("-").append(end);
            content.append(" of ").append(allLines.size()).append("\n");
            content.append("─".repeat(50)).append("\n");
            content.append(String.join("\n", selectedLines));
            
            if (end < allLines.size()) {
                content.append("\n... (").append(allLines.size() - end).append(" lines omitted)");
            }
            
            return ToolResult.success(content.toString());
            
        } catch (Exception e) {
            return ToolResult.error("Failed to read file: " + e.getMessage());
        }
    }
    
    private int getInt(Map<String, Object> input, String key, int defaultValue) {
        Object value = input.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
}
