package org.Little_100.projecte.listeners;

import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.compatibility.GeyserAdapter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.geysermc.geyser.api.GeyserApi;

public class GeyserPlayerJoinListener implements Listener {

    private final ProjectE plugin;
    private final GeyserAdapter geyserAdapter;

    public GeyserPlayerJoinListener(ProjectE plugin) {
        this.plugin = plugin;
        this.geyserAdapter = plugin.getGeyserAdapter();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (geyserAdapter == null || !geyserAdapter.isGeyserApiAvailable()) {
            return;
        }

        Player player = event.getPlayer();
        if (GeyserApi.api().isBedrockPlayer(player.getUniqueId())) {
            // The item conversion is now handled by Geyser's internal mapping.
            // This listener can be used for other Geyser-specific logic on player join.
            plugin.getLogger().info("Geyser player " + player.getName() + " has joined.");
        }
    }
}
