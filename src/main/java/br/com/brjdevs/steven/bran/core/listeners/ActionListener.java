package br.com.brjdevs.steven.bran.core.listeners;

import br.com.brjdevs.steven.bran.core.action.Action;
import br.com.brjdevs.steven.bran.core.action.Action.onInvalidResponse;
import br.com.brjdevs.steven.bran.core.action.ActionType;
import br.com.brjdevs.steven.bran.core.utils.StringUtils;
import br.com.brjdevs.steven.bran.refactor.BotContainer;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.EventListener;

import java.util.stream.Collectors;

import static br.com.brjdevs.steven.bran.core.utils.Util.containsEqualsIgnoreCase;

public class ActionListener implements EventListener {
	
	private BotContainer container;
	
	public ActionListener(BotContainer container) {
		this.container = container;
	}
	
	
	@Override
	public void onEvent(Event e) {
		if (e instanceof MessageReactionAddEvent) {
			MessageReactionAddEvent event = (MessageReactionAddEvent) e;
			Action action = Action.getAction(event.getUser().getId());
			String reaction = event.getReaction().getEmote().getName();
			if (action == null || action.getActionType() != ActionType.REACTION || !action.getMessageId().equals(event.getMessageId()))
				return;
			Action.remove(action);
			if (!containsEqualsIgnoreCase(action.getExpectedInput(), reaction) && action.getOnInvalidResponse() == onInvalidResponse.IGNORE) {
				return;
			} else if (!containsEqualsIgnoreCase(action.getExpectedInput(), reaction) && action.getOnInvalidResponse() == onInvalidResponse.CANCEL) {
				action.getChannel().sendMessage("You didn't type " + StringUtils.replaceLast((String.join(", ", action.getExpectedInput().stream().map(s -> "`" + s + "`").collect(Collectors.toList()))), ", ", " or ") + ", query canceled!").queue();
			} else {
				action.getListener().onRespond(event.getChannel().getMessageById(event.getMessageId()).complete(), event.getReaction().getEmote().getName());
			}
		} else if (e instanceof MessageReceivedEvent) {
			MessageReceivedEvent event = (MessageReceivedEvent) e;
			if (event.isFromType(ChannelType.PRIVATE)) return;
			Action action = Action.getAction(event.getAuthor().getId());
			String message = event.getMessage().getRawContent();
			if (action == null || action.getActionType() != ActionType.MESSAGE || !action.getChannelId().equals(event.getTextChannel().getId()))
				return;
			Action.remove(action);
			if (!containsEqualsIgnoreCase(action.getExpectedInput(), message) && action.getOnInvalidResponse() == onInvalidResponse.IGNORE) {
				return;
			} else if (!containsEqualsIgnoreCase(action.getExpectedInput(), message) && action.getOnInvalidResponse() == onInvalidResponse.CANCEL) {
				action.getChannel().sendMessage("You didn't type " + StringUtils.replaceLast((String.join(", ", action.getExpectedInput().stream().map(s -> "`" + s + "`").collect(Collectors.toList()))), ", ", " or ") + ", query canceled!").queue();
			} else {
				action.getListener().onRespond(event.getMessage());
			}
		}
	}
}
