package eu.spectrum.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import eu.spectrum.game.GameHandler;
import eu.spectrum.game.GameState;
import eu.spectrum.main.Main;

public class SkipCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (p.isOp()) {
				if (GameHandler.gameState == GameState.INGAME && GameHandler.ingameTime > 10) {
					GameHandler.ingameTime = 10;
					p.sendMessage(Main.PREFIX + Main.handler.format("game.skipped.local"));
					for (Player on : Bukkit.getOnlinePlayers())
						if (on != p)
							on.sendMessage(Main.PREFIX + Main.handler.format("game.skipped.global", p.getName()));
					
				} else {
					p.sendMessage(Main.PREFIX +  Main.handler.format("game.skipped.failed"));
				}
			}
		}

		return false;
	}

}
