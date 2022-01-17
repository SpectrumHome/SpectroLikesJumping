package eu.spectrum.listeners;

import static eu.spectrum.game.GameHandler.gameState;
import static eu.spectrum.game.GameHandler.playerData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import eu.spectrum.commands.SetupCommand;
import eu.spectrum.game.EnumGameState;
import eu.spectrum.game.GameHandler;
import eu.spectrum.game.PlayerData;
import eu.spectrum.game.states.IngameState;
import eu.spectrum.game.states.PVPState;
import eu.spectrum.main.Main;
import eu.spectrum.main.Systems;
import eu.spectrum.main.Systems.GameLocation;

import static eu.spectrum.game.GameHandler.*;

public class GameListener implements Listener {

	@EventHandler
	public void interact(PlayerInteractEvent e) {
		if (e.getAction() == Action.PHYSICAL && GameHandler.gameState == EnumGameState.INGAME) {
			Player p = e.getPlayer();
			if (GameHandler.playerData.containsKey(p) && Systems.isCheckpoint(p.getLocation())) {
				PlayerData data = GameHandler.playerData.get(p);
				if (p.getLocation().getBlock().getLocation().distance(data.getEnd().getBlock().getLocation()) < 2) {
					if (data.currentModule >= IngameState.gameModules.size() - 1) {
						p.sendMessage(Main.handler.format("game.reached-goal"));
					} else {
						p.sendMessage(Main.PREFIX + "§aCheckpoint");
						IngameState.spawnNextModule(p);
					}
					IngameState.updateCurrModuleView(p);
					IngameState.updateContestantView();
				}
			}
		}
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		Location loc = p.getLocation();
		if (playerData.containsKey(p) && gameState == EnumGameState.PVP) {
			p.spigot().respawn();
			PlayerData data = playerData.get(p);
			boolean lives = data.playerDied();
			p.setFireTicks(0);
			for (PotionEffect effect : p.getActivePotionEffects())
				p.removePotionEffect(effect.getType());

			if (lives) {
				e.setKeepInventory(true);
				List<Location> locs = SetupCommand.getLocations(GameLocation.PVP);
				List<Player> players = new ArrayList<>();
				for (Player pl : Bukkit.getOnlinePlayers()) {
					if (!playerData.containsKey(pl) && playerData.get(pl).lives() && pl != p)
						players.add(pl);
				}

				double sqare = Systems.ALLOWED_RESPAWN_ENIMY_DISTANCE * Systems.ALLOWED_RESPAWN_ENIMY_DISTANCE;

				for (Location l : locs) {
					for (Player pl : players) {
						if (pl.getLocation().distanceSquared(l) < sqare)
							locs.remove(l);
					}
				}
				if (locs.isEmpty()) {
					SetupCommand.teleport(p, GameLocation.PVP);
				} else {
					p.teleport(locs.get(new Random().nextInt(locs.size())));
				}
			} else {
				p.getInventory().clear();
				p.teleport(loc);
				p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, true));
				for (Player pl : Bukkit.getOnlinePlayers()) {
					if (pl != p) {
						pl.hidePlayer(p);
						p.spigot().setCollidesWithEntities(false);
						p.setAllowFlight(true);
					}
				}
			}

			Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
				PVPState.updateScoreboard();
			}, 20 * 2);

		}
	}

	@EventHandler
	public void onFall(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		if (GameHandler.gameState == EnumGameState.INGAME
				&& p.getLocation().getWorld().getName().equalsIgnoreCase(Main.worldName)
				&& GameHandler.playerData.containsKey(p)) {
			PlayerData data = GameHandler.playerData.get(p);
			if (p.getLocation().getBlockY() < data.minHeight) {
				p.teleport(data.getStart().clone().add(new Vector(0, 1, 0)));
			}
		}
	}

}
