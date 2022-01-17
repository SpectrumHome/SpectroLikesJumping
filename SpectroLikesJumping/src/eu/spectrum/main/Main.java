package eu.spectrum.main;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import eu.realms.commands.LootTableCommand;
import eu.realms.common.display.Server;
import eu.spectrum.commands.ModuleCommand;
import eu.spectrum.commands.SetupCommand;
import eu.spectrum.commands.SkipCommand;
import eu.spectrum.commands.StartCommand;
import eu.spectrum.game.GameHandler;
import eu.spectrum.game.TeamHandler;
import eu.spectrum.lang.LanguageHandler;
import eu.spectrum.listeners.ConnectionListener;
import eu.spectrum.listeners.GameListener;
import eu.spectrum.listeners.SecurityListener;
import eu.spectrum.main.Systems.GameLocation;

public class Main extends JavaPlugin {

	public static final String PREFIX = "§5SectroLikesJumping §7-> ";

	private static Main instance;

	public static final String worldName = "mapa";

	public static boolean loadingWorld = false;

	public static LanguageHandler handler;

	@Override
	public void onEnable() {
		instance = this;

		handler = new LanguageHandler(new File(getDataFolder() + "/lang"), "DEUTSCH");

		System.out.println(PREFIX + " -> Plugin started.");
		this.getCommand("module").setExecutor(new ModuleCommand());
		this.getCommand("setup").setExecutor(new SetupCommand());
		this.getCommand("start").setExecutor(new StartCommand());
		this.getCommand("skip").setExecutor(new SkipCommand());
		this.getCommand("loottable").setExecutor(new LootTableCommand());

		PluginManager manager = Bukkit.getPluginManager();
		manager.registerEvents(new ConnectionListener(), this);
		manager.registerEvents(new SecurityListener(), this);
		manager.registerEvents(new GameListener(), this);

		if (SetupCommand.missingLocs().length > 0)
			Server.broadcast(SetupCommand.missingLocError());

		createJumpWorld();
		worldModifications();

		TeamHandler.initTeams();

		GameHandler.initHandler();
		
	}

	public static void worldModifications() {
		for(String locName : SetupCommand.getConfig().yml.getKeys(false)) {
			Location loc = SetupCommand.getLocation(GameLocation.valueOf(locName.toUpperCase()));
			if(loc!=null) {
				if(!Bukkit.getWorlds().contains(loc.getWorld())) Bukkit.getWorlds().add(loc.getWorld());
				loc.getWorld().setGameRuleValue("doDaylightCycle", "false");
			}
		}
	}
	
	public static void createJumpWorld() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p.getWorld().getName().equalsIgnoreCase(worldName))
				SetupCommand.forceTeleport(p, GameLocation.WAITING_LOBBY);
		}

		loadingWorld = true;

		Bukkit.unloadWorld(worldName, false);

		File f;
		if ((f = new File(worldName)).exists()) {
			System.out.println("Deleting..");
			try {
				FileUtils.deleteDirectory(f);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
			World map = WorldCreator.name(worldName).type(WorldType.FLAT).generateStructures(false)
					.environment(World.Environment.NORMAL).generator(new VoidWorldGenerator()).createWorld();
			map.setGameRuleValue("doDaylightCycle", "false");
			Bukkit.getServer().getWorlds().add(map);
			System.out.println("Jump World loaded.");
			loadingWorld = false;
		}, 20 * 2);
	}

	public World getWorld() {
		return Bukkit.getWorld(worldName);
	}

	public static Main getInstance() {
		return instance;
	}

	@Override
	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
		return new VoidWorldGenerator();
	}

	public static class VoidWorldGenerator extends ChunkGenerator {

		public List<BlockPopulator> getDefaultPopulators(World world) {
			return Arrays.asList(new BlockPopulator[0]);
		}

		public boolean canSpawn(World world, int x, int z) {
			return true;
		}

		public byte[] generate(World world, Random rand, int chunkx, int chunkz) {
			return new byte[32768];
		}

		public Location getFixedSpawnLocation(World world, Random random) {
			return new Location(world, 0, 128, 0);
		}

	}

}
