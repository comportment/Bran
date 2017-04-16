package br.net.brjdevs.steven.bran.core.managers;

import br.com.brjdevs.java.utils.threads.ScheduledTaskProcessor;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import net.dv8tion.jda.core.entities.User;

public class RateLimiter {
	
	private static final ScheduledTaskProcessor TASK_PROCESSOR = new ScheduledTaskProcessor("RateLimiter");
	private final int timeout;
	private final TLongList usersRateLimited = new TLongArrayList();
	
	public RateLimiter(int timeout) {
		this.timeout = timeout;
	}
	
	public boolean process(long userId) {
		if (usersRateLimited.contains(userId)) return false;
		usersRateLimited.add(userId);
		TASK_PROCESSOR.addTask(System.currentTimeMillis() + timeout, () -> usersRateLimited.remove(userId));
		return true;
	}
	
	public boolean process(User user) {
		return process(user.getIdLong());
	}
    
    public TLongList getUsersRateLimited() {
        return usersRateLimited;
    }
}
