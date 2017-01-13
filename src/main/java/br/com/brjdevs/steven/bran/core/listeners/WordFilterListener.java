package br.com.brjdevs.steven.bran.core.listeners;

import br.com.brjdevs.steven.bran.core.data.guild.DiscordGuild;
import br.com.brjdevs.steven.bran.core.data.guild.configs.WordFilter;
import br.com.brjdevs.steven.bran.core.utils.RestActionSleep;
import br.com.brjdevs.steven.bran.core.utils.Util;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.guild.GenericGuildMessageEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.requests.RestAction;

import java.util.concurrent.TimeUnit;

public class WordFilterListener implements EventListener {
	
	private static boolean canManageMessages(TextChannel channel) {
		return channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE);
	}

	@Override
	public void onEvent(Event e) {
		if (!(e instanceof GenericGuildMessageEvent)) return;
		GenericGuildMessageEvent event = (GenericGuildMessageEvent) e;
		if (event.getMessage() == null) return;
		if (!canManageMessages(event.getChannel())) return;
		DiscordGuild discordGuild = DiscordGuild.getInstance(event.getGuild());
		WordFilter wordFilter = discordGuild.getWordFilter();
		if (!wordFilter.isEnabled()) return;
		boolean bool = false;
		for (String word : wordFilter.asList()) {
			if (event.getMessage().getRawContent().contains(word))
				bool = true;
		}
		if (bool) {
			event.getMessage().deleteMessage().queue();
			event.getChannel().sendTyping().queue(success ->
				event.getChannel().sendMessage("**" + Util.getUser(event.getAuthor()) + "** you can't say that!!").queue(msg -> new RestActionSleep(msg.deleteMessage()).sleepAndThen(TimeUnit.SECONDS.toMillis(1), RestAction::queue))
			);
		}
	}
}
