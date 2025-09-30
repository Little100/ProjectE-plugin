package org.Little_100.projecte.util;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ParticleHelper {
    
    private static boolean isLegacyParticle = false;
    private static Class<?> particleClass;
    private static Method registryGetMethod;
    private static Class<?> namespacedKeyClass;
    private static Method minecraftMethod;
    private static Method spawnParticleMethod;
    
    static {
        try {
            particleClass = Class.forName("org.bukkit.Particle");
            namespacedKeyClass = Class.forName("org.bukkit.NamespacedKey");
            
            if (particleClass.isEnum()) {
                isLegacyParticle = true;
            } else {
                Class<?> registryClass = Class.forName("org.bukkit.Registry");
                Field attributeField = registryClass.getField("PARTICLE");
                Object particleRegistry = attributeField.get(null);
                registryGetMethod = particleRegistry.getClass().getMethod("get", namespacedKeyClass);
                minecraftMethod = namespacedKeyClass.getMethod("minecraft", String.class);
            }
            
            spawnParticleMethod = Player.class.getMethod(
                "spawnParticle",
                particleClass,
                Location.class,
                int.class,
                double.class,
                double.class,
                double.class,
                double.class
            );
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void spawnParticle(
            Player player,
            String particleName,
            Location location,
            int count,
            double offsetX,
            double offsetY,
            double offsetZ,
            double extra) {
        
        try {
            Object particle = getParticle(particleName);
            if (particle == null) {
                return;
            }
            
            spawnParticleMethod.invoke(
                player,
                particle,
                location,
                count,
                offsetX,
                offsetY,
                offsetZ,
                extra
            );
        } catch (Exception e) {
            // 静默失败避免刷屏
        }
    }
    
    private static Object getParticle(String particleName) {
        try {
            String normalizedName = particleName.toLowerCase().replace(" ", "_");
            
            if (isLegacyParticle) {
                String enumName = normalizedName.toUpperCase();
                Method valueOfMethod = particleClass.getMethod("valueOf", String.class);
                return valueOfMethod.invoke(null, enumName);
            } else {
                Object key = minecraftMethod.invoke(null, normalizedName);
                Field registryField = Class.forName("org.bukkit.Registry").getField("PARTICLE");
                Object particleRegistry = registryField.get(null);
                return registryGetMethod.invoke(particleRegistry, key);
            }
        } catch (Exception e) {
            try {
                if (isLegacyParticle) {
                    Method valueOfMethod = particleClass.getMethod("valueOf", String.class);
                    return valueOfMethod.invoke(null, "END_ROD");
                } else {
                    Object key = minecraftMethod.invoke(null, "end_rod");
                    Field registryField = Class.forName("org.bukkit.Registry").getField("PARTICLE");
                    Object particleRegistry = registryField.get(null);
                    return registryGetMethod.invoke(particleRegistry, key);
                }
            } catch (Exception ex) {
                return null;
            }
        }
    }
}
