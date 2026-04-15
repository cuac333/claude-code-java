# JavaAgent CLI

> 基于Java标准库的Agent CLI设计稿，包含状态机驱动的Agent Loop、审批机制和工具系统

## 项目概述

本项目是一个**设计稿/伪代码项目**，展示如何用Java标准库实现一个Agent CLI的核心架构。主要关注算法设计和状态机逻辑，而非完整实现。

### 核心设计

1. **Agent Loop 状态机** - 清晰的状态转换逻辑
2. **审批机制** - 敏感工具执行前需要用户确认
3. **会话持久化** - 最后一次会话保存到JSON文件
4. **工具系统** - 可扩展的工具执行框架

## 核心数据流

```
用户输入
    ↓
Agent.process(input)
    ↓
ConversationManager.addUserMessage()
    ↓
ModelClient.chat() → AIResponse
    ↓
├─ 需要工具调用?
│   ↓
│   Agent.checkApproval() → 用户确认
│   ↓
│   ToolRegistry.execute() → ToolResult
│   ↓
│   Agent.injectToolResults()
│   ↓ (循环)
└─ 文本响应
    ↓
ConversationManager.addAssistantMessage()
    ↓
ConversationManager.saveCurrentSession()
    ↓
返回给用户
```

## Agent Loop 状态机

```
┌─────────┐     ┌─────────────┐     ┌─────────────────┐
│  IDLE   │────▶│ PROCESSING  │────▶│ WAITING_APPROVAL│
└─────────┘     └─────────────┘     └─────────────────┘
     ▲                                      │
     │                                      ▼
     │                              ┌─────────────────┐
     │                              │ EXECUTING_TOOL  │
     │                              └─────────────────┘
     │                                      │
     │                                      ▼
     │                              ┌─────────────────┐
     └──────────────────────────────│   RESPONDING    │
                                    └─────────────────┘
```

### 状态说明

| 状态 | 说明 |
|------|------|
| `IDLE` | 空闲状态，等待用户输入 |
| `PROCESSING` | 处理用户输入，调用AI模型 |
| `WAITING_APPROVAL` | 等待用户对工具执行的审批 |
| `EXECUTING_TOOL` | 执行已批准的工具 |
| `RESPONDING` | 生成并返回AI响应 |
| `ERROR` | 错误状态 |

## 审批流程

```
AI请求工具执行
    ↓
检查工具是否需要审批
    ↓
├─ 不需要审批 ──▶ 直接执行
│
└─ 需要审批
    ↓
显示审批提示
    ↓
等待用户输入 (yes/no/cancel)
    ↓
├─ yes  ──▶ 执行工具
├─ no   ──▶ 拒绝执行，返回错误
└─ cancel ──▶ 取消操作
```

## 工具结果回灌流程

```
工具执行完成
    ↓
创建ToolResult
    ↓
创建tool消息 (Message.tool())
    ↓
添加到ConversationManager
    ↓
更新Agent上下文
    ↓
重新进入PROCESSING状态
    ↓
发送带工具结果的新请求给AI
```

## 项目结构

```
src/main/java/com/javagent/
├── JavaAgentCLI.java              # 主入口
├── core/                          # 核心模块
│   ├── Agent.java                 # Agent状态机（伪代码）
│   ├── Config.java                # 配置管理
│   └── ConversationManager.java   # 会话管理
├── model/                         # 模型层
│   ├── Message.java               # 消息模型（Record）
│   └── ModelClient.java           # 模型客户端（伪代码）
└── tools/                         # 工具系统
    ├── Tool.java                  # 工具接口
    ├── ToolResult.java            # 工具结果
    ├── ToolRegistry.java          # 工具注册表
    ├── ReadFileTool.java          # 文件读取工具
    ├── GrepTool.java              # 文件搜索工具
    └── BashTool.java              # Shell执行工具（需审批）
```

## 实现状态

| 文件 | 状态 | 说明 |
|------|------|------|
| `Message.java` | ✅ 实现 | Record类型消息模型 |
| `Tool.java` | ✅ 实现 | 工具接口定义 |
| `ToolResult.java` | ✅ 实现 | 工具结果封装 |
| `Config.java` | ✅ 实现 | 配置管理 |
| `ConversationManager.java` | ✅ 实现 | 最后一次会话保存 |
| `ReadFileTool.java` | ✅ 实现 | 文件读取实现 |
| `GrepTool.java` | ✅ 实现 | 文件搜索实现 |
| `BashTool.java` | ✅ 实现 | Shell执行实现 |
| `ToolRegistry.java` | ✅ 实现 | 工具注册表 |
| `JavaAgentCLI.java` | ✅ 实现 | CLI主入口 |
| `Agent.java` | 🔶 伪代码 | 状态机核心逻辑 |
| `ModelClient.java` | 🔶 伪代码 | AI模型调用 |

> ✅ = 可运行的实现代码  
> 🔶 = 伪代码/设计稿，需补充完整逻辑

## 技术栈

- **Java 21+** - 使用Record、Switch表达式等新特性
- **Jackson** - JSON序列化
- **Java标准库** - HttpClient、Scanner等
- **Maven** - 构建工具

## 运行方式

```bash
# 编译
mvn clean compile

# 运行
java -cp "target/classes;target/lib/*" com.javagent.JavaAgentCLI
```

## 配置

配置文件 `~/.javaagent/config.properties`:

```properties
agent.mock_mode=true
agent.api_key=
agent.base_url=https://api.example.com/v1
agent.auto_save=true
agent.approval_required=true
```

## 设计要点

### 1. 状态机设计

Agent使用枚举状态机管理整个对话流程，每个状态有明确的职责和转换条件。

### 2. 审批机制

敏感工具（如BashTool）在`WAITING_APPROVAL`状态等待用户确认，通过后才进入`EXECUTING_TOOL`状态。

### 3. 会话简化

只保存**最后一次会话**到单个JSON文件，不实现复杂的session管理。

### 4. 纯标准库

不使用Spring Boot或WebFlux，仅用Java标准库实现，降低依赖复杂度。

## TODO

- [ ] 完善Agent状态机完整逻辑
- [ ] 实现真实AI模型调用
- [ ] 添加流式响应支持
- [ ] 实现更多工具
- [ ] 添加单元测试

## 参考

- Claude Code的Agent Loop设计
- 状态机模式在对话系统中的应用
