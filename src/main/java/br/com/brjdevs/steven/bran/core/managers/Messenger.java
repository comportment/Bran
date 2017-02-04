package br.com.brjdevs.steven.bran.core.managers;

import br.com.brjdevs.steven.bran.BotContainer;
import br.com.brjdevs.steven.bran.core.quote.Quotes;
import br.com.brjdevs.steven.bran.core.utils.Hastebin;
import net.dv8tion.jda.core.MessageBuilder;
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
			return channel.sendMessage(Quotes.getQuote(Quotes.SUCCESS) + " " + Hastebin.post(content));
		if (content.isEmpty()) return channel.sendMessage("Something broke!");
		return channel.sendMessage(content);
	}
	
	public RestAction<Message> sendMessage(MessageChannel channel, Message message) {
		channel.sendTyping().complete();
		if (message.getRawContent().length() > 2000)
			return channel.sendMessage(new MessageBuilder().setEmbed(message.getEmbeds().get(0)).append(Quotes.getQuote(Quotes.SUCCESS)).append(Hastebin.post(message.getRawContent())).build());
		if (message.getRawContent().isEmpty()) return channel.sendMessage("Something broke!");
		return channel.sendMessage(message);
	}
	
	public RestAction<Message> sendMessage(MessageChannel channel, MessageEmbed embed) {
		channel.sendTyping().complete();
		return channel.sendMessage(embed);
	}
}
