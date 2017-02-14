package br.com.brjdevs.steven.bran.core.action;

import br.com.brjdevs.steven.bran.Bot;
import br.com.brjdevs.steven.bran.BotContainer;
import br.com.brjdevs.steven.bran.core.audio.MusicManager;
import br.com.brjdevs.steven.bran.core.managers.Expirator;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Action {
	
	private static final CopyOnWriteArrayList<Action> actions = new CopyOnWriteArrayList<>();
	private static final Expirator EXPIRATOR = new Expirator();
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
	private long guildId;
	@Getter
	private String channelId;
	@Getter
	private ActionType actionType;
	@Getter
	@Setter
	private Object[] extras;
	@Getter
	private onInvalidResponse onInvalidResponse;
	
	public Action(ActionType actionType, onInvalidResponse onInvalidResponse, Message message, IEvent listener, List<String> expectedInput, BotContainer container, long timeout) {
		this.usersId = new ArrayList<>();
		this.expectedInput = expectedInput;
		this.listener = listener;
		this.messageId = message.getId();
		this.channelId = message.getChannel().getId();
		this.actionType = actionType;
		this.container = container;
		this.shardId = container.getShardId(message.getJDA());
		this.guildId = Long.parseLong(message.getGuild().getId());
		this.onInvalidResponse = onInvalidResponse;
		EXPIRATOR.letExpire(System.currentTimeMillis() + timeout, () -> {
			if (actions.contains(this)) {
				remove(this);
				Guild guild = container.getShards()[shardId].getJDA().getGuildById(String.valueOf(guildId));
				if (guild != null) {
					MusicManager musicManager = container.playerManager.get(guild);
					if (musicManager.getTrackScheduler().isStopped())
						guild.getAudioManager().closeAudioConnection();
					TextChannel channel = getChannel();
					if (channel != null) {
						channel.deleteMessageById(getMessageId()).queue();
						container.getMessenger().sendMessage(channel, "You took too long to respond, request timed out!").queue();
					}
				}
			}
		});
		actions.add(this);
	}
	
	public Action(ActionType actionType, onInvalidResponse onInvalidResponse, Message message, IEvent listener, BotContainer container, long timeout, String... expectedInputs) {
		this(actionType, onInvalidResponse, message, listener, Arrays.asList(expectedInputs), container, timeout);
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
