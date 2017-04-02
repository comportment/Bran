package br.net.brjdevs.steven.bran.core.client;

import br.net.brjdevs.steven.bran.core.utils.Utils;
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
        if (!Utils.isEmpty(Bran.getInstance().getConfig().redisPassword)) {
            try {
                jedis.ping();
            } catch (JedisDataException e) {
                jedis.auth(Bran.getInstance().getConfig().redisPassword);
            }
        }
        
        return jedis;
    }
}
