package br.com.brjdevs.steven.bran.cmds.guildAdmin;

import br.com.brjdevs.steven.bran.Bot;
import br.com.brjdevs.steven.bran.core.command.*;
import br.com.brjdevs.steven.bran.core.data.guild.DiscordGuild;
import br.com.brjdevs.steven.bran.core.managers.Permissions;
import br.com.brjdevs.steven.bran.core.utils.StringUtils;
import net.dv8tion.jda.core.MessageBuilder;

import java.util.Arrays;

public class PrefixesCommand {
	
	@Command
	public static ICommand prefixes() {
		return new TreeCommandBuilder(Category.GUILD_ADMINISTRATOR)
				.setName("Prefix Command")
				.setDefault("list")
				.setAliases("prefix", "p")
				.setHelp("prefix ?")
				.setPrivateAvailable(false)
				.addCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
						.setAliases("rm")
						.setDescription("Remove prefixes.")
						.setName("Prefix Remove Command")
						.setArgs("[prefixes]")
						.setRequiredPermission(Permissions.PREFIX)
						.setAction((event, rawArgs) -> {
							String[] args = StringUtils.splitSimple(rawArgs);
							if(args.length < 2) {
								event.sendMessage("You didn't tell me prefixes to remove, please use `" + event.getPrefix() + "prefix rm [prefixes]`").queue();
								return;
							}
							if(event.getGuild().getPrefixes().size() == 1) {
								event.sendMessage("You gotta keep at least one prefix!").queue();
								return;
							}
							String[] prefixes = rawArgs.substring(rawArgs.indexOf(" ") + 1).split(" ");
							int amount = event.getGuild().getPrefixes().size();
							if(event.getGuild().getPrefixes().size() <= prefixes.length) {
								event.sendMessage("You can't remove `" + prefixes.length + "` prefix(es) because you have to keep at least one prefix!").queue();
								return;
							}
							Arrays.stream(prefixes).filter(prefix -> event.getGuild().getPrefixes().contains(prefix))
									.forEach(p -> event.getGuild().getPrefixes().remove(p));
							amount -= event.getGuild().getPrefixes().size();
							if (amount == 0) return;
							event.sendMessage("Removed " + amount + " prefix" + (amount == 1 ? "" : "es") + ". Now these are my prefixes here: " + (String.join(", ", event.getGuild().getPrefixes()))).queue();
						})
						.build())
				.addCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
						.setAliases("add")
						.setDescription("Adds prefixes to the bot.")
						.setName("Prefix Add Command")
						.setArgs("[prefixes]")
						.setRequiredPermission(Permissions.PREFIX)
						.setAction((event, rawArgs) -> {
							DiscordGuild discordGuild = event.getGuild();
							String[] args = StringUtils.splitSimple(rawArgs);
							try{
								if(args.length < 2) {
									event.sendMessage("You didn't tell me prefixes to add, please use `" + event.getPrefix() + "prefix add [prefixes]`").queue();
									return;
								}
								if(discordGuild.getPrefixes().size() > Bot.MAX_PREFIXES) {
									event.sendMessage("You cannot have more than " + Bot.MAX_PREFIXES + " prefixes.").queue();
									return;
								}
								String[] prefixes = rawArgs.substring(rawArgs.indexOf(" ") + 1).split(" ");
								Arrays.stream(prefixes).filter(prefix -> !event.getGuild().getPrefixes().contains(prefix)).forEach(discordGuild.getPrefixes()::add);
								event.sendMessage("Now these are my prefixes here: " + (String.join(", ", discordGuild.getPrefixes()))).queue();
							}catch (Exception e1) {
								e1.printStackTrace();
							}
						})
						.build())
				.addCommand(new CommandBuilder(Category.INFORMATIVE)
						.setAliases("list")
						.setDescription("Lists this guild prefixes.")
						.setName("Prefix List Command")
						.setAction((event, args) -> {
							MessageBuilder builder = new MessageBuilder().append("These are my prefixes here: ");
							builder.append(String.join(", ", event.getGuild().getPrefixes()));
							event.sendMessage(builder.build()).queue();
						})
						.build())
				.build();
	}
}
