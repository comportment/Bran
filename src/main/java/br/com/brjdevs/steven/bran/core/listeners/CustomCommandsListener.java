package br.com.brjdevs.steven.bran.core.listeners;

import br.com.brjdevs.steven.bran.BotContainer;
import br.com.brjdevs.steven.bran.core.data.guild.DiscordGuild;
import br.com.brjdevs.steven.bran.core.managers.CustomCommand;
import br.com.brjdevs.steven.bran.core.managers.Permissions;
import br.com.brjdevs.steven.bran.core.managers.PrefixManager;
import br.com.brjdevs.steven.bran.core.utils.MathUtils;
import br.com.brjdevs.steven.bran.core.utils.StringUtils;
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
	public BotContainer container;
	
	public CustomCommandsListener(BotContainer container) {
		this.container = container;
	}
	
	private static String parseTag(String answer, Member member, TextChannel textChannel, Guild guild, String args) {
		answer = answer.replace("%user%", member.getEffectiveName());
		answer = answer.replace("%mention%", member.getAsMention());
		answer = answer.replace("%channel%", textChannel.getAsMention());
		answer = answer.replace("%guild%", guild.getName());
		answer = answer.replace("%input%", args);
		answer = answer.replace("%id%", member.getUser().getId());
		Matcher matcher = RANDOM_PATTERN.matcher(answer);
		while (matcher.find()) {
			String group = matcher.group(0);
			String[] options = group.substring(group.indexOf("{") + 1, group.lastIndexOf("}")).split(";");
			int random = MathUtils.random(options.length);
			group = Pattern.quote(group);
			answer = answer.replaceFirst(group, options[random]);
		}
		return answer.trim();
	}
	
	@Override
	public void onEvent(Event e) {
		if (!(e instanceof GuildMessageReceivedEvent)) return;
		GuildMessageReceivedEvent event = (GuildMessageReceivedEvent) e;
		if (event.getAuthor().isBot()) return;
		DiscordGuild discordGuild = DiscordGuild.getInstance(event.getGuild(), container);
		if (!discordGuild.getCustomCommands().check()) return;
		String msg = event.getMessage().getRawContent().trim().toLowerCase().split("\\s+")[0];
		String prefix = PrefixManager.getPrefix0(msg, discordGuild);
		if (prefix == null) return;
		if (!discordGuild.getMember(event.getMember(), container).hasPermission(Permissions.RUN_USRCMD, event.getJDA(), container))
			return;
		String baseCmd = msg.substring(prefix.length()).split("\\s+")[0];
		CustomCommand command = discordGuild.getCustomCommands().getCustomCommand(baseCmd);
		if (command == null) return;
		String args = StringUtils.splitArgs(event.getMessage().getRawContent(), 2)[1];
		String answer = parseTag(command.getAnswer(), event.getMember(), event.getChannel(), event.getGuild(), args);
		container.getMessenger().sendMessage(event.getChannel(), answer).queue();
	}
}
