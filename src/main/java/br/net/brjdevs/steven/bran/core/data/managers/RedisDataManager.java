package br.net.brjdevs.steven.bran.core.data.managers;

import br.net.brjdevs.steven.bran.core.redis.RedisDatabase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import redis.clients.jedis.Jedis;

import java.util.function.Supplier;

public class RedisDataManager<T> implements Supplier<T> {

    private final Gson GSON = new GsonBuilder().serializeNulls().create();
    private String key;
	private T data;
	
	public RedisDataManager(Class<T> clazz, String key, Supplier<T> constructor) {
        this.key = key;
        try (Jedis jedis = RedisDatabase.getDB().getResource()) {
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
        try (Jedis jedis = RedisDatabase.getDB().getResource()) {
            jedis.set(key, GSON.toJson(data));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
