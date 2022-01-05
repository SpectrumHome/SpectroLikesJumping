package eu.spectrum.guis;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import eu.spectrum.commands.CreateCommand;
import eu.spectrum.main.Main;
import eu.spectrum.main.Systems;
import eu.spectrum.utils.Difficulty;
import eu.spectrum.utils.ModuleData;
import eu.spectrum.utils.ModuleManager;
import eu.spigotui.ui.SpigotUI;
import eu.spigotui.ui.active.TextFieldInventory;
import eu.spigotui.ui.components.UIButton;
import eu.spigotui.ui.components.UIDisplayComponent;
import eu.spigotui.utils.ItemBuilder;
import eu.spigotui.utils.UISection;

public class ModuleRegisterGui extends SpigotUI {

	boolean change;

	public ModuleRegisterGui(Player p, boolean change) {
		super(p);
		this.change = change;
	}

	@Override
	public void initComponents() {
		Player p = getPlayer();
		ModuleData data = CreateCommand.creationMode.get(p);

		ItemStack tickbox = ItemBuilder.skull("MHF_youtube").setName("§a§l" + Main.handler.format("complete"))
				.build();
		ItemStack arrow = ItemBuilder.skull("MHF_ArrowRight").setName("§c§l" + Main.handler.format("difficulties"))
				.build();
		ItemStack changeBuild = ItemBuilder.skull("MHF_cam").setName("§a§l" + Main.handler.format("construct.change"))
				.build();

		TextFieldInventory field = new TextFieldInventory(data.name == null ? Systems.defModuleName : data.name);
		setActiveInventory(field);

		addComponent(UISection.BOTTOM, new UIDisplayComponent(ItemBuilder.paneFiller(7, "§8-"), 9, 4));

		addComponent(UISection.BOTTOM, new UIDisplayComponent(arrow).setPos(2, 1));
		addComponent(UISection.BOTTOM, new UIButton(tickbox).setOnClick((action) -> {
			confirmChange(field, data);
		}).setPos(change ? 3 : 4, 3));

		if (change) {
			addComponent(UISection.BOTTOM, new UIButton(changeBuild).setOnClick((action) -> {
				confirmChange(field, data);
				Location start = p.getLocation().add(new org.bukkit.util.Vector(3, 3, 3));
				data.tmpStart = start;
				ModuleManager.paste(start, data.name);
				data.resetCheckpoints();
				data.setAbsoluteLocations(start);
				CreateCommand.creationMode.put(p, data);
				CreateCommand.step(p);
			}).setPos(5, 3, 1));
		}

		int count = 0;
		Difficulty current = data.difficulty;
		UIDisplayComponent paneBg = new UIDisplayComponent(getDifficultyPane(current), 1, 3);
		paneBg.setPos(current.getDifficulty() + 3, 0);
		addComponent(UISection.BOTTOM, paneBg);
		for (Difficulty d : Difficulty.values()) {
			ItemStack dif = new ItemBuilder(Material.WOOL).setDamage(d.getSubColorID())
					.setName(d.getChatColor() + d.getName()).build();

			addComponent(UISection.BOTTOM, new UIButton(dif).setOnClick((action) -> {
				data.difficulty = d;
				paneBg.setStack(getDifficultyPane(d));
				paneBg.setPos(d.getDifficulty() + 3, 0);
				repaintBottom();

			}).setPos(count + 3, 1, 1));
			count++;

		}

		setActionOnClose(() -> quitCreation(field.getPlayer()));
	}

	public void confirmChange(TextFieldInventory field, ModuleData data) {
		try {

			String changedName = field.getValue();
			
			System.out.println("CHANGED TO: " + changedName);
			
			String originalName = field.getDefValue();
			boolean wasRenamed = field.valueChanged();

			System.out.println("ORIGINAL NAME: " + originalName);
			
			if (wasRenamed) {
				data.name = changedName;
			}

			if (wasRenamed && ModuleManager.isModule(changedName)) {
				field.displayError(Main.handler.format("name.exists"));
				return;
			}
			if (change || wasRenamed)
				CreateCommand.creationMode.remove(field.getPlayer());
			
			if (change) {
				YamlConfiguration config = ModuleManager.getModuleConfig(originalName);
				config.set("name", data.name);
				config.set("difficulty", data.difficulty.toString());
				ModuleManager.saveModuleConfig(config, originalName);

				if (wasRenamed) {
					ModuleManager.copyModule(originalName, data.name);
					ModuleManager.delete(originalName);
				}

			} else if (wasRenamed) {
				if (!ModuleManager.registerModule(field.getPlayer(), data)) {
					field.displayError(Main.handler.format("name.invalid"));
					return;
				}
			} else {
				field.displayError(Main.handler.format("name.missing"));
				return;
			}

			field.getUi().close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void quitCreation(Player p) {
		if (CreateCommand.creationMode.containsKey(p)) {
			CreateCommand.creationMode.remove(p);
			p.sendMessage(Main.handler.format("module.registration.canceled"));
		} else {
			p.sendMessage(Main.handler.format("module.registration.suceeded"));
			p.playSound(p.getLocation(), Sound.LEVEL_UP, 1, 1);
		}
	}

	private ItemStack getDifficultyPane(Difficulty current) {
		return ItemBuilder.paneFiller(current.getSubColorID(), current.getChatColor() + current.getName());
	}

}
