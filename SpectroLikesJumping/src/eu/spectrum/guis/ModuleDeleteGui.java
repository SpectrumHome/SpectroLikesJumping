package eu.spectrum.guis;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import eu.spectrum.main.Main;
import eu.spectrum.utils.ModuleData;
import eu.spectrum.utils.ModuleManager;
import eu.spigotui.ui.SpigotUI;
import eu.spigotui.ui.UIComponent;
import eu.spigotui.ui.active.SizedActiveInventory;
import eu.spigotui.ui.components.UIButton;
import eu.spigotui.utils.ItemBuilder;
import eu.spigotui.utils.ItemBuilder.BlockColor;
import eu.spigotui.utils.UISection;

public class ModuleDeleteGui extends SpigotUI {

	ModuleData data;
	boolean cancelled = true;

	public ModuleDeleteGui(Player p, ModuleData data) {
		super(p, new SizedActiveInventory(1));
		this.data = data;
	}

	@Override
	public void initComponents() {
		Player p = getPlayer();
		this.setName(Main.handler.format("cmd.delete.title", data.name));
		this.paintBackground(UISection.TOP);

		UIComponent cancelButton = new UIButton(
				new ItemBuilder(Material.WOOL).setColor(BlockColor.RED).setName("§c"+Main.handler.format("cancel")).build())
						.setOnClick((e) -> {
							p.closeInventory();
						}).setPos(3, 0);

		UIComponent deleteButton = new UIButton(new ItemBuilder(Material.WOOL).setColor(BlockColor.GREEN)
				.setName("§a§l"+Main.handler.format("delete")).build()).setOnClick((e) -> {
					cancelled = false;
					p.closeInventory();
					if (ModuleManager.delete(data.name)) {
						p.sendMessage(Main.PREFIX + Main.handler.format("cmd.delete.success", data.name));
						p.playSound(p.getLocation(), Sound.NOTE_PLING, 1, 1);
					} else {
						p.sendMessage(Main.PREFIX + Main.handler.format("cmd.delete.error"));
						p.playSound(p.getLocation(), Sound.COW_HURT, 1, 1);
					}
				}).setPos(5, 0);

		this.addComponent(UISection.TOP,cancelButton);
		this.addComponent(UISection.TOP,deleteButton);

		setActionOnClose(() -> {
			if (cancelled) {
				p.sendMessage(Main.handler.format("cmd.delete.cancel"));
				p.playSound(p.getLocation(), Sound.BURP, 1, 1);
			}
		});
	}

}
