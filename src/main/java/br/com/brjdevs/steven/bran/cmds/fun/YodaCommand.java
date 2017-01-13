package br.com.brjdevs.steven.bran.cmds.fun;

import br.com.brjdevs.steven.bran.Bot;
import br.com.brjdevs.steven.bran.core.command.Category;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.ICommand;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

public class YodaCommand {
	
	@Command
	public static ICommand yoda() {
		return new CommandBuilder(Category.FUN)
				.setAliases("yoda")
				.setName("Yoda Command")
				.setDescription("Turn your sentences into Yoda-speak!")
				.setArgs("[sentence]")
				.setAction((event) -> {
					HttpResponse<String> response = null;
					String string = event.getArgs(2)[1].replaceAll("\\s+", "+");
					if (string.isEmpty()) {
						event.sendMessage("Teach you to speak like me if you tell me a sentence I will.  Herh herh herh.").queue();
						return;
					}
					try {
						response = Unirest.get("https://yoda.p.mashape.com/yoda?sentence=" + string)
								.header("X-Mashape-Key", Bot.getInstance().getConfig().getMashapeKey())
								.header("Accept", "text/plain")
								.asString();
					} catch (Exception e) {
						event.sendMessage("Could not Yodify this! Try again later!").queue();
						return;
					}
					event.sendMessage(response.getBody()).queue();
				})
				.build();
	}
}
