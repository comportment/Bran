package br.com.brjdevs.bran.cmds.guildAdmin;

import br.com.brjdevs.bran.core.command.Category;
import br.com.brjdevs.bran.core.command.CommandBuilder;
import br.com.brjdevs.bran.core.command.CommandManager;
import br.com.brjdevs.bran.core.command.RegisterCommand;
import br.com.brjdevs.bran.core.messageBuilder.AdvancedMessageBuilder.Quote;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;

import java.util.List;
import java.util.stream.Collectors;

@RegisterCommand
public class FlushCommand {
	public FlushCommand() {
		CommandManager.addCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
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
						event.sendMessage(Quote.getQuote(Quote.SUCCESS) + "Deleted " + messages.size() + " messages!").queue();
					});
				})
				.build());
	}
}
