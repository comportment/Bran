package br.com.brjdevs.steven.bran.core.responsewaiter;

import br.com.brjdevs.steven.bran.core.client.Client;
import br.com.brjdevs.steven.bran.core.responsewaiter.events.ResponseListener;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.HashMap;
import java.util.Map;

public class ResponseWaiter {
	
	public static final Map<Long, ResponseWaiter> responseWaiters = new HashMap<>();
	public static final ResponseExpirationManager EXPIRATION = new ResponseExpirationManager();
	
	private long userId;
	private long channelId;
	private long guildId;
	private long expiresIn;
	private String[] validInputs;
    private Client client;
    private ResponseListener responseListener;
	private ExpectedResponseType expectedResponseType;
    
    public ResponseWaiter(User user, TextChannel textChannel, Client client, long expiresIn, String[] validInputs, ExpectedResponseType expectedResponseType, ResponseListener responseListener) {
        this.userId = Long.parseLong(user.getId());
		this.channelId = Long.parseLong(textChannel.getId());
		this.guildId = Long.parseLong(textChannel.getGuild().getId());
		this.expiresIn = expiresIn;
		this.validInputs = validInputs;
        this.client = client;
        this.responseListener = responseListener;
		this.expectedResponseType = expectedResponseType;
		if (responseWaiters.containsKey(userId)) {
			EXPIRATION.removeResponseWaiter(responseWaiters.get(userId));
		}
		responseWaiters.put(userId, this);
		EXPIRATION.addResponseWaiter(this, System.currentTimeMillis() + expiresIn);
	}
    
    public ResponseWaiter(long userId, long channelId, long guildId, Client client, long expiresIn, String[] validInputs, ExpectedResponseType expectedResponseType) {
        this.userId = userId;
		this.channelId = channelId;
		this.guildId = guildId;
        this.client = client;
        this.expiresIn = expiresIn;
		this.validInputs = validInputs;
		this.expectedResponseType = expectedResponseType;
		responseWaiters.put(userId, this);
		EXPIRATION.addResponseWaiter(this, System.currentTimeMillis() + expiresIn);
	}
	
	long getUserId() {
		return userId;
	}
    
    public Client getClient() {
        return client;
    }
	
	public JDA getJDA() {
        return getClient().getJDA();
    }
	
	public User getUser() {
		return getJDA().getUserById(String.valueOf(userId));
	}
	
	public TextChannel getTextChannel() {
		return getJDA().getTextChannelById(String.valueOf(channelId));
	}
	
	public Guild getGuild() {
		return getJDA().getGuildById(String.valueOf(guildId));
	}
	
	public long getExpiresIn() {
		return expiresIn;
	}
	
	public String[] getValidInputs() {
		return validInputs;
	}
	
	public ExpectedResponseType getExpectedResponseType() {
		return expectedResponseType;
	}
	
	public ResponseListener getResponseListener() {
		return responseListener;
	}
}
