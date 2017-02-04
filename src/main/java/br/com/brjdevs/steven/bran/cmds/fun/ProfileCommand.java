package br.com.brjdevs.steven.bran.cmds.fun;

import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.builders.TreeCommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.enums.CommandAction;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.data.bot.settings.Profile;
import br.com.brjdevs.steven.bran.core.data.bot.settings.Profile.Rank;
import br.com.brjdevs.steven.bran.core.itemManager.Item;
import br.com.brjdevs.steven.bran.core.itemManager.ItemContainer;
import br.com.brjdevs.steven.bran.core.managers.profile.Inventory;
import br.com.brjdevs.steven.bran.core.utils.ListBuilder;
import br.com.brjdevs.steven.bran.core.utils.ListBuilder.Format;
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
						.setArgs(new Argument<>("mention", String.class, true))
						.setName("Profile View Command")
						.setAction((event) -> {
							if (event.getGuild() != null && !event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_EMBED_LINKS)) {
								event.sendMessage("I need to have MESSAGE_EMBED_LINKS permission to send this message!").queue();
								return;
							}
							User user = event.getMessage().getMentionedUsers().isEmpty() ? event.getAuthor() : event.getMessage().getMentionedUsers().get(0);
							event.sendMessage(event.getBotContainer().getProfile(user).createEmbed(event.getJDA())).queue();
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.INFORMATIVE)
						.setAliases("inventory")
						.setDescription("Shows you your inventory.")
						.setArgs(new Argument<>("page", Integer.class, true))
						.setAction((event) -> {
							Inventory inventory = event.getBotContainer().getProfile(event.getAuthor()).getInventory();
							if (inventory.isEmpty()) {
								event.sendMessage("Your inventory is empty!").queue();
								return;
							}
							Argument arg = event.getArgument("page");
							int page = arg.isPresent() && (int) arg.get() > 0 ? (int) arg.get() : 1;
							List<String> items = inventory.getItems().entrySet()
									.stream().map(entry -> {
										Item item = ItemContainer.getItemById(entry.getKey());
										return item.getName() + "  x" + entry.getValue();
									}).collect(Collectors.toList());
							ListBuilder listBuilder = new ListBuilder(items, page, 15);
							listBuilder.setName("Your inventory");
							listBuilder.setFooter("Total Items (UNIQUE/TOTAL): " + inventory.size(true) + "/" + inventory.size(false));
							event.sendMessage(listBuilder.format(Format.CODE_BLOCK)).queue();
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
									Profile profile = event.getBotContainer().getProfile(event.getAuthor());
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
									} else {
										event.sendMessage("This does not look like a known hex...").queue();
									}
								})
								.build())
						.build())
				.build();
	}
}
