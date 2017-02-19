package br.com.brjdevs.steven.bran.core.listeners;

import br.com.brjdevs.steven.bran.Client;
import br.com.brjdevs.steven.bran.core.data.GuildData;
import br.com.brjdevs.steven.bran.core.utils.OtherUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.guild.member.GenericGuildMemberEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;

import javax.xml.ws.Holder;
import java.util.HashMap;
import java.util.Map;

public class AnnouncesListener extends OptimizedListener<GenericGuildMemberEvent> {
	
	public Client client;
	
	public AnnouncesListener(Client client) {
		super(GenericGuildMemberEvent.class);
		this.client = client;
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
	
	@Override
	public void event(GenericGuildMemberEvent event) {
		GuildData guildData = client.getData().getDataHolderManager().get().getGuild(event.getGuild(), client.getConfig());
		if (guildData.getAnnounceTextChannel(event.getJDA()) == null) return;
		Member member = event.getMember();
		if (event instanceof GuildMemberJoinEvent) {
			if (!OtherUtils.isEmpty(guildData.joinMsg))
				guildData.getAnnounceTextChannel(event.getJDA()).sendMessage(parse(guildData.joinMsg, member)).queue();
			if (!OtherUtils.isEmpty(guildData.joinMsgDM))
				event.getMember().getUser().openPrivateChannel().queue(chan -> chan.sendMessage(parse(guildData.joinMsgDM, member)).queue());
		} else if (event instanceof GuildMemberLeaveEvent) {
			if (!OtherUtils.isEmpty(guildData.leaveMsg))
				guildData.getAnnounceTextChannel(event.getJDA()).sendMessage(parse(guildData.leaveMsg, member)).queue();
		}
	}
}
