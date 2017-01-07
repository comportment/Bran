package br.com.brjdevs.bran.core.command;

import br.com.brjdevs.bran.Bot;
import br.com.brjdevs.bran.core.managers.TaskManager;
import br.com.brjdevs.bran.core.utils.Util;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class TooFast {
	private static final Map<String, JsonObject> userTimeout = new HashMap<>();
	private static boolean enabled = Bot.getInstance().getConfig().isTooFastEnabled();
	static {
			TaskManager.startAsyncTask(() -> {
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
	public static boolean checkCanExecute(CommandEvent event) {
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
	public static boolean isEnabled() {
		return enabled;
	}
	public static void setEnabled(boolean enabled) {
		TooFast.enabled = enabled;
	}
	private static JsonObject defaultObject() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("timeout", 0);
		jsonObject.addProperty("warned", false);
		return jsonObject;
	}
}
