package br.com.brjdevs.steven.bran.core.managers;

import br.com.brjdevs.steven.bran.core.client.Bran;
import br.com.brjdevs.steven.bran.core.data.GuildData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrefixManager {
	
	public static String getPrefix(String string, GuildData guildData) {
		List<String> prefixes = guildData != null ? guildData.prefixes : new ArrayList<>(Bran.getInstance().getConfig().defaultPrefixes);
		return prefixes.stream()
			    .filter(prefix -> string.length() > prefix.length() && string.startsWith(prefix)
					    && Bran.getInstance().getCommandManager().getCommands().stream()
					    .filter(cmd ->
							    Arrays.stream(cmd.getAliases()).anyMatch(s -> s.equals(string.substring(prefix.length()))))
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
