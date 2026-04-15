package com.claude.tools;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * ReadFileTool - 读取文件内容
 * 
 * 对应 TS 源码: tools/FileReadTool.ts
 */
@Component
public class ReadFileTool implements Tool {
    
    private static final int MAX_LINES = 1000;  // 最大读取行数
    
    @Override
    public String getName() {
        return "ReadFile";
    }
    
    @Override
    public String getDescription() {
        return "读取文件内容，支持指定行数范围";
    }
    
    @Override
    public String[] getAliases() {
        return new String[]{"read", "cat", "type"};
    }
    
    @Override
    public boolean isReadOnly() {
        return true;
    }
    
    @Override
    public Result execute(Map<String, Object> input) {
        try {
            // 获取参数
            String path = (String) input.get("path");
            Integer offset = (Integer) input.getOrDefault("offset", 0);    // 起始行
            Integer limit = (Integer) input.getOrDefault("limit", MAX_LINES);  // 行数限制
            
            if (path == null || path.isEmpty()) {
                return Result.error("路径不能为空");
            }
            
            Path filePath = Paths.get(path);
            
            // 检查文件是否存在
            if (!Files.exists(filePath)) {
                return Result.error("文件不存在: " + path);
            }
            
            // 检查是否是常规文件
            if (!Files.isRegularFile(filePath)) {
                return Result.error("不是常规文件: " + path);
            }
            
            // 读取文件
            List<String> allLines = Files.readAllLines(filePath);
            
            // 应用行范围
            int start = Math.min(offset, allLines.size());
            int end = Math.min(offset + limit, allLines.size());
            List<String> selectedLines = allLines.subList(start, end);
            
            // 构建结果
            StringBuilder content = new StringBuilder();
            content.append("File: ").append(path).append("\n");
            content.append("Lines: ").append(start + 1).append("-").append(end);
            content.append(" of ").append(allLines.size()).append("\n");
            content.append("─".repeat(50)).append("\n");
            content.append(String.join("\n", selectedLines));
            
            // 如果有截断，添加提示
            if (end < allLines.size()) {
                content.append("\n... (省略 ").append(allLines.size() - end).append(" 行)");
            }
            
            return Result.success(content.toString());
            
        } catch (Exception e) {
            return Result.error("读取文件失败: " + e.getMessage());
        }
    }
}
