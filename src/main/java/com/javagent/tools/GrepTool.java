package com.javagent.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

/**
 * 文件搜索工具 - 基于正则表达式搜索文件内容
 */
public class GrepTool implements Tool {
    
    private static final int MAX_RESULTS = 100;
    private static final int MAX_FILES = 1000;
    
    @Override
    public String getName() {
        return "Grep";
    }
    
    @Override
    public String getDescription() {
        return "Search file contents using regex pattern";
    }
    
    @Override
    public String[] getAliases() {
        return new String[]{"search", "find", "grep"};
    }
    
    @Override
    public ToolResult execute(Map<String, Object> input) {
        try {
            String pattern = (String) input.get("pattern");
            String path = (String) input.get("path");
            
            if (pattern == null || pattern.isEmpty()) {
                return ToolResult.error("Pattern is required");
            }
            if (path == null || path.isEmpty()) {
                return ToolResult.error("Path is required");
            }
            
            Path searchPath = Paths.get(path);
            if (!Files.exists(searchPath)) {
                return ToolResult.error("Path not found: " + path);
            }
            
            // 编译正则表达式
            boolean caseSensitive = getBoolean(input, "caseSensitive", false);
            int flags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE;
            Pattern regex;
            try {
                regex = Pattern.compile(pattern, flags);
            } catch (PatternSyntaxException e) {
                return ToolResult.error("Invalid regex pattern: " + e.getMessage());
            }
            
            // 搜索文件
            StringBuilder results = new StringBuilder();
            results.append("Search: \"").append(pattern).append("\"\n");
            results.append("Path: ").append(path).append("\n");
            results.append("─".repeat(50)).append("\n\n");
            
            int matchCount = 0;
            int fileCount = 0;
            
            try (Stream<Path> paths = Files.walk(searchPath)) {
                List<Path> files = paths
                    .filter(Files::isRegularFile)
                    .limit(MAX_FILES)
                    .toList();
                
                for (Path file : files) {
                    fileCount++;
                    int fileMatches = searchInFile(file, regex, results);
                    matchCount += fileMatches;
                    
                    if (matchCount >= MAX_RESULTS) {
                        results.append("\n... (max results reached)");
                        break;
                    }
                }
            }
            
            results.append("\n\n").append(matchCount).append(" matches in ")
                   .append(fileCount).append(" files");
            
            return ToolResult.success(results.toString());
            
        } catch (Exception e) {
            return ToolResult.error("Search failed: " + e.getMessage());
        }
    }
    
    private int searchInFile(Path file, Pattern regex, StringBuilder results) {
        int matches = 0;
        try {
            List<String> lines = Files.readAllLines(file);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (regex.matcher(line).find()) {
                    matches++;
                    results.append(file.getFileName())
                           .append(":").append(i + 1)
                           .append(": ").append(line)
                           .append("\n");
                }
            }
        } catch (IOException e) {
            // 忽略无法读取的文件
        }
        return matches;
    }
    
    private boolean getBoolean(Map<String, Object> input, String key, boolean defaultValue) {
        Object value = input.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return defaultValue;
    }
}
