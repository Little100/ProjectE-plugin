package org.Little_100.projecte;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

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

        // 将资源包文件提取到插件数据文件夹
        resourcePackFile = new File(resourcePackDir, "ProjectE_Resourcepack.zip");
        try {
            extractResourcePack();
        } catch (IOException e) {
            plugin.getLogger().severe("Error initializing resource pack: " + e.getMessage());
            e.printStackTrace();
        }

        // 从配置中获取资源包URL和是否自动发送
        resourcePackUrl = plugin.getConfig().getString("resourcepack.url", "");
        autoSendResourcePack = plugin.getConfig().getBoolean("resourcepack.auto_send", true);
    }

    public void extractResourcePack() throws IOException {
        // 检查资源包文件是否已存在，如果不存在或配置强制更新，则进行提取
        if (!resourcePackFile.exists() || plugin.getConfig().getBoolean("resourcepack.force_update", false)) {
            plugin.getLogger().info("Extracting resource pack to: " + resourcePackFile.getAbsolutePath());

            // 从JAR文件中提取资源包
            try (var inputStream = plugin.getResource("pack/ProjectE Resourcepack.zip")) {
                if (inputStream != null) {
                    Files.copy(
                            inputStream,
                            Path.of(resourcePackFile.getAbsolutePath()),
                            StandardCopyOption.REPLACE_EXISTING);
                    plugin.getLogger().info("Resource pack extracted successfully.");
                } else {
                    plugin.getLogger().warning("Could not find resource pack in JAR.");
                    throw new IOException("Resource pack file does not exist in JAR.");
                }
            }
        }
    }

    public void sendResourcePack(Player player) {
        if (resourcePackUrl.isEmpty()) {
            plugin.getLogger()
                    .warning("Cannot send resource pack to player " + player.getName() + ": URL not configured.");
            player.sendMessage("§cCould not load custom textures: Server resource pack URL is not configured.");
            return;
        }

        plugin.getLogger().info("Sending resource pack to player " + player.getName());
        player.sendMessage("§aSending custom texture pack...");
        player.setResourcePack(resourcePackUrl);
    }

    public void rebuildResourcePack() {
        if (!resourcePackSourceDir.exists() || !resourcePackSourceDir.isDirectory()) {
            plugin.getLogger().warning("Resource pack source directory does not exist, cannot rebuild resource pack.");
            return;
        }

        try {
            plugin.getLogger().info("Rebuilding resource pack from source directory...");

            try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(resourcePackFile))) {
                Files.walkFileTree(resourcePackSourceDir.toPath(), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        String entryPath = resourcePackSourceDir.toPath().relativize(file).toString().replace('\\',
                                '/');
                        ZipEntry zipEntry = new ZipEntry(entryPath);
                        zipOut.putNextEntry(zipEntry);
                        Files.copy(file, zipOut);
                        zipOut.closeEntry();
                        return FileVisitResult.CONTINUE;
                    }
                });
            }

            plugin.getLogger().info("Resource pack rebuilt successfully: " + resourcePackFile.getAbsolutePath());
        } catch (IOException e) {
            plugin.getLogger().severe("Error rebuilding resource pack: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public File getResourcePackFile() {
        return resourcePackFile;
    }

    public String getResourcePackUrl() {
        return resourcePackUrl;
    }
}