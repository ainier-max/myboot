package cbc.boot.myboot.tools.map.gcj02;

import java.io.IOException;
import java.util.Properties;


public class ConfigUtil {

	private static Properties properties;
	static{
		properties = new Properties();
		try {
			properties.load(ConfigUtil.class.getClassLoader().getResourceAsStream("conf.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String get(String key){
		return properties.getProperty(key);
	}
	
	public static void main(String[] args) {
		System.out.println(get("isProxy"));
	}
}
