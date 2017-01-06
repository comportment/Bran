package br.com.brjdevs.bran.cmds.misc;

import br.com.brjdevs.bran.core.RegisterCommand;
import br.com.brjdevs.bran.core.command.Category;
import br.com.brjdevs.bran.core.command.CommandBuilder;
import br.com.brjdevs.bran.core.command.CommandManager;
import br.com.brjdevs.bran.core.command.TreeCommandBuilder;

import java.util.Base64;

@RegisterCommand
public class Base64Command {
	public Base64Command() {
		CommandManager.addCommand(new TreeCommandBuilder(Category.MISCELLANEOUS)
				.setName("Base64 Command")
				.setHelp("base64 ?")
				.setAliases("base64")
				.addCommand(new CommandBuilder(Category.MISCELLANEOUS)
						.setAliases("encode")
						.setName("Base64 Encode Command")
						.setDescription("Encodes a String to Base64.")
						.setArgs("[string]")
						.setAction((event) -> {
							String string = event.getArgs(2)[1];
							String out;
							try {
								out = new String(Base64.getEncoder().encode(string.getBytes()));
							} catch (Exception e) {
								event.sendMessage(e.getMessage()).queue();
								return;
							}
							if (out.length() > 1900) {
								event.sendMessage("The output is too big, please ask for a smaller string.").queue();
								return;
							}
							event.sendMessage("**To Base64:**\n" + out).queue();
						})
						.build())
				.addCommand(new CommandBuilder(Category.MISCELLANEOUS)
						.setAliases("decode")
						.setName("Base64 Decode Command")
						.setDescription("Decodes a Base64 to String!")
						.setArgs("[string]")
						.setAction((event) -> {
							String string = event.getArgs(2)[1];
							String out;
							try {
								out = new String(Base64.getDecoder().decode(string.getBytes()));
							} catch (Exception e) {
								event.sendMessage(e.getMessage()).queue();
								return;
							}
							if (out.length() > 1900) {
								event.sendMessage("The output is too big, please ask for a smaller base64.").queue();
								return;
							}
							event.sendMessage("**From Base64:**\n" + out).queue();
						})
						.build())
				.build());
	}
}
