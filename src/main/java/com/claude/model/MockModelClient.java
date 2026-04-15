package com.claude.model;

import com.claude.core.Message;
import org.springframework.stereotype.Component;
import java.util.*;

/**
 * Mock 模型客户端 - 用于测试
 * 
 * 对应 TS 源码: 测试用的 mock 实现
 */
@Component
public class MockModelClient implements ModelClient {
    
    // private boolean mockMode = true;
    // private String apiKey;
    
    // ========== Mock 响应模板 ==========
    
    // private static final Map<String, String> MOCK_RESPONSES = Map.of(
    //     "hello", "你好！我是 Claude Code Java 版本。有什么可以帮助你的吗？",
    //     "help", "我可以帮你:\n1. 读取和搜索文件\n2. 执行 shell 命令\n3. 添加文件到上下文\n\n有什么具体任务需要我帮忙吗？"
    // );
    
    @Override
    public AIResponse chat(String systemPrompt, List<Message> messages) {
        // 获取用户最新消息
        // String userInput = messages.stream()
        //     .filter(m -> "user".equals(m.getRole()))
        //     .reduce((first, second) -> second)
        //     .map(Message::getContent)
        //     .orElse("");
        
        // 简单的 Mock 逻辑
        // if (userInput.contains("tool")) {
        //     // 返回一个工具调用示例
        //     Message.ToolCall toolCall = new Message.ToolCall();
        //     toolCall.setId(UUID.randomUUID().toString());
        //     toolCall.setName("ReadFile");
        //     toolCall.setInput(Map.of("path", "/test/file.txt"));
        //     return AIResponse.toolCalls(List.of(toolCall));
        // }
        
        // 检查是否有预设回复
        // for (Map.Entry<String, String> entry : MOCK_RESPONSES.entrySet()) {
        //     if (userInput.toLowerCase().contains(entry.getKey())) {
        //         return AIResponse.text(entry.getValue());
        //     }
        // }
        
        // 默认回复
        // return AIResponse.text("这是 Mock 模式的回复。你输入了: " + userInput);
        
        return AIResponse.text("Mock 模式 - 请实现具体逻辑");
    }
    
    @Override
    public void setApiKey(String apiKey) {
        // this.apiKey = apiKey;
    }
    
    @Override
    public void setMockMode(boolean mock) {
        // this.mockMode = mock;
    }
    
    @Override
    public boolean isMockMode() {
        return true;
    }
    
    // ========== Mock 辅助方法 ==========
    
    /**
     * 设置自定义 Mock 响应
     */
    // public void setMockResponse(String trigger, String response) {
    //     MOCK_RESPONSES.put(trigger, response);
    // }
    
    /**
     * 清除所有自定义 Mock 响应
     */
    // public void clearMockResponses() {
    //     MOCK_RESPONSES.clear();
    // }
    
    /**
     * 模拟 AI 思考过程
     */
    // private String simulateThinking(String input) {
    //     // 模拟 AI 的思考过程
    //     return "让我分析一下你的问题...\n" +
    //            "输入: " + input + "\n" +
    //            "这是一个简单的测试输入。";
    // }
    
    /**
     * 模拟工具调用检测
     */
    // private boolean shouldUseTool(String input) {
    //     // 简单的关键词检测
    //     String[] toolKeywords = {"read", "file", "search", "grep", "bash", "run", "execute"};
    //     String lowerInput = input.toLowerCase();
    //     
    //     for (String keyword : toolKeywords) {
    //         if (lowerInput.contains(keyword)) {
    //             return true;
    //         }
    //     }
    //     
    //     return false;
    // }
}
