package br.com.brjdevs.steven.bran.cmds.fun;

import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.currency.Items;
import br.com.brjdevs.steven.bran.core.currency.TextChannelGround;
import br.com.brjdevs.steven.bran.core.utils.MathUtils;

public class RollCommand {
	
	private static String ROLL_MATCH = "^(-?\\d+(d+\\d+)?)$";
	
	@Command
	private static ICommand roll() {
		return new CommandBuilder(Category.FUN)
				.setAliases("roll")
				.setName("Roll Command")
				.setDescription("Rolls a dice!")
				.setExample("roll 1d20")
				.setArgs(new Argument("range", String.class, true))
				.setAction((event) -> {
					try {
						@SuppressWarnings("unchecked")
						Argument rangeArg = event.getArgument("range");
						String s = !rangeArg.isPresent() ? "1d20" : ((String) rangeArg.get());
						if (!s.matches(ROLL_MATCH)) {
							event.sendMessage("Hey, `" + s + "` is not a valid argument! You should use for example `1d20`.").queue();
							return;
						}
						String[] split = s.split("d+");
						int min = split.length == 1 ? 1 : Integer.parseInt(split[0]);
						int max = Integer.parseInt(split[split.length - 1]);
						if (min >= max) {
							event.sendMessage("The minimum value can't be equals or higher than the maximum!").queue();
							return;
						}
						int roll = MathUtils.random(min, max);
						event.sendMessage("\uD83C\uDFB2 **You rolled a `" + roll + "`!**").queue();
						TextChannelGround.of(event.getTextChannel()).dropItemWithChance(Items.GAME_DIE, 2);
						
					} catch (NumberFormatException ex) {
						String input = ex.getMessage().substring(ex.getMessage().indexOf("\"") + 1).replace("\"", "");
						event.sendMessage("Wew, that's a big number, so big that I can't even process that please use a smaller number. *(Input: " + input + "/Maximum: " + Integer.MAX_VALUE + ")*").queue();
					}
				})
				.build();
	}
}
