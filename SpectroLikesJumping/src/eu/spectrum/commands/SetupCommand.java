package eu.spectrum.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import eu.realms.common.io.Config;
import eu.spectrum.guis.MapSetupGui;
import eu.spectrum.main.Main;
import eu.spectrum.main.Systems;
import eu.spectrum.main.Systems.GameLocation;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

public class SetupCommand implements CommandExecutor {

	public static HashMap<Player, MapSetupMode> setupMode = new HashMap<Player, MapSetupMode>();

	@SuppressWarnings("unchecked")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String name, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (p.isOp()) {
				if (args.length > 0) {
					String arg = args[0];
					if (arg.equalsIgnoreCase("list")) {
						p.sendMessage(
								Main.PREFIX + Main.handler.format("cmd.setup.list", arrString(GameLocation.toArray())));
						return false;
					} else if (arg.equalsIgnoreCase("tp") || arg.equalsIgnoreCase("reset")) {
						if (args.length > 1 && isLoc(args[1])) {
							String locName = args[1].toUpperCase();
							GameLocation loc = GameLocation.valueOf(locName);
							if (isLocSet(loc)) {
								switch (arg) {
								case "tp":
									teleport(p, loc);
									p.playSound(p.getLocation(), Sound.BURP, 1, 1);
									break;
								case "reset":
									Config config = getConfig();
									config.yml.set(locName, null);
									config.saveConfig();
									p.sendMessage(Main.PREFIX
											+ Main.handler.format("cmd.setup.reset", args[1].toLowerCase()));
									p.playSound(p.getLocation(), Sound.BURP, 1, 1);
									break;
								}
							} else
								p.sendMessage(Main.PREFIX + Main.handler.format("cmd.setup.missing"));
						} else {
							p.performCommand("setup list");
						}
						return false;
					} else if (arg.equalsIgnoreCase("map")) {
						if (args.length > 1) {
							if (!setupMode.containsKey(p) && Bukkit.getWorld(args[1]) == null) {
								p.sendMessage(Main.PREFIX + Main.handler.format("cmd.setup.map.no-setup"));
								return false;
							}
							MapSetupMode mode = setupMode.get(p);
							String operation = args[1];
							switch (operation) {
							case "confirm":
								cancelMapMode(p);
								p.sendMessage(Main.PREFIX + Main.handler.format("cmd.setup.map.confirm"));

								Bukkit.unloadWorld(mode.map.getName(), false);

								try {
									FileUtils.copyDirectory(new File(mode.map.getName()),
											new File(Systems.mapFolder + "/" + mode.map.getName() + "/map"));
									Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
										try {
											if (!mode.map.getName().equalsIgnoreCase("world_nether")
													&& !mode.map.getName().equalsIgnoreCase("world_the_end")) {
												FileUtils.deleteDirectory(new File(mode.map.getName()));
											}
										} catch (IOException e) {
											e.printStackTrace();
										}
									}, 20 * 2);
								} catch (IOException e) {
									e.printStackTrace();
								}

								Config cfg = new Config(
										Systems.mapFolder + "/" + mode.map.getName() + "/" + Systems.setUpFile, true);
								cfg.yml.set("locations", mode.locations);
								cfg.saveConfig();
								break;
							case "loc":
								if (mode.addLocation(p.getLocation())) {
									p.playSound(p.getLocation(), Sound.LEVEL_UP, 1, 1);
									p.sendMessage(Main.PREFIX + Main.handler.format("cmd.setup.map.loc-add"));
								} else
									p.sendMessage(Main.PREFIX + Main.handler.format("cmd.setup.map.different-world"));
								break;
							case "undo":
								mode.undo();
								p.playSound(p.getLocation(), Sound.BURP, 1, 1);
								p.sendMessage(Main.PREFIX + Main.handler.format("cmd.setup.undo"));
								break;
							case "cancel":
								cancelMapMode(p);
								p.sendMessage(Main.PREFIX + Main.handler.format("cmd.setup.map.cancel"));
								break;
							default:
								World world = Bukkit.getWorld(operation);
								if (world != null) {
									setupMode.put(p, new MapSetupMode(p.getLocation(), world));
									p.teleport(world.getSpawnLocation().add(0, 150, 0));
									p.setGameMode(GameMode.CREATIVE);
									p.setFlying(true);
									setupMessage(p);
								} else
									p.sendMessage(Main.PREFIX + Main.handler.format("cmd.setup.map.no-such-world"));
								break;
							}
						} else {
							new MapSetupGui(p).openInventory();
						}
						return false;
					}
				}
				if (args.length <= 0 || args[0].equalsIgnoreCase("help") || !isLoc(args[0])) {
					String[] arr = missingLocs();
					if (arr.length <= 0) {
						p.sendMessage(Main.PREFIX + Main.handler.format("cmd.startup.all-set"));
					} else {
						p.sendMessage(Main.PREFIX + missingLocError());
					}
					return false;
				} else if (args.length > 0 && isLoc(args[0])) {
					String locName = args[0].toUpperCase();
					GameLocation locData = GameLocation.valueOf(locName);
					if (!isLocSet(locData) || locData.isArray()) {
						Config config = getConfig();
						if (locData.isArray()) {
							List<Location> list = (List<Location>) (config.yml.get(locName) == null
									? new ArrayList<Location>()
									: config.yml.getList(locName));
							list.add(p.getLocation());
							config.yml.set(locName, list);
							p.sendMessage(Main.PREFIX + Main.handler.format("cmd.setup.add-loc", args[0]));
						} else {
							config.yml.set(locName, p.getLocation());
							p.sendMessage(Main.PREFIX + Main.handler.format("cmd.setup.set-loc", args[0]));
						}
						config.saveConfig();
						p.playSound(p.getLocation(), Sound.NOTE_PLING, 1, 1);
					} else
						p.sendMessage(Main.PREFIX + Main.handler.format("cmd.setup.already-set"));
				} else
					p.performCommand("setup list");
			}
		}
		return false;
	}

	public void setupMessage(Player p) {
		p.sendMessage("§8§m-------§7[§aMap Setup§7]-------§r\n \n");
		TextComponent confirm = new TextComponent("   §7[§a" + Main.handler.format("confirm") + "§7]");
		confirm.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/setup map confirm"));
		TextComponent location = new TextComponent(" §7[§6" + Main.handler.format("setup.set") + "§7]");
		location.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/setup map loc"));
		TextComponent undo = new TextComponent("   §7[§cUndo§7]");
		undo.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/setup map undo"));
		TextComponent cancel = new TextComponent(" §7[§4Cancel§7]");
		cancel.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/setup map cancel"));

		confirm.addExtra(location);
		undo.addExtra(cancel);

		p.spigot().sendMessage(confirm);
		p.spigot().sendMessage(undo);

		p.sendMessage("\n \n§8§m----------------------");
	}

	public static void cancelMapMode(Player p) {
		if (!setupMode.containsKey(p))
			return;
		MapSetupMode mode = setupMode.remove(p);

		for (Player p1 : Bukkit.getOnlinePlayers()) {
			if (p1.getWorld().getName().equals(mode.map.getName())) {
				p1.teleport(mode.startLoc);
			}
		}
	}

	public static class MapSetupMode {

		public Location startLoc;
		public List<Location> locations;
		public World map;

		public MapSetupMode(Location startLoc, World map) {
			this.startLoc = startLoc;
			locations = new ArrayList<>();
			this.map = map;
		}

		public void undo() {
			if (locations.size() > 0)
				locations.remove(locations.size() - 1);
		}

		public boolean addLocation(Location loc) {
			if (!map.getName().equals(loc.getWorld().getName()))
				return false;
			locations.add(loc);
			return true;
		}

	}

	public static String missingLocError() {
		return Main.handler.format("cmd.setup.missing-locs", arrString(missingLocs()));
	}

	public static Location getLocation(GameLocation loc) {
		if (!isLoc(loc.toString()))
			return null;
		Config config = getConfig();
		if (loc.isArray()) {
			List<Location> locs = getLocations(loc);
			if (locs.size() <= 0)
				return null;
			int randomIndex = new Random().nextInt(locs.size());
			return locs.get(randomIndex);
		} else
			return (Location) config.yml.get(loc.toString());
	}

	@SuppressWarnings("unchecked")
	public static List<Location> getLocations(GameLocation arrayLoc) {
		List<Location> locations = new ArrayList<Location>();
		if (arrayLoc.isArray()) {
			Config config = getConfig();
			if (config.yml.get(arrayLoc.toString()) != null) {
				locations = (List<Location>) config.yml.getList(arrayLoc.toString());
			}
		}
		return locations;
	}

	public static boolean isLoc(String loc) {
		boolean res = false;
		for (GameLocation l : GameLocation.values()) {
			if (loc.equalsIgnoreCase(l.toString())) {
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

	public static boolean isLocSet(GameLocation loc) {
		Config config = getConfig();
		return config.yml.contains(loc.toString().toLowerCase())
				&& config.yml.get(loc.toString().toLowerCase()) != null;
	}

	public static Config getConfig() {
		return new Config("/" + Systems.setUpFile);
	}

	public static String[] missingLocs() {
		Config config = getConfig();
		List<String> res = new ArrayList<String>();
		for (GameLocation loc : GameLocation.values()) {
			boolean contains = false;
			for (String key : config.yml.getKeys(false)) {
				if (loc.toString().equalsIgnoreCase(key) && config.yml.get(key) != null) {
					contains = true;
					break;
				}
			}
			if (!contains)
				res.add(loc.toString().toLowerCase());
		}
		return res.toArray(new String[0]);
	}

	public static void forceTeleport(Player p, GameLocation loc) {
		Location l = getLocation(loc);
		if (l == null)
			p.kickPlayer("");
		p.teleport(l);
	}

	public static void forceTeleportAll(GameLocation loc) {
		Location l = getLocation(loc);
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (l == null)
				p.kickPlayer("");
			else
				p.teleport(l);
		}
	}

	public static void teleport(Player p, GameLocation loc) {
		Location l = getLocation(loc);
		if (l == null)
			return;
		p.teleport(l);
	}

	public static void teleportAll(GameLocation loc) {
		if (loc.isArray()) {
			List<Location> locs = getLocations(loc);
			if (locs == null || locs.size() == 0)
				return;
			int index = 0, length = locs.size();
			for (Player p : Bukkit.getOnlinePlayers()) {
				p.teleport(locs.get(index));
				index++;
				index %= length;
			}
		} else {
			Location l = getLocation(loc);
			if (l == null)
				return;
			for (Player p : Bukkit.getOnlinePlayers()) {
				p.teleport(l);
			}
		}
	}

}
