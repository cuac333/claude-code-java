package com.claude.tools;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;

/**
 * 工具注册表
 * 
 * 对应 TS 源码: Tool.ts 中的工具注册逻辑
 */
@Component
public class ToolRegistry {
    
    // private Map<String, Tool> tools = new HashMap<>();
    // private Map<String, Tool> aliases = new HashMap<>();
    
    public ToolRegistry() {
        // registerDefaultTools();
    }
    
    /**
     * 注册工具
     */
    // public void register(Tool tool) {
    //     // 注册主名称
    //     tools.put(tool.getName(), tool);
    //     
    //     // 注册别名
    //     for (String alias : tool.getAliases()) {
    //         aliases.put(alias, tool);
    //     }
    // }
    
    /**
     * 获取工具
     */
    // public Tool getTool(String name) {
    //     Tool tool = tools.get(name);
    //     if (tool != null) return tool;
    //     return aliases.get(name);
    // }
    
    /**
     * 获取所有工具
     */
    // public List<Tool> getAllTools() {
    //     return tools.values().stream().distinct().toList();
    // }
    
    /**
     * 获取所有工具定义 (用于发送给 AI)
     */
    // public List<Map<String, Object>> getToolDefinitions() {
    //     return getAllTools().stream()
    //         .map(tool -> Map.of(
    //             "name", tool.getName(),
    //             "description", tool.getDescription()
    //         ))
    //         .toList();
    // }
    
    /**
     * 注册默认工具
     * 
     * 对应 TS: tools/ 目录下的各种工具
     */
    // private void registerDefaultTools() {
    //     // 文件读取工具
    //     register(new ReadFileTool());
    //     
    //     // 文件搜索工具
    //     register(new GrepTool());
    //     
    //     // Shell 执行工具
    //     register(new BashTool());
    //     
    //     // 文件添加工具 (/add)
    //     register(new AddTool());
    //     
    //     // 等等...
    // }
    
    /**
     * 获取工具描述列表 (用于 AI 工具调用)
     * 
     * 对应 TS: tools.ts 中的工具列表
     */
    // public String getToolsDescription() {
    //     StringBuilder sb = new StringBuilder();
    //     sb.append("可用工具:\n\n");
    //     
    //     for (Tool tool : getAllTools()) {
    //         sb.append("- ").append(tool.getName());
    //         if (tool.getAliases().length > 0) {
    //             sb.append(" (").append(String.join(", ", tool.getAliases())).append(")");
    //         }
    //         sb.append(": ").append(tool.getDescription()).append("\n");
    //     }
    //     
    //     return sb.toString();
    // }
}
