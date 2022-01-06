package eu.spectrum.game;

import static eu.spectrum.main.Systems.MIN_PLAYERS;
import static eu.spectrum.main.Systems.maxLobbyCount;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import eu.spectrum.commands.SetupCommand;
import eu.spectrum.main.Main;
import eu.spectrum.main.Systems;
import eu.spectrum.main.Systems.GameLocation;
import eu.spectrum.utils.ModuleData;
import eu.spectrum.utils.ModuleManager;
import eu.spectrum.utils.TitleAPI;

public class GameHandler {

	public static GameState gameState = GameState.LOBBY;

	public static boolean startCountdown = false;
	public static int waitTaskID = 0;
	public static int gameTaskID = 0;
	public static int startCount;

	public static List<ModuleData> gameModules = new ArrayList<ModuleData>();
	public static HashMap<Player, PlayerData> playerData = new HashMap<>();

	public static int ingameTime;

	/*
	 * INIT METHODS Methods that initialize another game state
	 */

	public static void startGame(Player caller) {
		if (gameState == GameState.LOBBY) {
			if (startCountdown)
				stopCountdown(caller);
			if (Main.loadingWorld) {
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

			for (Player p : Bukkit.getOnlinePlayers()) {
				p.setGameMode(GameMode.SURVIVAL);
				p.setHealth(20);
				p.setFoodLevel(20);
				p.setSaturation(20);
			}

			startIngameTimer();
			generateModules();

		} else {
			caller.sendMessage(Main.PREFIX + Main.handler.format("game.running"));
		}
	}

	private static void startPVP() {
		try {

			gameState = GameState.PVP;
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

	}

	/*
	 * COUNTDOWN LOGIC The countdown is the timer counting down in the waiting lobby
	 */

	public static void startCountdown(Player caller) {
		startCount = maxLobbyCount;
		if (gameState == GameState.LOBBY && !startCountdown) {
			startCountdown = true;
			waitTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), () -> {
				levelDisplay(startCount, maxLobbyCount);
				switch (startCount) {
				case 40:
				case 20:
					checkCountdownState(null);
				case 10:
					TitleAPI.broadcastTitle("�a" + startCount, "", 4, 13, 2);
					break;
				case 5:
				case 4:
					TitleAPI.broadcastTitle("�6" + startCount, "", 4, 13, 2);
					break;
				case 3:
				case 2:
				case 1:
					TitleAPI.broadcastTitle("�c" + startCount, "", 4, 13, 2);
					break;

				case 0:
					stopCountdown(caller);
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

	public static void stopCountdown(Player caller) {
		if (startCountdown) {
			startCountdown = false;
			Bukkit.getScheduler().cancelTask(waitTaskID);
		} else
			caller.sendMessage(Main.PREFIX + Main.handler.format("game.countdown.absent"));
	}

	public static void checkCountdownState(Integer players) {
		if (players == null)
			players = Bukkit.getOnlinePlayers().size();
		if (players >= (MIN_PLAYERS - 1) && !startCountdown) {
			startCountdown(null);
		} else if (players < MIN_PLAYERS && startCountdown) {
			stopCountdown(null);
		}
	}

	public static void levelDisplay(int value, int max) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.setLevel(value);
			p.setExp((float) value / max);
		}
	}

	/*
	 * TIMER LOGIC Controls the game timer
	 */

	private static void startIngameTimer() {
		ingameTime = Systems.gameSeconds;

		gameTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), () -> {
			ingameTime--;
			if (ingameTime <= 0) {
				startPVP();
				Bukkit.getScheduler().cancelTask(gameTaskID);
			} else {
				Bukkit.getOnlinePlayers().forEach(p -> TitleAPI.sendActionBar(p, getFormattedTime()));
			}
		}, 20, 20);
	}

	public static String getFormattedTime() {
		int mins = (int) Math.floor(ingameTime / 60d);
		String seconds = "" + ingameTime % 60;

		return "a" + mins + (seconds.length() > 1 ? ":" : ":0") + seconds;
	}

	/*
	 * MODULE LOGIC Generates and removes modules as the players progress in the
	 * game
	 */

	private static void generateModules() {

		Vector totalSize = ModuleManager.getSize(gameModules);

		int count = 0;
		for (Player p : Bukkit.getOnlinePlayers()) {
			Location loc = new Location(Main.getInstance().getWorld(), 0d, 100d, (totalSize.getZ() + 20) * count);
			playerData.get(p).start = loc;
			spawnNextModule(p);
			count++;
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
