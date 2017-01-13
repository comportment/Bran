package br.com.brjdevs.steven.bran.core.command;

import br.com.brjdevs.steven.bran.Bot;
import br.com.brjdevs.steven.bran.core.data.guild.configs.GuildMember;
import br.com.brjdevs.steven.bran.core.data.guild.configs.GuildMember.FakeGuildMember;
import br.com.brjdevs.steven.bran.core.managers.Permissions;
import br.com.brjdevs.steven.bran.core.quote.Quotes;
import br.com.brjdevs.steven.bran.core.utils.StringUtils;
import br.com.brjdevs.steven.bran.core.utils.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TreeCommandBuilder {
    private List<ICommand> commands = new ArrayList<>();
    private List<String> aliases = new ArrayList<>();
    private String name = null;
    private Long perm = Permissions.RUN_BASECMD;
    private String help = null;
    private boolean privateAvailable = true;
    private String defaultcmd = null;
    private String example = null;
    private Category category = Category.UNKNOWN;
	private CommandAction onNotFound = CommandAction.SHOW_ERROR;
	private CommandAction onMissingPermission = CommandAction.SHOW_ERROR;
	
	public TreeCommandBuilder(Category category) {
        this.category = category;
    }
    
    public TreeCommandBuilder setDefault(String alias){
        this.defaultcmd = alias;
        return this;
    }
    public TreeCommandBuilder addCommand(ICommand command) {
        commands.add(command);
        return this;
    }
    public TreeCommandBuilder setAliases(String... aliases) {
        Stream.of(aliases).forEach(this.aliases::add);
        return this;
    }
    public TreeCommandBuilder setName(String name) {
        this.name = name;
        return this;
    }
    public TreeCommandBuilder setPrivateAvailable(boolean b) {
        this.privateAvailable = b;
        return this;
    }
    public TreeCommandBuilder setRequiredPermission(Long perm) {
        this.perm = perm;
        return this;
    }
    public TreeCommandBuilder setHelp(String help) {
        this.help = help;
        return this;
    }
    public TreeCommandBuilder setExample(String example) {
        this.example = example;
        return this;
    }
	
	public TreeCommandBuilder onNotFound(CommandAction action) {
		this.onNotFound = action;
        return this;
    }
	
	public TreeCommandBuilder onMissingPermission(CommandAction action) {
		this.onMissingPermission = action;
        return this;
    }
    private boolean check() {
        return help != null && name != null && !commands.isEmpty() && category != Category.UNKNOWN;
    }

    public ICommand build() {
        if (!check())
            Bot.LOG.warn("Found Command with Null or Empty properties.\nName: " + name + "\nHelp: " + help + "\nCategory: " + category);
        return new ICommand() {
            @Override
            public void execute(CommandEvent event, String args) {
                ICommand cmd = null;
                String[] splitArgs = StringUtils.splitSimple(args);
                boolean isDefault = false;
                if (splitArgs.length == 1) {
                    splitArgs = new String[] {splitArgs[0], "?"};
                }
                if (splitArgs.length > 1) {
                    String baseCmd = splitArgs[1];
                    if (baseCmd.matches("^(\\?|help)$")) {
                        event.sendMessage(CommandManager.getHelp(event.getCommand(), event.getMember(), event.getOriginGuild().getSelfMember())).queue();
                        return;
                    }
                    cmd = getSubCommands().stream().filter(c -> c.getAliases().contains(baseCmd))
                            .findFirst().orElse(null);
                }
                if (cmd == null) {
                    //if (splitArgs.length > 1) {
	                if (onNotFound == CommandAction.SHOW_ERROR) {
		                event.sendMessage(Quotes.FAIL, "`" + splitArgs[1] + "` is not a valid command for `" + event.getCommand().getName() + "`. Please use `" + event.getPrefix() + help + "` to get help.").queue();
		                return;
                        } else if (onNotFound == CommandAction.SHOW_HELP) {
                            event.sendMessage(CommandManager.getHelp(this, event.getMember(), event.getOriginGuild().getSelfMember())).queue();
                            return;
                        } else if (onNotFound == CommandAction.REDIRECT && defaultcmd != null) {
                            cmd = getSubCommands().stream().filter(c -> c.getAliases().contains(defaultcmd))
                                    .findFirst().orElse(null);
                            isDefault = true;
                        }
                    //}
                }
                if (cmd == null) {
                    event.sendMessage("Uh-oh... Well, I thought this wasn't a problem... Well, redirect the following message to my master:\n" +
                            "Found Null Command in " + event.getOriginGuild().getName() + " (" + event.getOriginGuild().getId() + ") by " + Util.getUser(event.getAuthor()) + " (" + event.getAuthor().getId() + ").\nInformation on the command:\n      - Name: " + event.getCommand().getName() + "\n      - Required Args: " + event.getCommand().getRequiredArgs() + "\n      - Action: " + onNotFound).queue();
                    return;
                }
                if (!cmd.isPrivateAvailable() && Util.isPrivate(event.getEvent())) {
	                event.getAuthor().openPrivateChannel().queue(chan -> chan.sendMessage(Quotes.FAIL + "This command is not available through PMs, " +
			                "use it in a Text Channel please.").queue());
	                return;
                }
	            GuildMember member = event.getGuild() != null ? event.getGuild().getMember(event.getAuthor()) : new FakeGuildMember(event.getAuthor(), null);
	            if (!member.hasPermission(cmd.getRequiredPermission(), event.getJDA())) {
	                if (onMissingPermission == CommandAction.SHOW_ERROR) {
		                event.sendMessage(Quotes.FAIL, "You don't have enough permissions to do this!\n" +
				                "Missing Permission(s): *" +
				                String.join(", ", Permissions
						                .toCollection(cmd.getRequiredPermission())) + "*").queue();
		                return;
                    } else if (onMissingPermission == CommandAction.REDIRECT) {
                        if (!isDefault && defaultcmd != null) {
                            ICommand cmd2 = getSubCommands().stream().filter(c -> c.getAliases().contains(defaultcmd))
                                    .findFirst().orElse(null);
                            isDefault = true;
                            if (cmd2 == null) {
	                            event.sendMessage(Quotes.FAIL, "You don't have enough permissions to do this!\n" +
			                            "Missing Permission(s): *" +
			                            String.join(", ", Permissions
					                            .toCollection(cmd.getRequiredPermission())) + "*").queue();
	                            return;
                            } else
                                cmd = cmd2;
                        }
                    }
                }

                event.createChild(cmd, isDefault);
            }

            @Override
            public List<String> getAliases() {
                return aliases;
            }

            @Override
            public String getName() {
                return name == null ? "No name provided." : name;
            }

            @Override
            public String getDescription() {
                return "Use " + help + " to get help!";
            }

            @Override
            public String getRequiredArgs() {
                return "<parameter> [args]";
            }

            @Override
            public Long getRequiredPermission() {
                return perm;
            }

            @Override
            public boolean isPrivateAvailable() {
                return privateAvailable;
            }
            
            @Override
            public List<ICommand> getSubCommands() {
                return commands;
            }
            @Override
            public String getExample() {
                return example == null ? "Sorry, no example provided." : example;
            }
            
            @Override
            public Category getCategory() {
                return category;
            }
        };
    }
}
