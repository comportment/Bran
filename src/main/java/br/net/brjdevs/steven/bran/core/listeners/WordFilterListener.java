package br.net.brjdevs.steven.bran.core.listeners;

import br.net.brjdevs.steven.bran.core.client.Bran;
import br.net.brjdevs.steven.bran.core.data.GuildData;
import br.net.brjdevs.steven.bran.core.managers.Messenger;
import br.net.brjdevs.steven.bran.core.utils.Utils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.concurrent.TimeUnit;

public class WordFilterListener extends EventListener<GuildMessageReceivedEvent> {
	
	public WordFilterListener() {
		super(GuildMessageReceivedEvent.class);
	}
	
	@Override
	public void onEvent(GuildMessageReceivedEvent event) {
		if (event.getAuthor().equals(event.getJDA().getSelfUser()))
			return;
		if (!event.getChannel().getGuild().getSelfMember().hasPermission(event.getChannel(), Permission.MESSAGE_MANAGE))
			return;
        GuildData guildData = Bran.getInstance().getDataManager().getData().get().getGuildData(event.getGuild(), true);
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
			Messenger.sendMessage(event.getChannel(), "**" + Utils.getUser(event.getAuthor()) + "** you can't say that!!").queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
		}
	}
}
