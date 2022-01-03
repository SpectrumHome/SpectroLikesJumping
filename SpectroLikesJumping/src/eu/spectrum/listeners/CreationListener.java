package eu.spectrum.listeners;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import eu.spectrum.utils.ModuleData;

public class CreationListener implements Listener {

	public static HashMap<Player, ModuleData> creationMode = new HashMap<Player, ModuleData>();

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
					ModuleData data = creationMode.get(p);
					
				}
			} else {
				
			}

		}
	}

	@EventHandler
	public void onAnvilClose(InventoryCloseEvent e) {
		Player p = (Player) e.getPlayer();
		Inventory inv = e.getInventory();
		if (inv.getType() == InventoryType.ANVIL && creationMode.containsKey(p)) {
			p.sendMessage("quitting");
//			quitCreation(p, inv);
		}
	}

}
