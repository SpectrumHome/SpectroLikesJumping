package eu.spectrum.main;

import org.bukkit.Location;
import org.bukkit.Material;

public class Systems {
	
	public static final int maxTeamPlayers = 1;

	public static Material[] checkpoint = new Material[] { Material.WOOD_PLATE, Material.GOLD_BLOCK };

	public static boolean isCheckpoint(Location loc) {
		return loc.getBlock().getType() == checkpoint[0]
				&& loc.getBlock().getLocation().subtract(0, 1, 0).getBlock().getType() == checkpoint[1];
	}

	public static final String defModuleName = "Modul Name";

	public static final String setUpFile = "setup.yml";

	public static enum GameLocation {
		WAITING_LOBBY(false), ENDING_LOBBY(false), PVP(true);

		boolean array;

		GameLocation(boolean array) {
			this.array = array;
		}

		public boolean isArray() {
			return array;
		}
		
		public static String[] toArray() {
			String[] arr = new String[GameLocation.values().length];
			for(int i = 0;i<arr.length;i++) arr[i] = GameLocation.values()[i].toString();
			return arr;
		}
	}

	public static final int MAX_PLAYERS = 3;
	public static final int MIN_PLAYERS = 2;

	public static final int maxLobbyCount = 40;
	public static final int gameSeconds = 2 * 60;
	public static final int MAX_LIFES = 3;
	
	public static final double ALLOWED_RESPAWN_ENIMY_DISTANCE = 20;

	public static final String PVP_PRESET_NAME = "pvp_preset";
	public static final String PVP_NAME = "pvp_name";
}
