package br.com.brjdevs.steven.bran.core.managers;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolHelper {
	
	private static final ThreadPoolHelper threadPoolHelper = new ThreadPoolHelper();
	private final ThreadPoolExecutor executor = new ThreadPoolExecutor(0, 25, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
	
	public static ThreadPoolHelper getDefaultThreadPool() {
		return threadPoolHelper;
	}
	
	public ThreadPoolExecutor getThreadPool() {
		return executor;
	}
	
	public void purge() {
		executor.purge();
	}
	
	public void startThread(String task, Runnable thread) {
		executor.execute(thread);
	}
	
	public void startThread(String task, ThreadPoolExecutor exec, Runnable thread) {
		exec.execute(thread);
	}
}
