# Claude Code Java

> Claude Code 核心逻辑的 Java 伪代码实现（设计稿）

## ⚠️ 项目状态：伪代码 / 设计稿

**本项目目前处于设计阶段，大部分代码为伪代码骨架，核心逻辑均被注释掉，尚不可运行。**

代码中大量方法体以注释形式存在，仅作为架构设计和未来实现的参考。只有少数数据模型和接口定义是可以编译通过的。

---

## 项目概述

将 Claude Code 的 TypeScript 源码转换为 Java/Spring Boot 实现，保留核心功能。

本项目当前的价值：
- 作为 **架构设计参考**，展示各模块的职责和交互方式
- 作为 **开发蓝图**，注释中的伪代码描述了目标实现逻辑
- 作为 **类型定义**，`Message`、`Tool`、`Config` 等模型已基本成型

## 项目结构

```
claude-code-java/
├── pom.xml                          # Maven 配置
└── src/main/java/com/claude/
    ├── ClaudeCodeApplication.java   # 主入口 (伪代码)
    │
    ├── config/
    │   └── Config.java              # 配置类 ✅
    │
    ├── core/                        # 核心模块
    │   ├── Agent.java               # Agent 循环 (伪代码)
    │   ├── Context.java             # 上下文管理 (伪代码)
    │   └── Message.java             # 消息模型 ✅
    │
    ├── tools/                        # 工具系统
    │   ├── Tool.java                # 工具接口 ✅
    │   ├── ToolRegistry.java        # 工具注册表 (伪代码)
    │   ├── AddTool.java             # /add 添加工具 (伪代码)
    │   ├── ReadFileTool.java       # 文件读取 ✅ (唯一可运行的工具)
    │   ├── GrepTool.java           # 搜索工具 (伪代码)
    │   └── BashTool.java           # Shell 执行 (伪代码)
    │
    ├── model/                        # AI 模型层
    │   ├── ModelClient.java         # 模型客户端接口 ✅
    │   ├── MockModelClient.java     # Mock 实现 (伪代码)
    │   └── ClaudeApiClient.java     # 真实 API (伪代码)
    │
    └── history/                      # 历史管理
        ├── HistoryManager.java      # 历史管理 (伪代码)
        └── JsonHistoryStore.java    # JSON 持久化 (伪代码)
```

## 伪代码 / 实现状态

| 文件 | 状态 | 说明 |
|------|------|------|
| `Message.java` | ✅ 实现 | 消息模型，包含 ToolCall、ToolResult、工厂方法 |
| `Config.java` | ✅ 实现 | 配置类，支持 mock/real 切换 |
| `Tool.java` | ✅ 实现 | 工具接口 + Result record |
| `ReadFileTool.java` | ✅ 实现 | 可运行的读取文件工具 |
| `ModelClient.java` | ✅ 实现 | 接口定义 + AIResponse record |
| `AddTool.java` | 🔶 伪代码 | execute 有骨架，addFile/addDirectory 被注释 |
| `GrepTool.java` | 🔶 伪代码 | execute 有骨架，searchInFile/matchesGlob 被注释 |
| `BashTool.java` | 🔶 伪代码 | execute 有骨架，核心 ProcessBuilder 逻辑被注释 |
| `ToolRegistry.java` | 🔶 伪代码 | 所有方法都被注释 |
| `Agent.java` | 🔶 伪代码 | 所有方法都被注释 |
| `Context.java` | 🔶 伪代码 | 所有方法都被注释 |
| `MockModelClient.java` | 🔶 伪代码 | chat() 返回固定字符串，其余逻辑被注释 |
| `ClaudeApiClient.java` | 🔶 伪代码 | WebClient 初始化和 API 调用都被注释 |
| `HistoryManager.java` | 🔶 伪代码 | 所有方法都被注释 |
| `JsonHistoryStore.java` | 🔶 伪代码 | 所有方法都被注释 |
| `ClaudeCodeApplication.java` | 🔶 伪代码 | REPL 循环被注释 |

> ✅ = 可编译运行的实现代码
> 🔶 = 伪代码骨架，方法体被注释，需补充逻辑

## 核心数据流（设计）

```
用户输入 (CLI)
    ↓
Agent.process(input)
    ↓
Context.addUserMessage()
    ↓
ModelClient.chat() → AI 响应
    ↓
├─ 有工具调用 → ToolRegistry.execute() → Context.addToolResult()
    ↓ (循环)
└─ 文本回复 → Context.addAssistantMessage()
    ↓
HistoryManager.saveSession()
    ↓
返回给用户
```

## 计划命令

| 命令 | 说明 |
|------|------|
| `/add <path>` | 添加文件到上下文 |
| `/model mock` | 切换到 Mock 模式 |
| `/model real` | 切换到真实 API |
| `/history` | 查看会话历史 |
| `/clear` | 清空上下文 |
| `/help` | 显示帮助 |
| `/exit` | 退出程序 |

## 编译

```bash
mvn clean compile
```

> 注：不可编译通过

## 技术栈

- Java 21
- Spring Boot 3.2.0
- Jackson (JSON)
- Lombok
- Spring WebFlux (WebClient)

## TODO

- [ ] 实现 Agent 主循环逻辑
- [ ] 实现 Context 上下文管理
- [ ] 实现 ToolRegistry 工具注册
- [ ] 完善 GrepTool 搜索逻辑
- [ ] 完善 BashTool 执行逻辑
- [ ] 完善 AddTool 目录添加逻辑
- [ ] 实现 ClaudeApiClient 真实 API 调用
- [ ] 完善 MockModelClient Mock 响应
- [ ] 实现 HistoryManager 历史管理
- [ ] 实现 JsonHistoryStore JSON 持久化
- [ ] 实现 REPL 主循环
- [ ] 实现流式响应
- [ ] 添加更多工具 (WriteFile, Glob 等)
- [ ] 实现上下文压缩
- [ ] 添加单元测试

## 参考源码

- `restored-src/src/main.tsx` - 主入口
- `restored-src/src/QueryEngine.ts` - 查询引擎
- `restored-src/src/Tool.ts` - 工具系统
- `restored-src/src/tools/` - 各种工具实现
- `restored-src/src/services/api/claude.ts` - API 调用
- `restored-src/src/history.ts` - 历史管理