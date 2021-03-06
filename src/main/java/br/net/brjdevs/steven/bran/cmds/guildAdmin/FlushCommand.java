package br.net.brjdevs.steven.bran.cmds.guildAdmin;

import br.net.brjdevs.steven.bran.core.command.Command;
import br.net.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.net.brjdevs.steven.bran.core.command.enums.Category;
import br.net.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.net.brjdevs.steven.bran.core.managers.Permissions;
import br.net.brjdevs.steven.bran.core.quote.Quotes;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.requests.RestAction;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class FlushCommand {
	
	@Command
	private static ICommand flush() {
		return new CommandBuilder(Category.GUILD_ADMINISTRATOR)
				.setAliases("flush")
				.setName("Flush Command")
				.setDescription("Deletes all my messages in the latest 100 messages.")
				.setPrivateAvailable(false)
				.setRequiredPermission(Permissions.PRUNE_CLEANUP)
				.setAction((event) -> {
					if (!event.getGuild().getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_HISTORY)) {
						event.sendMessage("I can't get the latest messages due to a lack of permission. Missing Permission: MESSAGE_HISTORY.").queue();
						return;
					}
					if (!event.getGuild().getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_MANAGE)) {
						event.sendMessage("I must have MESSAGE_MANAGE in order to bulk delete messages in this channel regardless of author.").queue();
						return;
					}
					event.getTextChannel().getHistory().retrievePast(100).queue(history -> {
						List<Message> messages = history.stream()
								.filter(msg -> msg.getAuthor() == event.getJDA().getSelfUser()
										&& !msg.isPinned()).collect(Collectors.toList());
						if (messages.isEmpty()) {
							event.sendMessage("I didn't send any messages in the latest 100! *but this one, which I'm just deleting...*").queue(message -> message.delete().queueAfter(5, TimeUnit.SECONDS));
							return;
						}
						RestAction<Message> restAction = event.sendMessage(Quotes.SUCCESS, "Deleted " + messages.size() + " messages!");
						if (messages.size() < 2) {
							messages.get(0).delete().queue(success -> restAction.queue(message -> message.delete().queueAfter(5, TimeUnit.SECONDS)));
						} else {
							try {
								event.getTextChannel().deleteMessages(messages).queue(success -> restAction.queue(message -> message.delete().queueAfter(5, TimeUnit.SECONDS)));
							} catch (IllegalArgumentException e) {
								event.sendMessage(e.getMessage()).queue();
							}
						}
					});
				})
				.build();
	}
}
