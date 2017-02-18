package br.com.brjdevs.steven.bran.core.managers;

import br.com.brjdevs.steven.bran.core.utils.MathUtils;

public class EightBallManager {
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
	
	public static String getRandomAnswer() {
		return ANSWERS[MathUtils.random(ANSWERS.length)];
	}
}