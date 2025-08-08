/**
 * 此文件是由 Claude 4 thinking所创建
 * 因为我不想再去搞这破烂Custom Model Data
 * 哎我操这个mojang怎么那么坏🤬
 */
package org.Little_100.projecte.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
        Integer intValue = STRING_TO_INT_MAPPING.get(modelId);
        if (intValue == null) {
            try {
                // 如果没有映射，尝试将字符串ID解析为整数
                intValue = Integer.parseInt(modelId);
            } catch (NumberFormatException e) {
                // 如果无法解析，则默认为0
                intValue = 0;
            }
        }

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
        String modelId = INT_TO_STRING_MAPPING.getOrDefault(intValue, String.valueOf(intValue));

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
            // 获取现有的CustomModelDataComponent
            CustomModelDataComponent component = meta.getCustomModelDataComponent();

            // 如果组件为null，我们创建一个新的
            if (component == null) {
                // 使用我们实现的工厂方法创建组件
                component = createCustomModelDataComponent(modelId);
                
                // 如果创建成功，设置到ItemMeta
                if (component != null) {
                    meta.setCustomModelDataComponent(component);
                } else {
                    // 创建失败时尝试直接设置
                    // 某些Bukkit实现可能会在设置null组件时自动创建组件
                    try {
                        // 首先尝试获取可能的现有数值
                        Integer intValue = null;
                        if (meta.hasCustomModelData()) {
                            try {
                                intValue = meta.getCustomModelData();
                            } catch (Exception ignored) {
                                // 忽略异常，可能是不支持整数值的情况
                            }
                        }

                        // 由于无法直接创建组件，我们尝试通过其他API方法设置
                        // 比如使用反射调用可能存在的设置方法
                        try {
                            java.lang.reflect.Method setStringsMethod = meta.getClass().getMethod("setCustomModelDataStrings", java.util.List.class);
                            if (setStringsMethod != null) {
                                setStringsMethod.invoke(meta, Collections.singletonList(modelId));
                            }
                        } catch (Exception e) {
                            // 如果直接设置字符串失败，抛出异常让调用者处理
                            throw new RuntimeException("Unable to set CustomModelDataComponent: " + e.getMessage(), e);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Unable to set CustomModelDataComponent: " + e.getMessage(), e);
                    }
                }
            } else {
                // 如果组件已存在，更新它的字符串列表
                component.setStrings(Collections.singletonList(modelId));
                meta.setCustomModelDataComponent(component);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to set new API model data: " + e.getMessage(), e);
        }
    }

    /**
     * 创建CustomModelDataComponent的辅助方法
     * 由于CustomModelDataComponent是抽象类，我们需要找到正确的实例化方法
     *
     * @param modelId 字符串标识符
     * @return CustomModelDataComponent实例
     */
    private static CustomModelDataComponent createCustomModelDataComponent(String modelId) {
        // 使用Bukkit API提供的工厂方法创建CustomModelDataComponent
        try {
            // 尝试使用各种可能的静态工厂方法

            // 1. 尝试 CustomModelDataComponent.of(String)
            try {
                java.lang.reflect.Method ofMethod = CustomModelDataComponent.class.getMethod("of", String.class);
                if (ofMethod != null) {
                    return (CustomModelDataComponent) ofMethod.invoke(null, modelId);
                }
            } catch (NoSuchMethodException ignored) {
                // 忽略，尝试下一个方法
            }

            // 2. 尝试 CustomModelDataComponent.strings(List<String>)
            try {
                java.lang.reflect.Method stringsMethod = CustomModelDataComponent.class.getMethod("strings", java.util.List.class);
                if (stringsMethod != null) {
                    return (CustomModelDataComponent) stringsMethod.invoke(null, Collections.singletonList(modelId));
                }
            } catch (NoSuchMethodException ignored) {
                // 忽略，尝试下一个方法
            }
            
            // 3. 尝试 CustomModelDataComponent.strings(String...)
            try {
                java.lang.reflect.Method varargStringsMethod = CustomModelDataComponent.class.getMethod("strings", String[].class);
                if (varargStringsMethod != null) {
                    return (CustomModelDataComponent) varargStringsMethod.invoke(null, (Object) new String[]{modelId});
                }
            } catch (NoSuchMethodException ignored) {
                // 忽略，尝试下一个方法
            }
            
            // 4. 尝试 ItemMeta的直接方法
            return null; // 让调用者尝试使用ItemMeta的方法
            
        } catch (Exception e) {
            System.err.println("Failed to create CustomModelDataComponent: " + e.getMessage());
            // 不打印堆栈跟踪，避免过多日志
        }
        
        // 如果所有方法都失败，返回null，由调用者处理
        return null;
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

        // 检查是否支持新版API
        boolean newApiSupported = isNewApiSupported();
        
        if (newApiSupported) {
            // 在新版API中，正确的做法是先设置整数值，然后获取组件并添加字符串
            // 这样可以确保不会覆盖彼此

            // 1. 设置整数值。在现代版本中，这会创建或更新CustomModelDataComponent
            try {
                meta.setCustomModelData(intValue);
            } catch (Exception e) {
                System.err.println("Warning: Failed to set integer CustomModelData: " + e.getMessage());
            }

            // 2. 获取组件并设置字符串值
            try {
                CustomModelDataComponent component = meta.getCustomModelDataComponent();
                if (component != null) {
                    // 在现有组件上添加字符串
                    component.setStrings(Collections.singletonList(modelId));
                    // 重新设置组件以确保更改生效
                    meta.setCustomModelDataComponent(component);
                } else {
                    // 如果组件仍然是null（不应该发生但作为备用），则尝试单独设置字符串
                    setNewApiModelData(meta, modelId);
                }
            } catch (Exception e) {
                System.err.println("Warning: Failed to set string on CustomModelData component: " + e.getMessage());
            }
        } else {
            // 旧版API只支持整数值
            try {
                meta.setCustomModelData(intValue);
            } catch (Exception e) {
                System.err.println("Warning: Failed to set old API CustomModelData: " + e.getMessage());
            }
        }

        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * 创建同时支持字符串和整数值的CustomModelDataComponent
     * 这个方法已经不再尝试使用反射创建同时支持的组件
     * 而是先创建字符串组件，后续再分别设置整数值
     *
     * @param modelId 字符串标识符
     * @param intValue 整数值
     * @return 创建的组件，如果无法创建则返回null
     */
    private static CustomModelDataComponent createCustomModelDataComponentWithInt(String modelId, int intValue) {
        // 直接返回字符串组件，整数值将在ItemMeta上单独设置
        return createCustomModelDataComponent(modelId);
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
                if (component != null && !component.getStrings().isEmpty()) {
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
                if (component != null && !component.getStrings().isEmpty()) {
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
                return component != null && !component.getStrings().isEmpty();
            } catch (Exception e) {
                // 忽略错误，记录日志但继续执行
                System.err.println("Warning: Failed to check CustomModelData: " + e.getMessage());
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