package com.claude.tools;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

/**
 * GrepTool - 搜索文件内容
 * 
 * 对应 TS 源码: tools/GrepTool.ts
 */
@Component
public class GrepTool implements Tool {
    
    private static final int MAX_RESULTS = 100;  // 最大结果数
    
    @Override
    public String getName() {
        return "Grep";
    }
    
    @Override
    public String getDescription() {
        return "在文件中搜索匹配正则表达式的内容";
    }
    
    @Override
    public String[] getAliases() {
        return new String[]{"search", "find", "grep"};
    }
    
    @Override
    public boolean isReadOnly() {
        return true;
    }
    
    @Override
    public Result execute(Map<String, Object> input) {
        try {
            // 获取参数
            String pattern = (String) input.get("pattern");    // 正则表达式
            String path = (String) input.get("path");          // 搜索路径
            String glob = (String) input.get("glob");          // 文件过滤
            Boolean caseSensitive = (Boolean) input.getOrDefault("caseSensitive", false);
            Boolean showLineNumbers = (Boolean) input.getOrDefault("showLineNumbers", true);
            
            // 参数验证
            if (pattern == null || pattern.isEmpty()) {
                return Result.error("搜索模式不能为空");
            }
            if (path == null || path.isEmpty()) {
                return Result.error("搜索路径不能为空");
            }
            
            Path searchPath = Paths.get(path);
            
            // 编译正则表达式
            int flags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE;
            Pattern regex;
            try {
                regex = Pattern.compile(pattern, flags);
            } catch (PatternSyntaxException e) {
                return Result.error("无效的正则表达式: " + e.getMessage());
            }
            
            // 搜索文件
            StringBuilder results = new StringBuilder();
            int matchCount = 0;
            
            // try (Stream<Path> paths = Files.walk(searchPath)) {
            //     paths
            //         .filter(Files::isRegularFile)
            //         .filter(p -> matchesGlob(p, glob))
            //         .limit(1000)  // 限制搜索文件数
            //         .forEach(file -> {
            //             searchInFile(file, regex, results, showLineNumbers);
            //         });
            // }
            
            // 统计匹配结果
            // for (String line : results.toString().split("\n")) {
            //     if (line.contains(":")) matchCount++;
            // }
            
            // 构建最终结果
            StringBuilder output = new StringBuilder();
            output.append("搜索: \"").append(pattern).append("\"\n");
            output.append("路径: ").append(path).append("\n");
            if (glob != null) {
                output.append("过滤: ").append(glob).append("\n");
            }
            output.append("─".repeat(50)).append("\n\n");
            output.append(results);
            
            if (matchCount == 0) {
                output.append("\n未找到匹配");
            }
            
            return Result.success(output.toString());
            
        } catch (Exception e) {
            return Result.error("搜索失败: " + e.getMessage());
        }
    }
    
    /**
     * 在单个文件中搜索
     */
    // private void searchInFile(Path file, Pattern regex, StringBuilder results, boolean showLineNumbers) {
    //     try {
    //         List<String> lines = Files.readAllLines(file);
    //         
    //         for (int i = 0; i < lines.size(); i++) {
    //             String line = lines.get(i);
    //             if (regex.matcher(line).find()) {
    //                 if (showLineNumbers) {
    //                     results.append(file).append(":").append(i + 1).append(": ");
    //                 } else {
    //                     results.append(file).append(": ");
    //                 }
    //                 results.append(line).append("\n");
    //             }
    //         }
    //     } catch (Exception e) {
    //         // 忽略无法读取的文件
    //     }
    // }
    
    /**
     * 检查文件是否匹配 glob
     */
    // private boolean matchesGlob(Path file, String glob) {
    //     if (glob == null || glob.isEmpty()) return true;
    //     // 实现 glob 匹配逻辑
    // }
}
