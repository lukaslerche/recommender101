package org.recommender101.guiconfig;

import java.awt.Dimension;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JInternalFrame;

@SuppressWarnings("serial")
public abstract class FrmAbstractParentFrame extends JInternalFrame{
	
	private double percentWidth = 0;
	private double percentHeight = 0;
	
	private double percentOffsetX = 0;
	private double percentOffsetY = 0;
	
	// Needed to restore the mouse listeners when the windows is made movable
	private ArrayList<MouseListener> originalListeners;
	
    public FrmAbstractParentFrame(String name, int percentWidth, int percentHeight, Dimension parentSize,
    		int percentOffsetX, int percentOffsetY) {
        super(name, 
              false, //resizable
              false, //closable
              true, //maximizable
              true);//iconifiable
 
        this.originalListeners = new ArrayList<>();
        
        this.percentWidth = (double)percentWidth/100;
        this.percentHeight = (double)percentHeight/100;
        
        this.percentOffsetX = (double)percentOffsetX/100;
        this.percentOffsetY = (double)percentOffsetY/100;
        
            
        resizeWindow(parentSize);

        // No icon
        setFrameIcon(null);
        
        // Remember listeners     
        for(MouseListener listener : ((javax.swing.plaf.basic.BasicInternalFrameUI) getUI()).getNorthPane().getMouseListeners())
        {
        	originalListeners.add(listener);
        	((javax.swing.plaf.basic.BasicInternalFrameUI) getUI()).getNorthPane().removeMouseListener(listener);
 	       
        }
        
    }
    
       
    @Override
	public void setResizable(boolean arg0) {
		// Call original method, then do stuff to make window movable (or not)
		super.setResizable(arg0);
		
		if (arg0)
		{
			// Window should be movable
			for (MouseListener m : this.originalListeners)
			{
				((javax.swing.plaf.basic.BasicInternalFrameUI) getUI()).getNorthPane().addMouseListener(m);
			}
		}
		else
		{
			// Remove move listeners
			for(MouseListener listener : ((javax.swing.plaf.basic.BasicInternalFrameUI) getUI()).getNorthPane().getMouseListeners())
	        {
	        	((javax.swing.plaf.basic.BasicInternalFrameUI) getUI()).getNorthPane().removeMouseListener(listener);
	        }
		}
	}


    public void resizeWindow(Dimension newParentSize)
    {
    	resizeWindow(newParentSize, false);
    }

	public void resizeWindow(Dimension newParentSize, boolean forced)
    {
		// Do this only when manual resizing and repositioning is deactivated or forced
		if (!CommonRuntimeData.isWindowsResizableMovable() || forced)
		{
	    	setSize((int)((double)newParentSize.width*this.percentWidth), 
	    			(int)((double)newParentSize.height*this.percentHeight));
	    	
	    	// Reposition the window
	    	setLocation((int)((double)newParentSize.width*this.percentOffsetX), 
	    			(int)((double)newParentSize.height*this.percentOffsetY));
		}
    }
    
    /**
     * A function that has to be implemented by each subclass with logic to handle a new properties file (refresh GUI etc.)
     * @param newPropFile Pointer to the newly loaded properties file
     */
    public abstract void newFileLoaded(InternalR101PropertiesFile newPropFile);
    
}
