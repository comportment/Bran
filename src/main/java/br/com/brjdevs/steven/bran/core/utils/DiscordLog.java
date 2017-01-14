package br.com.brjdevs.steven.bran.core.utils;

import br.com.brjdevs.steven.bran.Bot;
import br.com.brjdevs.steven.bran.core.command.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.DisconnectEvent;
import net.dv8tion.jda.core.events.ReconnectedEvent;
import net.dv8tion.jda.core.events.guild.GenericGuildEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;

import java.awt.*;
import java.time.Instant;
import java.time.ZoneOffset;

public class DiscordLog {
    private static final TextChannel LOG_CHAN = Bot.LOG_CHANNEL;
    private static final Emote CMD_EMOTE = LOG_CHAN.getGuild().getEmoteById("255148787742277642");
    private static final Emote EXCEPTION_EMOTE = LOG_CHAN.getGuild().getEmoteById("255160818004393985");
    private static final Color COLOR = LOG_CHAN.getGuild().getSelfMember().getColor();

    public static void log(CommandEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        String args = StringUtils.splitArgs(event.getArgs(), 2)[1];
        builder.setTitle(CMD_EMOTE.getAsMention() + " Command Log");
	    String footer = "#" + event.getChannel().getName() + (event.getGuild() == null ? " through Private Message." : " on " + event.getGuild().getName() + ".");
	    builder.setFooter(footer, event.getGuild() == null ? Util.getAvatarUrl(event.getAuthor()) : event.getGuild().getIconUrl());
	    builder.setDescription("**Author** " + Util.getUser(event.getAuthor()) + " (" + event.getAuthor().getId() + ")\n" +
                "**Command** " + event.getCommand().getName() + "\n" +
                "**Arguments** " + (args.isEmpty() ? "No arguments were given." : args));
        builder.setColor(COLOR);
        builder.setTimestamp(event.getMessage().getCreationTime());
        LOG_CHAN.sendMessage(builder.build()).queue();
    }
    public static void log(GenericGuildEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        StringBuilder s = new StringBuilder();
        if (event instanceof GuildJoinEvent) {
            s.append("\uD83C\uDFE0 Joined Guild: ");
        } else if (event instanceof GuildLeaveEvent) {
            s.append("\uD83C\uDFDA Left Guild: ");
        } else return;
        s.append(event.getGuild().getName());
        builder.setTitle(s.toString());
        s.delete(0, s.toString().length());
        s.append("**ID** ").append(event.getGuild().getId()).append('\n');
        s.append("**Owner** ").append(Util.getUser(event.getGuild().getOwner().getUser()))
                .append('\n');
        s.append("**Members** ").append(event.getGuild().getMembers().size()).append(" (").append(RequirementsUtils.getBotsPercentage(event.getGuild())).append("% bots)\n");
        builder.setDescription(s.toString());
        builder.setColor(COLOR);
        builder.setTimestamp(Instant.now().atOffset(ZoneOffset.UTC));
        LOG_CHAN.sendMessage(builder.build()).queue();
    }
    public static void log(Throwable throwable) {
        log(throwable, Hastebin.post(Util.getStackTrace(throwable)));
    }
    public static void log(Throwable throwable, String hastebin) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(EXCEPTION_EMOTE.getAsMention() + " Exception Log");
        builder.setDescription("A **`" + throwable.getClass().getSimpleName() + "`** occurred, you can check out the error [here](" + hastebin + ")\n" +
                "**Message:** " + throwable.getMessage());
        builder.setColor(COLOR);
        builder.setTimestamp(Instant.now().atOffset(ZoneOffset.UTC));
        LOG_CHAN.sendMessage(builder.build()).queue();
    }
    public static void log(DisconnectEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setAuthor("\uD83C\uDF10 Connection Log", null, null);
        embedBuilder.setDescription("Got Disconnect Event on Shard " + Bot.getInstance().getShardId(event.getJDA()));
        embedBuilder.setTimestamp(Instant.now().atOffset(ZoneOffset.UTC));
        embedBuilder.setColor(COLOR);
        LOG_CHAN.sendMessage(embedBuilder.build()).queue();
    }
    public static void log(ReconnectedEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setAuthor("\uD83C\uDF10 Connection Log", null, null);
        embedBuilder.setDescription("Got Reconnect Event on Shard " + Bot.getInstance().getShardId(event.getJDA()));
        embedBuilder.setTimestamp(Instant.now().atOffset(ZoneOffset.UTC));
        embedBuilder.setColor(COLOR);
        LOG_CHAN.sendMessage(embedBuilder.build()).queue();
    }
}
