package br.com.brjdevs.steven.bran.cmds.fun;

import br.com.brjdevs.steven.bran.core.client.Bran;
import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.builders.TreeCommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.enums.CommandAction;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.currency.BankAccount;
import br.com.brjdevs.steven.bran.core.currency.Item;
import br.com.brjdevs.steven.bran.core.currency.Items;
import br.com.brjdevs.steven.bran.core.currency.Profile;
import br.com.brjdevs.steven.bran.core.currency.Profile.Rank;
import br.com.brjdevs.steven.bran.core.managers.profile.Inventory;
import br.com.brjdevs.steven.bran.core.utils.Emojis;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
				.addSubCommand(new CommandBuilder(Category.INFORMATIVE)
						.setAliases("view")
						.setDescription("Gives you information on the requested profile.")
						.setArgs(new Argument("mention", String.class, true))
						.setName("Profile View Command")
						.setAction((event) -> {
							if (event.getGuild() != null && !event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_EMBED_LINKS)) {
								event.sendMessage("I need to have MESSAGE_EMBED_LINKS permission to send this message!").queue();
								return;
							}
							User user = event.getMessage().getMentionedUsers().isEmpty() ? event.getAuthor() : event.getMessage().getMentionedUsers().get(0);
							event.sendMessage(Bran.getInstance().getProfile(user).createEmbed(event.getJDA())).queue();
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.MISCELLANEOUS)
						.setAliases("rankup")
						.setName("Rankup Command")
						.setDescription("Buy ranks!")
						.setAction((event) -> {
							Rank next = event.getUserData().getProfile().getRank().next();
							Profile profile = event.getUserData().getProfile();
							if (profile.getLevel() < next.getLevel()) {
								event.sendMessage(Emojis.X + " You have to be at least level " + next.getLevel() + " to rank up!").queue();
								return;
							} else if (!profile.getBankAccount().takeCoins(next.getCost(), BankAccount.MAIN_BANK)) {
								event.sendMessage(Emojis.X + " You don't have enough coins! (" + profile.getBankAccount().getCoins() + "/" + next.getColor() + ")").queue();
								return;
							}
							Rank r = profile.getRank();
							profile.setRank(next);
							event.sendMessage("You ranked up from " + r + " to " + next + "!").queue();
							Bran.getInstance().getDataManager().getDataHolderManager().update();
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.INFORMATIVE)
						.setAliases("inventory")
						.setName("Inventory Command")
						.setDescription("Shows you your inventory.")
						.setAction((event) -> {
							Inventory inventory = Bran.getInstance().getProfile(event.getAuthor()).getInventory();
							if (inventory.isEmpty()) {
								event.sendMessage("Your inventory is empty!").queue();
								return;
							}
							List<String> items = inventory.getItems().entrySet()
									.stream().map(entry -> {
										Item item = Items.fromId(entry.getKey());
										return item.getEmoji() + " " + item.getName() + "  x" + entry.getValue();
									}).collect(Collectors.toList());
							EmbedBuilder embedBuilder = new EmbedBuilder();
							embedBuilder.setAuthor("Your inventory", null, event.getAuthor().getEffectiveAvatarUrl());
							embedBuilder.setDescription(String.join("\n", items));
							event.sendMessage(embedBuilder.build()).queue();
						})
						.build())
				.addSubCommand(new TreeCommandBuilder(Category.MISCELLANEOUS)
						.setAliases("edit")
						.setName("Profile Edit Command")
						.setHelp("profile edit ?")
						.addSubCommand(new CommandBuilder(Category.MISCELLANEOUS)
								.setAliases("customcolor", "color")
								.setName("Profile Edit Color Command")
								.setArgs(new Argument("hex", String.class, true))
								.setDescription("Set or update your custom color!")
								.setAction((event, rawArgs) -> {
									Profile profile = Bran.getInstance().getProfile(event.getAuthor());
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
										event.sendMessage(success ? "Updated your profile Color!" : "Failed to update your profile color!").queue();
										Bran.getInstance().getDataManager().getDataHolderManager().update();
									} else {
										event.sendMessage("This does not look like a known hex...").queue();
									}
								})
								.build())
						.build())
				.build();
	}
}
