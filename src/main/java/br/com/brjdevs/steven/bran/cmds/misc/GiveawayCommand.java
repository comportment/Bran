package br.com.brjdevs.steven.bran.cmds.misc;

import br.com.brjdevs.steven.bran.Client;
import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.builders.TreeCommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.data.Giveaway;
import br.com.brjdevs.steven.bran.core.managers.Permissions;
import br.com.brjdevs.steven.bran.core.quote.Quotes;
import br.com.brjdevs.steven.bran.core.utils.CollectionUtils;
import br.com.brjdevs.steven.bran.core.utils.Hastebin;
import br.com.brjdevs.steven.bran.core.utils.OtherUtils;
import br.com.brjdevs.steven.bran.core.utils.TimeUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class GiveawayCommand {
	
	@Command
	private static ICommand giveaway() {
		return new TreeCommandBuilder(Category.MISCELLANEOUS)
				.setAliases("giveaway")
				.setName("Giveaway Command")
				.setDescription("Does this really need a description?")
				.setPrivateAvailable(false)
				.addSubCommand(new CommandBuilder(Category.MISCELLANEOUS)
						.setAliases("start")
						.setName("Giveaway Start Command")
						.setDescription("Starts a Giveaway!")
						.setArgs(new Argument<>("role", String.class), new Argument<>("winners", Integer.class), new Argument<>("time", String.class, true))
						.setRequiredPermission(Permissions.CREATE_GIVEAWAY)
						.setAction((event) -> {
							String n = ((String) event.getArgument("role").get());
							if (n.matches("all|everyone")) n = "@everyone";
							List<Role> matches = event.getGuild().getRolesByName(n, true);
							Role role;
							if (matches.isEmpty()) {
								event.sendMessage("No roles found matching that criteria.").queue();
								return;
							} else if (matches.size() > 1) {
								event.sendMessage("Found " + matches.size() + " roles matching that criteria, picking the first one...").queue();
							}
							role = matches.get(0);
							int numOfWinners = ((Integer) event.getArgument("winners").get());
							if (numOfWinners < 1) {
								event.sendMessage("The number of winners has to be bigger than 0!").queue();
								return;
							} else if (numOfWinners >= ((role == event.getGuild().getPublicRole() ? event.getGuild().getMembers() : event.getGuild().getMembersWithRoles(role)).size())) {
								event.sendMessage("The number of winners has to be smaller than the number of members " + (role.equals(event.getGuild().getPublicRole()) ? "in the Guild!" : "with the role `" + role.getName() + "`!")).queue();
								return;
							}
							Argument expiresIn = event.getArgument("time");
							long e = Long.MIN_VALUE;
							if (!expiresIn.isPresent()) {
								event.sendMessage("**Note:** This giveaway will not end until a member with GUILD_MOD permission run the `.giveway end` command!").queue();
							} else {
								e = TimeUtils.getTime(((String) expiresIn.get()), TimeUnit.MILLISECONDS);
								if (e > 10800000) {
									event.sendMessage("The maximum timeout for a Giveaway is 3 hours!").queue();
									return;
								}
							}
							event.getGuildData().giveaway = new Giveaway(event.getMember(), event.getGuild(), role, numOfWinners, e);
							event.sendMessage(Quotes.SUCCESS, "Created a Giveaway! You can check who's participating by typing `.giveaway info`.").queue();
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.INFORMATIVE)
						.setAliases("info", "information")
						.setName("Giveaway Information Command")
						.setDescription("Shows you information on giveaways running in the current Guild.")
						.setAction((event) -> {
							Client client = event.getClient();
							Giveaway giveaway = event.getGuildData().giveaway;
							if (giveaway == null) {
								event.sendMessage("No giveaway running in the current Guild!").queue();
								return;
							}
							EmbedBuilder embedBuilder = new EmbedBuilder();
							embedBuilder.setColor(Color.decode("#43474B"));
							embedBuilder.setAuthor("Information on the Giveaway for Guild " + event.getGuild().getName(), null, event.getGuild().getIconUrl());
							Member creator = giveaway.getCreator(client);
							embedBuilder.setFooter("Giveaway created by " + OtherUtils.getUser(creator == null ? null : creator.getUser()), creator == null ? null : giveaway.getCreator(client).getUser().getEffectiveAvatarUrl());
							String desc = "This giveaway is available for " + (giveaway.isPublic() ? "everyone" : "members with role `" + giveaway.getRole(client).getName() + "`") + ".\n\n";
							String participating = giveaway.getParticipants(client).stream().map(member -> OtherUtils.getUser(member.getUser())).collect(Collectors.joining("\n"));
							if (participating.length() > EmbedBuilder.TEXT_MAX_LENGTH - desc.length())
								participating = "The list was too long so I uploaded it to Hastebin: " + Hastebin.post(participating);
							desc += participating + "\n\nTotal users Participating: " + giveaway.getTotalParticipants() + " out of " + giveaway.getMaxWinners() + " winners... Who do you bet will win? \uD83D\uDC40";
							embedBuilder.setDescription(desc);
							event.sendMessage(embedBuilder.build()).queue();
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.MISCELLANEOUS)
						.setAliases("join")
						.setName("Giveaway Join Command")
						.setDescription("Joins a giveaway!")
						.setAction((event) -> {
							Giveaway giveaway = event.getGuildData().giveaway;
							if (giveaway == null) {
								event.sendMessage("No giveaways running in this Guild!").queue();
								return;
							}
							if (giveaway.isExpired()) {
								event.sendMessage("This Giveaway has expired, no more Members can participate!").queue();
								return;
							}
							Role role = giveaway.getRole(event.getClient());
							if (role != null && !event.getMember().getRoles().contains(role)) {
								event.sendMessage(Quotes.FAIL, "This Giveaway is only allowed for members with the role " + role.getName()).queue();
								return;
							}
							boolean success = giveaway.participate(event.getMember());
							event.sendMessage(success ? "You are now participating in the Giveaway!" : "You're already participating in the Giveaway!").queue();
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.MISCELLANEOUS)
						.setAliases("end")
						.setName("Giveaway End Command")
						.setDescription("Ends giveaways running in the current Guild.")
						.setRequiredPermission(Permissions.CREATE_GIVEAWAY)
						.setAction((event) -> {
							Client client = event.getClient();
							Giveaway giveaway = event.getGuildData().giveaway;
							if (giveaway == null) {
								event.sendMessage(Quotes.FAIL, "No giveaways running in the current Guild!").queue();
								return;
							}
							if (giveaway.isTimingOut()) {
								event.sendMessage("You **cannot** end Giveaways that are timing out!").queue();
								return;
							}
							if (giveaway.getParticipants().isEmpty()) {
								event.sendMessage("No users were participating in this Giveaway :cry:").queue();
								event.getGuildData().giveaway = null;
								return;
							}
							EmbedBuilder embedBuilder = new EmbedBuilder();
							embedBuilder.setColor(Color.decode("#43474B"));
							embedBuilder.setAuthor("Information on the Giveaway for Guild " + event.getGuild().getName(), null, event.getGuild().getIconUrl());
							embedBuilder.setFooter("Giveaway created by " + OtherUtils.getUser(giveaway.getCreator(client).getUser()), giveaway.getCreator(client).getUser().getEffectiveAvatarUrl());
							String desc = "This giveaway was available for " + (giveaway.isPublic() ? "everyone" : "members with role `" + giveaway.getRole(client).getName()) + ".\n";
							String participating = giveaway.getParticipants(client).stream().map(member -> OtherUtils.getUser(member.getUser())).collect(Collectors.joining("\n"));
							if (participating.length() > EmbedBuilder.TEXT_MAX_LENGTH - desc.length())
								participating = "The list was too long so I uploaded it to Hastebin: " + Hastebin.post(participating);
							List<Long> p = new ArrayList<>(giveaway.getParticipants());
							List<Member> winners = new ArrayList<>();
							for (int i = 0; i < giveaway.getMaxWinners() && !p.isEmpty(); i++) {
								long l = CollectionUtils.random(p);
								p.remove(l);
								Member m = event.getGuild().getMemberById(String.valueOf(l));
								if (m == null) continue;
								winners.add(m);
							}
							desc += participating + "\n\nThere was " + giveaway.getTotalParticipants() + " users participating on this Giveaway!\n\nAnd the " + (giveaway.getMaxWinners() > 1 ? "winners are" : "winner is") + "... " + winners.stream().map(m -> OtherUtils.getUser(m.getUser())).collect(Collectors.joining("\n"));
							embedBuilder.setDescription(desc);
							event.sendMessage(embedBuilder.build()).queue();
							event.sendMessage("Congratulations, " + (winners.stream().map(m -> m.getUser().getAsMention()).collect(Collectors.joining(", "))) + "! You won this Giveaway, contact " + OtherUtils.getUser(giveaway.getCreator(client).getUser()) + " to receive your prize(s)! :smile:").queue();
							winners.forEach(member -> member.getUser().openPrivateChannel().queue(channel -> channel.sendMessage("Hey there! Congratulations! You were one of the winners in a Giveaway running in " + giveaway.getGuild(client).getName() + ", contact " + OtherUtils.getUser(giveaway.getCreator(client).getUser()) + " to receive your prize(s)!").queue()));
							event.getGuildData().giveaway = null;
						})
						.build())
				.build();
	}
	
}
