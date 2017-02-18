package br.com.brjdevs.steven.bran.core.responsewaiter.events;

import br.com.brjdevs.steven.bran.core.responsewaiter.ResponseWaiter;

public class ResponseTimeoutEvent extends ResponseEvent {
	
	public ResponseTimeoutEvent(ResponseWaiter responseWaiter) {
		super(responseWaiter);
	}
}
