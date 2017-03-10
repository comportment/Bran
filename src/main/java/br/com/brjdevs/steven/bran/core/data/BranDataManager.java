package br.com.brjdevs.steven.bran.core.data;

import br.com.brjdevs.steven.bran.core.data.managers.GsonDataFileManager;
import br.com.brjdevs.steven.bran.core.poll.Polls;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BranDataManager {
	
	private GsonDataFileManager<DataHolder> dataHolderManager;
	private GsonDataFileManager<Config> configDataManager;
	private GsonDataFileManager<Map<String, List<String>>> hangmanWordsManager;
	private GsonDataFileManager<Polls> pollPersistence;
	
	public BranDataManager() {
		getUserDataManager();
		getConfigDataManager();
		getHangmanWordsManager();
		getPollPersistence();
	}
	
	public GsonDataFileManager<DataHolder> getUserDataManager() {
		if (dataHolderManager == null)
			dataHolderManager = new GsonDataFileManager<>(DataHolder.class, "dataholder.json", DataHolder::new);
		return dataHolderManager;
	}
	
	public GsonDataFileManager<Config> getConfigDataManager() {
		if (configDataManager == null)
			configDataManager = new GsonDataFileManager<>(Config.class, "config.json", Config::new);
		return configDataManager;
	}
	
	public GsonDataFileManager<Map<String, List<String>>> getHangmanWordsManager() {
		if (hangmanWordsManager == null)
			hangmanWordsManager = (GsonDataFileManager<Map<String, List<String>>>) new GsonDataFileManager(HashMap.class, "hangmanwords.json", HashMap::new);
		return hangmanWordsManager;
	}
	
	public GsonDataFileManager<Polls> getPollPersistence() {
		if (pollPersistence == null)
			pollPersistence = new GsonDataFileManager<>(Polls.class, "pollpersistence", Polls::new);
		return pollPersistence;
	}
}
