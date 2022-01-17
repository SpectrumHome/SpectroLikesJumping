package eu.spectrum.game.states;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import eu.realms.common.display.ScoreboardAPI.SidebarScoreboard;
import eu.spectrum.commands.SetupCommand;
import eu.spectrum.game.EnumGameState;
import eu.spectrum.game.GameHandler;
import eu.spectrum.game.GameState;
import eu.spectrum.game.PlayerData;
import eu.spectrum.main.Systems;
import eu.spectrum.main.Systems.GameLocation;

public class PVPState extends GameState {

	public PVPState() {
		super(EnumGameState.PVP);
	}

	@Override
	public void onStart(Player p) {
		try {
			Bukkit.unloadWorld(Systems.PVP_NAME, false);
			File pvpMap = new File(Systems.PVP_NAME);
			File pvpPreset = new File(Systems.PVP_PRESET_NAME);
			if (pvpMap.isDirectory()) {
				FileUtils.deleteDirectory(pvpMap);
			}
			if (!pvpPreset.isDirectory()) {
				System.err.println("PVP PRESET WAS NOT FOUND");
				return;
			}
			FileUtils.copyDirectory(pvpPreset, pvpMap);
			new WorldCreator(Systems.PVP_NAME).createWorld();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		SetupCommand.teleportAll(GameLocation.PVP);
		updateScoreboard();
	}

	public static int lastLines = 0;

	public static void updateScoreboard() {
		List<PlayerData> players = new ArrayList<>(GameHandler.playerData.values());
		players.sort((p1, p2) -> p2.lifes - p1.lifes);
		players.removeIf((p) -> !p.lives());
		int tmpLastLines = 0;
		for (PlayerData data : GameHandler.playerData.values()) {
			int startIndex = 0;

			for (int i = startIndex; i < lastLines; i++) {
				data.scoreboard.removeLine(i, data.p);
			}

			int currentLifes = Systems.MAX_LIFES + 1;
			for (PlayerData player : players) {
				if (player.getLifes() != currentLifes) {
					data.scoreboard.setLine(startIndex, "§" + startIndex + "  ", data.p);
					data.scoreboard.setLine(startIndex + 1, "§7»" + getTitle(player.getLifes()), data.p);
					currentLifes = player.getLifes();
					startIndex += 2;
				}
				
				data.scoreboard.setLine(startIndex,
						player.getTeam().getColor() + (data.p == player.p ? "§l" : "") + player.p.getName(), data.p);

				startIndex++;
			}

			tmpLastLines = startIndex;
		}
		PVPState.lastLines = tmpLastLines;
	}

	public static String getTitle(int lives) {
		String res = "";
		for (int i = 0; i < lives; i++) {
			res += "§c❤";
		}
		for (int i = 0; i < Systems.MAX_LIFES - lives; i++) {
			res += "§7❤";
		}
		return res;
	}

	@Override
	public void onStop(Player p) {

	}

	@Override
	public SidebarScoreboard getScoreboard(PlayerData data,SidebarScoreboard scoreboard) {
		return scoreboard;
	}

}
