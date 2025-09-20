package org.Little_100.projecte.managers;

import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.gui.GemHelmetGUI;
import org.Little_100.projecte.gui.NoEmcItemGUI;
import org.Little_100.projecte.gui.PhilosopherStoneGUI;
import org.Little_100.projecte.gui.TransmutationGUI;
import org.Little_100.projecte.storage.DatabaseManager;
import org.Little_100.projecte.tools.DiviningRod;
import org.Little_100.projecte.util.Constants;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.StringUtil;
import org.geysermc.geyser.api.GeyserApi;

import java.io.File;
import java.util.*;

public class CommandManager implements CommandExecutor, TabCompleter {

    private final ProjectE plugin;
    private final Map<String, Map<String, String>> openTableCommands = new HashMap<>();
    private final Map<Player, String> openGuiEditors = new HashMap<>();
    private final DatabaseManager databaseManager;
    private final EmcManager emcManager;
    private final LanguageManager languageManager;

    public CommandManager(ProjectE plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.emcManager = plugin.getEmcManager();
        this.languageManager = plugin.getLanguageManager();
        loadCommands();
    }

    private void loadCommands() {
        File commandsFile = new File(plugin.getDataFolder(), "command.yml");
        if (!commandsFile.exists()) {
            plugin.saveResource("command.yml", false);
        }
        FileConfiguration commandsConfig = YamlConfiguration.loadConfiguration(commandsFile);
        if (commandsConfig.isConfigurationSection("OpenTransmutationTable")) {
            for (String key : commandsConfig
                    .getConfigurationSection("OpenTransmutationTable")
                    .getKeys(false)) {
                String command = commandsConfig
                        .getString("OpenTransmutationTable." + key + ".command")
                        .replace("/", "");
                String permission = commandsConfig.getString("OpenTransmutationTable." + key + ".permission");
                String permissionMessage =
                        commandsConfig.getString("OpenTransmutationTable." + key + ".permission-message");
                Map<String, String> commandData = new HashMap<>();
                commandData.put("permission", permission);
                commandData.put("permission-message", permissionMessage);
                openTableCommands.put(command, commandData);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String commandName = command.getName().toLowerCase();

        if (openTableCommands.containsKey(commandName)) {
            handleOpenTable(sender, commandName);
            return true;
        }

        if (command.getName().equalsIgnoreCase("projecte")) {
            if (args.length == 0) {
                if (label.equalsIgnoreCase("po")) {
                    handleOpen(sender);
                    return true;
                }

                sendHelp(sender);
                return true;
            }

            String subCommand = args[0].toLowerCase();

            String fullCommand = "projecte " + subCommand;
            if (openTableCommands.containsKey(fullCommand)) {
                handleOpenTable(sender, fullCommand);
                return true;
            }

            switch (subCommand) {
                case "recalculate":
                    handleRecalculate(sender);
                    break;
                case "reload":
                    handleReload(sender);
                    break;
                case "setemc":
                    handleSetEmc(sender, args);
                    break;
                case "debug":
                    handleDebug(sender);
                    break;
                case "pay":
                    handlePayEmc(sender, args);
                    break;
                case "item":
                    handleGetItem(sender, args);
                    break;
                case "give":
                    handleGiveItem(sender, args);
                    break;
                case "noemcitem":
                    handleNoEmcItem(sender);
                    break;
                case "bag":
                    handleBag(sender, args);
                    break;
                case "lang":
                    handleLang(sender, args);
                    break;
                case "report":
                    handleReport(sender);
                    break;
                case "nbtdebug":
                    handleNbtDebug(sender);
                    break;
                case "o":
                case "open":
                    handleOpen(sender);
                    break;
                case "gui":
                    handleGui(sender, args);
                    break;
                case "table":
                    handleTableCommand(sender, args);
                    break;
                case "gemhelmet":
                    handleGemHelmet(sender);
                    break;
                case "gemboots":
                    handleGemBoots(sender);
                    break;
                default:
                    sendHelp(sender);
                    break;
            }

            return true;
        }

        return false;
    }

    public Map<Player, String> getOpenGuiEditors() {
        return openGuiEditors;
    }

    private void handleGui(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(languageManager.get("serverside.command.player_only"));
            return;
        }
        if (!sender.hasPermission("projecte.command.gui")) {
            sender.sendMessage(languageManager.get("serverside.command.no_permission"));
            return;
        }
        if (args.length != 2) {
            sender.sendMessage(languageManager.get("serverside.command.gui.usage"));
            return;
        }

        String fileName = args[1];
        if (!fileName.toLowerCase().endsWith(".yml")) {
            fileName += ".yml";
        }

        Player player = (Player) sender;
        String title = "GUI Editor: " + fileName;
        Inventory gui = Bukkit.createInventory(player, 54, title); // Use player as owner

        openGuiEditors.put(player, fileName);
        player.openInventory(gui);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("file", fileName);
        player.sendMessage(languageManager.get("serverside.command.gui.opening_editor", placeholders));
    }

    private void handleRecalculate(CommandSender sender) {
        if (!sender.hasPermission("projecte.command.recalculate")) {
            sender.sendMessage(languageManager.get("serverside.command.no_permission"));
            return;
        }

        plugin.getSchedulerAdapter().runTaskAsynchronously(() -> {
            sender.sendMessage(languageManager.get("serverside.command.recalculate.start"));
            emcManager.calculateAndStoreEmcValues(true);
            sender.sendMessage(languageManager.get("serverside.command.recalculate.success"));
        });
    }

    private void handleOpenTable(CommandSender sender, String commandName) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(languageManager.get("serverside.command.player_only"));
            return;
        }

        Map<String, String> commandData = openTableCommands.get(commandName);
        String permission = commandData.get("permission");

        if (!permission.equalsIgnoreCase("default")
                && !player.hasPermission("projecte.command." + permission)
                && !(permission.equalsIgnoreCase("op") && player.isOp())) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', commandData.get("permission-message")));
            return;
        }

        new TransmutationGUI(player).open();
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(languageManager.get("serverside.command.help.header"));
        sender.sendMessage(languageManager.get("serverside.command.help.reload"));
        sender.sendMessage(languageManager.get("serverside.command.help.setemc"));
        sender.sendMessage(languageManager.get("serverside.command.help.pay"));
        sender.sendMessage(languageManager.get("serverside.command.help.item"));
        sender.sendMessage(languageManager.get("serverside.command.help.give"));
        sender.sendMessage(languageManager.get("serverside.command.help.debug"));
        sender.sendMessage(languageManager.get("serverside.command.help.noemcitem"));
        sender.sendMessage(languageManager.get("serverside.command.help.bag_list"));
        sender.sendMessage(languageManager.get("serverside.command.help.lang"));
        sender.sendMessage(languageManager.get("serverside.command.help.report"));
        sender.sendMessage(languageManager.get("serverside.command.help.open"));
        sender.sendMessage(languageManager.get("serverside.command.help.table_learn"));
        // 移除 resourcepack 帮助信息
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("projecte.command.reload")) {
            sender.sendMessage(languageManager.get("serverside.command.no_permission"));
            return;
        }

        plugin.reloadPlugin();
        sender.sendMessage(languageManager.get("serverside.command.reload_success"));
    }

    private void handleSetEmc(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(languageManager.get("serverside.command.player_only"));
            return;
        }

        if (!sender.hasPermission("projecte.command.setemc")) {
            sender.sendMessage(languageManager.get("serverside.command.no_permission"));
            return;
        }

        if (args.length != 2) {
            sender.sendMessage(languageManager.get("serverside.command.set_emc.usage"));
            return;
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand.getType().isAir()) {
            sender.sendMessage(languageManager.get("serverside.command.set_emc.hold_item"));
            return;
        }

        try {
            long emc = Long.parseLong(args[1]);
            if (emc <= 0) {
                sender.sendMessage(languageManager.get("serverside.command.set_emc.must_be_positive"));
                return;
            }

            String itemKey = emcManager.getItemKey(itemInHand);
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
    }

    private void handleDebug(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(languageManager.get("serverside.command.player_only"));
            return;
        }

        if (!sender.hasPermission("projecte.command.debug")) {
            sender.sendMessage(languageManager.get("serverside.command.no_permission"));
            return;
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand.getType().isAir()) {
            sender.sendMessage(languageManager.get("serverside.command.set_emc.hold_item"));
            return;
        }

        String itemKey = plugin.getVersionAdapter().getItemKey(itemInHand);
        long emc = emcManager.getEmc(itemKey);
        boolean learned = databaseManager.isLearned(player.getUniqueId(), itemKey);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("item", itemKey);
        sender.sendMessage(languageManager.get("serverside.command.debug.header", placeholders));

        placeholders.put("emc", String.valueOf(emc));
        sender.sendMessage(languageManager.get("serverside.command.debug.emc_value", placeholders));

        placeholders.put("learned", learned ? "Yes" : "No");
        sender.sendMessage(languageManager.get("serverside.command.debug.is_learned", placeholders));

        List<Recipe> recipes = Bukkit.getRecipesFor(itemInHand);
        if (recipes.isEmpty()) {
            sender.sendMessage(languageManager.get("serverside.command.debug.no_recipe"));
        } else {
            placeholders.put("count", String.valueOf(recipes.size()));
            sender.sendMessage(languageManager.get("serverside.command.debug.recipe_found", placeholders));
            for (int i = 0; i < recipes.size(); i++) {
                Recipe recipe = recipes.get(i);
                Map<String, String> recipePlaceholders = new HashMap<>();
                recipePlaceholders.put("index", String.valueOf(i + 1));
                sender.sendMessage(languageManager.get("serverside.command.debug.recipe_header", recipePlaceholders));
                String divisionStrategy = plugin.getConfig()
                        .getString("gui.EMC.divisionStrategy", "floor")
                        .toLowerCase();
                List<String> debugInfo = plugin.getVersionAdapter().getRecipeDebugInfo(recipe, divisionStrategy);
                for (String line : debugInfo) {
                    sender.sendMessage(ChatColor.GRAY + "  " + line);
                }
            }
        }

        sender.sendMessage(languageManager.get("serverside.command.debug.footer"));
    }

    private void handlePayEmc(CommandSender sender, String[] args) {
        if (!(sender instanceof Player senderPlayer)) {
            sender.sendMessage(languageManager.get("serverside.command.player_only"));
            return;
        }

        if (!sender.hasPermission("projecte.command.pay")) {
            sender.sendMessage(languageManager.get("serverside.command.no_permission"));
            return;
        }

        if (args.length != 3) {
            sender.sendMessage(languageManager.get("serverside.command.pay_emc.usage"));
            return;
        }

        Player targetPlayer = Bukkit.getPlayer(args[1]);

        if (targetPlayer == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", args[1]);
            sender.sendMessage(languageManager.get("serverside.command.pay_emc.player_not_found", placeholders));
            return;
        }

        if (targetPlayer.equals(senderPlayer)) {
            sender.sendMessage(languageManager.get("serverside.command.pay_emc.cant_pay_self"));
            return;
        }

        long amount;
        try {
            amount = Long.parseLong(args[2]);
            if (amount <= 0) {
                sender.sendMessage(languageManager.get("serverside.command.pay_emc.must_be_positive"));
                return;
            }
        } catch (NumberFormatException e) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("amount", args[2]);
            sender.sendMessage(languageManager.get("serverside.command.pay_emc.invalid_amount", placeholders));
            return;
        }

        double feePercentage = plugin.getConfig().getDouble("gui.transfer-fee-percentage", 0.0);
        long fee = (long) (amount * (feePercentage / 100.0));
        long totalDeduction = amount + fee;

        long senderEmc = databaseManager.getPlayerEmc(senderPlayer.getUniqueId());

        if (senderEmc < totalDeduction) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("total", String.valueOf(totalDeduction));
            placeholders.put("fee", String.valueOf(fee));
            sender.sendMessage(languageManager.get("serverside.command.pay_emc.not_enough_emc", placeholders));
            return;
        }

        long targetEmc = databaseManager.getPlayerEmc(targetPlayer.getUniqueId());

        databaseManager.setPlayerEmc(senderPlayer.getUniqueId(), senderEmc - totalDeduction);
        databaseManager.setPlayerEmc(targetPlayer.getUniqueId(), targetEmc + amount);

        Map<String, String> senderPlaceholders = new HashMap<>();
        senderPlaceholders.put("player", targetPlayer.getName());
        senderPlaceholders.put("amount", String.valueOf(amount));
        senderPlaceholders.put("fee", String.valueOf(fee));
        senderPlayer.sendMessage(languageManager.get("serverside.command.pay_emc.pay_success", senderPlaceholders));

        Map<String, String> targetPlaceholders = new HashMap<>();
        targetPlaceholders.put("player", senderPlayer.getName());
        targetPlaceholders.put("amount", String.valueOf(amount));
        targetPlayer.sendMessage(languageManager.get("serverside.command.pay_emc.receive_success", targetPlaceholders));
    }

    private void handleGetItem(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(languageManager.get("serverside.command.player_only"));
            return;
        }

        if (!sender.hasPermission("projecte.command.item")) {
            sender.sendMessage(languageManager.get("serverside.command.no_permission"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(languageManager.get("serverside.command.item.usage"));
            return;
        }

        String itemId = args[1];
        ItemStack item = plugin.getItemStackFromKey(itemId);

        if (item == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("item", itemId);
            sender.sendMessage(languageManager.get("serverside.command.item.not_found", placeholders));
            return;
        }

        int amount = 1;
        if (args.length > 2) {
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(languageManager.get("serverside.command.item.invalid_amount"));
                return;
            }
        }

        item.setAmount(amount);

        player.getInventory().addItem(item);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("item", itemId);
        placeholders.put("amount", String.valueOf(amount));
        sender.sendMessage(languageManager.get("serverside.command.item.success", placeholders));
    }

    private void handleGiveItem(CommandSender sender, String[] args) {
        if (!sender.hasPermission("projecte.command.give")) {
            sender.sendMessage(languageManager.get("serverside.command.no_permission"));
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(languageManager.get("serverside.command.give.usage"));
            return;
        }

        Player targetPlayer = Bukkit.getPlayer(args[1]);
        if (targetPlayer == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", args[1]);
            sender.sendMessage(languageManager.get("serverside.command.give.player_not_found", placeholders));
            return;
        }

        String itemId = args[2];
        ItemStack item = plugin.getItemStackFromKey(itemId);

        if (item == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("item", itemId);
            sender.sendMessage(languageManager.get("serverside.command.give.item_not_found", placeholders));
            return;
        }

        int amount = 1;
        if (args.length > 3) {
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(languageManager.get("serverside.command.give.invalid_amount"));
                return;
            }
        }

        item.setAmount(amount);

        targetPlayer.getInventory().addItem(item);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", targetPlayer.getName());
        placeholders.put("item", itemId);
        placeholders.put("amount", String.valueOf(amount));
        sender.sendMessage(languageManager.get("serverside.command.give.success", placeholders));
    }

    private void handleNoEmcItem(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(languageManager.get("serverside.command.player_only"));
            return;
        }

        if (!sender.hasPermission("projecte.command.noemcitem")) {
            sender.sendMessage(languageManager.get("serverside.command.no_permission"));
            return;
        }

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
            return;
        }

        new NoEmcItemGUI(noEmcItems, 0).openInventory(player);
    }

    private void handleBag(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(languageManager.get("serverside.command.player_only"));
            return;
        }

        if (!sender.hasPermission("projecte.command.bag")) {
            sender.sendMessage(languageManager.get("serverside.command.no_permission"));
            return;
        }

        if (args.length < 2 || !args[1].equalsIgnoreCase("list")) {
            sender.sendMessage(languageManager.get("serverside.command.bag.usage"));
            return;
        }

        List<String> bagColors = databaseManager.getBagColors(player.getUniqueId());

        if (bagColors.isEmpty()) {
            sender.sendMessage(languageManager.get("serverside.command.bag.no_bags"));
            return;
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
    }

    private void handleLang(CommandSender sender, String[] args) {
        if (!sender.hasPermission("projecte.command.lang")) {
            sender.sendMessage(languageManager.get("serverside.command.no_permission"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(languageManager.get("serverside.command.lang.usage"));
            return;
        }

        String action = args[1].toLowerCase();
        if (action.equals("list")) {
            sender.sendMessage(languageManager.get("serverside.command.lang.list_header"));
            File langFolder = new File(plugin.getDataFolder(), "lang");
            if (!langFolder.exists() || !langFolder.isDirectory()) {
                sender.sendMessage(languageManager.get("serverside.command.lang.no_lang_folder"));
                return;
            }
            File[] files = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
                for (File file : files) {
                    sender.sendMessage(ChatColor.YELLOW + "- " + file.getName().replace(".yml", ""));
                }
            }
            return;
        }

        if (action.equals("set")) {
            if (args.length < 3) {
                sender.sendMessage(languageManager.get("serverside.command.lang.usage"));
                return;
            }
            List<String> newLangs = new ArrayList<>(Arrays.asList(args).subList(2, args.length));
            for (String lang : newLangs) {
                File langFile = new File(new File(plugin.getDataFolder(), "lang"), lang + ".yml");
                if (!langFile.exists()) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("file", lang + ".yml");
                    sender.sendMessage(languageManager.get("serverside.command.lang.file_not_found", placeholders));
                    return;
                }
            }

            plugin.getConfig().set("language", newLangs);
            plugin.saveConfig();
            languageManager.loadLanguageFiles(); // 立即重新加载语言文件
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("languages", String.join(", ", newLangs));
            sender.sendMessage(languageManager.get("serverside.command.lang.set_success", placeholders));
            return;
        }

        sender.sendMessage(languageManager.get("serverside.command.lang.usage"));
    }

    private void handleReport(CommandSender sender) {
        sender.sendMessage(languageManager.get("serverside.command.report.message"));
    }

    private void handleNbtDebug(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be executed by a player.");
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            sender.sendMessage(ChatColor.YELLOW + "Please hold an item in your hand to execute this command.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.AQUA).append("[NBT Debug]").append("\n");
        sb.append(ChatColor.GRAY).append("Type: ").append(item.getType().name()).append("\n");

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            sb.append(ChatColor.GRAY)
                    .append("DisplayName: ")
                    .append(meta.hasDisplayName() ? meta.getDisplayName() : "None")
                    .append("\n");
            sb.append(ChatColor.GRAY)
                    .append("Lore: ")
                    .append(meta.hasLore() ? meta.getLore() : "None")
                    .append("\n");

            if (meta.hasCustomModelData()) {
                try {
                    sb.append(ChatColor.GRAY)
                            .append("CustomModelData: ")
                            .append(meta.getCustomModelData())
                            .append("\n");
                } catch (IllegalStateException e) {
                    sb.append(ChatColor.GRAY)
                            .append("CustomModelData: Exists but cannot be read (")
                            .append(e.getMessage())
                            .append(")\n");
                }
            } else {
                sb.append(ChatColor.GRAY).append("CustomModelData: None\n");
            }

            sb.append(ChatColor.GRAY).append("PDC: ");
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            var keys = pdc.getKeys();
            if (keys.isEmpty()) {
                sb.append("None\n");
            } else {
                sb.append("\n");

                for (var key : keys) {
                    sb.append("  - ").append(key).append(": ");

                    boolean matched = false;
                    for (var entry : Constants.PDC_TYPES.entrySet()) {
                        if (tryAppendPdcValue(sb, pdc, key, entry.getKey(), entry.getValue())) {
                            matched = true;
                            break;
                        }
                    }

                    if (!matched) {
                        sb.append("Exists (unknown or complex type)");
                    }

                    sb.append("\n");
                }
            }
        } else {
            sb.append(ChatColor.GRAY).append("No ItemMeta\n");
        }

        player.sendMessage(sb.toString());
    }

    private <T, Z> boolean tryAppendPdcValue(
            StringBuilder sb,
            PersistentDataContainer pdc,
            NamespacedKey key,
            PersistentDataType<T, Z> type,
            String typeName) {
        if (pdc.has(key, type)) {
            try {
                Z value = pdc.get(key, type);
                sb.append(value).append(" (").append(typeName).append(")");
                return true;
            } catch (Exception e) {
                sb.append("Error reading (").append(typeName).append("): ").append(e.getMessage());
                return true;
            }
        }
        return false;
    }

    private void handleOpen(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(languageManager.get("serverside.command.player_only"));
            return;
        }

        try {
            Class.forName("org.geysermc.geyser.api.GeyserApi");
            if (!GeyserApi.api().isBedrockPlayer(player.getUniqueId())) {
                player.sendMessage(languageManager.get("serverside.command.geyser_only"));
                return;
            }
        } catch (ClassNotFoundException e) {
            player.sendMessage(languageManager.get("serverside.command.geyser_only"));
            return;
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (plugin.isPhilosopherStone(itemInHand)) {
            new PhilosopherStoneGUI(plugin, player).open();
            return;
        }

        DiviningRod diviningRod = plugin.getDiviningRod();
        if (diviningRod.isLowDiviningRod(itemInHand)
                || diviningRod.isMediumDiviningRod(itemInHand)
                || diviningRod.isHighDiviningRod(itemInHand)) {
            plugin.getDiviningRodGUI().openGUI(player);
            return;
        }

        player.sendMessage(languageManager.get("serverside.command.open.no_menu"));
    }

    private void handleTableCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(languageManager.get("serverside.command.player_only"));
            return;
        }

        if (args.length < 2 || !args[1].equalsIgnoreCase("learn")) {
            sender.sendMessage(languageManager.get("serverside.command.table.learn.usage"));
            return;
        }

        if (!sender.hasPermission("projecte.command.table.learn")) {
            sender.sendMessage(languageManager.get("serverside.command.no_permission"));
            return;
        }

        plugin.getSchedulerAdapter().runTaskAsynchronously(() -> {
            int learnedCount = 0;
            for (Material material : Material.values()) {
                if (material.isItem() && !material.isAir()) {
                    ItemStack item = new ItemStack(material);
                    String itemKey = plugin.getVersionAdapter().getItemKey(item);
                    if (emcManager.getEmc(itemKey) > 0) {
                        if (databaseManager.learnItem(player.getUniqueId(), itemKey)) {
                            learnedCount++;
                        }
                    }
                }
            }
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("count", String.valueOf(learnedCount));
            player.sendMessage(languageManager.get("serverside.command.table.learn.success", placeholders));
        });
    }

    private void handleGemHelmet(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(languageManager.get("serverside.command.player_only"));
            return;
        }

        if (plugin.getArmorManager().isGemHelmet(player.getInventory().getHelmet())) {
            GemHelmetGUI.open(player);
        } else {
            player.sendMessage(languageManager.get("serverside.command.gem_helmet.not_wearing"));
        }
    }

    private void handleGemBoots(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(languageManager.get("serverside.command.player_only"));
            return;
        }

        if (plugin.getArmorManager().isGemBoots(player.getInventory().getBoots())) {
            player.sendMessage(languageManager.get("serverside.command.gem_boots.disabled_for_folia"));
        } else {
            player.sendMessage(languageManager.get("serverside.command.gem_boots.not_wearing"));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("projecte")) {
            if (args.length == 1) {
                List<String> subCommands = getSubCommands();
                return StringUtil.copyPartialMatches(args[0], subCommands, new ArrayList<>());
            }
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("pay") || args[0].equalsIgnoreCase("give")) {
                List<String> playerNames = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    playerNames.add(player.getName());
                }
                return StringUtil.copyPartialMatches(args[1], playerNames, new ArrayList<>());
            }

            if (args[0].equalsIgnoreCase("item")) {
                List<String> itemIds = new ArrayList<>(plugin.getRecipeManager().getRegisteredItemIds());
                itemIds.addAll(Arrays.asList(
                        "dark_matter_helmet",
                        "dark_matter_chestplate",
                        "dark_matter_leggings",
                        "dark_matter_boots",
                        "red_matter_helmet",
                        "red_matter_chestplate",
                        "red_matter_leggings",
                        "red_matter_boots"));
                return StringUtil.copyPartialMatches(args[1], itemIds, new ArrayList<>());
            }

            if (args[0].equalsIgnoreCase("bag")) {
                return StringUtil.copyPartialMatches(args[1], Collections.singletonList("list"), new ArrayList<>());
            }

            if (args[0].equalsIgnoreCase("lang")) {
                return StringUtil.copyPartialMatches(args[1], Arrays.asList("list", "set"), new ArrayList<>());
            }

            if (args[0].equalsIgnoreCase("gui")) {
                File dataFolder = plugin.getDataFolder();
                File[] files =
                        dataFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
                List<String> guiFiles = new ArrayList<>();
                if (files != null) {
                    for (File file : files) {
                        guiFiles.add(file.getName());
                    }
                }
                return StringUtil.copyPartialMatches(args[1], guiFiles, new ArrayList<>());
            }
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("table")) {
            return StringUtil.copyPartialMatches(args[1], Collections.singletonList("learn"), new ArrayList<>());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            List<String> itemIds = new ArrayList<>(plugin.getRecipeManager().getRegisteredItemIds());
            itemIds.addAll(Arrays.asList(
                    "dark_matter_helmet",
                    "dark_matter_chestplate",
                    "dark_matter_leggings",
                    "dark_matter_boots",
                    "red_matter_helmet",
                    "red_matter_chestplate",
                    "red_matter_leggings",
                    "red_matter_boots"));
            itemIds.addAll(plugin.getToolManager().getToolIds());
            return StringUtil.copyPartialMatches(args[2], itemIds, new ArrayList<>());
        }

        if (args.length >= 3 && args[0].equalsIgnoreCase("lang") && args[1].equalsIgnoreCase("set")) {
            List<String> langFiles = new ArrayList<>();
            File langFolder = new File(plugin.getDataFolder(), "lang");
            if (langFolder.exists() && langFolder.isDirectory()) {
                File[] files = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));
                if (files != null) {
                    for (File file : files) {
                        langFiles.add(file.getName().replace(".yml", ""));
                    }
                }
            }
            return StringUtil.copyPartialMatches(args[args.length - 1], langFiles, new ArrayList<>());
        }

        return new ArrayList<>();
    }

    private List<String> getSubCommands() {
        return new ArrayList<>(Arrays.asList(
                "recalculate",
                "reload",
                "setemc",
                "debug",
                "pay",
                "item",
                "give",
                "noemcitem",
                "bag",
                "lang",
                "report",
                "nbtdebug",
                "open",
                "o",
                "gui",
                "table",
                "gemhelmet",
                "gemboots"));
    }
}
