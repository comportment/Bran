package br.com.brjdevs.steven.bran.core.data;

import br.com.brjdevs.steven.bran.core.client.Bran;
import br.com.brjdevs.steven.bran.core.client.BranShard;
import br.com.brjdevs.steven.bran.core.managers.CustomExpirationManager;
import br.com.brjdevs.steven.bran.core.utils.CollectionUtils;
import br.com.brjdevs.steven.bran.core.utils.Hastebin;
import br.com.brjdevs.steven.bran.core.utils.Utils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Giveaway {
	
	public static CustomExpirationManager<Giveaway> expiration = new CustomExpirationManager<>((giveaway) -> {
		Bran bran = Bran.getInstance();
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setColor(Color.decode("#43474B"));
		embedBuilder.setAuthor("Information on the Giveaway for Guild " + giveaway.getGuild(bran).getName(), null, giveaway.getGuild(bran).getIconUrl());
		embedBuilder.setFooter("Giveaway created by " + Utils.getUser(giveaway.getCreator(bran).getUser()), giveaway.getCreator(bran).getUser().getEffectiveAvatarUrl());
		String desc = "This giveaway was available for " + (giveaway.isPublic() ? "everyone" : "members with role `" + giveaway.getRole(bran).getName()) + ".\n";
		String participating = giveaway.getParticipants(bran).stream().map(member -> Utils.getUser(member.getUser())).collect(Collectors.joining("\n"));
		if (participating.length() > EmbedBuilder.TEXT_MAX_LENGTH - desc.length())
			participating = "The list was too long so I uploaded it to Hastebin: " + Hastebin.post(participating);
		List<Long> p = new ArrayList<>(giveaway.getParticipants());
		List<Member> winners = new ArrayList<>();
		for (int i = 0; i < giveaway.getMaxWinners() && !p.isEmpty(); i++) {
			long l = CollectionUtils.random(p);
			p.remove(l);
			Member m = giveaway.getGuild(bran).getMemberById(String.valueOf(l));
			if (m == null) continue;
			winners.add(m);
		}
		desc += participating + "\n\nThere was " + giveaway.getTotalParticipants() + " users participating on this Giveaway!\n\nAnd the " + (giveaway.getMaxWinners() > 1 ? "winners are" : "winner is") + "... " + winners.stream().map(m -> Utils.getUser(m.getUser())).collect(Collectors.joining("\n"));
		embedBuilder.setDescription(desc);
		giveaway.getChannel(bran).sendMessage(embedBuilder.build()).queue();
		giveaway.getChannel(bran).sendMessage("Congratulations, " + (winners.stream().map(m -> m.getUser().getAsMention()).collect(Collectors.joining(", "))) + "! You won this Giveaway, contact " + Utils.getUser(giveaway.getCreator(bran).getUser()) + " to receive your prize(s)! :smile:").queue();
		winners.forEach(member -> member.getUser().openPrivateChannel().queue(channel -> channel.sendMessage("Hey there! Congratulations! You were one of the winners in a Giveaway running in " + giveaway.getGuild(bran).getName() + ", contact " + Utils.getUser(giveaway.getCreator(bran).getUser()) + " to receive your prize(s)!").queue()));
		bran.getDataManager().getDataHolderManager().get().getGuild(giveaway.getGuild(bran)).giveaway = null;
		bran.getDataManager().getDataHolderManager().update();
	});
	
	private int maxUsers;
	private long creator;
	private long expiresIn;
	private long roleId;
	private long guildId;
	private long channel;
	private List<Long> participating;
	private boolean timedOut;
	
	public Giveaway(Member member, Guild guild, Role role, TextChannel channel, int maxUsers, long expiresIn) {
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
		this.channel = Long.parseLong(channel.getId());
		
		if (expiresIn > 0) expiration.letExpire(this, expiresIn);
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
	
	public Guild getGuild(Bran bran) {
		return getShard(bran).getJDA().getGuildById(String.valueOf(guildId));
	}
	
	public Role getRole(Bran bran) {
		return getGuild(bran).getRoleById(String.valueOf(roleId));
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
		return expiresIn > 0;
	}
	
	public Member getCreator(Bran bran) {
		return getGuild(bran).getMemberById(String.valueOf(creator));
	}
	
	public List<Member> getParticipants(Bran bran) {
		return Collections.unmodifiableList(participating.stream().filter(l -> getGuild(bran).getMemberById(String.valueOf(creator)) != null).map(l -> getGuild(bran).getMemberById(String.valueOf(l))).filter(Objects::nonNull).distinct().collect(Collectors.toList()));
	}
	
	public List<Long> getParticipants() {
		return Collections.unmodifiableList(participating);
	}
	
	public int getTotalParticipants() {
		return participating.size();
	}
	
	public TextChannel getChannel(Bran bran) {
		String id = String.valueOf(channel);
		return getGuild(bran).getTextChannelById(id) == null ? getGuild(bran).getPublicChannel() : getGuild(bran).getTextChannelById(id);
	}
	
	public BranShard getShard(Bran bran) {
		return bran.getShards()[bran.calcShardId(guildId)];
	}
}
