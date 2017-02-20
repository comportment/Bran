package br.com.brjdevs.steven.bran.core.data;

import br.com.brjdevs.steven.bran.core.data.bot.Config;
import br.com.brjdevs.steven.bran.core.data.managers.GsonDataFileManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Data {
	
	private GsonDataFileManager<DataHolder> dataHolderManager;
	private GsonDataFileManager<Config> configDataManager;
	private GsonDataFileManager<Map<String, List<String>>> hangmanWordsManager;
	
	public Data() {
		getDataHolderManager();
		getConfigDataManager();
		getHangmanWordsManager();
	}
	
	public GsonDataFileManager<DataHolder> getDataHolderManager() {
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
}
