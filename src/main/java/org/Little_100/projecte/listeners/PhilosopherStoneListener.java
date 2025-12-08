package org.Little_100.projecte.listeners;

import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.gui.PhilosopherStoneGUI;
import org.Little_100.projecte.gui.TransmutationGUI;
import org.Little_100.projecte.util.ParticleHelper;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Slab;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

public class PhilosopherStoneListener implements Listener {

    private final ProjectE plugin;

    // 持续粒子效果系统
    private final Map<UUID, Long> playerCooldowns = new HashMap<>();
    private final Set<UUID> interactedThisTick = new HashSet<>();

    private static final Material[] MATERIALS_CYCLE = {
        Material.COBBLESTONE, Material.STONE, Material.GRAVEL, Material.SAND
    };

    private static final Material[] BUILDING_CYCLE_1 = {Material.GRASS_BLOCK, Material.DIRT, Material.STONE};

    private static final Material[] BUILDING_CYCLE_2 = {
        Material.STONE_BRICKS,
        Material.CRACKED_STONE_BRICKS,
        Material.MOSSY_STONE_BRICKS,
        Material.CHISELED_STONE_BRICKS
    };

    public PhilosopherStoneListener(ProjectE plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // 只处理主手事件
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        ItemStack heldItem = player.getInventory().getItemInMainHand();

        // 检查贤者之石右键单击方块的操作
        if (plugin.isPhilosopherStone(heldItem)
                && event.getAction() == Action.RIGHT_CLICK_BLOCK
                && event.getClickedBlock() != null) {
            event.setCancelled(true);

            // 冷却（0.5秒）
            long currentTime = System.currentTimeMillis();
            long lastInteraction = playerCooldowns.getOrDefault(player.getUniqueId(), 0L);
            if (currentTime - lastInteraction < 500) {
                return; // 冷却中...
            }

            if (interactedThisTick.contains(player.getUniqueId())) {
                return;
            }

            playerCooldowns.put(player.getUniqueId(), currentTime);
            interactedThisTick.add(player.getUniqueId());

            final Block clickedBlock = event.getClickedBlock();
            final BlockFace blockFace = event.getBlockFace();

            plugin.getSchedulerAdapter()
                    .runTaskLater(
                            () -> {
                                interactedThisTick.remove(player.getUniqueId());

                                plugin.getSchedulerAdapter().runTaskAt(clickedBlock.getLocation(), () -> {
                                    handleBlockTransformation(player, player.isSneaking(), clickedBlock, blockFace);
                                });
                            },
                            1L);
        }

        // 如果贤者之石的操作未触发，则检查是否右键单击了石化橡木台阶
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
                && event.getClickedBlock() != null
                && event.getClickedBlock().getType() == Material.PETRIFIED_OAK_SLAB) {
            if (plugin.getConfig().getBoolean("gui.enabled", true)) {
                if (!player.hasPermission("projecte.interact.transmutationtable")) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to use the Transmutation Table.");
                    return;
                }
                if (event.getClickedBlock().getBlockData() instanceof Slab) {
                    Slab slab = (Slab) event.getClickedBlock().getBlockData();
                    if (slab.getType() == Slab.Type.BOTTOM) {
                        if (!player.isSneaking()) {
                            event.setCancelled(true);
                            new TransmutationGUI(player).open();
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();

        // 检查玩家是否手持贤者之石并潜行
        if (player.isSneaking() && plugin.isPhilosopherStone(mainHandItem)) {
            event.setCancelled(true); // 取消交换副手物品

            // 打开贤者之石设置的GUI
            openPhilosopherStoneGUI(player);
            player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f);
        }
    }

    private void openPhilosopherStoneGUI(Player player) {
        new PhilosopherStoneGUI(plugin, player).open();
    }

    private void handleBlockTransformation(
            Player player, boolean isShiftClick, Block clickedBlock, BlockFace clickedFace) {
        // 检查贤者之石功能是否启用
        if (!plugin.getConfig().getBoolean("philosopher_stone.enabled", true)) {
            return;
        }
        
        if (clickedBlock == null) return;

        // 获取转换区域
        PhilosopherStoneGUI.TransformationArea area = PhilosopherStoneGUI.getTransformationArea(player);

        // 根据点击的面获取要转换的方块列表
        List<Block> blocksToTransform = getBlocksInAreaByClickedFace(clickedBlock, area, clickedFace);

        if (blocksToTransform.isEmpty()) return;

        // 转换所有方块
        int transformedCount = 0;
        for (Block block : blocksToTransform) {
            Material blockMaterial = block.getType();
            Material newMaterial;

            if (isShiftClick) {
                newMaterial = getShiftRightClickTransformation(blockMaterial);
            } else {
                newMaterial = getRightClickTransformation(blockMaterial);
            }

            if (newMaterial != null) {
                block.setType(newMaterial);
                transformedCount++;
            }
        }

        if (transformedCount > 0) {
            player.playSound(clickedBlock.getLocation(), "projecte:custom.petransmute", 1.0f, 1.0f); // 需要 材质包支持

            // 发送转换消息
            String modeText = area.getMode().getDisplayName(plugin);
            player.sendMessage(ChatColor.GREEN + "Transformed " + transformedCount + " blocks [" + modeText + " Level "
                    + area.getChargeLevel() + "]");
        }
    }

    private List<Block> getBlocksInCube(Block center, int width, int height, int depth) {
        List<Block> blocks = new ArrayList<>();

        int halfWidth = width / 2;
        int halfHeight = height / 2;
        int halfDepth = depth / 2;

        for (int x = -halfWidth; x <= halfWidth; x++) {
            for (int y = -halfHeight; y <= halfHeight; y++) {
                for (int z = -halfDepth; z <= halfDepth; z++) {
                    Location loc = center.getLocation().add(x, y, z);
                    Block block = loc.getBlock();
                    blocks.add(block);
                }
            }
        }

        return blocks;
    }

    private List<Location> calculateOutlineLocations(List<Block> blocks) {
        List<Location> outlineLocations = new ArrayList<>();

        if (blocks.isEmpty()) {
            return outlineLocations;
        }

        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

        for (Block block : blocks) {
            Location loc = block.getLocation();
            minX = Math.min(minX, loc.getBlockX());
            maxX = Math.max(maxX, loc.getBlockX());
            minY = Math.min(minY, loc.getBlockY());
            maxY = Math.max(maxY, loc.getBlockY());
            minZ = Math.min(minZ, loc.getBlockZ());
            maxZ = Math.max(maxZ, loc.getBlockZ());
        }

        World world = blocks.get(0).getWorld();

        for (int x = minX; x <= maxX; x++) {
            outlineLocations.add(new Location(world, x, minY, minZ));
            outlineLocations.add(new Location(world, x, minY, maxZ));
        }
        for (int z = minZ + 1; z < maxZ; z++) {
            outlineLocations.add(new Location(world, minX, minY, z));
            outlineLocations.add(new Location(world, maxX, minY, z));
        }

        if (maxY > minY) {
            for (int x = minX; x <= maxX; x++) {
                outlineLocations.add(new Location(world, x, maxY, minZ));
                outlineLocations.add(new Location(world, x, maxY, maxZ));
            }
            for (int z = minZ + 1; z < maxZ; z++) {
                outlineLocations.add(new Location(world, minX, maxY, z));
                outlineLocations.add(new Location(world, maxX, maxY, z));
            }
        }

        for (int y = minY + 1; y < maxY; y++) {
            outlineLocations.add(new Location(world, minX, y, minZ));
            outlineLocations.add(new Location(world, minX, y, maxZ));
            outlineLocations.add(new Location(world, maxX, y, minZ));
            outlineLocations.add(new Location(world, maxX, y, maxZ));
        }

        return outlineLocations;
    }

    private Material getRightClickTransformation(Material material) {
        Material result;

        result = cycleMaterial(material, MATERIALS_CYCLE, false);
        if (result != null) return result;

        if (material == Material.GRASS_BLOCK) return Material.DIRT;
        if (material == Material.DIRT) return Material.STONE;
        result = cycleMaterial(material, BUILDING_CYCLE_2, false);
        if (result != null) return result;

        if (material == Material.OBSIDIAN) return Material.LAVA;
        if (material == Material.ICE) return Material.WATER;

        return getOtherCycleTransformation(material, false);
    }

    private Material getShiftRightClickTransformation(Material material) {
        Material result;

        result = cycleMaterial(material, MATERIALS_CYCLE, true);
        if (result != null) {
            if (material == Material.STONE) {
                return Material.GRASS_BLOCK;
            }
            return result;
        }

        if (material == Material.GRASS_BLOCK) return Material.STONE;
        if (material == Material.DIRT) return Material.GRASS_BLOCK;

        result = cycleMaterial(material, BUILDING_CYCLE_2, true);
        if (result != null) return result;
        if (material == Material.LAVA) return Material.OBSIDIAN;
        if (material == Material.WATER) return Material.ICE;

        return getOtherCycleTransformation(material, true);
    }

    private Material getOtherCycleTransformation(Material material, boolean reverse) {
        Material result;

        // 7种原版树苗
        Material[] saplings = {
            Material.OAK_SAPLING, Material.SPRUCE_SAPLING, Material.BIRCH_SAPLING,
            Material.JUNGLE_SAPLING, Material.ACACIA_SAPLING, Material.DARK_OAK_SAPLING,
            Material.MANGROVE_PROPAGULE
        };

        // 7种原版原木
        Material[] logs = {
            Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG,
            Material.JUNGLE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG,
            Material.MANGROVE_LOG
        };

        // 7种原版树叶
        Material[] leaves = {
            Material.OAK_LEAVES, Material.SPRUCE_LEAVES, Material.BIRCH_LEAVES,
            Material.JUNGLE_LEAVES, Material.ACACIA_LEAVES, Material.DARK_OAK_LEAVES,
            Material.MANGROVE_LEAVES
        };

        // 16种原版羊毛
        Material[] wools = {
            Material.WHITE_WOOL, Material.ORANGE_WOOL, Material.MAGENTA_WOOL,
            Material.LIGHT_BLUE_WOOL, Material.YELLOW_WOOL, Material.LIME_WOOL,
            Material.PINK_WOOL, Material.GRAY_WOOL, Material.LIGHT_GRAY_WOOL,
            Material.CYAN_WOOL, Material.PURPLE_WOOL, Material.BLUE_WOOL,
            Material.BROWN_WOOL, Material.GREEN_WOOL, Material.RED_WOOL,
            Material.BLACK_WOOL
        };

        result = cycleMaterial(material, saplings, reverse);
        if (result != null) return result;

        result = cycleMaterial(material, logs, reverse);
        if (result != null) return result;

        result = cycleMaterial(material, leaves, reverse);
        if (result != null) return result;

        return cycleMaterial(material, wools, reverse);
    }

    private Material cycleMaterial(Material current, Material[] materials, boolean reverse) {
        for (int i = 0; i < materials.length; i++) {
            if (materials[i] == current) {
                if (reverse) {
                    // 反向循环
                    return materials[(i - 1 + materials.length) % materials.length];
                } else {
                    // 正向循环
                    return materials[(i + 1) % materials.length];
                }
            }
        }
        return null;
    }

    private boolean hasPhilosopherStone(HumanEntity player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (plugin.isPhilosopherStone(item)) {
                return true;
            }
        }
        return false;
    }

    private List<Block> getBlocksInAreaByClickedFace(
            Block center, PhilosopherStoneGUI.TransformationArea area, BlockFace clickedFace) {
        List<Block> blocks = new ArrayList<>();

        int width = area.getWidth();
        int height = area.getHeight();
        int depth = area.getDepth();

        switch (area.getMode()) {
            case PANEL:
                // 平面模式：根据点击的面确定平面
                blocks.addAll(getBlocksInPlaneByFace(center, width, height, clickedFace));
                break;
            case LINE:
                // 直线模式：根据点击的面确定直线方向
                blocks.addAll(getBlocksInLineByFace(center, height, clickedFace));
                break;
            case CUBE:
                // 立方体模式：以点击的方块为中心的立方体
                blocks.addAll(getBlocksInCube(center, width, height, depth));
                break;
        }

        return blocks;
    }

    private List<Block> getBlocksInPlaneByFace(Block center, int width, int height, BlockFace clickedFace) {
        List<Block> blocks = new ArrayList<>();

        Vector right, up;

        switch (clickedFace) {
            case UP:
            case DOWN:
                right = new Vector(1, 0, 0);
                up = new Vector(0, 0, 1);
                break;
            case NORTH:
            case SOUTH:
                right = new Vector(1, 0, 0);
                up = new Vector(0, 1, 0);
                break;
            case EAST:
            case WEST:
                right = new Vector(0, 0, 1);
                up = new Vector(0, 1, 0);
                break;
            default:
                right = new Vector(1, 0, 0);
                up = new Vector(0, 0, 1);
                break;
        }

        int halfWidth = width / 2;
        int halfHeight = height / 2;

        for (int x = -halfWidth; x <= halfWidth; x++) {
            for (int y = -halfHeight; y <= halfHeight; y++) {
                Vector offset = right.clone().multiply(x).add(up.clone().multiply(y));
                Location loc = center.getLocation().add(offset);
                Block block = loc.getBlock();
                blocks.add(block);
            }
        }

        return blocks;
    }

    private List<Block> getBlocksInLineByFace(Block center, int length, BlockFace clickedFace) {
        List<Block> blocks = new ArrayList<>();

        Vector lineDirection;

        switch (clickedFace) {
            case UP:
            case DOWN:
                lineDirection = new Vector(0, 1, 0);
                break;
            case NORTH:
                lineDirection = new Vector(0, 0, -1);
                break;
            case SOUTH:
                lineDirection = new Vector(0, 0, 1);
                break;
            case EAST:
                lineDirection = new Vector(1, 0, 0);
                break;
            case WEST:
                lineDirection = new Vector(-1, 0, 0);
                break;
            default:
                lineDirection = new Vector(0, 1, 0);
                break;
        }

        int halfLength = length / 2;

        for (int i = -halfLength; i <= halfLength; i++) {
            Vector offset = lineDirection.clone().multiply(i);
            Location loc = center.getLocation().add(offset);
            Block block = loc.getBlock();
            blocks.add(block);
        }

        return blocks;
    }

    public void showContinuousOutline(Player player, Block targetBlock) {
        PhilosopherStoneGUI.TransformationArea area = PhilosopherStoneGUI.getTransformationArea(player);

        BlockFace face = getPlayerBlockFace(player);
        if (face == null) {
            face = BlockFace.UP;
        }

        List<Block> blocksToShow = getBlocksInAreaByClickedFace(targetBlock, area, face);
        spawnParticleOutline(player, blocksToShow);
    }

    private void spawnParticleOutline(Player player, List<Block> blocks) {
        FileConfiguration config = plugin.getConfig();
        String particleName = config.getString("philosopher_stone.particle.particle-name", "end_rod")
                .toLowerCase(); // 使用小写

        List<Location> outlineLocations = calculateOutlineLocations(blocks);
        for (Location loc : outlineLocations) {
            // 使用兼容的ParticleHelper
            ParticleHelper.spawnParticle(
                player,
                particleName,
                loc.clone().add(0.5, 0.6, 0.5),
                1,     // count
                0,     // offsetX
                0,     // offsetY
                0,     // offsetZ
                0      // extra (speed)
            );
        }
    }

    private BlockFace getPlayerBlockFace(Player player) {
        List<Block> lastTwoTargetBlocks = player.getLastTwoTargetBlocks(null, 10);
        if (lastTwoTargetBlocks.size() < 2) {
            return null;
        }
        Block targetBlock = lastTwoTargetBlocks.get(1);
        Block adjacentBlock = lastTwoTargetBlocks.get(0);
        return targetBlock.getFace(adjacentBlock);
    }
}
