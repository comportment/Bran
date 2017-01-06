package br.com.brjdevs.bran.cmds.fun;

import br.com.brjdevs.bran.core.command.Category;
import br.com.brjdevs.bran.core.command.CommandBuilder;
import br.com.brjdevs.bran.core.command.CommandManager;
import br.com.brjdevs.bran.core.command.RegisterCommand;
import br.com.brjdevs.bran.core.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RegisterCommand
public class RegionalCommand {
	private static final Pattern A_ZPattern = Pattern.compile("([A-Za-z])");
	public RegionalCommand () {
		register();
	}
	private static void register() {
		CommandManager.addCommand(new CommandBuilder(Category.FUN)
				.setAliases("reg", "regional")
				.setName("Regional Character Command")
				.setArgs("[phrase]")
				.setDescription("Makes a phrase cool.")
				.setExample("reg This phrase is cool")
				.setAction((event, rawArgs) -> {
					String toFormat = StringUtils.splitArgs(rawArgs, 2)[1].toLowerCase();
					if (toFormat.isEmpty()) {
						event.sendMessage("You didn't tell me a phrase! Use `" + event.getPrefix() + "regional [phrase]`").queue();
						return;
					}
					String[] characters = toFormat.split("");
					String formatted = String.join("", Stream.of(characters).map(RegionalCommand::toRegional).collect(Collectors.toList()));
					event.sendMessage(formatted).queue();
				})
				.build());
	}
	private static final Map<String, String> regional = new HashMap<String, String>() {{
		put("+", ":heavy_plus_sign:");
		put("-", ":heavy_minus_sign:");
		put("$", ":heavy_dollar_sign:");
		put("#", ":hash:");
		put("*", ":asterisk:");
		put(".", ":record_button:");
		put("0", ":zero:");
		put("1", ":one:");
		put("2", ":two:");
		put("3", ":three:");
		put("4", ":four:");
		put("5", ":five:");
		put("6", ":six:");
		put("7", ":seven:");
		put("8", ":eight:");
		put("9", ":nine:");
		put(" ", "    ");
		put("a", "");
		put("b", "");
		put("c", "");
		put("d", "");
		put("e", "");
		put("f", "");
		put("g", "");
		put("h", "");
		put("i", "");
		put("j", "");
		put("k", "");
		put("l", "");
		put("m", "");
		put("n", "");
		put("o", "");
		put("p", "");
		put("q", "");
		put("r", "");
		put("s", "");
		put("t", "");
		put("u", "");
		put("v", "");
		put("w", "");
		put("x", "");
		put("y", "");
		put("z", "");
	}};
	
	public static String toRegional(String str) {
		return (A_ZPattern.matcher(str).find() ? ":regional_indicator_" + str + ":" : regional.getOrDefault(str, str)) + " ";
	}
}
