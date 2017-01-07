package br.com.brjdevs.bran.cmds.guildAdmin;

import br.com.brjdevs.bran.core.command.*;
import br.com.brjdevs.bran.core.managers.Permissions;
import br.com.brjdevs.bran.core.utils.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

@RegisterCommand
public class WordFilterCommand {
	public WordFilterCommand() {
		CommandManager.addCommand(new TreeCommandBuilder(Category.GUILD_ADMINISTRATOR)
				.setAliases("wordfilter", "wf")
				.setName("WordFilter Command")
				.setHelp("wordfilter ?")
				.setPrivateAvailable(false)
				.addCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
						.setAliases("add")
						.setName("WordFilter Add Command")
						.setDescription("Adds a Word to the WordFilter")
						.setArgs("<word>")
						.setRequiredPermission(Permissions.GUILD_MANAGE)
						.setAction((event, rawArgs) -> {
							if (!event.getGuild().getWordFilter().isEnabled()) {
								event.sendMessage("Before adding words to the WordFilter you have to enable it! Use `" + event.getPrefix() + "wordfilter toggle` to enable.").queue();
								return;
							}
							String toSplit = StringUtils.splitArgs(rawArgs, 2)[1];
							if (toSplit.isEmpty()) {
								event.sendMessage("Please, you have to give me words to filter!").queue();
								return;
							}
							String[] words = Arrays.stream(toSplit.split("(?<!\\\\),"))
									.filter(w -> !event.getGuild().getWordFilter().asList().contains(w))
									.map(string -> string.trim().replaceAll("\\\\,", ","))
									.toArray(String[]::new);
							if (words.length == 0) {
								event.sendMessage("No words to filter found.").queue();
								return;
							}
							Arrays.stream(words)
									.forEach(word -> event.getGuild().getWordFilter().asList().add(word));
							event.sendMessage("\uD83D\uDC4C Added " + words.length + " word(s) to the filter! *(Total: " + event.getGuild().getWordFilter().asList().size() + ")*").queue();
						})
						.build())
				.addCommand(new CommandBuilder(Category.INFORMATIVE)
						.setAliases("list")
						.setName("WordFilter List Command")
						.setDescription("Lists the filtered words in the current guild.")
						.setAction((event) -> {
							if (!event.getGuild().getWordFilter().isEnabled()) {
								event.sendMessage("The WordFilter is disabled in this guild." + (event.getMember().hasPermission(Permissions.GUILD_MANAGE, event.getJDA()) ? " Use `" + event.getPrefix() + "wf toggle` to enable it." : "")).queue();
								return;
							}
							event.sendPrivate("These are the filtered words in " + event.getOriginGuild().getName()
									+ ":\n" + (String.join(", ", event.getGuild().getWordFilter().asList().stream()
									.map(w -> "`" + w + "`").collect(Collectors.toList())))).queue();
							event.sendMessage(
									"I've sent you the filtered words as a private message, check your DMs!")
									.queue();
						})
						.build())
				.addCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
						.setAliases("toggle")
						.setDescription("Enables/Disables the WordFilter.")
						.setName("WordFilter Toggle Command")
						.setRequiredPermission(Permissions.GUILD_MANAGE)
						.setAction((event) -> {
							event.getGuild().getWordFilter()
									.setEnabled(!event.getGuild().getWordFilter().isEnabled());
							boolean isEnabled = event.getGuild().getWordFilter().isEnabled();
							event.sendMessage(isEnabled ? "The WordFilter is now enabled!" : "The WordFilter is no longer enabled.").queue();
						})
						.build())
				.addCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
						.setRequiredPermission(Permissions.GUILD_MANAGE)
						.setAliases("remove")
						.setName("WordFilter Remove Command")
						.setDescription("Removes a word from the WordFilter.")
						.setAction((event, rawArgs) -> {
							String toSplit = StringUtils.splitArgs(rawArgs, 2)[1];
							if (toSplit.isEmpty()) {
								event.sendMessage("Please, you have to give me words to remove from the filter!").queue();
								return;
							}
							String[] words = Arrays.stream(toSplit.split("(?<!\\\\),"))
									.filter(w -> event.getGuild().getWordFilter().asList().contains(w))
									.map(string -> string.trim().replaceAll("\\\\,", ","))
									.toArray(String[]::new);
							if (words.length == 0) {
								event.sendMessage("No words to remove from the filter found.").queue();
								return;
							}
							Arrays.stream(words)
									.forEach(word -> event.getGuild().getWordFilter().asList().add(word));
							event.sendMessage("\uD83D\uDC4C Removed " + words.length + " word(s) to the filter! *(Total: " + event.getGuild().getWordFilter().asList().size() + ")*").queue();
						})
						.build())
				.build());
	}
}