package org.Little_100.projecte.AlchemicalBag;

import org.Little_100.projecte.LanguageManager;
import org.Little_100.projecte.ProjectE;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import net.md_5.bungee.api.ChatColor;

import java.util.HashMap;
import java.util.Map;

public class PlayerInteractListener implements Listener {

    private final ProjectE plugin;
    private final AlchemicalBagManager alchemicalBagManager;
    private final InventoryManager inventoryManager;
    private final LanguageManager languageManager;

    private static final Map<Color, DyeColor> BUKKIT_TO_DYE_COLOR = new HashMap<>();
    private static final Color DEFAULT_LEATHER_COLOR = Bukkit.getServer().getItemFactory().getDefaultLeatherColor();

    static {
        BUKKIT_TO_DYE_COLOR.put(Color.WHITE, DyeColor.WHITE);
        BUKKIT_TO_DYE_COLOR.put(Color.ORANGE, DyeColor.ORANGE);
        BUKKIT_TO_DYE_COLOR.put(Color.PURPLE, DyeColor.MAGENTA);
        BUKKIT_TO_DYE_COLOR.put(Color.AQUA, DyeColor.LIGHT_BLUE);
        BUKKIT_TO_DYE_COLOR.put(Color.YELLOW, DyeColor.YELLOW);
        BUKKIT_TO_DYE_COLOR.put(Color.LIME, DyeColor.LIME);
        BUKKIT_TO_DYE_COLOR.put(Color.FUCHSIA, DyeColor.PINK);
        BUKKIT_TO_DYE_COLOR.put(Color.GRAY, DyeColor.GRAY);
        BUKKIT_TO_DYE_COLOR.put(Color.SILVER, DyeColor.LIGHT_GRAY);
        BUKKIT_TO_DYE_COLOR.put(Color.BLUE, DyeColor.CYAN);
        BUKKIT_TO_DYE_COLOR.put(Color.RED, DyeColor.RED);
        BUKKIT_TO_DYE_COLOR.put(Color.BLACK, DyeColor.BLACK);
        BUKKIT_TO_DYE_COLOR.put(Color.fromRGB(102, 76, 51), DyeColor.BROWN);
        BUKKIT_TO_DYE_COLOR.put(Color.fromRGB(0, 128, 0), DyeColor.GREEN);
        BUKKIT_TO_DYE_COLOR.put(Color.fromRGB(128, 0, 128), DyeColor.PURPLE);
        BUKKIT_TO_DYE_COLOR.put(Color.fromRGB(51, 76, 178), DyeColor.BLUE);
    }

    public PlayerInteractListener(ProjectE plugin, AlchemicalBagManager alchemicalBagManager,
            InventoryManager inventoryManager) {
        this.plugin = plugin;
        this.alchemicalBagManager = alchemicalBagManager;
        this.inventoryManager = inventoryManager;
        this.languageManager = plugin.getLanguageManager();
    }

    public static String getBagColorIdentifier(Color color) {
        if (color == null || color.equals(DEFAULT_LEATHER_COLOR)) {
            return "DEFAULT";
        }
        DyeColor dye = BUKKIT_TO_DYE_COLOR.get(color);
        if (dye != null) {
            return dye.name();
        }
        return "RGB_" + color.getRed() + "_" + color.getGreen() + "_" + color.getBlue();
    }

    public static ChatColor getChatColor(String colorIdentifier) {
        if (colorIdentifier == null || colorIdentifier.equals("DEFAULT") || colorIdentifier.startsWith("RGB_")) {
            return ChatColor.WHITE;
        }
        try {
            DyeColor dye = DyeColor.valueOf(colorIdentifier.toUpperCase());
            switch (dye) {
                case WHITE:
                    return ChatColor.WHITE;
                case ORANGE:
                    return ChatColor.GOLD;
                case MAGENTA:
                    return ChatColor.LIGHT_PURPLE;
                case LIGHT_BLUE:
                    return ChatColor.AQUA;
                case YELLOW:
                    return ChatColor.YELLOW;
                case LIME:
                    return ChatColor.GREEN;
                case PINK:
                    return ChatColor.LIGHT_PURPLE;
                case GRAY:
                    return ChatColor.DARK_GRAY;
                case LIGHT_GRAY:
                    return ChatColor.GRAY;
                case CYAN:
                    return ChatColor.DARK_AQUA;
                case PURPLE:
                    return ChatColor.DARK_PURPLE;
                case BLUE:
                    return ChatColor.BLUE;
                case BROWN:
                    return ChatColor.DARK_RED;
                case GREEN:
                    return ChatColor.DARK_GREEN;
                case RED:
                    return ChatColor.RED;
                case BLACK:
                    return ChatColor.BLACK;
                default:
                    return ChatColor.WHITE;
            }
        } catch (IllegalArgumentException e) {
            return ChatColor.WHITE;
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand != null && itemInHand.getType() == Material.LEATHER_HORSE_ARMOR) {
            event.setCancelled(true);
            LeatherArmorMeta meta = (LeatherArmorMeta) itemInHand.getItemMeta();
            if (meta == null) {
                return;
            }

            Color bukkitColor = meta.getColor();
            String bagColorIdentifier = getBagColorIdentifier(bukkitColor);

            ItemStack[] inventoryContents = plugin.getDatabaseManager().loadBagInventory(player.getUniqueId(),
                    bagColorIdentifier);

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("color", getChatColor(bagColorIdentifier).toString());
            placeholders.put("color_name", bagColorIdentifier);
            String inventoryTitle = languageManager.get("clientside.alchemical_bag.colored_name", placeholders);
            if (bagColorIdentifier.equals("DEFAULT")) {
                inventoryTitle = languageManager.get("clientside.alchemical_bag.default_name");
            }

            Inventory bagInventory = Bukkit.createInventory(null, 54, inventoryTitle);

            bagInventory.setContents(inventoryContents);

            inventoryManager.openBagInventory(player.getUniqueId(), bagColorIdentifier, bagInventory);

            player.openInventory(bagInventory);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand != null && itemInHand.getType() == Material.LEATHER_HORSE_ARMOR) {
            event.setCancelled(true);
        }
    }
}