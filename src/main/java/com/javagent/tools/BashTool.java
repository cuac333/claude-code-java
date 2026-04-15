package com.javagent.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Shell命令执行工具 - 需要审批
 */
public class BashTool implements Tool {
    
    private static final int DEFAULT_TIMEOUT = 30000;  // 30秒
    private static final int MAX_OUTPUT_SIZE = 50000;  // 50KB
    
    @Override
    public String getName() {
        return "Bash";
    }
    
    @Override
    public String getDescription() {
        return "Execute shell commands (requires approval)";
    }
    
    @Override
    public String[] getAliases() {
        return new String[]{"shell", "exec", "run", "cmd"};
    }
    
    @Override
    public boolean requiresApproval() {
        return true;
    }
    
    @Override
    public boolean isReadOnly() {
        return false;
    }
    
    @Override
    public boolean isDestructive() {
        return true;
    }
    
    @Override
    public ToolResult execute(Map<String, Object> input) {
        try {
            String command = (String) input.get("command");
            if (command == null || command.isEmpty()) {
                return ToolResult.error("Command is required");
            }
            
            // 检查危险命令
            if (isDangerousCommand(command)) {
                return ToolResult.error("Command rejected by safety policy: " + command);
            }
            
            int timeout = getInt(input, "timeout", DEFAULT_TIMEOUT);
            String workingDir = (String) input.get("workingDir");
            
            // 构建进程
            ProcessBuilder pb = new ProcessBuilder();
            if (isWindows()) {
                pb.command("cmd.exe", "/c", command);
            } else {
                pb.command("/bin/bash", "-c", command);
            }
            
            // 设置工作目录
            if (workingDir != null && !workingDir.isEmpty()) {
                pb.directory(new File(workingDir));
            }
            
            // 启动进程
            Process process = pb.start();
            
            // 读取输出
            StringBuilder stdout = new StringBuilder();
            StringBuilder stderr = new StringBuilder();
            
            Thread stdoutThread = new Thread(() -> readStream(process, stdout, false));
            Thread stderrThread = new Thread(() -> readStream(process, stderr, true));
            
            stdoutThread.start();
            stderrThread.start();
            
            // 等待完成
            boolean finished = process.waitFor(timeout, TimeUnit.MILLISECONDS);
            
            if (!finished) {
                process.destroyForcibly();
                return ToolResult.error("Command timed out after " + (timeout / 1000) + " seconds");
            }
            
            // 等待输出读取完成
            stdoutThread.join(1000);
            stderrThread.join(1000);
            
            int exitCode = process.exitValue();
            
            // 构建结果
            StringBuilder output = new StringBuilder();
            output.append("$ ").append(command).append("\n\n");
            
            String stdoutStr = truncateOutput(stdout.toString());
            String stderrStr = truncateOutput(stderr.toString());
            
            if (!stdoutStr.isEmpty()) {
                output.append(stdoutStr);
            }
            
            if (!stderrStr.isEmpty()) {
                if (!stdoutStr.isEmpty()) {
                    output.append("\n");
                }
                output.append("[stderr]\n").append(stderrStr);
            }
            
            output.append("\n[exit code: ").append(exitCode).append("]");
            
            if (exitCode == 0) {
                return ToolResult.success(output.toString());
            } else {
                return ToolResult.error(output.toString());
            }
            
        } catch (Exception e) {
            return ToolResult.error("Command execution failed: " + e.getMessage());
        }
    }
    
    private void readStream(Process process, StringBuilder output, boolean isError) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                isError ? process.getErrorStream() : process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                if (output.length() > MAX_OUTPUT_SIZE) {
                    output.append("... (output truncated)\n");
                    break;
                }
            }
        } catch (Exception e) {
            // 忽略读取错误
        }
    }
    
    private String truncateOutput(String output) {
        if (output.length() <= MAX_OUTPUT_SIZE) {
            return output;
        }
        return output.substring(0, MAX_OUTPUT_SIZE) + 
               "\n... (output truncated, total: " + output.length() + " chars)";
    }
    
    private boolean isDangerousCommand(String command) {
        String lowerCmd = command.toLowerCase();
        String[] dangerousPatterns = {
            "rm -rf /",
            "rm -rf /*",
            "> /dev/sda",
            "dd if=/dev/zero",
            "mkfs.",
            "format c:",
            "del /f /s /q c:\\" 
        };
        
        for (String pattern : dangerousPatterns) {
            if (lowerCmd.contains(pattern)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }
    
    private int getInt(Map<String, Object> input, String key, int defaultValue) {
        Object value = input.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
}
