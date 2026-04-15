package com.javagent;

import com.javagent.core.Agent;
import com.javagent.core.Config;
import com.javagent.core.ConversationManager;
import com.javagent.tools.ToolRegistry;
import com.javagent.model.ModelClient;

import java.util.Scanner;

/**
 * JavaAgent CLI - 主入口
 * 基于Java标准库实现的通用Agent CLI，支持审批机制和工具执行
 */
public class JavaAgentCLI {
    
    private Agent agent;
    private Config config;
    private ConversationManager conversationManager;
    private ToolRegistry toolRegistry;
    private ModelClient modelClient;
    
    public static void main(String[] args) {
        new JavaAgentCLI().run(args);
    }
    
    private void run(String[] args) {
        // 初始化组件
        initializeComponents();
        
        // 解析命令行参数
        parseArgs(args);
        
        // 启动CLI
        startCLI();
    }
    
    private void initializeComponents() {
        System.out.println("Initializing JavaAgent CLI...");
        
        // 1. 加载配置
        config = Config.load();
        
        // 2. 初始化工具注册表
        toolRegistry = new ToolRegistry();
        toolRegistry.registerDefaultTools();
        
        // 3. 初始化模型客户端
        modelClient = ModelClient.create(config);
        
        // 4. 初始化会话管理器
        conversationManager = new ConversationManager(config);
        
        // 5. 初始化Agent
        agent = new Agent(config, modelClient, toolRegistry, conversationManager);
        
        System.out.println("✅ JavaAgent CLI initialized");
        System.out.println("Mode: " + (config.isMockMode() ? "MOCK" : "REAL"));
        System.out.println("Tools registered: " + toolRegistry.getToolCount());
    }
    
    private void parseArgs(String[] args) {
        // 简化参数解析，仅支持基本参数
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--mock":
                    config.setMockMode(true);
                    break;
                case "--real":
                    config.setMockMode(false);
                    break;
                case "--api-key":
                    if (i + 1 < args.length) {
                        config.setApiKey(args[++i]);
                    }
                    break;
                case "--help":
                    printHelp();
                    System.exit(0);
            }
        }
    }
    
    private void startCLI() {
        System.out.println("\n==========================================");
        System.out.println("JavaAgent CLI v1.0.0");
        System.out.println("Type /help for commands, /exit to quit");
        System.out.println("==========================================\n");
        
        // 加载上一次会话（如果存在）
        conversationManager.loadLastSession();
        
        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;
            
            while (running) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();
                
                if (input.isEmpty()) {
                    continue;
                }
                
                // 检查特殊命令
                if (input.startsWith("/")) {
                    if (handleCommand(input)) {
                        continue;
                    }
                }
                
                // 处理用户输入
                processUserInput(input);
            }
        }
    }
    
    private boolean handleCommand(String command) {
        String[] parts = command.split(" ", 2);
        String cmd = parts[0];
        String args = parts.length > 1 ? parts[1] : "";
        
        switch (cmd) {
            case "/exit":
            case "/quit":
                System.out.println("Goodbye!");
                System.exit(0);
                return true;
                
            case "/help":
                printHelp();
                return true;
                
            case "/clear":
                conversationManager.clearCurrentSession();
                System.out.println("✅ Session cleared");
                return true;
                
            case "/mode":
                if (args.equals("mock")) {
                    config.setMockMode(true);
                    modelClient.setMockMode(true);
                    System.out.println("✅ Switched to MOCK mode");
                } else if (args.equals("real")) {
                    config.setMockMode(false);
                    modelClient.setMockMode(false);
                    System.out.println("✅ Switched to REAL mode");
                } else {
                    System.out.println("Usage: /mode <mock|real>");
                }
                return true;
                
            case "/tools":
                System.out.println(toolRegistry.listTools());
                return true;
                
            case "/save":
                conversationManager.saveCurrentSession();
                System.out.println("✅ Session saved");
                return true;
                
            case "/load":
                conversationManager.loadLastSession();
                System.out.println("✅ Last session loaded");
                return true;
                
            default:
                System.out.println("❌ Unknown command: " + cmd);
                System.out.println("Type /help for available commands");
                return true;
        }
    }
    
    private void processUserInput(String input) {
        try {
            String response = agent.process(input);
            System.out.println("\n" + response + "\n");
            
            // 自动保存会话
            conversationManager.saveCurrentSession();
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void printHelp() {
        System.out.println("\n=== JavaAgent CLI Commands ===\n");
        System.out.println("  /help                Show this help");
        System.out.println("  /exit, /quit         Exit the CLI");
        System.out.println("  /clear               Clear current session");
        System.out.println("  /save                Save current session");
        System.out.println("  /load                Load last session");
        System.out.println("  /mode <mock|real>    Switch between mock/real mode");
        System.out.println("  /tools               List available tools");
        System.out.println("\n=== Interaction ===\n");
        System.out.println("  • Type anything to chat with the agent");
        System.out.println("  • Agent may ask for approval before executing certain tools");
        System.out.println("  • Type 'yes', 'no', or 'cancel' when prompted for approval");
    }
}