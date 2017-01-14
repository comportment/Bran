package br.com.brjdevs.steven.bran.cmds.guildAdmin;

import br.com.brjdevs.steven.bran.core.command.Category;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.ICommand;
import br.com.brjdevs.steven.bran.core.quote.Quotes;
import br.com.brjdevs.steven.bran.core.utils.RestActionSleep;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.requests.RestAction;

import java.util.List;
import java.util.stream.Collectors;

public class FlushCommand {
	
	@Command
	private static ICommand flush() {
		return new CommandBuilder(Category.GUILD_ADMINISTRATOR)
				.setAliases("flush")
				.setName("Flush Command")
				.setDescription("Deletes all my messages in the latest 100 messages.")
				.setPrivateAvailable(false)
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
							event.sendMessage("I didn't send any messages in the latest 100! *but this one, which I'm just deleting...*").queue(message -> new RestActionSleep(message.deleteMessage()).sleepAndThen(30000, RestAction::queue));
							return;
						}
						RestAction<Message> restAction = event.sendMessage(Quotes.SUCCESS, "Deleted " + messages.size() + " messages!");
						if (messages.size() < 2) {
							messages.get(0).deleteMessage().queue(success -> restAction.queue(message -> new RestActionSleep(message.deleteMessage()).sleepAndThen(30000, RestAction::queue)));
						} else {
							event.getTextChannel().deleteMessages(messages).queue(success -> restAction.queue(message -> new RestActionSleep(message.deleteMessage()).sleepAndThen(30000, RestAction::queue)));
						}
					});
				})
				.build();
	}
}
