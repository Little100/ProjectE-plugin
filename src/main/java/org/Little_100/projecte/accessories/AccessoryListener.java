package org.Little_100.projecte.accessories;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.util.Constants;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class AccessoryListener implements Listener {

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getType() == EntityType.HORSE) {
            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItem(event.getHand());
            if (isAccessory(item)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !event.getAction().name().contains("RIGHT_CLICK")) {
            return;
        }

        String id = getAccessoryId(item);
        if (id == null) return;

        long now = System.currentTimeMillis();
        long cooldownTime = cooldowns.getOrDefault(player.getUniqueId(), 0L);

        if (now < cooldownTime) {
            return;
        }

        switch (id) {
            case "body_stone":
                BodyStone.activate(player, item);
                break;
            case "soul_stone":
                SoulStone.activate(player, item);
                break;
            case "life_stone":
                LifeStone.activate(player, item);
                break;
            case "mind_stone":
                MindStone.activate(player, item);
                break;
        }
        cooldowns.put(player.getUniqueId(), now + 250); // 250ms cooldown
    }

    private String getAccessoryId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        PersistentDataContainer data = meta.getPersistentDataContainer();
        NamespacedKey key = Constants.ID_KEY;

        if (data.has(key, PersistentDataType.STRING)) {
            String id = data.get(key, PersistentDataType.STRING);
            if ("body_stone".equals(id)
                    || "soul_stone".equals(id)
                    || "life_stone".equals(id)
                    || "mind_stone".equals(id)) {
                return id;
            }
        }
        return null;
    }

    private boolean isAccessory(ItemStack item) {
        return getAccessoryId(item) != null;
    }
}
