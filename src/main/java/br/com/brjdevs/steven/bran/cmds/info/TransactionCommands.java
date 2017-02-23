package br.com.brjdevs.steven.bran.cmds.info;

import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.currency.Transaction;
import br.com.brjdevs.steven.bran.core.currency.Transaction.TransactionType;
import br.com.brjdevs.steven.bran.core.utils.OtherUtils;
import br.com.brjdevs.steven.bran.core.utils.TimeUtils;

public class TransactionCommands {
	
	private static final String RECEIVE = "\uD83D\uDCE5";
	private static final String GIVE = "\uD83D\uDCE4";
	
	@Command
	private static ICommand transaction() {
		return new CommandBuilder(Category.INFORMATIVE)
				.setAliases("transactions")
				.setName("Transactions Command")
				.setDescription("Show you your latest transactions!")
				.setAction((event) -> {
					if (event.getUserData().getProfile().getBankAccount() == null) {
						event.sendMessage("You should open a bank account first! Use `" + event.getPrefix() + "bank create` to open one!").queue();
						return;
					} else if (event.getUserData().getProfile().getBankAccount().getTransactions().isEmpty()) {
						event.sendMessage("You haven't done any transactions!").queue();
						return;
					}
					String s = "";
					for (Transaction transaction : event.getUserData().getProfile().getBankAccount().getTransactions()) {
						s += String.format("%s`\u200B%+4d` %s `\u200B%18s` - %s\n",
								transaction.getTransactionType() == TransactionType.RECEIVE ? RECEIVE : GIVE,
								transaction.getTransactionType() == TransactionType.GIVE ? -transaction.getAmount() : transaction.getAmount(),
								(transaction.getSender().matches("[0-9]{17,18}") ? OtherUtils.getUser(event.getJDA().getUserById(transaction.getSender())) : transaction.getSender()) + " > " + (transaction.getReceiver().matches("[0-9]{17,18}") ? OtherUtils.getUser(event.getJDA().getUserById(transaction.getReceiver())) : transaction.getReceiver()), transaction.getDescription(),
								TimeUtils.neat(System.currentTimeMillis() - transaction.getTime()) + " ago");
					}
					StringBuilder sb = new StringBuilder();
					event.getUserData().getProfile().getBankAccount().getTransactions().forEach(transaction -> sb.append(transaction.getTransactionType() == TransactionType.RECEIVE ? RECEIVE : GIVE).append(" `").append(transaction.getSender()).append("` - Coins: ").append(transaction.getAmount()).append("       ").append(TimeUtils.format0(System.currentTimeMillis() - transaction.getTime())).append(" ago.\n"));
					event.sendMessage(s).queue();
				})
				.build();
	}
}
