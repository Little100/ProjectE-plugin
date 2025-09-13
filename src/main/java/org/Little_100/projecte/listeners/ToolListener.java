package org.Little_100.projecte.listeners;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.devices.DeviceManager;
import org.Little_100.projecte.gui.ToolChargeGUI;
import org.Little_100.projecte.tools.AreaOfEffectManager;
import org.Little_100.projecte.tools.ToolManager;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ToolListener implements Listener {

    private final ProjectE plugin;
    private final ToolManager toolManager;
    private final DeviceManager deviceManager;
    private final Set<UUID> processingPlayers = new HashSet<>();

    public ToolListener(ProjectE plugin) {
        this.plugin = plugin;
        this.toolManager = plugin.getToolManager();
        this.deviceManager = plugin.getDeviceManager();
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();
        Player player = event.getPlayer();

        if (toolManager.isProjectETool(item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        ItemStack offHandItem = event.getOffHandItem();

        if (player.isSneaking() && toolManager.isProjectETool(offHandItem)) {
            event.setCancelled(true);
            new ToolChargeGUI(plugin, player, offHandItem).open();
            player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (processingPlayers.contains(playerUUID)) {
            return;
        }

        ItemStack tool = player.getInventory().getItemInMainHand();

        if (event.isCancelled() || !toolManager.isProjectETool(tool)) {
            return;
        }

        try {
            processingPlayers.add(playerUUID);

            ItemMeta meta = tool.getItemMeta();
            if (meta == null) return;

            PersistentDataContainer container = meta.getPersistentDataContainer();
            String mode = "standard";
            if (toolManager.isRedMatterMorningstar(tool)) {
                NamespacedKey key = new NamespacedKey(plugin, "projecte_morningstar_mode");
                mode = container.getOrDefault(key, PersistentDataType.STRING, "normal");
            } else if (toolManager.isDarkMatterTool(tool)) {
                NamespacedKey key = new NamespacedKey(plugin, "projecte_mode");
                mode = container.getOrDefault(key, PersistentDataType.STRING, "standard");
            }

            if ((!mode.equals("standard") && !mode.equals("normal"))
                    && !plugin.getConfig().getBoolean("Tools.area_of_effect_mining_enabled", true)) {
                return;
            }

            List<Block> affectedBlocks;

            switch (mode) {
                case "3x3":
                    affectedBlocks = AreaOfEffectManager.getBlocksIn3x3Area(player, event.getBlock());
                    handleBlockBreaking(player, tool, affectedBlocks);
                    break;
                case "tall":
                    affectedBlocks = AreaOfEffectManager.getBlocksInTallArea(event.getBlock());
                    handleBlockBreaking(player, tool, affectedBlocks);
                    break;
                case "wide":
                    affectedBlocks = AreaOfEffectManager.getBlocksInWideArea(player, event.getBlock());
                    handleBlockBreaking(player, tool, affectedBlocks);
                    break;
                case "long":
                    affectedBlocks = AreaOfEffectManager.getBlocksInLongArea(player, event.getBlock());
                    handleBlockBreaking(player, tool, affectedBlocks);
                    break;
                case "standard":
                case "normal":
                default:
                    break;
            }
        } finally {
            processingPlayers.remove(playerUUID);
        }
    }

    private void handleBlockBreaking(Player player, ItemStack tool, List<Block> blocks) {
        ItemMeta meta = tool.getItemMeta();
        if (!(meta instanceof Damageable)) return;

        Damageable damageable = (Damageable) meta;
        int maxDurability = tool.getType().getMaxDurability();

        for (Block block : blocks) {
            if (deviceManager.isDevice(block)) {
                continue;
            }

            BlockBreakEvent newEvent = new BlockBreakEvent(block, player);
            plugin.getServer().getPluginManager().callEvent(newEvent);

            if (!newEvent.isCancelled() && !block.isEmpty() && !block.isLiquid()) {
                if (toolManager.isValidMaterialForTool(block.getType(), tool)) {
                    java.util.Collection<ItemStack> drops = block.getDrops(tool, player);
                    block.setType(org.bukkit.Material.AIR);
                    for (ItemStack drop : drops) {
                        java.util.HashMap<Integer, ItemStack> leftover =
                                player.getInventory().addItem(drop);
                        if (!leftover.isEmpty()) {
                            player.getWorld().dropItemNaturally(player.getLocation(), leftover.get(0));
                        }
                    }
                }
            }
        }
        tool.setItemMeta(meta);
    }

    @EventHandler
    public void onEnchantItem(EnchantItemEvent event) {
        if (toolManager.isProjectETool(event.getItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        for (ItemStack item : event.getInventory().getContents()) {
            if (item != null && toolManager.isProjectETool(item)) {
                event.setResult(null);
                return;
            }
        }
    }
}
