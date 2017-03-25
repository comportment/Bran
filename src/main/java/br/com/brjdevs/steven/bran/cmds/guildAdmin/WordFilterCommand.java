package br.com.brjdevs.steven.bran.cmds.guildAdmin;

import br.com.brjdevs.steven.bran.core.client.Bran;
import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.builders.TreeCommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.managers.Permissions;

import java.util.stream.Collectors;

public class WordFilterCommand {
	
	@Command
	private static ICommand wordFilter() {
		return new TreeCommandBuilder(Category.GUILD_ADMINISTRATOR)
				.setAliases("wordfilter", "wf")
				.setName("WordFilter Command")
				.setHelp("wordfilter ?")
				.setDescription("Don't want people talking trash in your server? This will help you.")
				.setPrivateAvailable(false)
				.setRequiredPermission(Permissions.GUILD_MANAGE)
				.addSubCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
						.setAliases("add")
						.setName("WordFilter Add Command")
						.setDescription("Adds a Word to the WordFilter")
						.setArgs(new Argument("word", String.class))
						.setRequiredPermission(Permissions.GUILD_MANAGE)
						.setAction((event, rawArgs) -> {
                            if (!event.getGuildData(true).isWordFilterEnabled) {
                                event.sendMessage("Before adding words to the WordFilter you have to enable it! Use `" + event.getPrefix() + "wordfilter toggle` to enable.").queue();
								return;
							}
							String word = ((String) event.getArgument("word").get());
                            event.getGuildData(false).filteredWords.add(word);
                            event.sendMessage("\uD83D\uDC4C Added word to the filter! *(Total: " + event.getGuildData(true).filteredWords.size() + ")*").queue();
                            Bran.getInstance().getDataManager().getData().update();
                        })
						.build())
				.addSubCommand(new CommandBuilder(Category.INFORMATIVE)
						.setAliases("list")
						.setName("WordFilter List Command")
						.setDescription("Lists the filtered words in the current guild.")
						.setAction((event) -> {
                            if (!event.getGuildData(true).isWordFilterEnabled) {
                                event.sendMessage("The WordFilter is disabled in this guild.").queue();
								return;
							}
							event.sendPrivate("These are the filtered words in " + event.getGuild().getName()
                                    + ":\n" + (String.join(", ", event.getGuildData(true).filteredWords.stream()
                                    .map(w -> "`" + w + "`").collect(Collectors.toList())))).queue();
							event.sendMessage(
									"I've sent you the filtered words as a private message, check your DMs!")
									.queue();
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
						.setAliases("toggle")
						.setDescription("Enables/Disables the WordFilter.")
						.setName("WordFilter Toggle Command")
						.setRequiredPermission(Permissions.GUILD_MANAGE)
						.setAction((event) -> {
                            boolean isEnabled = event.getGuildData(false).isWordFilterEnabled = !event.getGuildData(true).isWordFilterEnabled;
                            event.sendMessage(isEnabled ? "The WordFilter is now enabled!" : "The WordFilter is no longer enabled.").queue();
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
						.setRequiredPermission(Permissions.GUILD_MANAGE)
						.setAliases("remove")
						.setName("WordFilter Remove Command")
						.setDescription("Removes a word from the WordFilter.")
						.setArgs(new Argument("word", String.class))
						.setAction((event, rawArgs) -> {
                            if (!event.getGuildData(true).isWordFilterEnabled) {
                                event.sendMessage("The WordFilter is disabled in this Guild.").queue();
								return;
							}
							String word = ((String) event.getArgument("word").get());
                            event.getGuildData(false).filteredWords.remove(word);
                            event.sendMessage("\uD83D\uDC4C Removed word from the filter! *(Total: " + event.getGuildData(true).filteredWords.size() + ")*").queue();
                        })
						.build())
				.build();
	}
}