package eu.spectrum.lang;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Language {
	
	File file;
	
	Properties prop = new Properties();
	
	public Language(File file) {
		this.file = file;
		try {
			prop.load(new FileInputStream(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getName() {
		String name = file.getName();
		return name.substring(0, name.length()-5); //removes .lang suffix
	}
	
	public String format(String s, Object... args) {
		if(prop.containsKey(s))
			s = prop.getProperty(s);
		int count = 0;
		for(Object o : args) {
			s = s.replace("%" + count, o.toString());
			count++;
		}
		return s;
	}
}
