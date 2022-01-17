package eu.spectrum.guis;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import eu.spigotui.ui.SpigotUI;
import eu.spigotui.ui.active.SizedActiveInventory;
import eu.spigotui.ui.components.UIButton;
import eu.spigotui.utils.ItemBuilder;
import eu.spigotui.utils.UISection;

public class MapSetupGui extends SpigotUI {

	public MapSetupGui(Player p) {
		super(p, new SizedActiveInventory(1));
	}

	@Override
	public void initComponents() {
		paintBackground(UISection.TOP);
		for (int i = 0; i < Bukkit.getWorlds().size(); i++) {
			World world = Bukkit.getWorlds().get(i);
			System.out.println(world.getName());
			addComponent(UISection.TOP,new UIButton(new ItemBuilder(Material.PAPER).setName("§a" + world.getName()).build())
					.setOnClick((c) -> {

						getPlayer().performCommand("setup map " + world.getName());

					}).setPos(i, 0, 1));
		}
	}

}
