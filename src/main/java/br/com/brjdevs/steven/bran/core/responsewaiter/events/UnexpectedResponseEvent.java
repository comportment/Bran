package br.com.brjdevs.steven.bran.core.responsewaiter.events;

import br.com.brjdevs.steven.bran.core.responsewaiter.ResponseWaiter;

public class UnexpectedResponseEvent extends ResponseEvent {
	
	public Object response;
	
	public UnexpectedResponseEvent(ResponseWaiter responseWaiter, Object response) {
		super(responseWaiter);
		this.response = response;
	}
}
