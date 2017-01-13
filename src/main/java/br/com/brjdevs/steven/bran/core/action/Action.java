package br.com.brjdevs.steven.bran.core.action;

import br.com.brjdevs.steven.bran.Bot;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.core.JDA;
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
	
	public Action(ActionType actionType, onInvalidResponse onInvalidResponse, Message message, IEvent listener, List<String> expectedInput) {
		this.usersId = new ArrayList<>();
		this.expectedInput = expectedInput;
		this.listener = listener;
		this.messageId = message.getId();
		this.channelId = message.getChannel().getId();
		this.actionType = actionType;
		this.shardId = Bot.getInstance().getShardId(message.getJDA());
		this.onInvalidResponse = onInvalidResponse;
		
		actions.add(this);
	}
	
	public Action(ActionType actionType, onInvalidResponse onInvalidResponse, Message message, IEvent listener, String... expectedInputs) {
		this(actionType, onInvalidResponse, message, listener, Arrays.asList(expectedInputs));
	}
	
	public static Action getAction(String userId) {
		return actions.stream().filter(action -> action.getUsersId().contains(userId)).findFirst().orElse(null);
	}
	
	public static void remove(Action action) {
		actions.remove(action);
	}
	
	public JDA getJDA() {
		return Bot.getInstance().getShard(shardId);
	}
	
	public TextChannel getChannel() {
		return getJDA().getTextChannelById(channelId);
	}
	
	public void addUser(User user) {
		usersId.add(user.getId());
	}
	
	public enum onInvalidResponse {
		CONTINUE, CANCEL, IGNORE
	}
}
