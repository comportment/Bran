package br.net.brjdevs.steven.bran.cmds.info;

import br.net.brjdevs.steven.bran.core.client.Bran;
import br.net.brjdevs.steven.bran.core.command.Argument;
import br.net.brjdevs.steven.bran.core.command.Command;
import br.net.brjdevs.steven.bran.core.command.CommandManager;
import br.net.brjdevs.steven.bran.core.command.HelpContainer;
import br.net.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.net.brjdevs.steven.bran.core.command.enums.Category;
import br.net.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.net.brjdevs.steven.bran.core.command.interfaces.ITreeCommand;
import br.net.brjdevs.steven.bran.core.utils.Emojis;
import br.net.brjdevs.steven.bran.core.utils.StringUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.awt.*;

public class HelpCommand {
	
	@Command
	private static ICommand help() {
		return new CommandBuilder(Category.INFORMATIVE)
				.setAliases("help")
				.setDescription("Gives you information on all the available commands")
				.setName("Help Command")
                .setArgs(new Argument("command", String.class, true))
                .setAction((event) -> {
					if (event.getGuild() != null && !event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_EMBED_LINKS)) {
						event.sendMessage("I need to have MESSAGE_EMBED_LINKS permission to send this message!").queue();
						return;
					}
                    CommandManager m = Bran.getInstance().getCommandManager();
                    ICommand cmd = null;
                    if (event.getArgument("command").isPresent()) {
                        String rawCmd = ((String) event.getArgument("command").get());
                        do {
                            String[] split = rawCmd.split(" ");
                            if (cmd == null)
                                cmd = m.getCommand(split[0]);
                            else
                                cmd = m.getCommand((ITreeCommand) cmd, split[0]);
                            try {
                                rawCmd = rawCmd.substring(split[0].length() + 1);
                            } catch (Exception ignored) {
                                rawCmd = "";
                            }
                        } while (!rawCmd.isEmpty() && cmd instanceof ITreeCommand);
                        if (cmd == null) {
                            event.sendMessage(Emojis.X + " No commands found matching the following criteria: " + event.getArgument("command").get() + ". Make sure you didn't include prefixes in the command.").queue();
                        } else {
                            event.sendMessage(HelpContainer.getHelp(cmd, event.getMember())).queue();
                        }
                        return;
                    }
                    StringBuilder builder = new StringBuilder();
					for (Category category : Category.values()) {
                        if (m.getCommands(category).isEmpty()) continue;
                        builder.append(category.getEmoji())
								.append(" **| ").append(category.getKey())
								.append("** - ");
                        for (ICommand command : m.getCommands(category))
                            builder.append("`").append(command.getAliases()[0]).append("`").append(", ");
						builder = new StringBuilder(StringUtils.replaceLast(builder.toString(), ", ", ""));
						builder.append('\n');
					}
					Color color = null;
					if (event.getGuild() != null)
						color = event.getGuild().getSelfMember().getColor();
					builder.append("\n**If you need a detailed description on a command [click here](http://bran.readthedocs.io/en/latest/)** *(WIP)*\n")
							.append("**To get help on a command use `").append(event.getPrefix()).append("[cmd] help`.**");
					MessageEmbed embed = new EmbedBuilder()
							.setDescription(builder.toString())
							.setColor(color == null ? Color.decode("#D68A38") : color)
							.build();
					event.sendMessage(embed).queue();
				})
				.build();
	}
}
