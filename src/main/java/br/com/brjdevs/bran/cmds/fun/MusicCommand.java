package br.com.brjdevs.bran.cmds.fun;

import br.com.brjdevs.bran.Bot;
import br.com.brjdevs.bran.core.Permissions;
import br.com.brjdevs.bran.core.audio.GuildMusicManager;
import br.com.brjdevs.bran.core.audio.TrackContext;
import br.com.brjdevs.bran.core.audio.TrackScheduler;
import br.com.brjdevs.bran.core.audio.utils.AudioUtils;
import br.com.brjdevs.bran.core.command.*;
import br.com.brjdevs.bran.core.messageBuilder.AdvancedMessageBuilder.Quote;
import br.com.brjdevs.bran.core.utils.ListBuilder;
import br.com.brjdevs.bran.core.utils.ListBuilder.Format;
import br.com.brjdevs.bran.core.utils.StringUtils;
import br.com.brjdevs.bran.core.utils.Util;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

import static br.com.brjdevs.bran.core.Permissions.DJ;

@RegisterCommand
public class MusicCommand {
	private static final String PLAYING = "\u25b6";
	private static final String PAUSED = "\u23f8";
	public MusicCommand() {
		CommandManager.addCommand(new TreeCommandBuilder(Category.FUN)
				.setAliases("music", "m")
				.setHelp("music ?")
				.setName("Music Command")
				.setExample("music play All We Know - The Chainsmokers")
				.setRequiredPermission(Permissions.MUSIC)
				.setPrivateAvailable(false)
				.addCommand(new CommandBuilder(Category.FUN)
						.setAliases("play", "p")
						.setName("Music Play Command")
						.setDescription("Play songs!")
						.setExample("music play Mercy - Shawn Mendes")
						.setArgs("[title/url]")
						.setAction((event, args) -> {
							VoiceChannel vchan = event.getOriginGuild().getSelfMember().getVoiceState().getChannel();
							if (vchan == null && event.getOriginMember().getVoiceState().inVoiceChannel()) {
								vchan = AudioUtils.connect(event.getOriginMember().getVoiceState().getChannel(), event.getTextChannel());
								if (vchan == null) return;
							} else if (vchan == null && !event.getOriginMember().getVoiceState().inVoiceChannel()) {
								event.sendMessage(Quote.getQuote(Quote.FAIL) + "Before asking for songs join a Voice Channel ").queue();
								return;
							}
							if (vchan == null) {
								event.sendMessage("Something went wrong...").queue();
								Bot.LOG.warn("Null Voice Channel in " + event.getOriginGuild().getName() + " from " + event.getOriginMember().getUser().getName());
								return;
							}
							if (!vchan.getMembers().contains(event.getOriginMember())) {
								event.sendMessage(Quote.getQuote(Quote.FAIL) + "You're not connected to the Voice Channel I am currently playing.").queue();
								return;
							}
							String trackUrl = StringUtils.splitArgs(args, 2)[1].trim();
							GuildMusicManager musicManager = AudioUtils.getManager().getGuildMusicManager(event.getOriginGuild());
							if (trackUrl.isEmpty()) {
								if (!musicManager.getPlayer().isPaused())
									event.sendMessage("You have to tell me a Song Name o URL!").queue();
								else {
									musicManager.getPlayer().setPaused(false);
									event.sendMessage("Resumed the Player!").queue();
								}
								return;
							}
							event.getMessage().deleteMessage();
							AudioUtils.getManager().loadAndPlay(event.getAuthor(), event.getTextChannel(), trackUrl);
						})
						.build())
				.addCommand(new CommandBuilder(Category.FUN)
						.setAliases("nowplaying", "nowp", "np", "n")
						.setName("Music NowPlaying Command")
						.setDescription("Gives you information about the current song.")
						.setAction((event) -> {
							GuildMusicManager musicManager = AudioUtils.getManager().getGuildMusicManager(event.getOriginGuild());
							if (musicManager.getPlayer().getPlayingTrack() == null) {
								event.sendMessage("I'm not playing anything, use `" + event.getPrefix() + "music play [SONG]` to play something!").queue();
								return;
							}
							AudioTrackInfo info = musicManager.getPlayer().getPlayingTrack().getInfo();
							TrackContext context = musicManager.getTrackScheduler().getCurrentTrack();
							EmbedBuilder embedBuilder = new EmbedBuilder();
							embedBuilder.setAuthor((musicManager.getPlayer().isPaused() ? PAUSED : PLAYING) + " Now Playing", "https://www.youtube.com/watch?v=" + info.identifier, null);
							embedBuilder.addField("Title", info.title, false)
									.addField("Duration", AudioUtils.format(context.getOrigin().getPosition()) + "/" + AudioUtils.format(info.length), true)
									.addField("Author", info.author, true);
							embedBuilder.addField("DJ", Util.getUser(context.getDJ(event.getJDA())), true);
							if (!musicManager.getTrackScheduler().getQueue().isEmpty() && musicManager.getTrackScheduler().isContinuous()) {
								context = musicManager.getTrackScheduler().getQueue().peek();
								info = context.getOrigin().getInfo();
								embedBuilder.addField("Next Up", info.title, false)
										.addField("Duration", AudioUtils.format(info.length), true)
										.addField("Author", info.author, true);
								embedBuilder.addField("DJ", Util.getUser(context.getDJ(event.getJDA())), true);
							}
							embedBuilder.setColor(Color.decode("#b31217"));
							event.sendMessage(embedBuilder.build()).queue();
						})
						.build())
				.addCommand(new CommandBuilder(Category.FUN)
						.setAliases("volume", "v")
						.setName("Music Volume Command")
						.setDescription("Changes the Volume.")
						.setExample("music volume 80")
						.setArgs("<0-100>")
						.setRequiredPermission(DJ)
						.setAction((event, args) -> {
							GuildMusicManager musicManager = AudioUtils.getManager().getGuildMusicManager(event.getOriginGuild());
							if (StringUtils.splitSimple(args).length == 1) {
								event.sendMessage("\uD83D\uDD09 " + musicManager.getPlayer().getVolume()).queue();
								return;
							}
							int volume = 1;
							try {
								volume = Integer.parseInt(StringUtils.splitSimple(args)[1]);
							} catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
							}
							if (volume < 1) {
								event.sendMessage("You can't change the volume to 0 or lower.").queue();
								return;
							}
							musicManager.getPlayer().setVolume(volume);
							event.sendMessage("Changed volume to `" + volume + "`." + (volume > 100 ? "\n*Warning: Volume above 100 may cause unpleasant noises.*" : "")).queue();
						})
						.build())
				.addCommand(new CommandBuilder(Category.FUN)
						.setAliases("queue", "q")
						.setName("Music Queue Command")
						.setDescription("Lists you the current queue.")
						.setAction((event, args) -> {
							int page = 1;
							try {
								page = Integer.parseInt(StringUtils.splitSimple(args)[1]);
							} catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
							}
							if (page == 0) page = 1;
							GuildMusicManager musicManager = AudioUtils.getManager().getGuildMusicManager(event.getOriginGuild());
							StringBuilder builder = new StringBuilder();
							if (musicManager.getTrackScheduler().getQueue().isEmpty()) {
								event.sendMessage("The queue is empty, use `" + event.getPrefix() + "music play [SONG]` to play something!").queue();
								return;
							}
							List<String> list = musicManager.getTrackScheduler().getQueue().stream().map(track -> {
								AudioTrackInfo i = track.getOrigin().getInfo();
								return "`[" + musicManager.getTrackScheduler().getPosition(track) + "]`" + i.title + " - " + Util.getUser(track.getDJ(event.getJDA())) + " *(" + AudioUtils.format(i.length) + ")*";
							}).collect(Collectors.toList());
							TrackScheduler scheduler = musicManager.getTrackScheduler();
							if (musicManager.getPlayer().getPlayingTrack() != null) {
								AudioTrackInfo info = musicManager.getPlayer().getPlayingTrack().getInfo();
								builder.append("__Now Playing__\n").append("Title: `" + info.title + "`\n");
								builder.append("Author: `" + info.author + "`\n");
								builder.append("Duration: `" + AudioUtils.format(musicManager.getPlayer().getPlayingTrack().getPosition()) + "/" + AudioUtils.format(info.length) + "`\n");
								builder.append("DJ: `" + Util.getUser(musicManager.getTrackScheduler().getCurrentTrack().getDJ(event.getJDA())) + "`\n");
								builder.append("URL: `https://www.youtube.com/watch?v=" + info.identifier + "`\n\n");
							}
							builder.append("__Queue__ (" + scheduler.getQueue().size() + " entries) - Page " + page + "/" + (list.size() / 15 + 1) + "\n\n");
							ListBuilder listBuilder = new ListBuilder(list, page, 15);
							builder.append(listBuilder.format(Format.NONE));
							builder.append("\n\n__Total Queue Duration__: " + scheduler.getQueueDuration());
							event.sendMessage(builder.toString()).queue();
						})
						.build())
				.addCommand(new CommandBuilder(Category.FUN)
						.setAliases("repeat")
						.setName("Repeat Toggle Command")
						.setDescription("Toggles the Repeat.")
						
						.setRequiredPermission(DJ)
						.setAction((event) -> {
							GuildMusicManager musicManager = AudioUtils.getManager().getGuildMusicManager(event.getOriginGuild());
							musicManager.getTrackScheduler().setRepeat(!musicManager.getTrackScheduler().isRepeat());
							event.sendMessage(musicManager.getTrackScheduler().isRepeat() ? "The player is now on repeat." : "The player is no longer on repeat.").queue();
						})
						.build())
				.addCommand(new CommandBuilder(Category.FUN)
						.setAliases("pause")
						.setName("Music (Un)Pause Command")
						.setDescription("Pauses/Resumes the player.")
						.setRequiredPermission(DJ)
						.setAction((event) -> {
							GuildMusicManager musicManager = AudioUtils.getManager().getGuildMusicManager(event.getOriginGuild());
							if (musicManager.getPlayer().getPlayingTrack() == null) {
								event.sendMessage("I can't pause if I'm not playing anything.").queue();
								return;
							}
							musicManager.getPlayer().setPaused(!musicManager.getPlayer().isPaused());
							event.sendMessage(musicManager.getPlayer().isPaused() ? "The player is now paused. Use `" + event.getPrefix() + "m pause` again to resume." : "The player is no longer paused.").queue();
						})
						.build())
				.addCommand(new CommandBuilder(Category.FUN)
						.setAliases("reset", "stop")
						.setName("Music Stop Command")
						.setDescription("Completely stops the Music Player.")
						
						.setRequiredPermission(DJ)
						.setAction((event) -> {
							GuildMusicManager musicManager = AudioUtils.getManager().getGuildMusicManager(event.getOriginGuild());
							if (musicManager.getPlayer().getPlayingTrack() == null) {
								event.sendMessage("I can't stop if I'm not playing anything.").queue();
								return;
							}
							musicManager.getTrackScheduler().stop();
							event.sendMessage("The Player has been completely stopped.").queue();
						})
						.build())
				.addCommand(new CommandBuilder(Category.FUN)
						.setAliases("shuffle")
						.setName("Music Shuffle Command")
						.setDescription("Set the Queue to Shuffle.")
						.setRequiredPermission(DJ)
						.setAction((event) -> {
							GuildMusicManager musicManager = AudioUtils.getManager().getGuildMusicManager(event.getOriginGuild());
							musicManager.getTrackScheduler().setShuffle(!musicManager.getTrackScheduler().isShuffle());
							event.sendMessage(musicManager.getTrackScheduler().isShuffle() ? "The player is now set to shuffle." : "The player is no longer set to shuffle.").queue();
						})
						.build())
				.addCommand(new CommandBuilder(Category.FUN)
						.setAliases("restart")
						.setName("Music Restart Command")
						.setDescription("If playing, restarts the current song otherwise restarts the last played track.")
						.setRequiredPermission(DJ)
						.setAction((event) -> {
							VoiceChannel vchan = event.getOriginGuild().getSelfMember().getVoiceState().getChannel();
							if (vchan == null && event.getOriginMember().getVoiceState().inVoiceChannel()) {
								vchan = AudioUtils.connect(event.getOriginMember().getVoiceState().getChannel(), event.getTextChannel());
								if (vchan == null) return;
							} else if (vchan == null && !event.getOriginMember().getVoiceState().inVoiceChannel()) {
								event.sendMessage(Quote.getQuote(Quote.FAIL) + "I'm not connected to a Voice Channel and I failed to track you. Are you even in a voice channel?").queue();
								return;
							}
							if (vchan == null) {
								event.sendMessage("Something went wrong...").queue();
								Bot.LOG.warn("Null Voice Channel in " + event.getOriginGuild().getName() + " from " + event.getOriginMember().getUser().getName());
								return;
							}
							if (!vchan.getMembers().contains(event.getOriginMember())) {
								event.sendMessage(Quote.getQuote(Quote.FAIL) + "You're not connected to the Voice Channel I am currently playing.").queue();
								return;
							}
							GuildMusicManager musicManager = AudioUtils.getManager().getGuildMusicManager(event.getOriginGuild());
							TrackScheduler scheduler = musicManager.getTrackScheduler();
							if (scheduler.getCurrentTrack() != null)
								event.sendMessage("Restarting the current song...").queue();
							else if (scheduler.getPreviousTrack() != null)
								event.sendMessage("Restarting the previous song...").queue();
							else {
								event.sendMessage("The player has never played a song, so it cannot restart a song.").queue();
								return;
							}
							musicManager.getTrackScheduler().restartSong();
						})
						.build())
				.addCommand(new CommandBuilder(Category.FUN)
						.setAliases("skip", "s")
						.setName("Music Vote Skip Command")
						.setDescription("Voting for skipping song.")
						.setAction((event) -> {
							GuildMusicManager manager = AudioUtils.getManager().getGuildMusicManager(event.getOriginGuild());
							TrackScheduler scheduler = manager.getTrackScheduler();
							VoiceChannel vchan = event.getOriginGuild().getSelfMember().getVoiceState().getChannel();
							if (vchan == null) {
								event.sendMessage(Quote.getQuote(Quote.FAIL) + "I'm not connected to a Voice Channel and I failed to locate you. Are you even in a voice channel?").queue();
								return;
							}
							if (!vchan.getMembers().contains(event.getOriginMember())) {
								event.sendMessage(Quote.getQuote(Quote.FAIL) + "You're not connected to the Voice Channel I am currently playing.").queue();
								return;
							}
							if (manager.getPlayer().getPlayingTrack() == null) {
								event.sendMessage("I'm not playing anything! Use `" + event.getPrefix() + "music play [SONG]` to play something!").queue();
								return;
							}
							TrackContext context = scheduler.getCurrentTrack();
							if (AudioUtils.isAllowed(event.getAuthor(), context)) {
								event.sendMessage("The DJ has decided to skip!").queue();
								scheduler.skip();
								return;
							}
							if (scheduler.getVoteSkips().contains(event.getAuthor().getId()))
								scheduler.getVoteSkips().remove(event.getAuthor().getId());
							else
								scheduler.getVoteSkips().add(event.getAuthor().getId());
							if (scheduler.getVoteSkips().size() >= scheduler.getRequiredVotes(vchan)) {
								event.sendMessage("Reached the required amount of votes, skipping `" + manager.getPlayer().getPlayingTrack().getInfo().title + "`...").queue();
								scheduler.skip();
								return;
							}
							event.sendMessage(scheduler.getVoteSkips().size() + " users voted for skipping `" + manager.getPlayer().getPlayingTrack().getInfo().title + "`. More `" + (scheduler.getRequiredVotes(vchan) - scheduler.getVoteSkips().size()) + "` votes are required.").queue();
						})
						.build())
				.addCommand(new CommandBuilder(Category.FUN)
						.setAliases("forceskip", "fskip", "fs")
						.setName("Music ForceSkip Command")
						.setDescription("Forces the player to skip the current song.")
						.setRequiredPermission(DJ)
						.setAction((event) -> {
							VoiceChannel vchan = event.getOriginGuild().getSelfMember().getVoiceState().getChannel();
							if (vchan == null) {
								event.sendMessage(Quote.getQuote(Quote.FAIL) + "I'm not connected to a Voice Channel and I failed to locate you. Are you even in a voice channel?").queue();
								return;
							}
							if (!vchan.getMembers().contains(event.getOriginMember())) {
								event.sendMessage(Quote.getQuote(Quote.FAIL) + "You're not connected to the Voice Channel I am currently playing.").queue();
								return;
							}
							GuildMusicManager manager = AudioUtils.getManager().getGuildMusicManager(event.getOriginGuild());
							TrackScheduler scheduler = manager.getTrackScheduler();
							if (manager.getPlayer().getPlayingTrack() == null) {
								event.sendMessage("I'm not playing anything, so I can't skip!").queue();
								return;
							}
							event.sendMessage("Skipping `" + manager.getPlayer().getPlayingTrack().getInfo().title + "`...").queue();
							scheduler.skip();
						})
						.build())
				.addCommand(new CommandBuilder(Category.FUN)
						.setAliases("remove", "r")
						.setName("Music Queue Remove Command")
						.setDescription("Removes a track from the queue.")
						.setArgs("[track queue position]")
						.setRequiredPermission(DJ)
						.setAction((event) -> {
							int index;
							try {
								index = Integer.parseInt(event.getArgs(2)[1]);
							} catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
								event.sendMessage(Quote.getQuote(Quote.FAIL) + "You have to tell me a valid queue index to remove!").queue();
								return;
							}
							index--;
							if (index < 0) {
								event.sendMessage("Invalid index.").queue();
								return;
							}
							VoiceChannel vchan = event.getOriginGuild().getSelfMember().getVoiceState().getChannel();
							if (vchan == null) {
								event.sendMessage(Quote.getQuote(Quote.FAIL) + "I'm not connected to a Voice Channel and I failed to locate you. Are you even in a voice channel?").queue();
								return;
							}
							if (!vchan.getMembers().contains(event.getOriginMember())) {
								event.sendMessage(Quote.getQuote(Quote.FAIL) + "You're not connected to the Voice Channel I am currently playing.").queue();
								return;
							}
							GuildMusicManager manager = AudioUtils.getManager().getGuildMusicManager(event.getOriginGuild());
							TrackScheduler scheduler = manager.getTrackScheduler();
							if (scheduler.getQueue().isEmpty()) {
								event.sendMessage("The queue is empty, so you can't remove any songs.").queue();
								return;
							}
							TrackContext removed = scheduler.removeAt(index);
							if (removed == null) {
								event.sendMessage("Could not remove from the queue. Reason: `Index is bigger than queue size`").queue();
								return;
							}
							event.sendMessage(Quote.getQuote(Quote.SUCCESS) + "Removed `" + removed.getOrigin().getInfo().title + "` from the queue.").queue();
						})
						.build())
				.build());
	}
}
