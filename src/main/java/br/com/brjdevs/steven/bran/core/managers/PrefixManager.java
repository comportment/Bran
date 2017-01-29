package br.com.brjdevs.steven.bran.core.managers;

import br.com.brjdevs.steven.bran.core.data.guild.DiscordGuild;
import br.com.brjdevs.steven.bran.refactor.BotContainer;

import java.util.ArrayList;
import java.util.List;

public class PrefixManager {
	
	public static String getPrefix(String string, DiscordGuild discordGuild, BotContainer container) {
		List<String> prefixes = discordGuild != null ? discordGuild.getPrefixes() : new ArrayList<>(container.config.getDefaultPrefixes());
		return prefixes.stream()
			    .filter(prefix -> string.length() > prefix.length() && string.startsWith(prefix)
					    && container.commandManager.getCommands().stream()
					    .filter(cmd ->
                                cmd.getAliases().contains(string.substring(prefix.length())))
                        .findFirst().orElse(null) != null)
                .findFirst().orElse(null);
    }
    public static String getPrefix0(String string, DiscordGuild discordGuild) {
	    List<String> prefixes = discordGuild.getPrefixes();
	    return prefixes.stream()
                .filter(prefix -> string.length() > prefix.length() && string.startsWith(prefix)
		                && discordGuild.getCustomCommands().hasCustomCommand(string.substring(prefix.length())))
			    .findFirst().orElse(null);
    }
}
