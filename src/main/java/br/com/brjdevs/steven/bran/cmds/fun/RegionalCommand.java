package br.com.brjdevs.steven.bran.cmds.fun;

import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RegionalCommand {
	private static final Pattern A_ZPattern = Pattern.compile("([A-Za-z])");
	
	private static final Map<String, String> regional = new HashMap<>();
	
	static {
		regional.put("+", ":heavy_plus_sign:");
		regional.put("-", ":heavy_minus_sign:");
		regional.put("$", ":heavy_dollar_sign:");
		regional.put("#", ":hash:");
		regional.put("*", ":asterisk:");
		regional.put(".", ":record_button:");
		regional.put("0", ":zero:");
		regional.put("1", ":one:");
		regional.put("2", ":two:");
		regional.put("3", ":three:");
		regional.put("4", ":four:");
		regional.put("5", ":five:");
		regional.put("6", ":six:");
		regional.put("7", ":seven:");
		regional.put("8", ":eight:");
		regional.put("9", ":nine:");
		regional.put(" ", "    ");
		regional.put("a", "");
		regional.put("b", "");
		regional.put("c", "");
		regional.put("d", "");
		regional.put("e", "");
		regional.put("f", "");
		regional.put("g", "");
		regional.put("h", "");
		regional.put("i", "");
		regional.put("j", "");
		regional.put("k", "");
		regional.put("l", "");
		regional.put("m", "");
		regional.put("n", "");
		regional.put("o", "");
		regional.put("p", "");
		regional.put("q", "");
		regional.put("r", "");
		regional.put("s", "");
		regional.put("t", "");
		regional.put("u", "");
		regional.put("v", "");
		regional.put("w", "");
		regional.put("x", "");
		regional.put("y", "");
		regional.put("z", "");
	}
	
	@Command
	private static ICommand regional() {
		return new CommandBuilder(Category.FUN)
				.setAliases("reg", "regional")
				.setName("Regional Character Command")
				.setArgs(new Argument("phrase", String.class))
				.setDescription("Makes a phrase cool.")
				.setExample("reg This phrase is cool")
				.setAction((event, rawArgs) -> {
					String phrase = ((String) event.getArgument("phrase").get()).toLowerCase();
					String[] characters = phrase.split("");
					String formatted = String.join("", Stream.of(characters).map(RegionalCommand::toRegional).collect(Collectors.toList()));
					event.sendMessage(formatted).queue();
				})
				.build();
	}
	
	private static String toRegional(String str) {
		return (A_ZPattern.matcher(str).find() ? ":regional_indicator_" + str + ":" : regional.getOrDefault(str, str)) + " ";
	}
}
