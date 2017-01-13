package br.com.brjdevs.steven.bran.core.command;

import br.com.brjdevs.steven.bran.core.data.guild.DiscordGuild;
import br.com.brjdevs.steven.bran.core.data.guild.configs.GuildMember;
import br.com.brjdevs.steven.bran.core.data.guild.configs.GuildMember.FakeGuildMember;
import br.com.brjdevs.steven.bran.core.quote.Quotes;
import br.com.brjdevs.steven.bran.core.utils.StringUtils;
import br.com.brjdevs.steven.bran.core.utils.Util;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.requests.RestAction;

public class CommandEvent {
    private Guild guild;
    private Message message;
    private Member member;
    private User author;
    private MessageReceivedEvent event;
    private ICommand command;
    private String args;
    private DiscordGuild discordGuild;
    private GuildMember guildMember;
    private String prefix;
    public CommandEvent(MessageReceivedEvent event, ICommand command, DiscordGuild discordGuild, String args, String prefix) {
        this.event = event;
        this.command = command;
        this.message = event.getMessage();
        this.author = event.getAuthor();
        this.args = args;
        this.prefix = prefix;
        if (!Util.isPrivate(event)) {
            this.discordGuild = discordGuild;
            this.member = event.getMember();
            this.guild = event.getGuild();
            this.guildMember = discordGuild.getMember(author);
        }
        if (this.guildMember == null)
	        this.guildMember = new FakeGuildMember(author, null);
    }
    public String getPrefix() {
        return prefix;
    }
	
	public RestAction<Message> sendMessage(Quotes quote, String msg) {
		return sendMessage(Quotes.getQuote(quote) + msg);
	}
	
	public RestAction<Message> sendMessage(String msg) {
        getChannel().sendTyping().complete();
        return getChannel().sendMessage(msg);
    }
    public RestAction<Message> sendMessage(Message message) {
        return sendMessage(message.getRawContent());
    }
    public RestAction<Message> sendMessage(MessageEmbed embed) {
        getChannel().sendTyping().complete();
        return getChannel().sendMessage(embed);
    }
    public RestAction<Message>  sendPrivate(String msg) {
        getChannel().sendTyping().complete();
        return getPrivateChannel().sendMessage(msg);
    }
    public RestAction<Message> sendPrivate(Message message) {
        return sendPrivate(message.getRawContent());
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
    public Member getOriginMember () {
        return member;
    }
    public GuildMember getMember() {
        return guildMember;
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
    public DiscordGuild getGuild() {
        return discordGuild;
    }
    public Guild getOriginGuild() {
        return guild;
    }
    public MessageChannel getChannel() {
        return event.getChannel();
    }
    public TextChannel getTextChannel() {
        return (TextChannel) getChannel();
    }
    public Member getSelfMember() {
        return guild.getSelfMember();
    }
    public MessageReceivedEvent getEvent() {
        return event;
    }
    public boolean isPrivate() {
        return event.isFromType(ChannelType.PRIVATE);
    }
    public CommandEvent createChild(ICommand command, boolean b) {
        String newArgs = b ? args : args.replaceFirst(" ", "");
        CommandEvent event = new CommandEvent(this.event, command, discordGuild, newArgs, prefix);
        Thread.currentThread().setName(command.getName() + ">" + Util.getUser(event.getAuthor()));
        command.execute(event, newArgs);
        return event;
    }
}