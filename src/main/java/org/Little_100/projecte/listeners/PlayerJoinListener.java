package org.Little_100.projecte.listeners;

import org.Little_100.projecte.ProjectE;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final ProjectE plugin;

    public PlayerJoinListener(ProjectE plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        boolean advancementDatapackEnabled = plugin.getConfig().getBoolean("Advancement_Datapack", false);
        boolean datapackConfirmed = plugin.getConfig().getBoolean("ConfrimDatapack", false);

        if (advancementDatapackEnabled && !datapackConfirmed) {
            plugin.getSchedulerAdapter().runTaskLater(() -> {
                player.sendMessage(plugin.getLanguageManager().get("serverside.datapack.not_loaded_warning"));
            }, 20L);
        }
    }
}