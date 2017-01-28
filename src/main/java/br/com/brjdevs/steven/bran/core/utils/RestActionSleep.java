package br.com.brjdevs.steven.bran.core.utils;

import net.dv8tion.jda.core.requests.RestAction;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class RestActionSleep {
	
	private static ExecutorService pool = Executors.newSingleThreadExecutor();
	
	private RestAction restAction;
	public RestActionSleep(RestAction restAction) {
		this.restAction = restAction;
	}
	public void sleepAndThen(long millis, Consumer<RestAction> callback) {
		pool.submit(() -> {
			Util.sleep(millis);
			callback.accept(restAction);
		});
	}
}
