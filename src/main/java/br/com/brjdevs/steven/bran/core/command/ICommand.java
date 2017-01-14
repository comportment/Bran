package br.com.brjdevs.steven.bran.core.command;

import java.util.List;

public interface ICommand {
	
	void execute(CommandEvent event);
	
	List<String> getAliases();
	
	String getName();
	
	String getDescription();
	
	Argument[] getArguments();
	
	Long getRequiredPermission();
	
	boolean isPrivateAvailable();
	
	String getExample();
	
	Category getCategory();
	
	String getHelp();
}
