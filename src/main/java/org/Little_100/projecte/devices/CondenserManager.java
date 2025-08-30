package org.Little_100.projecte.devices;

import org.Little_100.projecte.ProjectE;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.InputStreamReader;
import java.util.*;

public class CondenserManager {

    private final ProjectE plugin;
    private final Map<Location, CondenserState> activeCondensers = new HashMap<>();

    private final Map<CondenserType, ItemStack[]> guiLayouts = new EnumMap<>(CondenserType.class);
    private final Map<CondenserType, List<Integer>> inputSlots = new EnumMap<>(CondenserType.class);
    private final Map<CondenserType, Integer> targetSlot = new EnumMap<>(CondenserType.class);
    private final Map<CondenserType, List<Integer>> outputSlots = new EnumMap<>(CondenserType.class);
    private final Map<CondenserType, Integer> emcDisplaySlot = new EnumMap<>(CondenserType.class);
    private final Map<CondenserType, List<Integer>> progressBarSlots = new EnumMap<>(CondenserType.class);
    private final Map<CondenserType, List<Integer>> nonInteractiveSlots = new EnumMap<>(CondenserType.class);
    private long tickCounter = 0;

    public enum CondenserType {
        ENERGY_CONDENSER,
        ENERGY_CONDENSER_MK2
    }

    public static class CondenserState {
        private final UUID owner;
        private final Inventory inventory;
        private final CondenserType type;
        private final UUID armorStandId;
        private long storedEmc = 0;
        private int progress = 0;

        public CondenserState(UUID owner, Inventory inventory, CondenserType type, UUID armorStandId) {
            this.owner = owner;
            this.inventory = inventory;
            this.type = type;
            this.armorStandId = armorStandId;
        }

        public Inventory getInventory() { return inventory; }
        public CondenserType getType() { return type; }
        public long getStoredEmc() { return storedEmc; }
        public void setStoredEmc(long storedEmc) { this.storedEmc = storedEmc; }
        public void addEmc(long amount) { this.storedEmc += amount; }
        public void removeEmc(long amount) { this.storedEmc -= amount; }
        public int getProgress() { return progress; }
        public void setProgress(int progress) { this.progress = progress; }
    }

    public CondenserManager(ProjectE plugin) {
        this.plugin = plugin;
        loadGuiLayouts();
        startUpdateTask();
    }

    private void loadGuiLayouts() {
        for (CondenserType type : CondenserType.values()) {
            String fileName = (type == CondenserType.ENERGY_CONDENSER) ? "condenser.yml" : "condenser_mk2.yml";
            try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(plugin.getResource(fileName)))) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(reader);
                int size = config.getInt("size", 54);
                if (size > 54 || size % 9 != 0) {
                    plugin.getLogger().warning("Invalid size " + size + " in " + fileName + ". Defaulting to 54.");
                    size = 54;
                }
                ItemStack[] layout = new ItemStack[size];
                List<Integer> currentInput = new ArrayList<>();
                List<Integer> currentOutput = new ArrayList<>();
                List<Integer> currentProgressBar = new ArrayList<>();
                List<Integer> currentNonInteractive = new ArrayList<>();
                Set<Integer> definedSlots = new HashSet<>();

                ConfigurationSection itemsSection = config.getConfigurationSection("items");
                if (itemsSection != null) {
                    for (String key : itemsSection.getKeys(false)) {
                        int slot = Integer.parseInt(key);
                        definedSlots.add(slot);
                        String materialName = itemsSection.getString(key + ".material");
                        Material material = Material.getMaterial(materialName);
                        if (material == null) continue;

                        ItemStack item = new ItemStack(material);
                        ItemMeta meta = item.getItemMeta();
                        String name = itemsSection.getString(key + ".name");

                        if (meta != null) {
                            if ("null".equalsIgnoreCase(name)) {
                                meta.setDisplayName(" ");
                            } else if (name != null) {
                                meta.setDisplayName(name);
                            }
                            item.setItemMeta(meta);
                        }
                        layout[slot] = item;

                        if ("targetitem".equalsIgnoreCase(name)) {
                            targetSlot.put(type, slot);
                        } else if (material == Material.ARROW) {
                            emcDisplaySlot.put(type, slot);
                            currentNonInteractive.add(slot);
                        } else if ("progressbar".equalsIgnoreCase(name)) {
                            currentProgressBar.add(slot);
                            currentNonInteractive.add(slot);
                        } else if ("null".equalsIgnoreCase(name)) {
                            currentNonInteractive.add(slot);
                        }
                    }
                }

                for (int i = 0; i < size; i++) {
                    if (!definedSlots.contains(i)) {
                        currentInput.add(i);
                        currentOutput.add(i);
                    }
                }

                guiLayouts.put(type, layout);
                inputSlots.put(type, currentInput);
                outputSlots.put(type, currentOutput);
                progressBarSlots.put(type, currentProgressBar);
                nonInteractiveSlots.put(type, currentNonInteractive);

            } catch (Exception e) {
                plugin.getLogger().severe("Could not load GUI layout: " + fileName);
                e.printStackTrace();
            }
        }
    }

    private void startUpdateTask() {
        plugin.getSchedulerAdapter().runTimer(() -> {
            tickCounter++;
            activeCondensers.forEach((location, state) -> {
                if (state.getType() == CondenserType.ENERGY_CONDENSER) {
                    if (tickCounter % 2 == 0) {
                        tick(location, state);
                    }
                } else if (state.getType() == CondenserType.ENERGY_CONDENSER_MK2) {
                    tick(location, state);
                }
            });
        }, 0L, 1L);
    }

    public void addCondenser(Location location, UUID owner, CondenserType type, UUID armorStandId) {
        String titleKey = (type == CondenserType.ENERGY_CONDENSER) ? "gui.condenser.title" : "gui.condenser_mk2.title";
        String title = plugin.getLanguageManager().get(titleKey);
        int size = 54;
        Inventory inventory = Bukkit.createInventory(null, size, title);

        ItemStack[] layout = guiLayouts.get(type);
        if (layout != null) {
            ItemStack[] finalLayout = new ItemStack[layout.length];
            for (int i = 0; i < layout.length; i++) {
                if (layout[i] != null && layout[i].getType() == Material.BARRIER) {
                    finalLayout[i] = null;
                } else {
                    finalLayout[i] = layout[i];
                }
            }
            inventory.setContents(finalLayout);
        }

        CondenserState state = new CondenserState(owner, inventory, type, armorStandId);
        activeCondensers.put(location, state);
    }

    public void removeCondenser(Location location) {
        activeCondensers.remove(location);
    }

    public CondenserState getCondenserState(Location location) {
        return activeCondensers.get(location);
    }

    public Map<Location, CondenserState> getActiveCondensers() {
        return activeCondensers;
    }

    public boolean isCondenser(Location location) {
        return activeCondensers.containsKey(location);
    }

    public void openCondenserGUI(Player player, Location location) {
        CondenserState state = getCondenserState(location);
        if (state != null) {
            player.openInventory(state.getInventory());
        }
    }

    public boolean isNonInteractive(CondenserType type, int slot) {
        return nonInteractiveSlots.getOrDefault(type, Collections.emptyList()).contains(slot);
    }

    public boolean isTargetSlot(CondenserType type, int slot) {
        return targetSlot.get(type) != null && targetSlot.get(type) == slot;
    }

    private void tick(Location location, CondenserState state) {
        Inventory inv = state.getInventory();
        CondenserType type = state.getType();
        Integer targetSlotIndex = targetSlot.get(type);
        if (targetSlotIndex == null) return;

        ItemStack targetItem = inv.getItem(targetSlotIndex);

        // Consume input items
        int itemsToProcess = (type == CondenserType.ENERGY_CONDENSER) ? 1 : 6;
        int itemsProcessed = 0;
        for (int slot : inputSlots.getOrDefault(type, Collections.emptyList())) {
            if (itemsProcessed >= itemsToProcess) break;
            ItemStack item = inv.getItem(slot);
            if (item != null && (targetItem == null || !item.isSimilar(targetItem))) {
                long emc = plugin.getEmcManager().getEmc(item);
                if (emc > 0) {
                    state.addEmc(emc * item.getAmount());
                    inv.setItem(slot, null);
                    itemsProcessed += item.getAmount();
                } else {
                    inv.setItem(slot, null);
                    for (HumanEntity viewer : new ArrayList<>(inv.getViewers())) {
                        if (viewer instanceof Player) {
                            Player player = (Player) viewer;
                            HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(item);
                            if (!remaining.isEmpty()) {
                                for (ItemStack remainingItem : remaining.values()) {
                                    location.getWorld().dropItemNaturally(location, remainingItem);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (targetItem != null && targetItem.getAmount() > 0) {
            long targetEmc = plugin.getEmcManager().getEmc(targetItem);
            if (targetEmc > 0 && state.getStoredEmc() >= targetEmc) {
                produceItem(state, targetItem, targetEmc);
            }
        }

        updateEmcDisplay(inv, state);
        updateProgressBar(inv, state);
    }

    private void produceItem(CondenserState state, ItemStack targetItem, long targetEmc) {
        List<Integer> availableSlots = outputSlots.getOrDefault(state.getType(), Collections.emptyList());
        while (state.getStoredEmc() >= targetEmc) {
            boolean stacked = false;
            for (int slot : availableSlots) {
                ItemStack currentItem = state.getInventory().getItem(slot);
                if (currentItem != null && currentItem.isSimilar(targetItem) && currentItem.getAmount() < currentItem.getMaxStackSize()) {
                    currentItem.setAmount(currentItem.getAmount() + 1);
                    state.removeEmc(targetEmc);
                    stacked = true;
                    break;
                }
            }

            if (stacked) {
                continue;
            }

            boolean placedInNewSlot = false;
            for (int slot : availableSlots) {
                if (state.getInventory().getItem(slot) == null) {
                    ItemStack newItem = targetItem.clone();
                    newItem.setAmount(1);
                    state.getInventory().setItem(slot, newItem);
                    state.removeEmc(targetEmc);
                    placedInNewSlot = true;
                    break;
                }
            }

            if (!placedInNewSlot) {
                break;
            }
        }
    }

    private void updateEmcDisplay(Inventory inv, CondenserState state) {
        Integer displaySlot = emcDisplaySlot.get(state.getType());
        if (displaySlot == null) return;

        ItemStack displayItem = inv.getItem(displaySlot);
        if (displayItem == null) return;

        ItemMeta meta = displayItem.getItemMeta();
        if (meta == null) return;

        Integer targetSlotIndex = targetSlot.get(state.getType());
        ItemStack targetItem = (targetSlotIndex != null) ? inv.getItem(targetSlotIndex) : null;
        long targetEmc = (targetItem != null) ? plugin.getEmcManager().getEmc(targetItem) : 0;

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("emc", String.format("%,d", state.getStoredEmc()));
        meta.setDisplayName(plugin.getLanguageManager().get("gui.condenser.emc_display", placeholders));

        List<String> lore = new ArrayList<>();
        if (targetEmc > 0) {
            long remainingEmc = targetEmc - state.getStoredEmc();
            if (remainingEmc < 0) remainingEmc = 0;
            double progress = (double) state.getStoredEmc() / targetEmc * 100;
            if (progress > 100) progress = 100;

            Map<String, String> lorePlaceholders = new HashMap<>();
            lorePlaceholders.put("value", String.format("%,d", targetEmc));
            lore.add(plugin.getLanguageManager().get("gui.condenser.target_emc", lorePlaceholders));

            lorePlaceholders.put("value", String.format("%,d", remainingEmc));
            lore.add(plugin.getLanguageManager().get("gui.condenser.remaining_emc", lorePlaceholders));

            lorePlaceholders.put("value", String.format("%.2f%%", progress));
            lore.add(plugin.getLanguageManager().get("gui.condenser.progress", lorePlaceholders));
        }
        meta.setLore(lore);
        displayItem.setItemMeta(meta);
    }

    private void updateProgressBar(Inventory inv, CondenserState state) {
        List<Integer> pBarSlots = progressBarSlots.get(state.getType());
        if (pBarSlots == null || pBarSlots.isEmpty()) return;

        Integer targetSlotIndex = targetSlot.get(state.getType());
        ItemStack targetItem = (targetSlotIndex != null) ? inv.getItem(targetSlotIndex) : null;
        long targetEmc = (targetItem != null) ? plugin.getEmcManager().getEmc(targetItem) : 0;

        if (targetEmc <= 0) {
            for (int slot : pBarSlots) {
                ItemStack pane = inv.getItem(slot);
                if (pane != null) {
                    pane.setType(Material.BLACK_STAINED_GLASS_PANE);
                }
            }
            return;
        }

        double progress = (double) state.getStoredEmc() / targetEmc;
        if (progress > 1) progress = 1;

        int greenPanes = (int) Math.floor(progress * pBarSlots.size());

        for (int i = 0; i < pBarSlots.size(); i++) {
            int slot = pBarSlots.get(i);
            ItemStack pane = inv.getItem(slot);
            if (pane != null) {
                if (i < greenPanes) {
                    pane.setType(Material.LIME_STAINED_GLASS_PANE);
                } else {
                    pane.setType(Material.BLACK_STAINED_GLASS_PANE);
                }
            }
        }
    }
}