package br.com.brjdevs.steven.bran.cmds.guildAdmin;

import br.com.brjdevs.steven.bran.Bot;
import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.builders.TreeCommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.data.guild.DiscordGuild;
import br.com.brjdevs.steven.bran.core.managers.Permissions;
import net.dv8tion.jda.core.MessageBuilder;

import java.util.Arrays;

public class PrefixesCommand {
	
	@Command
	private static ICommand prefixes() {
		return new TreeCommandBuilder(Category.GUILD_ADMINISTRATOR)
				.setName("Prefix Command")
				.setDefault("list")
				.setAliases("prefix", "p")
				.setDescription("Too much bots in your Guild? Fix some prefix conflicts with this Command.")
				.setHelp("prefix ?")
				.setPrivateAvailable(false)
				.addSubCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
						.setAliases("rm")
						.setDescription("Remove prefixes.")
						.setName("Prefix Remove Command")
						.setArgs(new Argument<>("prefixes", String.class))
						.setRequiredPermission(Permissions.PREFIX)
						.setAction((event, rawArgs) -> {
							if (event.getDiscordGuild().getPrefixes().size() == 1) {
								event.sendMessage("This guild only has one prefix, you can't remove them anymore!").queue();
								return;
							}
							String[] prefixes = ((String) event.getArgument("prefixes").get()).split("\\s+");
							int amount = event.getDiscordGuild().getPrefixes().size();
							if (event.getDiscordGuild().getPrefixes().size() <= prefixes.length) {
								event.sendMessage("You can't remove `" + prefixes.length + "` prefix(es) because you have to keep at least one prefix!").queue();
								return;
							}
							Arrays.stream(prefixes).filter(prefix -> event.getDiscordGuild().getPrefixes().contains(prefix))
									.forEach(p -> event.getDiscordGuild().getPrefixes().remove(p));
							amount -= event.getDiscordGuild().getPrefixes().size();
							if (amount == 0) return;
							event.sendMessage("Removed " + amount + " prefix" + (amount == 1 ? "" : "es") + ". Now these are my prefixes here: " + (String.join(", ", event.getDiscordGuild().getPrefixes()))).queue();
						})
						.build())
				.addSubCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
						.setAliases("add")
						.setDescription("Adds prefixes to the bot.")
						.setName("Prefix Add Command")
						.setArgs(new Argument<>("prefixes", String.class))
						.setRequiredPermission(Permissions.PREFIX)
						.setAction((event, rawArgs) -> {
							DiscordGuild discordGuild = event.getDiscordGuild();
							try{
								if(discordGuild.getPrefixes().size() > Bot.MAX_PREFIXES) {
									event.sendMessage("You cannot have more than " + Bot.MAX_PREFIXES + " prefixes.").queue();
									return;
								}
								String[] prefixes = ((String) event.getArgument("prefixes").get()).split("\\s+");
								Arrays.stream(prefixes).filter(prefix -> !event.getDiscordGuild().getPrefixes().contains(prefix)).forEach(discordGuild.getPrefixes()::add);
								event.sendMessage("Now these are my prefixes here: " + (String.join(", ", discordGuild.getPrefixes()))).queue();
							}catch (Exception e1) {
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
							builder.append(String.join(", ", event.getDiscordGuild().getPrefixes()));
							event.sendMessage(builder.build()).queue();
						})
						.build())
				.build();
	}
}
