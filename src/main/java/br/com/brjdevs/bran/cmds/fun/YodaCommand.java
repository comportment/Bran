package br.com.brjdevs.bran.cmds.fun;

import br.com.brjdevs.bran.core.command.Category;
import br.com.brjdevs.bran.core.command.CommandBuilder;
import br.com.brjdevs.bran.core.command.CommandManager;
import br.com.brjdevs.bran.core.command.RegisterCommand;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

@RegisterCommand
public class YodaCommand {
	public YodaCommand() {
		CommandManager.addCommand(new CommandBuilder(Category.FUN)
				.setAliases("yoda")
				.setName("Yoda Command")
				.setDescription("Turn your sentences into Yoda-speak!")
				.setArgs("[sentence]")
				.setAction((event) -> {
					HttpResponse<String> response = null;
					String string = event.getArgs(2)[1].replaceAll(" ", "+");
					if (string.isEmpty()) {
						event.sendMessage("Teach you to speak like me if you tell me a sentence I will.  Herh herh herh.").queue();
						return;
					}
					try {
						response = Unirest.get("https://yoda.p.mashape.com/yoda?sentence=" + string)
								.header("X-Mashape-Key", "vPanVptLZomshf3VqeeNUB2Y520sp1tGqMnjsnsqkyiLsLnSbo")
								.header("Accept", "text/plain")
								.asString();
					} catch (Exception e) {
						event.sendMessage("Could not Yodify this! Try again later!").queue();
						return;
					}
					event.sendMessage(response.getBody()).queue();
				})
				.build());
	}
}
