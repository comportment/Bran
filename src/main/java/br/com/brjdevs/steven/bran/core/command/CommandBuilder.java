package br.com.brjdevs.steven.bran.core.command;

import br.com.brjdevs.steven.bran.Bot;
import br.com.brjdevs.steven.bran.core.managers.Permissions;
import br.com.brjdevs.steven.bran.core.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class CommandBuilder {
    private BiConsumer<CommandEvent, String> action = null;
    private List<String> aliases = new ArrayList<>();
    private String name = null;
    private String desc = null;
    private String requiredArgs = null;
    private boolean isPrivate = true;
    private String example = null;
    private Long perm = Permissions.RUN_BASECMD;
    private Category category;
    
    public CommandBuilder(Category category) {
        this.category = category;
    }
    
    public CommandBuilder setAction(Consumer<CommandEvent> action) {
        this.action = (e, a) -> action.accept(e);
        return this;
    }
    public CommandBuilder setAction(BiConsumer<CommandEvent, String> action) {
        this.action = action;
        return this;
    }
    public CommandBuilder setAliases(String... aliases) {
        Stream.of(aliases).forEach(this.aliases::add);
        return this;
    }
    public CommandBuilder setName(String name) {
        this.name = name;
        return this;
    }
    public CommandBuilder setDescription(String desc) {
        this.desc = desc;
        return this;
    }
    public CommandBuilder setArgs(String requiredArgs) {
        this.requiredArgs = requiredArgs;
        return this;
    }
    public CommandBuilder setPrivateAvailable(boolean isPrivate) {
        this.isPrivate = isPrivate;
        return this;
    }
    public CommandBuilder setRequiredPermission(Long perm) {
        this.perm = perm;
        return this;
    }
    public CommandBuilder setExample(String example) {
        this.example = example;
        return this;
    }
    protected boolean check() {
        return name != null && desc != null;
    }
    public ICommand build() {
        if (!check())
            Bot.LOG.warn("Found Command with Null or Empty properties.\nName: " + name + "\nDescriptipn: " + desc + "\nRequired Args: " + requiredArgs + "\nCategory: " + category);
        return new ICommand() {
            @Override
            public void execute(CommandEvent event, String args) {
                String[] s = StringUtils.splitSimple(args);
                if (s.length > 1 && s[1].matches("^(\\?|help)$")) {
                    event.sendMessage(CommandManager.getHelp(event.getCommand(), event.getMember(), event.getOriginMember())).queue();
                    return;
                }
                //DiscordLog.log(event);
                action.accept(event, args);
            }

            @Override
            public List<String> getAliases() {
                return aliases;
            }

            @Override
            public String getName() {
                return name != null ? name : "No name provided.";
            }

            @Override
            public String getDescription() {
                return desc != null ? desc : "No description provided.";
            }

            @Override
            public String getRequiredArgs() {
                return requiredArgs;
            }

            @Override
            public Long getRequiredPermission() {
                return perm;
            }

            @Override
            public boolean isPrivateAvailable() {
                return isPrivate;
            }
            
            @Override
            public List<ICommand> getSubCommands() {
                return null;
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
