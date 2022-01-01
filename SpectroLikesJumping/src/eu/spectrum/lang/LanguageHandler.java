package eu.spectrum.lang;

import java.io.File;
import java.util.ArrayList;

public class LanguageHandler {

	File dir;
	Language selected;

	public LanguageHandler(File dir) {
		this.dir = dir;
	}

	public LanguageHandler(File dir, String def) {
		this.dir = dir;
		loadLanguage(def);
	}

	public ArrayList<Language> getLanguages() {
		ArrayList<Language> languages = new ArrayList<>();

		for (File file : dir.listFiles(f -> f.getName().endsWith(".lang")))
			languages.add(new Language(file));

		return languages;
	}

	public String format(String s) {
		if (selected == null)
			return s;
		return selected.format(s);
	}

	public void loadLanguage(Language lang) {
		selected = lang;
	}

	public Language loadLanguage(String s) {
		for (Language lang : getLanguages())
			if (lang.getName().equalsIgnoreCase(s))
				return selected = lang;
		return selected = null;
	}
	
	public Language getSelected() {
		return selected;
	}

}
