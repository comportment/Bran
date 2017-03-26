package br.net.brjdevs.steven.bran.cmds.guildAdmin;

import br.net.brjdevs.steven.bran.core.command.Argument;
import br.net.brjdevs.steven.bran.core.command.Command;
import br.net.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.net.brjdevs.steven.bran.core.command.enums.Category;
import br.net.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.net.brjdevs.steven.bran.core.managers.Permissions;
import br.net.brjdevs.steven.bran.core.quote.Quotes;
import br.net.brjdevs.steven.bran.core.utils.Utils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.PermissionException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class KickCommand {
	
	@Command
	private static ICommand kick() {
		return new CommandBuilder(Category.GUILD_ADMINISTRATOR)
				.setAliases("kick")
				.setName("Kick Command")
				.setDescription("Does this really need a Description?")
				.setExample("kick <@219186621008838669>")
				.setArgs(new Argument("mention", String.class))
				.setPrivateAvailable(false)
				.setRequiredPermission(Permissions.KICK_USR)
				.setAction((event) -> {
					if (!event.getSelfMember().hasPermission(Permission.KICK_MEMBERS)) {
						event.sendMessage("I can't perform this action due to a lack of permission. Missing Permission: KICK_MEMBERS").queue();
						return;
					}
					List<User> users = event.getMessage().getMentionedUsers();
					if (users.isEmpty()) {
						event.sendMessage("You have to mention at least one user and at most 5!").queue();
						return;
					}
					if (users.size() > 5) {
						event.sendMessage("I can only kick 5 users per command!").queue();
						return;
					}
					for (User user : users) {
						if (user.equals(event.getJDA().getSelfUser())) {
							event.sendMessage("Cannot remove myself from the Guild with Moderation Commands, please remove me own your own.").queue();
							continue;
						}
						if (!event.getSelfMember().canInteract(event.getGuild().getMember(user))) {
							event.sendMessage("I can't kick " + Utils.getUser(user) + "!").queue();
							continue;
						}
						event.getGuild().getController().kick(user.getId()).queue(s -> {
									String out = "Kicked " + users.stream().filter(Objects::nonNull).map(Utils::getUser).collect(Collectors.joining(", ")) + "!";
									event.sendMessage(Quotes.SUCCESS, out).queue();
								},
								throwable -> {
									if (throwable instanceof PermissionException) {
										event.sendMessage("I can't kick that user because its higher than me in the role hierarchy!").queue();
									} else {
										event.sendMessage("An unexpected error occurred! " + throwable.getMessage()).queue();
									}
								});
					}
				})
				.build();
	}
}
