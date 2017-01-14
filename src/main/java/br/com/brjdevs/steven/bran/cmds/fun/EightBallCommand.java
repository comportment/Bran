package br.com.brjdevs.steven.bran.cmds.fun;

import br.com.brjdevs.steven.bran.core.command.*;
import br.com.brjdevs.steven.bran.core.utils.EightBallUtils;

import java.util.regex.Pattern;

public class EightBallCommand {
	private static final Pattern QUESTION = Pattern.compile("^([^?].+?\\?+)$");
	
	@Command
	private static ICommand eightBall() {
		return new CommandBuilder(Category.FUN)
				.setAliases("8ball", "8")
				.setName("8Ball Command")
				.setDescription("Ask the Magic 8Ball a question!")
				.setExample("8ball Am I alive?")
				.setArgs(new Argument<>("question", String.class, true))
				.setAction((event) -> {
					if (!event.getArgument("question").isPresent()) {
						event.sendMessage("\uD83C\uDFB1 *`Ask the 8Ball a question!`*").queue();
						return;
					}
					String question = (String) event.getArgument("qustion").get();
					if (!QUESTION.matcher(question).matches()) {
						event.sendMessage("\uD83C\uDFB1 *`This doesn't sound like a question...`*").queue();
						return;
					}
					event.sendMessage("\uD83C\uDFB1 *`" + EightBallUtils.getRandomAnswer() + "`*").queue();
				})
				.build();
	}
}
