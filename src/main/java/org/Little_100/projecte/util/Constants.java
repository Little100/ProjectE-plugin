package org.Little_100.projecte.util;

import org.Little_100.projecte.ProjectE;
import org.bukkit.NamespacedKey;

public class Constants {
    private Constants() {}

    public static final NamespacedKey ID_KEY = new NamespacedKey(ProjectE.getInstance(), "projecte_id");
    public static final NamespacedKey NAME_KEY = new NamespacedKey(ProjectE.getInstance(), "name");
    public static final NamespacedKey MODEL_KEY = new NamespacedKey(ProjectE.getInstance(), "cmd");
    public static final NamespacedKey MATERIAL_KEY = new NamespacedKey(ProjectE.getInstance(), "material");
    public static final NamespacedKey KATAR_MODE_KEY = new NamespacedKey(ProjectE.getInstance(), "projecte_katar_mode");
    public static final NamespacedKey CHARGE_KEY = new NamespacedKey(ProjectE.getInstance(), "projecte_charge");
}
