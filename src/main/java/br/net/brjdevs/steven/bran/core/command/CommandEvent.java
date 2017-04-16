package br.net.brjdevs.steven.bran.core.command;

import br.net.brjdevs.steven.bran.core.client.Bran;
import br.net.brjdevs.steven.bran.core.client.Shard;
import br.net.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.net.brjdevs.steven.bran.core.data.GuildData;
import br.net.brjdevs.steven.bran.core.data.UserData;
import br.net.brjdevs.steven.bran.core.managers.Messenger;
import br.net.brjdevs.steven.bran.core.quote.Quotes;
import br.net.brjdevs.steven.bran.core.utils.StringUtils;
import br.net.brjdevs.steven.bran.core.utils.Utils;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.requests.RestAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private String prefix;
	private Map<String, Argument> argsMap;
	private Argument[] arguments;
    
    public CommandEvent(MessageReceivedEvent event, ICommand command, String args, String prefix) {
        this.event = event;
        this.command = command;
        this.message = event.getMessage();
        this.author = event.getAuthor();
        this.args = args;
        this.prefix = prefix;
	    this.argsMap = new HashMap<>();
		this.arguments = command.getArguments().clone();
		Arrays.stream(arguments).forEach(arg -> argsMap.put(arg.getName(), arg));
		if (!Utils.isPrivate(event)) {
			this.member = event.getMember();
            this.guild = event.getGuild();
		}
	}
    
    public Shard getShard() {
        return Bran.getInstance().getShards()[Bran.getInstance().getShardId(event.getJDA())];
	}
	
	public String getPrefix() {
		return prefix;
    }
	
	public RestAction<Message> sendMessage(Quotes quote, String msg) {
		return sendMessage(Quotes.getQuote(quote) + msg);
	}
	
	public RestAction<Message> sendMessage(String msg) {
		return Messenger.sendMessage(getChannel(), msg);
	}
    public RestAction<Message> sendMessage(Message message) {
	    return Messenger.sendMessage(getChannel(), message);
    }
    public RestAction<Message> sendMessage(MessageEmbed embed) {
	    return Messenger.sendMessage(getChannel(), embed);
    }
    public RestAction<Message>  sendPrivate(String msg) {
	    return Messenger.sendMessage(getPrivateChannel(), msg);
    }
	
	public RestAction<Message> sendPrivate(Message message) {
		return Messenger.sendMessage(getPrivateChannel(), message);
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
    
    public GuildData getGuildData(boolean readOnly) {
        return Bran.getInstance().getDataManager().getData().get().getGuildData(guild, readOnly);
    }
	
	public UserData getUserData() {
        return Bran.getInstance().getDataManager().getData().get().getUserData(event.getAuthor());
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
		return argsMap.get(name);
	}
	
	public CommandEvent createChild(ICommand command, boolean b) {
        String newArgs = b ? args : args.replaceFirst(" ", "");
        CommandEvent event = new CommandEvent(this.event, command, newArgs, prefix);
        Thread.currentThread().setName(command.getName() + ">" + Utils.getUser(event.getAuthor()));
        Bran.getInstance().getCommandManager().execute(event);
		return event;
    }
}
