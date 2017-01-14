package br.com.brjdevs.steven.bran.cmds.fun;

import br.com.brjdevs.steven.bran.core.command.*;
import br.com.brjdevs.steven.bran.core.command.actions.CommandAction;
import br.com.brjdevs.steven.bran.core.data.guild.configs.GuildMember;
import br.com.brjdevs.steven.bran.core.data.guild.configs.profile.Profile;
import br.com.brjdevs.steven.bran.core.data.guild.configs.profile.Profile.Rank;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;
import java.util.regex.Pattern;

public class ProfileCommand {
	private static final Pattern pattern = Pattern.compile("^(reset|default|null|none)$");
	
	@Command
	private static ICommand profile() {
		return new TreeCommandBuilder(Category.FUN)
				.setName("Profile Command")
				.setAliases("profile")
				.setDefault("view")
				.setHelp("profile ?")
				.setDescription("Manage and View your Profile!")
				.onNotFound(CommandAction.REDIRECT)
				.setPrivateAvailable(false)
				.addSubCommand(new CommandBuilder(Category.INFORMATIVE)
						.setAliases("view")
						.setDescription("Gives you information on the requested profile.")
						.setArgs(new Argument<>("mention", String.class, true))
						.setName("Profile View Command")
						.setAction((event) -> {
							User user = event.getMessage().getMentionedUsers().isEmpty() ? event.getAuthor() : event.getMessage().getMentionedUsers().get(0);
							GuildMember member = event.getDiscordGuild().getMember(user);
							event.sendMessage(member.getProfile().createEmbed(event.getJDA())).queue();
						})
						.build())
				.addSubCommand(new TreeCommandBuilder(Category.MISCELLANEOUS)
						.setAliases("edit")
						.setName("Profile Edit Command")
						.setHelp("profile edit ?")
						.addSubCommand(new CommandBuilder(Category.MISCELLANEOUS)
								.setAliases("customcolor", "color")
								.setName("Profile Edit Color Command")
								.setArgs(new Argument<>("hex", String.class, true))
								.setDescription("Set or update your custom color!")
								.setAction((event, rawArgs) -> {
									Profile profile = event.getMember().getProfile();
									Argument argument = event.getArgument("hex");
									if (!argument.isPresent()) {
										if (profile.getCustomHex() != null) {
											event.sendMessage("You don't have any Custom Colors set! Append a Hex Code to the end of the command to").queue();
										}
										return;
									}
									String hex = (String) argument.get();
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
				.build();
	}
}
