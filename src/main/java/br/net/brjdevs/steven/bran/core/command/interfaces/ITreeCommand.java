package br.net.brjdevs.steven.bran.core.command.interfaces;

import br.net.brjdevs.steven.bran.core.command.enums.CommandAction;

import java.util.List;

public interface ITreeCommand extends ICommand {
	
	List<ICommand> getSubCommands();
	
	CommandAction onMissingPermission();
	
	CommandAction onNotFound();
	
	String getHelp();
}
