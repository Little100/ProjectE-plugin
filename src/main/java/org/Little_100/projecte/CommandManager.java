package org.Little_100.projecte;

import org.Little_100.projecte.TransmutationTable.NoEmcItemGUI;
import org.Little_100.projecte.storage.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandManager implements CommandExecutor, TabCompleter {

    private final ProjectE plugin;
    private final DatabaseManager databaseManager;
    private final EmcManager emcManager;
    private final LanguageManager languageManager;

    public CommandManager(ProjectE plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.emcManager = plugin.getEmcManager();
        this.languageManager = plugin.getLanguageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                return handleReload(sender);
            case "setemc":
                return handleSetEmc(sender, args);
            case "debug":
                return handleDebug(sender);
            case "give":
                return handleGiveEmc(sender, args);
            case "noemcitem":
                return handleNoEmcItem(sender);
            case "bag":
                return handleBag(sender, args);
            case "lang":
                return handleLang(sender, args);
            case "report":
                return handleReport(sender);
            case "nbtdebug":
                return handleNbtDebug(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(languageManager.get("serverside.command.help.header"));
        sender.sendMessage(languageManager.get("serverside.command.help.reload"));
        sender.sendMessage(languageManager.get("serverside.command.help.setemc"));
        sender.sendMessage(languageManager.get("serverside.command.help.give"));
        sender.sendMessage(languageManager.get("serverside.command.help.debug"));
        sender.sendMessage(languageManager.get("serverside.command.help.noemcitem"));
        sender.sendMessage(languageManager.get("serverside.command.help.bag_list"));
        sender.sendMessage(languageManager.get("serverside.command.help.lang"));
        sender.sendMessage(languageManager.get("serverside.command.help.report"));
        // 移除 resourcepack 帮助信息
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("projecte.command.reload")) {
            sender.sendMessage(languageManager.get("serverside.command.no_permission"));
            return true;
        }
        plugin.reloadPlugin();
        sender.sendMessage(languageManager.get("serverside.command.reload_success"));
        return true;
    }

    private boolean handleSetEmc(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(languageManager.get("serverside.command.player_only"));
            return true;
        }
        if (!sender.hasPermission("projecte.command.setemc")) {
            sender.sendMessage(languageManager.get("serverside.command.no_permission"));
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage(languageManager.get("serverside.command.set_emc.usage"));
            return true;
        }

        Player player = (Player) sender;
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand.getType().isAir()) {
            sender.sendMessage(languageManager.get("serverside.command.set_emc.hold_item"));
            return true;
        }

        try {
            long emc = Long.parseLong(args[1]);
            if (emc <= 0) {
                sender.sendMessage(languageManager.get("serverside.command.set_emc.must_be_positive"));
                return true;
            }

            String itemKey = plugin.getVersionAdapter().getItemKey(itemInHand);
            databaseManager.setEmc(itemKey, emc);
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("item", itemKey);
            placeholders.put("emc", String.valueOf(emc));
            sender.sendMessage(languageManager.get("serverside.command.set_emc.success", placeholders));
        } catch (NumberFormatException e) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("value", args[1]);
            sender.sendMessage(languageManager.get("serverside.command.set_emc.invalid_value", placeholders));
        }
        return true;
    }

    private boolean handleDebug(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(languageManager.get("serverside.command.player_only"));
            return true;
        }
        if (!sender.hasPermission("projecte.command.debug")) {
            sender.sendMessage(languageManager.get("serverside.command.no_permission"));
            return true;
        }

        Player player = (Player) sender;
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand.getType().isAir()) {
            sender.sendMessage(languageManager.get("serverside.command.set_emc.hold_item"));
            return true;
        }

        String itemKey = plugin.getVersionAdapter().getItemKey(itemInHand);
        long emc = emcManager.getEmc(itemKey);
        boolean learned = databaseManager.isLearned(player.getUniqueId(), itemKey);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("item", itemKey);
        sender.sendMessage(languageManager.get("serverside.command.debug.header", placeholders));

        placeholders.put("emc", String.valueOf(emc));
        sender.sendMessage(languageManager.get("serverside.command.debug.emc_value", placeholders));

        placeholders.put("learned", learned ? "是" : "否");
        sender.sendMessage(languageManager.get("serverside.command.debug.is_learned", placeholders));


        java.util.List<org.bukkit.inventory.Recipe> recipes = org.bukkit.Bukkit.getRecipesFor(itemInHand);
        if (recipes.isEmpty()) {
            sender.sendMessage(languageManager.get("serverside.command.debug.no_recipe"));
        } else {
            placeholders.put("count", String.valueOf(recipes.size()));
            sender.sendMessage(languageManager.get("serverside.command.debug.recipe_found", placeholders));
            for (int i = 0; i < recipes.size(); i++) {
                org.bukkit.inventory.Recipe recipe = recipes.get(i);
                Map<String, String> recipePlaceholders = new HashMap<>();
                recipePlaceholders.put("index", String.valueOf(i + 1));
                sender.sendMessage(languageManager.get("serverside.command.debug.recipe_header", recipePlaceholders));
                String divisionStrategy = plugin.getConfig().getString("TransmutationTable.EMC.divisionStrategy", "floor").toLowerCase();
                java.util.List<String> debugInfo = plugin.getVersionAdapter().getRecipeDebugInfo(recipe, divisionStrategy);
                for (String line : debugInfo) {
                    sender.sendMessage(ChatColor.GRAY + "  " + line);
                }
            }
        }

        sender.sendMessage(languageManager.get("serverside.command.debug.footer"));

        return true;
    }
    private boolean handleGiveEmc(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(languageManager.get("serverside.command.player_only"));
            return true;
        }
        if (!sender.hasPermission("projecte.command.give")) {
            sender.sendMessage(languageManager.get("serverside.command.no_permission"));
            return true;
        }
        if (args.length != 3) {
            sender.sendMessage(languageManager.get("serverside.command.give_emc.usage"));
            return true;
        }

        Player senderPlayer = (Player) sender;
        Player targetPlayer = Bukkit.getPlayer(args[1]);

        if (targetPlayer == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", args[1]);
            sender.sendMessage(languageManager.get("serverside.command.give_emc.player_not_found", placeholders));
            return true;
        }

        if (targetPlayer.equals(senderPlayer)) {
            sender.sendMessage(languageManager.get("serverside.command.give_emc.cant_give_self"));
            return true;
        }

        long amount;
        try {
            amount = Long.parseLong(args[2]);
            if (amount <= 0) {
                sender.sendMessage(languageManager.get("serverside.command.give_emc.must_be_positive"));
                return true;
            }
        } catch (NumberFormatException e) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("amount", args[2]);
            sender.sendMessage(languageManager.get("serverside.command.give_emc.invalid_amount", placeholders));
            return true;
        }

        double feePercentage = plugin.getConfig().getDouble("TransmutationTable.transfer-fee-percentage", 0.0);
        long fee = (long) (amount * (feePercentage / 100.0));
        long totalDeduction = amount + fee;

        long senderEmc = databaseManager.getPlayerEmc(senderPlayer.getUniqueId());

        if (senderEmc < totalDeduction) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("total", String.valueOf(totalDeduction));
            placeholders.put("fee", String.valueOf(fee));
            sender.sendMessage(languageManager.get("serverside.command.give_emc.not_enough_emc", placeholders));
            return true;
        }

        long targetEmc = databaseManager.getPlayerEmc(targetPlayer.getUniqueId());

        databaseManager.setPlayerEmc(senderPlayer.getUniqueId(), senderEmc - totalDeduction);
        databaseManager.setPlayerEmc(targetPlayer.getUniqueId(), targetEmc + amount);

        Map<String, String> senderPlaceholders = new HashMap<>();
        senderPlaceholders.put("player", targetPlayer.getName());
        senderPlaceholders.put("amount", String.valueOf(amount));
        senderPlaceholders.put("fee", String.valueOf(fee));
        senderPlayer.sendMessage(languageManager.get("serverside.command.give_emc.give_success", senderPlaceholders));

        Map<String, String> targetPlaceholders = new HashMap<>();
        targetPlaceholders.put("player", senderPlayer.getName());
        targetPlaceholders.put("amount", String.valueOf(amount));
        targetPlayer.sendMessage(languageManager.get("serverside.command.give_emc.receive_success", targetPlaceholders));

        return true;
    }

    private boolean handleNoEmcItem(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(languageManager.get("serverside.command.player_only"));
            return true;
        }
        if (!sender.hasPermission("projecte.command.noemcitem")) {
            sender.sendMessage(languageManager.get("serverside.command.no_permission"));
            return true;
        }

        Player player = (Player) sender;
        List<ItemStack> noEmcItems = new ArrayList<>();
        for (Material material : Material.values()) {
            if (material.isItem() && !material.isAir() && !material.name().startsWith("LEGACY_")) {
                ItemStack item = new ItemStack(material);
                String itemKey = plugin.getVersionAdapter().getItemKey(item);
                if (emcManager.getEmc(itemKey) == 0) {
                    noEmcItems.add(item);
                }
            }
        }

        if (noEmcItems.isEmpty()) {
            sender.sendMessage(languageManager.get("serverside.command.no_emc_item.all_have_emc"));
            return true;
        }

        new NoEmcItemGUI(noEmcItems, 0).openInventory(player);
        return true;
    }

    private boolean handleBag(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(languageManager.get("serverside.command.player_only"));
            return true;
        }
        if (!sender.hasPermission("projecte.command.bag")) {
            sender.sendMessage(languageManager.get("serverside.command.no_permission"));
            return true;
        }
        if (args.length < 2 || !args[1].equalsIgnoreCase("list")) {
            sender.sendMessage(languageManager.get("serverside.command.bag.usage"));
            return true;
        }

        Player player = (Player) sender;
        List<String> bagColors = databaseManager.getBagColors(player.getUniqueId());

        if (bagColors.isEmpty()) {
            sender.sendMessage(languageManager.get("serverside.command.bag.no_bags"));
            return true;
        }

        sender.sendMessage(languageManager.get("serverside.command.bag.list_header"));
        for (String colorName : bagColors) {
            try {
                ChatColor chatColor = ChatColor.valueOf(colorName.toUpperCase());
                sender.sendMessage(chatColor + "- " + colorName);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ChatColor.WHITE + "- " + colorName);
            }
        }
        return true;
    }

    private boolean handleLang(CommandSender sender, String[] args) {
        if (!sender.hasPermission("projecte.command.lang")) {
            sender.sendMessage(languageManager.get("serverside.command.no_permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(languageManager.get("serverside.command.lang.usage"));
            return true;
        }

        String action = args[1].toLowerCase();
        if (action.equals("list")) {
            sender.sendMessage(languageManager.get("serverside.command.lang.list_header"));
            File langFolder = plugin.getDataFolder();
            File[] files = langFolder.listFiles((dir, name) -> name.endsWith(".yml") && !name.equals("config.yml"));
            if (files != null) {
                for (File file : files) {
                    sender.sendMessage(ChatColor.YELLOW + "- " + file.getName().replace(".yml", ""));
                }
            }
            return true;
        }

        if (action.equals("set")) {
            if (args.length < 3) {
                sender.sendMessage(languageManager.get("serverside.command.lang.usage"));
                return true;
            }
            List<String> newLangs = new ArrayList<>(Arrays.asList(args).subList(2, args.length));
            for (String lang : newLangs) {
                File langFile = new File(plugin.getDataFolder(), lang + ".yml");
                if (!langFile.exists()) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("file", lang + ".yml");
                    sender.sendMessage(languageManager.get("serverside.command.lang.file_not_found", placeholders));
                    return true;
                }
            }

            plugin.getConfig().set("language", newLangs);
            plugin.saveConfig();
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("languages", String.join(", ", newLangs));
            sender.sendMessage(languageManager.get("serverside.command.lang.set_success", placeholders));
            return true;
        }

        sender.sendMessage(languageManager.get("serverside.command.lang.usage"));
        return true;
    }

    private boolean handleReport(CommandSender sender) {
        sender.sendMessage(languageManager.get("serverside.command.report.message"));
        return true;
    }
    
    private boolean handleNbtDebug(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "该命令只能由玩家执行。");
            return true;
        }
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            sender.sendMessage(ChatColor.YELLOW + "请手持一个物品再执行本命令。");
            return true;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.AQUA).append("【NBT调试】").append("\n");
        sb.append(ChatColor.GRAY).append("类型: ").append(item.getType().name()).append("\n");
        if (item.hasItemMeta()) {
            sb.append(ChatColor.GRAY).append("DisplayName: ").append(item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : "无").append("\n");
            sb.append(ChatColor.GRAY).append("Lore: ").append(item.getItemMeta().hasLore() ? item.getItemMeta().getLore() : "无").append("\n");
            if (item.getItemMeta().hasCustomModelData()) {
                sb.append(ChatColor.GRAY).append("CustomModelData: ").append(item.getItemMeta().getCustomModelData()).append("\n");
            } else {
                sb.append(ChatColor.GRAY).append("CustomModelData: 无\n");
            }
            sb.append(ChatColor.GRAY).append("PDC: ");
            var pdc = item.getItemMeta().getPersistentDataContainer();
            var keys = pdc.getKeys();
            if (keys.isEmpty()) {
                sb.append("无\n");
            } else {
                sb.append("\n");
                for (var key : keys) {
                    sb.append("  - ").append(key.toString()).append(": ");
                    Object value = pdc.get(key, org.bukkit.persistence.PersistentDataType.STRING);
                    if (value != null) {
                        sb.append(value).append(" (String)");
                    } else {
                        value = pdc.get(key, org.bukkit.persistence.PersistentDataType.BYTE);
                        if (value != null) {
                            sb.append(value).append(" (Byte)");
                        } else {
                            sb.append("存在（未知类型）");
                        }
                    }
                    sb.append("\n");
                }
            }
        } else {
            sb.append(ChatColor.GRAY).append("无ItemMeta\n");
        }
        player.sendMessage(sb.toString());
        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            final List<String> subCommands = Arrays.asList("reload", "setemc", "debug", "give", "noemcitem", "bag", "lang", "report", "nbtdebug");
            return StringUtil.copyPartialMatches(args[0], subCommands, new ArrayList<>());
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give")) {
                List<String> playerNames = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    playerNames.add(player.getName());
                }
                return StringUtil.copyPartialMatches(args[1], playerNames, new ArrayList<>());
            }
            if (args[0].equalsIgnoreCase("bag")) {
                return StringUtil.copyPartialMatches(args[1], Collections.singletonList("list"), new ArrayList<>());
            }
            if (args[0].equalsIgnoreCase("lang")) {
                return StringUtil.copyPartialMatches(args[1], Arrays.asList("list", "set"), new ArrayList<>());
            }
        }

        if (args.length >= 3 && args[0].equalsIgnoreCase("lang") && args[1].equalsIgnoreCase("set")) {
            List<String> langFiles = new ArrayList<>();
            File langFolder = plugin.getDataFolder();
            File[] files = langFolder.listFiles((dir, name) -> name.endsWith(".yml") && !name.equals("config.yml"));
            if (files != null) {
                for (File file : files) {
                    langFiles.add(file.getName().replace(".yml", ""));
                }
            }
            return StringUtil.copyPartialMatches(args[args.length - 1], langFiles, new ArrayList<>());
        }
        return new ArrayList<>();
    }
}