# JavaAgent CLI

> 基于Java标准库的Agent CLI，包含状态机驱动的Agent Loop、审批机制和工具系统

## 项目概述

JavaAgent CLI 是一个用 **Java 21** 标准库实现的通用 Agent CLI 框架。采用状态机模式驱动 Agent 主循环，内置审批机制确保敏感操作的安全性，并提供可扩展的工具执行框架。项目遵循纯标准库原则，不依赖 Spring 等框架，保持轻量与可控。

### 核心特性

- **Agent Loop 状态机** — 清晰的状态转换逻辑，5 种状态覆盖完整交互流程
- **审批机制** — 敏感工具（文件写入/删除/Shell执行）执行前需用户确认
- **会话持久化** — 自动保存/恢复最后一次会话到 JSON 文件
- **工具系统** — 可扩展的工具注册与执行框架，内置文件读取、搜索、Shell 三种工具
- **Mock 模式** — 开发阶段无需真实 API 即可运行和调试
- **纯标准库** — 零框架依赖，仅用 Java 标准库 + Jackson JSON

## 快速开始

### 环境要求

- **JDK 21+**（使用 Record、Switch 表达式等新特性）
- **Maven 3.8+**

### 编译与运行

```bash
# 编译
mvn clean compile

# 打包（生成可执行 fat JAR）
mvn clean package

# 方式一：直接运行 fat JAR
java -jar target/javaagent-cli-1.0.0.jar

# 方式二：指定 classpath 运行
java -cp "target/classes;target/lib/*" com.javagent.JavaAgentCLI

# 使用 mock 模式运行（无需 API Key）
java -jar target/javaagent-cli-1.0.0.jar --mock

# 指定 API Key
java -jar target/javaagent-cli-1.0.0.jar --api-key sk-xxx
```

### 命令行参数

| 参数 | 说明 |
|------|------|
| `--mock` | 启用 Mock 模式（默认已启用） |
| `--real` | 切换到真实 API 模式 |
| `--api-key <key>` | 设置 API Key |
| `--help` | 显示帮助信息 |

## 交互式命令

CLI 启动后支持以下内置命令：

| 命令 | 说明 |
|------|------|
| `/help` | 显示帮助信息 |
| `/exit` `/quit` | 退出 CLI |
| `/clear` | 清空当前会话 |
| `/save` | 手动保存当前会话 |
| `/load` | 加载上一次会话 |
| `/mode mock` | 切换到 Mock 模式 |
| `/mode real` | 切换到真实 API 模式 |
| `/tools` | 列出所有已注册工具 |

## 核心架构

### 数据流

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

### Agent Loop 状态机

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

| 状态 | 说明 | 转换条件 |
|------|------|----------|
| `IDLE` | 空闲，等待用户输入 | 收到输入 → `PROCESSING` |
| `PROCESSING` | 处理输入，调用 AI 模型 | 有工具调用 → `WAITING_APPROVAL`；纯文本 → `RESPONDING`；异常 → `ERROR` |
| `WAITING_APPROVAL` | 等待用户审批敏感工具 | 用户批准 → `EXECUTING_TOOL`；拒绝 → `IDLE` |
| `EXECUTING_TOOL` | 执行已批准的工具 | 成功 → `PROCESSING`（继续循环）；失败 → `ERROR` |
| `RESPONDING` | 生成并返回 AI 响应 | 完成 → `IDLE`；异常 → `ERROR` |
| `ERROR` | 错误状态 | 终态，返回错误信息 |

### 审批流程

```
AI请求工具执行
    ↓
检查工具是否需要审批
    ↓
├─ 不需要审批（只读工具） ──▶ 直接执行
│
└─ 需要审批（写操作/Shell）
    ↓
显示审批提示（工具名 + 参数）
    ↓
等待用户输入 (yes/no/cancel)
    ↓
├─ yes   ──▶ 执行工具
├─ no    ──▶ 拒绝执行，返回错误
└─ cancel ──▶ 取消操作
```

**需要审批的工具**：`write_file`、`delete_file`、`bash`、`exec`

**自动批准的工具**：`read_file`、`grep` 等只读工具

### 工具结果回灌

工具执行完成后，结果会通过 `injectToolResults()` 注入 Agent 上下文，重新进入 `PROCESSING` 状态，让 AI 基于工具结果继续推理：

```
工具执行完成 → 创建 ToolResult → 创建 tool 消息 → 添加到 ConversationManager
    → 更新 Agent 上下文 → 重新进入 PROCESSING 状态 → 发送带工具结果的新请求
```

## 项目结构

```
src/main/java/com/javagent/
├── JavaAgentCLI.java              # 主入口，CLI 交互循环
├── core/                          # 核心模块
│   ├── Agent.java                 # Agent 状态机（伪代码）
│   ├── Config.java                # 配置管理，Properties 持久化
│   └── ConversationManager.java   # 会话管理，JSON 持久化
├── model/                         # 模型层
│   ├── Message.java               # 消息模型（Record），支持 4 种角色
│   └── ModelClient.java           # 模型客户端（伪代码）
└── tools/                         # 工具系统
    ├── Tool.java                  # 工具接口定义
    ├── ToolResult.java            # 工具结果封装
    ├── ToolRegistry.java          # 工具注册表，管理工具生命周期
    ├── ReadFileTool.java          # 文件读取工具（只读，免审批）
    ├── GrepTool.java              # 文件内容搜索工具（只读，免审批）
    └── BashTool.java              # Shell 执行工具（需审批）
```

## 实现状态

| 文件 | 状态 | 说明 |
|------|------|------|
| `Message.java` | ✅ 实现 | Record 类型消息模型，支持 user/assistant/tool/system 四种角色 |
| `Tool.java` | ✅ 实现 | 工具接口定义（name/description/execute） |
| `ToolResult.java` | ✅ 实现 | 工具结果封装（content + isError） |
| `Config.java` | ✅ 实现 | Properties 配置管理，支持多路径查找与自动保存 |
| `ConversationManager.java` | ✅ 实现 | 会话管理，保存最后一次会话到 JSON |
| `ReadFileTool.java` | ✅ 实现 | 文件读取工具 |
| `GrepTool.java` | ✅ 实现 | 文件内容搜索工具 |
| `BashTool.java` | ✅ 实现 | Shell 命令执行工具 |
| `ToolRegistry.java` | ✅ 实现 | 工具注册表与执行调度 |
| `JavaAgentCLI.java` | ✅ 实现 | CLI 主入口与交互循环 |
| `Agent.java` | 🔶 伪代码 | 状态机核心逻辑，需补充审批交互与多轮工具调用 |
| `ModelClient.java` | 🔶 伪代码 | AI 模型调用，需对接真实 API |

> ✅ = 可运行的实现代码　🔶 = 伪代码/设计稿，需补充完整逻辑

## 配置

### 配置文件

配置文件查找顺序：
1. 当前目录下的 `config.properties`
2. 用户目录下的 `~/.javaagent/config.properties`
3. 以上都不存在时，自动在 `~/.javaagent/` 下创建默认配置

### 配置项

配置文件 `~/.javaagent/config.properties`：

```properties
agent.mock_mode=true
agent.api_key=
agent.base_url=https://api.example.com/v1
agent.auto_save=true
agent.max_tokens=200000
agent.approval_required=true
```

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `agent.mock_mode` | `true` | Mock 模式，不调用真实 API |
| `agent.api_key` | 空 | AI 模型 API Key |
| `agent.base_url` | `https://api.example.com/v1` | API 基础地址 |
| `agent.auto_save` | `true` | 是否自动保存会话 |
| `agent.max_tokens` | `200000` | 最大 Token 数 |
| `agent.approval_required` | `true` | 是否启用审批机制 |

### 会话存储

- 最后一次会话保存为 JSON 文件（`last_session.json`）
- 存储位置：优先当前目录，否则 `~/.javaagent/`
- 包含字段：`startTime`、`lastUpdated`、`messages`、`lastResponse`

## 技术栈

| 依赖 | 版本 | 用途 |
|------|------|------|
| Java 21 | — | Record、Switch 表达式、模式匹配 |
| Jackson Databind | 2.15.2 | JSON 序列化 |
| Jackson JSR-310 | 2.15.2 | Java Time 类型支持 |
| Lombok | 1.18.30 | 减少样板代码（provided） |
| JUnit Jupiter | 5.10.0 | 单元测试（test） |
| Maven Shade Plugin | 3.5.0 | 打包可执行 fat JAR |

## 设计要点

### 1. 状态机驱动

Agent 使用枚举状态机（`AgentState`）管理整个对话流程，每个状态有明确的职责和转换条件，避免了复杂的 if-else 嵌套，便于扩展新状态。

### 2. 审批机制

敏感工具（如 BashTool）在 `WAITING_APPROVAL` 状态等待用户确认，通过后才进入 `EXECUTING_TOOL` 状态。审批结果会被缓存，避免重复确认相同操作。

### 3. 消息模型

使用 Java Record 实现 `Message` 不可变消息模型，支持 4 种角色（user/assistant/tool/system），内置 `ToolCall` 和 `ToolResult` 嵌套记录，类型安全且不可变。

### 4. 会话简化

只保存**最后一次会话**到单个 JSON 文件，不实现复杂的 session 管理。重启后自动恢复上一次对话上下文。

### 5. 纯标准库

不使用 Spring Boot 或 WebFlux，仅用 Java 标准库实现，降低依赖复杂度，便于理解和定制。

## TODO

- [ ] 完善 Agent 状态机完整逻辑（审批交互、多轮工具调用）
- [ ] 实现真实 AI 模型调用（对接 OpenAI/Claude API）
- [ ] 添加流式响应支持
- [ ] 实现更多工具（文件写入、目录列表等）
- [ ] 支持多历史会话保存与切换
- [ ] 添加单元测试
- [ ] 支持系统提示词自定义

## 参考

- Claude Code 的 Agent Loop 设计
- 状态机模式在对话系统中的应用
