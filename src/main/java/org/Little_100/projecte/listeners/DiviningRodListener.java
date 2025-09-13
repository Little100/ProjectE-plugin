package org.Little_100.projecte.listeners;

import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.tools.DiviningRod;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DiviningRodListener implements Listener {

    private final ProjectE plugin;
    private final DiviningRod diviningRod;
    private final Map<UUID, Integer> playerModes = new HashMap<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<Material, Material> oreMappings = new HashMap<>();

    public DiviningRodListener(ProjectE plugin) {
        this.plugin = plugin;
        this.diviningRod = plugin.getDiviningRod();
        initializeOreMappings();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_BLOCK && !player.isSneaking()) {
            if (diviningRod.isLowDiviningRod(item) || diviningRod.isMediumDiviningRod(item) || diviningRod.isHighDiviningRod(item)) {
                event.setCancelled(true);
                if (checkCooldown(player)) {
                    scanBlocks(player, item, event.getClickedBlock().getLocation());
                    setCooldown(player);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (player.isSneaking() && (diviningRod.isLowDiviningRod(item) || diviningRod.isMediumDiviningRod(item) || diviningRod.isHighDiviningRod(item))) {
            event.setCancelled(true);
            if (diviningRod.isMediumDiviningRod(item) || diviningRod.isHighDiviningRod(item)) {
                plugin.getDiviningRodGUI().openGUI(player);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(plugin.getLanguageManager().get("clientside.divining_rod.gui.title"))) {
            return;
        }

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        int mode = -1;
        if (clickedItem.getType() == Material.STONE) {
            mode = 0;
        } else if (clickedItem.getType() == Material.IRON_INGOT) {
            mode = 1;
        } else if (clickedItem.getType() == Material.DIAMOND) {
            mode = 2;
        }

        if (mode != -1) {
            playerModes.put(player.getUniqueId(), mode);
            String modeName = getModeName(mode);
            player.sendMessage(plugin.getLanguageManager().get("clientside.divining_rod.mode_changed", Map.of("mode", modeName)));
            player.closeInventory();
        }
    }

    private void initializeOreMappings() {
        oreMappings.put(Material.COAL_ORE, Material.COAL);
        oreMappings.put(Material.DEEPSLATE_COAL_ORE, Material.COAL);
        oreMappings.put(Material.COPPER_ORE, Material.COPPER_INGOT);
        oreMappings.put(Material.DEEPSLATE_COPPER_ORE, Material.COPPER_INGOT);
        oreMappings.put(Material.IRON_ORE, Material.IRON_INGOT);
        oreMappings.put(Material.DEEPSLATE_IRON_ORE, Material.IRON_INGOT);
        oreMappings.put(Material.GOLD_ORE, Material.GOLD_INGOT);
        oreMappings.put(Material.DEEPSLATE_GOLD_ORE, Material.GOLD_INGOT);
        oreMappings.put(Material.DIAMOND_ORE, Material.DIAMOND);
        oreMappings.put(Material.DEEPSLATE_DIAMOND_ORE, Material.DIAMOND);
        oreMappings.put(Material.EMERALD_ORE, Material.EMERALD);
        oreMappings.put(Material.DEEPSLATE_EMERALD_ORE, Material.EMERALD);
        oreMappings.put(Material.LAPIS_ORE, Material.LAPIS_LAZULI);
        oreMappings.put(Material.DEEPSLATE_LAPIS_ORE, Material.LAPIS_LAZULI);
        oreMappings.put(Material.NETHER_GOLD_ORE, Material.GOLD_NUGGET);
        oreMappings.put(Material.NETHER_QUARTZ_ORE, Material.QUARTZ);
        oreMappings.put(Material.REDSTONE_ORE, Material.REDSTONE);
        oreMappings.put(Material.DEEPSLATE_REDSTONE_ORE, Material.REDSTONE);
        oreMappings.put(Material.ANCIENT_DEBRIS, Material.NETHERITE_SCRAP);
    }

    private boolean checkCooldown(Player player) {
        return !cooldowns.containsKey(player.getUniqueId()) || System.currentTimeMillis() - cooldowns.get(player.getUniqueId()) > 500;
    }

    private void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    private String getModeName(int mode) {
        switch (mode) {
            case 0:
                return "3x3x3";
            case 1:
                return "16x3x3";
            case 2:
                return "64x3x3";
            default:
                return "Unknown";
        }
    }

    private void scanBlocks(Player player, ItemStack item, Location center) {
        int mode = 0;
        if (!diviningRod.isLowDiviningRod(item)) {
            mode = playerModes.getOrDefault(player.getUniqueId(), 0);
        }

        int[] dimensions = getScanDimensions(mode);
        int halfX = dimensions[0] / 2;
        int halfY = dimensions[1] / 2;
        int halfZ = dimensions[2] / 2;

        long totalEmc = 0;
        int blockCount = 0;
        long maxEmc = 0;
        Material maxEmcMaterial = null;

        for (int x = -halfX; x <= halfX; x++) {
            for (int y = -halfY; y <= halfY; y++) {
                for (int z = -halfZ; z <= halfZ; z++) {
                    Block block = center.clone().add(x, y, z).getBlock();
                    if (block.getType() != Material.AIR) {
                        Material mappedMaterial = oreMappings.getOrDefault(block.getType(), block.getType());
                        if (mappedMaterial.isItem()) {
                            ItemStack blockStack = new ItemStack(mappedMaterial);
                            String itemKey = plugin.getEmcManager().getItemKey(blockStack);
                            long emc = plugin.getEmcManager().getEmc(itemKey);
                            if (emc > 0) {
                                totalEmc += emc;
                                blockCount++;
                                if (emc > maxEmc) {
                                    maxEmc = emc;
                                    maxEmcMaterial = mappedMaterial;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (blockCount > 0) {
            long averageEmc = totalEmc / blockCount;
            player.sendMessage(plugin.getLanguageManager().get("clientside.divining_rod.scan_results"));
            player.sendMessage(plugin.getLanguageManager().get("clientside.divining_rod.total_blocks", Map.of("count", String.valueOf(blockCount))));
            player.sendMessage(plugin.getLanguageManager().get("clientside.divining_rod.average_emc", Map.of("emc", String.valueOf(averageEmc))));

            if (diviningRod.isHighDiviningRod(item) && maxEmcMaterial != null) {
                player.sendMessage(plugin.getLanguageManager().get("clientside.divining_rod.max_emc", Map.of("emc", String.valueOf(maxEmc))));
            }
        } else {
            player.sendMessage(plugin.getLanguageManager().get("clientside.divining_rod.no_emc_blocks"));
        }
    }

    private int[] getScanDimensions(int mode) {
        switch (mode) {
            case 0: // 3x3x3
                return new int[]{3, 3, 3};
            case 1: // 16x3x3
                return new int[]{16, 3, 3};
            case 2: // 64x3x3
                return new int[]{64, 3, 3};
            default:
                return new int[]{3, 3, 3};
        }
    }
}