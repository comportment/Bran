package br.com.brjdevs.steven.bran.core.listeners;

import br.com.brjdevs.steven.bran.Client;
import br.com.brjdevs.steven.bran.DiscordLog.Level;
import br.com.brjdevs.steven.bran.core.command.CommandEvent;
import br.com.brjdevs.steven.bran.core.command.CommandStatsManager;
import br.com.brjdevs.steven.bran.core.command.CommandUtils;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.data.GuildData;
import br.com.brjdevs.steven.bran.core.managers.PrefixManager;
import br.com.brjdevs.steven.bran.core.quote.Quotes;
import br.com.brjdevs.steven.bran.core.utils.Hastebin;
import br.com.brjdevs.steven.bran.core.utils.OtherUtils;
import br.com.brjdevs.steven.bran.core.utils.StringUtils;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class CommandListener extends OptimizedListener<MessageReceivedEvent> {
	
	private static final SimpleLog LOG = SimpleLog.getLog("CommandListener");
	public Client client;
	
	public CommandListener(Client client) {
		super(MessageReceivedEvent.class);
		this.client = client;
	}
	
	@Override
	public void event(MessageReceivedEvent event) {
		if (event.getAuthor().isBot() || event.getAuthor().isFake() || !OtherUtils.isPrivate(event) && !event.getTextChannel().canTalk())
			return;
		String msg = event.getMessage().getRawContent().toLowerCase();
		String[] args = StringUtils.splitSimple(msg);
		GuildData guildData = client.getData().getDataHolderManager().get().getGuild(event.getGuild(), client.getConfig());
		String prefix = PrefixManager.getPrefix(args[0], guildData, client);
		if (prefix == null) return;
		String baseCmd = args[0].substring(prefix.length());
		ICommand cmd = CommandUtils.getCommand(client.commandManager, baseCmd);
		if (cmd == null) return;
		else if (!cmd.isPrivateAvailable() && OtherUtils.isPrivate(event)) {
			event.getChannel().sendTyping().queue(success -> event.getChannel().sendMessage(Quotes.getQuote(Quotes.FAIL) + "This command is not available through PMs, " +
					"use it in a Text Channel please.").queue());
			return;
		}
		CommandEvent e = new CommandEvent(event, cmd, guildData, event.getMessage().getRawContent(), prefix, client);
		client.getSession().cmds++;
		CommandStatsManager.log(cmd);
		OtherUtils.async(cmd.getName() + ">" + OtherUtils.getUser(event.getAuthor()),
				() -> {
					try {
						cmd.execute(e);
					} catch (Exception ex) {
						LOG.log(ex);
						e.sendMessage(Quotes.FAIL, "An unexpected `" + ex.getClass().getSimpleName() + "` occurred while executing this command, my owner has been informed about this so you don't need to report it.").queue();
						String url = Hastebin.post(OtherUtils.getStackTrace(ex));
						client.getDiscordLog().logToDiscord("Uncaught exception in Thread " + Thread.currentThread().getName(), "An unexpected `" + ex.getClass().getSimpleName() + "` occurred.\nMessage: " + ex.getMessage() + "\nStackTrace: " + url, Level.FATAL);
					}
				}).run();
	}
}
