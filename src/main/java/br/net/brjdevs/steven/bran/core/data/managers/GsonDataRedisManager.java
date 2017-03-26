package br.net.brjdevs.steven.bran.core.data.managers;

import br.net.brjdevs.steven.bran.core.client.Bran;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.core.utils.SimpleLog;
import redis.clients.jedis.Jedis;

import java.util.function.Supplier;

public class GsonDataRedisManager<T> implements Supplier<T> {
	
	private static final Gson GSON = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
	private static final SimpleLog LOG = SimpleLog.getLog("GsonDataRedisManager");
	
	private String key;
	private T data;
	
	public GsonDataRedisManager(Class<T> clazz, String key, Supplier<T> constructor) {
		this.key = key;
		try (Jedis jedis = Bran.getJedisPool().getResource()) {
			String s = jedis.get(key);
			if (s == null) {
				jedis.set(key, s = GSON.toJson(constructor.get()));
			}
			data = GSON.fromJson(s, clazz);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public T get() {
		return data;
	}
	
	public void update() {
		try (Jedis jedis = Bran.getJedisPool().getResource()) {
			jedis.set(key, GSON.toJson(data));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
