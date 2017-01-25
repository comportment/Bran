package br.com.brjdevs.steven.bran.core.action;

import net.dv8tion.jda.core.entities.Message;

public interface IEvent {
	
	void onRespond(Message message, String... args);
}
