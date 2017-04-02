package br.net.brjdevs.steven.bran.core.command;

import br.net.brjdevs.steven.bran.core.client.Bran;
import br.net.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.net.brjdevs.steven.bran.core.sql.SQLAction;
import br.net.brjdevs.steven.bran.core.sql.SQLDatabase;
import br.net.brjdevs.steven.bran.core.utils.StringUtils;
import br.net.brjdevs.steven.bran.core.utils.TimePeriod;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandStatsManager {
    
    public static Map<String, Integer> getIssuedCommands(TimePeriod period) {
        Map<String, Integer> result = new HashMap<>();
        try {
            SQLDatabase.getInstance().run((conn) -> {
                try {
                    long sessionId = Bran.getInstance().getSessionId();
                    for (ICommand cmd : Bran.getInstance().getCommandManager().getCommands()) {
                        PreparedStatement statement = conn.prepareStatement("SELECT COUNT(*) " +
                                "FROM CMDLOG WHERE cmd=? " +
                                "AND date + " + period.getMillis() + " > " + System.currentTimeMillis() + " " +
                                "AND sessionId = " + sessionId);
                        statement.setString(1, cmd.getName());
                        ResultSet set = statement.executeQuery();
                        int times;
                        if (set != null && set.next() && (times = set.getInt(1)) > 0)
                            result.put(cmd.getName(), times);
                    }
                } catch (SQLException e) {
                    SQLAction.LOGGER.log(e);
                }
            }).complete();
        } catch (SQLException e) {
            SQLAction.LOGGER.log(e);
        }
        return result;
    }
    
    public static String resume(Map<String, Integer> commands) {
        int total = commands.values().stream().mapToInt(Integer::intValue).sum();
    
        return (total == 0) ? ("No Commands issued.") : ("Count: " + total + "\n" + commands.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .sorted(Comparator.comparingInt(entry -> total - entry.getValue()))
                .limit(5)
				.map(entry -> {
                    int percent = entry.getValue() * 100 / total;
                    return String.format("[`%s`] %d%% **%s**", StringUtils.getProgressBar(percent), percent, entry.getKey());
                })
				.collect(Collectors.joining("\n")));
	}
}
