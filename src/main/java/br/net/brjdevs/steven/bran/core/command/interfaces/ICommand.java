package br.net.brjdevs.steven.bran.core.command.interfaces;

import br.net.brjdevs.steven.bran.core.command.Argument;
import br.net.brjdevs.steven.bran.core.command.CommandEvent;
import br.net.brjdevs.steven.bran.core.command.enums.Category;

import java.util.function.Function;

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
	
	String getHelpMessage();

	Function<String, String[]> getArgumentParser();
    
    default String getKey() {
        return "[" + getCategory() + "]=" + getName() + "." + getAliases()[0];
    }
}
