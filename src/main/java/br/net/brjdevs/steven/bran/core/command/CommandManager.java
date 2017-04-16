package br.net.brjdevs.steven.bran.core.command;

import br.net.brjdevs.steven.bran.core.client.Bran;
import br.net.brjdevs.steven.bran.core.command.enums.Category;
import br.net.brjdevs.steven.bran.core.command.enums.CommandAction;
import br.net.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.net.brjdevs.steven.bran.core.command.interfaces.ITreeCommand;
import br.net.brjdevs.steven.bran.core.managers.Permissions;
import br.net.brjdevs.steven.bran.core.sql.SQLAction;
import br.net.brjdevs.steven.bran.core.sql.SQLDatabase;
import br.net.brjdevs.steven.bran.core.utils.StringUtils;
import br.net.brjdevs.steven.bran.core.utils.TimePeriod;
import br.net.brjdevs.steven.bran.core.utils.Utils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class CommandManager {

    private static List<ICommand> commands = new ArrayList<>();
    private final Logger LOGGER = LoggerFactory.getLogger("Command Manager");
	
	public CommandManager() {

        try {
            SQLDatabase.getInstance().run((conn) -> {
                try {
                    conn.prepareStatement("CREATE TABLE IF NOT EXISTS CMDLOG (" +
                            "id int NOT NULL AUTO_INCREMENT, " +
                            "cmd text, " +
                            "arguments text, " +
                            "userid text, " +
                            "channelid text, " +
                            "guildid text, " +
                            "date bigint, " +
                            "successful int," +
                            "sessionId bigint, " +
                            "PRIMARY KEY (id)" +
                            ");").execute();
                    conn.prepareStatement("ALTER TABLE CMDLOG AUTO_INCREMENT=1;").execute();
                    
                } catch (SQLException exception) {
                    LOGGER.error("Failed to execute SQLAction!", exception);
                }
            }).queue();
        } catch (SQLException e) {
            LOGGER.error("Failed to execute SQLAction!", e);
        }
        new Thread(this::load).start();
    }
    
    public void addCommand(ICommand command) {
        if (command != null) {
            commands.add(command);
        }
    }
    
    public MessageEmbed getHelp(ICommand command, Member m) {
        return new EmbedBuilder().setColor(m != null && m.getColor() != null ? m.getColor() : Color.decode("#D68A38")).setDescription(command.getHelpMessage()).build();
    }
	
	public List<ICommand> getCommands() {
		return commands;
    }
	
	public List<ICommand> getCommands(Category category) {
		return getCommands().stream().filter(cmd -> cmd.getCategory() == category).collect(Collectors.toList());
    }
	
	public ICommand getCommand(ITreeCommand tree, String alias) {
		return tree.getSubCommands().stream().filter(sub -> Arrays.stream(sub.getAliases()).anyMatch(s -> s.equals(alias))).findFirst().orElse(null);
	}
	
	public ICommand getCommand(String alias) {
		return getCommands().stream().filter(cmd -> Arrays.stream(cmd.getAliases()).anyMatch(s -> s.equals(alias))).findFirst().orElse(null);
	}
	
	private void load() {
        String url = "br.net.brjdevs.steven.bran.cmds";
        Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forPackage(url)).setScanners(new SubTypesScanner(),
			    new TypeAnnotationsScanner(), new MethodAnnotationsScanner()).filterInputsBy(new FilterBuilder().includePackage(url)));
	    Set<Method> commands = reflections.getMethodsAnnotatedWith(Command.class);
	    commands.stream().filter(method -> method.getReturnType() == ICommand.class).forEach(method -> {
		    method.setAccessible(true);
	    	try {
			    ICommand command = (ICommand) method.invoke(null);
			    addCommand(command);
		    } catch (Exception e) {
	    		LOGGER.error("Failed to register Command!");
		    }
		    method.setAccessible(false);
	    });
		LOGGER.info("Finished loading " + commands.size() + " commands.");
	}
    
    public void log(ICommand cmd, String args, User user, MessageChannel channel, Guild guild, boolean successful) {
        try {
            SQLDatabase.getInstance().run((conn) -> {
                try {
                    PreparedStatement statement = conn.prepareStatement("INSERT INTO CMDLOG " +
                            "(cmd, arguments, userid, channelid, guildid, date, successful, sessionId) VALUES(" +
                            "?, " +
                            "?, " +
                            "?, " +
                            "?, " +
                            "?, " +
                            "?, " +
                            "?," +
                            "? " +
                            ");");
                    statement.setString(1, cmd.getName());
                    statement.setString(2, args);
                    statement.setString(3, user.getId());
                    statement.setString(4, channel.getId());
                    statement.setString(5, guild == null ? null : guild.getId());
                    statement.setLong(6, System.currentTimeMillis());
                    statement.setInt(7, successful ? 1 : 0);
                    statement.setLong(8, Bran.getInstance().getSessionId());
                    
                    statement.executeUpdate();
                } catch (SQLException exception) {
                    LOGGER.error("Failed to execute SQLAction!", exception);
                }
            }).queue();
        } catch (SQLException e) {
            LOGGER.error("Failed to execute SQLAction!", e);
        }
    }

    public Map<String, Integer> getIssuedCommands(TimePeriod period) {
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
                    LOGGER.error("Failed to execute SQLAction!", e);
                }
            }).complete();
        } catch (SQLException e) {
            LOGGER.error("Failed to execute SQLAction!", e);
        }
        return result;
    }

    public String resume(Map<String, Integer> commands) {
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

    public void execute(CommandEvent event) {
	    ICommand cmd = event.getCommand();
        if (event.isPrivate() && !cmd.isPrivateAvailable()) {
            event.sendMessage("This Command is not available in PMs, please use it in a Guild Text Channel.").queue();
            return;
        } else if (event.isPrivate() ? !event.getUserData().hasPermission(cmd.getRequiredPermission()) : !event.getGuildData(true).hasPermission(event.getAuthor(), cmd.getRequiredPermission())) {
            event.sendMessage("You don't have enough permissions to execute this Command!\n*Missing Permission(s): " + String.join(", ", Permissions.toCollection(cmd.getRequiredPermission())) + "*").queue();
            return;
        } else if (!event.isPrivate() && event.getGuildData(true).getDisabledCommands(event.getTextChannel()).contains(event.getCommand().getKey()))
            return;

        if (cmd instanceof ITreeCommand) {
            String alias = event.getArgs(3)[1].trim();
            boolean isDefault = false;
            if (alias.isEmpty()) {
                if (((ITreeCommand) cmd).getDefaultCommand() != null) {
                    alias = ((ITreeCommand) cmd).getDefaultCommand();
                    isDefault = true;
                } else {
                    if (event.getGuild() != null && !event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_EMBED_LINKS)) {
                        event.sendMessage("I can't send you help without the MESSAGE_EMBED_LINKS permission!").queue();
                    } else {
                        event.sendMessage(Bran.getInstance().getCommandManager().getHelp(event.getCommand(), event.getSelfMember())).queue();
                    }
                    return;
                }
            }
            String falias = alias;
            ICommand subCommand = ((ITreeCommand) cmd).getSubCommands().stream().filter(sub -> Arrays.stream(sub.getAliases()).anyMatch(x -> x.equals(falias))).findFirst().orElse(null);
            if (subCommand == null) {
                switch (((ITreeCommand) cmd).onNotFound()) {
                    case REDIRECT:
                        ICommand defCmd = ((ITreeCommand) cmd).getSubCommands().stream().filter(sub -> Arrays.stream(sub.getAliases()).anyMatch(x -> x.equals(((ITreeCommand) cmd).getDefaultCommand()))).findFirst().orElse(null);
                        if (defCmd != null) {
                            event.createChild(defCmd, true);
                            break;
                        }
                    case SHOW_ERROR:
                        event.sendMessage("No such sub command `" + alias + "` in " + cmd.getName() + ".").queue();
                        break;
                    case SHOW_HELP:
                        if (!event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_EMBED_LINKS)) {
                            event.sendMessage("I can't send you help without the MESSAGE_EMBED_LINKS permission!").queue();
                        } else {
                            event.sendMessage(Bran.getInstance().getCommandManager().getHelp(event.getCommand(), event.getSelfMember())).queue();
                        }
                        break;
                }
                return;
            } else if (event.isPrivate() ? !event.getUserData().hasPermission(cmd.getRequiredPermission()) : !event.getGuildData(true).hasPermission(event.getAuthor(), cmd.getRequiredPermission())) {
                switch (((ITreeCommand) cmd).onMissingPermission()) {
                    case REDIRECT:
                        ICommand defCmd = ((ITreeCommand) cmd).getSubCommands().stream().filter(sub -> Arrays.stream(sub.getAliases()).anyMatch(x -> x.equals(((ITreeCommand) cmd).getDefaultCommand()))).findFirst().orElse(null);
                        if (defCmd != null) {
                            event.createChild(defCmd, true);
                            break;
                        }
                    case SHOW_ERROR:
                        event.sendMessage("\u2757 I can't let you do that! You are missing the following permissions: " + String.join(", ", Permissions.toCollection(cmd.getRequiredPermission()))).queue();
                        break;
                    case SHOW_HELP:
                        if (!event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_EMBED_LINKS)) {
                            event.sendMessage("I can't send you help without the MESSAGE_EMBED_LINKS permission!").queue();
                        } else {
                            event.sendMessage(Bran.getInstance().getCommandManager().getHelp(event.getCommand(), event.getSelfMember())).queue();
                        }
                        break;
                }
                return;
            }
            event.createChild(subCommand, isDefault);
        } else {
            String[] split = event.getArgs(2);
            String[] s = cmd.getArgumentParser().apply(split[1].trim());
            Argument[] args = event.getArguments();
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    try {
                        if (s[i].trim().isEmpty() && !args[i].isOptional()) {
                            throw new ArgumentParsingException("Insufficient or invalid arguments were given!");
                        }
                        args[i].parse(s[i].trim());
                        if (!args[i].isPresent() && !args[i].isOptional())
                            throw new ArgumentParsingException("Insufficient or invalid arguments were given!");
                    } catch (ArgumentParsingException | ArrayIndexOutOfBoundsException ex) {
                        if (!args[i].isOptional()) {
                            event.sendMessage("**Bad Arguments:** " + ex.getMessage() + ".\nExpected arguments: " + (String.join(" ", Arrays.stream(cmd.getArguments()).map(arg -> (arg.isOptional() ? "<" : "[") + arg.getType().getSimpleName() + ": " + arg.getName() + (arg.isOptional() ? ">" : "]")).toArray(String[]::new)))).queue();
                            return;
                        }
                    }
                }
            }
            cmd.execute(event);
        }
    }
}