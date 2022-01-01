package eu.spectrum.utils;

import static eu.spectrum.utils.VectorUtils.toLocation;
import static eu.spectrum.utils.VectorUtils.toVector;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;

import com.sk89q.worldedit.CuboidClipboard;

import eu.spectrum.commands.CreateCommand;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

@SuppressWarnings("deprecation")
public class ModuleData {

	public Location start;
	public Location end;
	public Location loc1;
	public Location loc2;

	public String name;
	public Difficulty difficulty = Difficulty.EASY;

	public CuboidClipboard loadedClipboard;

	/* ignore; for removing at canceling edit */
	public Location tmpStart;

	public ModuleData() {
	}

	public ModuleData(Location start, Location end, Location loc1, Location loc2) {
		this.start = start;
		this.loc2 = loc2;
		this.loc1 = loc1;
	}

	public void resetLocs() {
		start = null;
		end = null;
		loc1 = null;
		loc2 = null;
	}

	public void resetCheckpoints() {
		start = null;
		end = null;
	}

	public WorldBox getWorldBox(Location absoluteStart) {
		com.sk89q.worldedit.Vector minVec = toVector(absoluteStart).add(loadedClipboard.getOffset());
		com.sk89q.worldedit.Vector maxVec = minVec.add(loadedClipboard.getSize());
		return new WorldBox(toLocation(minVec, absoluteStart.getWorld()), toLocation(maxVec, absoluteStart.getWorld()));
	}

	public void remove(Location absoluteStart) {
		WorldBox box = getWorldBox(absoluteStart);
		VectorUtils.fillWith(box.getMin(), box.getMax(), Material.AIR);
	}

	@Override
	public String toString() {
		return "start: " + start + ", end: " + end + ", loc1: " + loc1 + ", loc2: " + loc2 + ", name: " + name
				+ ", difficulty: " + difficulty.toString();
	}

	public void setAbsoluteLocations(Location absoluteStart) {
		WorldBox box = getWorldBox(absoluteStart);
		loc1 = box.getMin();
		loc2 = box.getMax();
	}

	public static ModuleData getFromFile(String moduleFolder) {
		ModuleData data = new ModuleData();
		YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(moduleFolder + "/info.yml"));
		data.setStart((Location) config.get("start"));
		data.setEnd((Location) config.get("end"));
		data.name = config.getString("name");
		data.difficulty = Difficulty.valueOf(config.getString("difficulty"));
		data.loadedClipboard = ModuleManager.getClipboard(data.name);
		com.sk89q.worldedit.Vector min = toVector(data.start).add(data.loadedClipboard.getOffset());
		data.setLoc1(toLocation(min));
		data.setLoc2(toLocation(min.add(data.loadedClipboard.getSize())));
		return data;
	}

	public void applyChanges(String moduleFolder) {
		File infoFile = new File(moduleFolder + "/info.yml");
		YamlConfiguration info = YamlConfiguration.loadConfiguration(infoFile);
		info.set("name", this.name);
		info.set("difficulty", this.difficulty.toString());
		try {
			info.save(infoFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Vector getMin() {
		return VectorUtils.getMin(loc1.toVector(), loc2.toVector());
	}

	public Vector getMax() {
		return VectorUtils.getMax(loc1.toVector(), loc2.toVector());
	}

	public void setLoc1(Location loc1) {
		this.loc1 = loc1;
	}

	public void setLoc2(Location loc2) {
		this.loc2 = loc2;
	}

	public Location getLoc1() {
		return loc1;
	}

	public Location getLoc2() {
		return loc2;
	}

	public List<CheckField> getCheckFields() {
		List<CheckField> fields = new ArrayList<CheckField>();
		try {
			fields.add(new CheckField(this.getClass().getField("start"), "Start",true));
			fields.add(new CheckField(this.getClass().getField("end"), "End",true));
			fields.add(new CheckField(this.getClass().getField("loc1"), "Ecke 1",false));
			fields.add(new CheckField(this.getClass().getField("loc2"), "Ecke 2",false));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return fields;
	}

	public boolean allSet() {
		boolean set = true;
		try {
			for (CheckField field : getCheckFields()) {
				if (field.field.get(this) == null)
					set = false;
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			set = false;
		}
		return set;
	}
	
	public CheckField getFieldByName(String name) {
		for(CheckField field : getCheckFields()) {
			if(field.field.getName().equalsIgnoreCase(name)) return field;
		}
		return null;
	}

	public boolean isField(String name) {
		for (CheckField field : getCheckFields()) {
			if (field.field.getName().equalsIgnoreCase(name))
				return true;
		}
		return false;
	}
	
	public boolean setField(String name, Object value) {
		if(isField(name)) {
			try {
				this.getClass().getField(name).set(this, value);
				return true;
			} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
//	public boolean locExists(Location loc) {
//		lolol
//	}

	public List<TextComponent> toStates() {
		List<TextComponent> comps = new ArrayList<>();
		List<CheckField> fields = getCheckFields();

		try {
			for (CheckField field : fields) {
				boolean isNull = field.field.get(this) == null;
				TextComponent fieldSection = new TextComponent((isNull ? "§2§l[x]" : "§a§l[✓]") + " §r§7" + field.name
						+ " wurde " + (isNull ? "nicht" : "") + " gesetzt");
				fieldSection.setClickEvent(new ClickEvent(Action.RUN_COMMAND,
						isNull ? (CreateCommand.setCommand + " " + field.field.getName())
								: (CreateCommand.removeCommand + " " + field.field.getName())));
				TextComponent fieldHover = new TextComponent(
						(isNull ? "§a" : "§c") + field.name + " " + (isNull ? "setzen" : "löschen"));
				fieldSection
						.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] { fieldHover }));
				comps.add(fieldSection);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return comps;
	}

	public static class CheckField {

		public Field field;
		public String name;
		public boolean needsPlate;

		public CheckField(Field field, String name, boolean needsPlate) {
			this.field = field;
			this.name = name;
			this.needsPlate = needsPlate;
		}

	}

	public Location getStart() {
		return start;
	}

	public void setStart(Location start) {
		this.start = start;
	}

	public Location getEnd() {
		return end;
	}

	public void setEnd(Location end) {
		this.end = end;
	}

}
