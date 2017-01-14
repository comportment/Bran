package br.com.brjdevs.steven.bran.cmds.fun;

import br.com.brjdevs.steven.bran.Bot;
import br.com.brjdevs.steven.bran.core.command.*;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

public class YodaCommand {
	
	@Command
	private static ICommand yoda() {
		return new CommandBuilder(Category.FUN)
				.setAliases("yoda")
				.setName("Yoda Command")
				.setDescription("Turn your sentences into Yoda-speak!")
				.setArgs(new Argument<>("sentence", String.class, true))
				.setAction((event) -> {
					HttpResponse<String> response = null;
					Argument argument = event.getArgument("sentence");
					if (!argument.isPresent()) {
						event.sendMessage("Teach you to speak like me if you tell me a sentence I will.  Herh herh herh.").queue();
						return;
					}
					String string = (String) argument.get();
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
