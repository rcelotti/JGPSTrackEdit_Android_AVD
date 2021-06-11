package jgpstrackedit.config;

import jgpstrackedit.international.International;
import jgpstrackedit.util.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;

/**
 * Serialize and deserialize the configuration. Set default values.
 * 
 * @author Hubert
 */
public class Configuration {
	private static final Logger logger = LoggerFactory.getLogger(Configuration.class);
	private static Properties properties = null;
	private static final List<ConfigurationObserver> observers = new ArrayList<>();
	
	private static final Map<String, String> propertiesDescriptions = new HashMap<>();
	
	static {
		propertiesDescriptions.put("AVERAGESPEED", "Average speed in kilometers per hour.");
		propertiesDescriptions.put("BREAKRATIO", "Ratio of break time to driving time. E.g. 0.5 that means, for one hour driving the break is half an hour.");
		propertiesDescriptions.put("COUNTRY_SPECIFIC_MAP", "");
		propertiesDescriptions.put("GUILOOKFEEL", "Application look and feel. Possible values: System, Cross-Platform");
		propertiesDescriptions.put("INCLINETIME100METERS", "Additional time to climb up 100 meter (min) in minutes.");
		propertiesDescriptions.put("LOCALE", "The locale of the application.");
		propertiesDescriptions.put("LAST_WORK_DIR", "Last open work directory.");
		propertiesDescriptions.put("MAPEXTRACT", "");
		propertiesDescriptions.put("MAPTYPE", "The type of the used map. Possible values: OpenStreetMap, OpenCycleMap, ThunderforestCycleMap, MapQuest, MapQuestSat, MapQuestHybride, HikeBikeMap, 4UMap");
		propertiesDescriptions.put("MAP_API_KEY_THUNDER_FOREST", "The API key for the thunder forest maps. See https://www.thunderforest.com/");
		propertiesDescriptions.put("MAXTOURTIME", "The maximum time of the tour in hours.");
		propertiesDescriptions.put("MAX_TILES_IN_MEMORY", "");
		propertiesDescriptions.put("POINT_DIAMETER", "Size of points");
		propertiesDescriptions.put("ROUTINGAVOIDLIMITEDACCESS", "");
		propertiesDescriptions.put("ROUTINGAVOIDTOLLROAD", "");
		propertiesDescriptions.put("ROUTINGPOINTDISTANCE", "");
		propertiesDescriptions.put("ROUTINGTYPE", "Type of routing. Possible values: bicycle, fastest, shortest, pedestrian, multimodal.");
		propertiesDescriptions.put("SELECTED_LINE_WIDTH", "");
		propertiesDescriptions.put("SHOW_DIRECTION_BUTTONS", "");
		propertiesDescriptions.put("SHOW_HELP_ON_STARTUP", "");
		propertiesDescriptions.put("SHOW_MAP_ON_STARTUP", "");
		propertiesDescriptions.put("UNSELECTED_LINE_WIDTH", "");
	}

	public static void addConfigurationObserver(ConfigurationObserver observer) {
		observers.add(observer);
	}

	public static void removeConfigurationObserver(ConfigurationObserver observer) {
		observers.remove(observer);
	}
	
	protected static void notifyConfigurationObservers() {
		for (ConfigurationObserver observer:observers) {
			observer.configurationChanged();
		}
	}

	protected static void checkInit() {
		if (properties == null) {
			properties = new SortedProperties();
			properties.setProperty("SHOW_MAP_ON_STARTUP","1");
			properties.setProperty("MAX_TILES_IN_MEMORY","250");
			properties.setProperty("SELECTED_LINE_WIDTH","1");
			properties.setProperty("UNSELECTED_LINE_WIDTH","1");
			properties.setProperty("POINT_DIAMETER", "7");
			properties.setProperty("SHOW_DIRECTION_BUTTONS", "0");
			properties.setProperty("SHOW_HELP_ON_STARTUP", "1");
			properties.setProperty("COUNTRY_SPECIFIC_MAP", "1");
			properties.setProperty("MAPEXTRACT", "");
			properties.setProperty("GUILOOKFEEL","System");
			properties.setProperty("ROUTINGTYPE","bicycle");
			properties.setProperty("ROUTINGPOINTDISTANCE","10");
			properties.setProperty("ROUTINGAVOIDLIMITEDACCESS","1");
			properties.setProperty("ROUTINGAVOIDTOLLROAD","1");
			properties.setProperty("AVERAGESPEED","20.0");  // km/h
			properties.setProperty("INCLINETIME100METERS","10.0"); // min
			properties.setProperty("BREAKRATIO","0.5"); //
			properties.setProperty("MAXTOURTIME","8.0"); // h
			properties.setProperty("MAPTYPE","OpenStreetMap");
			properties.setProperty("MAP_API_KEY_THUNDER_FOREST", "");
			properties.setProperty("PROXY","");
			properties.setProperty("PROXYPORT","");
			properties.setProperty("AUTOMATIC_COLORS","1");
			properties.setProperty("LOCALE", Locale.getDefault().toString());
			properties.setProperty("LAST_WORK_DIR", getDefaultPath());

			try(final BufferedReader reader = Files.newBufferedReader(new File("JGPSTrackEdit.properties").toPath(), Charset.forName("UTF-8"))) {
				properties.load(reader);
			} catch (FileNotFoundException e) {
				logger.warn("File not found while loading properties from file JGPSTrackEdit.properties!");
				saveProperties();
			} catch (IOException e) {
				logger.error("Exception while loading properties from file JGPSTrackEdit.properties!", e);
			}

			setProperty("LAST_WORK_DIR", getProperty("LAST_WORK_DIR"));
			International.setCurrentLocale(
					new Locale(
							properties.getProperty("LOCALE").split("_")[0],
							properties.getProperty("LOCALE").split("_")[1]));
			logger.info("Current locale: " + International.getCurrentLocale().toString());
		}
	}

	private static String getLastWorkDir(String value) {
		if (value == null || value.trim().length() == 0) {
			value = getDefaultPath();
		}
		if (pathNotExists(value)) {
			value = getDefaultPath();
		}
		if (isWindowsSystem()) {
			value = value.replace("\\\\", "\\");
		}
		return value;
	}

	private static boolean pathNotExists(String path) {
		try {
			return Files.notExists(new File(path).toPath());
		} catch (Exception e) {
			return true;
		}
	}

	public static String getProperty(String property) {
		checkInit();
		String value = (String)properties.get(property);
		if(property.equals("LAST_WORK_DIR")) {
			value = getLastWorkDir(value);
		}
		return value;
	}
	
	public static int getIntProperty(String property) {
		checkInit();
		String value = getProperty(property);
		return Parser.parseInt(value);
	}

	/**
	 * Returns the value of the given property as boolean value. True is stored as "1", false is stored as "0"
	 * @param property the property
	 * @return boolean value of the given property
	 */
	public static boolean getBooleanProperty(String property) {
		checkInit();
		String value = getProperty(property);
		return Parser.parseInt(value) == 1;
	}

	private static String getDefaultPath() {
		return System.getProperty("user.home");
	}

	private static String rewriteLastWorkDir(String value) {
		if(value == null || value.trim().length() == 0) {
			value = getDefaultPath();
		}
		if(isWindowsSystem()) {
			value = value.replace("\\", "\\\\");
		}
		return value;
	}

	public static void setProperty(String property, String value) {
		checkInit();
		if(property.equals("LAST_WORK_DIR")) {
			value = rewriteLastWorkDir(value);
		}
		properties.setProperty(property, value);
	}

	private static boolean isWindowsSystem() {
		return System.getProperty("os.name").startsWith("Windows");
	}

	public static void saveProperties() {
		notifyConfigurationObservers();

		try(final BufferedWriter writer = Files.newBufferedWriter(new File("JGPSTrackEdit.properties").toPath(), Charset.forName("UTF-8"))) {
			writer.write("#\n");
			writer.write(String.format("# Application properties for JGPSTrackEdit (%s)\n", new Date().toString()));
			
			final Enumeration<Object> keys = properties.keys();
			while(keys.hasMoreElements()) {
				final String key = keys.nextElement().toString();
				final Object value = properties.getProperty(key);
				final String description = propertiesDescriptions.get(key);
				
				writer.write("#\n");
				if(description != null && description.length() > 0) {
					writer.write(String.format("# %s\n", description));
				}
				
				writer.write(String.format("%s=%s\n", key, value));
			}
		} catch (FileNotFoundException e) {
			logger.error("File JGPSTrackEdit.properties not found!", e);
		} catch (IOException e) {
			logger.error("Exception while saving properties to file JGPSTrackEdit.properties!", e);
		}
	}

	public static double getDoubleProperty(String property) {
		checkInit();
		String value = getProperty(property);
		return Parser.parseDouble(value);
	}

	public static double getHourProperty(String property) {
		checkInit();
		String value = getProperty(property);
		return Parser.parseTime(value);
	}
	
	private static class SortedProperties extends Properties {
		private static final long serialVersionUID = 1L;

		@Override
		public synchronized Enumeration<Object> keys() {
			Enumeration<Object> keysEnum = super.keys();
			ArrayList<String> keyList = new ArrayList<>();
			while (keysEnum.hasMoreElements()) {
				keyList.add((String) keysEnum.nextElement());
			}
			Collections.sort(keyList);
			return (new Vector<Object>(keyList)).elements();
		}
	}
}
