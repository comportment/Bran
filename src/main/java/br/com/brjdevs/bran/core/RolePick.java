package br.com.brjdevs.bran.core;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.List;

public class RolePick {
	private static final List<RolePick> rolesPick;
	
	static {
		rolesPick = new ArrayList<>();
	}
	
	private String userId;
	private List<String> rolesId;
	private Message message;
	private String channelId;
	private int attempts;
	private RolePickAction action;
	
	public RolePick(User user, List<String> rolesId, Message message, RolePickAction action) {
		this.userId = user.getId();
		this.rolesId = rolesId;
		this.message = message;
		this.channelId = message.getTextChannel().getId();
		this.action = action;
		rolesPick.add(this);
	}
	public String getUserId() {
		return userId;
	}
	public List<String> getRolesId() {
		return rolesId;
	}
	public Message getMessage() {
		return message;
	}
	public String getChannelId() {
		return channelId;
	}
	public int getAttempts() {
		return attempts;
	}
	public void addAttempt() {
		attempts++;
	}
	public void remove() {
		rolesPick.remove(this);
	}
	public RolePickAction getAction() {
		return action;
	}
	
	public static RolePick getRolePick(User user) {
		return rolesPick.stream().filter(pick -> pick.getUserId().equals(user.getId()))
				.findFirst().orElse(null);
	}
	
	public enum RolePickAction {
		ADD_ROLE, REMOVE_ROLE, GIVE
	}
}
