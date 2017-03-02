package br.com.brjdevs.steven.bran.core.listeners;

import br.com.brjdevs.steven.bran.core.client.Client;
import br.com.brjdevs.steven.bran.core.client.DiscordLog.Level;
import br.com.brjdevs.steven.bran.core.command.CommandEvent;
import br.com.brjdevs.steven.bran.core.command.CommandStatsManager;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.data.GuildData;
import br.com.brjdevs.steven.bran.core.managers.Permissions;
import br.com.brjdevs.steven.bran.core.managers.PrefixManager;
import br.com.brjdevs.steven.bran.core.quote.Quotes;
import br.com.brjdevs.steven.bran.core.utils.Hastebin;
import br.com.brjdevs.steven.bran.core.utils.StringUtils;
import br.com.brjdevs.steven.bran.core.utils.Utils;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class CommandListener extends EventListener<MessageReceivedEvent> {
	
	private static final SimpleLog LOG = SimpleLog.getLog("CommandListener");
	
	public CommandListener(Client client) {
		super(MessageReceivedEvent.class, client);
	}
	
	@Override
	public void event(MessageReceivedEvent event) {
		if (event.getAuthor().isBot() || event.getAuthor().isFake() || !Utils.isPrivate(event) && !event.getTextChannel().canTalk())
			return;
		String msg = event.getMessage().getRawContent().toLowerCase();
		String[] args = StringUtils.splitSimple(msg);
		GuildData guildData = !event.isFromType(ChannelType.TEXT) ? null : client.getDiscordBotData().getDataHolderManager().get().getGuild(event.getGuild());
		String prefix = PrefixManager.getPrefix(args[0], guildData, client);
		if (prefix == null) return;
		String baseCmd = args[0].substring(prefix.length());
		ICommand cmd = client.getCommandManager().getCommand(baseCmd);
		if (cmd == null)
			return;
		else if (!cmd.isPrivateAvailable() && event.isFromType(ChannelType.PRIVATE)) {
			event.getChannel().sendTyping().queue(success -> event.getChannel().sendMessage(Quotes.getQuote(Quotes.FAIL) + "You cannot execute this Commands in PMs!").queue());
			
		} else if (event.isFromType(ChannelType.PRIVATE) ? !client.getDiscordBotData().getDataHolderManager().get().getUser(event.getAuthor()).hasPermission(cmd.getRequiredPermission()) : !client.getDiscordBotData().getDataHolderManager().get().getGuild(event.getGuild()).hasPermission(event.getAuthor(), cmd.getRequiredPermission())) {
			event.getChannel().sendTyping().queue(sent -> event.getChannel().sendMessage("You don't have enough permissions to execute this Command!\n*Missing Permission(currentArgs): " + String.join(", ", Permissions.toCollection(cmd.getRequiredPermission())) + "*").queue());
			return;
		}
		CommandEvent e = new CommandEvent(event, cmd, guildData, event.getMessage().getRawContent(), prefix, client);
		client.getSession().cmds++;
		CommandStatsManager.log(cmd);
		Utils.async(cmd.getName() + ">" + Utils.getUser(event.getAuthor()),
				() -> {
					try {
						cmd.execute(e);
					} catch (Exception ex) {
						LOG.log(ex);
						e.sendMessage(Quotes.FAIL, "An unexpected `" + ex.getClass().getSimpleName() + "` occurred while executing this command, my owner has been informed about this so you don't need to report it.").queue();
						String url = Hastebin.post(Utils.getStackTrace(ex));
						client.getDiscordLog().logToDiscord("Uncaught exception in Thread " + Thread.currentThread().getName(), "An unexpected `" + ex.getClass().getSimpleName() + "` occurred.\nMessage: " + ex.getMessage() + "\nStackTrace: " + url, Level.FATAL);
					}
				}).run();
	}
}