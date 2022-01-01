package eu.spectrum.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import eu.spectrum.game.GameHandler;

public class StartCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player) sender;
			if(args.length >= 1 && args[0].equalsIgnoreCase("now")) {
				GameHandler.startGame(p);
			} else {
				GameHandler.startGameTimer(p);
			}
		}
		
		return false;
	}

}
