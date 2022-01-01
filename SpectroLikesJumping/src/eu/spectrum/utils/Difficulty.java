package eu.spectrum.utils;

import org.bukkit.ChatColor;

public enum Difficulty {

	EASY("Zu einfach bitch", ChatColor.GREEN, 13, 0), INTERMEDIATE("Bisl bessa", ChatColor.GOLD, 1, 1),
	HARD("sus ((⇀‸↼))", ChatColor.RED, 14, 2), BEYOND_HARD("BEYOND HARD", ChatColor.DARK_PURPLE, 10, 3);

	String name;
	ChatColor chatColor;
	int subColorID;
	int difficulty;

	Difficulty(String name, ChatColor chatColor, int subColorID, int difficulty) {
		this.name = name;
		this.chatColor = chatColor;
		this.subColorID = subColorID;
		this.difficulty = difficulty;
	}

	public int getDifficulty() {
		return difficulty;
	}

	public String getName() {
		return name;
	}

	public ChatColor getChatColor() {
		return chatColor;
	}

	public int getSubColorID() {
		return subColorID;
	}
}
