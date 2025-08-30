package org.Little_100.projecte.Tools;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AreaOfEffectManager {

    public static List<Block> getBlocksIn3x3Area(Player player, Block origin) {
        List<Block> affectedBlocks = new ArrayList<>();
        BlockFace facing = player.getFacing();

        int originX = origin.getX();
        int originY = origin.getY();
        int originZ = origin.getZ();

        switch (facing) {
            case UP:
            case DOWN:
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && z == 0) continue;
                        affectedBlocks.add(origin.getWorld().getBlockAt(originX + x, originY, originZ + z));
                    }
                }
                break;
            case NORTH:
            case SOUTH:
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        if (x == 0 && y == 0) continue;
                        affectedBlocks.add(origin.getWorld().getBlockAt(originX + x, originY + y, originZ));
                    }
                }
                break;
            case EAST:
            case WEST:
                for (int z = -1; z <= 1; z++) {
                    for (int y = -1; y <= 1; y++) {
                        if (z == 0 && y == 0) continue;
                        affectedBlocks.add(origin.getWorld().getBlockAt(originX, originY + y, originZ + z));
                    }
                }
                break;
        }
        return affectedBlocks;
    }

    public static List<Block> getBlocksInTallArea(Block origin) {
        List<Block> affectedBlocks = new ArrayList<>();
        affectedBlocks.add(origin.getRelative(BlockFace.UP));
        affectedBlocks.add(origin.getRelative(BlockFace.DOWN));
        return affectedBlocks;
    }

    public static List<Block> getBlocksInWideArea(Player player, Block origin) {
        List<Block> affectedBlocks = new ArrayList<>();
        BlockFace facing = player.getFacing();

        switch (facing) {
            case UP:
            case DOWN:
            case NORTH:
            case SOUTH:
                affectedBlocks.add(origin.getRelative(1, 0, 0));
                affectedBlocks.add(origin.getRelative(-1, 0, 0));
                break;
            case EAST:
            case WEST:
                affectedBlocks.add(origin.getRelative(0, 0, 1));
                affectedBlocks.add(origin.getRelative(0, 0, -1));
                break;
        }
        return affectedBlocks;
    }

    public static List<Block> getBlocksInLongArea(Player player, Block origin) {
        List<Block> affectedBlocks = new ArrayList<>();
        BlockFace facing = player.getFacing();

        BlockFace forward = facing;
        BlockFace backward = facing.getOppositeFace();

        affectedBlocks.add(origin.getRelative(forward));
        affectedBlocks.add(origin.getRelative(backward));

        return affectedBlocks;
    }
}