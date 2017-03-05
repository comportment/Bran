package br.com.brjdevs.steven.bran.cmds.currency;

import br.com.brjdevs.steven.bran.core.client.Bran;
import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.currency.BankAccount;
import br.com.brjdevs.steven.bran.core.data.UserData;
import br.com.brjdevs.steven.bran.core.managers.RateLimiter;
import br.com.brjdevs.steven.bran.core.utils.Emojis;

import java.util.Random;

public class GambleCommand {
	
	private static final Random r = new Random();
	private static final RateLimiter RATELIMITER = new RateLimiter(3000);
	@Command
	private static ICommand gamble() {
		return new CommandBuilder(Category.CURRENCY)
				.setAliases("gamble")
				.setName("Gamble Command")
				.setDescription("Gamble your money here!")
				.setArgs(new Argument("quantity", String.class))
				.setAction((event) -> {
					if (!RATELIMITER.process(event.getAuthor())) {
						event.sendMessage("Hey, slow down a little bit buddy! You're gambling faster than I can print money!").queue();
						return;
					}
					String quantity = ((String) event.getArgument("quantity").get());
					UserData data = event.getUserData();
					long coins = data.getProfile().getBankAccount().getCoins();
					if (data.getProfile().getBankAccount().getCoins() <= 0) {
						event.getChannel().sendMessage("You're broke. Go get some credits before gambling!").queue();
						return;
					}
					
					double multiplier;
					long i;
					int luck;
					try {
						switch (quantity) {
							case "all":
							case "everything":
								i = coins;
								multiplier = 1.5d + (r.nextInt(1500) / 1000d);
								luck = 42 + (int) (multiplier * 10) + r.nextInt(20);
								break;
							case "half":
								i = coins == 1 ? 1 : coins / 2;
								multiplier = 1d + (r.nextInt(1500) / 1000d);
								luck = 35 + (int) (multiplier * 15) + r.nextInt(20);
								break;
							case "quarter":
								i = coins == 1 ? 1 : coins / 4;
								multiplier = 1d + (r.nextInt(1000) / 1000d);
								luck = 40 + (int) (multiplier * 15) + r.nextInt(20);
								break;
							default:
								i = Integer.parseInt(quantity);
								if (i > coins || i < 0) {
									event.getChannel().sendMessage("Please type a value within your credits amount.").queue();
									return;
								}
								multiplier = 1.2d + (i / coins * r.nextInt(1300) / 1000d);
								luck = 45 + (int) (multiplier * 15) + r.nextInt(10);
								break;
						}
					} catch (NumberFormatException e) {
						event.getChannel().sendMessage("Please type a valid number equal or less than your credits, `all`, `half` or `quarter`.").queue();
						return;
					}
					
					BankAccount acc = data.getProfile().getBankAccount();
					if (luck > r.nextInt(100)) {
						long gains = (long) (i * multiplier);
						gains = Math.round(gains * 0.55);
						if (acc.addCoins(gains, BankAccount.MAIN_BANK)) {
							event.sendMessage(Emojis.PARTY_POPPER + " You won " + gains + " credits!").queue();
						} else {
							event.sendMessage("You've got too many coins, go spend some first!").queue();
						}
					} else {
						acc.setCoins(Math.max(0, coins - i));
						event.getChannel().sendMessage("You lost " + (coins == 0 ? "all your" : i) + " credits! " + Emojis.CRY).queue();
					}
					Bran.getInstance().getDataManager().getDataHolderManager().update();
				})
				.build();
	}
}
