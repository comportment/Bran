package br.com.brjdevs.steven.bran.cmds.currency;

import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.builders.TreeCommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.currency.BankAccount;
import br.com.brjdevs.steven.bran.core.currency.Item;
import br.com.brjdevs.steven.bran.core.currency.Items;
import br.com.brjdevs.steven.bran.core.quote.Quotes;
import br.com.brjdevs.steven.bran.core.utils.EmojiConverter;
import br.com.brjdevs.steven.bran.core.utils.Emojis;
import br.com.brjdevs.steven.bran.core.utils.StringListBuilder;
import br.com.brjdevs.steven.bran.core.utils.StringListBuilder.Format;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ShopCommand {
	
	@Command
	private static ICommand market() {
		return new TreeCommandBuilder(Category.CURRENCY)
				.setAliases("market")
				.setName("Market Command")
				.setDescription("You should spend all your money here!")
				.addSubCommand(new CommandBuilder(Category.CURRENCY)
						.setAliases("list")
						.setName("Shop List Command")
						.setDescription("Gives you a full list of the available items do Buy!")
						.setArgs(new Argument("page", Integer.class, true))
						.setAction((event) -> {
							Argument arg = event.getArgument("page");
							int page = arg.isPresent() && (int) arg.get() > 0 ? (int) arg.get() : 1;
							List<String> items = Arrays.stream(Items.ALL)
									.map(item -> item.getEmoji() + " " + item.getName() + " - Price: " + item.getValue()).collect(Collectors.toList());
							StringListBuilder listBuilder = new StringListBuilder(items, page, 15);
							listBuilder.setName("Available Items");
							listBuilder.setFooter("Total Items: " + Items.ALL.length);
							event.sendMessage(listBuilder.format(Format.CODE_BLOCK)).queue();
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.CURRENCY)
						.setAliases("buy")
						.setName("Buy Command")
						.setDescription("Buys an item from the store.")
						.setArgs(new Argument("item", String.class))
						.setAction((event) -> {
							String sItem = ((String) event.getArgument("item").get());
							String unicode = EmojiConverter.toUnicode(sItem);
							Item item = Items.fromEmoji(unicode).orElse(null);
							if (item == null)
								item = Arrays.stream(Items.ALL).filter(i -> i.getName().equalsIgnoreCase(sItem)).findFirst().orElse(null);
							if (item == null) {
								event.sendMessage(Quotes.FAIL, "No items found matching that criteria.").queue();
								return;
							} else if (!event.getUserData().getProfile().getBankAccount().takeCoins(item.getValue(), BankAccount.MAIN_BANK)) {
								event.sendMessage(Emojis.X + " You don't have enough coins, go get some before spending these!").queue();
								return;
							}
							if (event.getUserData().getProfile().getInventory().put(item))
								event.sendMessage(Emojis.CHECK_MARK + " You've bought a " + item.getEmoji() + " " + item.getName() + "!").queue();
							else
								event.sendMessage(Emojis.X + " You can't buy " + item.getName() + " because you have too much of it!").queue();
							
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.CURRENCY)
						.setAliases("sell")
						.setName("Sell Command")
						.setDescription("Sells an item to the store.")
						.setArgs(new Argument("item", String.class))
						.setAction((event) -> {
							String sItem = ((String) event.getArgument("item").get());
							String unicode = EmojiConverter.toUnicode(sItem);
							Item item = Items.fromEmoji(unicode).orElse(null);
							if (item == null)
								item = Arrays.stream(Items.ALL).filter(i -> i.getName().equalsIgnoreCase(sItem)).findFirst().orElse(null);
							if (item == null) {
								event.sendMessage(Quotes.FAIL, "No items found matching that criteria.").queue();
								return;
							} else if (!event.getUserData().getProfile().getBankAccount().addCoins(item.getValue(), BankAccount.MAIN_BANK)) {
								event.sendMessage(Emojis.X + " You have too much coins! Spend some money before getting more!").queue();
								return;
							}
							if (event.getUserData().getProfile().getInventory().remove(item))
								event.sendMessage(Emojis.CHECK_MARK + " You've sold a " + item.getEmoji() + " " + item.getName() + "! Remaining " + item.getName() + ": " + event.getUserData().getProfile().getInventory().getAmountOf(item)).queue();
							else
								event.sendMessage(Emojis.X + " You don't have any " + item.getName() + " in your inventory so you can't sell any!").queue();
							
						})
						.build())
				.build();
	}
}
