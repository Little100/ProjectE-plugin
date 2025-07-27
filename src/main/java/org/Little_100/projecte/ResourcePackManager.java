package org.Little_100.projecte;

import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ResourcePackManager implements Listener {
    
    private final ProjectE plugin;
    private final File resourcePackFile;
    private final String resourcePackUrl;
    private final boolean autoSendResourcePack;
    
    public ResourcePackManager(ProjectE plugin) {
        this.plugin = plugin;
        
        // 创建插件资源包目录
        File resourcePackDir = new File(plugin.getDataFolder(), "resourcepacks");
        if (!resourcePackDir.exists()) {
            resourcePackDir.mkdirs();
        }
        
        // 提取资源包文件到插件数据文件夹
        resourcePackFile = new File(resourcePackDir, "ProjectE_Resourcepack.zip");
        try {
            extractResourcePack();
        } catch (IOException e) {
            plugin.getLogger().severe("初始化资源包时出错: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 从配置获取资源包URL和是否自动发送
        resourcePackUrl = plugin.getConfig().getString("resourcepack.url", "");
        autoSendResourcePack = plugin.getConfig().getBoolean("resourcepack.auto_send", true);
    }
    
    public void extractResourcePack() throws IOException {
        // 检查资源包文件是否已存在，如果不存在或配置为强制更新则提取
        if (!resourcePackFile.exists() || plugin.getConfig().getBoolean("resourcepack.force_update", false)) {
            plugin.getLogger().info("提取资源包到: " + resourcePackFile.getAbsolutePath());
            
            // 从JAR中提取资源包
            try (var inputStream = plugin.getResource("pack/ProjectE Resourcepack.zip")) {
                if (inputStream != null) {
                    Files.copy(
                        inputStream,
                        Path.of(resourcePackFile.getAbsolutePath()),
                        StandardCopyOption.REPLACE_EXISTING
                    );
                    plugin.getLogger().info("资源包提取成功");
                } else {
                    plugin.getLogger().warning("无法在JAR中找到资源包");
                    throw new IOException("资源包文件在JAR中不存在");
                }
            }
        }
    }
}