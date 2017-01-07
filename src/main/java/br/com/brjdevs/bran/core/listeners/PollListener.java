package br.com.brjdevs.bran.core.listeners;

import br.com.brjdevs.bran.core.action.Action;
import br.com.brjdevs.bran.core.data.guild.DiscordGuild;
import br.com.brjdevs.bran.core.data.guild.configs.GuildMember;
import br.com.brjdevs.bran.core.managers.Permissions;
import br.com.brjdevs.bran.core.poll.Poll;
import br.com.brjdevs.bran.core.utils.MathUtils;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.EventListener;

import java.util.regex.Pattern;

public class PollListener implements EventListener {
	private static final Pattern OPTION_INDEX = Pattern.compile("^([0-9]{1,2})$");
	@Override
	public void onEvent(Event e) {
		if (!(e instanceof GuildMessageReceivedEvent)) return;
		GuildMessageReceivedEvent event = (GuildMessageReceivedEvent) e;
		Action action = Action.getAction(event.getAuthor().getId());
		if (action != null && !action.getChannelId().equals(event.getChannel().getId())) return;
		String msg = event.getMessage().getRawContent();
		if (!MathUtils.isInteger(msg)) return;
		if (!OPTION_INDEX.matcher(msg).matches()) return;
		DiscordGuild discordGuild = DiscordGuild.getInstance(event.getGuild());
		GuildMember guildMember = discordGuild.getMember(event.getAuthor());
		if (!guildMember.hasPermission(Permissions.POLL, event.getJDA())) return;
		Poll poll = Poll.getPoll(event.getChannel());
		if (poll == null) return;
		int i = Integer.parseInt(msg) - 1;
		try {
			boolean added = poll.vote(event.getAuthor().getId(), i);
			event.getMessage().addReaction(added ? "\u2705" : "\u2796").queue();
		} catch (NullPointerException ex) {
			event.getMessage().addReaction("\u274c").queue();
		}
	}
}
