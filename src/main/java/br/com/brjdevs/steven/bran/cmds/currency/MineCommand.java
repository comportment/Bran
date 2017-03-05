package br.com.brjdevs.steven.bran.cmds.currency;

import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.currency.BankAccount;
import br.com.brjdevs.steven.bran.core.currency.ItemMeta;
import br.com.brjdevs.steven.bran.core.currency.Items;
import br.com.brjdevs.steven.bran.core.managers.RateLimiter;
import br.com.brjdevs.steven.bran.core.managers.profile.Inventory;
import br.com.brjdevs.steven.bran.core.utils.Emojis;
import br.com.brjdevs.steven.bran.core.utils.MathUtils;

public class MineCommand {
	
	private static final RateLimiter RATE_LIMITER = new RateLimiter(5000);
	
	@Command
	private static ICommand mine() {
		return new CommandBuilder(Category.CURRENCY)
				.setAliases("mine")
				.setName("Mine Command")
				.setDescription("Mines coins!")
				.setAction((event) -> {
					Inventory inv = event.getUserData().getProfile().getInventory();
					long amount = inv.getAmountOf(Items.PICKAXE);
					if (amount <= 0) {
						event.sendMessage("You don't have any pickaxes in your inventory!").queue();
						return;
					}
					int stamina = 10;
					if (!RATE_LIMITER.process(event.getAuthor())) {
						stamina += 5;
						event.sendMessage("*You are mining too fast so your stamina goes lower faster.*").queue();
					}
					
					event.getUserData().getProfile().takeStamina(stamina);
					long moneyFound = (long) (MathUtils.random.nextInt(stamina == 10 ? 250 : 2) * (1.0d + amount * 0.5d));
					event.getUserData().getProfile().getBankAccount().addCoins(moneyFound, BankAccount.MAIN_BANK);
					event.sendMessage("You found " + moneyFound + " while mining! " + Emojis.PARTY_POPPER).queue();
					ItemMeta meta = inv.getItems().get(Items.idOf(Items.PICKAXE));
					if (!meta.doDamage(stamina * 2)) {
						event.sendMessage("Unfortunately one of your pickaxes broke!").queue();
					}
				})
				.build();
	}
	
}
