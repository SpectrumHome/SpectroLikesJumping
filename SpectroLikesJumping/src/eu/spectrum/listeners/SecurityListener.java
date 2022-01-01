package eu.spectrum.listeners;

import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import eu.spectrum.game.GameHandler;
import eu.spectrum.game.GameState;

public class SecurityListener implements Listener {

	@EventHandler
	public void onBlockDestroy(BlockBreakEvent e) {
		if (e.getPlayer().getGameMode() == GameMode.SURVIVAL)
			e.setCancelled(true);
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		if (e.getPlayer().getGameMode() == GameMode.SURVIVAL)
			e.setCancelled(true);
	}

	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if (p.getGameMode() == GameMode.SURVIVAL) {
				actionForGameState(() -> e.setCancelled(true), true, GameState.PVP);
			}
		}
	}
	

	@EventHandler
	public void onHunger(FoodLevelChangeEvent e) {
		e.setCancelled(true);
		if (e.getEntity().getWorld().getDifficulty() != Difficulty.PEACEFUL)
			e.getEntity().getWorld().setDifficulty(Difficulty.PEACEFUL);
	}

	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent e) {
		if (e.getEntity() instanceof Item)
			actionForGameState(() -> e.setCancelled(true), true, GameState.PVP);
		else if (!(e.getEntity() instanceof Player))
			e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onWeatherChange(WeatherChangeEvent event) {
		boolean rain = event.toWeatherState();
		if (rain)
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onThunderChange(ThunderChangeEvent event) {
		boolean storm = event.toThunderState();
		if (storm)
			event.setCancelled(true);
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent e) {
		if (e.getPlayer().getGameMode() == GameMode.SURVIVAL)
			actionForGameState(() -> e.setCancelled(true), true, GameState.PVP);
	}

	public static void actionForGameState(Runnable action, boolean invert, GameState... states) {
		boolean isState = false;
		for (GameState state : states) {
			if (GameHandler.gameState == state) {
				isState = true;
				break;
			}
		}

		if (invert)
			isState = !isState;
		if (isState)
			action.run();
	}

}
