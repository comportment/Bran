package br.com.brjdevs.steven.bran.core.command;

import br.com.brjdevs.steven.bran.BotContainer;
import br.com.brjdevs.steven.bran.core.utils.Util;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class TooFast {
	
	private final Map<String, JsonObject> userTimeout = new HashMap<>();
	private BotContainer container;
	private boolean enabled;
	
	public TooFast(BotContainer container) {
		this.container = container;
		enabled = container.config.isTooFastEnabled();
		container.taskManager.startAsyncTask(() -> {
			synchronized (userTimeout) {
				userTimeout.replaceAll((user, json) -> {
					int count = Math.max(0, json.get("timeout").getAsInt() - 1);
					json.addProperty("timeout", count);
					if (count == 0 && json.get("warned").getAsBoolean())
						json.addProperty("warned", false);
					return json;
				});
			}
		}, 5);
	}
	
	private static JsonObject defaultObject() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("timeout", 0);
		jsonObject.addProperty("warned", false);
		return jsonObject;
	}
	
	public boolean checkCanExecute(CommandEvent event) {
		JsonObject jsonObject;
		int count;
		synchronized (userTimeout) {
			jsonObject = userTimeout.getOrDefault(event.getAuthor().getId(), defaultObject());
			jsonObject.addProperty("timeout", jsonObject.get("timeout").getAsInt() + 1);
			userTimeout.put(event.getAuthor().getId(), jsonObject);
			count = jsonObject.get("timeout").getAsInt();
		}
		if (count + 1 < 5) return true;
		if (!jsonObject.get("warned").getAsBoolean()) {
			event.sendMessage("***" + Util.getUser(event.getAuthor()) + "***, *you're running commands too fast please slow down!*").queue();
			jsonObject.addProperty("warned", true);
		}
		return false;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
