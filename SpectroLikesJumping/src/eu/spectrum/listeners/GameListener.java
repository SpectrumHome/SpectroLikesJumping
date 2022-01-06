package eu.spectrum.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import eu.spectrum.game.GameHandler;
import eu.spectrum.game.GameState;
import eu.spectrum.game.PlayerData;
import eu.spectrum.main.Main;
import eu.spectrum.main.Systems;

import static eu.spectrum.game.GameHandler.*;

public class GameListener implements Listener {

	@EventHandler
	public void interact(PlayerInteractEvent e) {
		if (e.getAction() == Action.PHYSICAL && GameHandler.gameState == GameState.INGAME) {
			Player p = e.getPlayer();
			if (GameHandler.playerData.containsKey(p) && Systems.isCheckpoint(p.getLocation())) {
				PlayerData data = GameHandler.playerData.get(p);
				if (p.getLocation().getBlock().getLocation().distance(data.getEnd().getBlock().getLocation()) < 2) {
					if (data.currentModule >= GameHandler.gameModules.size() - 1) {
						p.sendMessage(Main.handler.format("game.reached-goal"));
					} else {
						p.sendMessage(Main.PREFIX + "§aCheckpoint");
						GameHandler.spawnNextModule(p);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		if(playerData.containsKey(p) && gameState == GameState.PVP) {
			PlayerData data = playerData.get(p);
			boolean lives = data.playerDied();
			if(lives) {
				//respawn
			} else {
				//spectator
			}
		}
	}
	

	@EventHandler
	public void onFall(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		if(GameHandler.gameState==GameState.INGAME && p.getLocation().getWorld().getName().equalsIgnoreCase(Main.worldName) && GameHandler.playerData.containsKey(p)) {
			PlayerData data = GameHandler.playerData.get(p);
			if(p.getLocation().getBlockY()<data.minHeight) {
				p.teleport(data.getStart().clone().add(new Vector(0,1,0)));
			}
		}
	}

}
