package br.com.brjdevs.steven.bran.core.managers;

import br.com.brjdevs.steven.bran.BotContainer;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.requests.RestAction;

public class Messenger {
	
	public BotContainer container;
	
	public Messenger(BotContainer container) {
		this.container = container;
	}
	
	public RestAction<Message> sendMessage(MessageChannel channel, String content) {
		channel.sendTyping().complete();
		if (content.length() > 2000)
			content = "Cannot build a Message with more than 2000 characters. Please limit your input.";
		if (content.isEmpty()) return channel.sendMessage("Something broke!");
		return channel.sendMessage(content);
	}
	
	public RestAction<Message> sendMessage(MessageChannel channel, Message message) {
		channel.sendTyping().complete();
		if (message.getRawContent().length() > 2000)
			return channel.sendMessage("Cannot build a Message with more than 2000 characters. Please limit your input.");
		if (message.getRawContent().isEmpty()) return channel.sendMessage("Something broke!");
		return channel.sendMessage(message);
	}
	
	public RestAction<Message> sendMessage(MessageChannel channel, MessageEmbed embed) {
		channel.sendTyping().complete();
		return channel.sendMessage(embed);
	}
}
