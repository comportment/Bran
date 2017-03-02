package br.com.brjdevs.steven.bran.cmds.misc;

import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.builders.TreeCommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.enums.CommandAction;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.utils.StringUtils;
import br.com.brjdevs.steven.bran.core.utils.Utils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class EmotesCommand {
	
	@Command
	private static ICommand emotes() {
		return new TreeCommandBuilder(Category.INFORMATIVE)
				.setAliases("emote")
				.setName("Emote Command")
				.setDefault("info")
				.setDescription("Get information on Emotes!")
				.setHelp("emote ?")
				.onNotFound(CommandAction.REDIRECT)
				.setPrivateAvailable(false)
				.addSubCommand(new CommandBuilder(Category.INFORMATIVE)
						.setDescription("Returns you info about a certain emote.")
						.setArgs(new Argument("emote", String.class))
						.setName("Emote Info Command")
						.setAliases("info")
						.setAction((event, args) -> {
							if (event.getMessage().getEmotes().isEmpty()) {
								event.sendMessage("I can't give you info about emotes if you don't tell me one!").queue();
								return;
							}
							if (!event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_EMBED_LINKS)) {
								event.sendMessage("I need to have MESSAGE_EMBED_LINKS permission to send this message!").queue();
								return;
							}
							Emote emote = event.getMessage().getEmotes().get(0);
							OffsetDateTime creation = emote.getCreationTime();
							String createdAt = StringUtils.neat(creation.getDayOfWeek().toString().substring(0, 3)) + ", " + creation.getDayOfMonth() + " " + StringUtils.neat(creation.getMonth().toString().substring(0, 3)) + " " + creation.getYear() + " " + creation.getHour() + ":" + creation.getMinute() + ":" + creation.getSecond() + " GMT";
							MessageEmbed embed = new EmbedBuilder()
									.setFooter("Requested by " + event.getAuthor().getName(), Utils.getAvatarUrl(event.getAuthor()))
									.setTimestamp(event.getMessage().getCreationTime())
									.addField("Name: ", emote.getName(), true)
									.addField("ID: ", emote.getId(), true)
									.addField("Creation: ", createdAt + "\n\u00ad", true)
									.addField("Emote: ", emote.getAsMention(), true)
									.setColor(event.getMember().getColor() == null ? Color.decode("#f1c40f") : event.getMember().getColor())
									.setThumbnail(emote.getImageUrl())
									.build();
							event.sendMessage(embed).queue();
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.INFORMATIVE)
						.setAliases("list")
						.setName("Emote List Command")
						.setDescription("Lists all the available emotes.")
						.setAction((event) -> {
							List<String> emotes = event.getGuild().getEmotes().stream()
									.map(Emote::getAsMention)
									.collect(Collectors.toList());
							emotes.sort(Comparator.naturalOrder());
							if (emotes.isEmpty()) {
								event.sendMessage("This guild doesn't have emotes!").queue();
								return;
							}
							event.sendMessage("**__" + event.getGuild().getName() + " emotes:__**\n" + String.join(" ", emotes)
							).queue();
						})
						.build())
				.build();
	}
}
