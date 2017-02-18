package br.com.brjdevs.steven.bran.core.listeners;

import br.com.brjdevs.steven.bran.Client;
import br.com.brjdevs.steven.bran.core.responsewaiter.ExpectedResponseType;
import br.com.brjdevs.steven.bran.core.responsewaiter.ResponseWaiter;
import br.com.brjdevs.steven.bran.core.responsewaiter.events.UnexpectedResponseEvent;
import br.com.brjdevs.steven.bran.core.responsewaiter.events.ValidResponseEvent;
import br.com.brjdevs.steven.bran.core.utils.ArrayUtils;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.EventListener;

public class ActionListener implements EventListener {
	
	private Client client;
	
	public ActionListener(Client client) {
		this.client = client;
	}
	
	
	@Override
	public void onEvent(Event e) {
		ResponseWaiter responseWaiter = null;
		String response = null;
		Object obj = null;
		if (e instanceof MessageReactionAddEvent) {
			responseWaiter = ResponseWaiter.responseWaiters.get(Long.parseLong(((MessageReactionAddEvent) e).getUser().getId()));
			if (responseWaiter != null && responseWaiter.getExpectedResponseType() != ExpectedResponseType.REACTION)
				return;
			response = ((MessageReactionAddEvent) e).getReaction().getEmote().getName();
			obj = ((MessageReactionAddEvent) e).getReaction();
		} else if (e instanceof GuildMessageReceivedEvent) {
			responseWaiter = ResponseWaiter.responseWaiters.get(Long.parseLong(((GuildMessageReceivedEvent) e).getAuthor().getId()));
			if (responseWaiter != null && responseWaiter.getExpectedResponseType() != ExpectedResponseType.MESSAGE)
				return;
			response = ((GuildMessageReceivedEvent) e).getMessage().getRawContent();
			obj = ((GuildMessageReceivedEvent) e).getMessage();
		}
		if (responseWaiter != null) {
			ResponseWaiter.responseWaiters.remove(Long.parseLong(responseWaiter.getUser().getId()));
			ResponseWaiter.EXPIRATION.removeResponseWaiter(responseWaiter);
			if (ArrayUtils.contains(responseWaiter.getValidInputs(), response))
				responseWaiter.getResponseListener().onEvent(new ValidResponseEvent(responseWaiter, obj));
			else
				responseWaiter.getResponseListener().onEvent(new UnexpectedResponseEvent(responseWaiter, obj));
			
		}
	}
}
