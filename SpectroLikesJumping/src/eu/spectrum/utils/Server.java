package eu.spectrum.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import eu.spectrum.main.Main;

public class Server {
	
	public static void broadcast(String message) {
		broadcastPure(Main.PREFIX + message);
	}
	
	public static void broadcastPure(String message) {
		for(Player p : Bukkit.getOnlinePlayers()) {
			p.sendMessage(message);
		}
	}

}
