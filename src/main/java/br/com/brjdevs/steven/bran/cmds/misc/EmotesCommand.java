package br.com.brjdevs.steven.bran.cmds.misc;

import br.com.brjdevs.steven.bran.core.command.*;
import br.com.brjdevs.steven.bran.core.utils.StringUtils;
import br.com.brjdevs.steven.bran.core.utils.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class EmotesCommand {
	
	@Command
	public static ICommand emotes() {
		return new TreeCommandBuilder(Category.INFORMATIVE)
				.setAliases("emote")
				.setName("Emote Command")
				.setDefault("info")
				.setHelp("emote ?")
				.onNotFound(CommandAction.REDIRECT)
				.setPrivateAvailable(false)
				.addCommand(new CommandBuilder(Category.INFORMATIVE)
						.setDescription("Returns you info about a certain emote.")
						.setArgs("[EMOTE]")
						.setName("Emote Info Command")
						.setAliases("info")
						.setAction((event, args) -> {
							if (event.getMessage().getEmotes().isEmpty()) {
								event.sendMessage("I can't give you info about emotes if you don't tell me one!").queue();
								return;
							}
							Emote emote = event.getMessage().getEmotes().get(0);
							OffsetDateTime creation = emote.getCreationTime();
							String createdAt = StringUtils.neat(creation.getDayOfWeek().toString().substring(0, 3)) + ", " + creation.getDayOfMonth() + " " + StringUtils.neat(creation.getMonth().toString().substring(0, 3)) + " " + creation.getYear() + " " + creation.getHour() + ":" + creation.getMinute() + ":" + creation.getSecond() + " GMT";
							MessageEmbed embed = new EmbedBuilder()
									.setFooter("Requested by " + event.getAuthor().getName(), Util.getAvatarUrl(event.getAuthor()))
									.setTimestamp(event.getMessage().getCreationTime())
									.addField("Name: ", emote.getName(), true)
									.addField("ID: ", emote.getId(), true)
									.addField("Creation: ", createdAt + "\n\u00ad", true)
									.addField("Emote: ", emote.getAsMention(), true)
									.setColor(event.getOriginMember().getColor() == null ? Color.decode("#f1c40f") : event.getOriginMember().getColor())
									.setThumbnail(emote.getImageUrl())
									.build();
							event.sendMessage(embed).queue();
						})
						.build())
				.addCommand(new CommandBuilder(Category.INFORMATIVE)
						.setAliases("list")
						.setName("Emote List Command")
						.setDescription("Lists all the available emotes.")
						.setAction((event) -> {
							List<String> emotes = event.getOriginGuild().getEmotes().stream()
									.map(Emote::getAsMention)
									.collect(Collectors.toList());
							emotes.sort(Comparator.naturalOrder());
							if (emotes.isEmpty()) {
								event.sendMessage("This guild doesn't have emotes!").queue();
								return;
							}
							event.sendMessage("**__" + event.getOriginGuild().getName() + " emotes:__**\n" + String.join(" ", emotes)
							).queue();
						})
						.build())
				.build();
	}
}
