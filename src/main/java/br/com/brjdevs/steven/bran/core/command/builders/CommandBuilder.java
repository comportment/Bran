package br.com.brjdevs.steven.bran.core.command.builders;

import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.ArgumentParsingException;
import br.com.brjdevs.steven.bran.core.command.CommandEvent;
import br.com.brjdevs.steven.bran.core.command.HelpContainer;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.managers.Permissions;
import br.com.brjdevs.steven.bran.core.utils.Utils;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
	private String help;
	
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
	
	public CommandBuilder setRequiredPermission(Long perm) {
		this.perm = perm;
		return this;
	}
	
	public CommandBuilder setExample(String example) {
		this.example = example;
		return this;
	}
	
	public CommandBuilder setHelp(String help) {
		this.help = help;
		return this;
	}
	
	public ICommand build() {
		return new ICommand() {
			
			@Override
			public void execute(CommandEvent event) {
				if (event.isPrivate() && !isPrivateAvailable()) {
					event.sendMessage("This Command is not available in PMs, please use it in a Guild Text Channel.").queue();
					return;
				} else if (event.isPrivate() ? !event.getUserData().hasPermission(getRequiredPermission()) : !event.getGuildData().hasPermission(event.getAuthor(), getRequiredPermission())) {
					event.sendMessage("You don't have enough permissions to execute this Command!\n*Missing Permission(s): " + String.join(", ", Permissions.toCollection(getRequiredPermission())) + "*").queue();
					return;
				}
				String[] split = event.getArgs(2);
				if (split[1].matches("^(\\?|help)$")) {
					event.sendMessage(HelpContainer.getHelp(this, event.getMember())).queue();
					return;
				}
				split[1] = split[1].trim();
				if (split.length > 1 && !split[1].isEmpty() && split[1].charAt(0) == '\\' && split[1].matches("^(\\?|help)$"))
					split[1] = split[1].substring(1);
				String[] s = args.length > 1 ? Arrays.stream(Argument.split(split[1], args.length - 1)).filter(a -> !Utils.isEmpty(a)).toArray(String[]::new) : new String[] {split[1]};
				Argument[] args = event.getArguments();
				if (args != null) {
					for (int i = 0; i < args.length; i++) {
						try {
							if (s[i].trim().isEmpty() && !args[i].isOptional()) {
								throw new ArgumentParsingException("Invalid or no were given.");
							}
							args[i].parse(s[i].trim());
							if (!args[i].isPresent() && !args[i].isOptional())
								throw new ArgumentParsingException("Invalid or no were given.");
						} catch (ArgumentParsingException | ArrayIndexOutOfBoundsException ex) {
							if (!args[i].isOptional()) {
								event.sendMessage("**Bad Arguments:** " + ex.getMessage() + ".\nExpected arguments: " + (String.join(" ", Arrays.stream(getArguments()).map(arg -> (arg.isOptional() ? "<" : "[") + arg.getType().getSimpleName() + ": " + arg.getName() + (arg.isOptional() ? ">" : "]")).toArray(String[]::new)))).queue();
								return;
							}
						}
					}
				}
				action.accept(event, args);
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
			public String getHelp() {
				return help;
			}
		};
	}
}
