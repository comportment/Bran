package br.com.brjdevs.steven.bran.core.action;

import br.com.brjdevs.steven.bran.Bot;
import br.com.brjdevs.steven.bran.BotContainer;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Action {
	
	private static final List<Action> actions = new ArrayList<>();
	@Getter
	private final List<String> usersId;
	@Getter
	private final List<String> expectedInput;
	private final int shardId;
	public BotContainer container;
	@Getter
	private IEvent listener;
	@Getter
	private String messageId;
	@Getter
	private String channelId;
	@Getter
	private ActionType actionType;
	@Getter
	@Setter
	private Object[] extras;
	@Getter
	private onInvalidResponse onInvalidResponse;
	
	public Action(ActionType actionType, onInvalidResponse onInvalidResponse, Message message, IEvent listener, List<String> expectedInput, BotContainer container) {
		this.usersId = new ArrayList<>();
		this.expectedInput = expectedInput;
		this.listener = listener;
		this.messageId = message.getId();
		this.channelId = message.getChannel().getId();
		this.actionType = actionType;
		this.container = container;
		this.shardId = container.getShardId(message.getJDA());
		this.onInvalidResponse = onInvalidResponse;
		
		actions.add(this);
	}
	
	public Action(ActionType actionType, onInvalidResponse onInvalidResponse, Message message, IEvent listener, BotContainer container, String... expectedInputs) {
		this(actionType, onInvalidResponse, message, listener, Arrays.asList(expectedInputs), container);
	}
	
	public static Action getAction(String userId) {
		return actions.stream().filter(action -> action.getUsersId().contains(userId)).findFirst().orElse(null);
	}
	
	public static void remove(Action action) {
		actions.remove(action);
	}
	
	public Bot getShard() {
		return container.getShards()[shardId];
	}
	
	public TextChannel getChannel() {
		return getShard().getJDA().getTextChannelById(channelId);
	}
	
	public void addUser(User user) {
		if (Action.getAction(user.getId()) != null) remove(getAction(user.getId()));
		usersId.add(user.getId());
	}
	
	public enum onInvalidResponse {
		CONTINUE, CANCEL, IGNORE
	}
}
