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
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static miguel.nu.regula.utils.NamespaceKey.getNamespacedKey;

@NullMarked
public class NicknameCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (!(source.getExecutor() instanceof Player player)) {
            source.getSender().sendMessage(Component.text("Only a player can run this command"));
            return;
        }
        if (!RoleManager.hasPlayerPermission(player.getUniqueId().toString(), new String[] { "CHANGE_NICKNAME", "CHANGE_OTHER_NICKNAME" })) {
            source.getSender().sendMessage(Component.text("You dont have permission to run this command."));
            return;
        }
        if (args.length == 0) {
            source.getSender().sendMessage(Component.text("Command is executed with /nickname <nickname> or /nickname <player> <nickname>."));
            return;
        }

        if(args.length == 1){
            if(!RoleManager.hasPlayerPermission(player.getUniqueId().toString(), "CHANGE_NICKNAME")){
                source.getSender().sendMessage(Component.text("You dont have permission to change your own nickname. Please use /nickname <player> <nickname>"));
                return;
            }
            String nickname = cleanNickname(args[0]);
            int nicknameLength = getNicknameLength(nickname);

            if(nicknameLength > 16){
                source.getSender().sendMessage(Component.text("Nickname cannot be over 16 characters."));
                return;
            }
            if(nicknameLength < 1){
                source.getSender().sendMessage(Component.text("Nickname cannot be empty."));
                return;
            }
            changePlayerNickname((Player) source.getSender(), nickname);
            player.sendMessage(Component.text("Nickname has been changed!"));
            return;
        }
        else if(args.length == 2){
            if(!RoleManager.hasPlayerPermission(player.getUniqueId().toString(), "CHANGE_OTHER_NICKNAME")) {
                source.getSender().sendMessage(Component.text("You dont have permission to change others nickname. Please use /nickname <nickname>"));
                return;
            }
            if(args[0].equals(source.getSender().getName())){
                source.getSender().sendMessage(Component.text("You cannot change your own nickname with this command. Please use /nickname <nickname>."));
                return;
            }
            String nickname = cleanNickname(args[1]);
            int nicknameLength = getNicknameLength(nickname);

            if(nicknameLength > 16){
                source.getSender().sendMessage(Component.text("Nickname cannot be over 16 characters."));
                return;
            }
            if(nicknameLength < 1){
                source.getSender().sendMessage(Component.text("Nickname cannot be empty."));
                return;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if(target == null){
                player.sendMessage(Component.text(args[0] + " needs to be online!"));
                return;
            }
            changePlayerNickname(target, nickname);
            player.sendMessage(Component.text("Nickname of " + args[0] + " has been changed!"));
            return;
        }

        source.getSender().sendMessage(Component.text("Unknown command argument").color(NamedTextColor.RED));
    }

    static String cleanNickname(String nickname){
        String stripped = nickname
                .replaceAll("\\[[^\\]]*\\]", " ")
                .replace("§", "")
                .replaceAll("\\s+", " ")
                .trim();
        stripped = stripped.replaceFirst(
                "(?i)^((?:(?:&[0-9A-FK-OR]|&x(?:&[0-9A-F]){6}|&#[0-9A-F]{6})\\s*)+)\\s*",
                "$1"
        );

        return stripped;
    }

    static int getNicknameLength(String nickname){
        return nickname
                .replaceAll("(?i)&[0-9A-FK-OR]", "")
                .replaceAll("(?i)&#[0-9A-F]{6}", "")
                .replaceAll("(?i)&x(?:&[0-9A-F]){6}", "").length();

    }

    public static void changePlayerNickname(Player player, String nickname){
        Component current = player.displayName();

        String currentLegacy = LegacyComponentSerializer.legacySection().serialize(current);

        // capture existing prefixes like "§6[MOD] " "[VIP]" (with color) at the start
        java.util.regex.Pattern pat = java.util.regex.Pattern.compile(
                "^(?:\\s*" +
                        "(?:(?:§[0-9A-FK-OR])|(?:§x(?:§[0-9A-Fa-f]){6}))*" + // codes BEFORE [
                        "\\[[^\\]]*]" +                                     // [MOD]
                        "(?:(?:§[0-9A-FK-OR])|(?:§x(?:§[0-9A-Fa-f]){6}))*" + // codes AFTER ], e.g. §r
                        "\\s*" +                                             // spaces after whole prefix
                        ")+",
                java.util.regex.Pattern.CASE_INSENSITIVE
        );
        java.util.regex.Matcher m = pat.matcher(currentLegacy);
        String preservedPrefixLegacy = m.find() ? m.group().replaceAll("\\s+$", "") : "";

        nickname = colorize(nickname);
        Component nickComp = LegacyComponentSerializer.legacySection().deserialize(nickname);

        Component finalComp = preservedPrefixLegacy.isEmpty()
                ? nickComp
                : LegacyComponentSerializer.legacySection()
                .deserialize(preservedPrefixLegacy)
                .append(Component.space())
                .append(nickComp);

        player.displayName(finalComp);
        player.playerListName(finalComp);

        player.getPersistentDataContainer().set(
                getNamespacedKey("NICKNAME"),
                PersistentDataType.STRING,
                nickname
        );
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        String prefix = (args.length >= 1 ? args[0] : "").toLowerCase(Locale.ROOT);

        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .map(name -> name == null ? "" : name) // safety
                .filter(name -> prefix.isEmpty() || name.toLowerCase(Locale.ROOT).startsWith(prefix))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([0-9A-Fa-f]{6})");
    // convert &#RRGGBB + & codes to §-based colors
    public static String colorize(String input) {
        if (input == null || input.isEmpty()) return "";
        // Hex colors: &#RRGGBB -> §x§R§R§G§G§B§B
        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append('§').append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }
        matcher.appendTail(buffer);

        // & -> §
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
}
