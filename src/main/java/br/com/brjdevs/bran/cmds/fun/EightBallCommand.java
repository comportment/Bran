package br.com.brjdevs.bran.cmds.fun;

import br.com.brjdevs.bran.core.command.Category;
import br.com.brjdevs.bran.core.command.CommandBuilder;
import br.com.brjdevs.bran.core.command.CommandManager;
import br.com.brjdevs.bran.core.command.RegisterCommand;
import br.com.brjdevs.bran.core.utils.EightBallUtils;

import java.util.regex.Pattern;

@RegisterCommand
public class EightBallCommand {
	private static final Pattern QUESTION = Pattern.compile("^([^?].+?\\?+)$");
	public EightBallCommand() {
		CommandManager.addCommand(new CommandBuilder(Category.FUN)
				.setAliases("8ball", "8")
				.setName("8Ball Command")
				.setDescription("Ask the Magic 8Ball a question!")
				.setExample("8ball Am I alive?")
				.setArgs("[question]")
				.setAction((event) -> {
					String question = event.getArgs(2)[1];
					if (question.isEmpty()) {
						event.sendMessage("\uD83C\uDFB1 *`Ask the 8Ball a question!`*").queue();
						return;
					}
					if (!QUESTION.matcher(question).matches()) {
						event.sendMessage("\uD83C\uDFB1 *`This doesn't sound like a question...`*").queue();
						return;
					}
					event.sendMessage("\uD83C\uDFB1 *`" + EightBallUtils.getRandomAnswer() + "`*").queue();
				})
				.build());
	}
}
