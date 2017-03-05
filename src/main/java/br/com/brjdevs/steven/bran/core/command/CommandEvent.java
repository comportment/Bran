package br.com.brjdevs.steven.bran.core.command;

import br.com.brjdevs.steven.bran.core.client.Bran;
import br.com.brjdevs.steven.bran.core.client.BranShard;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.data.GuildData;
import br.com.brjdevs.steven.bran.core.data.UserData;
import br.com.brjdevs.steven.bran.core.managers.Messenger;
import br.com.brjdevs.steven.bran.core.quote.Quotes;
import br.com.brjdevs.steven.bran.core.utils.StringUtils;
import br.com.brjdevs.steven.bran.core.utils.Utils;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.utils.SimpleLog;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CommandEvent {
    private Guild guild;
    private Message message;
    private Member member;
    private User author;
    private MessageReceivedEvent event;
    private ICommand command;
    private String args;
	private GuildData guildData;
	private String prefix;
	private Map<String, Argument> argsMap;
	private Argument[] arguments;
	
	public CommandEvent(MessageReceivedEvent event, ICommand command, GuildData guildData, String args, String prefix) {
		this.event = event;
        this.command = command;
        this.message = event.getMessage();
        this.author = event.getAuthor();
        this.args = args;
        this.prefix = prefix;
	    this.argsMap = new HashMap<>();
		this.arguments = CommandUtils.copy(command);
		Arrays.stream(arguments).forEach(arg -> argsMap.put(arg.getName(), arg));
		if (!Utils.isPrivate(event)) {
			this.guildData = guildData;
			this.member = event.getMember();
            this.guild = event.getGuild();
		}
	}
	
	public Messenger getMessenger() {
		return Bran.getInstance().getMessenger();
	}
	
	public BranShard getShard() {
		return Bran.getInstance().getShards()[Bran.getInstance().getShardId(event.getJDA())];
	}
	
	public String getPrefix() {
		return prefix;
    }
	
	public RestAction<Message> sendMessage(Quotes quote, String msg) {
		return sendMessage(Quotes.getQuote(quote) + msg);
	}
	
	public RestAction<Message> sendMessage(String msg) {
		return getMessenger().sendMessage(getChannel(), msg);
	}
    public RestAction<Message> sendMessage(Message message) {
	    return getMessenger().sendMessage(getChannel(), message);
    }
    public RestAction<Message> sendMessage(MessageEmbed embed) {
	    return getMessenger().sendMessage(getChannel(), embed);
    }
    public RestAction<Message>  sendPrivate(String msg) {
	    return getMessenger().sendMessage(getPrivateChannel(), msg);
    }
	
	public RestAction<Message> sendPrivate(Message message) {
		return getMessenger().sendMessage(getPrivateChannel(), message);
	}
	
	public PrivateChannel getPrivateChannel() {
        return getAuthor().openPrivateChannel().complete();
    }
	
	public User getAuthor() {
        return author;
    }
	
	public JDA getJDA() {
        return event.getJDA();
    }
	
	public Member getMember() {
		return member;
    }
	
    public ICommand getCommand() {
        return command;
    }
    public String getArgs() {
        return args;
    }
    public String[] getArgs(int i) {
        return StringUtils.splitArgs(args, i);
    }
    public Message getMessage() {
        return message;
    }
	
	public GuildData getGuildData() {
		return guildData;
	}
	
	public UserData getUserData() {
		return Bran.getInstance().getDataManager().getDataHolderManager().get().getUser(event.getAuthor());
	}
	
	public Guild getGuild() {
		return guild;
    }
    public MessageChannel getChannel() {
        return event.getChannel();
    }
    public TextChannel getTextChannel() {
        return (TextChannel) getChannel();
    }
    public Member getSelfMember() {
	    if (isPrivate()) return null;
	    return guild.getSelfMember();
    }
    public MessageReceivedEvent getEvent() {
        return event;
    }
    public boolean isPrivate() {
        return event.isFromType(ChannelType.PRIVATE);
    }
	
	public Argument[] getArguments() {
		return arguments;
	}
	
	public Argument getArgument(String name) {
		if (!argsMap.containsKey(name)) SimpleLog.getLog("Argument Getter").fatal("TYPO CARALHO");
		return argsMap.get(name);
	}
	
	public CommandEvent createChild(ICommand command, boolean b) {
        String newArgs = b ? args : args.replaceFirst(" ", "");
		CommandEvent event = new CommandEvent(this.event, command, guildData, newArgs, prefix);
		Thread.currentThread().setName(command.getName() + ">" + Utils.getUser(event.getAuthor()));
		command.execute(event);
		return event;
    }
}
