package br.com.brjdevs.steven.bran;

import br.com.brjdevs.steven.bran.core.utils.Hastebin;
import br.com.brjdevs.steven.bran.core.utils.OtherUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

import java.awt.*;

public class DiscordLog {
	
	private static SimpleLog LOG = SimpleLog.getLog("DiscordLog");
	public String channelId;
	private boolean isEnabled;
	private long guildId;
	private Client client;
	private int shard;
	
	public DiscordLog(Client client) {
		this.client = client;
		this.guildId = Long.parseLong("219256419684188161");
		this.shard = client.calcShardId(guildId);
		Guild guild = client.getShards()[shard].getJDA().getGuildById(String.valueOf(guildId));
		if (guild == null) {
			this.isEnabled = false;
			LOG.fatal("Could not find Discord Log Guild.");
			return;
		}
		channelId = "249971874430320660";
		this.isEnabled = true;
	}
	
	public boolean isEnabled() {
		return isEnabled;
	}
	
	public void logToDiscord(Throwable throwable, String extra) {
		getLogChannel().sendMessage(Level.FATAL.getBaseEmbed("Uncaught exception in Thread '" + Thread.currentThread().getName() + "'")
				.setDescription("An unexpected `" + throwable.getClass().getSimpleName() + "` occurred.\n**Message:** " + throwable.getMessage() + "\n**StackTrace:** " + Hastebin.post(OtherUtils.getStackTrace(throwable)) + "\n**Extras:** " + (OtherUtils.isEmpty(extra) ? "No extra information was given." : extra))
				.build()).queue();
	}
	
	public void logToDiscord(GuildJoinEvent event) {
		Guild guild = event.getGuild();
		getLogChannel().sendMessage(Level.INFO.getBaseEmbed("\uD83C\uDFE0 Joined Guild")
				.setDescription("**Name:** " + guild.getName() + "\n**ID:** " + guild.getId() + "\n**Shard:** " + client.getShardId(guild.getJDA()) + "\n**Region:** " + guild.getRegion().toString() + "\n**Members:** " + guild.getMembers().size() + "\n**Owner:** " + OtherUtils.getUser(guild.getOwner().getUser()) + " (ID: " + guild.getOwner().getUser().getId() + ")")
				.build()).queue();
	}
	
	public void logToDiscord(GuildLeaveEvent event) {
		Guild guild = event.getGuild();
		getLogChannel().sendMessage(Level.INFO.getBaseEmbed("\uD83C\uDFDA Left Guild")
				.setDescription("**Name:** " + guild.getName() + "\n**ID:** " + guild.getId() + "\n**Region:** " + guild.getRegion().toString() + "\n**Members:** " + guild.getMembers().size() + "\n**Owner:** " + OtherUtils.getUser(guild.getOwner().getUser()) + " (ID: " + guild.getOwner().getUser().getId() + ")")
				.build()).queue();
	}
	
	public void logToDiscord(String title, String description, Level level) {
		getLogChannel().sendMessage(level.getBaseEmbed(title).setDescription(description).build()).queue();
	}
	
	public TextChannel getLogChannel() {
		return getGuild().getTextChannelById(channelId);
	}
	
	public Guild getGuild() {
		return client.getShards()[shard].getJDA().getGuildById(String.valueOf(guildId));
	}
	
	public enum Level {
		INFO(Color.decode("#4AD2EC")), WARN(Color.decode("#F6D653")), FATAL(Color.decode("#F65353"));
		
		private Color color;
		
		Level(Color color) {
			this.color = color;
		}
		
		public EmbedBuilder getBaseEmbed(String message) {
			return new EmbedBuilder().setColor(color).setTitle("[" + this + "] {}".replace("{}", message), null);
		}
	}
}
