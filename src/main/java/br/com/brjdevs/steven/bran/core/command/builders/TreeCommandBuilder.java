package br.com.brjdevs.steven.bran.core.command.builders;

import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.CommandEvent;
import br.com.brjdevs.steven.bran.core.command.CommandUtils;
import br.com.brjdevs.steven.bran.core.command.HelpContainer;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.enums.CommandAction;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.command.interfaces.ITreeCommand;
import br.com.brjdevs.steven.bran.core.managers.Permissions;
import net.dv8tion.jda.core.Permission;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TreeCommandBuilder {
	
	private List<ICommand> subCommands = new ArrayList<>();
	private List<String> aliases = new ArrayList<>();
	private String name = null;
	private String desc = null;
	private boolean isPrivate = true;
	private String example = null;
	private Long perm = Permissions.RUN_BASECMD;
	private Category category;
	private String defaultCmd;
	private String help;
	private CommandAction onNotFound = CommandAction.SHOW_ERROR;
	private CommandAction onMissingPermission = CommandAction.SHOW_ERROR;
	
	public TreeCommandBuilder(Category category) {
		this.category = category;
	}
	
	public TreeCommandBuilder setAliases(String... aliases) {
		Stream.of(aliases).forEach(this.aliases::add);
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
	
	public TreeCommandBuilder setExample(String example) {
		this.example = example;
		return this;
	}
	
	public TreeCommandBuilder addSubCommand(ICommand subCommand) {
		this.subCommands.add(subCommand);
		HelpContainer.generateHelp(subCommand);
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
	
	public TreeCommandBuilder setHelp(String help) {
		this.help = help;
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
			public String getHelp() {
				return help;
			}
			
			@Override
			public void execute(CommandEvent event) {
				if (event.isPrivate() && !isPrivateAvailable()) {
					event.sendMessage("This Command is not available in PMs, please use it in a Guild Text Channel.").queue();
					return;
				}
				if (event.getArgs(3)[1].matches("^(\\?|help)$")) {
					event.sendMessage(HelpContainer.getHelp(this, event.getMember())).queue();
					return;
				}
				String alias = event.getArgs(3)[1].trim();
				boolean isDefault = false;
				if (alias.isEmpty()) {
					if (defaultCmd != null) {
						alias = defaultCmd;
						isDefault = true;
					} else {
						if (event.getGuild() != null && !event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_EMBED_LINKS)) {
							event.sendMessage("I can't send you help without the MESSAGE_EMBED_LINKS permission!").queue();
						} else {
							event.sendMessage(HelpContainer.getHelp(event.getCommand(), event.getSelfMember())).queue();
						}
						return;
					}
				}
				ICommand subCommand = CommandUtils.getCommand(this, alias);
				if (subCommand == null) {
					switch (onNotFound) {
						case SHOW_ERROR:
							event.sendMessage("No such SubCommand `" + alias + "` in " + getName() + ".").queue();
							break;
						case REDIRECT:
							event.createChild(CommandUtils.getCommand(this, defaultCmd), true);
							break;
						case SHOW_HELP:
							if (!event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_EMBED_LINKS)) {
								event.sendMessage("I can't send you help without the MESSAGE_EMBED_LINKS permission!").queue();
							} else {
								event.sendMessage(HelpContainer.getHelp(event.getCommand(), event.getSelfMember())).queue();
							}
							break;
					}
					return;
				} else if (event.isPrivate() ? !event.getUserData().hasPermission(getRequiredPermission()) : !event.getGuildData().hasPermission(event.getAuthor(), getRequiredPermission())) {
					switch (onMissingPermission) {
						case SHOW_ERROR:
							event.sendMessage("You don't have enough permissions to execute this Command!\n*Missing Permission(s): " + String.join(", ", Permissions.toCollection(subCommand.getRequiredPermission())) + "*").queue();
							break;
						case REDIRECT:
							event.createChild(CommandUtils.getCommand(this, defaultCmd), true);
							break;
						case SHOW_HELP:
							if (!event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_EMBED_LINKS)) {
								event.sendMessage("I can't send you help without the MESSAGE_EMBED_LINKS permission!").queue();
							} else {
								event.sendMessage(HelpContainer.getHelp(event.getCommand(), event.getSelfMember())).queue();
							}
							break;
					}
					return;
				}
				event.createChild(subCommand, isDefault);
			}
			
			@Override
			public List<String> getAliases() {
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
				return example;
			}
			
			@Override
			public Category getCategory() {
				return category;
			}
		};
	}
}
