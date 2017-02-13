package br.com.brjdevs.steven.bran.core.data.guild;

import br.com.brjdevs.steven.bran.BotContainer;
import br.com.brjdevs.steven.bran.Main;
import br.com.brjdevs.steven.bran.core.data.guild.settings.*;
import br.com.brjdevs.steven.bran.core.managers.Permissions;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiscordGuild {
	
	public static final Map<Guild, DiscordGuild> instances = new HashMap<>();
	
	private List<GuildMember> members = new ArrayList<>();
	private List<String> prefixes;
	private AnnouncesSettings announces = new AnnouncesSettings();
	private CustomCmdsSettings cmdsSettings = new CustomCmdsSettings();
	private WordFilterSettings wordFilter = new WordFilterSettings(false);
	private MusicSettings musicSettings = new MusicSettings();
	private Giveaway giveaway = null;
	private long defaultPermission = Permissions.BASE_USR;
	
	public DiscordGuild(BotContainer container) {
		this.prefixes = new ArrayList<>(container.config.getDefaultPrefixes());
	}
	
	public static DiscordGuild getInstance(Guild guild, BotContainer botContainer) {
		if (!instances.containsKey(guild))
			instances.put(guild, new DiscordGuild(botContainer));
		return instances.get(guild);
	}
	
	public static DiscordGuild load(Guild guild, DiscordGuild discordGuild) {
		instances.put(guild, discordGuild);
		return instances.get(guild);
	}
	
	public List<String> getPrefixes() {
		return prefixes;
	}
	
	public Guild getOrigin() {
		return instances.entrySet().stream()
				.filter(entry -> entry.getValue().equals(this))
				.findFirst().orElse(null).getKey();
	}
	
	public AnnouncesSettings getAnnounces() {
		return announces;
	}
	
	public List<GuildMember> getMembers() {
		return members;
	}
	
	public GuildMember getMember(Member m1, BotContainer container) {
		User user = m1.getUser();
		GuildMember member = members.stream().filter(m -> m.getUserId().equals(user.getId())).findFirst().orElse(null);
		if (member == null) {
			member = new GuildMember(user, m1.getGuild().getId(), container);
			if (!user.isBot() && !user.isFake())
				members.add(member);
		}
		return member;
	}
	
	public boolean isMember(Member member, BotContainer container) {
		return getMember(member, container) != null;
	}
	
	public CustomCmdsSettings getCustomCommands() {
		return cmdsSettings;
	}
	
	public WordFilterSettings getWordFilter() {
		return wordFilter;
	}
	
	public MusicSettings getMusicSettings() {
		return musicSettings;
	}
	
	public long getDefaultPermission() {
		return defaultPermission;
	}
	
	public void setDefaultPermission(long defaultPermission) {
		this.defaultPermission = defaultPermission;
	}
	
	public Giveaway getGiveaway() {
		return giveaway;
	}
	
	public void setGiveaway(Giveaway giveaway) {
		this.giveaway = giveaway;
	}
	
	public void save(BotContainer container) {
		try {
			File file = new File(container.workingDir, getOrigin().getId() + ".json");
			if (!file.exists()) assert file.createNewFile();
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(Main.GSON.toJson(this));
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
