package br.com.brjdevs.bran.cmds.fun;

import br.com.brjdevs.bran.core.command.*;
import br.com.brjdevs.bran.core.data.guild.configs.GuildMember;
import br.com.brjdevs.bran.core.data.guild.configs.profile.Profile;
import br.com.brjdevs.bran.core.data.guild.configs.profile.Profile.Rank;
import br.com.brjdevs.bran.core.utils.StringUtils;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;
import java.util.regex.Pattern;

@RegisterCommand
public class ProfileCommand {
	private static final Pattern pattern = Pattern.compile("^(reset|default|null|none)$");
	public ProfileCommand() {
		CommandManager.addCommand(new TreeCommandBuilder(Category.FUN)
				.setName("Profile Command")
				.setAliases("profile")
				.setDefault("view")
				.setHelp("profile ?")
				.onNotFound(Action.REDIRECT)
				.setPrivateAvailable(false)
				.addCommand(new CommandBuilder(Category.INFORMATIVE)
						.setAliases("view")
						.setDescription("Gives you information on the requested profile.")
						.setArgs("<mention>")
						.setName("Profile View Command")
						.setAction((event) -> {
							User user = event.getMessage().getMentionedUsers().isEmpty() ? event.getAuthor() : event.getMessage().getMentionedUsers().get(0);
							GuildMember member = event.getGuild().getMember(user);
							event.sendMessage(member.getProfile().createEmbed(event.getJDA())).queue();
						})
						.build())
				.addCommand(new TreeCommandBuilder(Category.MISCELLANEOUS)
						.setAliases("edit")
						.setName("Profile Edit Command")
						.setHelp("profile edit ?")
						.addCommand(new CommandBuilder(Category.MISCELLANEOUS)
								.setAliases("customcolor", "color")
								.setName("Profile Edit Color Command")
								.setArgs("<hex>")
								.setDescription("Set or update your custom color!")
								.setAction((event, rawArgs) -> {
									String hex = StringUtils.splitArgs(rawArgs, 2)[1];
									Profile profile = event.getMember().getProfile();
									if (profile.getRank() != Rank.SKILLED) {
										event.sendMessage("You need to be at least Rank SKILLED to set a Custom Color!").queue();
										return;
									}
									if (pattern.matcher(hex).matches()) {
										profile.setCustomColor(null);
										event.sendMessage("\uD83D\uDC4C Reseted your Profile color.").queue();
										return;
									}
									if (hex.charAt(0) != '#')
										hex = "#" + hex;
									boolean isHex = false;
									try {
										Color.decode(hex);
										isHex = true;
									} catch (Exception ignored) {}
									if (isHex) {
										boolean success = profile.setCustomColor(hex);
										event.sendMessage(success + "").queue();
										return;
									}
									event.sendMessage("This does not look like a known hex...").queue();
								})
								.build())
						.build())
				.build());
	}
}
