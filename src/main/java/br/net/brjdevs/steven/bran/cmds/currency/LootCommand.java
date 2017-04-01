package br.net.brjdevs.steven.bran.cmds.currency;

import br.net.brjdevs.steven.bran.core.command.Command;
import br.net.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.net.brjdevs.steven.bran.core.command.enums.Category;
import br.net.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.net.brjdevs.steven.bran.core.currency.BankAccount;
import br.net.brjdevs.steven.bran.core.currency.DroppedMoney;
import br.net.brjdevs.steven.bran.core.managers.RateLimiter;
import br.net.brjdevs.steven.bran.core.utils.Emojis;

public class LootCommand {
    
    public static final RateLimiter RATELIMITER = new RateLimiter(10000);
    
    @Command
    private static ICommand loot() {
		return new CommandBuilder(Category.CURRENCY)
				.setAliases("loot")
				.setName("Loot Command")
				.setDescription("Loots money from the ground!")
				.setPrivateAvailable(false)
				.setAction((event) -> {
					if (!RATELIMITER.process(event.getAuthor())) {
                        event.sendMessage("Hey, slow down a little bit there buddy, don't be greedy!").queue();
                        return;
					}
                    int money = DroppedMoney.of(event.getTextChannel()).collect();
                    if (money <= 0) {
						event.sendMessage("Nothing to loot here.").queue();
						return;
					}
                    if (!event.getUserData().getProfileData().takeStamina(5)) {
                        event.sendMessage("You are too tired of walking, why don't you take a rest while your stamina regenerates?").queue();
						return;
					}
					if (money > 0) {
                        if (!event.getUserData().getProfileData().getBankAccount().addCoins(money, BankAccount.MAIN_BANK)) {
                            DroppedMoney.of(event.getTextChannel()).drop(money);
                            event.sendMessage("It looks like your bank account is full! Why don't you spend some money first?").queue();
						} else {
                            event.sendMessage(Emojis.PARTY_POPPER + " You walk a little and find " + money + " coins!").queue();
                        }
					}
				})
				.build();
	}
}
