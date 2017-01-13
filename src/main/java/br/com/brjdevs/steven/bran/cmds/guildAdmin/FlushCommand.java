package br.com.brjdevs.steven.bran.cmds.guildAdmin;

import br.com.brjdevs.steven.bran.core.command.Category;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.ICommand;
import br.com.brjdevs.steven.bran.core.quote.Quotes;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;

import java.util.List;
import java.util.stream.Collectors;

public class FlushCommand {
	
	@Command
	public static ICommand flush() {
		return new CommandBuilder(Category.GUILD_ADMINISTRATOR)
				.setAliases("flush")
				.setName("Flush Command")
				.setDescription("Deletes all my messages in the latest 100 messages.")
				.setPrivateAvailable(false)
				.setAction((event) -> {
					if (!event.getOriginGuild().getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_HISTORY)) {
						event.sendMessage("I can't get the latest messages due to a lack of permission. Missing Permission: MESSAGE_HISTORY.").queue();
						return;
					}
					if (!event.getOriginGuild().getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_MANAGE)) {
						event.sendMessage("I must have MESSAGE_MANAGE in order to bulk delete messages in this channel regardless of author.").queue();
						return;
					}
					event.getTextChannel().getHistory().retrievePast(100).queue(history -> {
						List<Message> messages = history.stream()
								.filter(msg -> msg.getAuthor() == event.getJDA().getSelfUser()
										&& !msg.isPinned()).collect(Collectors.toList());
						event.getTextChannel().deleteMessages(messages).complete();
						event.sendMessage(Quotes.SUCCESS, "Deleted " + messages.size() + " messages!").queue();
					});
				})
				.build();
	}
}
