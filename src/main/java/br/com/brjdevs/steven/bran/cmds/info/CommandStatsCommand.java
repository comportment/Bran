package br.com.brjdevs.steven.bran.cmds.info;

import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.CommandEvent;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import net.dv8tion.jda.core.EmbedBuilder;

import java.awt.*;

import static br.com.brjdevs.steven.bran.core.command.CommandStatsManager.*;

public class CommandStatsCommand {
	
	@Command
	private static ICommand stats() {
		return new CommandBuilder(Category.INFORMATIVE)
				.setAliases("cmdstats")
				.setDescription("Shows you the most used commands in this session!")
				.setArgs(new Argument<>("what", String.class, true))
				.setName("Command Stats Command")
				.setAction((event) -> {
					Argument<String> whatArg = (Argument<String>) event.getArgument("what");
					if (whatArg.isPresent()) {
						String what = whatArg.get();
						if (what.equals("total")) {
							event.sendMessage(fillEmbed(TOTAL_CMDS, baseEmbed(event, "Command Stats | Total")).build()).queue();
							return;
						}
						
						if (what.equals("daily")) {
							event.sendMessage(fillEmbed(DAY_CMDS, baseEmbed(event, "Command Stats | Daily")).build()).queue();
							return;
						}
						
						if (what.equals("hourly")) {
							event.sendMessage(fillEmbed(HOUR_CMDS, baseEmbed(event, "Command Stats | Hourly")).build()).queue();
							return;
						}
						
						if (what.equals("now")) {
							event.sendMessage(fillEmbed(MINUTE_CMDS, baseEmbed(event, "Command Stats | Now")).build()).queue();
							return;
						}
					}
					
					event.sendMessage(baseEmbed(event, "Command Stats")
							.addField("Now", resume(MINUTE_CMDS), false)
							.addField("Hourly", resume(HOUR_CMDS), false)
							.addField("Daily", resume(DAY_CMDS), false)
							.addField("Total", resume(TOTAL_CMDS), false)
							.build()
					).queue();
				})
				.build();
	}
	
	private static EmbedBuilder baseEmbed(CommandEvent event, String title) {
		Color color = Color.decode("#8B4AEC");
		if (event.getGuild() != null && event.getSelfMember().getColor() != null)
			color = event.getSelfMember().getColor();
		return new EmbedBuilder().setTitle(title, null).setColor(color);
	}
}
