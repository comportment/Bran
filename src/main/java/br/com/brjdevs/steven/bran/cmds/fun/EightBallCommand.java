package br.com.brjdevs.steven.bran.cmds.fun;

import br.com.brjdevs.steven.bran.core.command.Category;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.ICommand;
import br.com.brjdevs.steven.bran.core.utils.EightBallUtils;

import java.util.regex.Pattern;

public class EightBallCommand {
	private static final Pattern QUESTION = Pattern.compile("^([^?].+?\\?+)$");
	
	@Command
	public static ICommand eightBall() {
		return new CommandBuilder(Category.FUN)
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
				.build();
	}
}
