package br.com.brjdevs.steven.bran.core.data.guild.settings;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

import javax.xml.ws.Holder;
import java.util.HashMap;
import java.util.Map;

public class AnnouncesSettings {
	
	private String JOIN_MSG = null;
	private String JOIN_MSG_DM = null;
	private String LEAVE_MSG = null;
	private String ANNOUNCES_CHANNEL_ID = null;
	
	public AnnouncesSettings() {
	}
	
	public static String parse(String msg, Member member) {
		Guild guild = member.getGuild();
		Map<String, Object> replacements = new HashMap<>();
		replacements.put("%usercount%", guild.getMembers().size());
		replacements.put("%user%", member.getEffectiveName());
		replacements.put("%mention%", member.getAsMention());
		replacements.put("%guild%", guild.getName());
		replacements.put("%owner%", guild.getOwner().getEffectiveName());
		Holder<String> out = new Holder<>(msg);
		replacements.entrySet().forEach(entry -> out.value = out.value.replace(entry.getKey(), entry.getValue().toString()));
		return out.value;
	}
	
	public void setAnnouncesChannel(TextChannel channel) {
		this.ANNOUNCES_CHANNEL_ID = channel.getId();
	}
	
	public String getJoinAnnounce() {
		return JOIN_MSG;
	}
	
	public void setJoinAnnounce(String JOIN_MSG) {
		this.JOIN_MSG = JOIN_MSG;
	}
	
	public String getJoinDMAnnounce() {
		return JOIN_MSG_DM;
	}
	
	public void setJoinDMAnnounce(String JOIN_MSG_DM) {
		this.JOIN_MSG_DM = JOIN_MSG_DM;
	}
	
	public String getLeaveAnnounce() {
		return LEAVE_MSG;
	}
	
	public void setLeaveAnnounce(String LEAVE_MSG) {
		this.LEAVE_MSG = LEAVE_MSG;
	}
	
	public TextChannel getChannel(JDA jda) {
		return jda.getTextChannelById(ANNOUNCES_CHANNEL_ID);
	}
	
}
