package br.net.brjdevs.steven.bran.core.currency;

import net.dv8tion.jda.core.entities.User;

import java.util.LinkedList;

public class BankAccount {
	
	public static final BankAccount MAIN_BANK = new BankAccount("219186621008838669");
	public final String userId;
	private LinkedList<Transaction> transactions;
	private long coins;
	
	public BankAccount(User user) {
		this(user.getId());
	}
	
	public BankAccount(String id) {
		this.transactions = new LinkedList<>();
		this.coins = 0;
		this.userId = id;
	}
	
	private void setLastTransaction(Transaction transaction) {
		if (transactions.size() > 15) transactions.pollLast();
		transactions.offerFirst(transaction);
	}
	
	public LinkedList<Transaction> getTransactions() {
		return transactions;
	}
	
	public long getCoins() {
		return coins;
	}
	
	public void setCoins(long coins) {
		this.coins = coins;
	}
	
	public boolean addCoins(long coins, BankAccount sender) {
		if (getCoins() + coins < 0)
			return false;
		setCoins(getCoins() + coins);
		setLastTransaction(new Transaction(sender, this, coins));
		return true;
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
