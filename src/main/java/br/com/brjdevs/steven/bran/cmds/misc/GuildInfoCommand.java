package br.com.brjdevs.steven.bran.cmds.misc;

import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.utils.StringUtils;
import br.com.brjdevs.steven.bran.core.utils.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static br.com.brjdevs.steven.bran.core.utils.StringUtils.neat;

public class GuildInfoCommand {
	
	@Command
	private static ICommand guildInfo() {
		return new CommandBuilder(Category.INFORMATIVE)
				.setAliases("guildinfo", "guild", "serverinfo", "server")
				.setName("Guild Info Command")
				.setDescription("Gives you info about the given guild id.")
				.setArgs(new Argument<>("guildId", String.class, true))
				.setPrivateAvailable(false)
				.setAction((event) -> {
					Argument argument = event.getArgument("guildId");
					Guild guild = argument.isPresent() ? event.getJDA().getGuildById((String) argument.get()) : event.getGuild();
					if (guild == null) guild = event.getGuild();
					Member guildOnwer = guild.getOwner();
					OffsetDateTime creation = guild.getCreationTime();
					String creationDate = neat(creation.getDayOfWeek().toString().substring(0, 3)) + ", " + creation.getDayOfMonth() + " " + neat(creation.getMonth().toString().substring(0, 3)) + " " + creation.getYear() + " " + creation.getHour() + ":" + creation.getMinute() + ":" + creation.getSecond() + " GMT";
					boolean hasEmotes = !guild.getEmotes().isEmpty();
					EmbedBuilder embedBuilder = new EmbedBuilder();
					embedBuilder.setThumbnail(guild.getIconUrl());
					embedBuilder.setFooter("Requested by " + Util.getUser(event.getAuthor()),
							Util.getAvatarUrl(event.getAuthor()));
					embedBuilder.setColor(
							guildOnwer.getColor() == null ? Color.decode("#F38630") : guildOnwer.getColor());
					embedBuilder.addField("Guild", guild.getName() + "\n(ID: " + guild.getId() + ")", true);
					embedBuilder.addField("Owner", Util.getUser(guildOnwer.getUser()) + "\n(ID: " + guildOnwer.getUser().getId() + ")", true);
					embedBuilder.addField("Region", guild.getRegion().toString(), true);
					embedBuilder.addField("Created at", creationDate, true);
					List<Member> online = guild.getMembers().stream().filter(m -> m.getOnlineStatus() == OnlineStatus.ONLINE).collect(Collectors.toList());
					embedBuilder.addField("Members", String.valueOf(guild.getMembers().size()) + " (Online: " + online.size() + "/Offline: " + (guild.getMembers().size() - online.size()) + ")", true);
					embedBuilder.addField("Text Channels", String.valueOf(guild.getTextChannels().size()), true);
					embedBuilder.addField("Voice Channels", String.valueOf(guild.getVoiceChannels().size()), true);
					embedBuilder.addField("Verification Level", guild.getVerificationLevel().toString(), true);
					embedBuilder.addField("Emotes Count", String.valueOf(guild.getEmotes().size()), true);
					embedBuilder.addField("Role Count", String.valueOf(guild.getRoles().size()), true);
					List<Role> roles = guild.getRoles().stream()
							.filter(role -> !role.getName().equals("@everyone")).collect(Collectors.toList());
					String strRoles = "";
					int i = roles.size();
					for (Role role : roles) {
						if (strRoles.length() > EmbedBuilder.VALUE_MAX_LENGTH - 100) break;
						strRoles += role.getName();
						i--;
					}
					strRoles = StringUtils.replaceLast(strRoles, ", ", "");
					if (i != 0)
						strRoles += " *(+" + i + " roles)*";
					embedBuilder.addField("Roles", strRoles, false);
					if (hasEmotes) {
						List<Emote> emotes = guild.getEmotes();
						String strEmotes = "";
						int emotesSize = emotes.size();
						for (Emote emote : emotes) {
							if (strEmotes.length() > EmbedBuilder.VALUE_MAX_LENGTH - 100) break;
							strEmotes += emote.getAsMention();
							emotesSize--;
						}
						strEmotes = StringUtils.replaceLast(strEmotes, ", ", "");
						if (emotesSize != 0)
							strEmotes += " *(+" + emotesSize + " emote)*";
						embedBuilder.addField("Emotes", strEmotes, true);
					}
					event.sendMessage(embedBuilder.build()).queue();
				})
				.build();
	}
}
