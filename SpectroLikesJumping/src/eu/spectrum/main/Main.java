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

import eu.spectrum.commands.CreateCommand;
import eu.spectrum.commands.SetupCommand;
import eu.spectrum.commands.StartCommand;
import eu.spectrum.lang.LanguageHandler;
import eu.spectrum.listeners.ConnectionListener;
import eu.spectrum.listeners.CreationListener;
import eu.spectrum.listeners.GameListener;
import eu.spectrum.listeners.SecurityListener;

public class Main extends JavaPlugin {

	public static final String PREFIX = "�5SpectroLikesJumping �r�7-> ";

	private static Main instance;

	public static final String worldName = "mapa";
	
	public boolean loadingWorld = false;
	
	public static LanguageHandler handler = new LanguageHandler(new File("langs"));

	@Override
	public void onEnable() {
		instance = this;
		System.out.println(PREFIX + " -> Plugin started.");
		this.getCommand("module").setExecutor(new CreateCommand());
		this.getCommand("setup").setExecutor(new SetupCommand());
		this.getCommand("start").setExecutor(new StartCommand());

		PluginManager manager = Bukkit.getPluginManager();
		manager.registerEvents(new CreationListener(), this);
		manager.registerEvents(new ConnectionListener(), this);
		manager.registerEvents(new SecurityListener(), this);
		manager.registerEvents(new GameListener(), this);

		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p.getWorld().getName().equalsIgnoreCase(worldName))
				p.teleport(SetupCommand.getLocation("waiting_lobby"));
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

		Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
			World map = WorldCreator.name(worldName).type(WorldType.FLAT).generateStructures(false)
					.environment(World.Environment.NORMAL).generator(new VoidWorldGenerator()).createWorld();
			map.setGameRuleValue("doDaylightCycle", "false");
			Bukkit.getServer().getWorlds().add(map);
			System.out.println("Jump World loaded.");
			loadingWorld = false;
		}, 20*2);

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

	public class VoidWorldGenerator extends ChunkGenerator {

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
