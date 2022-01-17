package eu.spectrum.guis;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import eu.spectrum.game.GameHandler;
import eu.spectrum.game.JumpTeam;
import eu.spectrum.game.TeamHandler;
import eu.spectrum.main.Main;
import eu.spigotui.ui.SpigotUI;
import eu.spigotui.ui.active.SizedActiveInventory;
import eu.spigotui.ui.components.UIButton;
import eu.spigotui.utils.ItemBuilder;
import eu.spigotui.utils.UISection;

public class TeamSelectorGui extends SpigotUI {

	public TeamSelectorGui(Player p) {
		super(p, new SizedActiveInventory(1), Main.handler.format("teams.select"));
	}

	@Override
	public void initComponents() {
		this.paintBackground(UISection.TOP);
		int pos = 0;
		for (JumpTeam team : JumpTeam.values()) {
			List<Player> players = TeamHandler.getPlayers(team);
			String[] lore = new String[players.size()];

			for (int i = 0; i < players.size(); i++) {
				lore[i] = team.getColor() + players.get(i).getName();
			}

			ItemBuilder builder = new ItemBuilder(Material.WOOL).setColor(team.getColor()).setName(team.getColor() + team.getName()).setLore(lore);

			if (players.contains(getPlayer()))
				builder.enchantEffect();

			ItemStack button = builder.build();
			addComponent(UISection.TOP, new UIButton(button, (action) -> {
				if (!TeamHandler.isFull(team)) {
					GameHandler.playerData.get(getPlayer()).changeTeam(team);
				} else
					getPlayer().sendMessage(Main.PREFIX + Main.handler.format("team.full"));
				getPlayer().closeInventory();
			}).setPos(pos, 0));

			pos++;
		}
	}

}
