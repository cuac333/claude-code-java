package com.claude.model;

import com.claude.core.Message;
import java.util.List;

/**
 * 模型客户端接口
 * 
 * 对应 TS 源码:
 * - services/api/claude.ts - Claude API 调用
 * - services/api/transport.ts - 传输层
 */
public interface ModelClient {
    
    // ========== 对话方法 ==========
    
    /**
     * 发送对话请求
     * 
     * @param systemPrompt 系统提示
     * @param messages 消息历史
     * @return AI 响应
     * 
     * 对应 TS: services/api/claude.ts 中的 chat/complete 方法
     */
    AIResponse chat(String systemPrompt, List<Message> messages);
    
    /**
     * 流式对话请求
     * 
     * @param systemPrompt 系统提示
     * @param messages 消息历史
     * @param callback 流式回调
     */
    // void chatStream(String systemPrompt, List<Message> messages, StreamCallback callback);
    
    // ========== 配置方法 ==========
    
    /**
     * 设置 API Key
     */
    void setApiKey(String apiKey);
    
    /**
     * 设置是否 Mock 模式
     */
    void setMockMode(boolean mock);
    
    /**
     * 检查是否 Mock 模式
     */
    boolean isMockMode();
    
    // ========== 内部类 ==========
    
    /**
     * AI 响应
     */
    record AIResponse(
        String content,                    // 文本内容
        List<Message.ToolCall> toolCalls,  // 工具调用
        String model,                      // 使用的模型
        Integer tokensUsed,                // 使用的 token 数
        Boolean isError                    // 是否错误
    ) {
        /**
         * 是否有工具调用
         */
        public boolean hasToolCalls() {
            return toolCalls != null && !toolCalls.isEmpty();
        }
        
        /**
         * 是否是错误响应
         */
        public boolean isError() {
            return Boolean.TRUE.equals(isError);
        }
        
        /**
         * 创建普通文本响应
         */
        public static AIResponse text(String content) {
            return new AIResponse(content, null, null, null, false);
        }
        
        /**
         * 创建工具调用响应
         */
        public static AIResponse toolCalls(List<Message.ToolCall> calls) {
            return new AIResponse(null, calls, null, null, false);
        }
        
        /**
         * 创建错误响应
         */
        public static AIResponse error(String message) {
            return new AIResponse(message, null, null, null, true);
        }
    }
    
    /**
     * 流式回调接口
     */
    // interface StreamCallback {
    //     void onChunk(String chunk);
    //     void onComplete(AIResponse response);
    //     void onError(Exception e);
    // }
}
