package br.com.brjdevs.steven.bran.cmds.fun;

import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import com.fathzer.soft.javaluator.DoubleEvaluator;

public class MathCommand {
	private static final DoubleEvaluator calculator = new DoubleEvaluator();
	
	@Command
	private static ICommand math() {
		return new CommandBuilder(Category.FUN)
				.setAliases("math", "calc")
				.setName("Math Command")
				.setDescription("Resolves a Mathematical Expression!")
				.setArgs(new Argument<>("expression", String.class))
				.setAction((event) -> {
					String expression = ((String) event.getArgument("expression").get()).toLowerCase();
					double result;
					try {
						result = calculator.evaluate(expression);
					} catch (Exception e) {
						event.sendMessage("Could not resolve `" + expression + "`").queue();
						return;
					}
					event.sendMessage("\uD83D\uDD22 Operation result: `" + String.valueOf(result).replaceAll("[.]?0+$", "") + "`").queue();
				})
				.build();
	}
}
