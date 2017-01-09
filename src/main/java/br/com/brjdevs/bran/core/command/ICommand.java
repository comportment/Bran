package br.com.brjdevs.bran.core.command;

import java.util.List;

public interface ICommand {
	
	void execute(CommandEvent event, String args);
	
	List<String> getAliases();
	
	String getName();
	
	String getDescription();
	
	String getRequiredArgs();
	
	Long getRequiredPermission();
	
	boolean isPrivateAvailable();
	
	List<ICommand> getSubCommands();
	
	String getExample();
	
	Category getCategory();
}
