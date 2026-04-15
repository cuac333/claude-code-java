package com.javagent.tools;

import java.util.*;

/**
 * 工具注册表 - 管理所有可用工具
 */
public class ToolRegistry {
    
    private final Map<String, Tool> tools = new HashMap<>();
    private final Map<String, Tool> aliases = new HashMap<>();
    
    public ToolRegistry() {
        // 构造函数中注册默认工具
    }
    
    /**
     * 注册工具
     */
    public void register(Tool tool) {
        // 注册主名称
        tools.put(tool.getName().toLowerCase(), tool);
        
        // 注册别名
        for (String alias : tool.getAliases()) {
            aliases.put(alias.toLowerCase(), tool);
        }
    }
    
    /**
     * 注册默认工具
     */
    public void registerDefaultTools() {
        register(new ReadFileTool());
        register(new GrepTool());
        register(new BashTool());
    }
    
    /**
     * 获取工具
     */
    public Tool getTool(String name) {
        String lowerName = name.toLowerCase();
        Tool tool = tools.get(lowerName);
        if (tool != null) {
            return tool;
        }
        return aliases.get(lowerName);
    }
    
    /**
     * 执行工具
     */
    public ToolResult execute(String name, Map<String, Object> input) {
        Tool tool = getTool(name);
        if (tool == null) {
            return ToolResult.error("Tool not found: " + name);
        }
        
        try {
            return tool.execute(input);
        } catch (Exception e) {
            return ToolResult.error("Tool execution failed: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有工具
     */
    public Collection<Tool> getAllTools() {
        return new HashSet<>(tools.values());
    }
    
    /**
     * 获取工具数量
     */
    public int getToolCount() {
        return tools.size();
    }
    
    /**
     * 列出所有工具
     */
    public String listTools() {
        StringBuilder sb = new StringBuilder();
        sb.append("Available Tools:\n\n");
        
        for (Tool tool : getAllTools()) {
            sb.append("  ").append(tool.getName());
            if (tool.getAliases().length > 0) {
                sb.append(" (").append(String.join(", ", tool.getAliases())).append(")");
            }
            sb.append("\n    ").append(tool.getDescription());
            if (tool.requiresApproval()) {
                sb.append(" [requires approval]");
            }
            sb.append("\n\n");
        }
        
        return sb.toString();
    }
    
    /**
     * 获取工具描述列表
     */
    public List<String> getToolDescriptions() {
        List<String> descriptions = new ArrayList<>();
        for (Tool tool : getAllTools()) {
            String desc = tool.getName() + ": " + tool.getDescription();
            if (tool.requiresApproval()) {
                desc += " (requires approval)";
            }
            descriptions.add(desc);
        }
        return descriptions;
    }
}
