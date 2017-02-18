package br.com.brjdevs.steven.bran.core.listeners;

import br.com.brjdevs.steven.bran.Client;
import br.com.brjdevs.steven.bran.core.data.GuildData;
import br.com.brjdevs.steven.bran.core.utils.OtherUtils;
import br.com.brjdevs.steven.bran.core.utils.RestActionSleep;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.guild.GenericGuildMessageEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.requests.RestAction;

import java.util.concurrent.TimeUnit;

public class WordFilterListener implements EventListener {
	
	public Client client;
	
	public WordFilterListener(Client client) {
		this.client = client;
	}
	
	private static boolean canManageMessages(TextChannel channel) {
		return channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE);
	}

	@Override
	public void onEvent(Event e) {
		if (!(e instanceof GenericGuildMessageEvent)) return;
		GenericGuildMessageEvent event = (GenericGuildMessageEvent) e;
		if (event.getMessage() == null) return;
		if (!canManageMessages(event.getChannel())) return;
		GuildData guildData = client.getData().getDataHolderManager().get().getGuild(event.getGuild(), client.getConfig());
		if (!guildData.isWordFilterEnabled) return;
		boolean bool = false;
		for (String word : guildData.filteredWords) {
			if (event.getMessage().getRawContent().toLowerCase().contains(word.toLowerCase()))
				bool = true;
		}
		if (bool) {
			event.getMessage().delete().queue();
			client.getMessenger().sendMessage(event.getChannel(), "**" + OtherUtils.getUser(event.getAuthor()) + "** you can't say that!!").queue(msg -> new RestActionSleep(msg.delete()).sleepAndThen(TimeUnit.SECONDS.toMillis(1), RestAction::queue));
		}
	}
}
