package eu.spectrum.game;

import org.bukkit.ChatColor;

public enum JumpTeam {

	FISCHIES("Fischies", ChatColor.BLUE), SPECTROS("Spectros", ChatColor.RED), KEKSE("Kekse", ChatColor.GOLD),
	BACKFISCHE("Backfische", ChatColor.AQUA);

	String name;
	ChatColor color;

	JumpTeam(String name, ChatColor color) {
		this.name = name;
		this.color = color;
	}
	
	public ChatColor getColor() {
		return color;
	}
	
	public String getName() {
		return name;
	}
	
}
