package com.javagent.tools;

import java.util.Map;

/**
 * 工具接口定义
 */
public interface Tool {
    
    /**
     * 获取工具名称
     */
    String getName();
    
    /**
     * 获取工具描述
     */
    String getDescription();
    
    /**
     * 获取工具别名
     */
    default String[] getAliases() {
        return new String[0];
    }
    
    /**
     * 执行工具
     * @param input 工具参数
     * @return 工具执行结果
     */
    ToolResult execute(Map<String, Object> input);
    
    /**
     * 是否需要审批
     */
    default boolean requiresApproval() {
        return false;
    }
    
    /**
     * 是否为只读工具（不修改系统状态）
     */
    default boolean isReadOnly() {
        return true;
    }
    
    /**
     * 是否为破坏性操作（删除、覆盖等）
     */
    default boolean isDestructive() {
        return false;
    }
}
