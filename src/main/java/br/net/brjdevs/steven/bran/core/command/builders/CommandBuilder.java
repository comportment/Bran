package br.net.brjdevs.steven.bran.core.command.builders;

import br.net.brjdevs.steven.bran.core.client.Bran;
import br.net.brjdevs.steven.bran.core.command.Argument;
import br.net.brjdevs.steven.bran.core.command.ArgumentParsingException;
import br.net.brjdevs.steven.bran.core.command.CommandEvent;
import br.net.brjdevs.steven.bran.core.command.enums.Category;
import br.net.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.net.brjdevs.steven.bran.core.command.interfaces.ITreeCommand;
import br.net.brjdevs.steven.bran.core.managers.Permissions;
import br.net.brjdevs.steven.bran.core.utils.Utils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CommandBuilder {
	
	protected BiConsumer<CommandEvent, Argument[]> action = null;
	protected String[] aliases;
	protected String name = null;
	protected String desc = null;
	protected Argument[] args = {};
	protected boolean isPrivate = true;
	protected String example = null;
	protected Long perm = Permissions.RUN_BASECMD;
	protected Category category;
    private Function<String, String[]> parser = (raw) -> args.length > 1 ? Arrays.stream(Argument.split(raw, args.length - 1)).filter(a -> !Utils.isEmpty(a)).toArray(String[]::new) : new String[] {raw};
    
    public CommandBuilder(Category category) {
		this.category = category;
	}
	
	public CommandBuilder setAction(Consumer<CommandEvent> action) {
		this.action = (e, a) -> action.accept(e);
		return this;
	}
	
	public CommandBuilder setAction(BiConsumer<CommandEvent, Argument[]> action) {
		this.action = action;
		return this;
	}
	
	public CommandBuilder setAliases(String... aliases) {
		this.aliases = aliases;
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
	
	public CommandBuilder setArgs(Argument... args) {
		this.args = args;
		return this;
	}
	
	public CommandBuilder setPrivateAvailable(boolean isPrivate) {
		this.isPrivate = isPrivate;
		return this;
	}
    
    public CommandBuilder setArgumentParser(Function<String, String[]> parser) {
        this.parser = parser;
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
	
	public ICommand build() {
		return new ICommand() {
			
			@Override
			public void execute(CommandEvent event) {
				action.accept(event, event.getArguments());
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
				return args;
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
				return example;
			}
			
			@Override
			public Category getCategory() {
				return category;
			}
			
			@Override
			public String getHelpMessage() {
				String desc = "";
				desc += getCategory().getEmoji() + " **| " + getCategory().getKey() + "**\n**Command:** " + getName() + "\n";
				desc += "**Description:** " + getDescription() + "\n";
				if (getArguments() != null) {
					desc += "**Arguments:** " + (getArguments().length != 0 ? (String.join(" ", Arrays.stream(getArguments()).map(arg -> (arg.isOptional() ? "<" : "[") + arg.getType().getSimpleName() + ": " + arg.getName() + (arg.isOptional() ? ">" : "]")).toArray(String[]::new))) : "No arguments required.") + '\n';
					desc += "            *Please note: do **NOT** include <> or []*\n";
				}
				desc += "**Required Permission(s):** " + String.join(", ", Permissions.toCollection(getRequiredPermission())) + "\n";
				if (getExample() != null)
					desc += "**Example:** " + getExample();
				return desc;
			}
			@Override
			public Function<String, String[]> getArgumentParser() {
				return parser;
			}
		};
	}
}
