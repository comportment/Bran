package br.com.brjdevs.steven.bran.core.data.guild.settings;

import br.com.brjdevs.steven.bran.Client;
import br.com.brjdevs.steven.bran.ClientShard;
import br.com.brjdevs.steven.bran.core.managers.ExpirationManager;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Giveaway {
	
	public static ExpirationManager ExpirationManager = new ExpirationManager();
	
	private int maxUsers;
	private long creator;
	private long expiresIn;
	private long roleId;
	private long guildId;
	private List<Long> participating;
	private boolean timedOut;
	
	public Giveaway(Member member, Guild guild, Role role, int maxUsers, long expiresIn) {
		this.maxUsers = maxUsers;
		this.creator = Long.parseLong(member.getUser().getId());
		this.expiresIn = expiresIn + System.currentTimeMillis();
		if (role != null && role != guild.getPublicRole())
			this.roleId = Long.parseLong(role.getId());
		else
			this.roleId = -1L;
		this.guildId = Long.parseLong(guild.getId());
		this.participating = new ArrayList<>();
		this.timedOut = false;
		if (expiresIn > 0) ExpirationManager.letExpire(this.expiresIn, () -> {
			member.getUser().openPrivateChannel().queue(c -> c.sendMessage("Hey, the Giveaway you created in " + guild.getName() + " has expired, you should end it!").queue());
			this.timedOut = true;
		});
	}
	
	public boolean isExpired() {
		return timedOut;
	}
	
	public boolean participate(Member member) {
		long l = Long.parseLong(member.getUser().getId());
		if (participating.contains(l)) return false;
		participating.add(l);
		return true;
	}
	
	public boolean isPublic() {
		return roleId == -1L;
	}
	
	public Guild getGuild(Client client) {
		return getShard(client).getJDA().getGuildById(String.valueOf(guildId));
	}
	
	public Role getRole(Client client) {
		return getGuild(client).getRoleById(String.valueOf(roleId));
	}
	
	public long getExpiresIn() {
		return expiresIn;
	}
	
	public int getMaxWinners() {
		return maxUsers;
	}
	
	public boolean join(Member member) {
		if (participating.size() > maxUsers) return false;
		participating.add(Long.parseLong(member.getUser().getId()));
		return true;
	}
	
	public boolean isTimingOut() {
		return expiresIn == Long.MIN_VALUE;
	}
	
	public Member getCreator(Client client) {
		return getGuild(client).getMemberById(String.valueOf(creator));
	}
	
	public List<Member> getParticipants(Client client) {
		return Collections.unmodifiableList(participating.stream().filter(l -> getGuild(client).getMemberById(String.valueOf(creator)) != null).map(l -> getGuild(client).getMemberById(String.valueOf(l))).filter(Objects::nonNull).distinct().collect(Collectors.toList()));
	}
	
	public List<Long> getParticipants() {
		return Collections.unmodifiableList(participating);
	}
	
	public int getTotalParticipants() {
		return participating.size();
	}
	
	public ClientShard getShard(Client client) {
		return client.getShards()[client.calcShardId(guildId)];
	}
}
