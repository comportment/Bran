package br.com.brjdevs.steven.bran.core.command;

public class Test {
	
	@Command
	public static ICommand c() {
		return new TreeCommandBuilder(Category.FUN)
				.setAliases("top")
				.setName("Kek Command")
				.setDescription("Top")
				.setExample("top kek yes -10 \"sua mãe aquela puta intergalatica\"")
				.addSubCommand(new CommandBuilder(Category.FUN)
						.setAliases("kek")
						.setHelp("top kek ?")
						.setName("Top Kek Command")
						.setDescription("Top Kek")
						.setExample("top kek yest 10 akfhg")
						.setArgs(new Argument<>("bool", Boolean.class, true), new Argument<>("integer", Integer.class, true), new Argument<>("string", String.class, true))
						.setAction((event) -> {
							event.sendMessage(event.getArgument("bool").isPresent() ? "bool is present, value: " + event.getArgument("bool").get() : "bool is not present").queue();
							event.sendMessage(event.getArgument("integer").isPresent() ? "integer is present, value: " + event.getArgument("integer").get() : "integer is not present").queue();
							event.sendMessage(event.getArgument("string").isPresent() ? "string is present, value: " + event.getArgument("string").get() : "string is not present").queue();
						})
						.build())
				.addSubCommand(new TreeCommandBuilder(Category.FUN)
						.setAliases("workpls")
						.setName("Kek Workpls Command")
						.setDescription("Top Workpls")
						.setExample("top kek workpls topper")
						.addSubCommand(new CommandBuilder(Category.FUN)
								.setAliases("topper")
								.setHelp("top kek workpls topper ?")
								.setName("Top Kek Topper Command")
								.setDescription("Top Kek Topper")
								.setExample("top kek workpls topper")
								.setAction((event) -> event.sendMessage("WORKOOO").queue())
								.build())
						.build())
				.build();
	}
}
