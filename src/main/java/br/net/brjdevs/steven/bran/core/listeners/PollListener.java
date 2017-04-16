package br.net.brjdevs.steven.bran.core.listeners;

import br.net.brjdevs.steven.bran.core.client.Bran;
import br.net.brjdevs.steven.bran.core.data.GuildData;
import br.net.brjdevs.steven.bran.core.managers.Messenger;
import br.net.brjdevs.steven.bran.core.managers.Permissions;
import br.net.brjdevs.steven.bran.core.poll.Poll;
import br.net.brjdevs.steven.bran.core.utils.MathUtils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class PollListener extends EventListener<GuildMessageReceivedEvent> {
	
	public PollListener() {
		super(GuildMessageReceivedEvent.class);
	}
	
	@Override
	public void onEvent(GuildMessageReceivedEvent event) {
		if (event.getAuthor().isFake() || event.getAuthor().isBot()) return;
		String msg = event.getMessage().getRawContent();
		if (!MathUtils.isInteger(msg)) return;
        GuildData guildData = Bran.getInstance().getDataManager().getData().get().getGuildData(event.getGuild(), true);
        if (!guildData.hasPermission(event.getAuthor(), Permissions.POLL)) return;
		Poll poll = Poll.getPoll(event.getChannel());
		if (poll == null) return;
		int i = Integer.parseInt(msg) - 1;
		try {
			boolean added = poll.vote(event.getAuthor().getId(), i);
			if (!event.getGuild().getSelfMember().hasPermission(event.getChannel(), Permission.MESSAGE_ADD_REACTION)) {
				Messenger.sendMessage(event.getChannel(), added ? "\u2705" : "\u2796").queue();
				return;
			}
			event.getMessage().addReaction(added ? "\u2705" : "\u2796").queue();
        } catch (NullPointerException ignored) {
        }
        Bran.getInstance().getDataManager().getPolls().update();
    }
}
