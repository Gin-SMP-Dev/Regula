package miguel.nu.regula;

import miguel.nu.regula.utils.Invensee;
import miguel.nu.regula.utils.Msg;
import miguel.nu.regula.utils.NamespaceKey;
import miguel.nu.regula.utils.Vanish;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import miguel.nu.regula.commands.CommandListener;
import miguel.nu.regula.menus.GuiListener;


public final class Main extends JavaPlugin {
    public static Plugin plugin;
    @Override
    public void onEnable() {
        plugin = this;

        this.saveDefaultConfig();
        NamespaceKey.createNamespaceKeys();
        getServer().getPluginManager().registerEvents(new Msg(), this);
        getServer().getPluginManager().registerEvents(new Vanish(), this);
        getServer().getPluginManager().registerEvents(new Invensee(), this);
        new CommandListener(this);
        new GuiListener(this);
    }

    @Override
    public void onDisable() {

    }
}
