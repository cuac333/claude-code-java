package com.claude.tools;

import java.util.Map;
import java.util.function.Function;

/**
 * 工具接口
 * 
 * 对应 TS 源码:
 * - Tool.ts - 工具接口定义
 * - tools/*.ts - 各种工具实现
 */
public interface Tool {
    
    // ========== 工具元信息 ==========
    
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
    
    // ========== 工具执行 ==========
    
    /**
     * 执行工具
     * 
     * @param input 工具参数 (Map)
     * @return 执行结果
     * 
     * 对应 TS: Tool.execute()
     */
    Result execute(Map<String, Object> input);
    
    // ========== 工具属性 ==========
    
    /**
     * 是否启用
     */
    default boolean isEnabled() {
        return true;
    }
    
    /**
     * 是否只读工具 (不修改文件)
     */
    default boolean isReadOnly() {
        return false;
    }
    
    /**
     * 是否危险操作 (如删除、覆盖)
     */
    default boolean isDestructive() {
        return false;
    }
    
    // ========== 结果类 ==========
    
    /**
     * 工具执行结果
     */
    record Result(
        boolean success,      // 是否成功
        String content,       // 结果内容
        String error,         // 错误信息 (如果有)
        Map<String, Object> metadata  // 额外元数据
    ) {
        public static Result success(String content) {
            return new Result(true, content, null, null);
        }
        
        public static Result error(String error) {
            return new Result(false, null, error, null);
        }
    }
}
