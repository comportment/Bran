package br.net.brjdevs.steven.bran.core.managers;

import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.List;

public class RateLimiter {
	
	private static final ExpirationManager EXPIRATION = new ExpirationManager();
	private final int timeout;
	private final List<String> usersRateLimited = new ArrayList<>();
	
	public RateLimiter(int timeout) {
		this.timeout = timeout;
	}
	
	public boolean process(String userId) {
		if (usersRateLimited.contains(userId)) return false;
		usersRateLimited.add(userId);
		EXPIRATION.letExpire(System.currentTimeMillis() + timeout, () -> usersRateLimited.remove(userId));
		return true;
	}
	
	public boolean process(User user) {
		return process(user.getId());
	}
	
}
