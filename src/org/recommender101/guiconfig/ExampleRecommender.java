package org.recommender101.guiconfig;

import java.util.List;

import org.recommender101.gui.annotations.R101HideFromGui;
import org.recommender101.gui.annotations.R101Setting;
import org.recommender101.gui.annotations.R101Setting.SettingsType;
import org.recommender101.recommender.AbstractRecommender;

// Note: The following line would be necessary in order to declare the class as a R101Class
//@R101Class
// However, since this class should be hidden in the GUI, the following line has been inserted instead
@R101HideFromGui
@SuppressWarnings("serial")
public class ExampleRecommender extends AbstractRecommender {

	@Override
	public float predictRating(int user, int item) {
		return 0;
	}

	@Override
	public List<Integer> recommendItems(int user) {
		return null;
	}

	@Override
	public void init() throws Exception {	
	}

	@R101Setting (displayName="Boolean value", description="Boolean value with default value \"true\"", defaultValue="true",
			type=SettingsType.BOOLEAN)
	public void a(){}
	
	@R101Setting (displayName="Boolean value", description="Boolean value with default value \"false\"", defaultValue="false",
			type=SettingsType.BOOLEAN)
	public void b(){}
	
	@R101Setting (displayName="Integer value", description="Integer value between 0 and 1337", defaultValue="0",
			type=SettingsType.INTEGER, minValue=0, maxValue=1337)
	public void c(){}
	
	@R101Setting (displayName="Double value", description="Double value between -42 and 42", defaultValue="0",
			type=SettingsType.INTEGER, minValue=-42, maxValue=42)
	public void d(){}
	
	@R101Setting (displayName="Array value", description="Choose one of the values",
			type=SettingsType.ARRAY, values={"Wow","Toll!","Note 1,0"})
	public void e(){}
	
	@R101Setting (displayName="Text value", description="Normal text field",
			type=SettingsType.TEXT, defaultValue="Default text")
	public void f(){}
	
	@R101Setting (displayName="File", description="Click to choose a file",
			type=SettingsType.FILE)
	public void g(){}
}
