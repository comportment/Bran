package br.net.brjdevs.steven.bran.core.data.managers;

import br.net.brjdevs.steven.bran.core.client.Bran;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import redis.clients.jedis.Jedis;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class GsonDataRedisManager<T> implements Supplier<T> {
    
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();
    private static ExecutorService service = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("GsonDataRedisManager [T%d]").build());
    private String key;
	private T data;
	
	public GsonDataRedisManager(Class<T> clazz, String key, Supplier<T> constructor) {
		this.key = key;
		try (Jedis jedis = Bran.getJedisPool().getResource()) {
            synchronized (GSON) {
                String s = jedis.get(key);
                if (s == null) {
                    jedis.set(key, s = GSON.toJson(constructor.get()));
                }
                data = GSON.fromJson(s, clazz);
            }
        } catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public T get() {
		return data;
	}
	
	public void update() {
        service.submit(() -> {
            try (Jedis jedis = Bran.getJedisPool().getResource()) {
                synchronized (GSON) {
                    jedis.set(key, GSON.toJson(data));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
