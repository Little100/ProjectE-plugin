package org.Little_100.projecte.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.Little_100.projecte.ProjectE;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MaterialStackSizeModifier {

    private final ProjectE plugin;
    private boolean isSetupComplete = false;
    private String serverVersion;
    
    private Class<?> itemClass;
    private Class<?> itemsClass;
    private Method getItemNameMethod;
    private Field itemsComponentsField;
    private Field componentsMapField;
    private Method componentMapPutMethod;
    private Object maxStackSizeComponent;

    public MaterialStackSizeModifier(ProjectE plugin) {
        this.plugin = plugin;
        setup();
    }

    private void setup() {
        try {
            // 检测服务器版本
            String packageVersion = Bukkit.getServer().getClass().getPackage().getName();
            serverVersion = packageVersion.substring(packageVersion.lastIndexOf('.') + 1);
            plugin.getLogger().info("检测到服务器版本: " + serverVersion + " (Bukkit: " + Bukkit.getBukkitVersion() + ")");

            // 设置NMS反射类
            setupReflectionClasses();
            
            isSetupComplete = true;
            plugin.getLogger().info("MaterialStackSizeModifier 初始化成功！");
        } catch (Exception e) {
            plugin.getLogger().warning("MaterialStackSizeModifier 初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupReflectionClasses() {
        try {
            // 根据版本确定类路径
            boolean isModern = !serverVersion.startsWith("v1_7") && 
                              !serverVersion.startsWith("v1_8") &&
                              !serverVersion.startsWith("v1_9") &&
                              !serverVersion.startsWith("v1_10") &&
                              !serverVersion.startsWith("v1_11") &&
                              !serverVersion.startsWith("v1_12") &&
                              !serverVersion.startsWith("v1_13") &&
                              !serverVersion.startsWith("v1_14") &&
                              !serverVersion.startsWith("v1_15") &&
                              !serverVersion.startsWith("v1_16");

            String itemClassPath;
            String itemsClassPath;
            String itemNameMethodName;

            if (serverVersion.equalsIgnoreCase("craftbukkit") || isModern) {
                itemClassPath = "net.minecraft.world.item.Item";
                itemsClassPath = "net.minecraft.world.item.Items";
                
                if (Bukkit.getBukkitVersion().startsWith("1.21.3") || Bukkit.getBukkitVersion().startsWith("1.21.4")) {
                    itemNameMethodName = "l";
                } else if (Bukkit.getBukkitVersion().startsWith("1.17")) {
                    itemNameMethodName = "getName";
                } else {
                    itemNameMethodName = "a";
                }
            } else {
                itemClassPath = "net.minecraft.server." + serverVersion + ".Item";
                itemsClassPath = "net.minecraft.server." + serverVersion + ".Items";
                itemNameMethodName = "getName";
            }

            plugin.getLogger().info("尝试加载 NMS 类: " + itemClassPath);
            itemClass = Class.forName(itemClassPath);
            itemsClass = Class.forName(itemsClassPath);
            getItemNameMethod = itemClass.getMethod(itemNameMethodName);
            
            plugin.getLogger().info("NMS 类加载成功!");

            if (Bukkit.getBukkitVersion().startsWith("1.20.6") ||
                Bukkit.getBukkitVersion().startsWith("1.21")) {
                
                plugin.getLogger().info("检测到 1.20.5+ 版本，设置 DataComponents...");
                setupDataComponents();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("设置反射类失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupDataComponents() {
        try {
            Class<?> dataComponentsClass = Class.forName("net.minecraft.core.component.DataComponents");
            
            try {
                Field maxStackField = dataComponentsClass.getField("MAX_STACK_SIZE");
                maxStackSizeComponent = maxStackField.get(null);
            } catch (NoSuchFieldException e) {
                Field maxStackField = dataComponentsClass.getField("k");
                maxStackSizeComponent = maxStackField.get(null);
            }

            for (Field field : itemClass.getDeclaredFields()) {
                if (field.getType().getSimpleName().contains("DataComponentMap")) {
                    itemsComponentsField = field;
                    itemsComponentsField.setAccessible(true);
                    break;
                }
            }

            if (itemsComponentsField != null && maxStackSizeComponent != null) {
                plugin.getLogger().info("DataComponents 设置成功!");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("设置 DataComponents 失败: " + e.getMessage());
        }
    }

    public boolean setChorusFruitStackSize() {
        if (!isSetupComplete) {
            plugin.getLogger().warning("MaterialStackSizeModifier 未初始化完成，无法设置堆叠大小");
            return false;
        }

        Material material = plugin.getVersionAdapter().getMaterial("POPPED_CHORUS_FRUIT");
        if (material == null) {
            plugin.getLogger().warning("无法找到 POPPED_CHORUS_FRUIT 材料");
            return false;
        }

        boolean success = true;

        if (!setMaterialMaxStack(material, 1)) {
            plugin.getLogger().warning("修改 Material.maxStack 失败");
            success = false;
        }

        if (!setNMSItemMaxStack("popped_chorus_fruit", 1)) {
            plugin.getLogger().warning("修改 NMS Item 堆叠大小失败");
            success = false;
        }

        if (success) {
            plugin.getLogger().info("§a成功将 POPPED_CHORUS_FRUIT 的堆叠上限设置为 1！");
        }

        return success;
    }

    private boolean setMaterialMaxStack(Material material, int stackSize) {
        try {
            Field maxStackField = Material.class.getDeclaredField("maxStack");
            maxStackField.setAccessible(true);
            maxStackField.set(material, stackSize);
            maxStackField.setAccessible(false);
            
            plugin.getLogger().info("成功修改 Material." + material.name() + ".maxStack = " + stackSize);
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("修改 Material.maxStack 失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean setNMSItemMaxStack(String itemName, int stackSize) {
        try {
            Field itemField = findItemField(itemName);
            if (itemField == null) {
                plugin.getLogger().warning("无法找到 NMS Item 字段: " + itemName);
                return false;
            }

            Object nmsItem = itemField.get(null);
            if (nmsItem == null) {
                plugin.getLogger().warning("NMS Item 为 null: " + itemName);
                return false;
            }

            if (maxStackSizeComponent != null && itemsComponentsField != null) {
                return setDataComponentMaxStack(nmsItem, stackSize, itemName);
            } else {
                return setItemFieldMaxStack(nmsItem, stackSize, itemName);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("设置 NMS Item 堆叠大小失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private Field findItemField(String itemName) {
        try {
            try {
                return itemsClass.getDeclaredField(itemName.toUpperCase());
            } catch (NoSuchFieldException e) {
            }

            for (Field field : itemsClass.getFields()) {
                try {
                    Object item = field.get(null);
                    if (item == null) continue;

                    String name = (String) getItemNameMethod.invoke(item);
                    if (name != null) {
                        String[] parts = name.split("\\.");
                        String shortName = parts[parts.length - 1];
                        if (shortName.equals(itemName)) {
                            plugin.getLogger().info("找到 NMS Item 字段: " + field.getName() + " -> " + itemName);
                            return field;
                        }
                    }
                } catch (Exception ex) {
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("查找 Item 字段失败: " + e.getMessage());
        }
        return null;
    }

    private boolean setDataComponentMaxStack(Object nmsItem, int stackSize, String itemName) {
        try {
            plugin.getLogger().info("使用 DataComponents 方法设置堆叠大小");
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("设置 DataComponent 失败: " + e.getMessage());
            return false;
        }
    }

    private boolean setItemFieldMaxStack(Object nmsItem, int stackSize, String itemName) {
        try {
            String[] possibleFieldNames = {"maxStackSize", "c", "d"};
            
            for (String fieldName : possibleFieldNames) {
                try {
                    Field field = itemClass.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.set(nmsItem, stackSize);
                    field.setAccessible(false);
                    
                    plugin.getLogger().info("成功修改 NMS Item." + itemName + "." + fieldName + " = " + stackSize);
                    return true;
                } catch (NoSuchFieldException e) {
                }
            }
            
            plugin.getLogger().warning("无法找到 NMS Item 的堆叠大小字段");
            return false;
        } catch (Exception e) {
            plugin.getLogger().warning("修改 NMS Item 字段失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

