package br.net.brjdevs.steven.bran.core.currency;

import br.net.brjdevs.steven.bran.core.data.DataHolder;
import br.net.brjdevs.steven.bran.core.snowflakes.SnowflakeGenerator;

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
        return data.getUserDataById(senderId).getProfileData().getBankAccount();
    }
	
	public BankAccount getReceiver(DataHolder data) {
        return data.getUserDataById(receiverId).getProfileData().getBankAccount();
    }
}
