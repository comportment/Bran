package br.com.brjdevs.steven.bran.cmds.fun;

import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.builders.TreeCommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.managers.CustomCommand;
import br.com.brjdevs.steven.bran.core.managers.Permissions;
import br.com.brjdevs.steven.bran.core.quote.Quotes;
import br.com.brjdevs.steven.bran.core.utils.StringListBuilder;
import br.com.brjdevs.steven.bran.core.utils.StringListBuilder.Format;
import br.com.brjdevs.steven.bran.core.utils.Utils;

import java.util.List;
import java.util.stream.Collectors;

public class CustomCmdsCommand {
	
	@Command
	private static ICommand customCmds() {
		return new TreeCommandBuilder(Category.FUN)
				.setRequiredPermission(Permissions.CUSTOM_CMDS)
				.setAliases("cmds", "com")
				.setName("Custom Commands")
				.setExample("cmds create hello Hello %user%")
				.setHelp("cmds ?")
				.setDescription("Create Simple or Advanced custom commands, you choose!")
				.setDefault("list")
				.setPrivateAvailable(false)
				.addSubCommand(new CommandBuilder(Category.FUN)
						.setAliases("create")
						.setName("Custom Command Create")
						.setDescription("Creates Custom Commands!")
						.setArgs(new Argument("name", String.class), new Argument("answer", String.class))
						.setExample("cmds create hello Hello %user%")
						.setAction((event) -> {
							String cmdName = ((String) event.getArgument("name").get()).toLowerCase();
							if (event.getGuildData().customCommands.containsKey(cmdName)) {
								event.sendMessage(Quotes.FAIL, "This Guild already has a command named **" + cmdName + "**, if you want to add answers to the command use `" + event.getPrefix() + "cmds addanswer " + cmdName + " [answer]`.").queue();
								return;
							}
							String answer = (String) event.getArgument("answer").get();
							CustomCommand command = new CustomCommand(answer, event.getAuthor());
							event.getGuildData().customCommands.put(cmdName, command);
							event.sendMessage("Created Custom Command **" + cmdName + "**!").queue();
							event.getClient().getDiscordBotData().getDataHolderManager().update();
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.FUN)
						.setAliases("addanswer")
						.setName("Custom Command Add Answer")
						.setDescription("Add answers to existent commands")
						.setArgs(new Argument("name", String.class), new Argument("answer", String.class))
						.setExample("cmds addanswer hello Good Morning %user%.")
						.setAction((event) -> {
							String cmdName = (String) event.getArgument("name").get();
							String newAnswer = (String) event.getArgument("answer").get();
							if (!event.getGuildData().customCommands.containsKey(cmdName)) {
								event.sendMessage(Quotes.FAIL, "This Guild does not have a Custom Command named **" + cmdName + "**, if you want to create one use `" + event.getPrefix() + "cmds create " + cmdName + (newAnswer.isEmpty() ? " [command Answer]" : " " + newAnswer) + "`.").queue();
								return;
							}
							CustomCommand command = event.getGuildData().customCommands.get(cmdName);
							if (!command.getCreatorId().equals(event.getAuthor().getId())
									&& !event.getGuildData().hasPermission(event.getAuthor(), Permissions.GUILD_MOD)) {
								event.sendMessage("You can't add responses to this command because you're not its creator or has GUILD_MOD permission.").queue();
								return;
							}
							if (command.getAnswers().contains(newAnswer)) {
								event.sendMessage(Quotes.FAIL, "This Command already has this answer!").queue();
								return;
							}
							command.getAnswers().add(newAnswer);
							event.sendMessage(Quotes.SUCCESS, "Added a new answer for **" + cmdName + "**! This Command currently has " + command.getAnswers().size() + " answers.").queue();
							event.getClient().getDiscordBotData().getDataHolderManager().update();
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.FUN)
						.setAliases("removeanswer", "rmanswer")
						.setName("Custom Command Remove Answer")
						.setDescription("Removes answers from existent commands")
						.setArgs(new Argument("name", String.class), new Argument("answer index", Integer.class, true))
						.setExample("cmds rmanswer hello 0")
						.setAction((event, rawArgs) -> {
							String cmdName = (String) event.getArgument("name").get();
							CustomCommand command = event.getGuildData().customCommands.get(cmdName);
							if (command == null) {
								event.sendMessage(Quotes.FAIL, "This Guild does not have a Custom Command named **" + cmdName + "**, if you want to delete a specific answer from a command use `" + event.getPrefix() + "cmds rmanswer [command Name] [answer Index]` and if you want to delete a command use `" + event.getPrefix() + "cmds delete [command Name]`").queue();
								return;
							}
							if (!command.getCreatorId().equals(event.getAuthor().getId())
									&& !event.getGuildData().hasPermission(event.getAuthor(), Permissions.GUILD_MOD)) {
								event.sendMessage("You can't delete responses from this command because you're not its owner or has GUILD_MOD permission!").queue();
								return;
							}
							Argument argument = event.getArgument("answer index");
							int index;
							if (!argument.isPresent()) {
								event.sendMessage("You didn't give me a valid index, so I've chosen the last answer from the command " + cmdName + ".").queue();
								index = command.getAnswers().size() - 1;
							} else
								index = (int) argument.get();
							command.getAnswers().remove(index);
							event.sendMessage(Quotes.SUCCESS, "Removed answer index `" + index + "` from " + cmdName + ".\n").queue();
							event.getClient().getDiscordBotData().getDataHolderManager().update();
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.FUN)
						.setAliases("delete", "del")
						.setName("Custom Command Delete")
						.setDescription("Deletes Custom Commands from your guild.")
						.setArgs(new Argument("name", String.class))
						.setExample("cmds del hello")
						.setAction((event) -> {
							String cmdName = (String) event.getArgument("name").get();
							CustomCommand command = event.getGuildData().customCommands.get(cmdName);
							if (command == null) {
								event.sendMessage(Quotes.FAIL, "This Guild does not have a Custom Command named **" + cmdName + "**.").queue();
								return;
							}
							if (!command.getCreatorId().equals(event.getAuthor().getId())
									&& !event.getGuildData().hasPermission(event.getAuthor(), Permissions.GUILD_MOD)) {
								event.sendMessage("You can't delete this command because you're not its owner or has GUILD_MOD permission!").queue();
								return;
							}
							event.getGuildData().customCommands.remove(cmdName);
							event.sendMessage(Quotes.SUCCESS, "Deleted Custom Command `" + cmdName + "`.").queue();
							event.getClient().getDiscordBotData().getDataHolderManager().update();
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.INFORMATIVE)
						.setAliases("list")
						.setName("Custom Commands List")
						.setDescription("Gives you all the custom commands in the current guild.")
						.setArgs(new Argument("page", Integer.class, true))
						.setAction((event) -> {
							if (event.getGuildData().customCommands.isEmpty()) {
								event.sendMessage("No Custom Commands in this Guild.").queue();
								return;
							}
							int page = event.getArgument("page").isPresent() && (int) event.getArgument("page").get() > 0 ? (int) event.getArgument("page").get() : 1;
							List<String> cmds = event.getGuildData().customCommands.entrySet().stream()
									.map(entry -> entry.getKey() + " - Created by " + (entry.getValue().getCreator(event.getJDA()) != null ? Utils.getUser(entry.getValue().getCreator(event.getJDA())) : "Unknown (ID:" + entry.getValue().getCreatorId() + ")"))
									.collect(Collectors.toList());
							StringListBuilder listBuilder = new StringListBuilder(cmds, page, 15);
							listBuilder.setName("Custom Commands For " + event.getGuild().getName())
									.setFooter("Total Custom Commands: " + cmds.size());
							event.sendMessage(listBuilder.format(Format.CODE_BLOCK)).queue();
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.MISCELLANEOUS)
						.setAliases("rename")
						.setName("Custom Command Rename")
						.setDescription("Renames Custom Commands.")
						.setArgs(new Argument("old name", String.class), new Argument("new name", String.class))
						.setAction((event) -> {
							String oldName = ((String) event.getArgument("old name").get()).toLowerCase();
							CustomCommand command = event.getGuildData().customCommands.get(oldName);
							if (command == null) {
								event.sendMessage(String.format("I didn't find any commands named `%s` in this guild!", oldName)).queue();
								return;
							}
							String newName = ((String) event.getArgument("new name").get()).toLowerCase();
							if (newName.contains(" ")) {
								event.sendMessage("The new name cannot contain spaces.").queue();
								return;
							}
							if (event.getGuildData().customCommands.containsKey(newName)) {
								event.sendMessage("This guild already has a Command named `" + newName + "`.").queue();
								return;
							}
							event.getGuildData().customCommands.remove(oldName);
							event.getGuildData().customCommands.put(newName, command);
							event.sendMessage(String.format(":ok_hand: Renamed `%s` to `%s", oldName, newName)).queue();
							event.getClient().getDiscordBotData().getDataHolderManager().update();
							
						}).build())
				.build();
	}
}
