package br.com.brjdevs.steven.bran.core.audio;

import br.com.brjdevs.steven.bran.BotContainer;
import br.com.brjdevs.steven.bran.core.audio.impl.TrackContextImpl;
import br.com.brjdevs.steven.bran.core.utils.Util;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.SneakyThrows;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.utils.SimpleLog;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MusicPersistence {
	
	private static SimpleLog LOG;
	
	static {
		LOG = SimpleLog.getLog("Music Persistence");
	}
	
	private BotContainer container;
	
	public MusicPersistence(BotContainer container) {
		this.container = container;
		this.reloadPlaylists();
	}
	
	@SneakyThrows(Exception.class)
	public boolean savePlaylists() {
		if (!container.config.isMusicPersistenceEnabled()) {
			LOG.info("Music Persistence is disabled in config.json.");
			return true;
		}
		LOG.info("Initiating MusicPersistence pre shutdown.");
		File dir = new File(System.getProperty("user.dir"), "music_persistence");
		if (!dir.exists()) {
			try {
				if (!dir.mkdirs()) throw new RuntimeException("Could not create dir.");
			} catch (Exception e) {
				LOG.fatal("Could not create dir.");
				LOG.log(e);
				return false;
			}
		}
		String msg = "I'm going to restart, I'll be back in a minute and the current playlist will be reloaded!";
		for (MusicManager musicManager : container.playerManager.getMusicManagers().values()) {
			if (musicManager == null || musicManager.getGuild() == null) continue;
			TrackScheduler trackScheduler = musicManager.getTrackScheduler();
			if (trackScheduler == null) continue;
			if (container.taskManager.getChannelLeaveTimer().removeMusicPlayer(musicManager.getGuild().getId())) {
				musicManager.getTrackScheduler().setPaused(false);
			}
			if (trackScheduler.isStopped())
				continue;
			if (trackScheduler.getVoiceChannel() == null) continue;
			if (trackScheduler.getCurrentTrack() != null && trackScheduler.getCurrentTrack().getContext(trackScheduler.getShard().getJDA()) != null) {
				trackScheduler.getCurrentTrack().getContext(trackScheduler.getShard().getJDA()).sendMessage(msg).queue();
			}
			JSONObject data = new JSONObject();
			data.put("vc", trackScheduler.getVoiceChannel().getId());
			data.put("paused", trackScheduler.isPaused());
			data.put("shuffle", trackScheduler.isShuffle());
			data.put("repeat", trackScheduler.isRepeat());
			data.put("voteskips", trackScheduler.getVoteSkips());
			
			if (trackScheduler.getCurrentTrack() != null)
				data.put("position", trackScheduler.getCurrentTrack().getTrack().getPosition());
			
			List<JSONObject> sources = new ArrayList<>();
			for (TrackContext track : trackScheduler.getRemainingTracks()) {
				JSONObject src = new JSONObject();
				src.put("channel", track.getContextId());
				src.put("user", track.getDJId());
				src.put("url", track.getURL());
				
				sources.add(src);
			}
			data.put("sources", sources);
			
			FileUtils.writeStringToFile(new File(dir, trackScheduler.getGuild().getId()), data.toString(), Charset.forName("UTF-8"));
		}
		LOG.info("Finished MusicPersistence pre shutdown.");
		return true;
	}
	
	public boolean reloadPlaylists() {
		if (!container.config.isMusicPersistenceEnabled()) {
			LOG.info("Music Persistence is disabled in config.json.");
			return true;
		}
		File dir = new File(System.getProperty("user.dir"), "music_persistence");
		if (!dir.exists()) return true;
		
		File[] files = dir.listFiles();
		if (files == null) {
			LOG.fatal("Path is not a directory.");
			return false;
		}
		if (files.length == 0) {
			LOG.info("No files in path.");
			return true;
		}
		for (File file : files) {
			InputStream inputStream = null;
			boolean[] isFirst = {true};
			try {
				String guildId = file.getName();
				inputStream = new FileInputStream(file);
				Scanner scanner = new Scanner(inputStream);
				JSONObject data = new JSONObject(scanner.useDelimiter("\\A").next());
				scanner.close();
				if (!file.delete()) {
					LOG.warn("Could not delete File named '" + guildId + "'");
				}
				int shardId = container.calcShardId(Long.parseLong(guildId));
				JDA jda = container.getShards()[shardId].getJDA();
				if (jda == null) continue;
				Guild guild = jda.getGuildById(guildId);
				if (guild == null) continue;
				JSONArray sources = data.getJSONArray("sources");
				VoiceChannel vc = jda.getVoiceChannelById(data.getString("vc"));
				if (vc == null || vc.getMembers().isEmpty()) continue;
				if (!guild.getAudioManager().isConnected() && !guild.getAudioManager().isAttemptingToConnect()) {
					guild.getAudioManager().setSelfDeafened(true);
					guild.getAudioManager().setConnectionListener(new ConnectionListenerImpl(guild, container));
					guild.getAudioManager().openAudioConnection(vc);
				}
				boolean isPaused = data.getBoolean("paused");
				boolean shuffle = data.getBoolean("shuffle");
				boolean repeat = data.getBoolean("repeat");
				JSONArray voteSkips = data.getJSONArray("voteskips");
				
				TrackScheduler trackScheduler = container.playerManager.get(jda.getGuildById(guildId)).getTrackScheduler();
				
				trackScheduler.setPaused(isPaused);
				trackScheduler.setShuffle(shuffle);
				trackScheduler.setRepeat(repeat);
				voteSkips.forEach(o -> trackScheduler.getVoteSkips().add((String) o));
				PersistenceAudioLoader audioLoader = new PersistenceAudioLoader();
				sources.forEach((Object o) -> {
					JSONObject ident = (JSONObject) o;
					
					container.playerManager.getAudioPlayerManager()
							.loadItemOrdered(container.playerManager.get(jda.getGuildById(guildId)), ident.getString("url"), audioLoader);
					
					AudioTrack audioTrack = audioLoader.result();
					
					if (audioTrack == null) {
						LOG.info("Loaded null track! Skipping...");
						return;
					}
					TextChannel channel = jda.getTextChannelById(ident.getString("channel"));
					User author = jda.getUserById(ident.getString("user"));
					String url = ident.getString("url");
					TrackContext trackContext = new TrackContextImpl(audioTrack, url, author, channel);
					trackScheduler.silentQueue(trackContext);
					if (isFirst[0]) {
						isFirst[0] = false;
						if (data.has("position")) {
							trackContext.setPosition(data.getLong("position"));
						}
						channel.sendMessage("Reloading playlist, found `" + sources.length() + "` tracks...").queue();
					}
				});
			} catch (Exception e) {
				LOG.fatal("Error while loading persistence file.");
				LOG.log(e);
				return false;
			}
		}
		return true;
	}
	
	private static class PersistenceAudioLoader implements AudioLoadResultHandler {
		
		private AudioTrack result;
		private boolean called = false;
		
		@Override
		public void trackLoaded(AudioTrack audioTrack) {
			this.result = audioTrack;
			this.called = true;
		}
		
		@Override
		public void playlistLoaded(AudioPlaylist audioPlaylist) {
			this.called = true;
			throw new UnsupportedOperationException("Playlist aren't supported in this Audio Loader!");
		}
		
		@Override
		public void noMatches() {
			this.called = true;
			this.result = null;
		}
		
		@Override
		public void loadFailed(FriendlyException e) {
			this.called = true;
			this.result = null;
		}
		
		public AudioTrack result() {
			while (!called) Util.sleep(100);
			if (result == null) return null;
			AudioTrack track = result.makeClone();
			result = null;
			return track;
		}
	}
}
