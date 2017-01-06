package br.com.brjdevs.bran.cmds.fun;

import br.com.brjdevs.bran.core.command.Category;
import br.com.brjdevs.bran.core.command.CommandBuilder;
import br.com.brjdevs.bran.core.command.CommandManager;
import br.com.brjdevs.bran.core.command.RegisterCommand;
import br.com.brjdevs.bran.core.utils.MathUtils;

@RegisterCommand
public class ChooseCommand {
	public ChooseCommand() {
		CommandManager.addCommand(new CommandBuilder(Category.FUN)
				.setAliases("choose", "decide")
				.setName("Choose Command")
				.setDescription("Chooses between options!")
				.setArgs("[options]")
				.setAction((event, rawArgs) -> {
					String rawOptions = event.getArgs(2)[1];
					String[] options;
					if (rawOptions.contains(" or "))
						options = rawOptions.split(" or ");
					else
						options = rawOptions.split(" ");
					if (options.length == 1) {
						event.sendMessage("You need to give me at least 2 options to choose between!").queue();
						return;
					}
					String choice = options[MathUtils.random(options.length)];
					event.sendMessage(getRandomQuote() + " " + choice).queue();
				})
				.build());
	}
	
	private static final String[] QUOTES = {
			"I'd stay with",
			"The best option is",
			"For sure",
			"I'd choose"
	};
	private static String getRandomQuote() {
		return QUOTES[MathUtils.random(QUOTES.length)];
	}
}
