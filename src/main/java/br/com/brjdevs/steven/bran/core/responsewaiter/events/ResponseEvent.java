package br.com.brjdevs.steven.bran.core.responsewaiter.events;

import br.com.brjdevs.steven.bran.core.responsewaiter.ResponseWaiter;

public abstract class ResponseEvent {
	
	public ResponseWaiter responseWaiter;
	
	public ResponseEvent(ResponseWaiter responseWaiter) {
		this.responseWaiter = responseWaiter;
	}
}
