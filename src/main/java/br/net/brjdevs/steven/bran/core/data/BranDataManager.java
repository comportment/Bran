package br.net.brjdevs.steven.bran.core.data;

import br.net.brjdevs.steven.bran.core.data.managers.GsonDataFileManager;
import br.net.brjdevs.steven.bran.core.data.managers.GsonDataRedisManager;
import br.net.brjdevs.steven.bran.core.poll.Polls;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BranDataManager {
	
	private GsonDataRedisManager<DataHolder> dataHolderManager;
	private GsonDataFileManager<Config> configDataManager;
	private GsonDataRedisManager<Map<String, List<String>>> hangmanWordsManager;
	private GsonDataRedisManager<Polls> pollPersistence;
	
	public BranDataManager() {
        getData();
        getConfig();
        getHangmanWords();
        getPolls();
    }
    
    public GsonDataRedisManager<DataHolder> getData() {
        if (dataHolderManager == null)
			dataHolderManager = new GsonDataRedisManager<>(DataHolder.class, "dataholder.json", DataHolder::new);
		return dataHolderManager;
	}
    
    public GsonDataFileManager<Config> getConfig() {
        if (configDataManager == null)
			configDataManager = new GsonDataFileManager<>(Config.class, "config.json", Config::new);
		return configDataManager;
	}
    
    public GsonDataRedisManager<Map<String, List<String>>> getHangmanWords() {
        if (hangmanWordsManager == null)
			hangmanWordsManager = (GsonDataRedisManager<Map<String, List<String>>>) new GsonDataRedisManager(HashMap.class, "hangmanwords.json", HashMap::new);
		return hangmanWordsManager;
	}
    
    public GsonDataRedisManager<Polls> getPolls() {
        if (pollPersistence == null)
			pollPersistence = new GsonDataRedisManager<>(Polls.class, "pollpersistence", Polls::new);
		return pollPersistence;
	}
}
