package org.Little_100.projecte.util;

import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;
import java.util.List;

public class ReflectionUtil {

    public static void setCustomModelData(ItemMeta meta, List<String> modelData) {
        try {
            Class<?> componentClass = Class.forName("org.bukkit.inventory.meta.components.CustomModelData");
            Object component = componentClass.getMethod("of", int.class).invoke(null, Integer.parseInt(modelData.get(0).split(":")[1]));
            Method setComponentMethod = meta.getClass().getMethod("setCustomModelData", componentClass);
            setComponentMethod.invoke(meta, component);
        } catch (Exception e) {
            // Fallback for older versions
            try {
                Method setCustomModelDataMethod = meta.getClass().getMethod("setCustomModelData", Integer.class);
                setCustomModelDataMethod.invoke(meta, Integer.parseInt(modelData.get(0).split(":")[1]));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}