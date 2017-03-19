package br.com.brjdevs.steven.bran.cmds.currency;

import br.com.brjdevs.steven.bran.core.command.Argument;
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
import net.dv8tion.jda.core.EmbedBuilder;

import java.awt.*;
import java.util.Arrays;

public class ShopCommand {
	
	//@Command
	private static ICommand market() {
		return new TreeCommandBuilder(Category.CURRENCY)
				.setAliases("market")
				.setName("Market Command")
				.setDescription("You should spend all your money here!")
				.addSubCommand(new CommandBuilder(Category.CURRENCY)
						.setAliases("list")
						.setName("Shop List Command")
						.setDescription("Gives you a full list of the available items do Buy!")
						.setAction((event) -> {
							EmbedBuilder embedBuilder = new EmbedBuilder();
							embedBuilder.setColor(Color.DARK_GRAY);
							Arrays.stream(Items.ALL).forEach(item -> embedBuilder.addField(item.getEmoji() + " " + item.getName(), (item.isSellable() ? "\uD83D\uDCE4" + (item.isBuyable() ? "/" : "") : "") + (item.isBuyable() ? "\uD83D\uDCE5" : "") + " " + item.getValue() + " coins", true));
							event.sendMessage(embedBuilder.build()).queue();
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
							} else if (!item.isBuyable()) {
								event.sendMessage(Emojis.X + " This item is not buyable!").queue();
								return;
                            } else if (!event.getUserData().getProfileData().getBankAccount().takeCoins(item.getValue(), BankAccount.MAIN_BANK)) {
                                event.sendMessage(Emojis.X + " You don't have enough coins, go get some before spending these!").queue();
								return;
							}
                            if (event.getUserData().getProfileData().getInventory().put(item))
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
							Item item = Items.fromEmoji(unicode).orElse(Arrays.stream(Items.ALL).filter(i -> i.getName().equalsIgnoreCase(sItem)).findFirst().orElse(null));
							if (item == null) {
								event.sendMessage(Quotes.FAIL, "No items found matching that criteria.").queue();
							} else if (!item.isSellable()) {
								event.sendMessage(Emojis.X + " This item is not sellable!").queue();
                            } else if (event.getUserData().getProfileData().getInventory().getAmountOf(item) <= 0) {
                                event.sendMessage("You don't have any `" + item.getName() + "` left in your inventory!").queue();
                            } else if (event.getUserData().getProfileData().getInventory().remove(item)) {
                                if (!event.getUserData().getProfileData().getBankAccount().addCoins(item.getValue(), BankAccount.MAIN_BANK)) {
                                    event.sendMessage(Emojis.X + " You have too much coins! Spend some money before getting more!").queue();
                                    event.getUserData().getProfileData().getInventory().put(item);
                                } else {
                                    event.sendMessage(Emojis.CHECK_MARK + " You've sold a " + item.getEmoji() + " " + item.getName() + "! Remaining " + item.getName() + ": " + event.getUserData().getProfileData().getInventory().getAmountOf(item)).queue();
                                }
							}
							
						})
						.build())
				.build();
	}
}
