package br.com.brjdevs.steven.bran.core.data.guild.settings;

import br.com.brjdevs.steven.bran.BotContainer;
import br.com.brjdevs.steven.bran.core.command.CommandEvent;
import br.com.brjdevs.steven.bran.core.data.bot.settings.Profile;
import br.com.brjdevs.steven.bran.core.data.guild.DiscordGuild;
import br.com.brjdevs.steven.bran.core.managers.Permissions;
import br.com.brjdevs.steven.bran.core.operations.ResultType;
import br.com.brjdevs.steven.bran.core.operations.ResultType.OperationResult;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

import java.util.List;

public class GuildMember {
	
	private String userId;
	private String guildId;
	private Long perms;
	
	public GuildMember(User user, String guildId, BotContainer botContainer) {
		this.userId = user.getId();
		this.guildId = guildId;
		this.perms = DiscordGuild.getInstance(user.getJDA().getGuildById(guildId), botContainer).getDefaultPermission();
		botContainer.getProfiles().put(userId, new Profile(user));
	}
	
	public String getUserId() {
		return userId;
	}
	
	public String getGuildId() {
		return guildId;
	}
	
	public Long getRawPermissions(JDA jda, BotContainer container) {
		long l = container.getOwner().getId().equals(getUserId()) ? Permissions.BOT_OWNER : getGuild(jda) != null && getGuild(jda).getOwner().getUser().getId().equals(getUserId()) ? Permissions.GUILD_OWNER : 0L;
		return perms | l;
	}
	
	public OperationResult setPermission(CommandEvent event, long permsToAdd, long permsToTake) {
		if (event.getAuthor().getId().equals(userId))
			return ResultType.FAILURE.setExtras("You can't change your own permissions!", getRawPermissions(event.getJDA(), event.getBotContainer())); //Disable changing itself
		long senderPerm = event.getGuildMember().getRawPermissions(event.getJDA(), event.getBotContainer()), targetPerm = getRawPermissions(event.getJDA(), event.getBotContainer()); //Get perms
		if (!Permissions.checkPerms(senderPerm, targetPerm))
			return ResultType.FAILURE.setExtras("You don't have enough permission to do that."); //Check the Special Bits
		if ((senderPerm & (permsToAdd | permsToTake)) != (permsToAdd | permsToTake))
			return ResultType.FAILURE.setExtras("You don't have enough permission to do that.", (permsToAdd | permsToTake)); //Check if the Sender Perm have all the permissions
		long oldPerms = getRawPermissions(event.getJDA(), event.getBotContainer());
		perms = targetPerm ^ (targetPerm & permsToTake) | permsToAdd;
		return ResultType.SUCCESS.setExtras("Successfully updated permissions!", oldPerms, getRawPermissions(event.getJDA(), event.getBotContainer()));
	}
	
	public boolean hasPermission(Long perm, JDA jda, BotContainer container) {
		return (getRawPermissions(jda, container) & perm) == perm;
	}
	
	public List<String> getPermissions(JDA jda, BotContainer container) {
		return Permissions.toCollection(getRawPermissions(jda, container));
	}
	
	public Guild getGuild(JDA jda) {
		return jda.getGuildById(getGuildId());
	}
	
	public User getUser(JDA jda) {
		return jda.getUserById(getUserId());
	}
	
	public static class FakeGuildMember extends GuildMember {
		
		public FakeGuildMember(User user, String guildId, BotContainer container) {
			super(user, guildId, container);
		}
		
		@Override
		public String getGuildId() {
			throw new UnsupportedOperationException("This is a Fake Member, you can't get their Guild ID.");
		}
		
		@Override
		public Long getRawPermissions(JDA jda, BotContainer container) {
			return Permissions.BASE_USR;
		}
		
		@Override
		public OperationResult setPermission(CommandEvent event, long permsToAdd, long permsToTake) {
			return ResultType.INVALID.setExtras("This is a Fake Member, you can't update their permission.");
		}
		
		@Override
		public Guild getGuild(JDA jda) {
			throw new UnsupportedOperationException("This is a Fake Member, you can't get their guild.");
		}
	}
}
