package br.com.brjdevs.steven.bran.core.data;

import br.com.brjdevs.steven.bran.core.client.Client;
import br.com.brjdevs.steven.bran.core.client.ClientShard;
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
		Client client = Client.getInstance();
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setColor(Color.decode("#43474B"));
		embedBuilder.setAuthor("Information on the Giveaway for Guild " + giveaway.getGuild(client).getName(), null, giveaway.getGuild(client).getIconUrl());
		embedBuilder.setFooter("Giveaway created by " + Utils.getUser(giveaway.getCreator(client).getUser()), giveaway.getCreator(client).getUser().getEffectiveAvatarUrl());
		String desc = "This giveaway was available for " + (giveaway.isPublic() ? "everyone" : "members with role `" + giveaway.getRole(client).getName()) + ".\n";
		String participating = giveaway.getParticipants(client).stream().map(member -> Utils.getUser(member.getUser())).collect(Collectors.joining("\n"));
		if (participating.length() > EmbedBuilder.TEXT_MAX_LENGTH - desc.length())
			participating = "The list was too long so I uploaded it to Hastebin: " + Hastebin.post(participating);
		List<Long> p = new ArrayList<>(giveaway.getParticipants());
		List<Member> winners = new ArrayList<>();
		for (int i = 0; i < giveaway.getMaxWinners() && !p.isEmpty(); i++) {
			long l = CollectionUtils.random(p);
			p.remove(l);
			Member m = giveaway.getGuild(client).getMemberById(String.valueOf(l));
			if (m == null) continue;
			winners.add(m);
		}
		desc += participating + "\n\nThere was " + giveaway.getTotalParticipants() + " users participating on this Giveaway!\n\nAnd the " + (giveaway.getMaxWinners() > 1 ? "winners are" : "winner is") + "... " + winners.stream().map(m -> Utils.getUser(m.getUser())).collect(Collectors.joining("\n"));
		embedBuilder.setDescription(desc);
		giveaway.getChannel(client).sendMessage(embedBuilder.build()).queue();
		giveaway.getChannel(client).sendMessage("Congratulations, " + (winners.stream().map(m -> m.getUser().getAsMention()).collect(Collectors.joining(", "))) + "! You won this Giveaway, contact " + Utils.getUser(giveaway.getCreator(client).getUser()) + " to receive your prize(s)! :smile:").queue();
		winners.forEach(member -> member.getUser().openPrivateChannel().queue(channel -> channel.sendMessage("Hey there! Congratulations! You were one of the winners in a Giveaway running in " + giveaway.getGuild(client).getName() + ", contact " + Utils.getUser(giveaway.getCreator(client).getUser()) + " to receive your prize(s)!").queue()));
		client.getDiscordBotData().getDataHolderManager().get().getGuild(giveaway.getGuild(client)).giveaway = null;
		client.getDiscordBotData().getDataHolderManager().update();
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
		return expiresIn > 0;
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
	
	public TextChannel getChannel(Client client) {
		String id = String.valueOf(channel);
		return getGuild(client).getTextChannelById(id) == null ? getGuild(client).getPublicChannel() : getGuild(client).getTextChannelById(id);
	}
	
	public ClientShard getShard(Client client) {
		return client.getShards()[client.calcShardId(guildId)];
	}
}
