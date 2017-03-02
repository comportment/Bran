package br.com.brjdevs.steven.bran.cmds.fun;

import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.utils.MathUtils;

import java.util.regex.Pattern;

public class EightBallCommand {
	
	public static final String[] ANSWERS = {
			"It is certain",
			"It is decidedly so",
			"Without a doubt",
			"Yes, definitely",
			"You may rely on it",
			"As I see it, yes",
			"Most likely",
			"Outlook good",
			"Yes",
			"Signs point to yes",
			"Reply hazy try again",
			"Ask again later",
			"Better not tell you now",
			"Cannot predict now",
			"Concentrate and ask again",
			"Don't count on it",
			"My reply is no",
			"My sources say no",
			"Outlook not so good",
			"Very doubtful"
	};
	private static final Pattern QUESTION = Pattern.compile("^([^?].+?\\?+)$");

	@Command
	private static ICommand eightBall() {
		return new CommandBuilder(Category.FUN)
				.setAliases("8ball", "8")
				.setName("8Ball Command")
				.setDescription("Ask the Magic 8Ball a question!")
				.setExample("8ball Am I alive?")
				.setArgs(new Argument("question", String.class, true))
				.setAction((event) -> {
					if (!event.getArgument("question").isPresent()) {
						event.sendMessage("\uD83C\uDFB1 *`Ask the 8Ball a question!`*").queue();
						return;
					}
					String question = (String) event.getArgument("question").get();
					if (!QUESTION.matcher(question).matches()) {
						event.sendMessage("\uD83C\uDFB1 *`This doesn't sound like a question...`*").queue();
						return;
					}
					event.sendMessage("\uD83C\uDFB1 *`" + getRandomAnswer() + "`*").queue();
				})
				.build();
	}
	
	public static String getRandomAnswer() {
		return ANSWERS[MathUtils.random(ANSWERS.length)];
	}
}
