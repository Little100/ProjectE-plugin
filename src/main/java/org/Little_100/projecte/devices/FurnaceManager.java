package org.Little_100.projecte.devices;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.util.Constants;
import org.Little_100.projecte.util.CustomModelDataUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class FurnaceManager {

    private static final Set<Material> ORES = new HashSet<>(Arrays.asList(
            Material.COAL_ORE,
            Material.COPPER_ORE,
            Material.IRON_ORE,
            Material.GOLD_ORE,
            Material.LAPIS_ORE,
            Material.REDSTONE_ORE,
            Material.DIAMOND_ORE,
            Material.EMERALD_ORE,
            Material.NETHER_GOLD_ORE,
            Material.ANCIENT_DEBRIS,
            Material.DEEPSLATE_COAL_ORE,
            Material.DEEPSLATE_COPPER_ORE,
            Material.DEEPSLATE_IRON_ORE,
            Material.DEEPSLATE_GOLD_ORE,
            Material.DEEPSLATE_LAPIS_ORE,
            Material.DEEPSLATE_REDSTONE_ORE,
            Material.DEEPSLATE_DIAMOND_ORE,
            Material.DEEPSLATE_EMERALD_ORE,
            Material.RAW_IRON,
            Material.RAW_GOLD,
            Material.RAW_COPPER));

    private final ProjectE plugin;
    private final Map<Location, FurnaceState> activeFurnaces = new ConcurrentHashMap<>();

    private final Map<FurnaceType, ItemStack[]> guiLayouts = new EnumMap<>(FurnaceType.class);
    private final Map<FurnaceType, List<Integer>> inputSlots = new EnumMap<>(FurnaceType.class);
    private final Map<FurnaceType, List<Integer>> outputSlots = new EnumMap<>(FurnaceType.class);
    private final Map<FurnaceType, List<Integer>> progressBarSlots = new EnumMap<>(FurnaceType.class);
    private final Map<FurnaceType, Integer> fuelSlot = new EnumMap<>(FurnaceType.class);
    private final Map<FurnaceType, Integer> arrowSlot = new EnumMap<>(FurnaceType.class);
    private final Map<FurnaceType, Integer> fuelIndicatorSlot = new EnumMap<>(FurnaceType.class);
    private final Map<FurnaceType, List<Integer>> nonInteractiveSlots = new EnumMap<>(FurnaceType.class);

    public FurnaceManager(ProjectE plugin) {
        this.plugin = plugin;
        loadGuiLayouts();
        startUpdateTask();
    }

    public static class FurnaceState {
        private final UUID owner;
        private final Inventory inventory;
        private final FurnaceType type;
        private UUID armorStandId;
        private int fuelTicksLeft = 0;
        private int cookTimeProgress = 0;
        private boolean wasBurning = false;

        public FurnaceState(UUID owner, Inventory inventory, FurnaceType type, UUID armorStandId) {
            this.owner = owner;
            this.inventory = inventory;
            this.type = type;
            this.armorStandId = armorStandId;
        }

        public UUID getOwner() {
            return owner;
        }

        public Inventory getInventory() {
            return inventory;
        }

        public FurnaceType getType() {
            return type;
        }

        public UUID getArmorStandId() {
            return armorStandId;
        }

        public void setArmorStandId(UUID armorStandId) {
            this.armorStandId = armorStandId;
        }

        public int getFuelTicksLeft() {
            return fuelTicksLeft;
        }

        public void setFuelTicksLeft(int fuelTicksLeft) {
            this.fuelTicksLeft = fuelTicksLeft;
        }

        public int getCookTimeProgress() {
            return cookTimeProgress;
        }

        public void setCookTimeProgress(int cookTimeProgress) {
            this.cookTimeProgress = cookTimeProgress;
        }

        public boolean wasBurning() {
            return wasBurning;
        }

        public void setWasBurning(boolean wasBurning) {
            this.wasBurning = wasBurning;
        }
    }

    public enum FurnaceType {
        DARK_MATTER,
        RED_MATTER
    }

    private void loadGuiLayouts() {
        for (FurnaceType type : FurnaceType.values()) {
            String fileName = type.name().toLowerCase().replace("_", "") + "furnace.yml";
            try (java.io.InputStream is = plugin.getResource(fileName)) {
                if (is == null) {
                    plugin.getLogger().severe("GUI layout file not found in JAR: " + fileName);
                    continue;
                }
                YamlConfiguration config = YamlConfiguration.loadConfiguration(new java.io.InputStreamReader(is));
                int size = config.getInt("size", 54);
                ItemStack[] layout = new ItemStack[size];
                List<Integer> currentInput = new ArrayList<>();
                List<Integer> currentOutput = new ArrayList<>();
                List<Integer> currentProgressBar = new ArrayList<>();
                List<Integer> currentNonInteractive = new ArrayList<>();

                ConfigurationSection itemsSection = config.getConfigurationSection("items");
                if (itemsSection != null) {
                    for (String key : itemsSection.getKeys(false)) {
                        int slot = Integer.parseInt(key);
                        String materialName = itemsSection.getString(key + ".material");
                        Material material = Material.getMaterial(materialName);
                        if (material == null) continue;

                        ItemStack item = new ItemStack(material);
                        ItemMeta meta = item.getItemMeta();
                        String name = itemsSection.getString(key + ".name");

                        if ("null".equalsIgnoreCase(name)) {
                            if (meta != null) meta.setDisplayName(" ");
                            currentNonInteractive.add(slot);
                        } else if (name != null) {
                            if (meta != null) meta.setDisplayName(name);
                        }
                        if (meta != null) item.setItemMeta(meta);
                        layout[slot] = item;

                        if (material == Material.IRON_ORE) currentInput.add(slot);
                        else if (material == Material.IRON_INGOT) currentOutput.add(slot);
                        else if (material == Material.COAL) fuelSlot.put(type, slot);
                        else if (material == Material.ARROW) arrowSlot.put(type, slot);
                        else if (material == Material.CAMPFIRE) fuelIndicatorSlot.put(type, slot);
                        else if (material == Material.BLACK_STAINED_GLASS_PANE && slot < 9) {
                            currentProgressBar.add(slot);
                        }
                    }
                }
                guiLayouts.put(type, layout);
                inputSlots.put(type, currentInput);
                outputSlots.put(type, currentOutput);
                progressBarSlots.put(type, currentProgressBar);
                nonInteractiveSlots.put(type, currentNonInteractive);
            } catch (java.io.IOException e) {
                plugin.getLogger().log(java.util.logging.Level.SEVERE, "Could not load GUI layout: " + fileName, e);
            }
        }
    }

    private void startUpdateTask() {
        plugin.getSchedulerAdapter().runTimer(() -> activeFurnaces.forEach(this::tick), 0L, 1L);
    }

    public void addFurnace(Location location, UUID owner, FurnaceType type, UUID armorStandId) {
        String titleKey =
                (type == FurnaceType.DARK_MATTER) ? "gui.furnace.dark_matter_title" : "gui.furnace.red_matter_title";
        String title = plugin.getLanguageManager().get(titleKey);
        Inventory inventory = Bukkit.createInventory(null, 54, title);
        ItemStack[] layout = guiLayouts.get(type);
        if (layout != null) {
            for (int i = 0; i < layout.length; i++) {
                if (!inputSlots.get(type).contains(i)
                        && !outputSlots.get(type).contains(i)
                        && !fuelSlot.get(type).equals(i)) {
                    if (layout[i] != null) {
                        inventory.setItem(i, layout[i].clone());
                    }
                }
            }
        }
        FurnaceState state = new FurnaceState(owner, inventory, type, armorStandId);
        activeFurnaces.put(location, state);
    }

    public void removeFurnace(Location location) {
        FurnaceState state = activeFurnaces.remove(location);
        if (state != null) {
            plugin.getSchedulerAdapter().runTaskAt(location, () -> {
                Entity armorStand = Bukkit.getEntity(state.getArmorStandId());
                if (armorStand != null) {
                    armorStand.remove();
                }
            });
        }
    }

    public FurnaceState getFurnaceState(Location location) {
        return activeFurnaces.get(location);
    }

    public Map<Location, FurnaceState> getActiveFurnaces() {
        return activeFurnaces;
    }

    public boolean isFurnace(Location location) {
        return activeFurnaces.containsKey(location);
    }

    public boolean isNonInteractive(FurnaceType type, int slot) {
        return nonInteractiveSlots.getOrDefault(type, Collections.emptyList()).contains(slot);
    }

    public boolean isOutputSlot(FurnaceType type, int slot) {
        return outputSlots.getOrDefault(type, Collections.emptyList()).contains(slot);
    }

    public boolean isArrowSlot(FurnaceType type, int slot) {
        return this.arrowSlot.get(type) != null && this.arrowSlot.get(type) == slot;
    }

    public boolean isFuelIndicatorSlot(FurnaceType type, int slot) {
        return this.fuelIndicatorSlot.get(type) != null && this.fuelIndicatorSlot.get(type) == slot;
    }

    private void tick(Location location, FurnaceState state) {
        Inventory inv = state.getInventory();
        FurnaceType type = state.getType();
        boolean isBurning = state.getFuelTicksLeft() > 0;

        if (state.getFuelTicksLeft() > 0) {
            state.setFuelTicksLeft(state.getFuelTicksLeft() - 1);
        }

        ItemStack firstSmeltable = findNextInput(inv, type);

        if (isBurning) {
            if (firstSmeltable != null) {
                ItemStack result = getSmeltingResult(firstSmeltable);
                if (result != null) {
                    int maxPossibleAmount = 1;
                    if (isOre(firstSmeltable.getType())) {
                        if (type == FurnaceType.DARK_MATTER || type == FurnaceType.RED_MATTER) {
                            maxPossibleAmount = 2;
                        }
                    }

                    if (canFitOutput(inv, type, result, maxPossibleAmount)) {
                        int cookTimeNeeded = type == FurnaceType.DARK_MATTER ? 8 : 2;
                        state.setCookTimeProgress(state.getCookTimeProgress() + 1);

                        if (state.getCookTimeProgress() >= cookTimeNeeded) {
                            int finalAmount = 1;
                            if (isOre(firstSmeltable.getType())) {
                                if (type == FurnaceType.RED_MATTER) {
                                    finalAmount = 2;
                                } else if (type == FurnaceType.DARK_MATTER) {
                                    if (Math.random() < 0.5) {
                                        finalAmount = 2;
                                    }
                                }
                            }
                            smeltItem(inv, type, firstSmeltable, result, finalAmount);
                            state.setCookTimeProgress(0);
                        }
                    } else {
                        state.setCookTimeProgress(0);
                    }
                } else {
                    state.setCookTimeProgress(0);
                }
            } else {
                state.setCookTimeProgress(0);
            }
        } else {
            if (firstSmeltable != null) {
                Integer fuelSlotIndex = fuelSlot.get(type);
                if (fuelSlotIndex != null) {
                    ItemStack fuel = inv.getItem(fuelSlotIndex);
                    int fuelTime = getFuelTime(fuel);
                    if (fuelTime > 0) {
                        state.setFuelTicksLeft(fuelTime);
                        if (fuel.getAmount() > 1) fuel.setAmount(fuel.getAmount() - 1);
                        else inv.setItem(fuelSlotIndex, null);
                        isBurning = true;
                    }
                }
            }
            state.setCookTimeProgress(0);
        }

        updateProgressBar(inv, state);
        updateFuelDisplay(inv, state);

        if (isBurning != state.wasBurning()) {
            updateFurnaceModel(location, state, isBurning);
            state.setWasBurning(isBurning);
        }
    }

    private ItemStack findNextInput(Inventory inv, FurnaceType type) {
        for (int slot : inputSlots.getOrDefault(type, Collections.emptyList())) {
            ItemStack item = inv.getItem(slot);
            if (item != null && getSmeltingResult(item) != null) {
                return item;
            }
        }
        return null;
    }

    private boolean canFitOutput(Inventory inv, FurnaceType type, ItemStack result, int amount) {
        int spaceAvailable = 0;
        for (int slot : outputSlots.getOrDefault(type, Collections.emptyList())) {
            ItemStack existing = inv.getItem(slot);
            if (existing == null) {
                spaceAvailable += result.getMaxStackSize();
            } else if (existing.isSimilar(result)) {
                spaceAvailable += existing.getMaxStackSize() - existing.getAmount();
            }
        }
        return spaceAvailable >= amount;
    }

    private void smeltItem(Inventory inv, FurnaceType type, ItemStack input, ItemStack result, int amount) {
        input.setAmount(input.getAmount() - 1);

        ItemStack toAdd = result.clone();
        toAdd.setAmount(amount);

        int amountLeft = toAdd.getAmount();

        for (int slot : outputSlots.getOrDefault(type, Collections.emptyList())) {
            if (amountLeft <= 0) break;
            ItemStack existing = inv.getItem(slot);
            if (existing != null && existing.isSimilar(toAdd)) {
                int space = existing.getMaxStackSize() - existing.getAmount();
                if (space > 0) {
                    int addAmount = Math.min(amountLeft, space);
                    existing.setAmount(existing.getAmount() + addAmount);
                    amountLeft -= addAmount;
                }
            }
        }

        for (int slot : outputSlots.getOrDefault(type, Collections.emptyList())) {
            if (amountLeft <= 0) break;
            if (inv.getItem(slot) == null) {
                ItemStack newItem = toAdd.clone();
                int addAmount = Math.min(amountLeft, newItem.getMaxStackSize());
                newItem.setAmount(addAmount);
                inv.setItem(slot, newItem);
                amountLeft -= addAmount;
            }
        }
    }

    private void updateProgressBar(Inventory inv, FurnaceState state) {
        FurnaceType type = state.getType();
        int cookTimeNeeded = type == FurnaceType.DARK_MATTER ? 8 : 2;
        float percentage = (cookTimeNeeded > 0) ? (float) state.getCookTimeProgress() / cookTimeNeeded : 0;

        List<Integer> pBarSlots = progressBarSlots.getOrDefault(type, Collections.emptyList());
        int greenPanes = (int) (pBarSlots.size() * percentage);

        ItemStack greenPane = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta greenMeta = greenPane.getItemMeta();
        if (greenMeta != null) {
            greenMeta.setDisplayName(" ");
            greenPane.setItemMeta(greenMeta);
        }

        for (int i = 0; i < pBarSlots.size(); i++) {
            int slot = pBarSlots.get(i);
            if (i < greenPanes) {
                inv.setItem(slot, greenPane);
            } else {
                inv.setItem(slot, guiLayouts.get(type)[slot].clone());
            }
        }

        Integer arrowSlotIndex = arrowSlot.get(type);
        if (arrowSlotIndex != null) {
            ItemStack arrow = inv.getItem(arrowSlotIndex);
            if (arrow != null && arrow.getType() == Material.ARROW) {
                ItemMeta meta = arrow.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName("§a" + (int) (percentage * 100) + "%");
                    arrow.setItemMeta(meta);
                }
            }
        }
    }

    private void updateFuelDisplay(Inventory inv, FurnaceState state) {
        Integer fuelIndicatorIndex = fuelIndicatorSlot.get(state.getType());
        if (fuelIndicatorIndex != null) {
            ItemStack fuelItem = inv.getItem(fuelIndicatorIndex);
            if (fuelItem != null && fuelItem.getType() == Material.CAMPFIRE) {
                ItemMeta meta = fuelItem.getItemMeta();
                if (meta != null) {
                    int seconds = state.getFuelTicksLeft() / 20;
                    String key = "gui.furnace.fuel_time";
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("time", String.valueOf(seconds));
                    meta.setDisplayName(plugin.getLanguageManager().get(key, placeholders));
                    fuelItem.setItemMeta(meta);
                }
            }
        }
    }

    private ItemStack getSmeltingResult(ItemStack input) {
        if (input == null) return null;
        Iterator<Recipe> iterator = Bukkit.recipeIterator();
        while (iterator.hasNext()) {
            Recipe recipe = iterator.next();
            if (recipe instanceof FurnaceRecipe) {
                FurnaceRecipe furnaceRecipe = (FurnaceRecipe) recipe;
                if (furnaceRecipe.getInput().isSimilar(input)) {
                    return furnaceRecipe.getResult();
                }
            }
        }
        return null;
    }

    private int getFuelTime(ItemStack item) {
        if (item == null) return 0;

        if (item.hasItemMeta()) {
            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
            if (container.has(Constants.ID_KEY, PersistentDataType.STRING)) {
                String projecteId = container.get(Constants.ID_KEY, PersistentDataType.STRING);
                switch (projecteId) {
                    case "alchemical_coal":
                        return 320 * 20;
                    case "mobius_fuel":
                        return 1280 * 20;
                    case "aeternalis_fuel":
                        return 5120 * 20;
                    case "alchemical_coal_block":
                        return 2880 * 20;
                    case "mobius_fuel_block":
                        return 11520 * 20;
                    case "aeternalis_fuel_block":
                        return 46080 * 20;
                    default:
                        break;
                }
            }
        }

        Material type = item.getType();
        if (type.isFuel()) {
            if (type == Material.COAL) return 1600;
            if (type == Material.COAL_BLOCK) return 16000;
            if (type == Material.LAVA_BUCKET) return 20000;
            return 300;
        }
        return 0;
    }

    private boolean isOre(Material material) {
        return ORES.contains(material);
    }

    private void updateFurnaceModel(Location location, FurnaceState state, boolean isBurning) {
        plugin.getSchedulerAdapter().runTaskAt(location, () -> {
            Entity entity = Bukkit.getEntity(state.getArmorStandId());
            if (!(entity instanceof ArmorStand)) return;
            ArmorStand armorStand = (ArmorStand) entity;
            ItemStack furnaceItem = armorStand.getEquipment().getHelmet();
            if (furnaceItem == null || !furnaceItem.hasItemMeta()) return;
            int baseModelData = state.getType() == FurnaceType.DARK_MATTER ? 1 : 2;
            int newModelData = isBurning ? baseModelData + 2 : baseModelData;

            int currentModelData = CustomModelDataUtil.getCustomModelDataInt(furnaceItem);

            if (newModelData != currentModelData) {
                CustomModelDataUtil.setCustomModelData(furnaceItem, newModelData);
                armorStand.getEquipment().setHelmet(furnaceItem);
            }
        });
    }

    private ArmorStand getArmorStandAt(Location location) {
        Entity entity = Bukkit.getEntity(activeFurnaces.get(location).getArmorStandId());
        if (entity instanceof ArmorStand) {
            return (ArmorStand) entity;
        }
        return null;
    }
}
