package br.com.brjdevs.steven.bran.core.utils;

import br.com.brjdevs.steven.bran.Bot;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

import java.util.List;
import java.util.stream.Collectors;

public class RequirementsUtils {
    //TODO spam check
    public static final float MAX_BOTS = 75f;

    public static List<Guild> getBotCollections() {
        return Bot.getInstance().getGuilds().stream()
                .filter(g ->
                        (100f / (float) g.getMembers().size())
                                * (float) getBots(g).size() > MAX_BOTS)
                .collect(Collectors.toList());
    }
    public static List<User> getBots(Guild guild) {
        return guild.getMembers().stream()
                .filter(m -> m.getUser().isBot()).map(Member::getUser)
                .collect(Collectors.toList());
    }
    public static float getBotsPercentage (Guild g) {
        return (100f / (float) g.getMembers().size())
                * (float) getBots(g).size();
    }
}