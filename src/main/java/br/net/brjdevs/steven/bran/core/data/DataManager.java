package br.net.brjdevs.steven.bran.core.data;

import br.net.brjdevs.steven.bran.core.data.managers.GsonDataFileManager;
import br.net.brjdevs.steven.bran.core.data.managers.RedisDataManager;
import br.net.brjdevs.steven.bran.core.poll.Polls;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataManager {
	
	private RedisDataManager<DataHolder> dataHolderManager;
	private GsonDataFileManager<Config> configDataManager;
	private RedisDataManager<Map<String, List<String>>> hangmanWordsManager;
	private RedisDataManager<Polls> pollPersistence;
	
	public DataManager() {
        getConfig();
    }
    
    public RedisDataManager<DataHolder> getData() {
        if (dataHolderManager == null)
			dataHolderManager = new RedisDataManager<>(DataHolder.class, "dataholder.json", DataHolder::new);
		return dataHolderManager;
	}
    
    public GsonDataFileManager<Config> getConfig() {
        if (configDataManager == null)
			configDataManager = new GsonDataFileManager<>(Config.class, "config.json", Config::new);
		return configDataManager;
	}
    
    public RedisDataManager<Map<String, List<String>>> getHangmanWords() {
        if (hangmanWordsManager == null)
			hangmanWordsManager = (RedisDataManager<Map<String, List<String>>>) new RedisDataManager(HashMap.class, "hangmanwords.json", HashMap::new);
		return hangmanWordsManager;
	}
    
    public RedisDataManager<Polls> getPolls() {
        if (pollPersistence == null)
			pollPersistence = new RedisDataManager<>(Polls.class, "pollpersistence", Polls::new);
		return pollPersistence;
	}
}
