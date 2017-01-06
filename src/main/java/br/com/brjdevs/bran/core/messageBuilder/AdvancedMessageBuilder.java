package br.com.brjdevs.bran.core.messageBuilder;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.*;

import java.util.Random;

public class AdvancedMessageBuilder {
    private static final String[][] quotes =
            {{"Done!", "1, 2, 3 and... Done!", "Here you go.", "Fast, huh?", "Alright!"},
            {"Something went wrong...", "What?! What happened?!", "Uh-oh...",
                    "I don't know what happened! Maybe..."}};
    private static final Random random = new Random();
    private MessageBuilder builder;
    public AdvancedMessageBuilder() {
        builder = new MessageBuilder();
    }
    public AdvancedMessageBuilder append (Quote quoteType) {
        append(Quote.getQuote(quoteType));
        return this;
    }
    public StringBuilder getStringBuilder() {
        return builder.getStringBuilder();
    }
    public AdvancedMessageBuilder clear() {
        builder = new MessageBuilder();
        return this;
    }
    public boolean isEmpty() {
        return builder.isEmpty();
    }
    public boolean canBuild() {
        return getStringBuilder().length() < 2000;
    }
    public MessageBuilder getMessageBuilder() {
        return builder;
    }
    public int length() {
        return builder.length();
    }
    public AdvancedMessageBuilder setEmbed(MessageEmbed embed) {
        builder.setEmbed(embed);
        return this;
    }
    public AdvancedMessageBuilder append(Object obj) {
        builder.append(obj);
        return this;
    }
    public AdvancedMessageBuilder append(IMentionable iMentionable) {
        builder.append(iMentionable);
        return this;
    }
    public AdvancedMessageBuilder replaceFirst(String s, String r) {
        builder.replaceFirst(s, r);
        return this;
    }
    public AdvancedMessageBuilder replaceAll(String s, String r) {
        builder.replaceAll(s, r);
        return this;
    }
    public AdvancedMessageBuilder replaceLast(String s, String r) {
        builder.replaceLast(s, r);
        return this;
    }
    public Message build() {
        return builder.build();
    }

    public enum Quote {
        FAIL, SUCCESS;
        public static String getQuote(Quote quote) {
            if (quote.equals(Quote.FAIL)) return (quotes[1][random.nextInt(quotes[1].length)]) + " ";
            else return (quotes[0][random.nextInt(quotes[0].length)]) + " ";
        }
    }
}
