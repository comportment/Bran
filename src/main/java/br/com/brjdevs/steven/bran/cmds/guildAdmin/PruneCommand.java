package br.com.brjdevs.steven.bran.cmds.guildAdmin;

import br.com.brjdevs.steven.bran.core.command.*;
import br.com.brjdevs.steven.bran.core.managers.Permissions;
import br.com.brjdevs.steven.bran.core.quote.Quotes;
import br.com.brjdevs.steven.bran.core.utils.MathUtils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

import java.util.List;
import java.util.stream.Collectors;

public class PruneCommand {
	
	@Command
	public static ICommand prune() {
		return new TreeCommandBuilder(Category.GUILD_ADMINISTRATOR)
				.setAliases("prune")
				.setName("Prune Command")
				.setHelp("prune ?")
				.setPrivateAvailable(false)
				.setDefault("all")
				.onNotFound(CommandAction.REDIRECT)
				.setRequiredPermission(Permissions.PRUNE_CLEANUP)
				.addCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
						.setAliases("bot")
						.setName("Prune Bot Command")
						.setDescription("Deletes bot messages in the latest given amount messages.")
						.setArgs("[amount]")
						.setExample("prune bot 100")
						.setAction((event) -> {
							if (!event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_MANAGE)) {
								event.sendMessage("I can't delete the messages due to a lack of permission. Missing Permission: " + Permission.MESSAGE_MANAGE).queue();
								return;
							}
							String strAmount = event.getArgs(2)[1];
							int amount = MathUtils.parseIntOrDefault(strAmount, 100);
							if (!MathUtils.isInRange(amount, 1, 101)) {
								event.sendMessage("I can prune at least 2 or at most 100 messages.").queue();
								return;
							}
							event.getTextChannel().getHistory().retrievePast(amount).queue(messages -> {
								List<Message> filteredMessages = messages.stream().filter(msg -> msg.getAuthor().isBot() && !msg.isPinned()).collect(Collectors.toList());
								event.getTextChannel().deleteMessages(filteredMessages).queue(success -> event.sendMessage(Quotes.SUCCESS, "Deleted " + filteredMessages.size() + " messages.").queue());
							});
						})
						.build())
				.addCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
						.setAliases("user")
						.setName("Prune User Command")
						.setDescription("Deletes the given user messages in the latest given amount messages")
						.setArgs("[amount] [user]")
						.setExample("prune user 100 <@219186621008838669>")
						.setAction((event) -> {
							if (!event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_MANAGE)) {
								event.sendMessage("I can't delete the messages due to a lack of permission. Missing Permission: " + Permission.MESSAGE_MANAGE).queue();
								return;
							}
							String strAmount = event.getArgs(3)[1];
							int amount = MathUtils.parseIntOrDefault(strAmount, 100);
							if (!MathUtils.isInRange(amount, 1, 101)) {
								event.sendMessage("I can prune at least 2 or at most 100 messages.").queue();
								return;
							}
							String strUser = event.getArgs(3)[2];
							if (strUser.matches("(<@!?[0-9]{17,18}>)"))
								strUser = strUser.replaceAll("(<@!?|>)", "");
							User user = event.getJDA().getUserById(strUser);
							if (user == null) {
								event.sendMessage("I can't seem to find this user... ").queue();
								return;
							}
							if (!event.getOriginGuild().isMember(user)) {
								event.sendMessage("This user is not a member here!").queue();
								return;
							}
							event.getTextChannel().getHistory().retrievePast(amount).queue(messages -> {
								List<Message> filteredMessages = messages.stream().filter(msg -> msg.getAuthor().equals(user) && !msg.isPinned()).collect(Collectors.toList());
								event.getTextChannel().deleteMessages(filteredMessages).queue(success -> event.sendMessage(Quotes.SUCCESS, "Deleted " + filteredMessages.size() + " messages.").queue());
							});
						})
						.build())
				.addCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
						.setAliases("msg", "content")
						.setName("Prune Content Command")
						.setDescription("Deletes messages with the given content in the latest given amount messages.")
						.setArgs("[amount] [content]")
						.setExample("prune content 100 hello")
						.setAction((event) -> {
							if (!event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_MANAGE)) {
								event.sendMessage("I can't delete the messages due to a lack of permission. Missing Permission: " + Permission.MESSAGE_MANAGE).queue();
								return;
							}
							String strAmount = event.getArgs(3)[1];
							int amount = MathUtils.parseIntOrDefault(strAmount, 100);
							String content = event.getArgs(3)[2];
							if (content.isEmpty()) {
								event.sendMessage("You have to tell me a phrase to find.").queue();
								return;
							}
							if (!MathUtils.isInRange(amount, 1, 101)) {
								event.sendMessage("I can prune at least 2 or at most 100 messages.").queue();
								return;
							}
							event.getTextChannel().getHistory().retrievePast(amount).queue(messages -> {
								List<Message> filteredMessages = messages.stream().filter(msg -> msg.getRawContent().contains(content) && !msg.isPinned()).collect(Collectors.toList());
								event.getTextChannel().deleteMessages(filteredMessages).queue(success -> event.sendMessage(Quotes.SUCCESS, "Deleted " + filteredMessages.size() + " messages.").queue());
							});
						})
						.build())
				.addCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
						.setAliases("all")
						.setName("Prune All Command")
						.setDescription("Deletes all messages in the latest given amount messages.")
						.setArgs("[amount]")
						.setAction((event) -> {
							if (!event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_MANAGE)) {
								event.sendMessage("I can't delete the messages due to a lack of permission. Missing Permission: " + Permission.MESSAGE_MANAGE).queue();
								return;
							}
							String strAmount = event.getArgs(3)[1];
							int amount = MathUtils.parseIntOrDefault(strAmount, 100);
							if (!MathUtils.isInRange(amount, 1, 101)) {
								event.sendMessage("I can prune at least 2 or at most 100 messages.").queue();
								return;
							}
							event.getTextChannel().getHistory().retrievePast(amount).queue(messages ->
									event.getTextChannel().deleteMessages(messages).queue(success -> event.sendMessage(Quotes.SUCCESS, "Deleted " + messages.size() + " messages.").queue()));
						})
						.build())
				.build();
	}
}
