package miguel.nu.regula.commands;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.Plugin;

public class CommandListener {
    public CommandListener(Plugin plugin) {
        LifecycleEventManager<Plugin> lifecycleEventManager = plugin.getLifecycleManager();

        lifecycleEventManager.registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register("mod", new ModCommand());
            commands.registrar().register("regula", new ConfigCommand());
            commands.registrar().register("nickname", new NicknameCommand());
        });
    }
}
