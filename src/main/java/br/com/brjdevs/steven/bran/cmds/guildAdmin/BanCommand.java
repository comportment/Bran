package br.com.brjdevs.steven.bran.cmds.guildAdmin;

import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.currency.Items;
import br.com.brjdevs.steven.bran.core.currency.TextChannelGround;
import br.com.brjdevs.steven.bran.core.managers.Permissions;
import br.com.brjdevs.steven.bran.core.quote.Quotes;
import br.com.brjdevs.steven.bran.core.utils.Utils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.PermissionException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BanCommand {
	
	@Command
	private static ICommand ban() {
		return new CommandBuilder(Category.GUILD_ADMINISTRATOR)
				.setAliases("ban")
				.setName("Ban Command")
				.setDescription("Does this really need a Description?")
				.setExample("ban <@219186621008838669>")
				.setArgs(new Argument("mention", String.class))
				.setRequiredPermission(Permissions.BAN_USER)
				.setPrivateAvailable(false)
				.setAction((event) -> {
					if (!event.getSelfMember().hasPermission(Permission.BAN_MEMBERS)) {
						event.sendMessage("I can't perform this action due to a lack of permission. Missing Permission: BAN_MEMBERS").queue();
						return;
					}
					List<User> users = event.getMessage().getMentionedUsers();
					if (users.isEmpty()) {
						event.sendMessage("You have to mention at least one user and at most 5!").queue();
						return;
					}
					if (users.size() > 5) {
						event.sendMessage("I can only ban 5 users per command!").queue();
						return;
					}
					for (User user : users) {
						if (user.equals(event.getJDA().getSelfUser())) {
							event.sendMessage("Cannot remove myself from the Guild with Moderation Commands, please remove me own your own.").queue();
							continue;
						}
						if (!event.getSelfMember().canInteract(event.getGuild().getMember(user))) {
							event.sendMessage("I can't ban " + Utils.getUser(user) + "!").queue();
							continue;
						}
						event.getGuild().getController().ban(user.getId(), 7).queue(b -> {
									String out = "Banned " + users.stream().filter(Objects::nonNull).map(Utils::getUser).collect(Collectors.joining(", ")) + "!";
									event.sendMessage(Quotes.SUCCESS, out).queue();
									TextChannelGround.of(event.getTextChannel()).dropItemWithChance(Items.BAN_HAMMER, 10);
								},
								throwable -> {
									if (throwable instanceof PermissionException) {
										event.sendMessage("I can't ban that user because its higher than me in the role hierarchy!").queue();
									} else {
										event.sendMessage("An unexpected error occurred! " + throwable.getMessage()).queue();
									}
								});
					}
				})
				.build();
	}
}
