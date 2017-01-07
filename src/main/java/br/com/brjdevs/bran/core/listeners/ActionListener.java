package br.com.brjdevs.bran.core.listeners;

import br.com.brjdevs.bran.core.action.Action;
import br.com.brjdevs.bran.core.action.ActionType;
import br.com.brjdevs.bran.core.utils.Util;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.EventListener;

public class ActionListener implements EventListener {
	
	@Override
	public void onEvent(Event e) {
		if (e instanceof MessageReactionAddEvent) {
			MessageReactionAddEvent event = (MessageReactionAddEvent) e;
			Action action = Action.getAction(event.getUser().getId());
			String reaction = event.getReaction().getEmote().getName();
			if (action == null || action.getActionType() != ActionType.REACTION || !action.getExpectedInput().contains(reaction))
				return;
			action.getListener().onRespond(reaction);
			Action.remove(action);
		} else if (e instanceof MessageReceivedEvent) {
			MessageReceivedEvent event = (MessageReceivedEvent) e;
			Action action = Action.getAction(event.getAuthor().getId());
			String message = event.getMessage().getRawContent();
			if (action == null || action.getActionType() != ActionType.MESSAGE || !Util.containsEqualsIgnoreCase(action.getExpectedInput(), message))
				return;
			action.getListener().onRespond(message);
			Action.remove(action);
		}
	}
}
