package br.com.brjdevs.steven.bran.core.command;

import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.command.interfaces.ITreeCommand;

public class CommandUtils {
	
	public static ICommand getCommand(ITreeCommand tree, String alias) {
		return tree.getSubCommands().stream().filter(sub -> sub.getAliases().contains(alias)).findFirst().orElse(null);
	}
	
	public static ICommand getCommand(CommandManager manager, String alias) {
		return manager.getCommands().stream().filter(cmd -> cmd.getAliases().contains(alias)).findFirst().orElse(null);
	}
	
	public static Argument[] copy(ICommand cmd) {
		Argument[] copy = new Argument[cmd.getArguments().length];
		for (int i = 0; i < copy.length; i++) {
			copy[i] = cmd.getArguments()[i].copy();
		}
		return copy;
	}
}
