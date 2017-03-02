package br.com.brjdevs.steven.bran.core.command.interfaces;

import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.CommandEvent;
import br.com.brjdevs.steven.bran.core.command.enums.Category;

public interface ICommand {
	
	void execute(CommandEvent event);
	
	String[] getAliases();
	
	String getName();
	
	String getDescription();
	
	Argument[] getArguments();
	
	Long getRequiredPermission();
	
	boolean isPrivateAvailable();
	
	String getExample();
	
	Category getCategory();
	
	String getHelp();
}
