package br.net.brjdevs.steven.bran.cmds.music;

import br.net.brjdevs.steven.bran.core.audio.AudioUtils;
import br.net.brjdevs.steven.bran.core.audio.GuildMusicManager;
import br.net.brjdevs.steven.bran.core.audio.TrackContext;
import br.net.brjdevs.steven.bran.core.audio.TrackScheduler;
import br.net.brjdevs.steven.bran.core.client.Bran;
import br.net.brjdevs.steven.bran.core.command.Argument;
import br.net.brjdevs.steven.bran.core.command.Command;
import br.net.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.net.brjdevs.steven.bran.core.command.builders.TreeCommandBuilder;
import br.net.brjdevs.steven.bran.core.command.enums.Category;
import br.net.brjdevs.steven.bran.core.command.enums.CommandAction;
import br.net.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.net.brjdevs.steven.bran.core.managers.Permissions;
import br.net.brjdevs.steven.bran.core.quote.Quotes;
import br.net.brjdevs.steven.bran.core.utils.StringListBuilder;
import br.net.brjdevs.steven.bran.core.utils.StringListBuilder.Format;
import br.net.brjdevs.steven.bran.core.utils.Utils;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackState;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class MusicCommand {
    
    private static final String URL_REGEX = "^((ht|f)tp(s?)://)(\\w+(:\\w+)?@)?((((((25[0-5])|(2[0-4][0-9])|([01]?[0-9]?[0-9]))\\.){3}((25[0-4])|(2[0-4][0-9])|((1?[1-9]?[1-9])|([1-9]0))))|(0\\.){3}0)|([0-9a-z_!~*'()-]+\\.)*([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\\.[a-z]{2,6}|([a-zA-Z][-a-zA-Z0-9]+))(:[0-9]{1,5})?((/?)|(/[0-9a-zA-Z_!~*'().;?:@&=+$,%#-]+)+/?)$";
    private static final String PLAYING = "\u25b6";
    private static final String PAUSED = "\u23f8";
    private static final String REPEAT = "\uD83D\uDD01";
    
    @Command
    private static ICommand play() {
        return new CommandBuilder(Category.MUSIC)
                .setAliases("play", "p")
                .setName("Play Command")
                .setDescription("Play the songs you want!")
                .setExample("play Catch & Release")
                .setRequiredPermission(Permissions.MUSIC)
                .setPrivateAvailable(false)
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
                    if (event.getGuildData(true).maxSongsPerUser > 0 && tracksByUser.size() >= event.getGuildData(true).maxSongsPerUser) {
                        event.sendMessage("You can only have " + event.getGuildData(true).maxSongsPerUser + " songs in the queue.").queue();
                        return;
                    }
                    if (trackUrl.startsWith("soundcloud "))
                        trackUrl = trackUrl.replaceFirst("soundcloud ", "scsearch:");
                    if (!trackUrl.matches(URL_REGEX) && !trackUrl.startsWith("ytsearch:") && !trackUrl.startsWith("scsearch:"))
                        trackUrl = "ytsearch:" + trackUrl;
                    Bran.getInstance().getMusicManager().loadAndPlay(event.getAuthor(), event.getTextChannel(), trackUrl, false);
                })
                .build();
    }
    
    @Command
    private static ICommand forceskip() {
        return new CommandBuilder(Category.MUSIC)
                .setAliases("forceskip", "fskip", "fs")
                .setName("Music ForceSkip Command")
                .setDescription("Forces the player to skip the current song.")
                .setPrivateAvailable(false)
                .setRequiredPermission(Permissions.DJ)
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
                .build();
    }
    
    @Command
    private static ICommand nowplaying() {
        return new CommandBuilder(Category.MUSIC)
                .setAliases("nowplaying", "nowp", "np", "n")
                .setName("Music NowPlaying Command")
                .setDescription("Gives you information about the current song.")
                .setPrivateAvailable(false)
                .setAction((event) -> {
                    GuildMusicManager musicManager = Bran.getInstance().getMusicManager().get(event.getGuild());
                    if (musicManager.getTrackScheduler().getCurrentTrack() == null || musicManager.getPlayer().getPlayingTrack() == null) {
                        event.sendMessage("I'm not playing anything, use `" + event.getPrefix() + "play [SONG]` to play something!").queue();
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
                .build();
    }
    
    @Command
    private static ICommand repeat() {
        return new CommandBuilder(Category.MUSIC)
                .setAliases("repeat")
                .setName("Repeat Toggle Command")
                .setDescription("Toggles the Repeat.")
                .setPrivateAvailable(false)
                .setRequiredPermission(Permissions.DJ)
                .setAction((event) -> {
                    GuildMusicManager musicManager = Bran.getInstance().getMusicManager().get(event.getGuild());
                    musicManager.getTrackScheduler().setRepeat(!musicManager.getTrackScheduler().isRepeat());
                    event.sendMessage(musicManager.getTrackScheduler().isRepeat() ? "The player is now on repeat." : "The player is no longer on repeat.").queue();
                })
                .build();
    }
    
    @Command
    private static ICommand pause() {
        return new CommandBuilder(Category.MUSIC)
                .setAliases("pause")
                .setName("Music (Un)Pause Command")
                .setDescription("Pauses/Resumes the player.")
                .setPrivateAvailable(false)
                .setRequiredPermission(Permissions.DJ)
                .setAction((event) -> {
                    GuildMusicManager musicManager = Bran.getInstance().getMusicManager().get(event.getGuild());
                    if (musicManager.getPlayer().getPlayingTrack() == null) {
                        event.sendMessage("I can't pause if I'm not playing anything.").queue();
                        return;
                    }
                    musicManager.getTrackScheduler().setPaused(!musicManager.getTrackScheduler().isPaused());
                    event.sendMessage(musicManager.getTrackScheduler().isPaused() ? "The player is now paused. Use `" + event.getPrefix() + "pause` again to resume." : "The player is no longer paused.").queue();
                })
                .build();
    }
    
    @Command
    private static ICommand skip() {
        return new CommandBuilder(Category.MUSIC)
                .setAliases("skip", "s")
                .setName("Music Vote Skip Command")
                .setDescription("Voting for skipping song.")
                .setPrivateAvailable(false)
                .setAction((event) -> {
                    GuildMusicManager manager = Bran.getInstance().getMusicManager().get(event.getGuild());
                    TrackScheduler scheduler = manager.getTrackScheduler();
                    VoiceChannel vchan = event.getGuild().getSelfMember().getVoiceState().getChannel();
                    if (vchan == null) {
                        event.sendMessage(Quotes.FAIL, "I'm not connected to any channel.").queue();
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
                .build();
    }
    
    @Command
    private static ICommand restart() {
        return new CommandBuilder(Category.MUSIC)
                .setAliases("restart")
                .setName("Music Restart Command")
                .setDescription("If playing, restarts the current song otherwise restarts the last played track.")
                .setRequiredPermission(Permissions.DJ)
                .setPrivateAvailable(false)
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
                .build();
    }
    
    @Command
    private static ICommand queue() {
        return new TreeCommandBuilder(Category.MUSIC)
                .setAliases("queue", "q")
                .setDefault("list")
                .onNotFound(CommandAction.REDIRECT)
                .setName("Music Queue Command")
                .setDescription("Clears, lists and remove tracks from the queue!")
                .setPrivateAvailable(false)
                .addSubCommand(new CommandBuilder(Category.MUSIC)
                        .setAliases("list")
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
                .addSubCommand(new CommandBuilder(Category.MUSIC)
                        .setAliases("remove", "r", "rm")
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
                            if (!toRemove.getDJId().equals(event.getAuthor().getId()) && !event.getGuildData(true).hasPermission(event.getAuthor(), Permissions.DJ)) {
                                event.sendMessage("You can't do this because you're not this song's DJ!").queue();
                                return;
                            }
                            scheduler.getQueue().remove(toRemove);
                            event.sendMessage(Quotes.SUCCESS, "Removed `" + toRemove.getTrack().getInfo().title + "` from the queue.").queue();
                        })
                        .build())
                .addSubCommand(new CommandBuilder(Category.MUSIC)
                        .setAliases("clear")
                        .setName("Queue Clear Command")
                        .setDescription("Clears the queue.")
                        .setRequiredPermission(Permissions.DJ)
                        .setAction((event) -> {
                            GuildMusicManager musicManager = Bran.getInstance().getMusicManager().get(event.getGuild());
                            if (musicManager.getTrackScheduler().getQueue().isEmpty()) {
                                event.sendMessage("B-but... The queue is empty! \uD83D\uDE05").queue();
                                return;
                            }
                            int size = musicManager.getTrackScheduler().getQueue().size();
                            musicManager.getTrackScheduler().getQueue().clear();
                            event.sendMessage("Removed `" + size + "` songs from the queue. \uD83D\uDDD1").queue();
                        })
                        .build())
                .addSubCommand(new CommandBuilder(Category.MUSIC)
                        .setAliases("shuffle")
                        .setName("Music Shuffle Command")
                        .setDescription("Set the Queue to Shuffle.")
                        .setRequiredPermission(Permissions.DJ)
                        .setAction((event) -> {
                            GuildMusicManager musicManager = Bran.getInstance().getMusicManager().get(event.getGuild());
                            musicManager.getTrackScheduler().shuffle();
                            event.sendMessage("Shuffled the Queue!").queue();
                        })
                        .build())
                .build();
    }
    
    @Command
    private static ICommand stop() {
        return new CommandBuilder(Category.MUSIC)
                .setAliases("stop")
                .setName("Stop Command")
                .setDescription("Stops the player!")
                .setRequiredPermission(Permissions.DJ)
                .setPrivateAvailable(false)
                .setAction((event) -> {
                    GuildMusicManager musicManager = Bran.getInstance().getMusicManager().get(event.getGuild());
                    if (musicManager.getTrackScheduler().isStopped()) {
                        event.sendMessage("B-but... I am not playing anything! \uD83D\uDE05").queue();
                        return;
                    }
                    musicManager.getTrackScheduler().stop();
                })
                .build();
    }
    
    @Command
    private static ICommand forceplay() {
        return new CommandBuilder(Category.MUSIC)
                .setAliases("forceplay", "fp")
                .setName("Force Play Command")
                .setDescription("Toggles the force play flag.")
                .setArgs(new Argument("title/url", String.class))
                .setAction((event) -> {
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
                    if (event.getGuildData(true).maxSongsPerUser > 0 && tracksByUser.size() >= event.getGuildData(true).maxSongsPerUser) {
                        event.sendMessage("You can only have " + event.getGuildData(true).maxSongsPerUser + " songs in the queue.").queue();
                        return;
                    }
                    if (trackUrl.startsWith("soundcloud "))
                        trackUrl = trackUrl.replaceFirst("soundcloud ", "scsearch:");
                    if (!trackUrl.matches(URL_REGEX) && !trackUrl.startsWith("ytsearch:") && !trackUrl.startsWith("scsearch:"))
                        trackUrl = "ytsearch:" + trackUrl;
                    Bran.getInstance().getMusicManager().loadAndPlay(event.getAuthor(), event.getTextChannel(), trackUrl, true);
                })
                .build();
    }
    
    @Command
    private static ICommand music() {
        return new CommandBuilder(Category.MUSIC)
                .setAliases("music", "m")
                .setName("Music Info Command")
                .setDescription("Do you want to know about music?")
                .setAction((event) -> {
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setAuthor("How to play music", null, "https://is.gd/1avlwi");
                    embedBuilder.setDescription("This a small tutorial of how to use my music feature!");
                    embedBuilder.addField("How do I add songs to the queue?", "Use the `.play` command, it'll display three options, then you type the number of the song you want to add.", false);
                    embedBuilder.addField("I don't like this song, can I skip it?", "Yes, just use the `.skip` command. But remember, if there are other people listening to the song they have to want to skip it as well!", false);
                    embedBuilder.addField("I don't like this song and I have the DJ permission.", "Just use the `.forceskip` command!", false);
                    embedBuilder.addField("I don't like to always have to type `1, 2 or 3`", "Use the `.forceplay` command!", false);
                    embedBuilder.addField("I'm really into this song, I want to listen to it forever!", "And I can help, just use the `.repeat` command!", false);
                    embedBuilder.addField("I want to see the whole queue!", "Use the `.queue` command.", false);
                    embedBuilder.addField("I want to see what's playing now!", "Use the `.nowplaying` command.", false);
                    embedBuilder.addField("I'd like to clear the queue without stopping the current song!", "Use the `.queue clear` command.", false);
                    embedBuilder.addField("I want to shuffle the queue!", "Use the `.queue shuffle` command", false);
                    embedBuilder.addField("I accidentally added the wrong song to the queue, can I remove it?", "Of course you can, just use `.queue remove`!", false);
                    embedBuilder.addField("I want to stop the music player, can I do that?", "Surely, just use `.stop`!", false);
                    embedBuilder.addField("Can I pause the player?", "Yep, use the `.pause` command (unpause as well)", false);
    
                    event.sendMessage(embedBuilder.build()).queue();
                })
                .build();
    }
}
