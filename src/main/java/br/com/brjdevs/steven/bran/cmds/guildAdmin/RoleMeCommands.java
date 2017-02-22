package br.com.brjdevs.steven.bran.cmds.guildAdmin;

import br.com.brjdevs.steven.bran.core.command.Argument;
import br.com.brjdevs.steven.bran.core.command.Command;
import br.com.brjdevs.steven.bran.core.command.builders.CommandBuilder;
import br.com.brjdevs.steven.bran.core.command.enums.Category;
import br.com.brjdevs.steven.bran.core.command.interfaces.ICommand;
import br.com.brjdevs.steven.bran.core.data.GuildData;
import br.com.brjdevs.steven.bran.core.managers.Permissions;
import br.com.brjdevs.steven.bran.core.quote.Quotes;
import net.dv8tion.jda.core.entities.Role;

import java.util.List;
import java.util.stream.Collectors;

public class RoleMeCommands {
	
	@Command
	private static ICommand rolemeadd() {
		return new CommandBuilder(Category.GUILD_ADMINISTRATOR)
				.setPrivateAvailable(false)
				.setRequiredPermission(Permissions.GUILD_MANAGE)
				.setArgs(new Argument<>("rolename", String.class))
				.setAliases("rolemeadd")
				.setName("RoleMe Add Command")
				.setDescription("Sets a Role as public!")
				.setAction((event) -> {
					String role = ((String) event.getArgument("rolename").get());
					List<Role> matches = event.getGuild().getRolesByName(role, true);
					if (matches.isEmpty() && role.matches("[0-9]{17,18}")) {
						Role r = event.getGuild().getRoleById(role);
						if (r != null)
							matches.add(r);
					}
					if (matches.isEmpty()) {
						event.sendMessage("I couldn't find any roles with that name or ID!").queue();
					} else if (matches.size() > 1) {
						event.sendMessage("Found " + matches.size() + " matches, please narrow down your search!").queue();
					} else {
						Role publicRole = matches.get(0);
						if (event.getGuildData().getPublicRoles(event.getJDA()).contains(publicRole)) {
							event.sendMessage(Quotes.FAIL, "This role is already public! Users can acquire it by using `" + event.getPrefix() + "giveme " + publicRole.getName() + "`.").queue();
							return;
						}
						event.getGuildData().addPublicRole(publicRole);
						event.sendMessage(Quotes.SUCCESS, "Now " + publicRole.getName() + " is a Public Role, users can acquire it by using `" + event.getPrefix() + "giveme " + publicRole.getName() + "`.").queue();
					}
				})
				.build();
	}
	
	@Command
	private static ICommand rolemeremove() {
		return new CommandBuilder(Category.GUILD_ADMINISTRATOR)
				.setPrivateAvailable(false)
				.setRequiredPermission(Permissions.GUILD_MANAGE)
				.setArgs(new Argument<>("rolename", String.class))
				.setAliases("rolemeremove", "rolemerm")
				.setName("RoleMe Remove Command")
				.setDescription("Makes a role not public again!")
				.setAction((event) -> {
					String role = ((String) event.getArgument("rolename").get());
					List<Role> matches = event.getGuild().getRolesByName(role, true);
					if (matches.isEmpty() && role.matches("[0-9]{17,18}")) {
						Role r = event.getGuild().getRoleById(role);
						if (r != null)
							matches.add(r);
					}
					if (matches.isEmpty()) {
						event.sendMessage("I couldn't find any roles with that name or ID!").queue();
					} else if (matches.size() > 1) {
						event.sendMessage("Found " + matches.size() + " matches, please narrow down your search!").queue();
					} else {
						Role publicRole = matches.get(0);
						if (!event.getGuildData().getPublicRoles(event.getJDA()).contains(publicRole)) {
							event.sendMessage(Quotes.FAIL, "This role isn't public!").queue();
							return;
						}
						event.getGuildData().removePublicRole(publicRole);
						event.sendMessage(Quotes.SUCCESS, "Now " + publicRole.getName() + " is no longer a Public Role.").queue();
					}
				})
				.build();
	}
	
	@Command
	private static ICommand roleme() {
		return new CommandBuilder(Category.MISCELLANEOUS)
				.setPrivateAvailable(false)
				.setArgs(new Argument<>("rolename", String.class, true))
				.setAliases("roleme")
				.setName("RoleMe Command")
				.setDescription("Gives you a Public Role.")
				.setAction((event) -> {
					if (!event.getArgument("rolename").isPresent()) {
						if (event.getGuildData().getPublicRoles().isEmpty()) {
							event.sendMessage("Seems like there are no roles available for this Command!").queue();
						} else {
							event.sendMessage("These are all the available roles for this Command:\n" + event.getGuildData().getPublicRoles(event.getJDA()).stream().map(Role::getId).collect(Collectors.joining("\n"))).queue();
						}
						return;
					}
					String role = ((String) event.getArgument("rolename").get());
					List<Role> matches = event.getGuild().getRolesByName(role, true);
					if (matches.isEmpty() && role.matches("[0-9]{17,18}")) {
						Role r = event.getGuild().getRoleById(role);
						if (r != null)
							matches.add(r);
					}
					GuildData guildData = event.getGuildData();
					matches = matches.stream().filter(guildData::isPublic).collect(Collectors.toList());
					if (matches.isEmpty()) {
						event.sendMessage("I couldn't find any roles with that name or ID!").queue();
					} else if (matches.size() > 1) {
						event.sendMessage("Found " + matches.size() + " matches, please narrow down your search!").queue();
					} else {
						Role publicRole = matches.get(0);
						if (!event.getSelfMember().canInteract(publicRole)) {
							event.sendMessage("I can't do that because I can't modify a role with higher or equal role than mine!").queue();
							return;
						}
						event.getGuild().getController().modifyMemberRoles(event.getMember(), publicRole).queue(success ->
								event.sendMessage(Quotes.SUCCESS, "Done!" + (event.getMember().getRoles().contains(publicRole) ? " Added" : " Removed") + " " + publicRole.getName() + " from your roles!").queue()
						);
					}
				})
				.build();
	}
}
