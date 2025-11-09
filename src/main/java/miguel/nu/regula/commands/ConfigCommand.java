package miguel.nu.regula.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import miguel.nu.regula.Main;
import net.kyori.adventure.text.Component;

import java.util.Collection;
import java.util.List;

public class ConfigCommand implements BasicCommand {
    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (args.length < 1) {
            source.getSender().sendMessage(Component.text("Missing arguments!"));
            return;
        }

        if(args[0].equals("config")){
            if(args[1].equals("reload")){
                Main.plugin.reloadConfig();
                source.getSender().sendMessage(Component.text("Configuration has now been reloaded!"));
            }
        }
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (args.length == 1) {
            return List.of(new String[]{"config"});
        }
        if (args.length == 2) {
            return List.of(new String[]{"reload"});
        }
        return java.util.List.of();
    }
}
