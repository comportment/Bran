package br.com.brjdevs.steven.bran.core.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.dv8tion.jda.core.requests.RestAction;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class RestActionSleep {
	
	private static ExecutorService pool = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("RestActionSleep-%d").build());
	
	private RestAction restAction;
	public RestActionSleep(RestAction restAction) {
		this.restAction = restAction;
	}
	public void sleepAndThen(long millis, Consumer<RestAction> callback) {
		pool.submit(() -> {
			Utils.sleep(millis);
			callback.accept(restAction);
		});
	}
}
