package br.net.brjdevs.steven.bran.core.command;

import br.net.brjdevs.steven.bran.core.command.enums.Category;
import br.net.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.net.brjdevs.steven.bran.core.command.interfaces.ITreeCommand;
import br.net.brjdevs.steven.bran.core.managers.Permissions;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class HelpContainer {
	
	private static Map<ICommand, MessageEmbed> map = new HashMap<>();
	
	public static void generateHelp(ICommand command) {
		EmbedBuilder builder = new EmbedBuilder();
		String desc = "";
		desc += command.getCategory().getEmoji() + " **| " + command.getCategory().getKey() + "**\n**Command:** " + command.getName() + "\n";
		desc += "**Description:** " + command.getDescription() + "\n";
		if (command.getArguments() != null) {
			desc += "**Arguments:** " + (command.getArguments().length != 0 ? (String.join(" ", Arrays.stream(command.getArguments()).map(arg -> (arg.isOptional() ? "<" : "[") + arg.getType().getSimpleName() + ": " + arg.getName() + (arg.isOptional() ? ">" : "]")).toArray(String[]::new))) : "No arguments required.") + '\n';
			desc += "            *Please note: do **NOT** include <> or []*\n";
		}
		desc += "**Required Permission(s):** " + String.join(", ", Permissions.toCollection(command.getRequiredPermission())) + "\n";
		if (command instanceof ITreeCommand) {
			desc += "**Parameters**:\n";
			Set<Category> categories = ((ITreeCommand) command).getSubCommands().stream().map(ICommand::getCategory).collect(Collectors.toSet());
			for (Category category : categories) {
				List<ICommand> commands = ((ITreeCommand) command).getSubCommands().stream().filter(cmd -> cmd.getCategory() == category).collect(Collectors.toList());
				if (commands.isEmpty()) continue;
				desc += category.getEmoji() + " **| " + category.getKey() + "**\n";
				for (ICommand cmd : commands)
					desc += "          **" + cmd.getAliases()[0] + "** " + (cmd.getArguments() != null ? (String.join(" ", Arrays.stream(cmd.getArguments()).map(arg -> (arg.isOptional() ? "<" : "[") + arg.getType().getSimpleName() + ": " + arg.getName() + (arg.isOptional() ? ">" : "]")).toArray(String[]::new))) : "") + " - " + (cmd instanceof ITreeCommand ? "Use `" + cmd.getHelp() + "` to get help on this command!" : cmd.getDescription()) + "\n";
				desc += '\n';
			}
		}
		if (command.getExample() != null)
			desc += "**Example:** " + command.getExample();
		builder.setDescription(desc);
		map.put(command, builder.build());
	}
	
	public static MessageEmbed getHelp(ICommand command, Member m) {
		return new EmbedBuilder(map.get(command)).setColor(m != null && m.getColor() != null ? m.getColor() : Color.decode("#D68A38")).build();
	}
}
