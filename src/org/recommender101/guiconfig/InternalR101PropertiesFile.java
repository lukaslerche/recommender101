package org.recommender101.guiconfig;

import java.io.File;
import java.util.ArrayList;

import org.recommender101.gui.annotations.R101Setting.SettingsType;

/**
 * Represents a full R101 Properties file for internal use
 * 
 * @author timkraemer
 * 
 */
public class InternalR101PropertiesFile {

	private File file = new File("< new File >");

	private ArrayList<InternalR101Class> recommenders;
	private ArrayList<InternalR101Class> metrics;
	
	private InternalR101Class dataloader;
	private InternalR101Class datasplitter;
	
	public final String NAME_OF_DATALOADER_SETTING =  "DataLoaderClass";
	public final String NAME_OF_DATASPLITTER_SETTING =  "DataSplitterClass";
	
	// Special comments attributes for recommenders, metrics, datasplitter, dataloader
	private ArrayList<String> recommendersComments = new ArrayList<String>();
	private ArrayList<String> metricsComments = new ArrayList<String>();
	private ArrayList<String> dataloaderComments = new ArrayList<String>();
	private ArrayList<String> datasplitterComments = new ArrayList<String>();

	private InternalR101Class otherSettings = null;

	// TO DO: Missing the other properties

	public InternalR101PropertiesFile() {
		this.recommenders = new ArrayList<InternalR101Class>();
		this.metrics = new ArrayList<InternalR101Class>();

				
		ArrayList<InternalR101Setting> normalSettings = new ArrayList<InternalR101Setting>();
		for (InternalR101Setting s : new OtherSettingsConfiguration().normalSettings) {
			normalSettings.add(s);
		}
		otherSettings = new InternalR101Class(null, "dummy_otherSettings", "", normalSettings);
	}
	
	/**
	 * Returns a specific "other setting" that matches the given key or null otherwise 
	 * @param settingName The name of the setting to be returned
	 */
	public InternalR101Setting getSpecificOtherSetting(String settingName)
	{
		// Iterate over all settings, if the setting is found change its value, otherwise add it at the end
		for (InternalR101Setting s:otherSettings.getSettings())
		{
			if (s.getSettingName().equals(settingName))
			{
				return s;
			}
		}
		return null;
	}
	
	public void addOtherSetting(String key, String value, ArrayList<String> comments)
	{
		// Special handling to dataloader and datasplitter setting
		if (key.equals(NAME_OF_DATALOADER_SETTING))
		{
			this.dataloader = new InternalR101Class(value);
			this.dataloaderComments = comments;
			return;
		}
		if (key.equals(NAME_OF_DATASPLITTER_SETTING))
		{
			this.datasplitter = new InternalR101Class(value);
			this.datasplitterComments = comments;
			return;
		}
		
		// Iterate over all settings, if the setting is found change its value, otherwise add it at the end
		for (InternalR101Setting s:otherSettings.getSettings())
		{
			if (s.getSettingName().equals(key))
			{
				s.setCurrentValue(value);
				s.setComments(comments);
				return;
			}
		}
		
		// Setting not found
		// Find the index of the last non-dummy setting and add it there
		int i=0;
		for (InternalR101Setting s:otherSettings.getSettings())
		{
			if (s.getSettingName().equals(""))
			{
				break;
			}
			else
			{
				i++;
			}
		}
		
		otherSettings.getSettings().add(i,new InternalR101Setting(key,key,"",SettingsType.TEXT,0,0,new String[]{},value, comments));
	}

	public InternalR101Class getOtherSettings() {
		return otherSettings;
	}

	/**
	 * Used for building the properties file
	 */
	@Override
	public String toString() {
		//String ret = "# Recommender101 Properties File #\r\n\r\n";
		String ret="";
		// Recommenders
		for (String s:this.recommendersComments)
		{
			ret += s+"\r\n";
		}
		ret += "AlgorithmClasses=\\\r\n";
		for (InternalR101Class c : recommenders) {
			ret += "     " + c.toString() + ",\\\r\n";
		}
		// Remove last comma
		ret = ret.substring(0, ret.length() - 4);

		ret += "\r\n\r\n\r\n";

		// Metrics
		for (String s:this.metricsComments)
		{
			ret += s +"\r\n";
		}
		ret += "Metrics=\\\r\n";
		for (InternalR101Class c : metrics) {
			ret += "     " + c.toString() + ",\\\r\n";
		}
		// Remove last comma
		ret = ret.substring(0, ret.length() - 4);

		ret += "\r\n\r\n";

		// Dataloader
		for (String s:this.dataloaderComments)
		{
			ret += s +"\r\n";
		}
		if (this.dataloader == null)
		{
			ret += NAME_OF_DATALOADER_SETTING;
		}
		else
		{
			ret += NAME_OF_DATALOADER_SETTING+"="+this.dataloader;
		}
		
		
		ret += "\r\n\r\n";
		
		// Datasplitter
		for (String s:this.datasplitterComments)
		{
			ret += s +"\r\n";
		}
		if (this.datasplitter == null)
		{
			ret += NAME_OF_DATASPLITTER_SETTING;
		}
		else
		{
			ret += NAME_OF_DATASPLITTER_SETTING+"="+this.datasplitter;
		}
		
		
		ret += "\r\n\r\n";

		// Other settings
		for (InternalR101Setting s : otherSettings.getSettings()) {
			if ((!s.getSettingName().equals("")) && s.isSettingEnabled()) {
				//ret += s.getSettingName() + "=" + s.getCurrentValue() + "\r\n";
				ret += s.toStringWithComments();
			}
		}
		

		return ret;
	}

	public ArrayList<InternalR101Class> getRecommenders() {
		return recommenders;
	}

	public void setRecommenders(ArrayList<InternalR101Class> recommenders) {
		this.recommenders = recommenders;
	}

	public ArrayList<InternalR101Class> getMetrics() {
		return metrics;
	}

	public void setMetrics(ArrayList<InternalR101Class> metrics) {
		this.metrics = metrics;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File path) {
		this.file = path;
	}

	public void setRecommendersComments(ArrayList<String> recommendersComments) {
		this.recommendersComments = recommendersComments;
	}

	public void setMetricsComments(ArrayList<String> metricsComments) {
		this.metricsComments = metricsComments;
	}
	
	public InternalR101Class getDataloader() {
		return dataloader;
	}

	public void setDataloader(InternalR101Class dataloader) {
		this.dataloader = dataloader;
	}

	public InternalR101Class getDatasplitter() {
		return datasplitter;
	}

	public void setDatasplitter(InternalR101Class datasplitter) {
		this.datasplitter = datasplitter;
	}
	

}
