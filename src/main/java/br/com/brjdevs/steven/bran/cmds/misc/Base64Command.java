package br.com.brjdevs.steven.bran.cmds.misc;

import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.builders.TreeCommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;

import java.util.Base64;

public class Base64Command {
	
	@Command
	private static ICommand base64() {
		return new TreeCommandBuilder(Category.MISCELLANEOUS)
				.setName("Base64 Command")
				.setHelp("base64 ?")
				.setAliases("base64")
				.setDescription("Let's speak in Base64!")
				.addSubCommand(new CommandBuilder(Category.MISCELLANEOUS)
						.setAliases("encode")
						.setName("Base64 Encode Command")
						.setDescription("Encodes a String to Base64.")
						.setArgs(new Argument("string", String.class))
						.setAction((event) -> {
							String string = (String) event.getArgument("string").get();
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
				.addSubCommand(new CommandBuilder(Category.MISCELLANEOUS)
						.setAliases("decode")
						.setName("Base64 Decode Command")
						.setDescription("Decodes a Base64 to String!")
						.setArgs(new Argument("base64", String.class))
						.setAction((event) -> {
							String string = (String) event.getArgument("base64").get();
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
				.build();
	}
}
