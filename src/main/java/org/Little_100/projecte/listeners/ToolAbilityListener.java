package org.Little_100.projecte.listeners;

import java.util.*;
import java.util.stream.Collectors;
import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.tools.ToolManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class ToolAbilityListener implements Listener {

    private final ProjectE plugin;
    private final ToolManager toolManager;
    private final Map<UUID, Long> swordCooldowns = new HashMap<>();
    private static final long SWORD_ABILITY_COOLDOWN = 1000L;

    public ToolAbilityListener(ProjectE plugin) {
        this.plugin = plugin;
        this.toolManager = plugin.getToolManager();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (toolManager.isDarkMatterShears(itemInHand) || toolManager.isRedMatterShears(itemInHand)) {
            handleShearsAbility(player, itemInHand);
            return;
        }

        if (player.isSneaking()
                && (toolManager.isDarkMatterSword(itemInHand) || toolManager.isRedMatterSword(itemInHand))) {
            handleSwordAbility(player, itemInHand);
            event.setCancelled(true);
            return;
        }

        if (toolManager.isRedMatterKatar(itemInHand)) {
            if (player.isSneaking()) {
                handleKatarShear(player, itemInHand);
                event.setCancelled(true);
                return;
            }
            handleKatarAbility(player, itemInHand, event);
            return;
        }

        if (action == Action.RIGHT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();
            if (toolManager.isRedMatterMorningstar(itemInHand)) {
                handleMorningstarAbility(player, itemInHand, clickedBlock);
            } else if (toolManager.isDarkMatterHoe(itemInHand) || toolManager.isRedMatterHoe(itemInHand)) {
                handleHoeAbility(player, itemInHand, clickedBlock);
            } else if (toolManager.isDarkMatterShovel(itemInHand) || toolManager.isRedMatterShovel(itemInHand)) {
                handleShovelAbility(player, itemInHand, clickedBlock);
            } else if (toolManager.isDarkMatterPickaxe(itemInHand) || toolManager.isRedMatterPickaxe(itemInHand)) {
                handlePickaxeAbility(player, itemInHand, clickedBlock);
            } else if (toolManager.isDarkMatterHammer(itemInHand) || toolManager.isRedMatterHammer(itemInHand)) {
                handleHammerAbility(player, itemInHand, clickedBlock);
            }
        }
    }

    // @EventHandler
    // public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    //     Player player = event.getPlayer();
    //     ItemStack itemInHand = player.getInventory().getItemInMainHand();
    //
    //     if (toolManager.isRedMatterKatar(itemInHand) && event.getRightClicked() instanceof LivingEntity) {
    //         handleKatarRightClickEntity(player, itemInHand, (LivingEntity) event.getRightClicked());
    //         event.setCancelled(true);
    //     }
    // }

    private void handleKatarShear(Player player, ItemStack katar) {
        int range = 50;

        List<org.bukkit.entity.Sheep> sheepList =
                player.getWorld().getEntitiesByClass(org.bukkit.entity.Sheep.class).stream()
                        .filter(sheep ->
                                !sheep.isSheared() && sheep.getLocation().distance(player.getLocation()) <= range)
                        .collect(java.util.stream.Collectors.toList());

        if (!sheepList.isEmpty()) {
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_SHEEP_SHEAR, 1.0f, 1.0f);
        }

        for (org.bukkit.entity.Sheep sheep : sheepList) {
            sheep.setSheared(true);
            int woolAmount = 1 + new java.util.Random().nextInt(3);
            Material woolType = getWoolMaterial(sheep.getColor());
            ItemStack woolStack = new ItemStack(woolType, woolAmount);

            java.util.HashMap<Integer, ItemStack> leftover =
                    player.getInventory().addItem(woolStack);
            if (!leftover.isEmpty()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover.get(0));
            }
        }
    }

    private void handleKatarRightClickAir(Player player, ItemStack katar) {
        long now = System.currentTimeMillis();
        long lastUsed = swordCooldowns.getOrDefault(player.getUniqueId(), 0L);

        if (now - lastUsed < SWORD_ABILITY_COOLDOWN) {
            return;
        }

        if (!katar.hasItemMeta()) return;
        org.bukkit.inventory.meta.ItemMeta meta = katar.getItemMeta();
        org.bukkit.persistence.PersistentDataContainer container = meta.getPersistentDataContainer();
        int currentMode = container.getOrDefault(
                new org.bukkit.NamespacedKey(plugin, "projecte_katar_mode"),
                org.bukkit.persistence.PersistentDataType.INTEGER,
                0);

        // 检查配置是否允许全部模式伤害
        boolean attackAllModeEnabled = plugin.getConfig().getBoolean("Tools.katar_attack_all_mode_enabled", true);

        double damage = 1000.0;
        int range = 10; // 20x20

        Collection<Entity> nearbyEntities =
                player.getWorld().getNearbyEntities(player.getLocation(), range, range, range);

        List<LivingEntity> targets = nearbyEntities.stream()
                .filter(e ->
                        e instanceof LivingEntity && !e.equals(player) && !(e instanceof org.bukkit.entity.ArmorStand))
                .filter(e -> {
                    // 如果配置禁用了全部模式伤害，或者模式是1(仅敌对)
                    if (!attackAllModeEnabled || currentMode == 1) {
                        return e instanceof Monster;
                    } else { // 全部模式且配置允许
                        return true;
                    }
                })
                .map(e -> (LivingEntity) e)
                .collect(Collectors.toList());

        if (targets.isEmpty()) {
            return;
        }

        player.playSound(player.getLocation(), "projecte:custom.pecharge", 1.0f, 1.0f);
        for (LivingEntity target : targets) {
            target.setHealth(Math.max(0, target.getHealth() - damage));
        }
        swordCooldowns.put(player.getUniqueId(), now);
    }

    private void handleMorningstarAbility(Player player, ItemStack morningstar, Block clickedBlock) {
        if (clickedBlock == null) return;

        int charge = toolManager.getCharge(morningstar);
        if (charge == 0) return;

        int radius;
        switch (charge) {
            case 1:
                radius = 1;
                break;
            case 2:
                radius = 2;
                break;
            case 3:
                radius = 3;
                break;
            case 4:
                radius = 4;
                break;
            default:
                return;
        }

        List<Block> affectedBlocks;
        Material blockType = clickedBlock.getType();

        if (isRock(blockType)
                || isShovelable(blockType)
                || Tag.LOGS.isTagged(blockType)
                || toolManager.isValidMaterialForTool(blockType, new ItemStack(Material.DIAMOND_PICKAXE))
                || toolManager.isValidMaterialForTool(blockType, new ItemStack(Material.DIAMOND_SHOVEL))
                || toolManager.isValidMaterialForTool(blockType, new ItemStack(Material.DIAMOND_AXE))) {
            affectedBlocks = getBlocksInCube(clickedBlock, radius);
        } else {
            affectedBlocks = getBlocksInCube(clickedBlock, radius).stream()
                    .filter(b -> b.getType() == blockType)
                    .collect(Collectors.toList());
        }

        if (!affectedBlocks.isEmpty()) {
            player.playSound(player.getLocation(), "projecte:custom.pedestruct", 1.0f, 1.0f);
            breakBlocksAndDropAtCenter(
                    player,
                    morningstar,
                    affectedBlocks,
                    clickedBlock.getLocation().add(0.5, 0.5, 0.5));
        }
    }

    private boolean isShovelable(Material material) {
        return Tag.DIRT.isTagged(material)
                || Tag.SAND.isTagged(material)
                || material == Material.GRAVEL
                || material == Material.CLAY;
    }

    private void breakBlocksAndGiveToPlayer(Player player, ItemStack tool, List<Block> blocks) {
        for (Block block : blocks) {
            if (!block.isPassable() && !block.isLiquid()) {
                Collection<ItemStack> drops = block.getDrops(tool);
                block.setType(Material.AIR);
                for (ItemStack drop : drops) {
                    java.util.HashMap<Integer, ItemStack> leftover =
                            player.getInventory().addItem(drop);
                    if (!leftover.isEmpty()) {
                        player.getWorld().dropItemNaturally(block.getLocation(), leftover.get(0));
                    }
                }
            }
        }
    }

    private void breakBlocksAndDropAtCenter(Player player, ItemStack tool, List<Block> blocks, Location dropLocation) {
        for (Block block : blocks) {
            if (block.getType() == Material.AIR
                    || block.getType() == Material.BEDROCK
                    || block.isLiquid()
                    || block.isPassable()) {
                continue;
            }
            Collection<ItemStack> drops = block.getDrops(tool);
            block.setType(Material.AIR);
            for (ItemStack drop : drops) {
                player.getWorld().dropItemNaturally(dropLocation, drop);
            }
        }
    }

    private List<Block> getBlocksInCube(Block centerBlock, int radius) {
        List<Block> blocks = new ArrayList<>();
        Location centerLoc = centerBlock.getLocation();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = centerLoc.clone().add(x, y, z).getBlock();
                    blocks.add(block);
                }
            }
        }
        return blocks;
    }

    private void handleKatarAbility(Player player, ItemStack katar, PlayerInteractEvent event) {
        Action action = event.getAction();
        Block clickedBlock = event.getClickedBlock();

        if (action == Action.RIGHT_CLICK_AIR) {
            handleKatarRightClickAir(player, katar);
        } else if (action == Action.RIGHT_CLICK_BLOCK && clickedBlock != null) {
            Material type = clickedBlock.getType();
            if (Tag.LEAVES.isTagged(type) || Tag.LOGS.isTagged(type)) {
                handleKatarChop(player, katar, clickedBlock);
                event.setCancelled(true);
                return;
            }

            if (type == Material.DIRT || type == Material.GRASS_BLOCK || type == Material.DIRT_PATH) {
                handleKatarTill(player, katar, clickedBlock);
                event.setCancelled(true);
            }
        }
    }

    private void handleKatarTill(Player player, ItemStack katar, Block clickedBlock) {
        int charge = toolManager.getCharge(katar);
        if (charge == 0) return;

        int range = 4; // 9x9 Area
        List<Block> affectedBlocks = new ArrayList<>();
        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                Block block = clickedBlock.getRelative(x, 0, z);
                Material type = block.getType();
                if (type == Material.DIRT || type == Material.GRASS_BLOCK || type == Material.DIRT_PATH) {
                    affectedBlocks.add(block);
                }
            }
        }

        if (!affectedBlocks.isEmpty()) {
            player.playSound(player.getLocation(), org.bukkit.Sound.ITEM_HOE_TILL, 1.0f, 1.0f);
        }

        for (Block block : affectedBlocks) {
            block.setType(Material.FARMLAND);
        }
    }

    private List<Block> findTree(Block startBlock) {
        List<Block> treeBlocks = new ArrayList<>();
        List<Block> toCheck = new ArrayList<>();
        List<Block> checked = new ArrayList<>();

        toCheck.add(startBlock);
        checked.add(startBlock);

        int limit = 5000;
        int count = 0;

        while (!toCheck.isEmpty() && count < limit) {
            Block current = toCheck.remove(0);
            treeBlocks.add(current);
            count++;

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;

                        Block neighbor = current.getRelative(x, y, z);
                        Material type = neighbor.getType();
                        if ((Tag.LOGS.isTagged(type) || Tag.LEAVES.isTagged(type)) && !checked.contains(neighbor)) {
                            toCheck.add(neighbor);
                            checked.add(neighbor);
                        }
                    }
                }
            }
        }
        return treeBlocks;
    }

    private void handleKatarChop(Player player, ItemStack katar, Block clickedBlock) {
        List<Block> blocksToBreak = findTree(clickedBlock);

        if (!blocksToBreak.isEmpty()) {
            player.playSound(player.getLocation(), "projecte:custom.pedestruct", 1.0f, 1.0f);
        }

        breakBlocksAndGiveToPlayer(player, katar, blocksToBreak);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof org.bukkit.entity.ArmorStand) {
            if (event.getDamager() instanceof Player) {
                Player player = (Player) event.getDamager();
                if (toolManager.isRedMatterKatar(player.getInventory().getItemInMainHand())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (!toolManager.isRedMatterKatar(itemInHand)) return;

        if (!itemInHand.hasItemMeta()) return;
        org.bukkit.inventory.meta.ItemMeta meta = itemInHand.getItemMeta();
        org.bukkit.persistence.PersistentDataContainer container = meta.getPersistentDataContainer();
        int currentMode = container.getOrDefault(
                new org.bukkit.NamespacedKey(plugin, "projecte_katar_mode"),
                org.bukkit.persistence.PersistentDataType.INTEGER,
                0);

        // 检查配置是否允许全部模式伤害
        boolean attackAllModeEnabled = plugin.getConfig().getBoolean("Tools.katar_attack_all_mode_enabled", true);

        // 如果配置禁用了全部模式伤害，或者模式是1(仅敌对)
        if (!attackAllModeEnabled || currentMode == 1) {
            // 如果不是怪物，取消伤害
            if (!(event.getEntity() instanceof Monster)) {
                event.setCancelled(true);
            }
        }
    }

    private void handleShearsAbility(Player player, ItemStack shears) {
        int charge = toolManager.getCharge(shears);
        if (charge == 0 && !toolManager.isRedMatterKatar(shears)) return;

        int range;
        boolean isRedMatter = toolManager.isRedMatterShears(shears);
        if (isRedMatter) {
            range = charge == 1 ? 30 : (charge == 2 ? 60 : (charge == 3 ? 90 : 0));
        } else {
            range = charge == 1 ? 30 : (charge == 2 ? 60 : 0);
        }

        if (range == 0) return;

        List<org.bukkit.entity.Sheep> sheepList =
                player.getWorld().getEntitiesByClass(org.bukkit.entity.Sheep.class).stream()
                        .filter(sheep ->
                                !sheep.isSheared() && sheep.getLocation().distance(player.getLocation()) <= range)
                        .collect(java.util.stream.Collectors.toList());

        if (!sheepList.isEmpty()) {
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_SHEEP_SHEAR, 1.0f, 1.0f);
        }

        for (org.bukkit.entity.Sheep sheep : sheepList) {
            sheep.setSheared(true);
            int woolAmount = 1 + new java.util.Random().nextInt(3);
            Material woolType = getWoolMaterial(sheep.getColor());
            ItemStack woolStack = new ItemStack(woolType, woolAmount);

            java.util.HashMap<Integer, ItemStack> leftover =
                    player.getInventory().addItem(woolStack);
            if (!leftover.isEmpty()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover.get(0));
            }
        }
    }

    private Material getWoolMaterial(org.bukkit.DyeColor color) {
        if (color == null) return Material.WHITE_WOOL;
        try {
            return Material.valueOf(color.name() + "_WOOL");
        } catch (IllegalArgumentException e) {
            return Material.WHITE_WOOL;
        }
    }

    private void handleSwordAbility(Player player, ItemStack sword) {
        long now = System.currentTimeMillis();
        long lastUsed = swordCooldowns.getOrDefault(player.getUniqueId(), 0L);

        if (now - lastUsed < SWORD_ABILITY_COOLDOWN) {
            return;
        }

        if (toolManager.isDarkMatterSword(sword)) {
            double damage = 12.0;
            int range = 2;

            Location eyeLocation = player.getEyeLocation();
            Vector direction = eyeLocation.getDirection();
            Location center = eyeLocation.add(direction.multiply(range + 1));

            Collection<Entity> nearbyEntities = player.getWorld().getNearbyEntities(center, range, range, range);

            List<LivingEntity> targets = nearbyEntities.stream()
                    .filter(e -> e instanceof Monster && !e.equals(player))
                    .map(e -> (LivingEntity) e)
                    .collect(Collectors.toList());

            if (targets.isEmpty()) {
                return;
            }

            player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.0f);
            player.playSound(player.getLocation(), "projecte:custom.pecharge", 1.0f, 1.0f);
            for (LivingEntity target : targets) {
                target.damage(damage, player);
            }
            swordCooldowns.put(player.getUniqueId(), now);
        } else if (toolManager.isRedMatterSword(sword)) {
            int charge = toolManager.getCharge(sword);
            if (charge == 0) return;

            double damage = 18.0;
            int range = charge;
            int mode = toolManager.getSwordMode(sword);

            Location eyeLocation = player.getEyeLocation();
            Vector direction = eyeLocation.getDirection();
            Location center = eyeLocation.add(direction.multiply(range + 1));

            Collection<Entity> nearbyEntities = player.getWorld().getNearbyEntities(center, range, range, range);

            List<LivingEntity> targets = nearbyEntities.stream()
                    .filter(e -> e instanceof LivingEntity && !e.equals(player))
                    .filter(e -> {
                        if (mode == 0) {
                            return e instanceof Monster;
                        } else {
                            return true;
                        }
                    })
                    .map(e -> (LivingEntity) e)
                    .collect(Collectors.toList());

            if (targets.isEmpty()) {
                return;
            }

            player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.5f);
            player.playSound(player.getLocation(), "projecte:custom.pecharge", 1.0f, 1.0f);
            for (LivingEntity target : targets) {
                target.damage(damage, player);
            }
            swordCooldowns.put(player.getUniqueId(), now);
        }
    }

    private void handleHammerAbility(Player player, ItemStack hammer, Block clickedBlock) {
        int charge = toolManager.getCharge(hammer);
        if (charge == 0) return;

        int width, height, depth;
        switch (charge) {
            case 1:
                width = 3;
                height = 3;
                depth = 2;
                break;
            case 2:
                width = 5;
                height = 4;
                depth = 3;
                break;
            case 3:
                width = 7;
                height = 5;
                depth = 4;
                break;
            default:
                return;
        }

        List<Block> affectedBlocks = getBlocksInVolume(clickedBlock, player.getFacing(), width, height, depth);

        if (!affectedBlocks.isEmpty()) {
            player.playSound(player.getLocation(), "projecte:custom.pedestruct", 1.0f, 1.0f);
        }

        for (Block block : affectedBlocks) {
            if (isRock(block.getType())) {
                block.breakNaturally(hammer);
            } else if (block.isLiquid()) {
                block.setType(Material.AIR);
            }
        }
    }

    private boolean isRock(Material material) {
        String name = material.name();
        return name.endsWith("_ORE")
                || name.endsWith("STONE")
                || name.equals("GRANITE")
                || name.equals("DIORITE")
                || name.equals("ANDESITE")
                || name.equals("DEEPSLATE")
                || name.equals("NETHERRACK")
                || name.equals("BASALT")
                || name.equals("BLACKSTONE")
                || name.equals("END_STONE")
                || name.equals("OBSIDIAN");
    }

    private List<Block> getBlocksInVolume(Block start, BlockFace facing, int width, int height, int depth) {
        List<Block> blocks = new ArrayList<>();
        int halfWidth = (width - 1) / 2;
        int halfHeight = (height - 1) / 2;

        boolean isHorizontal = (facing == BlockFace.NORTH
                || facing == BlockFace.SOUTH
                || facing == BlockFace.EAST
                || facing == BlockFace.WEST);

        if (isHorizontal) {
            for (int d = 0; d < depth; d++) {
                Block depthBlock = start.getRelative(facing.getOppositeFace(), d);
                for (int w = -halfWidth; w <= halfWidth; w++) {
                    for (int h = -halfHeight; h <= halfHeight; h++) {
                        Block horizontalBlock = depthBlock.getRelative(getPerpendicularFace(facing), w);
                        blocks.add(horizontalBlock.getRelative(BlockFace.UP, h));
                    }
                }
            }
        } else {
            for (int d = 0; d < depth; d++) {
                Block depthBlock = start.getRelative(facing.getOppositeFace(), d);
                for (int w = -halfWidth; w <= halfWidth; w++) {
                    for (int h = -halfWidth; h <= halfWidth; h++) {
                        blocks.add(depthBlock.getRelative(w, 0, h));
                    }
                }
            }
        }
        return blocks;
    }

    private BlockFace getPerpendicularFace(BlockFace facing) {
        switch (facing) {
            case NORTH:
            case SOUTH:
                return BlockFace.EAST;
            case EAST:
            case WEST:
            default:
                return BlockFace.NORTH;
        }
    }

    private void handlePickaxeAbility(Player player, ItemStack pickaxe, Block clickedBlock) {
        if (clickedBlock == null) return;

        Material blockType = clickedBlock.getType();
        if (!blockType.toString().endsWith("_ORE")) {
            return;
        }

        List<Block> vein = findVein(clickedBlock);

        if (!vein.isEmpty()) {
            player.playSound(player.getLocation(), "projecte:custom.pedestruct", 1.0f, 1.0f);
        }

        for (Block block : vein) {
            block.breakNaturally(pickaxe);
        }
    }

    private List<Block> findVein(Block startBlock) {
        List<Block> vein = new ArrayList<>();
        List<Block> toCheck = new ArrayList<>();
        List<Block> checked = new ArrayList<>();

        toCheck.add(startBlock);
        checked.add(startBlock);
        Material oreType = startBlock.getType();

        while (!toCheck.isEmpty()) {
            Block current = toCheck.remove(0);
            vein.add(current);

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;

                        Block neighbor = current.getRelative(x, y, z);
                        if (neighbor.getType() == oreType && !checked.contains(neighbor)) {
                            toCheck.add(neighbor);
                            checked.add(neighbor);
                        }
                    }
                }
            }
        }
        return vein;
    }

    private void handleShovelAbility(Player player, ItemStack shovel, Block clickedBlock) {
        if (clickedBlock == null) return;

        Material blockType = clickedBlock.getType();
        if (!blockType.toString().endsWith("_CONCRETE_POWDER")
                && !Tag.SAND.isTagged(blockType)
                && blockType != Material.CLAY
                && blockType != Material.DIRT
                && blockType != Material.GRASS_BLOCK
                && blockType != Material.DIRT_PATH
                && blockType != Material.GRAVEL
                && blockType != Material.SOUL_SAND
                && blockType != Material.SOUL_SOIL) {
            return;
        }

        int charge = toolManager.getCharge(shovel);
        if (charge == 0) return;

        int range;
        boolean isDarkMatter = toolManager.isDarkMatterShovel(shovel);

        if (isDarkMatter) {
            range = (charge >= 1) ? 1 : 0;
        } else {
            range = charge;
        }

        if (range == 0) return;

        BlockFace facing = player.getFacing();
        List<Block> affectedBlocks = getBlocksInArea(clickedBlock, facing, range);

        for (Block block : affectedBlocks) {
            if (block.getType() == blockType) {
                block.breakNaturally(shovel);
            }
        }
    }

    private void handleHoeAbility(Player player, ItemStack hoe, Block clickedBlock) {
        if (clickedBlock == null) return;

        Material blockType = clickedBlock.getType();
        if (blockType != Material.DIRT && blockType != Material.GRASS_BLOCK && blockType != Material.DIRT_PATH) {
            return;
        }

        int charge = toolManager.getCharge(hoe);
        if (charge == 0) return;

        int range;
        boolean isDarkMatter = toolManager.isDarkMatterHoe(hoe);

        if (isDarkMatter) {
            range = (charge == 1) ? 1 : (charge == 2 ? 2 : 0);
        } else {
            range = (charge == 1) ? 1 : (charge == 2 ? 2 : (charge == 3 ? 3 : 0));
        }

        if (range == 0) return;

        BlockFace facing = player.getFacing();
        List<Block> affectedBlocks = getBlocksInArea(clickedBlock, facing, range);

        for (Block block : affectedBlocks) {
            Material type = block.getType();
            if (type == Material.DIRT || type == Material.GRASS_BLOCK || type == Material.DIRT_PATH) {
                block.setType(Material.FARMLAND);
            }
        }
    }

    private List<Block> getBlocksInArea(Block centerBlock, BlockFace facing, int range) {
        List<Block> blocks = new ArrayList<>();
        Location centerLoc = centerBlock.getLocation();
        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                Block block = centerLoc.clone().add(x, 0, z).getBlock();
                blocks.add(block);
            }
        }
        return blocks;
    }
}
