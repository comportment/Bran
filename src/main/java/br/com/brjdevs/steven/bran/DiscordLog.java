package br.com.brjdevs.steven.bran;

import br.com.brjdevs.steven.bran.core.utils.Hastebin;
import br.com.brjdevs.steven.bran.core.utils.RequirementsUtils;
import br.com.brjdevs.steven.bran.core.utils.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

import java.awt.*;

public class DiscordLog {
	
	private static SimpleLog LOG = SimpleLog.getLog("DiscordLog");
	public TextChannel logChannel;
	private boolean isEnabled;
	private Guild guild;
	
	public DiscordLog(BotContainer botContainer) {
		for (Bot bot : botContainer.getShards()) {
			guild = bot.getJDA().getGuildById("219256419684188161");
			if (guild != null) break;
		}
		if (guild == null) {
			this.isEnabled = false;
			LOG.fatal("Could not find Discord Log Guild.");
			return;
		}
		logChannel = guild.getTextChannelById("249971874430320660");
		this.isEnabled = true;
	}
	
	public boolean isEnabled() {
		return isEnabled;
	}
	
	public void logToDiscord(Throwable throwable, String extra) {
		logChannel.sendMessage(Level.FATAL.getBaseEmbed("Uncaught exception in Thread '" + Thread.currentThread().getName() + "'")
				.setDescription("An unexpected `" + throwable.getClass().getSimpleName() + "` occurred.\n**Message:** " + throwable.getMessage() + "\n**StackTrace:** " + Hastebin.post(Util.getStackTrace(throwable)) + "\n**Extras:** " + (Util.isEmpty(extra) ? "No extra information was given." : extra))
				.build()).queue();
	}
	
	public void logToDiscord(GuildJoinEvent event) {
		logChannel.sendMessage(Level.INFO.getBaseEmbed("\uD83C\uDFE0 Joined Guild")
				.setDescription("**Name:** " + guild.getName() + "\n**ID:** " + guild.getId() + "\n**Region:** " + guild.getRegion().toString() + "\n**Members:** " + guild.getMembers().size() + "  (" + RequirementsUtils.getBotsPercentage(guild) + "% bots)\n**Owner:** " + Util.getUser(guild.getOwner().getUser()) + " (ID: " + guild.getOwner().getUser().getId() + ")")
				.build()).queue();
	}
	
	public void logToDiscord(GuildLeaveEvent event) {
		logChannel.sendMessage(Level.INFO.getBaseEmbed("\uD83C\uDFDA Left Guild")
				.setDescription("**Name:** " + guild.getName() + "\n**ID:** " + guild.getId() + "\n**Region:** " + guild.getRegion().toString() + "\n**Members:** " + guild.getMembers().size() + "  (" + RequirementsUtils.getBotsPercentage(guild) + "% bots)\n**Owner:** " + Util.getUser(guild.getOwner().getUser()) + " (ID: " + guild.getOwner().getUser().getId() + ")")
				.build()).queue();
	}
	
	public void logToDiscord(String title, String description, Level level) {
		logChannel.sendMessage(level.getBaseEmbed(title).setDescription(description).build()).queue();
	}
	
	public enum Level {
		INFO(Color.decode("#4AD2EC")), WARN(Color.decode("#F6D653")), FATAL(Color.decode("#F65353"));
		
		private Color color;
		
		Level(Color color) {
			this.color = color;
		}
		
		public EmbedBuilder getBaseEmbed(String message) {
			return new EmbedBuilder().setColor(color).setTitle("[" + this + "] {}".replace("{}", message));
		}
	}
}
