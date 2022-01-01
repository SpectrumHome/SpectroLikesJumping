package eu.spectrum.game;

import org.bukkit.Location;

import com.sk89q.worldedit.Vector;
import static eu.spectrum.utils.VectorUtils.*;
import eu.spectrum.utils.ModuleData;

public class PlayerData {
	
	public int currentModule;
	Location start;
	Location end;
	public int minHeight;

	public PlayerData(Location start) {
		currentModule = 0;
		setStart(start);
	}
	
	public void setStart(Location start) {
		this.start = start;
		calcEnd();
	}
	
	public Location getStart() {
		return start;
	}
	
	public Location getEnd() {
		if(end==null) calcEnd();
		return end;
	}
	
	public void setEnd(Location end) {
		this.end = end;
	}
	
	@SuppressWarnings("deprecation")
	public void calcEnd() {
		ModuleData mod = GameHandler.gameModules.get(currentModule);
		Vector endVec = toVector(start).add(toVector(mod.getEnd()).subtract(toVector(mod.getStart())));
		end = new Location(start.getWorld(),endVec.getX(),endVec.getY(),endVec.getZ());
		
		minHeight = toVector(start).add(mod.loadedClipboard.getOffset()).getBlockY();
	}
}
