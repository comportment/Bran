package br.net.brjdevs.steven.bran.core.sql;

import java.sql.Connection;

public interface SQLTask {
    
    void run(Connection c);
}
 