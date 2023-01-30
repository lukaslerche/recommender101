package org.recommender101.guiconfig;

import java.util.ArrayList;

import org.recommender101.gui.annotations.R101Setting;
import org.recommender101.gui.annotations.R101Setting.SettingsType;

public class InternalR101Setting {

	private String settingName;
	private String displayName;
	private String description;

	private SettingsType type;

	private double minValue;
	private double maxValue;

	private String[] values;

	private String defaultValue;

	private Object currentValue;
	
	private ArrayList<String> comments;
	
	private boolean settingEnabled = false;
	
	private String originalMethodName = "";
	

	/**
	 * A boolean value representing whether or not this instance has been
	 * created with an annotation or not This will later be used to determine if
	 * the name of the setting is editable
	 */
	private boolean customSetting = false;

	/**
	 * Needed for getCopy() method
	 */
	R101Setting annotation = null;

	/**
	 * Creates a new InternalR101Class object based on a settings string which
	 * is being parsed.
	 * 
	 * @param parseString
	 *            String to parse, for example "numFeatures=50"
	 */
	public InternalR101Setting(String parseString, ArrayList<String> comments) {
		// TO DO match setting to annotated setting

		String[] splitString = parseString.split("=");

		setSettingName(splitString[0]);
		setDisplayName(splitString[0]);
		setDescription("");
		setType(SettingsType.TEXT);
		setMinValue(0);
		setMaxValue(Integer.MAX_VALUE);
		setValues(new String[] {});
		setDefaultValue("");
		if (splitString.length>1)
		{
			setCurrentValue(splitString[1]);
		}
		else
		{
			setCurrentValue("");
		}

		this.comments = comments;
		
		customSetting = true;
	}

	public InternalR101Setting() {
		setSettingName("");
		setDisplayName("");
		setDescription("");
		setType(SettingsType.TEXT);
		setMinValue(0);
		setMaxValue(Integer.MAX_VALUE);
		setValues(new String[] {});
		setDefaultValue("");
		setCurrentValue("");

		this.comments = new ArrayList<String>();
		
		customSetting = true;
		
		// Empty settings are disabled at first
		setSettingEnabled(false);
	}

	/**
	 * This constructor is being used to create the "other settings" dialog at
	 * runtime
	 * 
	 * @param settingName
	 *            Name of the setting to be used in the properties file
	 * @param displayName
	 *            Human-readable display name of the setting to show in the GUI
	 * @param description
	 *            Description of the setting to show in the GUI
	 * @param type
	 *            Type of the setting, of Type R101Setting.SettingsType
	 * @param minValue
	 *            If the setting is of a numeric type, this can be used to set a
	 *            minimum value
	 * @param maxValue
	 *            If the setting is of a numeric type, this can be used to set a
	 *            maximum value
	 * @param values
	 *            If the setting is of type ARRAY, this can be used to set a
	 *            String array containing the possible values
	 * @param defaultValue
	 *            The default value. It's always being stored as a string no
	 *            matter which SettingsType has been set
	 */
	public InternalR101Setting(String settingName, String displayName, String description, SettingsType type,
			int minValue, int maxValue, String[] values, String defaultValue, ArrayList<String> comments) {
		setSettingName(settingName);
		setDisplayName(displayName);
		setDescription(description);
		setType(type);
		setMinValue(minValue);
		setMaxValue(maxValue);
		setValues(values);
		setDefaultValue(defaultValue);
		setCurrentValue(defaultValue);
		
		this.comments = comments;

		customSetting = false;
	}

	public InternalR101Setting(String methodName, R101Setting annotation) {
		this.annotation = annotation;
		this.originalMethodName = methodName;
		
		// The method name starts with "Set" and then an uppercase letter of the setting
		// Remove the "set" and make the first letter lowercase
		if (methodName.startsWith("set"))
		{
			String firstChar = methodName.substring(3, 4);
			methodName = methodName.substring(4);
			
			methodName = firstChar.toLowerCase() + methodName;
			
		}		
		
		setSettingName(methodName);
		//setSettingName(annotation.name());
		setDisplayName(annotation.displayName());
		setDescription(annotation.description());
		setType(annotation.type());
		setMinValue(annotation.minValue());
		setMaxValue(annotation.maxValue());
		setValues(annotation.values());
		setDefaultValue(annotation.defaultValue());

		if (defaultValue != null && !defaultValue.equals("")) {
			currentValue = defaultValue;
		} else {
			switch (type) {
			case BOOLEAN:
				currentValue = "false";
				break;
			case DOUBLE:
			case INTEGER:
				currentValue = minValue;
				break;
			case ARRAY:
				if (values[0] != null) {
					currentValue = values[0];
				}
			default:
				currentValue = defaultValue;
				break;
			}
		}
		
		// Is this setting optional? If so, disable it at first.
		if (annotation.optional())
		{
			this.setSettingEnabled(false);
		}
		
		this.comments = new ArrayList<String>();
	}

	/**
	 * Used for assembly of the properties file
	 */
	@Override
	public String toString() {
		return settingName + "=" + currentValue;
	}
	
	public String toStringWithComments()
	{
		String ret = "";
		if (this.comments == null)
		{
			this.comments = new ArrayList<String>();
		}
		for (String s:this.comments)
		{
			ret += s+"\r\n";
		}
		ret+= settingName + "=" + currentValue+"\r\n";
		return ret;
	}

	/**
	 * This method is necessary because settings object are likely to be reused
	 * frequently.
	 * 
	 * @return An identical settings object
	 */
	public InternalR101Setting getCopy() {
		if (annotation == null) {
			return new InternalR101Setting();
		}
		return new InternalR101Setting(originalMethodName, annotation);
	}

	public String getSettingName() {
		return settingName;
	}

	public void setSettingName(String settingName) {
		this.settingName = settingName;
	}

	public String getDisplayName() {
		if (displayName.equals("unknown")) {
			return this.settingName;
		}
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

	public SettingsType getType() {
		return type;
	}

	public void setType(SettingsType type) {
		this.type = type;
	}

	public double getMinValue() {
		return minValue;
	}

	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

	public String[] getValues() {
		return values;
	}

	public void setValues(String[] values) {
		this.values = values;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public Object getCurrentValue() {
		return currentValue;
	}

	public void setCurrentValue(Object currentValue) {
		this.currentValue = currentValue;
	}

	public boolean isCustomSetting() {
		return customSetting;
	}
	
	
	public boolean isSettingEnabled() {
		return settingEnabled;
	}

	public void setSettingEnabled(boolean settingEnabled) {
		this.settingEnabled = settingEnabled;
	}
	
	
	public void setComments(ArrayList<String> comments) {
		this.comments = comments;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof InternalR101Setting) {
			InternalR101Setting setting = (InternalR101Setting) obj;
			return (setting.getSettingName().equals(this.getSettingName()));
		}

		return super.equals(obj);
	}


}
