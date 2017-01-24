package br.com.brjdevs.steven.bran.cmds.fun;

import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.quote.Quotes;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class LoveCommand {
	
	@Command
	private static ICommand love() {
		return new CommandBuilder(Category.FUN)
				.setName("Love Command")
				.setAliases("love")
				.setDescription("Gives you the love percentage between to names!")
				.setArgs(new Argument<>("firstName", String.class), new Argument<>("secondName", String.class))
				.setAction((event) -> {
					try {
						String firstName = URLEncoder.encode((String) event.getArgument("firstName").get(), "UTF-8");
						String secondName = URLEncoder.encode((String) event.getArgument("secondName").get(), "UTF-8");
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
						event.sendMessage("**" + percentage + "%** of love between " + firstName + " and " + secondName + "!\n" + result).queue();
						
					} catch (UnsupportedEncodingException e) {
						event.sendMessage("Failed to encode URL.").queue();
					}
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
			r = "You can choose someone better.";
		else if (percentage <= 60)
			r = "A lovely ship.";
		else if (percentage <= 80)
			r = "Love was handmade for each other.";
		else
			r = "A lovely ship!";
		v.put("result", r);
		return v;
	}
}
