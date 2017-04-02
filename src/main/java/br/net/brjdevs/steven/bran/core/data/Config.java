package br.net.brjdevs.steven.bran.core.data;

import java.util.ArrayList;
import java.util.List;

public class Config {
	
	public String jenkinsUser = "";
	public String jenkinsPass = "";
	public String jenkinsToken = "";
	public String jenkinsLatestBuild = "";
    public String redisPassword = null;
    public String botToken = "";
	public String ownerId = "";
	public String discordBotsToken = "";
	public String discordBotsOrgToken = "";
	public String mashapeKey = "";
	public String defaultGame = "";
	public boolean gameStream = false;
	public List<String> defaultPrefixes = new ArrayList<>();
}
