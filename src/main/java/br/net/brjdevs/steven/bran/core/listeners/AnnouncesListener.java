package br.net.brjdevs.steven.bran.core.listeners;

import br.net.brjdevs.steven.bran.core.client.Bran;
import br.net.brjdevs.steven.bran.core.data.GuildData;
import br.net.brjdevs.steven.bran.core.utils.Utils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.guild.member.GenericGuildMemberEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;

import javax.xml.ws.Holder;
import java.util.HashMap;
import java.util.Map;

public class AnnouncesListener extends EventListener<GenericGuildMemberEvent> {
	
	public AnnouncesListener() {
		super(GenericGuildMemberEvent.class);
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
	public void onEvent(GenericGuildMemberEvent event) {
        if (!event.getGuild().isMember(event.getJDA().getSelfUser()))
            return;
        GuildData guildData = Bran.getInstance().getDataManager().getData().get().getGuildData(event.getGuild(), true);
        if (guildData.getAnnounceTextChannel(event.getJDA()) == null || !guildData.getAnnounceTextChannel(event.getJDA()).canTalk())
			return;
		Member member = event.getMember();
		if (event instanceof GuildMemberJoinEvent) {
			if (!Utils.isEmpty(guildData.joinMsg))
				guildData.getAnnounceTextChannel(event.getJDA()).sendMessage(parse(guildData.joinMsg, member)).queue();
			if (!Utils.isEmpty(guildData.joinMsgDM))
				event.getMember().getUser().openPrivateChannel().queue(chan -> chan.sendMessage(parse(guildData.joinMsgDM, member)).queue());
		} else if (event instanceof GuildMemberLeaveEvent) {
			if (!Utils.isEmpty(guildData.leaveMsg))
				guildData.getAnnounceTextChannel(event.getJDA()).sendMessage(parse(guildData.leaveMsg, member)).queue();
		}
	}
}
