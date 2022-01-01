package eu.spectrum.utils;

import org.bukkit.Location;

public class WorldBox {
	
	Location min;
	Location max;
	
	public WorldBox(Location min, Location max) {
		this.min = min;
		this.max = max;
	}
	
	public Location getMax() {
		return max;
	}
	
	public Location getMin() {
		return min;
	}
	
	public void setMax(Location max) {
		this.max = max;
	}
	
	public void setMin(Location min) {
		this.min = min;
	}

	
}
