package br.com.brjdevs.bran.core.listeners;

import br.com.brjdevs.bran.core.data.guild.DiscordGuild;
import br.com.brjdevs.bran.core.data.guild.configs.customcommands.CustomCommand;
import br.com.brjdevs.bran.core.managers.Permissions;
import br.com.brjdevs.bran.core.managers.PrefixManager;
import br.com.brjdevs.bran.core.utils.MathUtils;
import br.com.brjdevs.bran.core.utils.StringUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.EventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomCommandsListener implements EventListener {
	
	private static Pattern RANDOM_PATTERN = Pattern.compile("(\\$random\\{.+?;+.+?})", Pattern.CASE_INSENSITIVE);

	private static String parseTag
			(String answer, Member member, TextChannel textChannel, Guild guild, String args, CustomCommand cmd) {
		answer = answer.replaceAll("%user%", member.getEffectiveName());
		answer = answer.replaceAll("%mention%", member.getAsMention());
		answer = answer.replaceAll("%channel%", textChannel.getAsMention());
		answer = answer.replaceAll("%guild%", guild.getName());
		answer = answer.replaceAll("%cmdName%", cmd.getName());
		answer = answer.replaceAll("%input%", args);
		Matcher matcher = RANDOM_PATTERN.matcher(answer);
		while (matcher.find()) {
			String group = matcher.group(0);
			String[] options = group.substring(group.indexOf("{") + 1, group.lastIndexOf("}")).split(";");
			int random = MathUtils.random(options.length);
			group = Pattern.quote(group);
			answer = answer.replaceAll(group,
					options[random]);
		}
		return answer;
	}
	
	@Override
	public void onEvent(Event e) {
		if (!(e instanceof GuildMessageReceivedEvent)) return;
		GuildMessageReceivedEvent event = (GuildMessageReceivedEvent) e;
		DiscordGuild discordGuild = DiscordGuild.getInstance(event.getGuild());
		if (!discordGuild.getCustomCommands().check()) return;
		String msg = event.getMessage().getRawContent().trim().toLowerCase();
		String prefix = PrefixManager.getPrefix0(msg, discordGuild);
		if (prefix == null) return;
		if (!discordGuild.getMember(event.getAuthor()).hasPermission(Permissions.RUN_USRCMD, event.getJDA())) return;
		String baseCmd = msg.substring(prefix.length()).split("\\s+")[0];
		CustomCommand command = discordGuild.getCustomCommands().getCustomCommand(baseCmd);
		if (command == null) return;
		String args = StringUtils.splitArgs(msg, 2)[1];
		String answer = parseTag(command.getAnswer(), event.getMember(), event.getChannel(), event.getGuild(), args, command);
		event.getChannel().sendTyping().queue(success ->
				event.getChannel().sendMessage(answer).queue());
	}
}
