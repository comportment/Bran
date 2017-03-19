package br.com.brjdevs.steven.bran.cmds.fun;

import br.com.brjdevs.steven.bran.core.audio.AudioUtils;
import br.com.brjdevs.steven.bran.core.audio.GuildMusicManager;
import br.com.brjdevs.steven.bran.core.audio.TrackContext;
import br.com.brjdevs.steven.bran.core.audio.TrackScheduler;
import br.com.brjdevs.steven.bran.core.client.Bran;
import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.builders.TreeCommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.managers.Permissions;
import br.com.brjdevs.steven.bran.core.quote.Quotes;
import br.com.brjdevs.steven.bran.core.utils.StringListBuilder;
import br.com.brjdevs.steven.bran.core.utils.StringListBuilder.Format;
import br.com.brjdevs.steven.bran.core.utils.Utils;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackState;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static br.com.brjdevs.steven.bran.core.managers.Permissions.DJ;

public class MusicCommand {
	
	private static final String URL_REGEX = "^((ht|f)tp(s?)://)(\\w+(:\\w+)?@)?((((((25[0-5])|(2[0-4][0-9])|([01]?[0-9]?[0-9]))\\.){3}((25[0-4])|(2[0-4][0-9])|((1?[1-9]?[1-9])|([1-9]0))))|(0\\.){3}0)|([0-9a-z_!~*'()-]+\\.)*([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\\.[a-z]{2,6}|([a-zA-Z][-a-zA-Z0-9]+))(:[0-9]{1,5})?((/?)|(/[0-9a-zA-Z_!~*'().;?:@&=+$,%#-]+)+/?)$";
	private static final String PLAYING = "\u25b6";
	private static final String PAUSED = "\u23f8";
	private static final String REPEAT = "\uD83D\uDD01";
	
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
						.setArgs(new Argument("title/url", String.class, true))
						.setAction((event, args) -> {
							String trackUrl = event.getArgument("title/url").isPresent() ? ((String) event.getArgument("title/url").get()) : "";
							GuildMusicManager musicManager = Bran.getInstance().getMusicManager().get(event.getGuild());
							
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
							VoiceChannel vchan = event.getGuild().getSelfMember().getVoiceState().getChannel();
							if (vchan == null && event.getMember().getVoiceState().inVoiceChannel()) {
								vchan = AudioUtils.connect(event.getMember().getVoiceState().getChannel(), event.getTextChannel());
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
							if (event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_MANAGE))
								event.getMessage().delete().queue();
							TrackScheduler scheduler = Bran.getInstance().getMusicManager().get(event.getGuild()).getTrackScheduler();
							List<TrackContext> tracksByUser = scheduler.getTracksBy(event.getAuthor());
							if (event.getGuildData().maxSongsPerUser > 0 && tracksByUser.size() >= event.getGuildData().maxSongsPerUser) {
								event.sendMessage("You can only have " + event.getGuildData().maxSongsPerUser + " songs in the queue.").queue();
								return;
							}
							if (!trackUrl.matches(URL_REGEX) && !trackUrl.startsWith("ytsearch:") && !trackUrl.startsWith("scsearch:"))
								trackUrl = "ytsearch:" + trackUrl;
							Bran.getInstance().getMusicManager().loadAndPlay(event.getAuthor(), event.getTextChannel(), trackUrl);
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.FUN)
						.setAliases("nowplaying", "nowp", "np", "n")
						.setName("Music NowPlaying Command")
						.setDescription("Gives you information about the current song.")
						.setAction((event) -> {
							GuildMusicManager musicManager = Bran.getInstance().getMusicManager().get(event.getGuild());
							if (musicManager.getTrackScheduler().getCurrentTrack() == null || musicManager.getPlayer().getPlayingTrack() == null) {
								event.sendMessage("I'm not playing anything, use `" + event.getPrefix() + "music play [SONG]` to play something!").queue();
								return;
							}
							AudioTrackInfo info = musicManager.getPlayer().getPlayingTrack().getInfo();
							TrackContext context = musicManager.getTrackScheduler().getCurrentTrack();
							TrackScheduler scheduler = musicManager.getTrackScheduler();
							Iterator<TrackContext> it = scheduler.getQueue().iterator();
							TrackContext next = it.hasNext() ? it.next() : null;
							VoiceChannel channel = event.getGuild().getAudioManager().isAttemptingToConnect() ? event.getGuild().getAudioManager().getQueuedAudioConnection() : event.getGuild().getAudioManager().getConnectedChannel();
							String out = "[Current song information for `" + channel.getName() + "`] \uD83C\uDFB6 " + info.title + "\n\n";
							out += "\uD83D\uDC49 DJ » `" + Utils.getUser(context.getDJ()) + "`\n\n";
							out += (scheduler.isPaused() ? PAUSED : scheduler.isRepeat() ? REPEAT : PLAYING) + " ";
							out += AudioUtils.getProgressBar(context.getTrack().getPosition(), context.getTrack().getInfo().length) + " [" + AudioUtils.format(context.getTrack().getPosition()) + "/" + AudioUtils.format(info.length) + "]";
							if (next != null) {
								info = next.getTrack().getInfo();
								out += "\n\n";
								out += "[Next up in ` " + channel.getName() + "`] \uD83C\uDFB6 " + info.title + "\n\n";
								out += "\uD83D\uDC49 DJ » `" + Utils.getUser(next.getDJ()) + "`\n";
								out += "\uD83D\uDC49 Duration » " + " `" + AudioUtils.format(info.length) + "`";
							}
							event.sendMessage(out).queue();
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.FUN)
						.setAliases("queue", "q", "list")
						.setName("Music Queue Command")
						.setDescription("Lists you the current queue.")
						.setArgs(new Argument("page", Integer.class, true))
						.setAction((event, args) -> {
							Argument argument = event.getArgument("page");
							int page = argument.isPresent() && (int) argument.get() > 0 ? (int) argument.get() : 1;
							GuildMusicManager musicManager = Bran.getInstance().getMusicManager().get(event.getGuild());
							StringBuilder builder = new StringBuilder();
							List<String> list = musicManager.getTrackScheduler().getQueue().stream().map(track -> {
								AudioTrackInfo i = track.getTrack().getInfo();
								return "**" + (musicManager.getTrackScheduler().getPosition(track) + 1) + "**) " + i.title + " » (`" + AudioUtils.format(i.length) + "`)" + " (DJ: `" + Utils.getUser(track.getDJ()) + "`)";
							}).collect(Collectors.toList());
							TrackScheduler scheduler = musicManager.getTrackScheduler();
							VoiceChannel channel = event.getGuild().getAudioManager().isAttemptingToConnect() ? event.getGuild().getAudioManager().getQueuedAudioConnection() : event.getGuild().getAudioManager().getConnectedChannel();
							if (musicManager.getPlayer().getPlayingTrack() != null) {
								AudioTrackInfo info = musicManager.getPlayer().getPlayingTrack().getInfo();
								TrackContext context = musicManager.getTrackScheduler().getCurrentTrack();
								builder.append("[Current song information for `").append(channel == null ? "Not connected." : channel.getName()).append("`] \uD83C\uDFB6 ").append(info.title).append("\n\n");
								builder.append("\uD83D\uDC49 DJ » `").append(Utils.getUser(context.getDJ())).append("`\n\n");
								builder.append(scheduler.isPaused() ? PAUSED : scheduler.isRepeat() ? REPEAT : PLAYING).append(" ");
								builder.append(AudioUtils.getProgressBar(context.getTrack().getPosition(), context.getTrack().getInfo().length)).append(" [").append(AudioUtils.format(context.getTrack().getPosition())).append("/").append(AudioUtils.format(info.length)).append("]");
								builder.append("\n");
							}
							StringListBuilder listBuilder = new StringListBuilder(list, page, 10);
							builder.append("\n[Queue information for `").append(event.getGuild().getName()).append("`] (").append(scheduler.getQueue().size()).append(" entries) - Page ").append(page).append("/").append(listBuilder.getMaxPages()).append("\n\n");
							if (!scheduler.getQueue().isEmpty()) {
								builder.append(listBuilder.format(Format.NONE));
								builder.append("\n\n__Total Queue Duration__: ").append(scheduler.getQueueDuration());
							} else {
								builder.append("The queue is empty.");
							}
							event.sendMessage(builder.toString()).queue();
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.FUN)
						.setAliases("repeat")
						.setName("Repeat Toggle Command")
						.setDescription("Toggles the Repeat.")
						.setRequiredPermission(DJ)
						.setAction((event) -> {
							GuildMusicManager musicManager = Bran.getInstance().getMusicManager().get(event.getGuild());
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
							GuildMusicManager musicManager = Bran.getInstance().getMusicManager().get(event.getGuild());
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
							GuildMusicManager musicManager = Bran.getInstance().getMusicManager().get(event.getGuild());
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
							GuildMusicManager musicManager = Bran.getInstance().getMusicManager().get(event.getGuild());
							musicManager.getTrackScheduler().shuffle();
							event.sendMessage("Shuffled the Queue!").queue();
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
								vchan = AudioUtils.connect(event.getMember().getVoiceState().getChannel(), event.getTextChannel());
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
							GuildMusicManager musicManager = Bran.getInstance().getMusicManager().get(event.getGuild());
							musicManager.getTrackScheduler().restartSong(event.getTextChannel());
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.FUN)
						.setAliases("skip", "s")
						.setName("Music Vote Skip Command")
						.setDescription("Voting for skipping song.")
						.setAction((event) -> {
							GuildMusicManager manager = Bran.getInstance().getMusicManager().get(event.getGuild());
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
							if (manager.getPlayer().getPlayingTrack().getState() != AudioTrackState.PLAYING) {
								event.sendMessage("This track is still loading, please wait!").queue();
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
							if (scheduler.getVoteSkips().size() >= scheduler.getRequiredSkipVotes()) {
								event.sendMessage("Reached the required amount of votes, skipping `" + manager.getPlayer().getPlayingTrack().getInfo().title + "`...").queue();
								scheduler.skip();
								return;
							}
							event.sendMessage(scheduler.getVoteSkips().size() + " users voted for skipping `" + manager.getPlayer().getPlayingTrack().getInfo().title + "`. More `" + (scheduler.getRequiredSkipVotes() - scheduler.getVoteSkips().size()) + "` votes are required.").queue();
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
							GuildMusicManager manager = Bran.getInstance().getMusicManager().get(event.getGuild());
							TrackScheduler scheduler = manager.getTrackScheduler();
							if (manager.getPlayer().getPlayingTrack() == null) {
								event.sendMessage("I'm not playing anything, so I can't skip!").queue();
								return;
							}
							if (manager.getPlayer().getPlayingTrack().getState() != AudioTrackState.PLAYING) {
								event.sendMessage("This track is still loading, please wait!").queue();
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
						.setArgs(new Argument("trackPos", Integer.class))
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
							GuildMusicManager manager = Bran.getInstance().getMusicManager().get(event.getGuild());
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
							if (!toRemove.getDJId().equals(event.getAuthor().getId()) && !event.getGuildData().hasPermission(event.getAuthor(), Permissions.DJ)) {
								event.sendMessage("You can't do this because you're not this song's DJ!").queue();
								return;
							}
							scheduler.getQueue().remove(toRemove);
							event.sendMessage(Quotes.SUCCESS, "Removed `" + toRemove.getTrack().getInfo().title + "` from the queue.").queue();
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
						.setAliases("fairqueue")
						.setName("FairQueue Command")
						.setDescription("Change the FairQueue configs for the Guild.")
						.setArgs(new Argument("level", Integer.class, true))
						.setRequiredPermission(Permissions.GUILD_MOD)
						.setAction((event) -> {
							Argument argument = event.getArgument("level");
							if (!argument.isPresent()) {
								event.sendMessage("The current FairQueue Level for this Guild is `" + event.getGuildData().fairQueueLevel + "`.").queue();
							} else if (((int) argument.get()) > 2) {
								event.sendMessage("The biggest FairQueue Level is 2!").queue();
							} else if (((int) argument.get()) < 0) {
								event.sendMessage("The FairQueue Level has to be bigger or equal 0!").queue();
							} else {
								event.getGuildData().fairQueueLevel = ((int) argument.get());
								event.sendMessage("Done, now the FairQueue Level for this Guild is `" + argument.get() + "`.").queue();
                                Bran.getInstance().getDataManager().getData().update();
                            }
						})
						.build())
				.build();
	}
}
