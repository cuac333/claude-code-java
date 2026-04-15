package com.javagent.core;

import com.javagent.model.ModelClient;
import com.javagent.model.ModelClient.AIResponse;
import com.javagent.model.Message;
import com.javagent.tools.ToolRegistry;
import com.javagent.tools.ToolResult;

import java.util.*;

/**
 * Agent 核心状态机 - 实现Agent主循环逻辑
 * 基于状态机模式的Agent Loop伪代码实现
 */
public class Agent {
    
    // Agent状态枚举
    private enum AgentState {
        IDLE,              // 空闲状态
        PROCESSING,        // 处理用户输入
        WAITING_APPROVAL,  // 等待审批
        EXECUTING_TOOL,    // 执行工具
        RESPONDING,        // 生成响应
        ERROR              // 错误状态
    }
    
    private AgentState currentState = AgentState.IDLE;
    private final Config config;
    private final ModelClient modelClient;
    private final ToolRegistry toolRegistry;
    private final ConversationManager conversationManager;
    
    // 暂存数据
    private String currentInput;
    private List<Message> currentContext;
    private AIResponse currentAiResponse;
    private String pendingApprovalTool;
    private Map<String, Object> pendingApprovalArgs;
    
    // 审批相关
    private boolean approvalRequired = true;  // 默认需要审批
    private final Map<String, Boolean> approvalCache = new HashMap<>();
    
    public Agent(Config config, ModelClient modelClient, 
                 ToolRegistry toolRegistry, ConversationManager conversationManager) {
        this.config = config;
        this.modelClient = modelClient;
        this.toolRegistry = toolRegistry;
        this.conversationManager = conversationManager;
    }
    
    /**
     * Agent主循环伪代码
     * 状态机驱动的Agent Loop实现
     */
    public String process(String userInput) {
        // 1. 初始化状态和数据
        initializeProcessing(userInput);
        
        // 2. 状态机循环
        while (currentState != AgentState.IDLE && currentState != AgentState.ERROR) {
            switch (currentState) {
                case PROCESSING:
                    handleProcessing();
                    break;
                    
                case WAITING_APPROVAL:
                    // 由CLI线程处理，这里只是状态占位
                    // 实际实现中会等待用户审批输入
                    break;
                    
                case EXECUTING_TOOL:
                    executeTool();
                    break;
                    
                case RESPONDING:
                    generateResponse();
                    break;
                    
                default:
                    currentState = AgentState.ERROR;
                    break;
            }
        }
        
        // 3. 返回结果或错误
        if (currentState == AgentState.ERROR) {
            return "Agent encountered an error. Please try again.";
        }
        
        return conversationManager.getLastResponse();
    }
    
    /**
     * 初始化处理过程
     */
    private void initializeProcessing(String userInput) {
        currentInput = userInput;
        currentContext = conversationManager.getCurrentContext();
        currentState = AgentState.PROCESSING;
        
        // 添加用户消息到会话
        conversationManager.addUserMessage(userInput);
    }
    
    /**
     * 处理用户输入状态
     */
    private void handleProcessing() {
        try {
            // 1. 构建系统提示
            String systemPrompt = buildSystemPrompt();
            
            // 2. 发送请求到AI模型
            currentAiResponse = modelClient.chat(systemPrompt, currentContext);
            
            // 3. 根据响应类型转移状态
            if (currentAiResponse.isError()) {
                currentState = AgentState.ERROR;
            } else if (currentAiResponse.hasToolCalls()) {
                currentState = AgentState.WAITING_APPROVAL;
            } else {
                currentState = AgentState.RESPONDING;
            }
            
        } catch (Exception e) {
            System.err.println("Processing error: " + e.getMessage());
            currentState = AgentState.ERROR;
        }
    }
    
    /**
     * 审批流程伪代码
     */
    public boolean checkApproval(String toolName, Map<String, Object> args) {
        // 1. 检查是否需要审批
        if (!approvalRequired) {
            return true;  // 自动批准
        }
        
        // 2. 检查审批缓存
        String cacheKey = toolName + ":" + args.hashCode();
        if (approvalCache.containsKey(cacheKey)) {
            return approvalCache.get(cacheKey);
        }
        
        // 3. 审批规则检查
        boolean approved = evaluateApprovalRules(toolName, args);
        
        // 4. 缓存审批结果
        approvalCache.put(cacheKey, approved);
        
        return approved;
    }
    
    /**
     * 工具结果回灌流程伪代码
     */
    public void injectToolResults(String toolCallId, ToolResult result) {
        // 1. 创建工具结果消息
        Message toolMessage = Message.tool(toolCallId, result.content(), result.isError());
        
        // 2. 添加到上下文
        conversationManager.addToolResult(toolMessage);
        
        // 3. 如果工具执行成功，继续Agent循环
        if (!result.isError()) {
            // 重新构建上下文
            currentContext = conversationManager.getCurrentContext();
            currentState = AgentState.PROCESSING;
        } else {
            currentState = AgentState.ERROR;
        }
    }
    
    /**
     * 执行工具状态
     */
    private void executeTool() {
        try {
            // 1. 执行工具调用
            List<Message.ToolCall> toolCalls = currentAiResponse.toolCalls();
            
            for (Message.ToolCall toolCall : toolCalls) {
                // 2. 检查审批（对于敏感工具）
                if (requiresApproval(toolCall.name())) {
                    boolean approved = checkApproval(toolCall.name(), toolCall.input());
                    if (!approved) {
                        System.out.println("❌ Tool execution denied by approval system");
                        currentState = AgentState.ERROR;
                        return;
                    }
                }
                
                // 3. 执行工具
                ToolResult result = toolRegistry.execute(toolCall.name(), toolCall.input());
                
                // 4. 注入结果到Agent循环
                injectToolResults(toolCall.id(), result);
                
                // 5. 更新状态
                if (result.isError()) {
                    currentState = AgentState.ERROR;
                } else {
                    currentState = AgentState.PROCESSING;
                }
            }
            
        } catch (Exception e) {
            System.err.println("Tool execution error: " + e.getMessage());
            currentState = AgentState.ERROR;
        }
    }
    
    /**
     * 生成响应状态
     */
    private void generateResponse() {
        try {
            // 1. 从AI响应中提取文本内容
            String responseText = currentAiResponse.content();
            
            // 2. 添加助手消息到会话
            conversationManager.addAssistantMessage(responseText);
            
            // 3. 保存会话（可选）
            if (config.shouldAutoSave()) {
                conversationManager.saveCurrentSession();
            }
            
            // 4. 返回空闲状态
            currentState = AgentState.IDLE;
            
        } catch (Exception e) {
            System.err.println("Response generation error: " + e.getMessage());
            currentState = AgentState.ERROR;
        }
    }
    
    // ========== 辅助方法 ==========
    
    private String buildSystemPrompt() {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("# JavaAgent CLI System Prompt\n\n");
        prompt.append("You are a helpful coding assistant with access to tools.\n");
        prompt.append("Available tools:\n");
        
        for (String toolDesc : toolRegistry.getToolDescriptions()) {
            prompt.append("- ").append(toolDesc).append("\n");
        }
        
        prompt.append("\nRules:\n");
        prompt.append("1. Always ask for approval before executing potentially dangerous tools\n");
        prompt.append("2. Provide clear explanations for your actions\n");
        prompt.append("3. Use tools when appropriate to gather information or execute tasks\n");
        
        return prompt.toString();
    }
    
    private boolean evaluateApprovalRules(String toolName, Map<String, Object> args) {
        // 简化实现：基于工具名称的审批规则
        switch (toolName) {
            case "write_file":
            case "delete_file":
            case "bash":  // shell执行
                // 对于这些工具，返回false（需要用户手动审批）
                // 实际实现中应该显示审批对话框
                System.out.println("⚠️ Approval required for tool: " + toolName);
                System.out.println("Args: " + args);
                System.out.print("Approve? (yes/no/cancel): ");
                // 实际实现中应该等待用户输入
                return false;
                
            default:
                // 对于只读工具，自动批准
                return true;
        }
    }
    
    private boolean requiresApproval(String toolName) {
        // 需要审批的工具列表
        String[] approvalTools = {"write_file", "delete_file", "bash", "exec"};
        return Arrays.asList(approvalTools).contains(toolName);
    }
    
    // ========== 状态查询方法 ==========
    
    public boolean isWaitingForApproval() {
        return currentState == AgentState.WAITING_APPROVAL;
    }
    
    public String getPendingApprovalDetails() {
        if (currentAiResponse != null && currentAiResponse.hasToolCalls()) {
            List<Message.ToolCall> toolCalls = currentAiResponse.toolCalls();
            if (toolCalls != null && !toolCalls.isEmpty()) {
                Message.ToolCall firstCall = toolCalls.get(0);
                return "Tool: " + firstCall.name() + ", Args: " + firstCall.input();
            }
        }
        return "";
    }
    
    public void setApprovalResult(boolean approved) {
        if (approved) {
            currentState = AgentState.EXECUTING_TOOL;
        } else {
            currentState = AgentState.IDLE;
        }
    }
}