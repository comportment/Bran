package br.com.brjdevs.steven.bran.core.listeners;

import br.com.brjdevs.steven.bran.core.client.Client;
import br.com.brjdevs.steven.bran.core.data.GuildData;
import br.com.brjdevs.steven.bran.core.utils.RestActionSleep;
import br.com.brjdevs.steven.bran.core.utils.Utils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.requests.RestAction;

import java.util.concurrent.TimeUnit;

public class WordFilterListener extends EventListener<GuildMessageReceivedEvent> {
	
	public WordFilterListener(Client client) {
		super(GuildMessageReceivedEvent.class, client);
	}
	
	private static boolean canManageMessages(TextChannel channel) {
		return channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE);
	}
	
	@Override
	public void event(GuildMessageReceivedEvent event) {
		if (event.getMessage() == null) return;
		if (!canManageMessages(event.getChannel())) return;
		GuildData guildData = client.getDiscordBotData().getDataHolderManager().get().getGuild(event.getGuild());
		if (!guildData.isWordFilterEnabled) return;
		boolean hasFilteredWord = false;
		for (String word : guildData.filteredWords) {
			if (event.getMessage().getRawContent().toLowerCase().contains(word.toLowerCase())) {
				hasFilteredWord = true;
				break;
			}
		}
		if (hasFilteredWord) {
			event.getMessage().delete().queue();
			client.getMessenger().sendMessage(event.getChannel(), "**" + Utils.getUser(event.getAuthor()) + "** you can't say that!!").queue(msg -> new RestActionSleep(msg.delete()).sleepAndThen(TimeUnit.SECONDS.toMillis(1), RestAction::queue));
		}
	}
}
