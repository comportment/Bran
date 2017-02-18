package br.com.brjdevs.steven.bran.core.responsewaiter.events;

import br.com.brjdevs.steven.bran.core.responsewaiter.ResponseWaiter;

public class ValidResponseEvent extends ResponseEvent {
	
	public Object response;
	
	public ValidResponseEvent(ResponseWaiter responseWaiter, Object response) {
		super(responseWaiter);
		this.response = response;
	}
}
