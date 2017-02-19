package br.com.brjdevs.steven.bran.core.listeners;

import br.com.brjdevs.steven.bran.Client;
import br.com.brjdevs.steven.bran.core.data.GuildData;
import br.com.brjdevs.steven.bran.core.managers.CustomCommand;
import br.com.brjdevs.steven.bran.core.managers.Permissions;
import br.com.brjdevs.steven.bran.core.managers.PrefixManager;
import br.com.brjdevs.steven.bran.core.utils.MathUtils;
import br.com.brjdevs.steven.bran.core.utils.StringUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomCommandsListener extends OptimizedListener<GuildMessageReceivedEvent> {
	
	private static Pattern RANDOM_PATTERN = Pattern.compile("(\\$random\\{.+?;+.+?})", Pattern.CASE_INSENSITIVE);
	public Client client;
	
	public CustomCommandsListener(Client client) {
		super(GuildMessageReceivedEvent.class);
		this.client = client;
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
	public void event(GuildMessageReceivedEvent event) {
		if (event.getAuthor().isFake() || event.getAuthor().isBot()) return;
		GuildData guildData = client.getData().getDataHolderManager().get().getGuild(event.getGuild(), client.getConfig());
		if (!guildData.customCommands.isEmpty()) return;
		String msg = event.getMessage().getRawContent().trim().toLowerCase().split("\\s+")[0];
		String prefix = PrefixManager.getPrefix0(msg, guildData);
		if (prefix == null) return;
		if (!guildData.hasPermission(event.getAuthor(), Permissions.RUN_USRCMD))
			return;
		String baseCmd = msg.substring(prefix.length()).split("\\s+")[0];
		CustomCommand command = guildData.customCommands.get(baseCmd);
		if (command == null) return;
		String args = StringUtils.splitArgs(event.getMessage().getRawContent(), 2)[1];
		String answer = parseTag(command.getAnswer(), event.getMember(), event.getChannel(), event.getGuild(), args);
		client.getMessenger().sendMessage(event.getChannel(), answer).queue();
	}
}
