package eu.spectrum.main;

import org.bukkit.Location;
import org.bukkit.Material;

public class Systems {

	public static Material[] checkpoint = new Material[] { Material.WOOD_PLATE, Material.GOLD_BLOCK };

	public static boolean isCheckpoint(Location loc) {
		return loc.getBlock().getType() == checkpoint[0]
				&& loc.getBlock().getLocation().subtract(0, 1, 0).getBlock().getType() == checkpoint[1];
	}

	public static final String defModuleName = "Modul Name";

	public static final String setUpFile = "setup.yml";
	public static final String[] locs = new String[] { "waiting_lobby", "ending_lobby" };

	public static final int MAX_PLAYERS = 3;
	public static final int MIN_PLAYERS = 2;

	public static final int maxLobbyCount = 40;
	public static final int gameSeconds = 2*60;
	public static final int MAX_LIFES = 3;
	
	public static final String PVP_PRESET_NAME = "pvp_preset";
	public static final String PVP_NAME = "pvp_name";
}
