package org.recommender101.guiconfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.recommender101.gui.annotations.R101Class;

public class InternalR101Class {

	private Class<?> classObject;
	private String displayName;
	private String description;

	/**
	 * Only set if no class object present
	 */
	private String fullClassName = "";

	private ArrayList<InternalR101Setting> settings;

	/**
	 * Creates a new InternalR101Class object based on a settings string which
	 * is being parsed. The method tries to retrieve a corresponding class
	 * object. However, it will also work if the class doesn't exist.
	 * 
	 * @param parseString
	 *            String to parse, for example
	 *            "org.recommender101.recommender.extensions.funksvd.FunkSVDRecommender:numFeatures=50|initialSteps=100"
	 */
	public InternalR101Class(String parseString) {
		this.settings = new ArrayList<InternalR101Setting>();

		String className = "";
		String options = "";
		if (parseString.contains(":") && parseString.split(":").length > 1) {
			String[] split = parseString.split(":");
			className = split[0];
			options = split[1];
		} else {
			className = parseString;
		}

		// Try to retrieve class object
		Class<?> c = ClassScanner.findClass(className);
		if (c != null) {
			// Class has been found
			// System.out.println("Class found: " + c.getName());
			this.classObject = c;

			R101Class annotation = ClassScanner.getClassAnnotation(c);
			if (annotation != null) {
				if (annotation.name().equals("unknown")) {
					this.displayName = attemptSimpleClassName(c.getName());
				} else {
					this.displayName = annotation.name();
				}

				this.description = annotation.description();
			} else {
				this.displayName = attemptSimpleClassName(c.getName());
				this.description = "";
			}
		} else {
			// Class not found
			this.classObject = null;
			this.displayName = attemptSimpleClassName(className);
			this.fullClassName = className;
			this.description = "";
		}

		// Create settings, match them if class object exists
		if (!options.equals("")) {
			String[] splitOptions = options.split("\\|");
			for (String s : splitOptions) {
				InternalR101Setting setting = new InternalR101Setting(s, new ArrayList<String>());
				this.settings.add(setting);
			}
		}

		// Try to retrieve annotated settings and match them to the settings
		// which have been loaded to avoid duplicates
		if (c != null) {
			ArrayList<InternalR101Setting> settings = ClassScanner.getClassSettings(c);
			
			if (settings != null && settings.size() > 0) {
				for (InternalR101Setting currSetting : settings) {					
					// Search if the setting has been loaded
					// If yes, take its current value and replace it with this
					// better, annotated version
					// Keep the position of the setting
					// Otherwise add it at the end
					boolean currSettingfound = false;
					for (int i = 0; i < this.settings.size(); i++) {
						InternalR101Setting prevSetting = this.settings.get(i);
						if (prevSetting.equals(currSetting)) {
							currSetting.setCurrentValue(prevSetting.getCurrentValue());
							this.settings.remove(prevSetting);
							this.settings.add(i, currSetting);
							currSettingfound = true;
							currSetting.setSettingEnabled(true);
						}
					}
					if (!currSettingfound)
					{
						this.settings.add(currSetting);
					}
				}
			}
		}

		//addDummyLines();

	}

	/**
	 * Used for assembly of the properties file
	 */
	@Override
	public String toString() {
		String ret = "";

		// Add fully qualified class name
		if (this.classObject != null) {
			ret += classObject.getName();
		} else {
			ret += fullClassName;
		}

		if (settings.size() > 0) {
			ret += ":";

			// Add settings
			for (InternalR101Setting s : this.settings) {
				if ((!s.getSettingName().equals("")) && s.isSettingEnabled()) {
					ret += s.toString() + "|";
				}
			}

			// Remove last pipe
			ret = ret.substring(0, ret.length() - 1);
		}
		return ret;
	}

	/*
	 * Similar to toString(), but creates a better readable String to show in
	 * the GUI
	 * 
	 * @return A readable String representing the class and its settings.
	 */
	/*public String toPrettyString() {
		String ret = "";

		// Add name
		ret += this.getDisplayName();

		// 20 because of the dummy lines which are always added
		if (settings.size() > 20) {
			ret += " (";

			// Add settings
			for (InternalR101Setting s : this.settings) {
				if (!s.getSettingName().equals("")) {
					ret += s.toString() + ", ";
				}
			}

			// Remove last pipe
			ret = ret.substring(0, ret.length() - 2);
			ret += ")";
		}

		return ret;
	}*/
	
	/**
	 * Creates a string containing all of the settings. used in the GUI to display the current selection of classes.
	 */
	public String getPrettySettingsString() {
		String ret = "";
		for (InternalR101Setting s : this.settings) {
			if ((!s.getSettingName().equals("")) && s.isSettingEnabled()) {
				ret += s.toString() + ", ";
			}
		}
		if (ret.endsWith(", "))
		{
			ret = ret.substring(0, ret.length()-2);
		}
		return ret;
	}

	/**
	 * Attempts to get a simple class name
	 */
	private String attemptSimpleClassName(String fullName) {
		if (fullName.contains(".")) {
			return fullName.substring(fullName.lastIndexOf(".") + 1);
		}

		return fullName;
	}

	public InternalR101Class(Class<?> c, String displayName, String description, ArrayList<InternalR101Setting> settings) {
		setClassObject(c);
		if (displayName.equals("unknown")) {
			setDisplayName(c.getSimpleName());
		} else {
			setDisplayName(displayName);
		}
		setDescription(description);
		setSettings(settings);

		//addDummyLines();
	}

	/*private void addDummyLines() {
		// Add 20 empty settings for custom typing
		
		// When a deep copy is created, this would be called again, so we check if the last setting already is a dummy setting
		if (settings.size() > 0)
		{
			if (settings.get(settings.size()-1).getSettingName().equals(""))
			{
				return;
			}
		}
		
		for (int i = 0; i < 20; i++) {
			settings.add(new InternalR101Setting());
		}
	}*/

	/**
	 * This clone method is necessary in order to be able to add multiple
	 * classes of the same type to a properties file.
	 */
	public InternalR101Class getDeepCopy() {

		ArrayList<InternalR101Setting> newSettings = new ArrayList<InternalR101Setting>();

		for (InternalR101Setting s : settings) {
			newSettings.add(s.getCopy());
		}

		InternalR101Class newClass = new InternalR101Class(classObject, displayName, description, newSettings);

		return newClass;
	}

	public Class<?> getClassObject() {
		return classObject;
	}

	public void setClassObject(Class<?> classObject) {
		this.classObject = classObject;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ArrayList<InternalR101Setting> getSettings() {	
		sortSettingsAlphabetically();
		return settings;
	}
	
	public void sortSettingsAlphabetically(){
		Collections.sort(settings, new Comparator<InternalR101Setting>() {

			@Override
			public int compare(InternalR101Setting o1, InternalR101Setting o2) {
				return o1.getDisplayName().compareTo(o2.getDisplayName());
			}
		});
	}

	public void setSettings(ArrayList<InternalR101Setting> settings) {
		this.settings = settings;
	}
	
	public ArrayList<InternalR101Setting> getActiveSettings()
	{
		ArrayList<InternalR101Setting> activeSettings = new ArrayList<InternalR101Setting>();
		
		for (InternalR101Setting curr : getSettings())
		{
			if (curr.isSettingEnabled())
			{
				activeSettings.add(curr);
			}
		}
		
		return activeSettings;
	}

}
