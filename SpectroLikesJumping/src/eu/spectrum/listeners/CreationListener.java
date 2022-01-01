package eu.spectrum.listeners;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.Vector;

import eu.spectrum.commands.CreateCommand;
import eu.spectrum.main.Main;
import eu.spectrum.utils.Difficulty;
import eu.spectrum.utils.ModuleData;
import eu.spectrum.utils.ModuleManager;

public class CreationListener implements Listener {

	public static HashMap<Player, ModuleData> creationMode = new HashMap<Player, ModuleData>();

	public static boolean isCheckpoint(Location loc) {
		return loc.getBlock().getType()==Material.GOLD_PLATE && loc.getBlock().getLocation().subtract(0, 1, 0).getBlock().getType()==Material.GOLD_BLOCK;
	}

	@EventHandler
	public void onAnvilInv(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		Inventory inv = e.getInventory();
		if (inv.getType() == InventoryType.ANVIL && creationMode.containsKey(p)) {
			e.setCancelled(true);

			ItemStack currentItem = e.getCurrentItem();
			if (currentItem.getItemMeta() instanceof SkullMeta) {
				boolean cam = ((SkullMeta) currentItem.getItemMeta()).getOwner().equalsIgnoreCase("MHF_cam");
				if (((SkullMeta) currentItem.getItemMeta()).getOwner().equalsIgnoreCase("MHF_youtube") || cam) {
					ModuleData data = creationMode.remove(p);

					try {

						ItemStack namedItem = inv.getItem(9);
						ItemStack originItem = inv.getItem(0);
						boolean wasRenamed = namedItem != null && namedItem.getType() != Material.STAINED_GLASS_PANE;

						if (wasRenamed) {
							data.name = namedItem.getItemMeta().getDisplayName().trim();
						}

						if (wasRenamed && namedItem.getItemMeta() != null
								&& ModuleManager.isModule(namedItem.getItemMeta().getDisplayName().trim())) {
							inv.setItem(9, CreateCommand.paneFiller((byte) 14, Main.handler.format("name.exists")));
							p.updateInventory();
							return;
						}
						if (!originItem.getItemMeta().getDisplayName().equalsIgnoreCase(ModuleManager.defModuleName)) {
							String oldModule = originItem.getItemMeta().getDisplayName().trim();
							YamlConfiguration config = ModuleManager.getModuleConfig(oldModule);
							config.set("name", data.name);
							config.set("difficulty", data.difficulty.toString());
							ModuleManager.saveModuleConfig(config, oldModule);

							if (wasRenamed) {
								ModuleManager.copyModule(oldModule, data.name);
								ModuleManager.delete(oldModule);
							}

						} else if (wasRenamed) {
							ModuleManager.registerModule(p, data);
						} else {
							inv.setItem(9, CreateCommand.paneFiller((byte) 14, Main.handler.format("name.missing")));
							p.updateInventory();
						}
						quitCreation(p, inv);
						p.closeInventory();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					if (cam) {
						Location start = p.getLocation().add(new Vector(3,3,3));
						data.tmpStart = start;
						ModuleManager.paste(start, data.name);
						data.resetCheckpoints();
						data.setAbsoluteLocations(start);
						creationMode.put(p, data);
						CreateCommand.step(p);
					}
				}
			} else {
				byte b = (byte) currentItem.getDurability();
				for (Difficulty d : Difficulty.values()) {
					if (d.getSubColorID() == b) {
						creationMode.get(p).difficulty = d;
						Inventory pInv = p.getInventory();
						CreateCommand.setDifficultyPart(d, pInv);
						p.updateInventory();
						break;
					}
				}
			}

		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if (creationMode.containsKey(p) && e.getAction() == Action.PHYSICAL
				&& p.getLocation().getBlock().getType() == Material.GOLD_PLATE) {
			e.setCancelled(true);
		}
	}

	public static void quitCreation(Player p, Inventory inv) {
		inv.clear();
		if (creationMode.containsKey(p)) {
			creationMode.remove(p);
			p.sendMessage(Main.handler.format("module.registration.canceled"));
		} else {
			p.sendMessage(Main.handler.format("module.registration.suceeded"));
			p.playSound(p.getLocation(), Sound.LEVEL_UP, 1, 1);
		}
		p.getInventory().clear();
		if (CreateCommand.itemCache.containsKey(p)) {
			for (int i : CreateCommand.itemCache.get(p).keySet()) {
				p.getInventory().setItem(i, CreateCommand.itemCache.get(p).get(i));
			}
			CreateCommand.itemCache.remove(p);
			p.updateInventory();
		}
	}

	@EventHandler
	public void onAnvilClose(InventoryCloseEvent e) {
		Player p = (Player) e.getPlayer();
		Inventory inv = e.getInventory();
		if (inv.getType() == InventoryType.ANVIL && creationMode.containsKey(p)) {
			quitCreation(p, inv);
		}
	}

}
