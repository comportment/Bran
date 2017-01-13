package br.com.brjdevs.steven.bran.cmds.fun;

import br.com.brjdevs.steven.bran.core.command.Category;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.ICommand;
import com.fathzer.soft.javaluator.DoubleEvaluator;

public class MathCommand {
	private static final DoubleEvaluator calculator = new DoubleEvaluator();
	
	@Command
	public static ICommand math() {
		return new CommandBuilder(Category.FUN)
				.setAliases("math", "calc")
				.setName("Math Command")
				.setDescription("Resolves a Mathematical Expression!")
				.setArgs("[expression]")
				.setAction((event) -> {
					String expression = event.getArgs(2)[1].toLowerCase();
					double result;
					try {
						result = calculator.evaluate(expression);
					} catch (Exception e) {
						event.sendMessage("Could not resolve `" + expression + "`").queue();
						return;
					}
					event.sendMessage(String.valueOf(result).replaceAll("[.]?0+$", "")).queue();
				})
				.build();
	}
}
