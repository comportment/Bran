package br.com.brjdevs.steven.bran.cmds.guildAdmin;

import br.com.brjdevs.steven.bran.core.command.*;
import br.com.brjdevs.steven.bran.core.command.actions.CommandAction;
import br.com.brjdevs.steven.bran.core.managers.Permissions;
import br.com.brjdevs.steven.bran.core.quote.Quotes;
import br.com.brjdevs.steven.bran.core.utils.MathUtils;
import br.com.brjdevs.steven.bran.core.utils.RestActionSleep;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.RestAction;

import java.util.List;
import java.util.stream.Collectors;

public class PruneCommand {
	
	@Command
	private static ICommand prune() {
		return new TreeCommandBuilder(Category.GUILD_ADMINISTRATOR)
				.setAliases("prune")
				.setName("Prune Command")
				.setHelp("prune ?")
				.setPrivateAvailable(false)
				.setDefault("all")
				.setDescription("Delete multiple messages instantly with this command!")
				.onNotFound(CommandAction.REDIRECT)
				.setRequiredPermission(Permissions.PRUNE_CLEANUP)
				.addSubCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
						.setAliases("bot")
						.setName("Prune Bot Command")
						.setDescription("Deletes bot messages in the latest given amount messages.")
						.setArgs(new Argument<>("amount", Integer.class, true))
						.setExample("prune bot 100")
						.setAction((event) -> {
							if (!event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_MANAGE)) {
								event.sendMessage("I can't delete the messages due to a lack of permission. Missing Permission: " + Permission.MESSAGE_MANAGE).queue();
								return;
							}
							Argument argument = event.getArgument("amount");
							int amount = argument.isPresent() ? (int) argument.get() : 100;
							if (!MathUtils.isInRange(amount, 1, 101)) {
								event.sendMessage("I can prune at least 2 or at most 100 messages.").queue();
								return;
							}
							event.getTextChannel().getHistory().retrievePast(amount).queue(messages -> {
								List<Message> filteredMessages = messages.stream().filter(msg -> msg.getAuthor().isBot() && !msg.isPinned()).collect(Collectors.toList());
								if (filteredMessages.isEmpty()) {
									event.sendMessage("No bot messages found in the latest 100 messages.").queue();
									return;
								}
								RestAction<Message> restAction = event.sendMessage(Quotes.SUCCESS, "Deleted " + filteredMessages.size() + " messages.");
								if (filteredMessages.size() < 2) {
									filteredMessages.get(0).deleteMessage().queue(success -> restAction.queue(msg -> new RestActionSleep(msg.deleteMessage()).sleepAndThen(30000, RestAction::queue)));
								} else {
									event.getTextChannel().deleteMessages(filteredMessages).queue(success -> restAction.queue(msg -> new RestActionSleep(msg.deleteMessage()).sleepAndThen(30000, RestAction::queue)));
								}
							});
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
						.setAliases("user")
						.setName("Prune User Command")
						.setDescription("Deletes the given user messages in the latest given amount messages")
						.setArgs(new Argument<>("user", String.class), new Argument<>("amount", Integer.class, true))
						.setExample("prune user 100 <@219186621008838669>")
						.setAction((event) -> {
							if (!event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_MANAGE)) {
								event.sendMessage("I can't delete the messages due to a lack of permission. Missing Permission: " + Permission.MESSAGE_MANAGE).queue();
								return;
							}
							Argument amountArg = event.getArgument("amount");
							int amount = amountArg.isPresent() ? (int) amountArg.get() : 100;
							if (!MathUtils.isInRange(amount, 1, 101)) {
								event.sendMessage("I can prune at least 2 or at most 100 messages.").queue();
								return;
							}
							String strUser = (String) event.getArgument("user").get();
							if (strUser.matches("(<@!?[0-9]{17,18}>)"))
								strUser = strUser.replaceAll("(<@!?|>)", "");
							User user = event.getJDA().getUserById(strUser);
							if (user == null) {
								event.sendMessage("I can't seem to find this user... ").queue();
								return;
							}
							if (!event.getGuild().isMember(user)) {
								event.sendMessage("This user is not a member here!").queue();
								return;
							}
							event.getTextChannel().getHistory().retrievePast(amount).queue(messages -> {
								List<Message> filteredMessages = messages.stream().filter(msg -> msg.getAuthor().equals(user) && !msg.isPinned()).collect(Collectors.toList());
								if (filteredMessages.isEmpty()) {
									event.sendMessage("No messages from this user found in the latest 100 messages.").queue();
									return;
								}
								RestAction<Message> restAction = event.sendMessage(Quotes.SUCCESS, "Deleted " + filteredMessages.size() + " messages.");
								if (filteredMessages.size() < 2) {
									filteredMessages.get(0).deleteMessage().queue(success -> restAction.queue(msg -> new RestActionSleep(msg.deleteMessage()).sleepAndThen(30000, RestAction::queue)));
								} else {
									event.getTextChannel().deleteMessages(filteredMessages).queue(success -> restAction.queue(msg -> new RestActionSleep(msg.deleteMessage()).sleepAndThen(30000, RestAction::queue)));
								}
							});
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
						.setAliases("msg", "content")
						.setName("Prune Content Command")
						.setDescription("Deletes messages with the given content in the latest given amount messages.")
						.setArgs(new Argument<>("content", String.class), new Argument<>("amount", Integer.class, true))
						.setExample("prune content 100 hello")
						.setAction((event) -> {
							if (!event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_MANAGE)) {
								event.sendMessage("I can't delete the messages due to a lack of permission. Missing Permission: " + Permission.MESSAGE_MANAGE).queue();
								return;
							}
							Argument amountArg = event.getArgument("amount");
							int amount = amountArg.isPresent() ? (int) amountArg.get() : 100;
							String content = (String) event.getArgument("content").get();
							if (!MathUtils.isInRange(amount, 1, 101)) {
								event.sendMessage("I can prune at least 2 or at most 100 messages.").queue();
								return;
							}
							event.getTextChannel().getHistory().retrievePast(amount).queue(messages -> {
								List<Message> filteredMessages = messages.stream().filter(msg -> msg.getRawContent().contains(content) && !msg.isPinned()).collect(Collectors.toList());
								if (filteredMessages.isEmpty()) {
									event.sendMessage("No messages found matching the given content.").queue();
									return;
								}
								RestAction<Message> restAction = event.sendMessage(Quotes.SUCCESS, "Deleted " + filteredMessages.size() + " messages.");
								if (filteredMessages.size() < 2) {
									filteredMessages.get(0).deleteMessage().queue(success -> restAction.queue(msg -> new RestActionSleep(msg.deleteMessage()).sleepAndThen(30000, RestAction::queue)));
								} else {
									event.getTextChannel().deleteMessages(filteredMessages).queue(success -> restAction.queue(msg -> new RestActionSleep(msg.deleteMessage()).sleepAndThen(30000, RestAction::queue)));
								}
							});
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
						.setAliases("all")
						.setName("Prune All Command")
						.setDescription("Deletes all messages in the latest given amount messages.")
						.setArgs(new Argument<>("amount", Integer.class, true))
						.setAction((event) -> {
							if (!event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_MANAGE)) {
								event.sendMessage("I can't delete the messages due to a lack of permission. Missing Permission: " + Permission.MESSAGE_MANAGE).queue();
								return;
							}
							Argument amountArg = event.getArgument("amount");
							int amount = amountArg.isPresent() ? (int) amountArg.get() : 100;
							if (!MathUtils.isInRange(amount, 1, 101)) {
								event.sendMessage("I can prune at least 2 or at most 100 messages.").queue();
								return;
							}
							event.getTextChannel().getHistory().retrievePast(amount).queue(messages ->
									event.getTextChannel().deleteMessages(messages).queue(success -> event.sendMessage(Quotes.SUCCESS, "Deleted " + messages.size() + " messages.").queue(msg -> new RestActionSleep(msg.deleteMessage()).sleepAndThen(30000, RestAction::queue))));
						})
						.build())
				.build();
	}
}
