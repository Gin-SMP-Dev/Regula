package miguel.nu.regula.users;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import miguel.nu.regula.Main;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class BedrockNameIndex {
    private static final Gson GSON = new Gson();
    private static final Type SET_TYPE = new TypeToken<Set<String>>(){}.getType();

    private static File storeFile;
    private static final Set<String> names = Collections.synchronizedSet(new HashSet<>());

    private BedrockNameIndex() {}

    public static void init() {
        storeFile = new File(Main.plugin.getDataFolder(), "bedrock_names.json");
        Main.plugin.getDataFolder().mkdirs();
        load();
    }

    public static void add(String name) {
        if (name == null || name.isEmpty()) return;
        names.add(name.toLowerCase(java.util.Locale.ROOT));
        saveAsync();
    }

    public static boolean containsName(String name) {
        if (name == null) return false;
        return names.contains(name.toLowerCase(java.util.Locale.ROOT));
    }

    public static Set<String> all() {
        return new HashSet<>(names);
    }

    private static void load() {
        if (!storeFile.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(storeFile))) {
            Set<String> loaded = GSON.fromJson(br, SET_TYPE);
            if (loaded != null) {
                names.clear();
                names.addAll(loaded);
            }
        } catch (Exception ignored) {}
    }

    private static void saveAsync() {
        if (Main.plugin == null) return;
        Main.plugin.getServer().getScheduler().runTaskAsynchronously(Main.plugin, BedrockNameIndex::saveSync);
    }

    private static void saveSync() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(storeFile))) {
            bw.write(GSON.toJson(names, SET_TYPE));
        } catch (Exception ignored) {}
    }
}