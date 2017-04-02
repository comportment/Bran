package br.net.brjdevs.steven.bran.core.managers;

import br.net.brjdevs.steven.bran.core.client.Bran;
import br.net.brjdevs.steven.bran.core.sql.SQLAction;
import br.net.brjdevs.steven.bran.core.sql.SQLDatabase;
import br.net.brjdevs.steven.bran.core.utils.StringUtils;
import br.net.brjdevs.steven.bran.core.utils.TimePeriod;
import net.dv8tion.jda.core.entities.Guild;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class GuildStatsManager {
	
	public static final Map<LoggedEvent, AtomicInteger>
			TOTAL_EVENTS = new HashMap<>(),
			DAY_EVENTS = new HashMap<>(),
			HOUR_EVENTS = new HashMap<>(),
			MINUTE_EVENTS = new HashMap<>();
	private static final char ACTIVE_BLOCK = '\u2588';
	private static final char EMPTY_BLOCK = '\u200b';
	private static final ExpirationManager EXPIRATION = new ExpirationManager();
	private static final int MINUTE = 60000, HOUR = 3600000, DAY = 86400000;
    
    static {
        try {
            SQLDatabase.getInstance().run((conn) -> {
                try {
                    conn.prepareStatement("CREATE TABLE IF NOT EXISTS GUILDLOG (" +
                            "guildId varchar(18)," +
                            "loggedEvent int," + // 0 = Join; 1 = Leave
                            "date bigint," +
                            "sessionId bigint" +
                            ");").execute();
                } catch (SQLException e) {
                    SQLAction.LOGGER.log(e);
                }
            }).queue();
        } catch (SQLException e) {
            SQLAction.LOGGER.log(e);
        }
    }
    
    public static String bar(int percent, int total) {
		int activeBlocks = (int) ((float) percent / 100f * total);
		StringBuilder builder = new StringBuilder().append('`').append(EMPTY_BLOCK);
		for (int i = 0; i < total; i++) builder.append(activeBlocks > i ? ACTIVE_BLOCK : ' ');
		return builder.append(EMPTY_BLOCK).append('`').toString();
	}
    
    public static Map<LoggedEvent, Integer> getLoggedEvents(TimePeriod period) {
        Map<LoggedEvent, Integer> result = new HashMap<>();
        try {
            SQLDatabase.getInstance().run((conn) -> {
                try {
                    for (int i = 0; i < 2; i++) {
                        long sessionId = Bran.getInstance().getSessionId();
                        PreparedStatement statement = conn.prepareStatement("SELECT COUNT(*) " +
                                "FROM GUILDLOG WHERE loggedEvent=? " +
                                "AND date + " + period.getMillis() + " > " + System.currentTimeMillis() + " " +
                                "AND sessionId = " + sessionId);
                        statement.setInt(1, i);
                        ResultSet set = statement.executeQuery();
                        int times;
                        if (set != null && set.next() && (times = set.getInt(1)) > 0)
                            result.put(LoggedEvent.from(i), times);
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
    
    public static void log(LoggedEvent loggedEvent, Guild guild) {
        try {
            SQLDatabase.getInstance().run((conn) -> {
                try {
                    PreparedStatement statement = conn.prepareStatement("INSERT INTO GUILDLOG VALUES(" +
                            "?, " +
                            "?, " +
                            System.currentTimeMillis() + ", " +
                            Bran.getInstance().getSessionId() + "" +
                            ");");
                    statement.setString(1, guild.getId());
                    statement.setInt(2, loggedEvent == LoggedEvent.JOIN ? 0 : 1);
                    statement.executeUpdate();
                } catch (SQLException e) {
                    SQLAction.LOGGER.log(e);
                }
            }).complete();
        } catch (SQLException e) {
            SQLAction.LOGGER.log(e);
        }
    }
	
	public static String resume(Map<LoggedEvent, AtomicInteger> commands) {
		int total = commands.values().stream().mapToInt(AtomicInteger::get).sum();
		
		return (total == 0) ? ("No Events Logged.") : ("Count: " + total + "\n" + commands.entrySet().stream()
				.filter(entry -> entry.getValue().get() > 0)
				.sorted(Comparator.comparingInt(entry -> total - entry.getValue().get()))
				.limit(5)
				.map(entry -> {
					int percent = entry.getValue().get() * 100 / total;
					return String.format("%s %d%% **%s** (%d)", bar(percent, 15), percent, entry.getKey().toString(), entry.getValue().get());
				})
				.collect(Collectors.joining("\n")));
	}
	
	public enum LoggedEvent {
		JOIN, LEAVE;
        
        public static LoggedEvent from(int i) {
            return i == 0 ? JOIN : LEAVE;
        }
        
        @Override
		public String toString() {
			return StringUtils.capitalize(name());
		}
	}
	
}
