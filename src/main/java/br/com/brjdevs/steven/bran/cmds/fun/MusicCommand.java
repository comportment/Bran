package br.com.brjdevs.steven.bran.cmds.fun;

import br.com.brjdevs.steven.bran.core.audio.MusicManager;
import br.com.brjdevs.steven.bran.core.audio.TrackContext;
import br.com.brjdevs.steven.bran.core.audio.TrackScheduler;
import br.com.brjdevs.steven.bran.core.audio.utils.AudioUtils;
import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.builders.TreeCommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.managers.Permissions;
import br.com.brjdevs.steven.bran.core.quote.Quotes;
import br.com.brjdevs.steven.bran.core.utils.ListBuilder;
import br.com.brjdevs.steven.bran.core.utils.ListBuilder.Format;
import br.com.brjdevs.steven.bran.core.utils.Util;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import static br.com.brjdevs.steven.bran.core.managers.Permissions.DJ;

public class MusicCommand {
	private static final String PLAYING = "\u25b6";
	private static final String PAUSED = "\u23f8";
	
	@Command
	private static ICommand music() {
		return new TreeCommandBuilder(Category.FUN)
				.setAliases("music", "m")
				.setHelp("music ?")
				.setName("Music Command")
				.setDescription("Because everyone loves some music, right?")
				.setExample("music play All We Know - The Chainsmokers")
				.setRequiredPermission(Permissions.MUSIC)
				.setPrivateAvailable(false)
				.addSubCommand(new CommandBuilder(Category.FUN)
						.setAliases("play", "p")
						.setName("Music Play Command")
						.setDescription("Play songs!")
						.setExample("music play Mercy - Shawn Mendes")
						.setArgs(new Argument<>("title/url", String.class, true))
						.setAction((event, args) -> {
							VoiceChannel vchan = event.getGuild().getSelfMember().getVoiceState().getChannel();
							if (vchan == null && event.getMember().getVoiceState().inVoiceChannel()) {
								vchan = AudioUtils.connect(event.getMember().getVoiceState().getChannel(), event.getTextChannel(), event.getBotContainer());
								if (vchan == null) return;
							} else if (vchan == null && !event.getMember().getVoiceState().inVoiceChannel()) {
								event.sendMessage(Quotes.FAIL, "Before asking for songs you should join a Voice Channel ").queue();
								return;
							}
							if (vchan == null) {
								event.sendMessage("Something went wrong!").queue();
								return;
							}
							if (!vchan.getMembers().contains(event.getMember())) {
								event.sendMessage(Quotes.FAIL, "You're not connected to the Voice Channel I am currently playing.").queue();
								return;
							}
							String trackUrl = event.getArgument("title/url").isPresent() ? ((String) event.getArgument("title/url").get()) : "";
							MusicManager musicManager = event.getBotContainer().playerManager.get(event.getGuild());
							
							if (trackUrl.isEmpty()) {
								if (musicManager.getTrackScheduler().isPaused()) {
									musicManager.getTrackScheduler().setPaused(false);
									event.sendMessage("Resumed the Player!").queue();
								} else
									event.sendMessage("You have to tell me a song name or link!").queue();
								return;
							}
							if (musicManager.getTrackScheduler().isPaused()) {
								musicManager.getTrackScheduler().setPaused(false);
								event.sendMessage("Resumed the Player!").queue();
							}
							if (event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_MANAGE))
								event.getMessage().deleteMessage().queue();
							TrackScheduler scheduler = event.getBotContainer().playerManager.get(event.getGuild()).getTrackScheduler();
							List<TrackContext> tracksByUser = scheduler.getTracksBy(event.getAuthor());
							if (event.getDiscordGuild().getMusicSettings().getMaxSongsPerUser() > 0 && tracksByUser.size() >= event.getDiscordGuild().getMusicSettings().getMaxSongsPerUser()) {
								event.sendMessage("You can only have " + event.getDiscordGuild().getMusicSettings().getMaxSongsPerUser() + " songs in the queue.").queue();
								return;
							}
							try {
								new URL(trackUrl);
							} catch (MalformedURLException ignored) {
								trackUrl = "ytsearch:" + trackUrl;
							}
							event.getBotContainer().playerManager.loadAndPlay(event.getAuthor(), event.getTextChannel(), trackUrl);
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.FUN)
						.setAliases("nowplaying", "nowp", "np", "n")
						.setName("Music NowPlaying Command")
						.setDescription("Gives you information about the current song.")
						.setAction((event) -> {
							MusicManager musicManager = event.getBotContainer().playerManager.get(event.getGuild());
							if (musicManager.getPlayer().getPlayingTrack() == null) {
								event.sendMessage("I'm not playing anything, use `" + event.getPrefix() + "music play [SONG]` to play something!").queue();
								return;
							}
							AudioTrackInfo info = musicManager.getPlayer().getPlayingTrack().getInfo();
							TrackContext context = musicManager.getTrackScheduler().getCurrentTrack();
							EmbedBuilder embedBuilder = new EmbedBuilder();
							embedBuilder.setAuthor((musicManager.getTrackScheduler().isPaused() ? PAUSED : PLAYING) + " Now Playing", context.getURL(), null);
							embedBuilder.addField("Title", info.title, false)
									.addField("Duration", AudioUtils.format(context.getTrack().getPosition()) + "/" + AudioUtils.format(info.length), true)
									.addField("Author", info.author, true);
							embedBuilder.addField("DJ", Util.getUser(context.getDJ(event.getJDA())), true);
							if (!musicManager.getTrackScheduler().getQueue().isEmpty() && musicManager.getTrackScheduler().isContinuous()) {
								context = musicManager.getTrackScheduler().getQueue().peek();
								info = context.getTrack().getInfo();
								embedBuilder.addField("Next Up", info.title, false)
										.addField("Duration", AudioUtils.format(info.length), true)
										.addField("Author", info.author, true);
								embedBuilder.addField("DJ", Util.getUser(context.getDJ(event.getJDA())), true);
							}
							embedBuilder.setColor(Color.decode("#b31217"));
							event.sendMessage(embedBuilder.build()).queue();
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.FUN)
						.setAliases("queue", "q", "list")
						.setName("Music Queue Command")
						.setDescription("Lists you the current queue.")
						.setArgs(new Argument<>("page", Integer.class, true))
						.setAction((event, args) -> {
							Argument argument = event.getArgument("page");
							int page = argument.isPresent() && (int) argument.get() > 0 ? (int) argument.get() : 1;
							MusicManager musicManager = event.getBotContainer().playerManager.get(event.getGuild());
							StringBuilder builder = new StringBuilder();
							if (musicManager.getTrackScheduler().getQueue().isEmpty()) {
								event.sendMessage("The queue is empty, use `" + event.getPrefix() + "music play [SONG]` to play something!").queue();
								return;
							}
							List<String> list = musicManager.getTrackScheduler().getQueue().stream().map(track -> {
								AudioTrackInfo i = track.getTrack().getInfo();
								return "`[" + (musicManager.getTrackScheduler().getPosition(track) + 1) + "]`" + i.title + " - " + Util.getUser(track.getDJ(event.getJDA())) + " *(" + AudioUtils.format(i.length) + ")*";
							}).collect(Collectors.toList());
							TrackScheduler scheduler = musicManager.getTrackScheduler();
							builder.append("**__Music Player__**\n");
							builder.append("Repeat: `").append(scheduler.isRepeat()).append("`\n");
							builder.append("Shuffle: `").append(scheduler.isShuffle()).append("`\n");
							builder.append("Paused: `").append(scheduler.isPaused()).append("`\n\n");
							if (musicManager.getPlayer().getPlayingTrack() != null) {
								AudioTrackInfo info = musicManager.getPlayer().getPlayingTrack().getInfo();
								builder.append("**__Now Playing__**\n").append("Title: `" + info.title + "`\n");
								builder.append("Author: `" + info.author + "`\n");
								builder.append("Duration: `" + AudioUtils.format(musicManager.getPlayer().getPlayingTrack().getPosition()) + "/" + AudioUtils.format(info.length) + "`\n");
								builder.append("DJ: `" + Util.getUser(musicManager.getTrackScheduler().getCurrentTrack().getDJ(event.getJDA())) + "`\n");
								builder.append("URL: `" + scheduler.getCurrentTrack().getURL() + "`\n");
							}
							ListBuilder listBuilder = new ListBuilder(list, page, 15);
							builder.append("\n**__Queue__** (" + scheduler.getQueue().size() + " entries) - Page " + page + "/" + listBuilder.getMaxPages() + "\n\n");
							builder.append(listBuilder.format(Format.NONE));
							builder.append("\n\n__Total Queue Duration__: " + scheduler.getQueueDuration());
							event.sendMessage(builder.toString()).queue();
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.FUN)
						.setAliases("repeat")
						.setName("Repeat Toggle Command")
						.setDescription("Toggles the Repeat.")
						.setRequiredPermission(DJ)
						.setAction((event) -> {
							MusicManager musicManager = event.getBotContainer().playerManager.get(event.getGuild());
							musicManager.getTrackScheduler().setRepeat(!musicManager.getTrackScheduler().isRepeat());
							event.sendMessage(musicManager.getTrackScheduler().isRepeat() ? "The player is now on repeat." : "The player is no longer on repeat.").queue();
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.FUN)
						.setAliases("pause")
						.setName("Music (Un)Pause Command")
						.setDescription("Pauses/Resumes the player.")
						.setRequiredPermission(DJ)
						.setAction((event) -> {
							MusicManager musicManager = event.getBotContainer().playerManager.get(event.getGuild());
							if (musicManager.getPlayer().getPlayingTrack() == null) {
								event.sendMessage("I can't pause if I'm not playing anything.").queue();
								return;
							}
							musicManager.getTrackScheduler().setPaused(!musicManager.getTrackScheduler().isPaused());
							event.sendMessage(musicManager.getTrackScheduler().isPaused() ? "The player is now paused. Use `" + event.getPrefix() + "m pause` again to resume." : "The player is no longer paused.").queue();
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.FUN)
						.setAliases("reset", "stop")
						.setName("Music Stop Command")
						.setDescription("Completely stops the Music Player.")
						.setRequiredPermission(DJ)
						.setAction((event) -> {
							MusicManager musicManager = event.getBotContainer().playerManager.get(event.getGuild());
							if (musicManager.getPlayer().getPlayingTrack() == null) {
								event.sendMessage("I can't stop if I'm not playing anything.").queue();
								return;
							}
							musicManager.getTrackScheduler().stop();
							event.sendMessage("The Player has been completely stopped.").queue();
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.FUN)
						.setAliases("shuffle")
						.setName("Music Shuffle Command")
						.setDescription("Set the Queue to Shuffle.")
						.setRequiredPermission(DJ)
						.setAction((event) -> {
							MusicManager musicManager = event.getBotContainer().playerManager.get(event.getGuild());
							musicManager.getTrackScheduler().setShuffle(!musicManager.getTrackScheduler().isShuffle());
							event.sendMessage(musicManager.getTrackScheduler().isShuffle() ? "The player is now set to shuffle." : "The player is no longer set to shuffle.").queue();
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.FUN)
						.setAliases("restart")
						.setName("Music Restart Command")
						.setDescription("If playing, restarts the current song otherwise restarts the last played track.")
						.setRequiredPermission(DJ)
						.setAction((event) -> {
							VoiceChannel vchan = event.getGuild().getSelfMember().getVoiceState().getChannel();
							if (vchan == null && event.getMember().getVoiceState().inVoiceChannel()) {
								vchan = AudioUtils.connect(event.getMember().getVoiceState().getChannel(), event.getTextChannel(), event.getBotContainer());
								if (vchan == null) return;
							} else if (vchan == null && !event.getMember().getVoiceState().inVoiceChannel()) {
								event.sendMessage(Quotes.FAIL, "I'm not connected to a Voice Channel and I failed to track you. Are you even in a voice channel?").queue();
								return;
							}
							if (vchan == null) {
								event.sendMessage("Something went wrong...").queue();
								return;
							}
							if (!vchan.getMembers().contains(event.getMember())) {
								event.sendMessage(Quotes.FAIL, "You're not connected to the Voice Channel I am currently playing.").queue();
								return;
							}
							MusicManager musicManager = event.getBotContainer().playerManager.get(event.getGuild());
							TrackScheduler scheduler = musicManager.getTrackScheduler();
							if (scheduler.getCurrentTrack() != null)
								event.sendMessage("Restarting the current song...").queue();
							else if (scheduler.getPreviousTrack() != null)
								event.sendMessage("Restarting the previous song...").queue();
							else {
								event.sendMessage("The player has never played a song in the last 30 minutes, so it cannot restart a song.").queue();
								event.getGuild().getAudioManager().closeAudioConnection();
								return;
							}
							musicManager.getTrackScheduler().restartSong();
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.FUN)
						.setAliases("skip", "s")
						.setName("Music Vote Skip Command")
						.setDescription("Voting for skipping song.")
						.setAction((event) -> {
							MusicManager manager = event.getBotContainer().playerManager.get(event.getGuild());
							TrackScheduler scheduler = manager.getTrackScheduler();
							VoiceChannel vchan = event.getGuild().getSelfMember().getVoiceState().getChannel();
							if (vchan == null) {
								event.sendMessage(Quotes.FAIL, "I'm not connected to a Voice Channel and I failed to locate you. Are you even in a voice channel?").queue();
								return;
							}
							if (!vchan.getMembers().contains(event.getMember())) {
								event.sendMessage(Quotes.FAIL, "You're not connected to the Voice Channel I am currently playing.").queue();
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
							if (scheduler.getVoteSkips().size() >= scheduler.getRequiredVotes()) {
								event.sendMessage("Reached the required amount of votes, skipping `" + manager.getPlayer().getPlayingTrack().getInfo().title + "`...").queue();
								scheduler.skip();
								return;
							}
							event.sendMessage(scheduler.getVoteSkips().size() + " users voted for skipping `" + manager.getPlayer().getPlayingTrack().getInfo().title + "`. More `" + (scheduler.getRequiredVotes() - scheduler.getVoteSkips().size()) + "` votes are required.").queue();
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.FUN)
						.setAliases("forceskip", "fskip", "fs")
						.setName("Music ForceSkip Command")
						.setDescription("Forces the player to skip the current song.")
						.setRequiredPermission(DJ)
						.setAction((event) -> {
							VoiceChannel vchan = event.getGuild().getSelfMember().getVoiceState().getChannel();
							if (vchan == null) {
								event.sendMessage(Quotes.FAIL, "I'm not connected to a Voice Channel and I failed to locate you. Are you even in a voice channel?").queue();
								return;
							}
							if (!vchan.getMembers().contains(event.getMember())) {
								event.sendMessage(Quotes.FAIL, "You're not connected to the Voice Channel I am currently playing.").queue();
								return;
							}
							MusicManager manager = event.getBotContainer().playerManager.get(event.getGuild());
							TrackScheduler scheduler = manager.getTrackScheduler();
							if (manager.getPlayer().getPlayingTrack() == null) {
								event.sendMessage("I'm not playing anything, so I can't skip!").queue();
								return;
							}
							event.sendMessage("Skipping `" + manager.getPlayer().getPlayingTrack().getInfo().title + "`...").queue();
							scheduler.skip();
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.FUN)
						.setAliases("remove", "r")
						.setName("Music Queue Remove Command")
						.setDescription("Removes a track from the queue.")
						.setArgs(new Argument<>("trackPos", Integer.class))
						.setAction((event) -> {
							Argument argument = event.getArgument("trackPos");
							int index = (int) argument.get() - 1;
							if (index < 0) {
								event.sendMessage("The Track position has to be bigger than 0. You can see all tracks and positions by using `" + event.getPrefix() + "music queue`.").queue();
								return;
							}
							VoiceChannel vchan = event.getGuild().getSelfMember().getVoiceState().getChannel();
							if (vchan == null) {
								event.sendMessage(Quotes.FAIL, "I'm not connected to a Voice Channel and I failed to locate you. Are you even in a voice channel?").queue();
								return;
							}
							if (!vchan.getMembers().contains(event.getMember())) {
								event.sendMessage(Quotes.FAIL, "You're not connected to the Voice Channel I am currently playing.").queue();
								return;
							}
							MusicManager manager = event.getBotContainer().playerManager.get(event.getGuild());
							TrackScheduler scheduler = manager.getTrackScheduler();
							if (scheduler.getQueue().isEmpty()) {
								event.sendMessage("The queue is empty, so you can't remove any songs.").queue();
								return;
							}
							TrackContext toRemove = scheduler.getByPosition(index);
							if (toRemove == null) {
								event.sendMessage("Could not remove from the queue. Reason: `Index is bigger than queue size`").queue();
								return;
							}
							if (!toRemove.getDJId().equals(event.getAuthor().getId()) && !event.getGuildMember().hasPermission(Permissions.DJ, event.getJDA(), event.getBotContainer())) {
								event.sendMessage("You can't do this because you're not this song's DJ!").queue();
								return;
							}
							scheduler.getQueue().remove(toRemove);
							event.sendMessage(Quotes.SUCCESS, "Removed `" + toRemove.getTrack().getInfo().title + "` from the queue.").queue();
						})
						.build())
				.build();
	}
}
