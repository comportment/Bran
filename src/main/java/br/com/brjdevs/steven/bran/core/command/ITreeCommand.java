package br.com.brjdevs.steven.bran.core.command;

import br.com.brjdevs.steven.bran.core.command.actions.CommandAction;

import java.util.List;

public interface ITreeCommand extends ICommand {
	
	List<ICommand> getSubCommands();
	
	CommandAction onMissingPermission();
	
	CommandAction onNotFound();
	
	String getHelp();
}
