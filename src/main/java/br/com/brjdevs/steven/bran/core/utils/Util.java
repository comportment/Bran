package br.com.brjdevs.steven.bran.core.utils;

import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Random;

public class Util {
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
    public static boolean isPrivate(MessageReceivedEvent event) {
        return event.isFromType(ChannelType.PRIVATE);
    }
    public static String getUser(User user) {
        if (user == null) return "Unknown#0000";
        return user.getName() + "#" + user.getDiscriminator();
    }
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }
    public static String getAvatarUrl(User user) {
        return user.getAvatarUrl() == null ? user.getDefaultAvatarUrl() : user.getAvatarUrl();
    }
    public static Runnable async(final Runnable doAsync) {
        return new Thread(doAsync)::start;
    }
    public static Runnable async(final String name, final Runnable doAsync) {
        return new Thread(doAsync, name)::start;
    }
    public static boolean isInteger(String str) {
        try {
            int i = Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    public static String getStackTrace(Throwable e){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
    public static boolean isEmpty(Object object) {
        return object == null || object.toString().isEmpty();
    }
    public static String randomName(int randomLength) {
        char[] characters = new char[]
                {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
                        'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
                        '1','2','3','4','5','6','7','8','9','0'};

        Random rand = new Random();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < randomLength; i++) {
            builder.append(characters[rand.nextInt(characters.length)]);
        }
        return builder.toString();
    }
	
	public static boolean containsEqualsIgnoreCase(Collection<String> collection, String s) {
		return collection.stream().anyMatch((item) -> item.equalsIgnoreCase(s));
	}
}