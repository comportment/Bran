package br.com.brjdevs.steven.bran.core.utils;

import net.dv8tion.jda.core.requests.RestAction;

import java.util.function.Consumer;

public class RestActionSleep {
	private RestAction restAction;
	public RestActionSleep(RestAction restAction) {
		this.restAction = restAction;
	}
	public void sleepAndThen(long millis, Consumer<RestAction> callback) {
		Util.sleep(millis);
		callback.accept(restAction);
	}
}
