/**
 * 此文件是由 Claude 4 thinking所创建
 * 因为我不想再去搞这破烂Custom Model Data
 * 哎我操这个mojang怎么那么坏
 */
package org.Little_100.projecte.util;

import org.Little_100.projecte.ProjectE;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * 用于处理CustomModelData的工具类，同时支持1.21.4+的字符串标识符和旧版的整数值
 * 
 * === 使用指南 ===
 * 
 * 1. 基本用法 - 自动适配新旧版本：
 *    CustomModelDataUtil.setCustomModelData(item, "my_model_id");
 * 
 * 2. 设置新版本的字符串标识符（1.21.4+）：
 *    CustomModelDataUtil.setNewCustomModelData(item, "my_model_id");
 * 
 * 3. 设置旧版本的整数值（1.21.3-）：
 *    CustomModelDataUtil.setOldCustomModelData(item, 12345);
 * 
 * 4. 同时设置新旧版本（推荐用于跨版本兼容）：
 *    CustomModelDataUtil.setCustomModelDataBoth(item, "my_model_id", 12345);
 * 
 * 5. 获取字符串标识符：
 *    String modelId = CustomModelDataUtil.getCustomModelDataString(item);
 * 
 * 6. 获取整数值：
 *    int modelData = CustomModelDataUtil.getCustomModelDataInt(item);
 * 
 * 7. 注册新的映射关系：
 *    CustomModelDataUtil.registerMapping("my_model_id", 12345);
 * 
 * === 注意事项 ===
 * - 在1.21.4+版本中，优先使用字符串标识符
 * - 在旧版本中，只能使用整数值
 * - 建议为每个模型同时设置字符串标识符和整数值以确保兼容性
 * - 字符串标识符建议使用命名空间格式，如："plugin_name:model_name"
 */
public class CustomModelDataUtil {
    // 新旧API检测结果缓存
    private static Boolean useNewApi = null;
    
    // 字符串标识符到整数的映射
    private static final Map<String, Integer> STRING_TO_INT_MAPPING = new HashMap<>();
    
    // 整数到字符串标识符的映射（用于反向查找）
    private static final Map<Integer, String> INT_TO_STRING_MAPPING = new HashMap<>();
    
    // 初始化常用的映射关系
    static {
        // 这里添加项目中常用的CustomModelData映射
        registerMapping("diamond_lattice", 1001);
        // 可以根据需要添加更多映射...
    }
    
    /**
     * 检查当前环境是否支持新版CustomModelData API (1.21.4+)
     * 
     * @return 如果支持新版API返回true，否则返回false
     */
    public static boolean isNewApiSupported() {
        if (useNewApi == null) {
            try {
                Class.forName("org.bukkit.inventory.meta.components.CustomModelDataComponent");
                useNewApi = true;
            } catch (ClassNotFoundException e) {
                useNewApi = false;
            }
        }
        return useNewApi;
    }
    
    /**
     * 为物品设置CustomModelData，自动适配新旧API
     * 推荐使用此方法，会自动根据服务器版本同时设置字符串和整数值
     * 确保跨版本兼容
     *
     * @param item 需要设置CustomModelData的物品
     * @param modelId 字符串标识符，如 "diamond_lattice"
     * @return 设置后的物品
     */
    public static ItemStack setCustomModelData(ItemStack item, String modelId) {
        if (item == null) return null;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        
        // 获取对应的整数值
        Integer intValue = STRING_TO_INT_MAPPING.getOrDefault(modelId, 0);
        
        // 同时设置两种格式的CustomModelData
        return setCustomModelDataBoth(item, modelId, intValue);
    }
    
    /**
     * 为物品设置CustomModelData，接受整数值参数
     * 会同时设置字符串标识符（如果有映射）和整数值
     * 确保跨版本兼容
     *
     * @param item 需要设置CustomModelData的物品
     * @param intValue 整数值
     * @return 设置后的物品
     */
    public static ItemStack setCustomModelData(ItemStack item, int intValue) {
        if (item == null) return null;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        
        // 获取对应的字符串标识符
        String modelId = INT_TO_STRING_MAPPING.getOrDefault(intValue, "cmd_" + intValue);
        
        // 同时设置两种格式的CustomModelData
        return setCustomModelDataBoth(item, modelId, intValue);
    }
    
    /**
     * 设置新版API的CustomModelData（仅限1.21.4+）
     * 直接设置字符串标识符，不进行版本检测
     * 
     * @param item 需要设置CustomModelData的物品
     * @param modelId 字符串标识符
     * @return 设置后的物品
     */
    public static ItemStack setNewCustomModelData(ItemStack item, String modelId) {
        if (item == null) return null;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        
        try {
            // 直接设置新版API的字符串标识符
            setNewApiModelData(meta, modelId);
            item.setItemMeta(meta);
        } catch (Exception e) {
            // 如果出现异常，记录并返回原物品
            System.err.println("Failed to set new CustomModelData: " + e.getMessage());
        }
        
        return item;
    }
    
    /**
     * 设置旧版API的CustomModelData（1.21.3-）
     * 直接设置整数值，不进行版本检测
     * 
     * @param item 需要设置CustomModelData的物品
     * @param intValue 整数值
     * @return 设置后的物品
     */
    public static ItemStack setOldCustomModelData(ItemStack item, int intValue) {
        if (item == null) return null;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        
        try {
            meta.setCustomModelData(intValue);
            item.setItemMeta(meta);
        } catch (Exception e) {
            // 如果出现异常，记录并返回原物品
            System.err.println("Failed to set old CustomModelData: " + e.getMessage());
        }
        
        return item;
    }
    
    /**
     * 设置新版API的CustomModelData（私有方法）
     * 使用安全的方式设置字符串标识符
     * 
     * @param meta ItemMeta对象
     * @param modelId 字符串标识符
     */
    private static void setNewApiModelData(ItemMeta meta, String modelId) {
        try {
            CustomModelDataComponent component = meta.getCustomModelDataComponent();
            
            // 如果组件为null，我们需要通过设置字符串来创建它
            // 由于CustomModelDataComponent是抽象类，我们不能直接实例化
            // 但我们可以通过ItemMeta的方法来间接操作
            component.setStrings(Collections.singletonList(modelId));
            meta.setCustomModelDataComponent(component);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set new API model data: " + e.getMessage(), e);
        }
    }
    
    /**
     * 为物品同时设置新旧两种CustomModelData
     * 这在过渡期或多版本兼容时特别有用
     * 
     * @param item 需要设置CustomModelData的物品
     * @param modelId 字符串标识符
     * @param intValue 整数值
     * @return 设置后的物品
     */
    public static ItemStack setCustomModelDataBoth(ItemStack item, String modelId, int intValue) {
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        // 步骤 1: 始终设置整数值，以兼容旧版客户端和服务器。
        if (intValue > 0) {
            try {
                meta.setCustomModelData(intValue);
            } catch (Exception e) {
                System.err.println("[CMD] 设置整数值时出错: " + e.getMessage());
            }
        }

        // 步骤 2: 如果支持新版API，则显式处理字符串组件。
        if (isNewApiSupported() && modelId != null && !modelId.isEmpty()) {
            try {
                // 获取组件，如果不存在则创建一个新的。
                CustomModelDataComponent component = meta.getCustomModelDataComponent();
                if (component == null) {
                    // 无法直接实例化 CustomModelDataComponent，因为它是一个接口。
                    // 我们必须通过ItemFactory或类似方式来创建，但目前没有公共API。
                    // 然而，在调用 meta.setCustomModelData(int) 之后，Paper/Spigot应该已经为我们创建了组件。
                    // 如果它仍然是null，说明服务器实现不支持这种方式，我们无法安全地继续。
                    System.err.println("[CMD警告] 无法获取或创建CustomModelDataComponent。字符串ID将不会被设置。");
                } else {
                    // 为组件设置字符串值。
                    // 使用整数值的字符串形式，而不是modelId
                    component.setStrings(Collections.singletonList(String.valueOf(intValue)));
                    // 将修改后的组件应用回ItemMeta。
                    meta.setCustomModelDataComponent(component);
                }
            } catch (Throwable e) {
                System.err.println("[CMD] 设置字符串组件时出错: " + e.getMessage());
            }
        }

        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * 获取物品的CustomModelData字符串值
     * 使用双向映射提高查找效率
     *
     * @param item 物品
     * @return 如果存在则返回字符串值，否则返回null
     */
    public static String getCustomModelDataString(ItemStack item) {
        if (item == null) return null;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        
        // 首先尝试从新版API获取字符串值
        if (isNewApiSupported()) {
            try {
                CustomModelDataComponent component = meta.getCustomModelDataComponent();
                if (!component.getStrings().isEmpty()) {
                    return component.getStrings().get(0);
                }
            } catch (Exception e) {
                // 忽略错误，继续尝试其他方法
            }
        }
        
        // 如果使用旧版API或者新版API没有字符串值，尝试从整数值反向查找
        if (meta.hasCustomModelData()) {
            try {
                int intValue = meta.getCustomModelData();
                // 使用反向映射直接查找
                String modelId = INT_TO_STRING_MAPPING.get(intValue);
                if (modelId != null) {
                    return modelId;
                }
            } catch (IllegalStateException e) {
                // 忽略错误，因为这可能是只有字符串CMD的情况
            }
        }
        
        return null;
    }
    
    /**
     * 获取物品的CustomModelData整数值
     * 使用双向映射提高查找效率
     *
     * @param item 物品
     * @return 如果存在则返回整数值，否则返回0
     */
    public static int getCustomModelDataInt(ItemStack item) {
        if (item == null) return 0;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;
        
        // 优先使用原生的整数值
        if (meta.hasCustomModelData()) {
            try {
                return meta.getCustomModelData();
            } catch (IllegalStateException e) {
                // 忽略错误，因为这可能是只有字符串CMD的情况
                // 继续尝试从字符串转换
            }
        }
        
        // 如果没有整数值但有字符串值，尝试转换
        if (isNewApiSupported()) {
            try {
                CustomModelDataComponent component = meta.getCustomModelDataComponent();
                if (!component.getStrings().isEmpty()) {
                    String modelId = component.getStrings().get(0);
                    return STRING_TO_INT_MAPPING.getOrDefault(modelId, 0);
                }
            } catch (Exception e) {
                // 忽略错误，记录日志但继续执行
                System.err.println("Warning: Failed to get CustomModelDataInt: " + e.getMessage());
            }
        }
        
        return 0;
    }
    
    /**
     * 获取字符串标识符对应的整数值
     * 
     * @param modelId 字符串标识符
     * @return 对应的整数值，如果不存在则返回0
     */
    public static int getIntValueForModelId(String modelId) {
        return STRING_TO_INT_MAPPING.getOrDefault(modelId, 0);
    }
    
    /**
     * 注册新的CustomModelData映射
     * 建议在插件启动时调用此方法来注册所有的模型映射
     * 同时添加到正向和反向映射中
     *
     * @param modelId 字符串标识符
     * @param intValue 对应的整数值
     */
    public static void registerMapping(String modelId, int intValue) {
        STRING_TO_INT_MAPPING.put(modelId, intValue);
        INT_TO_STRING_MAPPING.put(intValue, modelId);
    }
    
    /**
     * 清理缓存，强制重新检测API版本
     * 在某些特殊情况下可能需要调用此方法
     */
    public static void clearCache() {
        useNewApi = null;
    }
    
    /**
     * 获取当前已注册的所有映射关系
     * 主要用于调试和日志记录
     *
     * @return 映射关系的副本
     */
    public static Map<String, Integer> getAllMappings() {
        return new HashMap<>(STRING_TO_INT_MAPPING);
    }
    
    /**
     * 获取当前已注册的所有反向映射关系
     * 主要用于调试和日志记录
     *
     * @return 反向映射关系的副本
     */
    public static Map<Integer, String> getAllReverseMappings() {
        return new HashMap<>(INT_TO_STRING_MAPPING);
    }
    
    /**
     * 检查物品是否有CustomModelData（任意格式）
     * 增强错误处理确保兼容性
     *
     * @param item 物品
     * @return 如果物品有CustomModelData则返回true
     */
    public static boolean hasCustomModelData(ItemStack item) {
        if (item == null) return false;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        
        // 检查旧版API
        try {
            if (meta.hasCustomModelData()) {
                return true;
            }
        } catch (Exception e) {
            // 忽略错误，可能是某些版本不支持此方法
        }
        
        // 检查新版API
        if (isNewApiSupported()) {
            try {
                CustomModelDataComponent component = meta.getCustomModelDataComponent();
                return !component.getStrings().isEmpty();
            } catch (Exception e) {
                // 忽略错误，记录日志但继续执行
                ProjectE.getInstance().getLogger().log(Level.WARNING, "Failed to check CustomModelData", e);
            }
        }
        
        return false;
    }
    
    /**
     * 批量注册多个CustomModelData映射
     * 方便在插件启动时一次性注册多个映射
     *
     * @param mappings 包含映射关系的Map，key为字符串标识符，value为整数值
     */
    public static void registerMappings(Map<String, Integer> mappings) {
        if (mappings == null || mappings.isEmpty()) return;
        
        for (Map.Entry<String, Integer> entry : mappings.entrySet()) {
            registerMapping(entry.getKey(), entry.getValue());
        }
    }
}