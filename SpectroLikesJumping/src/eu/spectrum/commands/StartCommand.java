package eu.spectrum.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import eu.spectrum.game.EnumGameState;
import eu.spectrum.game.GameHandler;
import eu.spectrum.game.states.LobbyState;
import eu.spectrum.main.Main;

public class StartCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (args.length >= 1 && args[0].equalsIgnoreCase("now")) {
				if (GameHandler.gameState == EnumGameState.LOBBY) {
					GameHandler.changeGameState(EnumGameState.INGAME, p);
				} else p.sendMessage(Main.PREFIX + Main.handler.format("game.running"));
			} else {
				if (LobbyState.startCountdown) {
					GameHandler.changeGameState(EnumGameState.INGAME, p);
				} else {
					LobbyState.startCountdown(p);
				}
			}
		}

		return false;
	}

}
