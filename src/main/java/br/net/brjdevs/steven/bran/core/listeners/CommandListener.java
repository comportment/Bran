package br.net.brjdevs.steven.bran.core.listeners;

import br.net.brjdevs.steven.bran.core.client.Bran;
import br.net.brjdevs.steven.bran.core.client.DiscordLog.Level;
import br.net.brjdevs.steven.bran.core.command.CommandEvent;
import br.net.brjdevs.steven.bran.core.command.CommandStatsManager;
import br.net.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.net.brjdevs.steven.bran.core.currency.DroppedMoney;
import br.net.brjdevs.steven.bran.core.data.GuildData;
import br.net.brjdevs.steven.bran.core.managers.Permissions;
import br.net.brjdevs.steven.bran.core.managers.PrefixManager;
import br.net.brjdevs.steven.bran.core.quote.Quotes;
import br.net.brjdevs.steven.bran.core.utils.Hastebin;
import br.net.brjdevs.steven.bran.core.utils.MathUtils;
import br.net.brjdevs.steven.bran.core.utils.StringUtils;
import br.net.brjdevs.steven.bran.core.utils.Utils;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.utils.SimpleLog;

public class CommandListener extends EventListener<MessageReceivedEvent> {
	
	private static final SimpleLog LOG = SimpleLog.getLog("CommandListener");
	
	public CommandListener() {
		super(MessageReceivedEvent.class);
	}
	
	@Override
	public void event(MessageReceivedEvent event) {
		if (event.getAuthor().isBot() || event.getAuthor().isFake() || !Utils.isPrivate(event) && !event.getTextChannel().canTalk())
			return;
		String msg = event.getMessage().getRawContent().toLowerCase();
		String[] args = StringUtils.splitSimple(msg);
        GuildData guildData = !event.isFromType(ChannelType.TEXT) ? null : Bran.getInstance().getDataManager().getData().get().getGuildData(event.getGuild(), true);
        String prefix = PrefixManager.getPrefix(args[0], guildData);
		if (prefix == null) return;
		String baseCmd = args[0].substring(prefix.length());
		ICommand cmd = Bran.getInstance().getCommandManager().getCommand(baseCmd);
		if (cmd == null)
			return;
		else if (!cmd.isPrivateAvailable() && event.isFromType(ChannelType.PRIVATE)) {
			event.getChannel().sendTyping().queue(success -> event.getChannel().sendMessage(Quotes.getQuote(Quotes.FAIL) + "You cannot execute this Commands in PMs!").queue());
            
        } else if (event.isFromType(ChannelType.PRIVATE) ? !Bran.getInstance().getDataManager().getData().get().getUserData(event.getAuthor()).hasPermission(cmd.getRequiredPermission()) : !Bran.getInstance().getDataManager().getData().get().getGuildData(event.getGuild(), true).hasPermission(event.getAuthor(), cmd.getRequiredPermission())) {
            event.getChannel().sendTyping().queue(sent -> event.getChannel().sendMessage("\u2757 I can't let you do that! You are missing the following permissions: " + String.join(", ", Permissions.toCollection(cmd.getRequiredPermission()))).queue());
            return;
		}
        CommandEvent e = new CommandEvent(event, cmd, event.getMessage().getRawContent(), prefix);
        Bran.getInstance().getSession().cmds++;
		CommandStatsManager.log(cmd);
		Utils.async(cmd.getName() + ">" + Utils.getUser(event.getAuthor()),
				() -> {
					try {
						cmd.execute(e);
						if (!e.isPrivate())
							DroppedMoney.of(event.getTextChannel()).dropWithChance(MathUtils.random(100), 5);
					} catch (Exception ex) {
                        if (ex instanceof PermissionException) {
                            e.sendMessage(ex.getMessage()).queue();
                            return;
                        }
                        LOG.log(ex);
						e.sendMessage(Quotes.FAIL, "An unexpected `" + ex.getClass().getSimpleName() + "` occurred while executing this command, my owner has been informed about this so you don't need to report it.\nException message: `" + ex.getMessage() + "`").queue();
						String url = Hastebin.post(Utils.getStackTrace(ex));
						Bran.getInstance().getDiscordLog().logToDiscord("Uncaught exception in Thread " + Thread.currentThread().getName(), "An unexpected `" + ex.getClass().getSimpleName() + "` occurred.\nMessage: " + ex.getMessage() + "\nStackTrace: " + url, Level.FATAL);
					}
				}).run();
	}
}
