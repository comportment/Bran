package br.com.brjdevs.bran.cmds.guildAdmin;

import br.com.brjdevs.bran.core.command.*;
import br.com.brjdevs.bran.core.managers.Permissions;
import br.com.brjdevs.bran.core.managers.RolePick;
import br.com.brjdevs.bran.core.managers.RolePick.RolePickAction;
import br.com.brjdevs.bran.core.utils.ListBuilder;
import br.com.brjdevs.bran.core.utils.ListBuilder.Format;
import br.com.brjdevs.bran.core.utils.MathUtils;
import net.dv8tion.jda.core.entities.Role;

import java.util.List;
import java.util.stream.Collectors;

@RegisterCommand
public class GiveableRolesCommand {
	public GiveableRolesCommand() {
		CommandManager.addCommand(new TreeCommandBuilder(Category.GUILD_ADMINISTRATOR)
				.setAliases("giveme")
				.setName("Giveme Command")
				.setHelp("giveme ?")
				.setPrivateAvailable(false)
				.onMissingPermission(CommandAction.REDIRECT)
				.onNotFound(CommandAction.REDIRECT)
				.setDefault("role")
				.addCommand(new CommandBuilder(Category.MISCELLANEOUS)
						.setAliases("role")
						.setName("Giveme Role Command")
						.setDescription("Gives or remove public roles.")
						.setArgs("[role name]")
						.setAction((event) -> {
							String roleName = event.getArgs(2)[1];
							if (roleName.isEmpty()) {
								event.sendMessage("You have to tell me a Role Name!").queue();
								return;
							}
							if (event.getGuild().getGiveableRoles().getRolesId().isEmpty()) {
								event.sendMessage("There are no giveable roles available in this guild!").queue();
								return;
							}
							List<Role> matches = event.getOriginGuild().getRolesByName(roleName, true)
									.stream()
									.filter(role -> event.getGuild().getGiveableRoles().isGiveable(role))
									.collect(Collectors.toList());
							if (matches.size() > 1) {
								event.sendMessage(
										"Found " + matches.size() + " roles matching that criteria.\n```md\n" +
												(String.join("\n", matches.stream()
														.map(role -> (matches.indexOf(role) + 1) + ". " +
																role.getName() + " ID: " + role.getId() + (event.getOriginGuild().getRoles().contains(role) ? " *" : "")).collect(Collectors.toList()))) +
												"\n* Roles you have (would be removed if chosen)```\nType the number below to choose one!")
										.queue(msg -> new RolePick(event.getAuthor(),
												matches.stream().map(Role::getId).collect(Collectors.toList()), msg, RolePickAction.GIVE));
								return;
							}
							if (matches.isEmpty()) {
								event.sendMessage("Could not find any roles matching that criteria.").queue();
								return;
							}
							event.getOriginGuild().getController().addRolesToMember(event.getOriginMember(), matches.get(0)).queue();
							event.sendMessage("You have been given the role`" + matches.get(0).getName() + "`!").queue();
						})
						.build())
				.addCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
						.setAliases("add")
						.setName("Giveme Add Command")
						.setDescription("Set Roles as public.")
						.setArgs("[role name]")
						.setRequiredPermission(Permissions.GUILD_MANAGE)
						.setAction((event) -> {
							String roleName = event.getArgs(2)[1];
							if (roleName.isEmpty()) {
								event.sendMessage("You have to tell me a Role Name!").queue();
								return;
							}
							if (event.getGuild().getGiveableRoles().getRolesId().isEmpty()) {
								event.sendMessage("There are no giveable roles available in this guild!").queue();
								return;
							}
							List<Role> matches = event.getOriginGuild().getRolesByName(roleName, true)
									.stream()
									.filter(role -> !event.getGuild().getGiveableRoles().isGiveable(role)
											&& role != event.getOriginGuild().getPublicRole())
									.collect(Collectors.toList());
							if (matches.size() > 1) {
								event.sendMessage(
										"Found " + matches.size() + " roles matching that criteria.\n```md\n" +
												(String.join("\n", matches.stream()
														.map(role -> (matches.indexOf(role) + 1) + ". " +
																role.getName() + " ID: " + role.getId()).collect(Collectors.toList()))) +
												"```\nType the number below to choose one!")
										.queue(msg -> new RolePick(event.getAuthor(),
												matches.stream().map(Role::getId).collect(Collectors.toList()), msg, RolePickAction.ADD_ROLE));
								return;
							}
							if (matches.isEmpty()) {
								event.sendMessage("Could not find any roles matching that criteria.").queue();
								return;
							}
							event.getGuild().getGiveableRoles().getRolesId().add(matches.get(0).getId());
							event.sendMessage("Done! Now `" + matches.get(0).getName() + "` is a public role!").queue();
						})
						.build())
				.addCommand(new CommandBuilder(Category.GUILD_ADMINISTRATOR)
						.setAliases("remove")
						.setName("Giveme Remove Command")
						.setDescription("Removes a role from public roles.")
						.setArgs("[role name]")
						.setRequiredPermission(Permissions.GUILD_MANAGE)
						.setAction((event) -> {
							String roleName = event.getArgs(2)[1];
							if (roleName.isEmpty()) {
								event.sendMessage("You have to tell me a Role Name!").queue();
								return;
							}
							List<Role> matches = event.getOriginGuild().getRolesByName(roleName, true)
									.stream()
									.filter(role -> event.getGuild().getGiveableRoles().isGiveable(role)
											&& role != event.getOriginGuild().getPublicRole())
									.collect(Collectors.toList());
							if (matches.size() > 1) {
								event.sendMessage(
										"Found " + matches.size() + " roles matching that criteria.\n```md\n" +
												(String.join("\n", matches.stream()
														.map(role -> (matches.indexOf(role) + 1) + ". " +
																role.getName() + " ID: " + role.getId()).collect(Collectors.toList()))) +
												"```\nType the number below to choose one!")
										.queue(msg -> new RolePick(event.getAuthor(),
												matches.stream().map(Role::getId).collect(Collectors.toList()), msg, RolePickAction.REMOVE_ROLE));
								return;
							}
							if (matches.isEmpty()) {
								event.sendMessage("Could not find any roles matching that criteria.").queue();
								return;
							}
							event.getGuild().getGiveableRoles().getRolesId().remove(matches.get(0).getId());
							event.sendMessage("Done! Now `" + matches.get(0).getName() + "` is no longer a public role!").queue();
						})
						.build())
				.addCommand(new CommandBuilder(Category.INFORMATIVE)
						.setAliases("list")
						.setName("Giveme List COmmand")
						.setDescription("Lists you all the giveable roles in the current guild")
						.setArgs("<page>")
						.setAction((event) -> {
							if (event.getGuild().getGiveableRoles().getRolesId().isEmpty()) {
								event.sendMessage("There are no giveable roles available in this guild!").queue();
								return;
							}
							String r = event.getArgs(2)[1];
							int i = MathUtils.parseIntOrDefault(r, 1);
							if (i == 0) i = 1;
							List<String> list = event.getGuild().getGiveableRoles().getRolesId()
									.stream()
									.filter(id -> event.getOriginGuild().getRoleById(id) != null)
									.map(id -> {
										Role role = event.getOriginGuild().getRoleById(id);
										return role.getName() + " (ID: " + role.getId() + ")";
									}).collect(Collectors.toList());
							ListBuilder listBuilder = new ListBuilder(list, i, 15);
							listBuilder.setName("Giveable Roles for " + event.getOriginGuild().getName());
							listBuilder.setFooter("Total Roles: " + list.size());
							event.sendMessage(listBuilder.format(Format.CODE_BLOCK, "md")).queue();
						})
						.build())
				.build());
	}
}
