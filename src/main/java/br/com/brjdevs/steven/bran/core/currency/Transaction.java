package br.com.brjdevs.steven.bran.core.currency;

public class Transaction {
	
	private TransactionType transactionType;
	private long amount;
	private String sender;
	private String receiver;
	private String description;
	private long time;
	
	public Transaction(TransactionType transactionType, String sender, String receiver, String description, long amount) {
		this.transactionType = transactionType;
		this.sender = sender;
		this.receiver = receiver;
		this.description = description;
		this.amount = amount;
		this.time = System.currentTimeMillis();
	}
	
	public TransactionType getTransactionType() {
		return transactionType;
	}
	
	public String getSender() {
		return sender;
	}
	
	public String getReceiver() {
		return receiver;
	}
	
	public String getDescription() {
		return description;
	}
	
	public long getAmount() {
		return amount;
	}
	
	public long getTime() {
		return time;
	}
	
	public enum TransactionType {
		RECEIVE, GIVE, UKNOWN
	}
}
