package br.com.brjdevs.bran.core.listeners;

import br.com.brjdevs.bran.core.RolePick;
import br.com.brjdevs.bran.core.RolePick.RolePickAction;
import br.com.brjdevs.bran.core.data.guild.DiscordGuild;
import br.com.brjdevs.bran.core.utils.MathUtils;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;
import net.dv8tion.jda.core.hooks.EventListener;

public class RolePickListener implements EventListener {
	
	@Override
	public void onEvent(Event e) {
		if (!(e instanceof GuildMessageReceivedEvent)) return;
		GuildMessageReceivedEvent event = (GuildMessageReceivedEvent) e;
		RolePick rolePick = RolePick.getRolePick(event.getAuthor());
		if (rolePick == null) return;
		if (!rolePick.getChannelId().equals(event.getChannel().getId())) return;
		String msg = event.getMessage().getRawContent();
		if (!MathUtils.isInteger(msg)) {
			rolePick.remove();
			if (msg.matches("^(c|cancel)$"))
				event.getChannel().sendMessage("Canceled!").queue();
			else
				event.getChannel().sendMessage("You didn't type a number, `cancel` or `c`, query canceled!").queue();
			try {
				rolePick.getMessage().deleteMessage().queue();
			} catch (ErrorResponseException ignored) {}
			return;
		}
		int pick = Integer.parseInt(msg) - 1;
		if (!MathUtils.isInRange(pick, -1, rolePick.getRolesId().size())) {
			event.getChannel().sendMessage(pick + "").queue();
			event.getMessage().addReaction("\u274c").queue();
			if (rolePick.getAttempts() == 3) {
				event.getChannel().sendMessage("You said invalid numbers 3 times, query canceled!").queue();
				rolePick.remove();
				try {
					rolePick.getMessage().deleteMessage().queue();
				} catch (ErrorResponseException ignored) {}
				return;
			}
			rolePick.addAttempt();
			return;
		}
		Role role = event.getGuild().getRoleById(rolePick.getRolesId().get(pick));
		if (role == null) {
			event.getChannel().sendMessage("Could not find role with ID `" + rolePick.getRolesId().get(pick) + "`, did someone delete it? Query canceled!").queue();
			if (rolePick.getAction() == RolePickAction.GIVE)
				DiscordGuild.getInstance(event.getGuild()).getGiveableRoles().getRolesId().remove(rolePick.getRolesId().get(pick));
			rolePick.remove();
			try {
				rolePick.getMessage().deleteMessage().queue();
			} catch (ErrorResponseException ignored) {}
			return;
		}
		if (rolePick.getAction() == RolePickAction.ADD_ROLE) {
			DiscordGuild discordGuild = DiscordGuild.getInstance(event.getGuild());
			discordGuild.getGiveableRoles().getRolesId().add(role.getId());
			event.getChannel().sendMessage("Done! Now `" + role.getName() + "` is a public role!").queue();
		} else if (rolePick.getAction() == RolePickAction.REMOVE_ROLE) {
			event.getGuild().getController().addRolesToMember(event.getMember(), role)
					.queue(success -> event.getChannel().sendMessage("You have been given the role `" + role.getName() + "`!").queue(),
							fail -> event.getChannel().sendMessage("Could not give your role: " + fail.getMessage()));
		} else {
			event.getGuild().getController().removeRolesFromMember(event.getMember(), role)
					.queue(success -> event.getChannel().sendMessage("You have been removed the role `" + role.getName() + "`!").queue(),
							fail -> event.getChannel().sendMessage("Could not remove your role: " + fail.getMessage()).queue());
		}
		rolePick.remove();
		try {
			rolePick.getMessage().deleteMessage().queue();
		} catch (ErrorResponseException ignored) {}
	}
}
