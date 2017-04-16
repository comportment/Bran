package br.net.brjdevs.steven.bran.log;

import br.net.brjdevs.steven.bran.core.client.Bran;
import br.net.brjdevs.steven.bran.core.utils.Hastebin;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import net.dv8tion.jda.core.entities.TextChannel;

/**
 * @author Kodehawa, AdrianTodt
 */

public class DiscordLogBack extends AppenderBase<ILoggingEvent> {
    private static boolean enabled = false;

    private static TextChannel consoleChannel() {
        return Bran.getInstance().getTextChannelById("249971874430320660");
    }

    public static void disable() {
        enabled = false;
    }

    public static void enable() {
        enabled = true;
    }

    private PatternLayout patternLayout;
    private ILoggingEvent previousEvent;

    @Override
    protected void append(ILoggingEvent event) {
        if (!enabled) return;
        if (!event.getLevel().isGreaterOrEqual(Level.INFO)) return;
        String toSend = patternLayout.doLayout(event);
        if (previousEvent != null && event.getMessage().equals(previousEvent.getMessage())) return;
        if (toSend.length() > 1920) toSend = "Received a message but it was too long, Hastebin:" + Hastebin.post(toSend);
        consoleChannel().sendMessage(toSend).queue();
        previousEvent = event;
    }

    @Override
    public void start() {
        patternLayout = new PatternLayout();
        patternLayout.setContext(getContext());
        patternLayout.setPattern("[`%d{HH:mm:ss}`] [`%t/%level`] [`%logger{0}`]: %msg");
        patternLayout.start();

        super.start();
    }
}
