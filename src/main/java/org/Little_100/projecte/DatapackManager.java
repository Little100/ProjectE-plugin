package org.Little_100.projecte;

import org.bukkit.Bukkit;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

public class DatapackManager {

    private final ProjectE plugin;
    private final Logger logger;

    public DatapackManager(ProjectE plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void setupDatapack() {
        if (!plugin.getConfig().getBoolean("Advancement_Datapack", false)) {
            logger.info("Advancement datapack is disabled in the config. Skipping setup.");
            return;
        }

        // 如果已经确认过数据包 则跳过安装步骤
        if (plugin.getConfig().getBoolean("ConfrimDatapack", false)) {
            logger.info("Datapack is already confirmed. Skipping setup.");
            return;
        }

        File worldContainer = plugin.getServer().getWorldContainer();
        File datapackDir = new File(worldContainer, "world/datapacks");
        if (!datapackDir.exists()) {
            if (!datapackDir.mkdirs()) {
                logger.severe("Failed to create datapacks directory: " + datapackDir.getAbsolutePath());
                return;
            }
        }

        String datapackFileName = "ProjectE_Datapack.zip";
        File datapackFile = new File(datapackDir, datapackFileName);
        String resourcePath = "pack/ProjectE Datapack.zip";
        
        try (InputStream in = plugin.getResource(resourcePath)) {
            if (in == null) {
                logger.warning("Datapack file not found in plugin resources: " + resourcePath);
                return;
            }

            if (datapackFile.exists()) {
                 logger.info("ProjectE datapack already exists. Skipping installation.");
                 return;
            }

            try (OutputStream out = new FileOutputStream(datapackFile)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                logger.info("Successfully installed ProjectE datapack to: " + datapackFile.getAbsolutePath());
                logger.info("Please run '/datapack enable \"file/" + datapackFileName + "\"' and '/reload' or restart the server to apply the datapack.");

                plugin.getConfig().set("ConfrimDatapack", true); // 确认加载
                plugin.saveConfig();
                logger.info("Set 'ConfrimDatapack' to true in config.yml.");
            }
        } catch (Exception e) {
            logger.severe("Could not install ProjectE datapack: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
