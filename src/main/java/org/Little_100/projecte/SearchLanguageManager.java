package org.Little_100.projecte;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SearchLanguageManager {

    private final ProjectE plugin;
    private Map<String, String> idToNameMap = new HashMap<>();
    private Map<String, String> nameToIdMap = new HashMap<>();

    // 搜索结果缓存
    private final Map<String, Map<String, String>> searchCache = new ConcurrentHashMap<>();

    // 常用搜索词索引
    private final Map<String, Map<String, String>> keywordIndex = new HashMap<>();

    public SearchLanguageManager(ProjectE plugin) {
        this.plugin = plugin;
        loadSearchLanguageFile();
    }

    /**
     * 加载搜索语言文件
     */
    public void loadSearchLanguageFile() {
        idToNameMap.clear();
        nameToIdMap.clear();
        searchCache.clear();
        keywordIndex.clear();

        String fileName = "searchlang/zh_cn.json";
        File langFile = new File(plugin.getDataFolder(), fileName);

        if (!langFile.exists()) {
            File parent = langFile.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            plugin.saveResource(fileName, false);
        }

        try (FileReader reader = new FileReader(langFile, StandardCharsets.UTF_8)) {
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, String>>() {}.getType();
            idToNameMap = gson.fromJson(reader, type);

            for (Map.Entry<String, String> entry : idToNameMap.entrySet()) {
                nameToIdMap.put(entry.getValue(), entry.getKey());
            }

            // 预构建一些关键词索引
            buildKeywordIndex();

            plugin.getLogger().info("成功加载 " + idToNameMap.size() + " 个物品名称映射");
        } catch (Exception e) {
            plugin.getLogger().severe("无法加载搜索语言文件: " + fileName);
            e.printStackTrace();
        }
    }

    /**
     * 构建关键词索引以加速搜索
     */
    private void buildKeywordIndex() {
        Map<String, String> commonTerms = findCommonTerms();

        for (Map.Entry<String, String> term : commonTerms.entrySet()) {
            String chineseTerm = term.getKey();
            String englishTerm = term.getValue();

            Map<String, String> matchingItems = new HashMap<>();

            for (Map.Entry<String, String> item : idToNameMap.entrySet()) {
                String itemId = item.getKey();
                String itemName = item.getValue();

                if (itemId.toLowerCase().contains(englishTerm)
                        || (itemName != null && itemName.toLowerCase().contains(chineseTerm))) {
                    matchingItems.put(itemId, itemName);
                }
            }

            if (!matchingItems.isEmpty()) {
                keywordIndex.put(chineseTerm, matchingItems);
                keywordIndex.put(englishTerm, matchingItems);
            }
        }

        plugin.getLogger().info("已构建 " + keywordIndex.size() + " 个关键词索引");
    }

    public String getLocalizedName(String itemId) {
        return idToNameMap.getOrDefault(itemId, itemId);
    }

    public Map<String, String> findMatchingIds(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return new HashMap<>();
        }

        String lowerSearchTerm = searchTerm.toLowerCase().trim();

        if (searchCache.containsKey(lowerSearchTerm)) {
            return searchCache.get(lowerSearchTerm);
        }

        Map<String, String> results = new HashMap<>();

        if (keywordIndex.containsKey(lowerSearchTerm)) {
            results = keywordIndex.get(lowerSearchTerm);
            searchCache.put(lowerSearchTerm, results);
            return results;
        }

        for (String keyword : keywordIndex.keySet()) {
            if (keyword.contains(lowerSearchTerm) || lowerSearchTerm.contains(keyword)) {
                results.putAll(keywordIndex.get(keyword));
                if (results.size() > 100) {
                    break;
                }
            }
        }

        if (results.size() > 20) {
            searchCache.put(lowerSearchTerm, results);
            return results;
        }

        int count = 0;
        for (Map.Entry<String, String> entry : idToNameMap.entrySet()) {
            if (entry.getValue().toLowerCase().contains(lowerSearchTerm)) {
                results.put(entry.getKey(), entry.getValue());
            }

            if (++count > 500) break;
        }

        if (results.size() < 20) {
            count = 0;
            for (String id : idToNameMap.keySet()) {
                String simplifiedId = id.replaceAll("(item|block)\\.minecraft\\.", "");
                if (simplifiedId.toLowerCase().contains(lowerSearchTerm)) {
                    results.put(id, idToNameMap.get(id));
                }

                if (++count > 500) break;
            }
        }

        searchCache.put(lowerSearchTerm, results);

        return results;
    }

    public boolean matches(String searchTerm, String itemId) {
        if (searchTerm == null || searchTerm.isEmpty() || itemId == null || itemId.isEmpty()) {
            return false;
        }

        String lowerSearchTerm = searchTerm.toLowerCase();

        String localizedName = idToNameMap.get(itemId);
        if (localizedName != null && localizedName.toLowerCase().contains(lowerSearchTerm)) {
            return true;
        }

        String simplifiedId = itemId.replaceAll("(item|block)\\.minecraft\\.", "");
        if (simplifiedId.toLowerCase().contains(lowerSearchTerm)) {
            return true;
        }

        Map<String, String> commonTerms = findCommonTerms();

        for (Map.Entry<String, String> entry : commonTerms.entrySet()) {
            if (lowerSearchTerm.contains(entry.getKey())) {
                if (itemId.toLowerCase().contains(entry.getValue())) {
                    return true;
                }
                if (localizedName != null && localizedName.toLowerCase().contains(entry.getKey())) {
                    return true;
                }
            }
        }

        for (Map.Entry<String, String> entry : commonTerms.entrySet()) {
            if (lowerSearchTerm.contains(entry.getValue())) {
                if (itemId.toLowerCase().contains(entry.getValue())) {
                    return true;
                }
            }
        }

        if (lowerSearchTerm.length() > 1) {
            for (int i = 1; i < lowerSearchTerm.length(); i++) {
                String firstPart = lowerSearchTerm.substring(0, i);
                String secondPart = lowerSearchTerm.substring(i);

                String firstPartEnglish = null;
                String secondPartEnglish = null;

                for (Map.Entry<String, String> entry : commonTerms.entrySet()) {
                    if (firstPart.equals(entry.getKey())) {
                        firstPartEnglish = entry.getValue();
                        break;
                    }
                }

                for (Map.Entry<String, String> entry : commonTerms.entrySet()) {
                    if (secondPart.equals(entry.getKey())) {
                        secondPartEnglish = entry.getValue();
                        break;
                    }
                }

                if (firstPartEnglish != null && secondPartEnglish != null) {
                    if (itemId.toLowerCase().contains(firstPartEnglish)
                            && itemId.toLowerCase().contains(secondPartEnglish)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private Map<String, String> findCommonTerms() {
        Map<String, String> commonTerms = new HashMap<>();
        commonTerms.put("铁", "iron");
        commonTerms.put("金", "gold");
        commonTerms.put("钻石", "diamond");
        commonTerms.put("木", "wood");
        commonTerms.put("石", "stone");
        commonTerms.put("煤", "coal");
        commonTerms.put("青金石", "lapis");
        commonTerms.put("红石", "redstone");
        commonTerms.put("绿宝石", "emerald");
        commonTerms.put("下界", "nether");
        commonTerms.put("末地", "end");
        commonTerms.put("铜", "copper");
        commonTerms.put("苔石", "mossy");
        commonTerms.put("羊毛", "wool");
        commonTerms.put("染色", "stained");
        commonTerms.put("玻璃", "glass");
        commonTerms.put("陶瓦", "terracotta");
        commonTerms.put("混凝土", "concrete");
        commonTerms.put("地毯", "carpet");
        commonTerms.put("沙子", "sand");
        commonTerms.put("沙砾", "gravel");
        commonTerms.put("黏土", "clay");
        commonTerms.put("海绵", "sponge");
        commonTerms.put("雪", "snow");
        commonTerms.put("冰", "ice");
        commonTerms.put("南瓜", "pumpkin");
        commonTerms.put("西瓜", "melon");

        commonTerms.put("剑", "sword");
        commonTerms.put("斧", "axe");
        commonTerms.put("镐", "pickaxe");
        commonTerms.put("锄", "hoe");
        commonTerms.put("锹", "shovel");
        commonTerms.put("头盔", "helmet");
        commonTerms.put("胸甲", "chestplate");
        commonTerms.put("护腿", "leggings");
        commonTerms.put("靴子", "boots");
        commonTerms.put("种子", "seeds");
        commonTerms.put("树苗", "sapling");
        commonTerms.put("花", "flower");
        commonTerms.put("矿石", "ore");
        commonTerms.put("桶", "bucket");
        commonTerms.put("箱子", "chest");
        commonTerms.put("床", "bed");

        return commonTerms;
    }
}
