package org.Little_100.projecte.storage;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.UUID;

public class DatabaseManager {

    private Connection connection;
    private final File dataFolder;

    public DatabaseManager(File dataFolder) {
        this.dataFolder = dataFolder;
        connect();
    }

    private void connect() {
        try {
            if (connection != null && !connection.isClosed()) {
                return;
            }

            File dbFile = new File(dataFolder, "gui/transmutation.db");
            if (!dbFile.getParentFile().exists()) {
                dbFile.getParentFile().mkdirs();
            }

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            createTables();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void createTables() {
        try (Statement statement = connection.createStatement()) {
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS emc_values (" + "item_key TEXT PRIMARY KEY," + "emc BIGINT NOT NULL);");

            statement.execute("CREATE TABLE IF NOT EXISTS player_emc (" + "player_uuid TEXT PRIMARY KEY,"
                    + "emc BIGINT NOT NULL);");

            statement.execute("CREATE TABLE IF NOT EXISTS learned_items (" + "player_uuid TEXT NOT NULL,"
                    + "item_key TEXT NOT NULL,"
                    + "PRIMARY KEY (player_uuid, item_key));");

            statement.execute("CREATE TABLE IF NOT EXISTS alchemical_bags (" + "player_uuid TEXT NOT NULL,"
                    + "bag_color TEXT NOT NULL,"
                    + "inventory_contents TEXT,"
                    + "PRIMARY KEY (player_uuid, bag_color));");

            statement.execute("CREATE TABLE IF NOT EXISTS alchemical_chests (" + "location TEXT PRIMARY KEY,"
                    + "owner_uuid TEXT NOT NULL,"
                    + "inventory_contents TEXT);");

            statement.execute("CREATE TABLE IF NOT EXISTS energy_collectors (" + "location TEXT PRIMARY KEY,"
                    + "owner_uuid TEXT NOT NULL,"
                    + "collector_type INTEGER NOT NULL,"
                    + "stored_emc BIGINT NOT NULL DEFAULT 0,"
                    + "inventory_contents TEXT);");
            
            statement.execute("CREATE TABLE IF NOT EXISTS energy_condensers (" + "location TEXT PRIMARY KEY,"
                    + "owner_uuid TEXT NOT NULL,"
                    + "condenser_type INTEGER NOT NULL,"
                    + "inventory_contents TEXT,"
                    + "target_item TEXT,"
                    + "stored_emc BIGINT NOT NULL DEFAULT 0);");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setEmc(String itemKey, long emc) {
        String sql = "INSERT OR REPLACE INTO emc_values (item_key, emc) VALUES (?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, itemKey);
            pstmt.setLong(2, emc);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public long getEmc(String itemKey) {
        String sql = "SELECT emc FROM emc_values WHERE item_key = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, itemKey);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("emc");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean hasEmcValues() {
        String sql = "SELECT 1 FROM emc_values LIMIT 1;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void clearEmcValues() {
        String sql = "DELETE FROM emc_values;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public long getPlayerEmc(UUID playerUuid) {
        String sql = "SELECT emc FROM player_emc WHERE player_uuid = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("emc");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void setPlayerEmc(UUID playerUuid, long emc) {
        String sql = "INSERT OR REPLACE INTO player_emc (player_uuid, emc) VALUES (?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid.toString());
            pstmt.setLong(2, emc);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addLearnedItem(UUID playerUuid, String itemKey) {
        String sql = "INSERT OR IGNORE INTO learned_items (player_uuid, item_key) VALUES (?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid.toString());
            pstmt.setString(2, itemKey);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean learnItem(UUID playerUuid, String itemKey) {
        if (isLearned(playerUuid, itemKey)) {
            return false;
        }
        addLearnedItem(playerUuid, itemKey);
        return true;
    }

    public java.util.List<String> getLearnedItems(UUID playerUuid) {
        java.util.List<String> learnedItems = new java.util.ArrayList<>();
        String sql = "SELECT item_key FROM learned_items WHERE player_uuid = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                learnedItems.add(rs.getString("item_key"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return learnedItems;
    }

    public boolean isLearned(UUID playerUuid, String itemKey) {
        String sql = "SELECT 1 FROM learned_items WHERE player_uuid = ? AND item_key = ? LIMIT 1;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid.toString());
            pstmt.setString(2, itemKey);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveBagInventory(UUID playerUUID, String bagColor, ItemStack[] items) {
        if (items == null) return;

        String base64Data;
        try {
            base64Data = itemStackArrayToBase64(items);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return;
        }

        String sql =
                "INSERT OR REPLACE INTO alchemical_bags (player_uuid, bag_color, inventory_contents) VALUES (?, ?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID.toString());
            pstmt.setString(2, bagColor);
            pstmt.setString(3, base64Data);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ItemStack[] loadBagInventory(UUID playerUUID, String bagColor) {
        String sql = "SELECT inventory_contents FROM alchemical_bags WHERE player_uuid = ? AND bag_color = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID.toString());
            pstmt.setString(2, bagColor);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String base64Data = rs.getString("inventory_contents");
                return itemStackArrayFromBase64(base64Data);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return new ItemStack[54];
    }

    private String itemStackArrayToBase64(ItemStack[] items) throws IllegalStateException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
            dataOutput.writeInt(items.length);
            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("Could not serialize ItemStack[] to Base64!", e);
        }
    }

    private String itemStackToBase64(ItemStack item) throws IllegalStateException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
            dataOutput.writeObject(item);
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to serialize ItemStack", e);
        }
    }

    private ItemStack itemStackFromBase64(String data) throws IOException {
        if (data == null || data.isEmpty()) {
            return null;
        }
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
                BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
            return (ItemStack) dataInput.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to deserialize ItemStack", e);
        }
    }

    private ItemStack[] itemStackArrayFromBase64(String data) throws IOException {
        if (data == null || data.isEmpty()) {
            return new ItemStack[54];
        }
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
                BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
            int size = dataInput.readInt();
            ItemStack[] items = new ItemStack[size];
            for (int i = 0; i < size; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }
            if (size < 54) {
                ItemStack[] resizedItems = new ItemStack[54];
                System.arraycopy(items, 0, resizedItems, 0, size);
                return resizedItems;
            }
            return items;
        } catch (ClassNotFoundException e) {
            throw new IOException("Could not find ItemStack class for deserialization!", e);
        }
    }

    public java.util.List<String> getBagColors(UUID playerUuid) {
        java.util.List<String> bagColors = new java.util.ArrayList<>();
        String sql = "SELECT bag_color FROM alchemical_bags WHERE player_uuid = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                bagColors.add(rs.getString("bag_color"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bagColors;
    }

    public void saveChestInventory(String locationKey, UUID ownerUUID, ItemStack[] items) {
        if (items == null) return;

        String base64Data;
        try {
            base64Data = itemStackArrayToBase64(items);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return;
        }

        String sql =
                "INSERT OR REPLACE INTO alchemical_chests (location, owner_uuid, inventory_contents) VALUES (?, ?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, locationKey);
            pstmt.setString(2, ownerUUID.toString());
            pstmt.setString(3, base64Data);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ItemStack[] loadChestInventory(String locationKey) {
        String sql = "SELECT inventory_contents FROM alchemical_chests WHERE location = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, locationKey);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String base64Data = rs.getString("inventory_contents");
                ItemStack[] items = itemStackArrayFromBase64(base64Data);
                if (items.length < 104) {
                    ItemStack[] resizedItems = new ItemStack[104];
                    System.arraycopy(items, 0, resizedItems, 0, items.length);
                    return resizedItems;
                }
                return items;
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return new ItemStack[104];
    }

    public void deleteChestInventory(String locationKey) {
        String sql = "DELETE FROM alchemical_chests WHERE location = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, locationKey);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public UUID getChestOwner(String locationKey) {
        String sql = "SELECT owner_uuid FROM alchemical_chests WHERE location = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, locationKey);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return UUID.fromString(rs.getString("owner_uuid"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveCollectorData(String locationKey, UUID ownerUUID, int collectorType, long storedEmc, ItemStack[] items) {
        String base64Data = null;
        if (items != null) {
            try {
                base64Data = itemStackArrayToBase64(items);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        String sql =
                "INSERT OR REPLACE INTO energy_collectors (location, owner_uuid, collector_type, stored_emc, inventory_contents) VALUES (?, ?, ?, ?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, locationKey);
            pstmt.setString(2, ownerUUID.toString());
            pstmt.setInt(3, collectorType);
            pstmt.setLong(4, storedEmc);
            pstmt.setString(5, base64Data);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ItemStack[] loadCollectorInventory(String locationKey, int expectedSize) {
        String sql = "SELECT inventory_contents FROM energy_collectors WHERE location = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, locationKey);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String base64Data = rs.getString("inventory_contents");
                if (base64Data != null && !base64Data.isEmpty()) {
                    ItemStack[] items = itemStackArrayFromBase64(base64Data);
                    if (items.length < expectedSize) {
                        ItemStack[] resizedItems = new ItemStack[expectedSize];
                        System.arraycopy(items, 0, resizedItems, 0, items.length);
                        return resizedItems;
                    }
                    return items;
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return new ItemStack[expectedSize];
    }

    public long loadCollectorEmc(String locationKey) {
        String sql = "SELECT stored_emc FROM energy_collectors WHERE location = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, locationKey);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("stored_emc");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int loadCollectorType(String locationKey) {
        String sql = "SELECT collector_type FROM energy_collectors WHERE location = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, locationKey);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("collector_type");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public void deleteCollectorData(String locationKey) {
        String sql = "DELETE FROM energy_collectors WHERE location = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, locationKey);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public UUID getCollectorOwner(String locationKey) {
        String sql = "SELECT owner_uuid FROM energy_collectors WHERE location = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, locationKey);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return UUID.fromString(rs.getString("owner_uuid"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public void saveCondenserData(String locationKey, UUID ownerUUID, int condenserType, 
            ItemStack[] inventoryContents, ItemStack targetItem, long storedEmc) {
        String base64Inventory = "";
        if (inventoryContents != null) {
            try {
                base64Inventory = itemStackArrayToBase64(inventoryContents);
            } catch (IllegalStateException e) {
                e.printStackTrace();
                return;
            }
        }

        String base64Target = "";
        if (targetItem != null && !targetItem.getType().isAir()) {
            try {
                base64Target = itemStackToBase64(targetItem);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        String sql = "INSERT OR REPLACE INTO energy_condensers (location, owner_uuid, condenser_type, "
                + "inventory_contents, target_item, stored_emc) VALUES (?, ?, ?, ?, ?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, locationKey);
            pstmt.setString(2, ownerUUID.toString());
            pstmt.setInt(3, condenserType);
            pstmt.setString(4, base64Inventory);
            pstmt.setString(5, base64Target);
            pstmt.setLong(6, storedEmc);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ItemStack[] loadCondenserInventory(String locationKey, int expectedSize) {
        String sql = "SELECT inventory_contents FROM energy_condensers WHERE location = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, locationKey);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String base64Data = rs.getString("inventory_contents");
                if (base64Data != null && !base64Data.isEmpty()) {
                    ItemStack[] items = itemStackArrayFromBase64(base64Data);
                    if (items.length < expectedSize) {
                        ItemStack[] resizedItems = new ItemStack[expectedSize];
                        System.arraycopy(items, 0, resizedItems, 0, items.length);
                        return resizedItems;
                    }
                    return items;
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return new ItemStack[expectedSize];
    }

    public ItemStack loadCondenserTargetItem(String locationKey) {
        String sql = "SELECT target_item FROM energy_condensers WHERE location = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, locationKey);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String base64Data = rs.getString("target_item");
                if (base64Data != null && !base64Data.isEmpty()) {
                    return itemStackFromBase64(base64Data);
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public long loadCondenserEmc(String locationKey) {
        String sql = "SELECT stored_emc FROM energy_condensers WHERE location = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, locationKey);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("stored_emc");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int loadCondenserType(String locationKey) {
        String sql = "SELECT condenser_type FROM energy_condensers WHERE location = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, locationKey);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("condenser_type");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public void deleteCondenserData(String locationKey) {
        String sql = "DELETE FROM energy_condensers WHERE location = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, locationKey);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public UUID getCondenserOwner(String locationKey) {
        String sql = "SELECT owner_uuid FROM energy_condensers WHERE location = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, locationKey);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return UUID.fromString(rs.getString("owner_uuid"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
