package org.Little_100.projecte.util;

import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.lang.reflect.Method;
import java.util.List;

public class ReflectionUtil {

    public static void setCustomModelData(ItemMeta meta, List<String> modelData) {
        try {
            Class<?> componentClass = Class.forName("org.bukkit.inventory.meta.components.CustomModelDataComponent");
            Object component = componentClass.getMethod("ofStrings", List.class).invoke(null, modelData);
            Method setComponentMethod = meta.getClass().getMethod("setCustomModelDataComponent", componentClass);
            setComponentMethod.invoke(meta, component);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}