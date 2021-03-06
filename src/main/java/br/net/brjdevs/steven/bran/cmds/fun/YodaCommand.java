package br.net.brjdevs.steven.bran.cmds.fun;

import br.net.brjdevs.steven.bran.core.client.Bran;
import br.net.brjdevs.steven.bran.core.command.Argument;
import br.net.brjdevs.steven.bran.core.command.Command;
import br.net.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.net.brjdevs.steven.bran.core.command.enums.Category;
import br.net.brjdevs.steven.bran.core.command.interfaces.ICommand;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import java.net.URLEncoder;

public class YodaCommand {
	
	@Command
	private static ICommand yoda() {
		return new CommandBuilder(Category.FUN)
				.setAliases("yoda")
				.setName("Yoda Command")
				.setDescription("Turn your sentences into Yoda-speak!")
				.setArgs(new Argument("sentence", String.class, true))
				.setAction((event) -> {
					HttpResponse<String> response;
					Argument argument = event.getArgument("sentence");
					if (!argument.isPresent()) {
						event.sendMessage("Teach you to speak like me if you tell me a sentence I will.  Herh herh herh.").queue();
						return;
					}
					try {
						String string = URLEncoder.encode((String) argument.get(), "UTF-8");
						response = Unirest.get("https://yoda.p.mashape.com/yoda?sentence=" + string)
								.header("X-Mashape-Key", Bran.getInstance().getConfig().mashapeKey)
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
