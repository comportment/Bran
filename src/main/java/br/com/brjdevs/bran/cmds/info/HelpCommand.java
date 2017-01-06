package br.com.brjdevs.bran.cmds.info;

import br.com.brjdevs.bran.core.command.*;
import br.com.brjdevs.bran.core.utils.StringUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.awt.*;
import java.util.Comparator;

@RegisterCommand
public class HelpCommand {
	public HelpCommand() {
		CommandManager.addCommand(new CommandBuilder(Category.INFORMATIVE)
				.setAliases("help")
				.setDescription("Gives you information on all the available commands")
				.setName("Help Command")
				.setAction((event) -> {
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
					Color color = event.getOriginGuild().getSelfMember().getColor();
					builder.append("\n")
							.append("**To get help on a command use `" + event.getPrefix() + "[cmd] help`.**");
					MessageEmbed embed = new EmbedBuilder()
							.setDescription(builder.toString())
							.setColor(color == null ? Color.decode("#D68A38") : color)
							.build();
					event.sendMessage(embed).queue();
				})
				.build());
	}
}
