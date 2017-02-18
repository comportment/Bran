package br.com.brjdevs.steven.bran.core.command;

import br.com.brjdevs.steven.bran.Client;
import br.com.brjdevs.steven.bran.DiscordLog.Level;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.command.interfaces.ITreeCommand;
import br.com.brjdevs.steven.bran.core.data.GuildData;
import br.com.brjdevs.steven.bran.core.managers.PrefixManager;
import br.com.brjdevs.steven.bran.core.quote.Quotes;
import br.com.brjdevs.steven.bran.core.utils.Hastebin;
import br.com.brjdevs.steven.bran.core.utils.OtherUtils;
import br.com.brjdevs.steven.bran.core.utils.StringUtils;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.utils.SimpleLog;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static br.com.brjdevs.steven.bran.core.utils.OtherUtils.isEmpty;

public class CommandManager implements EventListener {
	
	private final Client client;
	private final List<ICommand> commands = new ArrayList<>();
	private final SimpleLog LOG = SimpleLog.getLog("Command Manager");
	
	public CommandManager(Client client) {
		this.client = client;
		load();
	}
	
	public void addCommand(ICommand command) {
		if (command != null)
            commands.add(command);
    }
	
	public List<ICommand> getCommands() {
		return commands;
    }
	
	public List<ICommand> getCommands(Category category) {
		return getCommands().stream().filter(cmd -> cmd.getCategory() == category).collect(Collectors.toList());
    }
	
	private void load() {
	    String url = "br.com.brjdevs.steven.bran.cmds";
	    Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forPackage(url)).setScanners(new SubTypesScanner(),
			    new TypeAnnotationsScanner(), new MethodAnnotationsScanner()).filterInputsBy(new FilterBuilder().includePackage(url)));
	    Set<Method> commands = reflections.getMethodsAnnotatedWith(Command.class);
	    commands.forEach(method -> {
		    Class clazz = method.getDeclaringClass();
		    method.setAccessible(true);
		    if (!method.getReturnType().equals(ICommand.class)) {
			    LOG.fatal("Method annotated with Command.class returns " + method.getReturnType().getSimpleName() + " instead of ICommand.class.");
			    return;
		    }
	    	try {
			    ICommand command = (ICommand) method.invoke(null);
			    if (command.getAliases().isEmpty()) {
				    LOG.fatal("Attempted to register ICommand without aliases. (" + clazz.getSimpleName() + ")");
				    return;
			    }
			    if (isEmpty(command.getDescription())) {
				    LOG.fatal("Attempted to register ICommand without description. (" + clazz.getSimpleName() + ")");
				    return;
			    }
			    if (isEmpty(command.getName())) {
				    LOG.fatal("Attempted to register ICommand without name. (" + clazz.getSimpleName() + ")");
				    return;
			    }
			    if (command instanceof ITreeCommand && ((ITreeCommand) command).getSubCommands() != null && ((ITreeCommand) command).getSubCommands().isEmpty()) {
				    LOG.fatal("Attempted to register Tree ICommand without SubCommands. (" + clazz.getSimpleName() + ")");
				    return;
			    }
			    if (command.getCategory() == Category.UNKNOWN) {
				    LOG.fatal("Registered ICommand with UNKNOWN Category. (" + clazz.getSimpleName() + ")");
			    }
			    HelpContainer.generateHelp(command);
			    addCommand(command);
		    } catch (Exception e) {
	    		LOG.log(e);
		    }
		    method.setAccessible(false);
	    });
		LOG.info("Finished loading all Commands.");
	}
	
	@Override
	public void onEvent(Event ev) {
		if (!(ev instanceof MessageReceivedEvent)) return;
		MessageReceivedEvent event = (MessageReceivedEvent) ev;
		if (event.getAuthor().isBot() || event.getAuthor().isFake() || !OtherUtils.isPrivate(event) && !((MessageReceivedEvent) ev).getTextChannel().canTalk())
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