package br.com.brjdevs.steven.bran.core.managers;

import br.com.brjdevs.steven.bran.core.utils.HttpUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class WeatherSearch {
	private static final String URL = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D\"{query}\")&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";
	public static JsonElement search(String query) {
		String url = URL.replace("{query}", query.replaceAll(" ", "%20"));
		FutureTask<Object> task = new FutureTask<>(() -> {
			try {
				return HttpUtils.read(url);
			} catch (Exception e) {
				return e;
			}
		});
		task.run();
		Object obj;
		try {
			obj = task.get(10, TimeUnit.SECONDS);
		} catch (TimeoutException | ExecutionException | InterruptedException e) {
			throw new RuntimeException("Yahoo API didn't respond.");
		}
		if (obj instanceof Exception) {
			throw new RuntimeException((Exception) obj);
		}
		return new JsonParser().parse((String) obj)
				.getAsJsonObject().get("query").getAsJsonObject()
				.get("results");
	}
}
