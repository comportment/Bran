package br.com.brjdevs.steven.bran.cmds.currency;

import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.currency.BankAccount;
import br.com.brjdevs.steven.bran.core.currency.Transaction;
import br.com.brjdevs.steven.bran.core.snowflakes.SnowflakeGenerator;
import br.com.brjdevs.steven.bran.core.utils.TimeUtils;
import br.com.brjdevs.steven.bran.core.utils.Utils;

import java.util.LinkedList;

public class TransactionCommands {
	
	private static final String RECEIVE = "\uD83D\uDCE5";
	private static final String GIVE = "\uD83D\uDCE4";
	
	@Command
	private static ICommand transaction() {
		return new CommandBuilder(Category.CURRENCY)
				.setAliases("transactions")
				.setName("Transactions Command")
				.setDescription("Show you your latest transactions!")
				.setAction((event) -> {
					if (event.getUserData().getProfile().getBankAccount().getTransactions().isEmpty()) {
						event.sendMessage("No transactions found in your bank account!").queue();
						return;
					}
					String currentArgs = "Your transactions: \n\n";
					BankAccount bank = event.getUserData().getProfile().getBankAccount();
					LinkedList<Transaction> transactions = event.getUserData().getProfile().getBankAccount().getTransactions();
					for (Transaction transaction : transactions) {
						currentArgs += String.format("%s`\u200B%+4d` %s - %s\n",
								!transaction.senderId.equals(bank.userId) ? RECEIVE : GIVE,
								transaction.senderId.equals(bank.userId) ? -transaction.amount : transaction.amount,
								Utils.getUser(event.getJDA().getUserById(transaction.senderId)) + " > " + Utils.getUser(event.getJDA().getUserById(transaction.receiverId)),
								TimeUtils.neat(System.currentTimeMillis() - SnowflakeGenerator.getDefaultGenerator().getCreationTime(transaction.id)) + " ago");
					}
					event.sendMessage(currentArgs).queue();
				})
				.build();
	}
}
