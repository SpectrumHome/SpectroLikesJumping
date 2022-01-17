package eu.spectrum.game.states;

import static eu.spectrum.main.Systems.MIN_PLAYERS;
import static eu.spectrum.main.Systems.maxLobbyCount;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import eu.realms.common.display.ScoreboardAPI.SidebarScoreboard;
import eu.realms.common.display.TitleAPI;
import eu.spectrum.game.EnumGameState;
import eu.spectrum.game.GameHandler;
import eu.spectrum.game.GameState;
import eu.spectrum.game.PlayerData;
import eu.spectrum.game.TeamHandler;
import eu.spectrum.main.Main;

public class LobbyState extends GameState {

	public static boolean startCountdown = false;
	public static int waitTaskID = 0;
	public static int startCount;
	
	public LobbyState() {
		super(EnumGameState.LOBBY);
	}

	@Override
	public void onStart(Player caller) {
		GameHandler.resetScoreboards();
		for (Player p1 : Bukkit.getOnlinePlayers()) {
			p1.spigot().setCollidesWithEntities(true);
			p1.setGameMode(GameMode.SURVIVAL);
			p1.setAllowFlight(false);
			
			PlayerData data;
			GameHandler.playerData.put(p1, data = new PlayerData(p1));
			data.changeTeam(TeamHandler.findEmptyTeam());
			
			for (PotionEffect effect : p1.getActivePotionEffects())
				p1.removePotionEffect(effect.getType());
			for (Player p2 : Bukkit.getOnlinePlayers())
				if (p2 != p1)
					p1.showPlayer(p2);
		}
	}
	
	public static void startCountdown(Player caller) {
		startCount = maxLobbyCount;
		if (GameHandler.gameState == EnumGameState.LOBBY && !startCountdown) {
			startCountdown = true;
			waitTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), () -> {
				levelDisplay(startCount, maxLobbyCount);
				switch (startCount) {
				case 40:
				case 20:
					checkCountdownState(null);
				case 10:
					TitleAPI.broadcastTitle("§a" + startCount, "", 4, 13, 2);
					break;
				case 5:
				case 4:
					TitleAPI.broadcastTitle("§6" + startCount, "", 4, 13, 2);
					break;
				case 3:
				case 2:
				case 1:
					TitleAPI.broadcastTitle("§c" + startCount, "", 4, 13, 2);
					break;

				case 0:
					stopCountdown(caller);
					GameHandler.changeGameState(EnumGameState.INGAME, caller);
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

	@Override
	public void onStop(Player p) {
		if(startCountdown) stopCountdown(p);
	}

	@Override
	public SidebarScoreboard getScoreboard(PlayerData data,SidebarScoreboard scoreboard) {
		return scoreboard;
	}

}
