package br.net.brjdevs.steven.bran.cmds.guildAdmin;

import br.net.brjdevs.steven.bran.core.client.Bran;
import br.net.brjdevs.steven.bran.core.command.Argument;
import br.net.brjdevs.steven.bran.core.command.Command;
import br.net.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.net.brjdevs.steven.bran.core.command.builders.TreeCommandBuilder;
import br.net.brjdevs.steven.bran.core.command.enums.Category;
import br.net.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.net.brjdevs.steven.bran.core.managers.Permissions;
import net.dv8tion.jda.core.MessageBuilder;

import java.util.Arrays;
import java.util.List;

public class PrefixesCommand {
	
	@Command
	private static ICommand prefixes() {
		return new TreeCommandBuilder(Category.GUILD_ADMINISTRATOR)
				.setName("Prefix Command")
				.setDefault("list")
                .setAliases("prefix")
                .setDescription("Too much bots in your Guild? Fix some prefix conflicts with this Command.")
								.setPrivateAvailable(false)
				.addSubCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
						.setAliases("rm")
						.setDescription("Remove prefixes.")
						.setName("Prefix Remove Command")
						.setArgs(new Argument("prefixes", String.class))
						.setRequiredPermission(Permissions.PREFIX)
						.setAction((event, rawArgs) -> {
                            List<String> list = Bran.getInstance().getDataManager().getData().get().getGuildData(event.getGuild(), false).prefixes;
                            if (list.size() == 1) {
								event.sendMessage("This guild only has one prefix, you can't remove them anymore!").queue();
								return;
							}
							String[] prefixes = ((String) event.getArgument("prefixes").get()).split("\\s+");
							int amount = list.size();
							if (list.size() <= prefixes.length) {
								event.sendMessage("You can't remove `" + prefixes.length + "` prefix(es) because you have to keep at least one prefix!").queue();
								return;
							}
							Arrays.stream(prefixes).filter(list::contains)
									.forEach(list::remove);
							amount -= list.size();
							if (amount == 0) return;
							event.sendMessage("Removed " + amount + " prefix" + (amount == 1 ? "" : "es") + ". Now these are my prefixes here: " + (String.join(", ", list))).queue();
                            Bran.getInstance().getDataManager().getData().update();
                        })
						.build())
				.addSubCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
						.setAliases("add")
						.setDescription("Adds prefixes to the bot.")
						.setName("Prefix Add Command")
						.setArgs(new Argument("prefixes", String.class))
						.setRequiredPermission(Permissions.PREFIX)
						.setAction((event, rawArgs) -> {
							try{
                                if (Bran.getInstance().getDataManager().getData().get().getGuildData(event.getGuild(), true).prefixes.size() > 5) {
                                    event.sendMessage("You cannot have more than " + 5 + " prefixes.").queue();
									return;
								}
                                List<String> list = Bran.getInstance().getDataManager().getData().get().getGuildData(event.getGuild(), false).prefixes;
                                String[] prefixes = ((String) event.getArgument("prefixes").get()).split("\\s+");
								Arrays.stream(prefixes).filter(prefix -> !list.contains(prefix)).forEach(list::add);
								event.sendMessage("Now these are my prefixes here: " + (String.join(", ", list))).queue();
                                Bran.getInstance().getDataManager().getData().update();
                            } catch (Exception e1) {
								e1.printStackTrace();
							}
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.INFORMATIVE)
						.setAliases("list")
						.setDescription("Lists this guild prefixes.")
						.setName("Prefix List Command")
						.setAction((event, args) -> {
							MessageBuilder builder = new MessageBuilder().append("These are my prefixes here: ");
                            builder.append(String.join(", ", Bran.getInstance().getDataManager().getData().get().getGuildData(event.getGuild(), true).prefixes));
                            event.sendMessage(builder.build()).queue();
						})
						.build())
				.build();
	}
}
