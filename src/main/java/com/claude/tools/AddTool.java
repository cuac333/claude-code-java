package com.claude.tools;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * AddTool - 添加文件到上下文
 * 
 * 对应 TS 源码:
 * - commands/add-dir/ 目录
 * - tools/AddTool.ts
 * 
 * 功能:
 * - /add <file> - 添加单个文件
 * - /add <dir> - 添加目录下所有文件
 * - /add <glob> - 添加匹配 glob 的文件
 */
@Component
public class AddTool implements Tool {
    
    @Autowired
    private com.claude.core.Context context;
    
    @Override
    public String getName() {
        return "add";
    }
    
    @Override
    public String getDescription() {
        return "添加文件到上下文，支持单个文件、目录或 glob 模式";
    }
    
    @Override
    public String[] getAliases() {
        return new String[]{"add-file", "add-dir"};
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
            boolean recursive = input.containsKey("recursive") && (Boolean) input.get("recursive");
            String glob = (String) input.get("glob");  // 可选，glob 模式
            
            if (path == null || path.isEmpty()) {
                return Result.error("路径不能为空");
            }
            
            Path targetPath = Paths.get(path);
            
            // 检查路径是否存在
            if (!Files.exists(targetPath)) {
                return Result.error("路径不存在: " + path);
            }
            
            // 判断是文件还是目录
            if (Files.isRegularFile(targetPath)) {
                return addFile(targetPath);
            } else if (Files.isDirectory(targetPath)) {
                return addDirectory(targetPath, recursive, glob);
            } else {
                return Result.error("不支持的路径类型: " + path);
            }
            
        } catch (Exception e) {
            return Result.error("添加文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 添加单个文件
     */
    // private Result addFile(Path path) {
    //     try {
    //         // 读取文件内容
    //         String content = Files.readString(path);
    //         
    //         // 添加到上下文
    //         context.addFile(path.toString(), content);
    //         
    //         int lines = content.split("\n").length;
    //         return Result.success("已添加文件: " + path + " (" + lines + " 行)");
    //         
    //     } catch (Exception e) {
    //         return Result.error("读取文件失败: " + e.getMessage());
    //     }
    // }
    
    /**
     * 添加目录下的文件
     */
    // private Result addDirectory(Path dir, boolean recursive, String glob) {
    //     // 实现目录遍历和 glob 匹配
    //     // 类似于 TS 中的 glob.ts 和 AddTool 的实现
    //     
    //     // List<String> addedFiles = new ArrayList<>();
    //     // int totalLines = 0;
    //     
    //     // for (Path file : findMatchingFiles(dir, glob, recursive)) {
    //     //     String content = Files.readString(file);
    //     //     context.addFile(file.toString(), content);
    //     //     addedFiles.add(file.toString());
    //     //     totalLines += content.split("\n").length;
    //     // }
    //     
    //     // return Result.success("已添加 " + addedFiles.size() + " 个文件, 共 " + totalLines + " 行");
    // }
    
    /**
     * 查找匹配的文件
     */
    // private List<Path> findMatchingFiles(Path dir, String glob, boolean recursive) {
    //     // 使用 Java NIO 的 PathMatcher 或第三方库如 io.github._2guzs.querystar
    //     // 实现 glob 匹配逻辑
    // }
}
