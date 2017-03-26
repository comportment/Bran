package br.net.brjdevs.steven.bran.core.command;

import br.net.brjdevs.steven.bran.core.command.interfaces.ICommand;

public class CommandUtils {
	
	public static Argument[] copy(ICommand cmd) {
		Argument[] copy = new Argument[cmd.getArguments().length];
		for (int i = 0; i < copy.length; i++) {
			copy[i] = cmd.getArguments()[i].copy();
		}
		return copy;
	}
}
