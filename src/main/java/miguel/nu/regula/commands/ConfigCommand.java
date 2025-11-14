package miguel.nu.regula.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import miguel.nu.regula.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ConfigCommand implements BasicCommand {
    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (args.length < 1) {
            source.getSender().sendMessage(Component.text("Missing arguments!"));
            return;
        }

        if(args[0].equals("config")){
            if(!source.getSender().isOp()){
                source.getSender().sendMessage(Component.text("You dont have permission to run this!")
                        .color(NamedTextColor.RED));
                return;
            }
            if(args[1].equals("reload")){
                Main.plugin.reloadConfig();
                source.getSender().sendMessage(Component.text("Configuration has now been reloaded!"));
                return;
            }
        }
        source.getSender().sendMessage(Component.text("Unknown command argument").color(NamedTextColor.RED));
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (args.length == 0 && source.getSender().isOp()) {
            return List.of(new String[]{"config"});
        }
        if (args.length == 1 && Objects.equals(args[0], "config") && source.getSender().isOp()) {
            return List.of(new String[]{"reload"});
        }
        return java.util.List.of();
    }
}
