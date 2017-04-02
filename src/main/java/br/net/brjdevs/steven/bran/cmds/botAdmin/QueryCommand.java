package br.net.brjdevs.steven.bran.cmds.botAdmin;

import br.net.brjdevs.steven.bran.core.command.Argument;
import br.net.brjdevs.steven.bran.core.command.Command;
import br.net.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.net.brjdevs.steven.bran.core.command.enums.Category;
import br.net.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.net.brjdevs.steven.bran.core.managers.Permissions;
import br.net.brjdevs.steven.bran.core.sql.SQLDatabase;
import br.net.brjdevs.steven.bran.core.utils.Emojis;
import br.net.brjdevs.steven.bran.core.utils.Hastebin;
import br.net.brjdevs.steven.bran.core.utils.Utils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QueryCommand {
    
    @Command
    private static ICommand sql() {
        return new CommandBuilder(Category.BOT_ADMINISTRATOR)
                .setAliases("query")
                .setRequiredPermission(Permissions.BOT_ADMIN)
                .setName("SQL Command")
                .setDescription("Is is really necessary?")
                .setArgs(new Argument("statement", String.class))
                .setAction((event) -> {
                    String statement = ((String) event.getArgument("statement").get());
                    try {
                        SQLDatabase.getInstance().run((conn) -> {
                            try {
                                ResultSet set;
                                try {
                                    set = conn.prepareStatement(statement).executeQuery();
                                } catch (SQLException e) {
                                    try {
                                        conn.prepareStatement(statement).execute();
                                        event.sendMessage(Emojis.CHECK_MARK + " Query was successfully executed!").queue();
                                    } catch (SQLException e1) {
                                        event.sendMessage("Failed to execute query! " + Hastebin.post(Utils.getStackTrace(e1))).queue();
                                    }
                                    return;
                                }
                                List<String> header = new ArrayList<>();
                                List<List<String>> table = new ArrayList<>();
                                ResultSetMetaData metaData = set.getMetaData();
                                int columnsCount = metaData.getColumnCount();
                                for (int i = 0; i < columnsCount; i++) {
                                    header.add(metaData.getColumnName(i + 1));
                                }
                                while (set.next()) {
                                    List<String> row = new ArrayList<>();
                                    for (int i = 0; i < columnsCount; i++) {
                                        String s = String.valueOf(set.getString(i + 1)).trim();
                                        row.add(s.substring(0, Math.min(30, s.length())));
                                    }
                                    table.add(row);
                                }
                                String output = makeAsciiTable(header, table, null);
                                event.sendMessage(output).queue();
                            } catch (SQLException e) {
                                event.sendMessage(Emojis.X + " Failed to build ascii table! " + Hastebin.post(Utils.getStackTrace(e))).queue();
                            }
                        }).queue();
                    } catch (SQLException e) {
                        event.sendMessage(Emojis.X + " Failed to run query! " + Hastebin.post(Utils.getStackTrace(e))).queue();
                    }
                })
                .build();
    }
    
    public static String makeAsciiTable(List<String> headers, List<List<String>> table, List<String> footer) {
        StringBuilder sb = new StringBuilder();
        int padding = 1;
        int[] widths = new int[headers.size()];
        for (int i = 0; i < widths.length; i++) {
            widths[i] = 0;
        }
        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).length() > widths[i]) {
                widths[i] = headers.get(i).length();
                if (footer != null) {
                    widths[i] = Math.max(widths[i], footer.get(i).length());
                }
            }
        }
        for (List<String> row : table) {
            for (int i = 0; i < row.size(); i++) {
                String cell = row.get(i);
                if (cell.length() > widths[i]) {
                    widths[i] = cell.length();
                }
            }
        }
        sb.append("```").append("\n");
        String formatLine = "|";
        for (int width : widths) {
            formatLine += " %-" + width + "s |";
        }
        formatLine += "\n";
        sb.append(appendSeparatorLine("+", "+", "+", padding, widths));
        sb.append(String.format(formatLine, headers.toArray()));
        sb.append(appendSeparatorLine("+", "+", "+", padding, widths));
        for (List<String> row : table) {
            sb.append(String.format(formatLine, row.toArray()));
        }
        if (footer != null) {
            sb.append(appendSeparatorLine("+", "+", "+", padding, widths));
            sb.append(String.format(formatLine, footer.toArray()));
        }
        sb.append(appendSeparatorLine("+", "+", "+", padding, widths));
        sb.append("```");
        return sb.toString();
    }
    
    private static String appendSeparatorLine(String left, String middle, String right, int padding, int... sizes) {
        boolean first = true;
        StringBuilder ret = new StringBuilder();
        for (int size : sizes) {
            if (first) {
                first = false;
                ret.append(left).append(String.join("", Collections.nCopies(size + padding * 2, "-")));
            } else {
                ret.append(middle).append(String.join("", Collections.nCopies(size + padding * 2, "-")));
            }
        }
        return ret.append(right).append("\n").toString();
    }
}
