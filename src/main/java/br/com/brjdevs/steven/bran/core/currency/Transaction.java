package br.com.brjdevs.steven.bran.core.currency;

import br.com.brjdevs.steven.bran.core.data.DataHolder;
import br.com.brjdevs.steven.bran.core.snowflakes.SnowflakeGenerator;

public class Transaction {
	
	public final long amount, id;
	public final String receiverId, senderId;
	
	public Transaction(BankAccount sender, BankAccount receiver, long amount) {
		this.senderId = sender.userId;
		this.receiverId = receiver.userId;
		this.amount = amount;
		this.id = SnowflakeGenerator.getDefaultGenerator().nextId();
	}
	
	public BankAccount getSender(DataHolder data) {
		return data.getUserById(senderId).getProfile().bankAccount;
	}
	
	public BankAccount getReceiver(DataHolder data) {
		return data.getUserById(receiverId).getProfile().bankAccount;
	}
}
