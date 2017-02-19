package br.com.brjdevs.steven.bran.core.listeners;

import br.com.brjdevs.steven.bran.Client;
import br.com.brjdevs.steven.bran.core.data.GuildData;
import br.com.brjdevs.steven.bran.core.utils.OtherUtils;
import br.com.brjdevs.steven.bran.core.utils.RestActionSleep;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.requests.RestAction;

import java.util.concurrent.TimeUnit;

public class WordFilterListener extends OptimizedListener<GuildMessageReceivedEvent> {
	
	public Client client;
	
	public WordFilterListener(Client client) {
		super(GuildMessageReceivedEvent.class);
		this.client = client;
	}
	
	private static boolean canManageMessages(TextChannel channel) {
		return channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE);
	}
	
	@Override
	public void event(GuildMessageReceivedEvent event) {
		if (event.getMessage() == null) return;
		if (!canManageMessages(event.getChannel())) return;
		GuildData guildData = client.getData().getDataHolderManager().get().getGuild(event.getGuild(), client.getConfig());
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
			client.getMessenger().sendMessage(event.getChannel(), "**" + OtherUtils.getUser(event.getAuthor()) + "** you can't say that!!").queue(msg -> new RestActionSleep(msg.delete()).sleepAndThen(TimeUnit.SECONDS.toMillis(1), RestAction::queue));
		}
	}
}
