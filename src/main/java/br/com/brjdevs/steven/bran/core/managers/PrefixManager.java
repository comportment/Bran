package br.com.brjdevs.steven.bran.core.managers;

import br.com.brjdevs.steven.bran.Client;
import br.com.brjdevs.steven.bran.core.data.GuildData;

import java.util.ArrayList;
import java.util.List;

public class PrefixManager {
	
	public static String getPrefix(String string, GuildData guildData, Client client) {
		List<String> prefixes = guildData != null ? guildData.prefixes : new ArrayList<>(client.getConfig().defaultPrefixes);
		return prefixes.stream()
			    .filter(prefix -> string.length() > prefix.length() && string.startsWith(prefix)
					    && client.commandManager.getCommands().stream()
					    .filter(cmd ->
                                cmd.getAliases().contains(string.substring(prefix.length())))
                        .findFirst().orElse(null) != null)
                .findFirst().orElse(null);
    }
	
	public static String getPrefix0(String string, GuildData guildData) {
		return guildData.prefixes.stream()
				.filter(prefix -> string.length() > prefix.length() && string.startsWith(prefix)
		                && guildData.customCommands.containsKey(string.substring(prefix.length())))
			    .findFirst().orElse(null);
    }
}
