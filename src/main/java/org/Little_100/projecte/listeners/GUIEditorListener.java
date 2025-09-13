package org.Little_100.projecte.listeners;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.managers.CommandManager;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GUIEditorListener implements Listener {

    private final ProjectE plugin;
    private final CommandManager commandManager;

    public GUIEditorListener(ProjectE plugin, CommandManager commandManager) {
        this.plugin = plugin;
        this.commandManager = commandManager;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Map<Player, String> editors = commandManager.getOpenGuiEditors();

        if (editors.containsKey(player)) {
            String fileName = editors.get(player);
            Inventory inventory = event.getInventory();

            boolean isEmpty = true;
            for (ItemStack item : inventory.getContents()) {
                if (item != null) {
                    isEmpty = false;
                    break;
                }
            }

            if (isEmpty) {
                editors.remove(player);
                return;
            }

            File guiFile = new File(plugin.getDataFolder(), fileName);
            YamlConfiguration guiConfig = new YamlConfiguration();

            guiConfig.set(
                    "title", inventory.getViewers().get(0).getOpenInventory().getTitle());
            guiConfig.set("size", inventory.getSize());

            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack item = inventory.getItem(i);
                if (item != null) {
                    String path = "items." + i;
                    guiConfig.set(path + ".material", item.getType().toString());
                    if (item.hasItemMeta()) {
                        ItemMeta meta = item.getItemMeta();
                        if (meta.hasDisplayName()) {
                            guiConfig.set(
                                    path + ".name",
                                    meta.getDisplayName().replace(String.valueOf(ChatColor.COLOR_CHAR), "&"));
                        }
                        if (meta.hasLore()) {
                            List<String> lore = new ArrayList<>();
                            for (String line : meta.getLore()) {
                                lore.add(line.replace(String.valueOf(ChatColor.COLOR_CHAR), "&"));
                            }
                            guiConfig.set(path + ".lore", lore);
                        }
                        if (meta.hasCustomModelData()) {
                            guiConfig.set(path + ".custom_model_data", meta.getCustomModelData());
                        }
                    }
                }
            }

            try {
                guiConfig.save(guiFile);
                player.sendMessage(plugin.getLanguageManager().get("serverside.command.gui.saved"));
            } catch (IOException e) {
                e.printStackTrace();
                player.sendMessage(plugin.getLanguageManager().get("serverside.command.gui.save_failed"));
            }

            editors.remove(player);
        }
    }
}
