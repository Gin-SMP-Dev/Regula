package miguel.nu.regula;

import miguel.nu.regula.users.BedrockJoinListener;
import miguel.nu.regula.users.BedrockNameIndex;
import miguel.nu.regula.utils.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import miguel.nu.regula.commands.CommandListener;
import miguel.nu.regula.menus.GuiListener;


public final class Main extends JavaPlugin {
    public static Plugin plugin;
    public static LuckyPerms luckyPerms;
    @Override
    public void onEnable() {
        plugin = this;

        this.saveDefaultConfig();
        NamespaceKey.createNamespaceKeys();
        BedrockNameIndex.init();
        BedrockJoinListener.seedFromOfflineCache();

        luckyPerms = new LuckyPerms();

        getServer().getPluginManager().registerEvents(new BedrockJoinListener(), this);
        getServer().getPluginManager().registerEvents(new Msg(), this);
        getServer().getPluginManager().registerEvents(new Vanish(), this);
        getServer().getPluginManager().registerEvents(new Invensee(), this);
        getServer().getPluginManager().registerEvents(new GraveBreak(), this);
        getServer().getPluginManager().registerEvents(new Prefix(), this);
        getServer().getPluginManager().registerEvents(new Join(), this);

        Mute.init();
        new CommandListener(this);
        new GuiListener(this);
    }

    @Override
    public void onDisable() {

    }
}
