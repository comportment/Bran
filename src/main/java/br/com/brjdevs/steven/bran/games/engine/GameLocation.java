package br.com.brjdevs.steven.bran.games.engine;

import br.com.brjdevs.steven.bran.core.client.Bran;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.requests.RestAction;

public class GameLocation {
    
    private boolean isprivate;
    private String channelId;
    private int shard;
    
    public GameLocation(MessageChannel channel) {
        this.isprivate = channel instanceof PrivateChannel;
        this.channelId = channel.getId();
        this.shard = Bran.getInstance().getShardId(channel.getJDA());
    }
    
    public MessageChannel getChannel() {
        JDA jda = Bran.getInstance().getShards()[shard].getJDA();
        return isPrivate() ? jda.getPrivateChannelById(channelId) : jda.getTextChannelById(channelId);
    }
    
    public RestAction<Message> send(String msg) {
        return getChannel().sendMessage(msg);
    }
    
    public RestAction<Message> send(Message msg) {
        return getChannel().sendMessage(msg);
    }
    
    public RestAction<Message> send(MessageEmbed msg) {
        return getChannel().sendMessage(msg);
    }
    
    public boolean isPrivate() {
        return isprivate;
    }
}
