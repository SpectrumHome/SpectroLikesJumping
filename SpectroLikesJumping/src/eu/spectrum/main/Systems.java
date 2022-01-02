package eu.spectrum.main;

import org.bukkit.Location;
import org.bukkit.Material;

public class Systems {
	
	public static boolean isCheckpoint(Location loc) {
		return loc.getBlock().getType() == Material.WOOD_PLATE
				&& loc.getBlock().getLocation().subtract(0, 1, 0).getBlock().getType() == Material.GOLD_BLOCK;
	}
	
	public static final String defModuleName = "Modul Name";
	
	public static final String setUpFile = "setup.yml";
	public static final String[] locs = new String[] { "waiting_lobby", "ending_lobby" };

	public static final int MAX_PLAYERS = 3;
	public static final int MIN_PLAYERS = 2;

	public static final int maxLobbyCount = 40;
}