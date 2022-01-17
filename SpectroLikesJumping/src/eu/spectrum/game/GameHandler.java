package eu.spectrum.game;

import static eu.spectrum.main.Systems.MIN_PLAYERS;
import static eu.spectrum.main.Systems.maxLobbyCount;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;

import eu.realms.common.display.ScoreboardAPI.SidebarScoreboard;
import eu.spectrum.game.states.IngameState;
import eu.spectrum.game.states.LobbyState;
import eu.spectrum.game.states.PVPState;

public class GameHandler {

	private static GameState gameStateMechanic;
	public static EnumGameState gameState;
	
	private static List<GameState> gameStates = new ArrayList<>();
	public static HashMap<Player, PlayerData> playerData = new HashMap<>();
	
	public static String map;
	
	public static void initHandler() {
		initGameStates();
		changeGameState(EnumGameState.LOBBY);
	}
	
	public static void changeGameState(EnumGameState state) {
		changeGameState(state, null);
	}
	
	public static void resetScoreboards() {
		for (PlayerData data : playerData.values()) {
			if (data.scoreboard != null) {
				data.scoreboard.destroy(data.p);
				data.scoreboard = null;
			}
			else {
				SidebarScoreboard board = new SidebarScoreboard("");
				board.create(data.p);
				board.destroy(data.p);
			}
		}
	}
	
	public static void changeGameState(EnumGameState state,Player p) {
		GameState s = getGameState(state);
		if(s!=null) {
			if(gameStateMechanic!=null) {
				gameStateMechanic.onStop(p);
				resetScoreboards();
			}
			gameStateMechanic = s;
			
			gameState = state;
			gameStateMechanic.showScoreboard();
			gameStateMechanic.onStart(p);
		}
	}
	
	private static void initGameStates() {
		gameStates.add(new IngameState());
		gameStates.add(new PVPState());
		gameStates.add(new LobbyState());
	}
	
	public static GameState getGameState(EnumGameState state) {
		for(GameState s : gameStates) {
			if(s.state==state) return s;
		}
		return null;
	}

}
