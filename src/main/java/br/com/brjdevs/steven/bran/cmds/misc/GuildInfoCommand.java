package br.com.brjdevs.steven.bran.cmds.misc;

import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.utils.OtherUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.Permission;
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
					if (!event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_EMBED_LINKS)) {
						event.sendMessage("I need to have MESSAGE_EMBED_LINKS permission to send this message!").queue();
						return;
					}
					Argument argument = event.getArgument("guildId");
					Guild guild = argument.isPresent() ? event.getJDA().getGuildById((String) argument.get()) : event.getGuild();
					if (guild == null) guild = event.getGuild();
					Member guildOwner = guild.getOwner();
					OffsetDateTime creation = guild.getCreationTime();
					String creationDate = neat(creation.getDayOfWeek().toString().substring(0, 3)) + ", " + creation.getDayOfMonth() + " " + neat(creation.getMonth().toString().substring(0, 3)) + " " + creation.getYear() + " " + creation.getHour() + ":" + creation.getMinute() + ":" + creation.getSecond() + " GMT";
					boolean hasEmotes = !guild.getEmotes().isEmpty();
					EmbedBuilder embedBuilder = new EmbedBuilder();
					embedBuilder.setThumbnail(guild.getIconUrl());
					embedBuilder.setFooter("Requested by " + OtherUtils.getUser(event.getAuthor()),
							OtherUtils.getAvatarUrl(event.getAuthor()));
					embedBuilder.setColor(
							guildOwner.getColor() == null ? Color.decode("#F38630") : guildOwner.getColor());
					embedBuilder.setTitle("\uD83C\uDFE0 Guild information on " + guild.getName(), null);
					embedBuilder.addField("ID", guild.getId(), true);
					embedBuilder.addField("Owner", OtherUtils.getUser(guildOwner.getUser()) + "\n(ID: " + guildOwner.getUser().getId() + ")", true);
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
					String strRoles = roles.stream().map(Role::getName).collect(Collectors.joining(", "));
					if (strRoles.length() <= EmbedBuilder.VALUE_MAX_LENGTH)
						embedBuilder.addField("Roles", strRoles, false);
					if (hasEmotes) {
						List<Emote> emotes = guild.getEmotes();
						String strEmotes = emotes.stream().map(Emote::getAsMention).collect(Collectors.joining(", "));
						if (strEmotes.length() <= EmbedBuilder.VALUE_MAX_LENGTH)
							embedBuilder.addField("Emotes", strEmotes, true);
					}
					event.sendMessage(embedBuilder.build()).queue();
				})
				.build();
	}
}
