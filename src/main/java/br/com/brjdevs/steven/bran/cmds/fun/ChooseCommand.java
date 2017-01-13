package br.com.brjdevs.steven.bran.cmds.fun;

import br.com.brjdevs.steven.bran.core.command.Category;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.ICommand;
import br.com.brjdevs.steven.bran.core.utils.MathUtils;

public class ChooseCommand {
	
	private static final String[] QUOTES = {
			"I'd stay with",
			"The best option is",
			"For sure",
			"I'd choose"
	};
	
	@Command
	public static ICommand choose() {
		return new CommandBuilder(Category.FUN)
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
						options = rawOptions.split("\\s+");
					if (options.length == 1) {
						event.sendMessage("You need to give me at least 2 options to choose between!").queue();
						return;
					}
					String choice = options[MathUtils.random(options.length)];
					event.sendMessage(getRandomQuote() + " " + choice).queue();
				})
				.build();
	}
	
	private static String getRandomQuote() {
		return QUOTES[MathUtils.random(QUOTES.length)];
	}
}
