package org.recommender101.guiconfig;

import java.awt.Image;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTextField;


/**
 * This class is being used to store common runtime data and methods to be accessed by all parts of the GUI
 *
 */
public class CommonRuntimeData {

	// Path to a file that is loaded when a "new file" is created (containing standard options)
	private static final File DEFAULT_FILE_PATH = new File("conf/recommender101.properties");
	
	// Path to the file which contains information of the last opened files
	private static final String PATH_TO_LASTOPENEDFILES = "lastOpenedFiles.txt";
	
	// Last opened files
	public static ArrayList<String> lastOpenedFiles = new ArrayList<String>();
	
	// Current file
	private static InternalR101PropertiesFile propFile;
	
	// Windows
	public static FrmClassTree frmClassTree;
	public static FrmDescription frmDescription;
	public static FrmCurrentClasses frmCurrentClasses;
	public static FrmPreview frmPreview;
	public static FrmSettings frmSettings;
	
	// Special GUI components
	public static JTextField searchBox;
	
	// Settings
	private static boolean windowsResizableMovable = false;
	
	// Locks
	public static boolean experimentRunning = false;

	/**
	 * stdout. needed in order to be able to restore it later
	 */
	public static PrintStream stdout = System.out;
	
	/**
	 * stderr. needed in order to be able to restore it later
	 */
	public static PrintStream stderr = System.err;
	
	public static boolean isWindowsResizableMovable() {
		return windowsResizableMovable;
	}

	public static void setWindowsResizableMovable(boolean windowsResizableMovable) {
		CommonRuntimeData.windowsResizableMovable = windowsResizableMovable;
		
		frmClassTree.setResizable(windowsResizableMovable);
		frmDescription.setResizable(windowsResizableMovable);
		frmCurrentClasses.setResizable(windowsResizableMovable);
		frmPreview.setResizable(windowsResizableMovable);
		frmSettings.setResizable(windowsResizableMovable);
	}

	/**
	 * Shows a description in the "description" window in the lower left corner
	 * @param description The String that contains the description to be shown
	 */
	public static void showDescription(String description)
	{				
		frmDescription.showText(description);
	}
	
	
	/**
	 * Loads the list of the previously opened files into the ArrayList lastOpenedFiles (attribute of this class)
	 */
	public static void loadLastOpenedFiles()
	{
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(PATH_TO_LASTOPENEDFILES));
			String line;
			while ((line = br.readLine()) != null) {
			   lastOpenedFiles.add(line);
			}
			br.close();
			
		} catch (FileNotFoundException e) {
			// File not found / first run - ignore
		} catch (IOException e) {
			System.err.println("Couldn't read from LastOpenedFiles path: "+PATH_TO_LASTOPENEDFILES);
		}	
	}
	
	/**
	 * Adds a path to the list of last opened files. Also saves the new list to the hard disk.
	 * @param path The path of the recently opened file
	 */
	public static void addToLastOpenedFiles(String path)
	{
		// Check if the file already is in the list
		if (lastOpenedFiles.contains(path))
		{
			lastOpenedFiles.remove(lastOpenedFiles.indexOf(path));
		}
		
		if (lastOpenedFiles.size() == 5)
		{
			// Remove the last item / the file which hasn't been opened for the longest time
			lastOpenedFiles.remove(4);
		}
		
		lastOpenedFiles.add(0, path);
		
		// Write to file		
		try {
	        FileWriter fstream = new FileWriter(PATH_TO_LASTOPENEDFILES, false);
	        BufferedWriter out = new BufferedWriter(fstream);
	        for (String text : lastOpenedFiles) {
		        out.write(text);
		        out.newLine();
		    }       
	        //Close the output stream
	        out.close();
		} catch (IOException e) {
		    System.err.println("Couldn't write to LastOpenedFiles path: "+PATH_TO_LASTOPENEDFILES);

		}
        
        
		
	}
	
	
	/**
	 * Creates a new, empty properties file
	 * @return The newly created properties file
	 */
	public static InternalR101PropertiesFile createNewPropFile()
	{
		// Load defaut properties file or create a new one
        if (DEFAULT_FILE_PATH.exists())
        {
        	InternalR101PropertiesFile f = PropertiesFileManager.getPropertiesFile(DEFAULT_FILE_PATH.getAbsolutePath());				
        	propFile = f;	
        }
        else
        {
    		// The file which has been set as default doesn't exist, so an empty one is created
        	propFile = new InternalR101PropertiesFile();
        	System.out.println("Default configuration file not found - creating new one.");
        }
		notifyWindows(null);
		return propFile;
	}
	
	/**
	 * Returns the currently loaded properties file
	 */
	public static InternalR101PropertiesFile getPropFile()
	{		
		return propFile;
	}
	
	/**
	 * Sets the currently loaded properties file
	 */
	public static void setPropFile(InternalR101PropertiesFile newFile)
	{		
		propFile = newFile;
		notifyWindows(null);
	}
	
	/**
	 * Notifies all windows that a new propFile has been loaded
	 */
	public static void notifyWindows(FrmAbstractParentFrame caller)
	{
		FrmAbstractParentFrame[] windows
		= new FrmAbstractParentFrame[]{frmClassTree, frmDescription, frmCurrentClasses, frmPreview, frmSettings};
	
		for (FrmAbstractParentFrame currWindow:windows)
		{
			// Don't notify the caller since his view doesn't need refreshing
			//if (!currWindow.equals(caller))
			//{
				currWindow.newFileLoaded(propFile);
			//}	
		}
	}
	
	/**
	 * Creates an image-showing JButton for the toolbars.
	 * @param imageName The filename of the image. Has to be inside the /images subfolder
	 * @param actionCommand An action command that will be associated with the button
	 * @param toolTipText Tooltip text
	 * @param altText Alternative text
	 * @param listener An ActionListener to be added to the button
	 * @return
	 */
	public static JButton makeButton(String imageName, String actionCommand, String toolTipText, String altText, ActionListener listener) {
		final int iconSize = 20;
		
		//Create and initialize the button.
		JButton button = new JButton();
		button.setActionCommand(actionCommand);
		button.setToolTipText(toolTipText);
		if (listener != null)
		{
			button.addActionListener(listener);
		}
		
		
		ImageIcon icon = new ImageIcon("images/"+imageName);
		Image image = icon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);		
		button.setIcon(new ImageIcon(image));
		
		
		return button;
	}
}
