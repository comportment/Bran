package br.net.brjdevs.steven.bran.core.sql;

import net.dv8tion.jda.core.utils.SimpleLog;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SQLAction {
    
    public static final SimpleLog LOGGER = SimpleLog.getLog("SQLAction");
    private static final ExecutorService SQL_SERVICE = Executors.newCachedThreadPool(r -> new Thread(r, "SQL Thread "));
    private Connection conn;
    private SQLTask task;
    
    public SQLAction(Connection conn, SQLTask task) {
        this.conn = conn;
        this.task = task;
    }
    
    public void complete() throws SQLException {
        task.run(conn);
        if (!conn.isClosed())
            conn.close();
    }
    
    public void queue() {
        SQL_SERVICE.submit(() -> {
            try {
                complete();
            } catch (Exception e) {
                LOGGER.log(e);
            }
        });
    }
    
}
