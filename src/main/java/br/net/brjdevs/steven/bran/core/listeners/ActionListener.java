package br.net.brjdevs.steven.bran.core.listeners;

import br.net.brjdevs.steven.bran.core.responsewaiter.ExpectedResponseType;
import br.net.brjdevs.steven.bran.core.responsewaiter.ResponseWaiter;
import br.net.brjdevs.steven.bran.core.responsewaiter.events.UnexpectedResponseEvent;
import br.net.brjdevs.steven.bran.core.responsewaiter.events.ValidResponseEvent;
import br.net.brjdevs.steven.bran.core.utils.ArrayUtils;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;

public class ActionListener extends EventListener<Event> {
	
	public ActionListener() {
		super(Event.class);
	}
	
	@Override
	public void onEvent(Event e) {
		ResponseWaiter responseWaiter = null;
		String response = null;
		Object obj = null;
		if (e instanceof MessageReactionAddEvent) {
			responseWaiter = ResponseWaiter.responseWaiters.get(((MessageReactionAddEvent) e).getUser().getIdLong());
			if (responseWaiter != null && responseWaiter.getExpectedResponseType() != ExpectedResponseType.REACTION)
				return;
			response = ((MessageReactionAddEvent) e).getReaction().getEmote().getName();
			obj = ((MessageReactionAddEvent) e).getReaction();
		} else if (e instanceof GuildMessageReceivedEvent) {
			if (((GuildMessageReceivedEvent) e).getAuthor().isBot() || ((GuildMessageReceivedEvent) e).getAuthor().isFake())
				return;
			responseWaiter = ResponseWaiter.responseWaiters.get(((GuildMessageReceivedEvent) e).getAuthor().getIdLong());
			if (responseWaiter != null && responseWaiter.getExpectedResponseType() != ExpectedResponseType.MESSAGE)
				return;
			response = ((GuildMessageReceivedEvent) e).getMessage().getRawContent();
			obj = ((GuildMessageReceivedEvent) e).getMessage();
		}
		if (responseWaiter != null) {
			ResponseWaiter.responseWaiters.remove(responseWaiter.getUser().getIdLong());
			ResponseWaiter.EXPIRATION.removeResponseWaiter(responseWaiter);
			if (ArrayUtils.contains(responseWaiter.getValidInputs(), response))
				responseWaiter.getResponseListener().onEvent(new ValidResponseEvent(responseWaiter, obj));
			else
				responseWaiter.getResponseListener().onEvent(new UnexpectedResponseEvent(responseWaiter, obj));
			
		}
		
	}
}
