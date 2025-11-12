package miguel.nu.regula.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import miguel.nu.regula.Main;
import miguel.nu.regula.menus.AdminMenu;
import miguel.nu.regula.menus.player.PlayerMenu;
import miguel.nu.regula.roles.RoleManager;
import miguel.nu.regula.users.Minecraft;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NullMarked;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static miguel.nu.regula.utils.NamespaceKey.getNamespacedKey;

@NullMarked
public class NicknameCommand implements BasicCommand {
    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (!(source.getExecutor() instanceof Player player)) {
            source.getSender().sendMessage(Component.text("Only a player can run this command"));
            return;
        }
        if (!RoleManager.hasPlayerPermission(player.getUniqueId().toString(), "CHANGE_NICKNAME")) {
            source.getSender().sendMessage(Component.text("You dont have permission to run this command."));
            return;
        }
        if (args.length == 0) {
            source.getSender().sendMessage(Component.text("Command is executed with /nickname <nickname>."));
            return;
        }

        String rawInput = String.join(" ", args).trim();
        String stripped = rawInput.replaceAll("\\[[^\\]]*\\]", " ").replace("§", "").replaceAll("\\s+", " ").trim();
        String lengthCheck = stripped
                .replaceAll("(?i)&[0-9A-FK-OR]", "")
                .replaceAll("(?i)&#[0-9A-F]{6}", "")
                .replaceAll("(?i)&x(?:&[0-9A-F]){6}", "");
        if (lengthCheck.isEmpty()) {
            source.getSender().sendMessage(Component.text("Nickname cannot be empty."));
            return;
        }
        if (lengthCheck.length() > 16) {
            source.getSender().sendMessage(Component.text("Nickname cannot be over 16 characters."));
            return;
        }

        Component current = player.displayName();
        String currentLegacy = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(current);

        java.util.regex.Pattern pat = java.util.regex.Pattern.compile(
                "^(?:\\s*(?:(?:§[0-9A-FK-OR])|(?:§x(?:§[0-9A-Fa-f]){6}))*\\[[^\\]]*]\\s*)+",
                java.util.regex.Pattern.CASE_INSENSITIVE
        );
        java.util.regex.Matcher m = pat.matcher(currentLegacy);
        String preservedPrefixLegacy = m.find() ? m.group().replaceAll("\\s+$", "") : "";

        Component nickComp = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize(stripped);

        Component finalComp = preservedPrefixLegacy.isEmpty()
                ? nickComp
                : net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection()
                .deserialize(preservedPrefixLegacy)
                .append(net.kyori.adventure.text.Component.space())
                .append(nickComp);

        player.displayName(finalComp);
        player.playerListName(finalComp);

        player.getPersistentDataContainer().set(getNamespacedKey("NICKNAME"),
                org.bukkit.persistence.PersistentDataType.STRING, stripped);

        player.sendMessage(Component.text("Nickname has been changed!"));
    }


}
