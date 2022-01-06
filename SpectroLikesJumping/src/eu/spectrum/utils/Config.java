package eu.spectrum.utils;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;

import eu.spectrum.main.Main;

public class Config {

	File file;
	public YamlConfiguration yml;
	
	public Config(String path) {
		file = new File(Main.getInstance().getDataFolder()+path);
		file.getParentFile().mkdirs();
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		yml = YamlConfiguration.loadConfiguration(file);
	}
	
	public void saveConfig() {
		if(yml!=null && file!=null) {
			try {
				yml.save(file);
			} catch (IOException e) {
			}
		}
	}

}
