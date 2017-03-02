package br.com.brjdevs.steven.bran.cmds.fun;

import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.quote.Quotes;
import br.com.brjdevs.steven.bran.core.utils.StringUtils;
import net.dv8tion.jda.core.entities.User;
import org.json.JSONObject;

public class LoveCommand {
	@Command
	private static ICommand love() {
		return new CommandBuilder(Category.FUN)
				.setName("Love Command")
				.setAliases("love")
				.setDescription("Gives you the love percentage between to names!")
				.setArgs(new Argument("firstName", String.class), new Argument("secondName", String.class))
				.setAction((event) -> {
					String firstName = ((String) event.getArgument("firstName").get());
					String secondName = ((String) event.getArgument("secondName").get());
					if (firstName.equals(secondName)) {
						event.sendMessage("So lonely...").queue();
						return;
					}
					for (User user : event.getMessage().getMentionedUsers()) {
						firstName = firstName.replaceAll("<@!?" + user.getId() + ">", user.getName());
						secondName = secondName.replaceAll("<@!?" + user.getId() + ">", user.getName());
					}
					if (firstName.isEmpty() || secondName.isEmpty()) {
						event.sendMessage("You have to provide me at least two names!").queue();
						return;
					}
					JSONObject response;
					try {
						response = calculate(firstName, secondName);
					} catch (Exception e) {
						event.sendMessage(Quotes.FAIL, "Could not calculate love, try someone else!").queue();
						return;
					}
					String percentage = response.getString("percentage");
					String result = response.getString("result");
					event.sendMessage("**LOVE CALCULATOR**\n\uD83D\uDC97 *`" + firstName + "`*\n\uD83D\uDC97 *`" + secondName + "`*\n**" + percentage + "%** `" + StringUtils.getProgressBar(Integer.parseInt(percentage), 15) + "` " + result).queue();
				})
				.build();
	}
	
	private static JSONObject calculate(String a, String b) {
		JSONObject v = new JSONObject();
		int percentage = (a.codePoints().sum() + b.codePoints().sum()) % 101;
		String r;
		v.put("percentage", String.valueOf(percentage));
		if (percentage <= 20)
			r = "Better luck next time.";
		else if (percentage <= 40)
			r = "Not so bad.";
		else if (percentage == 69)
			r = "( ͡° ͜ʖ ͡°)";
		else if (percentage <= 60)
			r = "Pretty great!";
		else if (percentage <= 80)
			r = "A lovely ship!";
		else
			r = "Perfect! \u2764";
		v.put("result", r);
		return v;
	}
}
