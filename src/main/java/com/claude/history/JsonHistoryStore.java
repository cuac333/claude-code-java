package com.claude.history;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JSON 文件存储实现
 * 
 * 对应 TS: history.ts 中的文件存储逻辑
 */
@Component
public class JsonHistoryStore implements HistoryManager.HistoryStore {
    
    // private final Path storageDir;
    // private final ObjectMapper objectMapper;
    
    public JsonHistoryStore(String storagePath) {
        // this.storageDir = Paths.get(storagePath).toAbsolutePath();
        // this.objectMapper = new ObjectMapper();
        // this.objectMapper.registerModule(new JavaTimeModule());
        // this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 
        // // 确保目录存在
        // try {
        //     Files.createDirectories(storageDir);
        // } catch (Exception e) {
        //     throw new RuntimeException("无法创建历史存储目录: " + storageDir, e);
        // }
    }
    
    @Override
    public void save(HistoryManager.Session session) {
        // try {
        //     Path filePath = storageDir.resolve(session.getId() + ".json");
        //     objectMapper.writeValue(filePath.toFile(), session);
        // } catch (Exception e) {
        //     throw new RuntimeException("保存会话失败: " + session.getId(), e);
        // }
    }
    
    @Override
    public HistoryManager.Session load(String sessionId) {
        // try {
        //     Path filePath = storageDir.resolve(sessionId + ".json");
        //     if (!Files.exists(filePath)) {
        //         return null;
        //     }
        //     return objectMapper.readValue(filePath.toFile(), HistoryManager.Session.class);
        // } catch (Exception e) {
        //     throw new RuntimeException("加载会话失败: " + sessionId, e);
        // }
        return null;
    }
    
    @Override
    public List<HistoryManager.Session> listAll() {
        // try {
        //     return Files.list(storageDir)
        //         .filter(p -> p.toString().endsWith(".json"))
        //         .map(this::loadFromPath)
        //         .filter(session -> session != null)
        //         .sorted((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()))
        //         .collect(Collectors.toList());
        // } catch (Exception e) {
        //     throw new RuntimeException("列出会话失败", e);
        // }
        return List.of();
    }
    
    /**
     * 从路径加载会话
     */
    // private HistoryManager.Session loadFromPath(Path filePath) {
    //     try {
    //         return objectMapper.readValue(filePath.toFile(), HistoryManager.Session.class);
    //     } catch (Exception e) {
    //         return null;
    //     }
    // }
    
    @Override
    public void delete(String sessionId) {
        // try {
        //     Path filePath = storageDir.resolve(sessionId + ".json");
        //     Files.deleteIfExists(filePath);
        // } catch (Exception e) {
        //     throw new RuntimeException("删除会话失败: " + sessionId, e);
        // }
    }
    
    @Override
    public void clear() {
        // try {
        //     Files.list(storageDir)
        //         .filter(p -> p.toString().endsWith(".json"))
        //         .forEach(p -> {
        //             try {
        //                 Files.delete(p);
        //             } catch (Exception ignored) {}
        //         });
        // } catch (Exception e) {
        //     throw new RuntimeException("清空会话失败", e);
        // }
    }
}
