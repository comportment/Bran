package br.com.brjdevs.steven.bran.cmds.info;

public class TransactionCommands {
	
	private static final String RECEIVE = "\uD83D\uDCE5";
	private static final String GIVE = "\uD83D\uDCE4";
	
	/*@Command
	private static ICommand transaction() {
		return new CommandBuilder(Category.INFORMATIVE)
				.setAliases("transactions")
				.setName("Transactions Command")
				.setDescription("Show you your latest transactions!")
				.setAction((event) -> {
					if (event.getUserData().getProfile().) == null) {
						event.sendMessage("You should open a bank account first! Use `" + event.getPrefix() + "bank create` to open one!").queue();
						return;
					} else if (event.getUserData().getProfile().getTransactions().isEmpty()) {
						event.sendMessage("No transactions found in your bank account!").queue();
						return;
					}
					String currentArgs = "Your transactions: \n\n";
					LinkedList<Transaction> transactions = event.getUserData().getProfile().getTransactions();
					for (Transaction transaction : transactions) {
						currentArgs += String.format("%currentArgs`\u200B%+4d` %currentArgs `\u200B%18s` - %currentArgs\n",
								transaction.getTransactionType() == TransactionType.RECEIVE ? RECEIVE : GIVE,
								transaction.getTransactionType() == TransactionType.GIVE ? -transaction.getAmount() : transaction.getAmount(),
								(transaction.getSender().matches("[0-9]{17,18}") ? OtherUtils.getUser(event.getJDA().getUserById(transaction.getSender())) : transaction.getSender()) + " > " + (transaction.getReceiver().matches("[0-9]{17,18}") ? OtherUtils.getUser(event.getJDA().getUserById(transaction.getReceiver())) : transaction.getReceiver()), transaction.getDescription(),
								TimeUtils.neat(System.currentTimeMillis() - transaction.getTime()) + " ago");
					}
					event.sendMessage(currentArgs).queue();
				})
				.build();
	}*/
}
