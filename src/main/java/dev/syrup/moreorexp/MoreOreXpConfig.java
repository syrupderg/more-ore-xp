package dev.syrup.moreorexp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MoreOreXpConfig {

    public boolean modEnabled = true;
    public Map<String, XpRange> customExperience = new LinkedHashMap<>();

    public static class XpRange {
        public int min;
        public int max;

        public XpRange() {}

        public XpRange(int min, int max) {
            this.min = min;
            this.max = max;
        }
    }

    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("moreorexp.json").toFile();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static MoreOreXpConfig INSTANCE;

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, MoreOreXpConfig.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (INSTANCE == null) {
            INSTANCE = new MoreOreXpConfig();
        }

        INSTANCE.ensureDefaults();
        INSTANCE.save();
    }

    public void save() {
        this.customExperience = this.customExperience.entrySet().stream()
                .sorted((e1, e2) -> {
                    String key1 = e1.getKey();
                    String key2 = e2.getKey();
                    String name1 = key1.contains(":") ? key1.split(":")[1] : key1;
                    String name2 = key2.contains(":") ? key2.split(":")[1] : key2;

                    boolean isDeepslate1 = name1.startsWith("deepslate_");
                    boolean isDeepslate2 = name2.startsWith("deepslate_");
                    String base1 = isDeepslate1 ? name1.replace("deepslate_", "") : name1;
                    String base2 = isDeepslate2 ? name2.replace("deepslate_", "") : name2;

                    int comparison = base1.compareTo(base2);
                    if (comparison != 0) return comparison;
                    if (isDeepslate1 == isDeepslate2) return 0;
                    return isDeepslate1 ? 1 : -1;
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ensureDefaults() {
        // UPDATED DEFAULTS HERE
        addIfMissing("minecraft:ancient_debris", 7, 17);
        addIfMissing("minecraft:coal_ore", 0, 2);
        addIfMissing("minecraft:deepslate_coal_ore", 0, 2);
        addIfMissing("minecraft:copper_ore", 3, 7);
        addIfMissing("minecraft:deepslate_copper_ore", 3, 7);
        addIfMissing("minecraft:diamond_ore", 3, 7);
        addIfMissing("minecraft:deepslate_diamond_ore", 3, 7);
        addIfMissing("minecraft:emerald_ore", 3, 7);
        addIfMissing("minecraft:deepslate_emerald_ore", 3, 7);
        addIfMissing("minecraft:iron_ore", 3, 7);
        addIfMissing("minecraft:deepslate_iron_ore", 3, 7);
        addIfMissing("minecraft:gold_ore", 3, 7);
        addIfMissing("minecraft:deepslate_gold_ore", 3, 7);
        addIfMissing("minecraft:lapis_ore", 2, 5);
        addIfMissing("minecraft:deepslate_lapis_ore", 2, 5);
        addIfMissing("minecraft:nether_quartz_ore", 2, 5);
        addIfMissing("minecraft:nether_gold_ore", 0, 1);
        addIfMissing("minecraft:redstone_ore", 1, 5);
        addIfMissing("minecraft:deepslate_redstone_ore", 1, 5);
    }

    private void addIfMissing(String blockId, int min, int max) {
        this.customExperience.putIfAbsent(blockId, new XpRange(min, max));
    }
}