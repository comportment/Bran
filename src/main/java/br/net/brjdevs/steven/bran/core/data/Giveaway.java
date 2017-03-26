package br.net.brjdevs.steven.bran.core.data;

import br.net.brjdevs.steven.bran.core.client.Bran;
import br.net.brjdevs.steven.bran.core.client.Client;
import br.net.brjdevs.steven.bran.core.managers.TaskManager;
import br.net.brjdevs.steven.bran.core.utils.CollectionUtils;
import br.net.brjdevs.steven.bran.core.utils.Hastebin;
import br.net.brjdevs.steven.bran.core.utils.Utils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Giveaway {
    
    static {
        TaskManager.startAsyncTask("Giveaway Expirator", (service) ->
                Bran.getInstance().getDataManager().getData().get().guilds.values().stream().map(guildData -> guildData.giveaway)
                        .filter(giveaway -> giveaway != null && giveaway.getExpiration() < System.currentTimeMillis()).forEach(giveaway -> {
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setColor(Color.decode("#43474B"));
                    embedBuilder.setAuthor("Information on the Giveaway for Guild " + giveaway.getGuild().getName(), null, giveaway.getGuild().getIconUrl());
                    embedBuilder.setFooter("Giveaway created by " + Utils.getUser(giveaway.getCreator().getUser()),
                            giveaway.getCreator().getUser().getEffectiveAvatarUrl());
                    String desc = "This giveaway was available for " + (giveaway.isPublic() ? "everyone" : "members with role `" +
                            giveaway.getRole().getName()) + ".\n";
                    String participating = giveaway.getParticipants().stream().map(member -> Utils.getUser(member.getUser()))
                            .collect(Collectors.joining("\n"));
                    if (participating.length() > MessageEmbed.TEXT_MAX_LENGTH - desc.length())
                        participating = "The list was too long so I uploaded it to Hastebin: " + Hastebin.post(participating);
                    List<Long> p = new ArrayList<>(giveaway.getParticipantsRaw());
                    List<Member> winners = new ArrayList<>();
                    for (int i = 0; i < giveaway.getMaxWinners() && !p.isEmpty(); i++) {
                        long l = CollectionUtils.random(p);
                        p.remove(l);
                        Member m = giveaway.getGuild().getMemberById(String.valueOf(l));
                        if (m == null) continue;
                        winners.add(m);
                    }
                    desc += participating + "\n\nThere was " + giveaway.getTotalParticipants() + " users participating on this Giveaway!\n\nAnd the "
                            + (giveaway.getMaxWinners() > 1 ? "winners are" : "winner is") + "... " + winners.stream().map(m -> Utils.getUser(m.getUser()))
                            .collect(Collectors.joining("\n"));
                    embedBuilder.setDescription(desc);
                    giveaway.getChannel().sendMessage(embedBuilder.build()).queue();
                    giveaway.getChannel().sendMessage("Congratulations, " + (winners.stream().map(m -> m.getUser().getAsMention())
                            .collect(Collectors.joining(", "))) + "! You won this Giveaway, contact " + Utils.getUser(giveaway.getCreator().getUser())
                            + " to receive your prize(s)! :smile:").queue();
                    winners.forEach(member -> member.getUser().openPrivateChannel().queue(channel -> channel.sendMessage("Hey there! Congratulations! You were one of the winners in a Giveaway running in " + giveaway.getGuild().getName() + ", contact " + Utils.getUser(giveaway.getCreator().getUser()) + " to receive your prize(s)!").queue()));
                    Bran.getInstance().getDataManager().getData().get().getGuildData(giveaway.getGuild(), true).giveaway = null;
                    Bran.getInstance().getDataManager().getData().update();
                }), 1);
    }
    
	private int maxUsers;
	private long creator;
	private long expiresIn;
	private long roleId;
	private long guildId;
	private long channel;
	private List<Long> participating;
	
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
		this.channel = Long.parseLong(channel.getId());
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
    
    public Guild getGuild() {
        return getShard().getJDA().getGuildById(String.valueOf(guildId));
    }
    
    public Role getRole() {
        return getGuild().getRoleById(String.valueOf(roleId));
    }
    
    public long getExpiration() {
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
    
    public Member getCreator() {
        return getGuild().getMemberById(String.valueOf(creator));
    }
    
    public List<Member> getParticipants() {
        return Collections.unmodifiableList(participating.stream().filter(l -> getGuild().getMemberById(String.valueOf(creator)) != null).map(l -> getGuild().getMemberById(String.valueOf(l))).filter(Objects::nonNull).distinct().collect(Collectors.toList()));
    }
    
    public List<Long> getParticipantsRaw() {
        return Collections.unmodifiableList(participating);
	}
	
	public int getTotalParticipants() {
		return participating.size();
	}
    
    public TextChannel getChannel() {
        String id = String.valueOf(channel);
        return getGuild().getTextChannelById(id) == null ? getGuild().getPublicChannel() : getGuild().getTextChannelById(id);
    }
    
    public Client getShard() {
        return Bran.getInstance().getShards()[Bran.getInstance().calcShardId(guildId)];
    }
}
