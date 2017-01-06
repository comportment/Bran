package br.com.brjdevs.bran.core;

import br.com.brjdevs.bran.Bot;
import br.com.brjdevs.bran.core.command.CommandManager;
import br.com.brjdevs.bran.core.data.guild.DiscordGuild;

import java.util.Arrays;
import java.util.List;

public class PrefixManager {
    public static String getPrefix(String string, DiscordGuild discordGuild) {
        List<String> prefixes = discordGuild != null ? discordGuild.getPrefixes() : Arrays.asList(Bot.getInstance().getDefaultPrefixes());
        return prefixes.stream()
                .filter(prefix -> string.length() > prefix.length() && string.startsWith(prefix)
                        && CommandManager.getCommands().stream()
                        .filter(cmd ->
                                cmd.getAliases().contains(string.substring(prefix.length())))
                        .findFirst().orElse(null) != null)
                .findFirst().orElse(null);
    }
    public static String getPrefix0(String string, DiscordGuild discordGuild) {
        List<String> prefixes = discordGuild != null ? discordGuild.getPrefixes() : Arrays.asList(Bot.getInstance().getDefaultPrefixes());
        return prefixes.stream()
                .filter(prefix -> string.length() > prefix.length() && string.startsWith(prefix)
                        && discordGuild.getCustomCommands().getCustomCommand(string.substring(prefix.length())) != null)
                .findFirst().orElse(null);
    }
}
