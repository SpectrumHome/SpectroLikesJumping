package eu.spectrum.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import eu.spectrum.main.Main;
import eu.spectrum.utils.ModuleData;
import eu.spectrum.utils.ModuleManager;
import eu.spectrum.utils.TitleAPI;
import static eu.spectrum.main.Systems.*;

public class GameHandler {

	public static GameState gameState = GameState.LOBBY;

	public static boolean startCountdown = false;
	public static int taskID = 0;
	public static int startCount;

	public static List<ModuleData> gameModules = new ArrayList<ModuleData>();
	public static HashMap<Player, PlayerData> playerData = new HashMap<>();

	public static void startGameTimer(Player caller) {
		startCount = maxLobbyCount;
		if (gameState == GameState.LOBBY && !startCountdown) {
			startCountdown = true;
			taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), () -> {
				levelDisplay(startCount, maxLobbyCount);
				switch (startCount) {
				case 40:
				case 20:
					checkCountdownState(null);
				case 10:
					TitleAPI.broadcastTitle("ï¿½a" + startCount, "", 4, 13, 2);
					break;
				case 5:
				case 4:
					TitleAPI.broadcastTitle("ï¿½6" + startCount, "", 4, 13, 2);
					break;
				case 3:
				case 2:
				case 1:
					TitleAPI.broadcastTitle("ï¿½c" + startCount, "", 4, 13, 2);
					break;

				case 0:
					stopGameTimer(caller);
					startGame(caller);
					break;
				default:
					break;
				}
				startCount--;
			}, 20, 20);
		} else {
			caller.sendMessage(Main.PREFIX + Main.handler.format("game.running"));
		}
	}

	public static void stopGameTimer(Player caller) {
		if (startCountdown) {
			startCountdown = false;
			Bukkit.getScheduler().cancelTask(taskID);
		} else
			caller.sendMessage(Main.PREFIX + Main.handler.format("game.countdown.absent"));
	}

	public static void checkCountdownState(Integer players) {
		if (players == null)
			players = Bukkit.getOnlinePlayers().size();
		if (players >= (MIN_PLAYERS - 1) && !startCountdown) {
			startGameTimer(null);
		} else if (players < MIN_PLAYERS && startCountdown) {
			stopGameTimer(null);
		}
	}

	public static void levelDisplay(int value, int max) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.setLevel(value);
			p.setExp((float) value / max);
		}
	}

	public static void startGame(Player caller) {
		if (gameState == GameState.LOBBY) {
			if (startCountdown)
				stopGameTimer(caller);
			if (Main.getInstance().loadingWorld) {
				caller.sendMessage(Main.PREFIX + Main.handler.format("game.world.loading"));
				return;
			}
			List<ModuleData> modules = ModuleManager.loadModules();
			if (modules.size() <= 0) {
				caller.sendMessage(Main.PREFIX + "§cEs existieren keine Module.");
				return;
			}
			gameState = GameState.INGAME;
			gameModules.clear();
			gameModules = modules;

			Vector totalSize = ModuleManager.getSize(gameModules);

			int count = 0;
			for (Player p : Bukkit.getOnlinePlayers()) {
				Location loc = new Location(Main.getInstance().getWorld(), 0d, 100d, (totalSize.getZ() + 20) * count);
				playerData.put(p, new PlayerData(loc));
				spawnNextModule(p);
				count++;
			}

		} else {
			caller.sendMessage(Main.PREFIX + Main.handler.format("game.running"));
		}
	}

	public static void spawnNextModule(Player p) {
		if (!playerData.containsKey(p))
			return;
		PlayerData data = playerData.get(p);

		if (data.currentModule >= 0) {
			ModuleData currModule = gameModules.get(data.currentModule);
			currModule.remove(data.getStart());
		}

		Location start = data.currentModule > -1 ? p.getLocation() : data.start;
		data.currentModule++;
		Location moduleStart = gameModules.get(data.currentModule).getStart();
		start.setYaw(moduleStart.getYaw());
		start.setPitch(moduleStart.getPitch());
		start.add(0.5, 0, 0.5);
		data.setStart(start);

		ModuleManager.paste(start, gameModules.get(data.currentModule).name);

		Location correctPos = start.clone();
		if (data.currentModule <= 0) {
			correctPos.setY(data.start.getY() + 2);
		}
		p.teleport(correctPos);

	}

}
