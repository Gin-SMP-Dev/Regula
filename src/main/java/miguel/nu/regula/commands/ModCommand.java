package miguel.nu.regula.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import miguel.nu.regula.menus.AdminMenu;
import miguel.nu.regula.menus.player.PlayerMenu;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

@NullMarked
public class ModCommand implements BasicCommand {
    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (args.length < 1) {
            if(!(source.getExecutor() instanceof Player player)){
                source.getSender().sendMessage(Component.text("Only players can execute this command"));
                return;
            }
            AdminMenu.open((Player)source.getExecutor());
            return;
        }

        String name = args[0];
        if(name.length() > 16){
            source.getSender().sendMessage(Component.text("Player username cannot be over 16 character"));
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(name);
        //TODO FIX
        //if (target.) {
        //    source.getSender().sendMessage(Component.text("Player not found: " + name));
        //    return;
        //}

        // Your logic here
        if(!(source.getExecutor() instanceof Player player)){
            source.getSender().sendMessage(Component.text("Only players can execute this command"));
            return;
        }
        PlayerMenu.open((Player)source.getExecutor(), target);
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(prefix))
                    .sorted()
                    .collect(Collectors.toList());
        }
        return java.util.List.of();
    }
}
