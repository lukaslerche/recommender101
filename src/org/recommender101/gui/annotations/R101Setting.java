package org.recommender101.gui.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface R101Setting {
	
	/**
	 * The internal name of the setting, written in the exact same way as in the properties file.
	 * The GUI will use this name to build the properties file
	 */
	//public String name() default "REMOVE" ; // no longer being used, detected automatically
	
	
	/**
	 * A user-readable display name for this setting which will be shown in the GUI.
	 */
	public String displayName() default "unknown";

	
	/**
	 * A description for this setting that will be shown in the GUI.
	 */
	public String description() default "";


	public enum SettingsType { BOOLEAN, INTEGER, TEXT, ARRAY, DOUBLE, FILE }
	/**
	 * Sets the type of this setting. It has to be one of R101Setting.SettingsType. Depending on the choice,
	 * other options may be set (for example min/max values) and the style in the GUI will be different
	 * (for example, SettingsType.FILE opens a FileChooser window on click).
	 */
	SettingsType type() default SettingsType.TEXT;
	
	/** 
	 * if the type is integer or double, this allows setting the min value
	 */
	public double minValue() default 0;
	
	/** 
	 * if the type is integer or double, this allows setting the max value
	 */
	public double maxValue() default Double.MAX_VALUE;
	
	/**
	 *  if the type is array, this String array should be used to set the possible values (optional)
	 */
	public String[] values() default {};
	
	/** 
	 * Used to set a default value for this setting (optional)
	 * @return
	 */
	public String defaultValue() default "";
	
	/**
	 *  Set the optional to true if the setting is optional
	 */
	public boolean optional() default false;
}
