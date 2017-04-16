package br.net.brjdevs.steven.bran.core.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisDatabase {
    
    private static RedisDatabase db;
    
    public static RedisDatabase getDB() {
        if (db == null)
            db = new RedisDatabase();
        return db;
    }
    
    private JedisPool jedisPool;
    
    private RedisDatabase() {
         jedisPool = new JedisPool("localhost", 6379);
    }
    
    public Jedis getResource() {
        return jedisPool.getResource();
    }

    public JedisPool getPool() {
        return jedisPool;
    }
}
