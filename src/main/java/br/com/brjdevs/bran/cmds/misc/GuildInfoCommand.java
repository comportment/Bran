package br.com.brjdevs.bran.cmds.misc;

import br.com.brjdevs.bran.core.command.Category;
import br.com.brjdevs.bran.core.command.CommandBuilder;
import br.com.brjdevs.bran.core.command.CommandManager;
import br.com.brjdevs.bran.core.command.RegisterCommand;
import br.com.brjdevs.bran.core.utils.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static br.com.brjdevs.bran.core.utils.StringUtils.neat;

@RegisterCommand
public class GuildInfoCommand {
	public GuildInfoCommand() {
		CommandManager.addCommand(new CommandBuilder(Category.INFORMATIVE)
				.setAliases("guildinfo", "guild", "serverinfo", "server")
				.setName("Guild Info Command")
				.setDescription("Gives you info about the given guild id")
				.setArgs("<guild ID>")
				.setPrivateAvailable(false)
				.setExample("guild 219256419684188161")
				.setAction((event) -> {
					String guildId = event.getArgs(2)[1];
					Guild guild = (guildId.isEmpty() ? event.getOriginGuild() : event.getJDA().getGuildById(guildId) == null ? event.getOriginGuild() : event.getJDA().getGuildById(guildId));
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
					embedBuilder.addField("Members", String.valueOf(guild.getMembers().size()), true);
					embedBuilder.addField("Text Channels", String.valueOf(guild.getTextChannels().size()), true);
					embedBuilder.addField("Voice Channels", String.valueOf(guild.getVoiceChannels().size()), true);
					embedBuilder.addField("Verification Level", guild.getVerificationLevel().toString(), true);
					embedBuilder.addField("Emotes Count", String.valueOf(guild.getEmotes().size()), true);
					embedBuilder.addField("Role Count", String.valueOf(guild.getRoles().size()), true);
					List<Role> roles = guild.getRoles().stream()
							.filter(role -> !role.getName().equals("@everyone")).collect(Collectors.toList());
					String string = "";
					int i = roles.size();
					for (Role role : roles) {
						if (string.length() > 900) break;
						string += role.getName();
						i--;
						if (string.length() < 900 && roles.indexOf(role) != roles.size() - 1)
							string += ", ";
					}
					if (i != 0)
						string += " *(+" + i + " roles)*";
					embedBuilder.addField("Roles", string, false);
					if (hasEmotes) {
						List<Emote> emotes = guild.getEmotes();
						String str = "";
						int emotesSize = emotes.size();
						for (Emote emote : emotes) {
							if (str.length() > 900) break;
							str += emote.getAsMention();
							emotesSize--;
							if (str.length() < 900 && emotes.indexOf(emote) != emotes.size() - 1)
								str += " ";
						}
						if (emotesSize != 0)
							str += " *(+" + emotesSize + " emote)*";
						embedBuilder.addField("Emotes", str, true);
					}
					event.sendMessage(embedBuilder.build()).queue();
				})
				.build());
	}
}
