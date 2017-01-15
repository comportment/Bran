package br.com.brjdevs.steven.bran.core.command.enums;

public enum Category {
	MISCELLANEOUS("Miscellaneous", "\uD83D\uDD2E"),
	FUN("Fun", "\uD83D\uDD79"),
	GUILD_ADMINISTRATOR("Guild Administrator", "\uD83D\uDDA5"),
	BOT_ADMINISTRATOR("Bot Administrator", "\u2699"),
	INFORMATIVE("Informative", "\uD83C\uDF10"),
	UNKNOWN("Unknown", "\u2049");
	private String key;
	private String emoji;
	Category(String key, String emoji) {
		this.key = key;
		this.emoji = emoji;
	}

	public static Category getCategoryByKey(String key) {
		for (Category commandCategory : Category.values())
			if (key.equals(commandCategory.key)) return commandCategory;
		return UNKNOWN;
	}
	
	public static Category getCategoryByEmoji(String emoji) {
		for (Category commandCategory : Category.values())
			if (emoji.equals(commandCategory.emoji)) return commandCategory;
		return UNKNOWN;
	}
	
	public String getKey() {
		return key;
	}
	
	public String getEmoji() {
		return emoji;
	}
}
