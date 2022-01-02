package eu.spectrum.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import eu.spectrum.commands.SetupCommand;
import eu.spectrum.game.GameHandler;
import eu.spectrum.game.GameState;
import eu.spectrum.main.Main;
import eu.spectrum.main.Systems;

public class ConnectionListener implements Listener {

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		e.setJoinMessage(Main.handler.format("game.player.joined", p.getName()));
		p.teleport(SetupCommand.getLocation("waiting_lobby"));
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), ()->{
			GameHandler.checkCountdownState(null);
		},20);
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
		GameHandler.checkCountdownState(Bukkit.getOnlinePlayers().size()-1);
		e.setQuitMessage(Main.handler.format("game.player.left", p.getName()));
	}

}
