package br.com.brjdevs.steven.bran.cmds.fun;

import br.com.brjdevs.steven.bran.Bot;
import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.quote.Quotes;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
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
						HttpResponse<JsonNode> response = null;
						String firstName = URLEncoder.encode((String) event.getArgument("firstName").get(), "UTF-8");
						String secondName = URLEncoder.encode((String) event.getArgument("secondName").get(), "UTF-8");
						if (firstName.isEmpty() || secondName.isEmpty()) {
							event.sendMessage("You have to provide me at least two names!").queue();
							return;
						}
						try {
							response = Unirest.get("https://love-calculator.p.mashape.com/getPercentage?fname=" + firstName + "&sname=" + secondName)
									.header("X-Mashape-Key", Bot.getConfig().getMashapeKey())
									.header("Accept", "application/json")
									.asJson();
						} catch (Exception e) {
							event.sendMessage(Quotes.FAIL, "Could not calculate love, try someone else!").queue();
							return;
						}
						JSONObject object = new JSONObject(response.getBody().toString());
						String percentage = object.getString("percentage");
						String result = object.getString("result");
						event.sendMessage("**" + percentage + "%** of love between " + firstName + " and " + secondName + "!\n" + result).queue();
						
					} catch (UnsupportedEncodingException e) {
						event.sendMessage("Failed to encode URL.").queue();
					}
				})
				.build();
	}
}
