package org.Little_100.projecte;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ResourcePackManager implements Listener {
    
    private final ProjectE plugin;
    private final File resourcePackFile;
    private final String resourcePackUrl;
    private final boolean autoSendResourcePack;
    private File resourcePackSourceDir;
    
    public ResourcePackManager(ProjectE plugin) {
        this.plugin = plugin;
        
        // 创建插件资源包目录
        File resourcePackDir = new File(plugin.getDataFolder(), "resourcepacks");
        if (!resourcePackDir.exists()) {
            resourcePackDir.mkdirs();
        }
        
        // 设置资源包源目录（用于从本地文件构建资源包）
        resourcePackSourceDir = new File(plugin.getDataFolder(), "ResourcePackSource");
        if (!resourcePackSourceDir.exists()) {
            resourcePackSourceDir.mkdirs();
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
    
    /**
     * 向玩家发送资源包
     * @param player 目标玩家
     */
    public void sendResourcePack(Player player) {
        if (resourcePackUrl.isEmpty()) {
            plugin.getLogger().warning("无法向玩家 " + player.getName() + " 发送资源包: 未配置URL");
            player.sendMessage("§c无法加载自定义材质: 服务器未配置资源包URL");
            return;
        }
        
        plugin.getLogger().info("向玩家 " + player.getName() + " 发送资源包");
        player.sendMessage("§a正在发送自定义材质包...");
        player.setResourcePack(resourcePackUrl);
    }
    
    /**
     * 从本地资源包目录构建新的ZIP资源包
     * 这可以用于在服务器修改资源包后重新生成
     */
    public void rebuildResourcePack() {
        if (!resourcePackSourceDir.exists() || !resourcePackSourceDir.isDirectory()) {
            plugin.getLogger().warning("资源包源目录不存在，无法重建资源包");
            return;
        }
        
        try {
            plugin.getLogger().info("正在从源目录重建资源包...");
            
            try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(resourcePackFile))) {
                Files.walkFileTree(resourcePackSourceDir.toPath(), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        String entryPath = resourcePackSourceDir.toPath().relativize(file).toString().replace('\\', '/');
                        ZipEntry zipEntry = new ZipEntry(entryPath);
                        zipOut.putNextEntry(zipEntry);
                        Files.copy(file, zipOut);
                        zipOut.closeEntry();
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
            
            plugin.getLogger().info("资源包重建完成: " + resourcePackFile.getAbsolutePath());
        } catch (IOException e) {
            plugin.getLogger().severe("重建资源包时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 获取资源包文件
     * @return 资源包文件对象
     */
    public File getResourcePackFile() {
        return resourcePackFile;
    }
    
    /**
     * 获取资源包URL
     * @return 资源包URL字符串
     */
    public String getResourcePackUrl() {
        return resourcePackUrl;
    }
    
    /**
     * 玩家加入服务器时，如果配置了自动发送资源包，则发送资源包
     * 注意 默认是关闭的，需要在配置文件中开启
     */
    /*
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (autoSendResourcePack) {
            Player player = event.getPlayer();
            // 延迟发送资源包，确保玩家完全进入服务器
            plugin.getSchedulerAdapter().runTaskLater(() -> sendResourcePack(player), 40L); // 2秒后发送
        }
    }
    */
}