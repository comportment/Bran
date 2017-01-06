package br.com.brjdevs.bran.core.audio;

import br.com.brjdevs.bran.Bot;
import br.com.brjdevs.bran.core.audio.impl.TrackContextImpl;
import br.com.brjdevs.bran.core.audio.utils.AudioUtils;
import br.com.brjdevs.bran.core.utils.MathUtils;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;
import net.dv8tion.jda.core.hooks.EventListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Choice {
	private static final List<Choice> CHOICES = new ArrayList<>();
	private final LinkedList<TrackContext> trackContexts;
	private final Long creationInMillis;
	private final String messageId;
	private final String guildId;
	private final int shard;
	
	public Choice (User user, TextChannel channel, Map<AudioTrack, String> tracks, Message message) {
		this.trackContexts = new LinkedList<>();
		tracks.forEach((track, url) -> trackContexts.add(new TrackContextImpl(track, url, user, channel)));
		this.creationInMillis = System.currentTimeMillis();
		this.messageId = message.getId();
		this.guildId = channel.getGuild().getId();
		this.shard = Bot.getInstance().getShardId(channel.getJDA());
		CHOICES.add(this);
	}
	public long getCreationInMillis() {
		return creationInMillis;
	}
	public JDA getJDA() {
		return Bot.getInstance().getShard(shard);
	}
	public TextChannel getChannel() {
		return trackContexts.get(0).getContext(getJDA());
	}
	public Guild getGuild() {
		return getJDA().getGuildById(guildId);
	}
	public Message getMessage() {
		try {
			return getChannel().getMessageById(messageId).complete();
		} catch (ErrorResponseException e) {
			return null;
		}
	}
	public User getUser(JDA jda) {
		return jda.getUserById(trackContexts.get(0).getDJId());
	}
	public String getUserId() {
		return trackContexts.get(0).getDJId();
	}
	public void remove() {
		boolean isStopped = AudioUtils.getManager().getGuildMusicManager(getGuild()).getTrackScheduler().isStopped();
		if (isStopped && getGuild().getAudioManager().isConnected())
			getGuild().getAudioManager().closeAudioConnection();
		CHOICES.remove(this);
	}
	public static Choice getChoice(User user, TextChannel channel) {
		for (Choice choice : CHOICES) {
			if (choice.getUserId() != null &&
					!choice.getUserId().equals(user.getId())) continue;
			if (choice.getChannel() == null
					|| choice.getUser(user.getJDA()) == null
					|| choice.getMessage() == null) {
				choice.remove();
				continue;
			}
			if (choice.getUser(user.getJDA()).equals(user) && choice.getChannel().equals(channel))
				return choice;
		}
		return null;
	}
	public static List<Choice> getChoices() {
		return CHOICES;
	}
	
	public static class ChoiceListener implements EventListener {
		private static final String CANCEL_REGEX = "^(c|cancel)$";
		private static final String ERROR = "You didn't type `1`, `2`, `3`, `c` or `cancel`, canceled query.";
		@Override
		public void onEvent(Event e) {
			if (!(e instanceof GuildMessageReceivedEvent)) return;
			GuildMessageReceivedEvent event = (GuildMessageReceivedEvent) e;
			Choice choice = Choice.getChoice(event.getAuthor(), event.getChannel());
			if (choice == null) return;
			if (choice.getChannel() != event.getChannel()) return;
			String msg = event.getMessage().getRawContent();
			if (!check(msg)) {
				if (choice.getMessage() != null)
					choice.getMessage().editMessage(ERROR).queue();
				else
					event.getChannel().sendMessage(ERROR).queue();
				choice.remove();
				return;
			}
			if (msg.matches(CANCEL_REGEX)) {
				if (choice.getMessage() != null)
					choice.getMessage().editMessage("Query canceled!").queue();
				else
					event.getChannel().sendMessage("Query canceled!").queue();
				choice.remove();
				return;
			}
			int i = Integer.parseInt(msg);
			if (i > 3) return;
			GuildMusicManager musicManager = AudioUtils.getManager()
					.getGuildMusicManager(event.getGuild());
			TrackScheduler scheduler = musicManager.getTrackScheduler();
			TrackContext track = choice.trackContexts.get(i - 1);
			scheduler.queue(new TrackContextImpl(track.getOrigin(), track.getURL(), event.getAuthor(), event.getChannel()));
			if (choice.getMessage() != null)
				choice.getMessage().deleteMessage().queue();
			if (event.getGuild().getSelfMember().hasPermission(event.getChannel(), Permission.MESSAGE_MANAGE))
				event.getMessage().deleteMessage().queue();
			choice.remove();
		}
		private static boolean check(String string) {
			return (MathUtils.isInteger(string) && MathUtils.isInRange(Integer.parseInt(string), 0, 4)) || string.matches(CANCEL_REGEX);
		}
	}
}
