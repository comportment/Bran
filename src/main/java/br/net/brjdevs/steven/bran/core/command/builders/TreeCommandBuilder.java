package br.net.brjdevs.steven.bran.core.command.builders;

import br.net.brjdevs.steven.bran.core.client.Bran;
import br.net.brjdevs.steven.bran.core.command.Argument;
import br.net.brjdevs.steven.bran.core.command.CommandEvent;
import br.net.brjdevs.steven.bran.core.command.enums.Category;
import br.net.brjdevs.steven.bran.core.command.enums.CommandAction;
import br.net.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.net.brjdevs.steven.bran.core.command.interfaces.ITreeCommand;
import br.net.brjdevs.steven.bran.core.managers.Permissions;
import net.dv8tion.jda.core.Permission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TreeCommandBuilder {
    
    private List<ICommand> subCommands = new ArrayList<>();
	private String[] aliases;
	private String name = null;
	private String desc = null;
	private boolean isPrivate = true;
	private Long perm = Permissions.RUN_BASECMD;
	private Category category;
	private String defaultCmd;
	private CommandAction onNotFound = CommandAction.SHOW_ERROR;
	private CommandAction onMissingPermission = CommandAction.SHOW_ERROR;
	
	public TreeCommandBuilder(Category category) {
		this.category = category;
	}
	
	public TreeCommandBuilder setAliases(String... aliases) {
		this.aliases = aliases;
		return this;
	}
	
	public TreeCommandBuilder setName(String name) {
		this.name = name;
		return this;
	}
	
	public TreeCommandBuilder setDescription(String desc) {
		this.desc = desc;
		return this;
	}
	
	public TreeCommandBuilder setPrivateAvailable(boolean isPrivate) {
		this.isPrivate = isPrivate;
		return this;
	}
	
	public TreeCommandBuilder setRequiredPermission(Long perm) {
		this.perm = perm;
		return this;
	}
	
	public TreeCommandBuilder addSubCommand(ICommand subCommand) {
		this.subCommands.add(subCommand);
        return this;
	}
	
	public TreeCommandBuilder setDefault(String alias) {
		this.defaultCmd = alias;
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
	
	public ITreeCommand build() {
		return new ITreeCommand() {
			
			@Override
			public List<ICommand> getSubCommands() {
				return subCommands;
			}
			
			@Override
			public CommandAction onMissingPermission() {
				return onMissingPermission;
			}
			
			@Override
			public CommandAction onNotFound() {
				return onNotFound;
			}
            @Override
            public String getDefaultCommand() {
                return defaultCmd;
            }

            @Override
			public String getHelpMessage() {
				String desc = "";
				desc += getCategory().getEmoji() + " **| " + getCategory().getKey() + "**\n**Command:** " + getName() + "\n";
				desc += "**Description:** " + getDescription() + "\n";
				desc += "**Required Permission(s):** " + String.join(", ", Permissions.toCollection(getRequiredPermission())) + "\n";
				desc += "**Parameters**:\n";
				Set<Category> categories = getSubCommands().stream().map(ICommand::getCategory).collect(Collectors.toSet());
				for (Category category : categories) {
					List<ICommand> commands = getSubCommands().stream().filter(cmd -> cmd.getCategory() == category).collect(Collectors.toList());
					if (commands.isEmpty()) continue;
					desc += category.getEmoji() + " **| " + category.getKey() + "**\n";
					for (ICommand cmd : commands)
						desc += "          **" + cmd.getAliases()[0] + "** " + (cmd.getArguments() != null ? (String.join(" ", Arrays.stream(cmd.getArguments()).map(arg -> (arg.isOptional() ? "<" : "[") + arg.getType().getSimpleName() + ": " + arg.getName() + (arg.isOptional() ? ">" : "]")).toArray(String[]::new))) : "") + " - " + (cmd instanceof ITreeCommand ? "Use the help command to get help!" : cmd.getDescription()) + "\n";
					desc += '\n';
				}
				return desc;
			}
            @Override
            public Function<String, String[]> getArgumentParser() {
                return null;
            }

            @Override
			public void execute(CommandEvent event) {
                Bran.getInstance().getCommandManager().execute(event);
            }
            
            @Override
			public String[] getAliases() {
				return aliases;
			}
			
			@Override
			public String getName() {
				return name;
			}
			
			@Override
			public String getDescription() {
				return desc;
			}
			
			@Override
			public Argument[] getArguments() {
				return new Argument[0];
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
			public String getExample() {
				return null;
			}
			
			@Override
			public Category getCategory() {
				return category;
			}
		};
	}
}
