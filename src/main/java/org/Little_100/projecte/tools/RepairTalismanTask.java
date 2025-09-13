package org.Little_100.projecte.tools;

import org.Little_100.projecte.ProjectE;
import org.Little_100.projecte.compatibility.SchedulerAdapter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
 
public class RepairTalismanTask {
 
    private final ProjectE plugin;
    private final SchedulerAdapter scheduler;
    private final RepairTalisman repairTalisman;

    public RepairTalismanTask(ProjectE plugin) {
        this.plugin = plugin;
        this.scheduler = plugin.getSchedulerAdapter();
        this.repairTalisman = plugin.getRepairTalisman();
        startRepairTask();
    }
 
    private void startRepairTask() {
        scheduler.runTimer(() -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                scheduler.runTaskOnEntity(player, () -> {
                    boolean hasTalisman = false;
                    for (ItemStack item : player.getInventory().getContents()) {
                        if (repairTalisman.isRepairTalisman(item)) {
                            hasTalisman = true;
                            break;
                        }
                    }

                    if (hasTalisman) {
                        for (ItemStack item : player.getInventory().getContents()) {
                            if (item != null && item.getItemMeta() instanceof Damageable) {
                                if (plugin.getKleinStarManager().isKleinStar(item)) {
                                    continue;
                                }
                                if (plugin.getToolManager().isProjectETool(item)) {
                                    continue;
                                }
                                Damageable meta = (Damageable) item.getItemMeta();
                                if (meta.hasDamage() && meta.getDamage() > 0) {
                                    meta.setDamage(Math.max(0, meta.getDamage() - 1));
                                    item.setItemMeta(meta);
                                }
                            }
                        }
                        for (ItemStack item : player.getInventory().getArmorContents()) {
                            if (item != null && item.getItemMeta() instanceof Damageable) {
                                if (plugin.getKleinStarManager().isKleinStar(item)) {
                                    continue;
                                }
                                if (plugin.getToolManager().isProjectETool(item)) {
                                    continue;
                                }
                                Damageable meta = (Damageable) item.getItemMeta();
                                if (meta.hasDamage() && meta.getDamage() > 0) {
                                    meta.setDamage(Math.max(0, meta.getDamage() - 1));
                                    item.setItemMeta(meta);
                                }
                            }
                        }
                    }
                });
            }
        }, 1L, 20L);
    }
}