package br.com.brjdevs.steven.bran.cmds.fun;

import br.com.brjdevs.steven.bran.core.command.*;
import br.com.brjdevs.steven.bran.core.data.guild.configs.customcommands.CustomCmdsSettings;
import br.com.brjdevs.steven.bran.core.data.guild.configs.customcommands.CustomCommand;
import br.com.brjdevs.steven.bran.core.managers.Permissions;
import br.com.brjdevs.steven.bran.core.quote.Quotes;
import br.com.brjdevs.steven.bran.core.utils.ListBuilder;
import br.com.brjdevs.steven.bran.core.utils.ListBuilder.Format;
import br.com.brjdevs.steven.bran.core.utils.MathUtils;
import br.com.brjdevs.steven.bran.core.utils.StringUtils;
import br.com.brjdevs.steven.bran.core.utils.Util;

import java.util.List;
import java.util.stream.Collectors;

public class CustomCmdsCommand {
	
	@Command
	public static ICommand customCmds() {
		return new TreeCommandBuilder(Category.FUN)
				.setRequiredPermission(Permissions.CUSTOM_CMDS)
				.setAliases("cmds", "com")
				.setName("Custom Commands")
				.setExample("cmds create hello Hello $user")
				.setHelp("cmds ?")
				.setDefault("list")
				.setPrivateAvailable(false)
				.addCommand(new CommandBuilder(Category.FUN)
						.setAliases("create")
						.setName("Custom Command Create")
						.setDescription("Creates Custom Commands!")
						.setArgs("[command Name] [command Answer]")
						.setExample("cmds create hello Hello $user")
						.setAction((event, rawArgs) -> {
							if (!event.getGuild().getCustomCommands().isEnabled) {
								event.sendMessage("You have to enable Custom Commands using `" + event.getPrefix() + "cmds enabled toggle` to use this command!").queue();
								return;
							}
							String[] args = StringUtils.splitArgs(rawArgs, 3);
							String cmdName = args[1];
							if (event.getGuild().getCustomCommands().hasCustomCommand(cmdName)) {
								event.sendMessage(Quotes.FAIL, "This Guild already has a command named **" + cmdName + "**, if you want to add answers to the command use `" + event.getPrefix() + "cmds addanswer " + cmdName + " [answer]`.").queue();
								return;
							}
							String answer = args[2];
							CustomCommand command = new CustomCommand(cmdName, answer, event.getAuthor());
							event.getGuild().getCustomCommands().asList().add(command);
							event.sendMessage("Created Custom Command **" + cmdName + "**!").queue();
						})
						.build())
				.addCommand(new CommandBuilder(Category.FUN)
						.setAliases("addanswer")
						.setName("Custom Command Add Answer")
						.setDescription("Add answers to existent commands")
						.setArgs("[command Name] [command Answer]")
						.setExample("cmds addanswer hello Good Morning $user.")
						.setAction((event, rawArgs) -> {
							if (!event.getGuild().getCustomCommands().isEnabled) {
								event.sendMessage("You have to enable Custom Commands using `" + event.getPrefix() + "cmds enabled toggle` to use this command!").queue();
								return;
							}
							String[] args = StringUtils.splitArgs(rawArgs, 3);
							String cmdName = args[1];
							String newAnswer = args[2];
							if (!event.getGuild().getCustomCommands().hasCustomCommand(cmdName)) {
								event.sendMessage(Quotes.FAIL, "This Guild does not have a Custom Command named **" + cmdName + "**, if you want to create one use `" + event.getPrefix() + "cmds create " + cmdName + (newAnswer.isEmpty() ? " [command Answer]" : " " + newAnswer) + "`.").queue();
								return;
							}
							CustomCommand command = event.getGuild().getCustomCommands().getCustomCommand(cmdName);
							if (!command.getCreatorId().equals(event.getAuthor().getId())
									&& !event.getMember().hasPermission(Permissions.GUILD_MOD, event.getJDA())) {
								event.sendMessage("You can't add responses to this command because you're not its creator or has GUILD_MOD permission.").queue();
								return;
							}
							if (command.getAnswers().contains(newAnswer)) {
								event.sendMessage(Quotes.FAIL, "This Command already has this answer!").queue();
								return;
							}
							command.getAnswers().add(newAnswer);
							event.sendMessage(Quotes.SUCCESS, "Added a new answer for **" + cmdName + "**! This Command currently has " + command.getAnswers().size() + " answers.").queue();
						})
						.build())
				.addCommand(new CommandBuilder(Category.FUN)
						.setAliases("removeanswer", "rmanswer")
						.setName("Custom Command Remove Answer")
						.setDescription("Removes answers from existent commands")
						.setArgs("[commandName] [answer Index]")
						.setExample("cmds rmanswer hello 0")
						.setAction((event, rawArgs) -> {
							if (!event.getGuild().getCustomCommands().isEnabled) {
								event.sendMessage("You have to enable Custom Commands using `" + event.getPrefix() + "cmds enabled toggle` to use this command!").queue();
								return;
							}
							String[] args = StringUtils.splitArgs(rawArgs, 3);
							String cmdName = args[1];
							CustomCommand command = event.getGuild()
									.getCustomCommands().getCustomCommand(cmdName);
							if (command == null) {
								event.sendMessage(Quotes.FAIL, "This Guild does not have a Custom Command named **" + cmdName + "**, if you want to delete a specific answer from a command use `" + event.getPrefix() + "cmds rmanswer [command Name] [answer Index]` and if you want to delete a command use `" + event.getPrefix() + "cmds delete [command Name]`").queue();
								return;
							}
							if (!command.getCreatorId().equals(event.getAuthor().getId())
									&& !event.getMember().hasPermission(Permissions.GUILD_MOD, event.getJDA())) {
								event.sendMessage("You can't delete responses from this command because you're not its owner or has GUILD_MOD permission!").queue();
								return;
							}
							if (!MathUtils.isInteger(args[2])) {
								event.sendMessage("You didn't give me a valid index, so I've chosen the last answer from the command " + cmdName + ".").queue();
								args[2] = String.valueOf(command.getAnswers().size() - 1);
							}
							int index = Integer.parseInt(args[2]);
							command.getAnswers().remove(index);
							event.sendMessage(Quotes.SUCCESS, "Removed answer index `" + index + "` from " + cmdName + ".\n").queue();
						})
						.build())
				.addCommand(new CommandBuilder(Category.FUN)
						.setAliases("delete", "del")
						.setName("Custom Command Delete")
						.setDescription("Deletes Custom Commands from your guild.")
						.setArgs("[command Name]")
						.setExample("cmds del hello")
						.setAction((event, rawArgs) -> {
							if (!event.getGuild().getCustomCommands().isEnabled) {
								event.sendMessage("You have to enable Custom Commands using `" + event.getPrefix() + "cmds enabled toggle` to use this command!").queue();
								return;
							}
							String[] args = StringUtils.splitArgs(rawArgs, 3);
							String cmdName = args[1];
							CustomCommand command = event.getGuild()
									.getCustomCommands().getCustomCommand(cmdName);
							if (command == null) {
								event.sendMessage(Quotes.FAIL, "This Guild does not have a Custom Command named **" + cmdName + "**.").queue();
								return;
							}
							if (!command.getCreatorId().equals(event.getAuthor().getId())
									&& !event.getMember().hasPermission(Permissions.GUILD_MOD, event.getJDA())) {
								event.sendMessage("You can't delete this command because you're not its owner or has GUILD_MOD permission!").queue();
								return;
							}
							event.getGuild().getCustomCommands().asList().remove(command);
							event.sendMessage(Quotes.SUCCESS, "Deleted Custom Command `" + cmdName + "`.").queue();
						})
						.build())
				.addCommand(new TreeCommandBuilder(Category.MISCELLANEOUS)
						.setAliases("enabled")
						.setName("Custom Commands Enabled")
						.setExample("cmds enabled toggle")
						.setHelp("cmds enabled ?")
						.setDefault("check")
						.addCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
								.setAliases("toggle")
								.setName("Custom Command Enable/Disable")
								.setDescription("Toggles the Custom Commands.")
								.setRequiredPermission(Permissions.GUILD_MANAGE)
								.setAction((event) -> {
									event.getGuild().getCustomCommands().isEnabled
											= !event.getGuild().getCustomCommands().isEnabled;
									event.sendMessage(Quotes.SUCCESS,
											(event.getGuild().getCustomCommands().isEnabled ?
													"Enabled Custom Commands for this guild!" :
													"Disabled Custom Commands for this guild!")).queue();
								})
								.build())
						.addCommand(new CommandBuilder(Category.INFORMATIVE)
								.setAliases("check")
								.setName("Custom Command is Enabled/Disabled")
								.setDescription("Tells you if the Custom Commands are enabled or not in this guild.")
								.setAction((event) -> event.sendMessage(event.getGuild().getCustomCommands().isEnabled ? "The Custom Commands are enabled for this server." : "The Custom Commands are disabled for this server.").queue())
								.build())
						.build())
				.addCommand(new CommandBuilder(Category.INFORMATIVE)
						.setAliases("list")
						.setName("Custom Commands List")
						.setDescription("Gives you all the custom commands in the current guild.")
						.setArgs("<page>")
						.setAction((event) -> {
							if (!event.getGuild().getCustomCommands().isEnabled) {
								event.sendMessage("The Custom Commands are disabled for this guild!").queue();
								return;
							}
							CustomCmdsSettings settings = event.getGuild().getCustomCommands();
							if (settings.asList().isEmpty()) {
								event.sendMessage("This guild has no Custom Commands!").queue();
								return;
							}
							int page = MathUtils.parseIntOrDefault(event.getArgs(2)[1], 1);
							if (page == 0) page = 1;
							List<String> cmds = settings.asList().stream()
									.map(cmd -> cmd.getName() + " - Created by " + (cmd.getCreator(event.getJDA()) != null ? Util.getUser(cmd.getCreator(event.getJDA())) : "Unknown (ID:" + cmd.getCreatorId() + ")"))
									.collect(Collectors.toList());
							ListBuilder listBuilder = new ListBuilder(cmds, page, 15);
							listBuilder.setName("Custom Commands For " + event.getOriginGuild().getName())
									.setFooter("Total Custom Commands: " + cmds.size());
							event.sendMessage(listBuilder.format(Format.CODE_BLOCK, "md")).queue();
						})
						.build())
				.addCommand(new CommandBuilder(Category.MISCELLANEOUS)
						.setAliases("rename")
						.setName("Custom Command Rename")
						.setDescription("Renames Custom Commands.")
						.setArgs("[current name] [new name]")
						.setAction((event) -> {
							String oldName = event.getArgs(3)[1];
							CustomCommand command = event.getGuild().getCustomCommands().getCustomCommand(oldName);
							if (command == null) {
								event.sendMessage(String.format("I didn't find any commands named `%s` in this guild!", oldName)).queue();
								return;
							}
							String newName = event.getArgs(4)[2];
							command.rename(newName);
							event.sendMessage(String.format(":ok_hand: Renamed `%s` to `%s`", oldName, newName)).queue();
							
						}).build())
				.build();
	}
}
