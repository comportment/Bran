package br.net.brjdevs.steven.bran.core.client;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisDataException;

public class BranJedisPool extends JedisPool {
    
    public BranJedisPool(String host, int port) {
        super(host, port);
    }
    
    @Override
    public Jedis getResource() {
        Jedis jedis = super.getResource();
        jedis.setDataSource(this);
        try {
            jedis.ping();
        } catch (JedisDataException e) {
        }
        
        return jedis;
    }
}
