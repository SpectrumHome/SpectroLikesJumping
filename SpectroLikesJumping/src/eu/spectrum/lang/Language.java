package eu.spectrum.lang;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;

public class Language {

	File file;

	HashMap<String, String> phrases = new HashMap<>();

	public Language(File file) {
		this.file = file;
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
			reader.lines().forEach(s -> {
				s = s.replaceAll("&", "§");
				String[] arr = s.split("=");
				if (arr.length > 1) {
					phrases.put(arr[0], arr[1]);
				}
			});
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getName() {
		String name = file.getName();
		return name.substring(0, name.length() - 5); // removes .lang suffix
	}

	public String format(String s, Object... args) {
		if (phrases.containsKey(s))
			s = phrases.get(s);
		int count = 0;
		for (Object o : args) {
			s = s.replace("%" + count, o.toString());
			count++;
		}
		return s;
	}

}