package br.com.brjdevs.bran.core.command;

import br.com.brjdevs.bran.Bot;
import br.com.brjdevs.bran.core.Permissions;
import br.com.brjdevs.bran.core.PrefixManager;
import br.com.brjdevs.bran.core.data.guild.DiscordGuild;
import br.com.brjdevs.bran.core.data.guild.configs.GuildMember;
import br.com.brjdevs.bran.core.data.guild.configs.impl.GuildMemberImpl.FakeGuildMemberImpl;
import br.com.brjdevs.bran.core.messageBuilder.AdvancedMessageBuilder;
import br.com.brjdevs.bran.core.messageBuilder.AdvancedMessageBuilder.Quote;
import br.com.brjdevs.bran.core.utils.DiscordLog;
import br.com.brjdevs.bran.core.utils.StringUtils;
import br.com.brjdevs.bran.core.utils.Util;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.utils.SimpleLog;
import org.reflections.Reflections;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CommandManager implements EventListener {
    private static final List<ICommand> commands = new ArrayList<>();

    public static void addCommand (ICommand command) {
        if (command != null)
            commands.add(command);
    }

    public static List<ICommand> getCommands () {
        return commands;
    }

    public void onEvent (Event ev) {
        if (!(ev instanceof MessageReceivedEvent)) return;
        MessageReceivedEvent event = (MessageReceivedEvent) ev;
        if (event.getAuthor().isBot() || event.getAuthor().isFake()) return;
        String msg = event.getMessage().getRawContent();
        String[] args = StringUtils.splitSimple(msg);
        DiscordGuild discordGuild = event.getGuild() != null ? DiscordGuild.getInstance(event.getGuild()) : null;
        String prefix = PrefixManager.getPrefix(args[0], discordGuild);
        if (prefix == null) return;
        String baseCmd = args[0].substring(prefix.length());
        ICommand cmd = getCommands().stream().filter(c -> c.getAliases().contains(baseCmd))
                .findFirst().orElse(null);
        if (cmd == null) return;
        CommandEvent e = new CommandEvent(event, cmd, discordGuild, event.getMessage().getRawContent(), prefix);
        if (TooFast.isEnabled() && !TooFast.checkCanExecute(e)) return;
        AdvancedMessageBuilder builder = new AdvancedMessageBuilder();
        if (!cmd.isPrivateAvailable() && Util.isPrivate(event)) {
            builder.append(Quote.FAIL);
            builder.append("This command is not available through PMs, " +
                    "use it in a Text Channel please.");
            e.sendMessage(builder.build()).queue();
            return;
        }
        GuildMember member = discordGuild != null ? discordGuild.getMember(event.getAuthor()) : new FakeGuildMemberImpl(event.getAuthor().getId(), null);
        if (!member.hasPermission(cmd.getRequiredPermission(), event.getJDA())) {
            builder.append(Quote.FAIL);
            builder.append("You don't have enough permissions to do this!\n" +
                    "Missing Permission(s): *" +
                    String.join(", ", Permissions
                            .toCollection(cmd.getRequiredPermission())) + "*");
            e.sendMessage(builder.build()).queue();
            return;
        }

        Bot.getInstance().getSession().cmds++;
        Util.async(cmd.getName() + ">" + Util.getUser(event.getAuthor()),
                () -> {
                    try {
                        cmd.execute(e, event.getMessage().getRawContent());
                    } catch (Exception ex) {
                        Bot.LOG.log(ex);
                        e.sendMessage(Quote.getQuote(Quote.FAIL) + "A `" + ex.getClass().getSimpleName() + "` occurred while executing this command, my owner has been informed about this so you don't need to report it.").queue();
                        DiscordLog.log(ex);
                    }
                }).run();
    }
    public static List<ICommand> getCommands(Category category) {
        return getCommands().stream().filter(cmd -> cmd.getCategory() == category).collect(Collectors.toList());
    }
    private static final SimpleLog LOG = SimpleLog.getLog("Command Manager");
    public static void load() {
    	String url = "br.com.brjdevs.bran.cmds";
	    Reflections reflections = new Reflections(url);
	    Set<Class<?>> commands = reflections.getTypesAnnotatedWith(RegisterCommand.class);
	    commands.forEach(clazz -> {
	    	try {
			    clazz.newInstance();
		    } catch (Exception e) {
	    		LOG.log(e);
		    }
	    });
    }
    @Deprecated
    public static MessageEmbed getHelpEmbed(ICommand command) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor("Help for " + command.getName(),null,null);
        if (command.getSubCommands() != null) {
            builder.addField("Name", command.getName(), true).addField("Aliases", String.join(", ", command.getAliases()), true).addField("Required Permission(s)", String.join(", ", Permissions.toCollection(command.getRequiredPermission())), true);
            command.getSubCommands().forEach(cmd -> builder.addField((String.join(", ", cmd.getAliases())) + ": " + (cmd.getDescription() == null ? "Type '" + cmd.getRequiredArgs() + "' to get help." : cmd.getDescription()), (cmd.getSubCommands() == null ? "Usage Instruction: " + cmd.getRequiredArgs() + "\n" : "") + "Required Permission(s): " + (String.join(", ", Permissions.toCollection(cmd.getRequiredPermission()))), false));
        } else {
            builder.addField("Name", command.getName(), false);
            builder.addField("Description", command.getDescription(), true).addField("Aliases", String.join(", ", command.getAliases()), true).addField("Required Permission(s)", String.join(", ", Permissions.toCollection(command.getRequiredPermission())), true);
            builder.addField("Usage Instruction", command.getRequiredArgs(), false);
        }
        builder.setColor(Color.decode("#ff8f50"));
        return builder.build();
    }
    public static MessageEmbed getHelp(ICommand command, GuildMember member, Member m) {
        //StringBuilder builder = new StringBuilder();
        //builder.append("**" + command.getName() + "** - Required Permission(s): " + (String.join(", ", Permissions.toCollection(command.getRequiredPermission()))) + " - Aliases: " + (String.join(", ", command.getAliases())));
        //builder.append("Command: ").append(command.getName()).append('\n');
        //builder.append("Parameters:");
        //builder.append('\n');
        //if (command.getSubCommands() != null)
        //    command.getSubCommands().forEach(cmd -> builder.append("    **").append(cmd.getAliases().get(0)).append("** ").append(cmd.getRequiredArgs() != null ? cmd.getRequiredArgs() : "").append(" - ").append(cmd.getDescription()).append("  ").append(member.hasPermission(cmd.getRequiredPermission()) ? "" : "*(" + String.join(", ", Permissions.toCollection(cmd.getRequiredPermission())) + ")*").append('\n'));
        //builder.append("\nExample:\n    *" + command.getExample() + "*");
        //return builder.toString();
        EmbedBuilder builder = new EmbedBuilder();
        String desc = "";
        //desc += command.getCategory().getEmoji() + " **| " + command.getCategory().getKey() + "**\n";
        desc += "**" + command.getName() + "**\n";
        if (command.getSubCommands() == null)
            desc += "Description: " + command.getDescription() + "\n";
        if (command.getRequiredArgs() != null) {
            desc += "Required Arguments: " + command.getRequiredArgs() + "";
            desc += "     *Please note: do **NOT** include <> or []*\n";
        }
        desc += "Required Permission(s): " + String.join(", ", Permissions.toCollection(command.getRequiredPermission())) + "\n";
        if (command.getSubCommands() != null) {
            desc += "Parameters:\n";
            Set<Category> categories = command.getSubCommands().stream().map(ICommand::getCategory).collect(Collectors.toSet());
            for (Category category : categories) {
                List<ICommand> commands = command.getSubCommands().stream().filter(cmd -> cmd.getCategory() == category).collect(Collectors.toList());
                if (commands.isEmpty()) continue;
                desc += category.getEmoji() + " **| " + category.getKey() + "**\n";
                for (ICommand cmd : commands)
                    desc += "          **" + cmd.getAliases().get(0) + "** " + (cmd.getRequiredArgs() != null ? cmd.getRequiredArgs() : "") + " - " + cmd.getDescription() + (member.hasPermission(cmd.getRequiredPermission(), m.getJDA()) ? "" : String.join(", ", Permissions.toCollection(cmd.getRequiredPermission()))) + "\n";
                desc += '\n';
            }
        }
        if (command.getExample() != null)
            desc += "Example:\n       *" + command.getExample() + "*";
        builder.setColor(m != null && m.getColor() != null ? m.getColor() : Color.decode("#D68A38"));
        builder.setDescription(desc);
        return builder.build();
    }
}