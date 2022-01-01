package eu.spectrum.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class VectorUtils {
	public static Vector getMin(Vector loc1, Vector loc2) {
		return new Vector(Math.min(loc1.getX(), loc2.getX()), Math.min(loc1.getY(), loc2.getY()),
				Math.min(loc1.getZ(), loc2.getZ()));
	}
	
	public static Vector getMax(Vector loc1, Vector loc2) {
		return new Vector(Math.max(loc1.getX(), loc2.getX()), Math.max(loc1.getY(), loc2.getY()),
				Math.max(loc1.getZ(), loc2.getZ()));
	}
	
	public static com.sk89q.worldedit.Vector toVector(Location loc) {
		com.sk89q.worldedit.Vector res = new com.sk89q.worldedit.Vector(loc.getX(), loc.getY(), loc.getZ());
		return res;
	}

	public static Location toLocation(com.sk89q.worldedit.Vector v) {
		Location res = new Location(null, v.getX(), v.getY(), v.getZ());
		return res;
	}
	
	public static Location toLocation(Vector v) {
		Location res = new Location(null, v.getX(), v.getY(), v.getZ());
		return res;
	}
	
	public static Location toLocation(Vector v, World world) {
		Location res = new Location(world, v.getX(), v.getY(), v.getZ());
		return res;
	}
	
	public static Location toLocation(com.sk89q.worldedit.Vector v, World world) {
		Location res = new Location(world, v.getX(), v.getY(), v.getZ());
		return res;
	}
	
	public static void fillWith(Location loc1, Location loc2, Material mat) {
		Location min = toLocation(getMin(loc1.toVector(), loc2.toVector()), loc1.getWorld());
		Location max = toLocation(getMax(loc1.toVector(), loc2.toVector()), loc1.getWorld());
		
		for(int x = min.getBlockX(); x <= max.getBlockX();x++) {
			for(int y = min.getBlockY(); y <= max.getBlockY(); y++) {
				for(int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
					loc1.getWorld().getBlockAt(x,y,z).setType(mat);
				}
			}
		}
	}
}
