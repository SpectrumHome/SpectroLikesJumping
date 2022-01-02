package eu.spectrum.utils;

import org.bukkit.ChatColor;

import eu.spectrum.main.Main;

public enum Difficulty {

	EASY("difficulty.easy", ChatColor.GREEN, 13, 0), INTERMEDIATE("difficulty.intermediate", ChatColor.GOLD, 1, 1),
	HARD("difficulty.hard", ChatColor.RED, 14, 2), BEYOND_HARD("difficulty.beyond-hard", ChatColor.DARK_PURPLE, 10, 3);

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
		return Main.handler.format(name);
	}

	public ChatColor getChatColor() {
		return chatColor;
	}

	public int getSubColorID() {
		return subColorID;
	}
}
