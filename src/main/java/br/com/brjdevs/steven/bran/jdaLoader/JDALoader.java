package br.com.brjdevs.steven.bran.jdaLoader;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import javax.security.auth.login.LoginException;
import java.util.Map;

public interface JDALoader {
	LoaderType getType();
	Map<Integer, JDA> build(boolean isComplete, int shards)
			throws LoginException, RateLimitedException;
	
}
