package br.com.brjdevs.steven.bran.cmds.misc;

import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.builders.TreeCommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.enums.CommandAction;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.itemManager.Item;
import br.com.brjdevs.steven.bran.core.itemManager.ItemContainer;
import br.com.brjdevs.steven.bran.core.managers.profile.Inventory;
import br.com.brjdevs.steven.bran.core.utils.ListBuilder;
import br.com.brjdevs.steven.bran.core.utils.ListBuilder.Format;

import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class ShopCommand {
	
	@Command
	private static ICommand shop() {
		return new TreeCommandBuilder(Category.MISCELLANEOUS)
				.setAliases("shop")
				.setName("Shop Command")
				.setDescription("You should spend all your money here!")
				.setDefault("buy")
				.onNotFound(CommandAction.REDIRECT)
				.addSubCommand(new CommandBuilder(Category.MISCELLANEOUS)
						.setAliases("list")
						.setName("Shop List Command")
						.setDescription("Gives you a full list of the available items do Buy!")
						.setArgs(new Argument<>("page", Integer.class, true))
						.setAction((event) -> {
							Argument arg = event.getArgument("page");
							int page = arg.isPresent() && (int) arg.get() > 0 ? (int) arg.get() : 1;
							List<String> items = ItemContainer.map()
									.entrySet().stream()
									.map(entry -> {
										Item item = entry.getValue();
										return item.getName() + " - Price: " + item.getPrice() + (item.getMinimumRank().getLevel() > event.getGuildMember().getProfile().getRank().getLevel() ? " You must be at least rank " + item.getMinimumRank() + " to buy this item!" : "");
									}).collect(Collectors.toList());
							ListBuilder listBuilder = new ListBuilder(items, page, 15);
							listBuilder.setName("Available Items");
							listBuilder.setFooter("Total Items: " + ItemContainer.map().size());
							event.sendMessage(listBuilder.format(Format.CODE_BLOCK)).queue();
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.MISCELLANEOUS)
						.setAliases("buy")
						.setName("Buy Command")
						.setDescription("Buys an item from the store.")
						.setArgs(new Argument<>("item", String.class))
						.setAction((event) -> {
							String name = ((String) event.getArgument("item").get());
							Entry<String, Item> result = ItemContainer.map().entrySet().stream()
									.filter(entry -> entry.getValue().getName().equalsIgnoreCase(name)).findFirst().orElse(null);
							if (result == null) {
								event.sendMessage("No items named \"" + name + "\".").queue();
								return;
							}
							Item item = result.getValue();
							if (item.getMinimumRank().getLevel() > event.getGuildMember().getProfile().getRank().getLevel()) {
								event.sendMessage("You must be at least rank " + item.getMinimumRank() + " to buy this item!").queue();
								return;
							}
							Inventory inventory = event.getGuildMember().getProfile().getInventory();
							if (inventory.getAmountOf(item) > item.getMaxStack()) {
								event.sendMessage("You have too much of this item! (" + inventory.getAmountOf(item) + "/" + item.getMaxStack() + ")").queue();
								return;
							}
							if (!event.getGuildMember().getProfile().takeCoins(item.getPrice())) {
								event.sendMessage("You don't have enough coins to buy this item! " +
										"(" + event.getGuildMember().getProfile().getCoins() + "/" + item.getPrice() + ")").queue();
							} else {
								inventory.put(item);
								event.sendMessage("You've bought a " + item.getName() + "! Remaining coins: " + event.getGuildMember().getProfile().getCoins()).queue();
							}
						})
						.build())
				.build();
	}
}