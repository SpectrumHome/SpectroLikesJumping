package eu.spectrum.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import eu.spectrum.main.Main;

public class SetupCommand implements CommandExecutor {

	public static final String file = "setup.yml";

	public static final String[] locs = new String[] { "waiting_lobby", "ending_lobby" };

	@Override
	public boolean onCommand(CommandSender sender, Command command, String name, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (p.isOp()) {
				if (args.length > 0) {
					String arg = args[0];
					if (arg.equalsIgnoreCase("list")) {
						p.sendMessage(Main.PREFIX + "§7Locations: §a" + arrString(locs));
						return false;
					} else if (arg.equalsIgnoreCase("tp")) {
						if (args.length > 1 && isLoc(args[1])) {
							p.teleport(getLocation(args[1]));
							p.playSound(p.getLocation(), Sound.BURP, 1, 1);
						} else {
							p.performCommand("setup list");
						}
						return false;
					} else if(arg.equalsIgnoreCase("map")) {
						p.teleport(new Location(Main.getInstance().getWorld(),0,200,0));
						p.playSound(p.getLocation(), Sound.NOTE_PLING, 1, 1);
					}
				}
				if (args.length <= 0 || args[0].equalsIgnoreCase("help") || !isLoc(args[0])) {
					String[] arr = missingLocs();
					if (arr.length <= 0) {
						p.sendMessage(Main.PREFIX + "§aDu hast schon alle Locations gesetzt uwu");
					} else
						p.sendMessage(Main.PREFIX + "§7Es fehlt noch: §a/setup <" + arrString(arr) +">");
					return false;
				} else {
					File file = new File(Main.getInstance().getDataFolder() + "/" + SetupCommand.file);
					YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
					config.set(args[0].toLowerCase(), p.getLocation());
					try {
						config.save(file);
					} catch (IOException e) {
						e.printStackTrace();
					}
					p.sendMessage(Main.PREFIX + "§7Du hast §a" + args[0] + " §7gesetzt.");
					p.playSound(p.getLocation(), Sound.NOTE_PLING, 1, 1);
				}
			}
		}
		return false;
	}

	public static Location getLocation(String key) {
		File file = new File(Main.getInstance().getDataFolder() + "/" + SetupCommand.file);
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		if (!isLoc(key))
			return null;
		return (Location) config.get(key.toLowerCase());
	}

	public static boolean isLoc(String loc) {
		boolean res = false;
		for (String l : locs) {
			if (loc.equalsIgnoreCase(l)) {
				res = true;
				break;
			}
		}
		return res;
	}

	public static String arrString(String[] arr) {
		String res = "";
		for (int i = 0; i < arr.length; i++) {

			res += arr[i] + (i < arr.length - 1 ? ", " : "");
		}
		return res;
	}

	public static String[] missingLocs() {
		File file = new File(Main.getInstance().getDataFolder() + "/" + SetupCommand.file);
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		List<String> res = new ArrayList<String>();
		for (String loc : locs) {
			boolean contains = false;
			for (String key : config.getKeys(false)) {
				if (loc.equalsIgnoreCase(key)) {
					contains = true;
					break;
				}
			}
			if (!contains)
				res.add(loc);
		}
		return res.toArray(new String[0]);
	}

}
