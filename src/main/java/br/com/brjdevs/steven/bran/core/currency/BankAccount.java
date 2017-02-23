package br.com.brjdevs.steven.bran.core.currency;

import br.com.brjdevs.steven.bran.core.currency.Transaction.TransactionType;

import java.util.LinkedList;

public class BankAccount {
	
	private LinkedList<Transaction> transactions;
	private long coins;
	
	public BankAccount() {
		this.transactions = new LinkedList<>();
		this.coins = 0;
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
	
	public void addCoins(long coins, String sender, String receiver, String description) {
		setCoins(getCoins() + coins);
		setLastTransaction(new Transaction(TransactionType.RECEIVE, sender, receiver, description, coins));
	}
	
	public boolean takeCoins(long coinsToTake, String sender, String receiver, String description) {
		if (coinsToTake > getCoins()) {
			return false;
		}
		setCoins(getCoins() - coinsToTake);
		setLastTransaction(new Transaction(TransactionType.GIVE, sender, receiver, description, coins));
		return true;
	}
}
