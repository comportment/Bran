package br.net.brjdevs.steven.bran.core.data;

import br.net.brjdevs.steven.bran.core.audio.AudioUtils;
import br.net.brjdevs.steven.bran.core.client.Bran;
import br.net.brjdevs.steven.bran.core.command.CommandEvent;
import br.net.brjdevs.steven.bran.core.managers.CustomCommand;
import br.net.brjdevs.steven.bran.core.managers.Permissions;
import br.net.brjdevs.steven.bran.core.operations.ResultType;
import br.net.brjdevs.steven.bran.core.operations.ResultType.OperationResult;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.*;
import java.util.stream.Collectors;

public class GuildData {
	
	public Map<String, CustomCommand> customCommands = new HashMap<>();
	public List<String> prefixes = new ArrayList<>(5);
	public long maxSongDuration = AudioUtils.MAX_SONG_LENGTH;
	public int maxSongsPerUser;
	public long defaultPermission = Permissions.BASE_USR;
	public int fairQueueLevel = 0;
	public boolean isWordFilterEnabled = false;
	public List<String> filteredWords = new ArrayList<>();
	public String joinMsg = null;
	public String joinMsgDM = null;
	public String leaveMsg = null;
	public Giveaway giveaway = null;
    public Map<Long, Long> permissions = new HashMap<>();
    private boolean modLogEnabled = false;
    private long modLogChannel;
    private long guildId;
	private long announceChannelId;
	private List<Long> publicRoles = new ArrayList<>();
    private Map<Long, List<String>> disabledCommands = new HashMap<>();
    
    public GuildData(Guild guild) {
		this.guildId = Long.parseLong(guild.getId());
        this.prefixes.addAll(Bran.getInstance().getDataManager().getConfig().get().defaultPrefixes);
    }
	
	public Guild getGuild(JDA jda) {
		return jda.getGuildById(String.valueOf(guildId));
	}
	
	public long getPermissionForUser(User user) {
        return permissions.getOrDefault(Long.parseLong(user.getId()), user.getId().equals(Bran.getInstance().getDataManager().getConfig().get().ownerId) ? Permissions.BOT_OWNER : getGuild(user.getJDA()).getMember(user).isOwner() ? Permissions.GUILD_OWNER : Permissions.BASE_USR);
    }
	
	public OperationResult setPermission(CommandEvent event, long permsToAdd, long permsToTake, User user) {
		if (event.getAuthor().getId().equals(user.getId()))
			return ResultType.FAILURE.setExtras("You can't change your own permissions!", getPermissionForUser(user)); //Disable changing itself
		long senderPerm = getPermissionForUser(event.getAuthor()), targetPerm = getPermissionForUser(user); //Get perms
		if (!Permissions.checkPerms(senderPerm, targetPerm))
			return ResultType.FAILURE.setExtras("You don't have enough permission to do that."); //Check the Special Bits
		if ((senderPerm & (permsToAdd | permsToTake)) != (permsToAdd | permsToTake))
			return ResultType.FAILURE.setExtras("You don't have enough permission to do that.", (permsToAdd | permsToTake)); //Check if the Sender Perm have all the permissions
		long oldPerms = getPermissionForUser(user);
		permissions.put(Long.parseLong(user.getId()), targetPerm ^ (targetPerm & permsToTake) | permsToAdd);
		return ResultType.SUCCESS.setExtras("Successfully updated permissions!", oldPerms, getPermissionForUser(user));
	}
	
	public boolean hasPermission(User user, long perm) {
		return (getPermissionForUser(user) & perm) == perm;
	}
	
	public TextChannel getAnnounceTextChannel(JDA jda) {
		return jda.getTextChannelById(String.valueOf(announceChannelId));
	}
	
	public void setAnnounceTextChannel(TextChannel textChannel) {
		if (textChannel == null)
			announceChannelId = 0;
		else
			this.announceChannelId = Long.parseLong(textChannel.getId());
	}
	
	public List<Long> getPublicRoles() {
		if (publicRoles == null) publicRoles = new ArrayList<>();
		return publicRoles;
	}
	
	public List<Role> getPublicRoles(JDA jda) {
		if (publicRoles == null) publicRoles = new ArrayList<>();
		Guild guild = getGuild(jda);
		return Collections.unmodifiableList(publicRoles.stream().map(id -> guild.getRoleById(String.valueOf(id))).filter(Objects::nonNull).collect(Collectors.toList()));
	}
	
	public boolean isPublic(Role role) {
		if (publicRoles == null) publicRoles = new ArrayList<>();
		return publicRoles.contains(Long.parseLong(role.getId()));
	}
	
	public void addPublicRole(Role role) {
		if (publicRoles == null) publicRoles = new ArrayList<>();
		publicRoles.add(Long.valueOf(role.getId()));
	}
	
	public void removePublicRole(Role role) {
		if (publicRoles == null) publicRoles = new ArrayList<>();
		publicRoles.remove(Long.valueOf(role.getId()));
	}
    
    public boolean isModLogEnabled() {
        return modLogEnabled;
    }
    
    public void setModLogEnabled(boolean modLogEnabled) {
        this.modLogEnabled = modLogEnabled;
    }
    
    public TextChannel getModLogChannel(JDA jda) {
        return jda.getTextChannelById(String.valueOf(modLogChannel));
    }
    
    public void setModLogChannel(TextChannel modLogChannel) {
        this.modLogChannel = Long.parseLong(modLogChannel.getId());
    }
    
    public Map<Long, List<String>> getDisabledCommands() {
        if (disabledCommands == null) disabledCommands = new HashMap<>();
        return disabledCommands;
    }
    
    public List<String> getDisabledCommands(TextChannel channel) {
        return getDisabledCommands().getOrDefault(Long.parseLong(channel.getId()), new ArrayList<>());
    }
}