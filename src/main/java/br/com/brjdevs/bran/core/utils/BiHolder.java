package br.com.brjdevs.bran.core.utils;

public class BiHolder<T, Z> {
	
	private T value1;
	private Z value2;
	
	public BiHolder() {}
	
	public T getFirst() {
		return value1;
	}
	public Z getSecond() {
		return value2;
	}
	public T setFirst(T value1) {
		this.value1 = value1;
		return value1;
	}
	public Z setSecond(Z value2) {
		this.value2 = value2;
		return value2;
	}
	
	public BiHolder(T value1, Z value2) {
		this.value1 = value1;
		this.value2 = value2;
	}
}
