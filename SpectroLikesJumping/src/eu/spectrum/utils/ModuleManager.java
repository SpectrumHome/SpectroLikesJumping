package eu.spectrum.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.schematic.MCEditSchematicFormat;
import com.sk89q.worldedit.schematic.SchematicFormat;

import eu.spectrum.main.Main;

@SuppressWarnings("deprecation")
public class ModuleManager {

	private static final char[] ILLEGAL_CHARACTERS = { '/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>',
			'|', '\"', ':' };

	public static void saveSchematic(Player player, Location loc1, Location loc2, Location startLoc, String name) {
		try {
			File schematic = new File(moduleFolder(name) + "/construct.schematic");

			WorldEditPlugin wep = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
			LocalPlayer localPlayer = wep.wrapPlayer(player);
			CuboidRegion region = new CuboidRegion(localPlayer.getWorld(),
					new com.sk89q.worldedit.Vector(loc1.getBlockX(), loc1.getBlockY(), loc1.getBlockZ()),
					new com.sk89q.worldedit.Vector(loc2.getBlockX(), loc2.getBlockY(), loc2.getBlockZ()));
			BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

			EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(region.getWorld(),
					-1);

			ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(editSession, region, clipboard,
					region.getMinimumPoint());
			forwardExtentCopy.setCopyingEntities(true);
			Operations.complete(forwardExtentCopy);

			com.sk89q.worldedit.Vector min = clipboard.getMinimumPoint();
			com.sk89q.worldedit.Vector max = clipboard.getMaximumPoint();

			editSession.enableQueue();
			com.sk89q.worldedit.Vector start = new com.sk89q.worldedit.Vector(startLoc.getBlockX(),
					startLoc.getBlockY(), startLoc.getBlockZ());
			CuboidClipboard clip = new CuboidClipboard(max.subtract(min).add(new com.sk89q.worldedit.Vector(1, 1, 1)),
					min, min.subtract(start));
			clip.copy(editSession);

			SchematicFormat.MCEDIT.save(clip, schematic);
			editSession.flushQueue();
		} catch (IOException | DataException ex) {
			ex.printStackTrace();
		} catch (EmptyClipboardException ex) {
			ex.printStackTrace();
		} catch (WorldEditException e) {
			e.printStackTrace();
		}
	}

	public static boolean delete(String name) {
		File schematic = new File(moduleFolder(name));
		if (schematic.exists()) {
			try {
				FileUtils.deleteDirectory(schematic);
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public static String moduleFolder(String name) {
		return Main.getInstance().getDataFolder() + "/modules/" + name;
	}

	public static ModuleData getModule(String name) {
		ModuleData data = ModuleData.getFromFile(moduleFolder(name));
		return data;
	}

	public static boolean isModule(String name) {
		return new File(moduleFolder(name)).exists();
	}

	public static void paste(Location loc, String name) {
		WorldEditPlugin worldEditPlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
		File schematic = new File(moduleFolder(name) + "/construct.schematic");
		EditSession session = worldEditPlugin.getWorldEdit().getEditSessionFactory()
				.getEditSession(new BukkitWorld(loc.getWorld()), Integer.MAX_VALUE);
		try {
			CuboidClipboard clipboard = MCEditSchematicFormat.getFormat(schematic).load(schematic);
			clipboard.paste(session, new com.sk89q.worldedit.Vector(loc.getX(), loc.getY(), loc.getZ()), false);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static CuboidClipboard getClipboard(String name) {
		File schematic = new File(moduleFolder(name) + "/construct.schematic");
		try {
			CuboidClipboard clipboard = MCEditSchematicFormat.getFormat(schematic).load(schematic);
			return clipboard;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static boolean registerModule(Player p, ModuleData data) {
		if (!checkValidity(data.name))
			return false;
		Location min = data.getLoc1().getBlockY() <= data.getLoc2().getBlockY() ? data.loc1 : data.loc2;
		Location max = data.getLoc1().getBlockY() > data.getLoc2().getBlockY() ? data.loc1 : data.loc2;

		YamlConfiguration info = getModuleConfig(data.name);
		info.set("name", data.name);
		info.set("difficulty", data.difficulty.toString());

		World world = data.getStart().getWorld();
		float yaw = data.getStart().getYaw();
		float pitch = data.getStart().getPitch();

		info.set("start", data.getStart().toVector().subtract(min.toVector()).toLocation(world, yaw, pitch));
		info.set("end", data.getEnd().toVector().subtract(min.toVector()).toLocation(world, yaw, pitch));
		saveModuleConfig(info, data.name);

		p.teleport(data.getStart());
		ModuleManager.saveSchematic(p, min, max, data.getStart(), data.name);
		VectorUtils.fillWith(min, max, Material.AIR);
		return true;
	}

	public static boolean checkValidity(String s) {
		for (char c : ILLEGAL_CHARACTERS) {
			if (s.contains(c + ""))
				return false;
		}
		return true;
	}

	public static boolean copyModule(String originModule, String newModule) {
		try {
			File oldFile = new File(ModuleManager.moduleFolder(originModule));
			File newFile = new File(ModuleManager.moduleFolder(newModule));
			for (File f : oldFile.listFiles()) {
				FileUtils.copyFileToDirectory(f, newFile);
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean saveModuleConfig(YamlConfiguration config, String name) {
		File infoFile = new File(moduleFolder(name) + "/info.yml");
		try {
			config.save(infoFile);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static YamlConfiguration getModuleConfig(String name) {
		File infoFile = new File(moduleFolder(name) + "/info.yml");
		infoFile.getParentFile().mkdirs();
		try {
			infoFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		YamlConfiguration info = YamlConfiguration.loadConfiguration(infoFile);
		return info;
	}

	public static List<ModuleData> loadModules() {
		File dir = new File(Main.getInstance().getDataFolder() + "/modules");
		dir.mkdirs();
		List<ModuleData> modules = new ArrayList<ModuleData>();
		for (File f : dir.listFiles(File::isDirectory)) {
			if (f.exists()) {
				try {
					modules.add(ModuleData.getFromFile(f.toString()));
				} catch (Exception ex) {
				}
			}
		}
		modules.sort((m1, m2) -> m1.difficulty.getDifficulty() - m2.difficulty.getDifficulty());
		return modules;

	}

	public static Vector getSize(List<ModuleData> datata) {
		Vector currPos = new Vector(0, 0, 0);
		Vector currMin = new Vector(0, 0, 0);
		Vector currMax = new Vector(0, 0, 0);
		for (ModuleData module : datata) {
			Vector moduleMin = module.getMin().clone().add(currPos);
			Vector moduleMax = module.getMax().clone().add(currPos);
			currMin = extremeVector(moduleMin, currMin, false);
			currMax = extremeVector(moduleMax, currMax, true);
			currPos = module.getEnd().toVector();
		}
		return currMax.subtract(currMin);
	}

	public static Vector extremeVector(Vector v1, Vector v2, boolean positive) {
		Vector nVec = new Vector();
		if (!positive) {
			nVec.setX(Math.min(v1.getX(), v2.getX()));
			nVec.setY(Math.min(v1.getY(), v2.getY()));
			nVec.setZ(Math.min(v1.getZ(), v2.getZ()));
		} else {
			nVec.setX(Math.max(v1.getX(), v2.getX()));
			nVec.setY(Math.max(v1.getY(), v2.getY()));
			nVec.setZ(Math.max(v1.getZ(), v2.getZ()));
		}
		return nVec;

	}

}
