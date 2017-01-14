package br.com.brjdevs.steven.bran.core.command;

public class CommandUtils {
	
	public static ICommand getCommand(ITreeCommand tree, String alias) {
		return tree.getSubCommands().stream().filter(sub -> sub.getAliases().contains(alias)).findFirst().orElse(null);
	}
	
	public static ICommand getCommand(String alias) {
		return CommandManager.getCommands().stream().filter(cmd -> cmd.getAliases().contains(alias)).findFirst().orElse(null);
	}
	
	public static Argument[] copy(ICommand cmd) {
		if (cmd.getArguments() == null) return null;
		Argument[] copy = new Argument[cmd.getArguments().length];
		for (int i = 0; i < copy.length; i++) {
			copy[i] = cmd.getArguments()[i].copy();
		}
		return copy;
	}
}
