package eu.spectrum.game;

import static eu.realms.common.vector.VectorUtils.*;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.Vector;

import eu.realms.common.display.ScoreboardAPI.ScoreTeam;
import eu.realms.common.display.ScoreboardAPI.SidebarScoreboard;
import eu.realms.common.display.TitleAPI;
import eu.spectrum.game.states.IngameState;
import eu.spectrum.main.Main;
import eu.spectrum.main.Systems;
import eu.spectrum.utils.ModuleData;

public class PlayerData {

	public Player p;

	public int currentModule;
	public Location start;
	Location end;
	public int minHeight;
	public int lifes;

	private JumpTeam team;

	public SidebarScoreboard scoreboard;

	public PlayerData(Player p) {
		currentModule = -1;
		lifes = Systems.MAX_LIFES;
		this.p = p;
	}

	public PlayerData(Player p, Location start) {
		currentModule = -1;
		this.start = start;
		lifes = Systems.MAX_LIFES;
		this.p = p;
	}

	public void setStart(Location start) {
		this.start = start;
		calcEnd();
	}

	public void changeTeam(JumpTeam newTeam) {
		if (team != null) {
			ScoreTeam oldTeam = TeamHandler.getTeam(team);
			if (oldTeam.members.contains(p)) {
				oldTeam.members.remove(p);
			}
		}
		team = newTeam;
		if (newTeam != null) {
			ScoreTeam nTeam = TeamHandler.getTeam(newTeam);
			if (!nTeam.members.contains(p)) {
				nTeam.members.add(p);
			}
			TitleAPI.sendTitle(p, newTeam.getColor() + newTeam.getName(), Main.handler.format("team.apply"), minHeight,
					20, 20);
		}
		TeamHandler.updateTabScoreboard();
	}

	public JumpTeam getTeam() {
		return team;
	}

	public Location getStart() {
		return start;
	}

	public Location getEnd() {
		if (end == null)
			calcEnd();
		return end;
	}

	public void setEnd(Location end) {
		this.end = end;
	}

	public int getLifes() {
		return lifes;
	}

	public boolean playerDied() {
		if (lifes >= 1) {
			lifes--;
		}
		if (lifes <= 0)
			return false;
		return true;
	}

	public boolean lives() {
		return lifes > 0;
	}

	public boolean spectator() {
		return !lives();
	}

	@SuppressWarnings("deprecation")
	public void calcEnd() {
		ModuleData mod = IngameState.gameModules.get(currentModule);
		Vector endVec = toVector(start).add(toVector(mod.getEnd()).subtract(toVector(mod.getStart())));
		end = new Location(start.getWorld(), endVec.getX(), endVec.getY(), endVec.getZ());

		minHeight = toVector(start).add(mod.loadedClipboard.getOffset()).getBlockY();
	}
}
