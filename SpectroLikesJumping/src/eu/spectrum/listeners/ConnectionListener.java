package eu.spectrum.listeners;

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

import eu.realms.common.display.Hologram;
import eu.spectrum.commands.SetupCommand;
import eu.spectrum.game.EnumGameState;
import eu.spectrum.game.GameHandler;
import eu.spectrum.game.JumpTeam;
import eu.spectrum.game.PlayerData;
import eu.spectrum.game.TeamHandler;
import eu.spectrum.game.states.LobbyState;
import eu.spectrum.guis.TeamSelectorGui;
import eu.spectrum.main.Main;
import eu.spectrum.main.Systems;
import eu.spectrum.main.Systems.GameLocation;
import eu.spigotui.ui.SpigotUI;
import eu.spigotui.utils.ItemBuilder;

public class ConnectionListener implements Listener {

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		new Hologram(p.getLocation(), "§cCedric","§7isn Kek","§aund lenni","auch").showAll();
		e.setJoinMessage(Main.handler.format("game.player.joined", p.getName()));
		SetupCommand.teleport(p, GameLocation.WAITING_LOBBY);
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
			LobbyState.checkCountdownState(null);
		}, 20);

		if (!GameHandler.playerData.containsKey(p))
			GameHandler.playerData.put(p, new PlayerData(p));

		ItemStack teamSelector = new ItemBuilder(Material.PAPER).setName("§aTeams").build();

		SpigotUI.addClickListener(teamSelector, (action, player) -> new TeamSelectorGui(player).openInventory());
		p.getInventory().clear();
		p.getInventory().addItem(teamSelector);

		JumpTeam foundTeam = TeamHandler.findEmptyTeam();

		if (foundTeam != null) {
			GameHandler.playerData.get(p).changeTeam(foundTeam);
		}
		
	}

	@EventHandler
	public void onPreConnect(AsyncPlayerPreLoginEvent e) {
		if (Bukkit.getOnlinePlayers().size() >= Systems.MAX_PLAYERS)
			e.disallow(Result.KICK_FULL, Main.handler.format("game.full"));
		else if (GameHandler.gameState != EnumGameState.LOBBY)
			e.disallow(Result.KICK_FULL, Main.handler.format("game.running"));

	}

	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		e.setQuitMessage(Main.handler.format("game.player.left", p.getName()));

		if (GameHandler.gameState == EnumGameState.LOBBY)
			LobbyState.checkCountdownState(Bukkit.getOnlinePlayers().size() - 1);

		GameHandler.playerData.get(p).changeTeam(null);
		if (GameHandler.playerData.containsKey(p))
			GameHandler.playerData.remove(p);
		
	}

}
