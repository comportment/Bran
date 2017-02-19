package br.com.brjdevs.steven.bran.core.listeners;

import br.com.brjdevs.steven.bran.Client;
import br.com.brjdevs.steven.bran.core.data.GuildData;
import br.com.brjdevs.steven.bran.core.managers.Permissions;
import br.com.brjdevs.steven.bran.core.poll.Poll;
import br.com.brjdevs.steven.bran.core.utils.MathUtils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.regex.Pattern;

public class PollListener extends OptimizedListener<GuildMessageReceivedEvent> {
	private static final Pattern OPTION_INDEX = Pattern.compile("^([0-9]{1,2})$");
	
	public Client client;
	
	public PollListener(Client client) {
		super(GuildMessageReceivedEvent.class);
		this.client = client;
	}
	
	@Override
	public void event(GuildMessageReceivedEvent event) {
		if (event.getAuthor().isFake() || event.getAuthor().isBot()) return;
		//TODO fix dis
		//Action action = Action.getAction(event.getAuthor().getId());
		//if (action != null && !action.getChannelId().equals(event.getChannel().getId())) return;
		String msg = event.getMessage().getRawContent();
		if (!MathUtils.isInteger(msg)) return;
		if (!OPTION_INDEX.matcher(msg).matches()) return;
		GuildData guildData = client.getData().getDataHolderManager().get().getGuild(event.getGuild(), client.getConfig());
		if (!guildData.hasPermission(event.getAuthor(), Permissions.POLL)) return;
		Poll poll = Poll.getPoll(event.getChannel());
		if (poll == null) return;
		int i = Integer.parseInt(msg) - 1;
		try {
			boolean added = poll.vote(event.getAuthor().getId(), i);
			if (!event.getGuild().getSelfMember().hasPermission(event.getChannel(), Permission.MESSAGE_ADD_REACTION)) {
				client.getMessenger().sendMessage(event.getChannel(), added ? "\u2705" : "\u2796").queue();
				return;
			}
			event.getMessage().addReaction(added ? "\u2705" : "\u2796").queue();
		} catch (NullPointerException ex) {
			if (!event.getGuild().getSelfMember().hasPermission(event.getChannel(), Permission.MESSAGE_ADD_REACTION)) {
				client.getMessenger().sendMessage(event.getChannel(), "\u274c").queue();
				return;
			}
			event.getMessage().addReaction("\u274c").queue();
		}
	}
}
