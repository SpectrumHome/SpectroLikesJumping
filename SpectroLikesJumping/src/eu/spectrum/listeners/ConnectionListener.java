package eu.spectrum.listeners;

import java.util.ArrayList;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import eu.spectrum.commands.SetupCommand;
import eu.spectrum.game.GameHandler;
import eu.spectrum.game.GameState;
import eu.spectrum.game.PlayerData;
import eu.spectrum.main.Main;
import eu.spectrum.main.Systems;
import eu.spectrum.main.Systems.GameLocation;
import eu.spectrum.utils.ScoreboardAPI.ScoreTeam;
import eu.spectrum.utils.ScoreboardAPI.TablistScoreboard;
import eu.spigotui.ui.SpigotUI;
import eu.spigotui.utils.ItemBuilder;

public class ConnectionListener implements Listener {

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		e.setJoinMessage(Main.handler.format("game.player.joined", p.getName()));
		SetupCommand.teleport(p, GameLocation.WAITING_LOBBY);
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
			GameHandler.checkCountdownState(null);
		}, 20);
		if (!GameHandler.playerData.containsKey(p))
			GameHandler.playerData.put(p, new PlayerData());

		ItemStack teamSelector = new ItemBuilder(Material.PAPER).setName("$aTeamselector").build();

		SpigotUI.addClickListener(teamSelector, (action, player) -> player.sendMessage(action.toString()));
		p.getInventory().clear();
		p.getInventory().addItem(teamSelector);
		
		scoreboard();
	}

	@EventHandler
	public void onPreConnect(AsyncPlayerPreLoginEvent e) {
		if (Bukkit.getOnlinePlayers().size() >= Systems.MAX_PLAYERS)
			e.disallow(Result.KICK_FULL, Main.handler.format("game.full"));
		else if (GameHandler.gameState != GameState.LOBBY)
			e.disallow(Result.KICK_FULL, Main.handler.format("game.running"));

	}

	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		GameHandler.checkCountdownState(Bukkit.getOnlinePlayers().size() - 1);
		e.setQuitMessage(Main.handler.format("game.player.left", p.getName()));
		if (GameHandler.playerData.containsKey(p))
			GameHandler.playerData.remove(p);
	}

	public void sortTeams() {
		for (ScoreTeam team : Main.teams.values()) {
			team.resetMembers();
		}
		for (Entry<Player, PlayerData> data : GameHandler.playerData.entrySet()) {
			PlayerData pData = data.getValue();
			if (pData.getTeam() != null) {
				ScoreTeam team = Main.getTeam(pData.getTeam());
				team.addMember(data.getKey());
			}
		}
	}

	public void scoreboard() {
		sortTeams();
		TablistScoreboard scoreboard = new TablistScoreboard(new ArrayList<>(Main.teams.values()));
		scoreboard.createScoreboard();
		scoreboard.applyScoreboard();
	}

}
