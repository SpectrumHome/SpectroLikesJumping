package eu.spectrum.game.states;

import static eu.spectrum.game.GameHandler.playerData;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import eu.realms.common.display.ScoreboardAPI.SidebarScoreboard;
import eu.realms.common.display.TitleAPI;
import eu.spectrum.game.EnumGameState;
import eu.spectrum.game.GameHandler;
import eu.spectrum.game.GameState;
import eu.spectrum.game.PlayerData;
import eu.spectrum.main.Main;
import eu.spectrum.main.Systems;
import eu.spectrum.utils.ModuleData;
import eu.spectrum.utils.ModuleManager;

public class IngameState extends GameState {

	public static int gameTaskID = 0;
	private static int ingameTime;
	public static List<ModuleData> gameModules = new ArrayList<ModuleData>();

	public IngameState() {
		super(EnumGameState.INGAME);
	}

	public static int getIngameTime() {
		return ingameTime;
	}

	public static void setIngameTime(int ingameTime) {
		IngameState.ingameTime = ingameTime;
	}

	@Override
	public void onStart(Player caller) {
		if (Main.loadingWorld) {
			caller.sendMessage(Main.PREFIX + Main.handler.format("game.world.loading"));
			return;
		}
		List<ModuleData> modules = ModuleManager.loadModules();
		if (modules.size() <= 0) {
			caller.sendMessage(Main.PREFIX + "§cEs existieren keine Module.");
			return;
		}
		gameModules.clear();
		gameModules = modules;

		for (Player p : Bukkit.getOnlinePlayers()) {
			p.setGameMode(GameMode.SURVIVAL);
			p.setHealth(20);
			p.setFoodLevel(20);
			p.setSaturation(20);
			p.getInventory().clear();
			p.setLevel(0);
			p.setExp(0);
		}

		startIngameTimer();
		generateModules();
		
		updateContestantView();
	}

	public static void updateCurrModuleView(Player p) {
		PlayerData data = playerData.get(p);
		ModuleData currModule = gameModules.get(data.currentModule);
		data.scoreboard.setLine(2, "  §7» " + currModule.difficulty.getChatColor() + currModule.name, p);
	}

	public static void applyToAllScoreboards(int i, String line) {
		for (PlayerData data : playerData.values()) {
			data.scoreboard.setLine(i, line, data.p);
		}
	}

	public static void updateContestantView() {
		int startIndex = 4;

		List<PlayerData> contestants = new ArrayList<PlayerData>(playerData.values());
		contestants.sort((c1, c2) -> c2.currentModule - c1.currentModule);

		int count = 0;
		for (PlayerData data : contestants) {
			for (PlayerData targets : playerData.values()) {
				String pref = targets.p == data.p ? "§l" : "";
				targets.scoreboard.setLine(startIndex + count, data.getTeam().getColor() + pref + data.p.getName()
						+ "§7 (§a" + (data.currentModule + 1) + "§7/" + gameModules.size() + ")", targets.p);
			}
			count++;
		}
	}

	private static void startIngameTimer() {
		ingameTime = Systems.gameSeconds;

		gameTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), () -> {
			ingameTime--;
			if (ingameTime <= 0) {
				GameHandler.changeGameState(EnumGameState.PVP);
				Bukkit.getScheduler().cancelTask(gameTaskID);
			} else {
				Bukkit.getOnlinePlayers().forEach(p -> TitleAPI.sendActionBar(p, getFormattedTime()));
			}
		}, 20, 20);
	}

	public static String getFormattedTime() {
		int mins = (int) Math.floor(ingameTime / 60d);
		String seconds = "" + ingameTime % 60;

		return "§a" + mins + (seconds.length() > 1 ? ":" : ":0") + seconds;
	}

	private static void generateModules() {

		Vector totalSize = ModuleManager.getSize(gameModules);

		int count = 0;
		for (Player p : Bukkit.getOnlinePlayers()) {
			Location loc = new Location(Main.getInstance().getWorld(), 0d, 100d, (totalSize.getZ() + 20) * count);
			GameHandler.playerData.get(p).start = loc;
			spawnNextModule(p);
			count++;
		}
	}

	public static void spawnNextModule(Player p) {
		if (!GameHandler.playerData.containsKey(p))
			return;
		PlayerData data = GameHandler.playerData.get(p);

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

	@Override
	public void onStop(Player caller) {
		GameHandler.resetScoreboards();
	}

	@Override
	public SidebarScoreboard getScoreboard(PlayerData data,SidebarScoreboard sideScoreboard) {
		sideScoreboard.setLine(0, "  ", data.p);
		sideScoreboard.setLine(1, "§7Du spielst", data.p);
		updateCurrModuleView(data.p);
		sideScoreboard.setLine(3, "§7 ", data.p);
		return sideScoreboard;
	}

}
