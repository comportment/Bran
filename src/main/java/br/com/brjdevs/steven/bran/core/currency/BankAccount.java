package br.com.brjdevs.steven.bran.core.currency;

import net.dv8tion.jda.core.entities.User;

import java.util.LinkedList;

public class BankAccount {
	
	public static final BankAccount MAIN_BANK = new BankAccount(219186621008838669L);
	public final long userId;
	private LinkedList<Transaction> transactions;
	private long coins;
	
	public BankAccount(User user) {
		this(Long.parseLong(user.getId()));
	}
	
	public BankAccount(long id) {
		this.transactions = new LinkedList<>();
		this.coins = 0;
		this.userId = id;
	}
	
	private void setLastTransaction(Transaction transaction) {
		if (transactions.size() == 25) transactions.pollLast();
		transactions.offerFirst(transaction);
	}
	
	public LinkedList<Transaction> getTransactions() {
		return transactions;
	}
	
	public long getCoins() {
		return coins;
	}
	
	private void setCoins(long coins) {
		this.coins = coins;
	}
	
	public void addCoins(long coins, BankAccount sender) {
		setCoins(getCoins() + coins);
		setLastTransaction(new Transaction(sender, this, coins));
	}
	
	public boolean takeCoins(long coinsToTake, BankAccount receiver) {
		if (coinsToTake > getCoins()) {
			return false;
		}
		setCoins(getCoins() - coinsToTake);
		setLastTransaction(new Transaction(this, receiver, coins));
		return true;
	}
}
