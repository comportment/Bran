package br.com.brjdevs.steven.bran.cmds.info;

import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.CommandManager;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.utils.StringUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.awt.*;
import java.util.Comparator;

public class HelpCommand {
	
	@Command
	private static ICommand help() {
		return new CommandBuilder(Category.INFORMATIVE)
				.setAliases("help")
				.setDescription("Gives you information on all the available commands")
				.setName("Help Command")
				.setAction((event) -> {
					if (!event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_EMBED_LINKS)) {
						event.sendMessage("I need to have MESSAGE_EMBED_LINKS permission to send this message!").queue();
						return;
					}
					CommandManager.getCommands().sort(Comparator.comparing(ICommand::getCategory));
					StringBuilder builder = new StringBuilder();
					for (Category category : Category.values()) {
						if (CommandManager.getCommands(category).isEmpty()) continue;
						builder.append(category.getEmoji())
								.append(" **| ").append(category.getKey())
								.append("** - ");
						for (ICommand command : CommandManager.getCommands(category))
							builder.append("`").append(command.getAliases().get(0)).append("`").append(", ");
						builder = new StringBuilder(StringUtils.replaceLast(builder.toString(), ", ", ""));
						builder.append('\n');
					}
					Color color = event.getGuild().getSelfMember().getColor();
					builder.append("If you need a detailed description on each command [click here](http://bran.readthedocs.io/en/latest/) *(WIP)*\n")
							.append("**To get help on a command use `" + event.getPrefix() + "[cmd] help`.**");
					MessageEmbed embed = new EmbedBuilder()
							.setDescription(builder.toString())
							.setColor(color == null ? Color.decode("#D68A38") : color)
							.build();
					event.sendMessage(embed).queue();
				})
				.build();
	}
}
