package br.net.brjdevs.steven.bran.core.responsewaiter.events;

import br.net.brjdevs.steven.bran.core.responsewaiter.ResponseWaiter;

public class ResponseTimeoutEvent extends ResponseEvent {
	
	public ResponseTimeoutEvent(ResponseWaiter responseWaiter) {
		super(responseWaiter);
	}
}
