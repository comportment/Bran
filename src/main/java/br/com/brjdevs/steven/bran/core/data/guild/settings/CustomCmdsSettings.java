package br.com.brjdevs.steven.bran.core.data.guild.settings;

import br.com.brjdevs.steven.bran.core.managers.CustomCommand;

import java.util.HashMap;
import java.util.Map;

public class CustomCmdsSettings {
	
	public boolean isEnabled;
	private Map<String, CustomCommand> customCommands;
	
	public CustomCmdsSettings() {
		this.isEnabled = true;
		this.customCommands = new HashMap<>();
	}
	
	public Map<String, CustomCommand> asMap() {
		return customCommands;
	}
	
	public boolean hasCustomCommand(String name) {
		return customCommands.containsKey(name);
	}
	
	public CustomCommand getCustomCommand(String name) {
		return customCommands.get(name);
	}
	
	public void rename(String oldName, String newName) {
		CustomCommand cmd = getCustomCommand(oldName);
		customCommands.remove(oldName, cmd);
		customCommands.put(newName, cmd);
	}
	
	public boolean check() {
		return isEnabled && !customCommands.isEmpty();
	}
}