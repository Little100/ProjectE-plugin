package org.Little_100.projecte.util;

import java.util.Map;
import org.Little_100.projecte.ProjectE;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

@SuppressWarnings("unused")
public class Constants {
    private Constants() {}

    public static final Map<PersistentDataType<?, ?>, String> PDC_TYPES = Map.of(
            PersistentDataType.STRING, "String",
            PersistentDataType.INTEGER, "Integer",
            PersistentDataType.LONG, "Long",
            PersistentDataType.DOUBLE, "Double",
            PersistentDataType.FLOAT, "Float",
            PersistentDataType.BYTE, "Byte",
            PersistentDataType.SHORT, "Short");

    public static final NamespacedKey ID_KEY = new NamespacedKey(ProjectE.getInstance(), "projecte_id");
    public static final NamespacedKey NAME_KEY = new NamespacedKey(ProjectE.getInstance(), "name");
    public static final NamespacedKey MODEL_KEY = new NamespacedKey(ProjectE.getInstance(), "cmd");
    public static final NamespacedKey MATERIAL_KEY = new NamespacedKey(ProjectE.getInstance(), "material");
    public static final NamespacedKey KATAR_MODE_KEY = new NamespacedKey(ProjectE.getInstance(), "projecte_katar_mode");
    public static final NamespacedKey CHARGE_KEY = new NamespacedKey(ProjectE.getInstance(), "projecte_charge");
    public static final NamespacedKey BLOCK_DATA_KEY =
            new NamespacedKey(ProjectE.getInstance(), "custom_block_data"); // 目前未被使用
    public static final NamespacedKey GUI_ITEM_KEY = new NamespacedKey(ProjectE.getInstance(), "gui_item");
    public static final NamespacedKey CHARGE_LEVEL_KEY = new NamespacedKey(ProjectE.getInstance(), "charge_level");
    public static final NamespacedKey KLEIN_STAR_KEY = new NamespacedKey(ProjectE.getInstance(), "klein_star_level");
}
