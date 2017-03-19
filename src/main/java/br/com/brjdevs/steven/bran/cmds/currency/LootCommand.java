package br.com.brjdevs.steven.bran.cmds.currency;

import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.currency.BankAccount;
import br.com.brjdevs.steven.bran.core.currency.DroppedMoney;
import br.com.brjdevs.steven.bran.core.managers.RateLimiter;
import br.com.brjdevs.steven.bran.core.utils.Emojis;

public class LootCommand {
	
	private static final RateLimiter RATELIMITER = new RateLimiter(10000);
	
	//@Command
	private static ICommand loot() {
		return new CommandBuilder(Category.CURRENCY)
				.setAliases("loot")
				.setName("Loot Command")
				.setDescription("Loots money from the ground!")
				.setPrivateAvailable(false)
				.setAction((event) -> {
					if (!RATELIMITER.process(event.getAuthor())) {
						event.sendMessage("Hey, slow down a little bit there buddy! Let other people loot too!").queue();
						return;
					}
					DroppedMoney ground = DroppedMoney.of(event.getTextChannel());
					int money = ground.collect();
					if (money <= 0) {
						event.sendMessage("Nothing to loot here.").queue();
						return;
					}
                    if (!event.getUserData().getProfileData().takeStamina(5)) {
                        event.sendMessage("You are too tired of walking, why don't you take a rest while your stamina regenerates?").queue();
						return;
					}
					StringBuilder sb = new StringBuilder().append(Emojis.PARTY_POPPER + " ");
					sb.append("You walk a little and find ");
					if (money > 0) {
                        if (!event.getUserData().getProfileData().getBankAccount().addCoins(money, BankAccount.MAIN_BANK)) {
                            ground.drop(money);
							event.sendMessage("It looks like your bank account is full! Why don't you spend some money first?").queue();
						} else {
							sb.append(money).append(" coins");
						}
					}
					sb.append("!");
					event.sendMessage(sb.toString()).queue();
				})
				.build();
	}
}
