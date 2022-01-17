package eu.spectrum.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;

import eu.realms.common.display.ScoreboardAPI.ScoreTeam;
import eu.realms.common.display.ScoreboardAPI.TablistScoreboard;
import eu.spectrum.main.Systems;

public class TeamHandler {

	public static HashMap<JumpTeam, ScoreTeam> teams;

	public static boolean isInited = false;

	public static void initTeams() {
		teams = new HashMap<>();
		for (JumpTeam team : JumpTeam.values()) {
			teams.put(team, new ScoreTeam(team.getColor().toString()+ (""+team.name.charAt(0)).toUpperCase() + " §7| ", team.getName()));
		}
		isInited = true;
	}

	public static ScoreTeam getTeam(JumpTeam team) {
		if (teams.containsKey(team))
			return teams.get(team);
		return null;
	}

	public static void updateTabScoreboard() {
		if (isInited) {
			TablistScoreboard scoreboard = new TablistScoreboard(new ArrayList<>(teams.values()));
			scoreboard.createScoreboard();
			scoreboard.applyScoreboard();
		}
	}
	
	public static List<Player> getPlayers(JumpTeam team) {
		if(!isInited) return new ArrayList<>();
		return teams.get(team).members;
	}
	
	public static boolean isFull(JumpTeam team) {
		if(!isInited) return false;
		return teams.get(team).members.size()>=Systems.maxTeamPlayers;
	}
 
	public static JumpTeam findEmptyTeam() {
		for (JumpTeam team : teams.keySet()) {
			if (!isFull(team))
				return team;
		}
		return null;
	}

}
