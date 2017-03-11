package br.com.brjdevs.steven.bran.core.data;

import br.com.brjdevs.steven.bran.core.data.managers.GsonDataFileManager;
import br.com.brjdevs.steven.bran.core.data.managers.GsonDataRedisManager;
import br.com.brjdevs.steven.bran.core.poll.Polls;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BranDataManager {
	
	private GsonDataRedisManager<DataHolder> dataHolderManager;
	private GsonDataFileManager<Config> configDataManager;
	private GsonDataRedisManager<Map<String, List<String>>> hangmanWordsManager;
	private GsonDataRedisManager<Polls> pollPersistence;
	
	public BranDataManager() {
		getUserDataManager();
		getConfigDataManager();
		getHangmanWordsManager();
		getPollPersistence();
	}
	
	public GsonDataRedisManager<DataHolder> getUserDataManager() {
		if (dataHolderManager == null)
			dataHolderManager = new GsonDataRedisManager<>(DataHolder.class, "dataholder.json", DataHolder::new);
		return dataHolderManager;
	}
	
	public GsonDataFileManager<Config> getConfigDataManager() {
		if (configDataManager == null)
			configDataManager = new GsonDataFileManager<>(Config.class, "config.json", Config::new);
		return configDataManager;
	}
	
	public GsonDataRedisManager<Map<String, List<String>>> getHangmanWordsManager() {
		if (hangmanWordsManager == null)
			hangmanWordsManager = (GsonDataRedisManager<Map<String, List<String>>>) new GsonDataRedisManager(HashMap.class, "hangmanwords.json", HashMap::new);
		return hangmanWordsManager;
	}
	
	public GsonDataRedisManager<Polls> getPollPersistence() {
		if (pollPersistence == null)
			pollPersistence = new GsonDataRedisManager<>(Polls.class, "pollpersistence", Polls::new);
		return pollPersistence;
	}
}
