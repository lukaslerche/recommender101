package org.recommender101;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;

import org.recommender101.guiconfig.AboutDialog;
import org.recommender101.guiconfig.CommonRuntimeData;
import org.recommender101.guiconfig.FrmAbstractParentFrame;
import org.recommender101.guiconfig.FrmClassTree;
import org.recommender101.guiconfig.FrmConsole;
import org.recommender101.guiconfig.FrmCurrentClasses;
import org.recommender101.guiconfig.FrmDescription;
import org.recommender101.guiconfig.FrmPreview;
import org.recommender101.guiconfig.FrmSettings;
import org.recommender101.guiconfig.InternalR101PropertiesFile;
import org.recommender101.guiconfig.PropertiesFileManager;
 
/*
 * InternalFrameDemo.java requires:
 *   MyInternalFrame.java
 */
@SuppressWarnings("serial")
public class GUI extends JFrame
                               implements ActionListener {
	
	
    JDesktopPane desktop;
    ArrayList<FrmAbstractParentFrame> frames = null;
 
    /**
	 * JFileChooser object. It's an attribute of the class in order to be able
	 * to remember the last directory
	 */
	private JFileChooser chooser;
	
	
	private JButton toggleMovementButton; 
    private JCheckBoxMenuItem toggleMovementMenuItem;
    
    private JMenuItem newDocumentMenuItem;
    private JMenuItem openDocumentMenuItem;
    private JMenuItem saveDocumentMenuItem;
    private JMenuItem runConfigurationMenuItem;
    private JMenuItem restoreWindowsMenuItem;
    
    private JMenu openSubMenu;
    
    private final int iconSize = 20;
	
	public GUI() {
        super("Recommender 101");
 
        setMinimumSize(new Dimension(1024, 768));
        
        //Make the big window be indented 50 pixels from each edge
        //of the screen.
        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        
        if (screenSize.width-inset*2 > 1024 && screenSize.height-inset*2 > 768)
        {
        	setBounds(inset, inset,
                    screenSize.width  - inset*2,
                    screenSize.height - inset*2);
        }
        
        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
               
        
        CommonRuntimeData.loadLastOpenedFiles();
        
        //Set up the GUI.
        desktop = new JDesktopPane();  
        //setContentPane(desktop);
        
        
    
        
        Container pane = getContentPane();
        pane.add(createToolbar(), BorderLayout.PAGE_START);              
        pane.add(desktop, BorderLayout.CENTER);
        
               
        setJMenuBar(createMenuBar());
        
        
        
        desktop.setDragMode(JDesktopPane.LIVE_DRAG_MODE);
        
        //-------------------------------------------
        // Add frames
        //-------------------------------------------
        this.frames = new ArrayList<FrmAbstractParentFrame>();
        
        // Class Library (Available Recommenders / Metrics)
        FrmAbstractParentFrame currFrame = new FrmClassTree("Library", 20, 85, desktop.getSize(), 0, 0);
        currFrame.setVisible(true);
        desktop.add(currFrame);
        frames.add(currFrame);
        CommonRuntimeData.frmClassTree = (FrmClassTree) currFrame;
        
        // Set up listener for search Box
        // This has to be done here in order to make the shortcut available in all sub-windows
        getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK),
                "pressed");
        getRootPane().getActionMap().put( "pressed", new AbstractAction() {			
			@Override
			public void actionPerformed(ActionEvent e) {			
				CommonRuntimeData.searchBox.requestFocusInWindow();
			}
		} );
        
        // Description window
        currFrame = new FrmDescription("Description", 20, 15, desktop.getSize(), 0, 85);
        currFrame.setVisible(true);
        desktop.add(currFrame);
        frames.add(currFrame);
        CommonRuntimeData.frmDescription = (FrmDescription) currFrame;
        
        
        // Current Classes List
        currFrame = new FrmCurrentClasses("Currently selected classes", 45, 50, desktop.getSize(), 20, 0);
        currFrame.setVisible(true);
        desktop.add(currFrame);
        frames.add(currFrame);
        CommonRuntimeData.frmCurrentClasses = (FrmCurrentClasses) currFrame;
        
        // Preview
        currFrame = new FrmPreview("Editable Preview", 35, 100, desktop.getSize(), 65, 0);
        currFrame.setVisible(true);
        desktop.add(currFrame);
        frames.add(currFrame);
        CommonRuntimeData.frmPreview = (FrmPreview) currFrame;
        
        // Settings Window
        currFrame = new FrmSettings("Settings", 45, 50, desktop.getSize(), 20, 50);
        currFrame.setVisible(true);
        desktop.add(currFrame);
        frames.add(currFrame);
        CommonRuntimeData.frmSettings = (FrmSettings) currFrame;
        
       
    
        desktop.addComponentListener(new ComponentListener() {		
			@Override
			public void componentResized(ComponentEvent arg0) {
				// Notify child frames so that they can resize
				for (FrmAbstractParentFrame frame:frames)
				{
					frame.resizeWindow(desktop.getSize());
				}
			}
			
			@Override
			public void componentMoved(ComponentEvent arg0) {
				// Do nothing				
			}
			
			@Override
			public void componentHidden(ComponentEvent arg0) {
				// Do nothing			
			}

			@Override
			public void componentShown(ComponentEvent e) {
				// Do nothing			
			}
		});
        
        CommonRuntimeData.createNewPropFile();
        

    }
 
    private JToolBar createToolbar() {
    	JToolBar toolbar = new JToolBar();
        toolbar.setPreferredSize(new Dimension(200,30));
        toolbar.setFloatable(false);

        // new document button
        ImageIcon icon = new ImageIcon("images/document-new.png");
        Image img = icon.getImage();
        img = img.getScaledInstance( 25, 25,  java.awt.Image.SCALE_SMOOTH );
        JButton btn = new JButton(new ImageIcon(img));
        btn.setToolTipText("New document");
        toolbar.add(btn);
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	for (ActionListener l : newDocumentMenuItem.getActionListeners())
                {
                	l.actionPerformed(event);
                } 
            }
        });
        
        // open document button
        icon = new ImageIcon("images/document-open.png");
        img = icon.getImage();
        img = img.getScaledInstance( 25, 25,  java.awt.Image.SCALE_SMOOTH );
        btn = new JButton(new ImageIcon(img));
        btn.setToolTipText("Open document");
        toolbar.add(btn);
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            for (ActionListener l : openDocumentMenuItem.getActionListeners())
            {
            	l.actionPerformed(event);
            }                                 }
        });
        
    	// open document button
        icon = new ImageIcon("images/media-floppy.png");
        img = icon.getImage();
        img = img.getScaledInstance( 25, 25,  java.awt.Image.SCALE_SMOOTH );
        btn = new JButton(new ImageIcon(img));
        btn.setToolTipText("Save document");
        toolbar.add(btn);
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	for (ActionListener l : saveDocumentMenuItem.getActionListeners())
                {
                	l.actionPerformed(event);
                } 
            }
        });
        
        toolbar.addSeparator();
        
        // Run configuration button
        icon = new ImageIcon("images/media-playback-start.png");
        img = icon.getImage();
        img = img.getScaledInstance( 25, 25,  java.awt.Image.SCALE_SMOOTH );
        btn = new JButton(new ImageIcon(img));
        btn.setToolTipText("Run configuration");
        toolbar.add(btn);
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	for (ActionListener l : runConfigurationMenuItem.getActionListeners())
                {
                	l.actionPerformed(event);
                } 
            }
        });       	
        
        toolbar.addSeparator();
        
        // Move windows button
        icon = new ImageIcon("images/windows-move-inactive.png");
        img = icon.getImage();
        img = img.getScaledInstance( 25, 25,  java.awt.Image.SCALE_SMOOTH );
        btn = new JButton(new ImageIcon(img));
        btn.setToolTipText("Toggle movement and resizing of windows");
        this.toggleMovementButton = btn;
        toolbar.add(btn);
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                CommonRuntimeData.setWindowsResizableMovable(!CommonRuntimeData.isWindowsResizableMovable());
                adjustToggleMovementButtonImage();
                toggleMovementMenuItem.setSelected(CommonRuntimeData.isWindowsResizableMovable());
            }
        });  
        
        // Restore windows
        icon = new ImageIcon("images/restore-windows.png");
        img = icon.getImage();
        img = img.getScaledInstance( 25, 25,  java.awt.Image.SCALE_SMOOTH );
        btn = new JButton(new ImageIcon(img));
        btn.setToolTipText("Restore window positions and sizes");
        toolbar.add(btn);
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	for (ActionListener l : restoreWindowsMenuItem.getActionListeners())
                {
                	l.actionPerformed(event);
                } 
            }
        }); 
        
        toolbar.addSeparator();
                
        // Restore windows
        icon = new ImageIcon("images/exit.png");
        img = icon.getImage();
        img = img.getScaledInstance( 25, 25,  java.awt.Image.SCALE_SMOOTH );
        btn = new JButton(new ImageIcon(img));
        btn.setToolTipText("Exit Recommender101 GUI");
        toolbar.add(btn);
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	System.exit(0);
            }
        }); 
        
        
        return toolbar;
	}

	protected JMenuBar createMenuBar() {
		ImageIcon icon = null;
		Image image = null;

		// Create the menu bar.
		JMenuBar menuBar = new JMenuBar();

		JMenu menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menu);

		icon = new ImageIcon("images/document-new.png");
		image = icon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
		JMenuItem menuItem = new JMenuItem("New", new ImageIcon(image));
		newDocumentMenuItem = menuItem;
		menuItem.setMnemonic(KeyEvent.VK_N);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		menu.add(menuItem);
		
		// Action Listener for "new document" button
		menuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				CommonRuntimeData.createNewPropFile();
			}
		});

		openSubMenu = new JMenu("Open");
		menu.add(openSubMenu);
		
		refreshOpenSubMenu();
		
		

		
		icon = new ImageIcon("images/media-floppy.png");
		image = icon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
		menuItem = new JMenuItem("Save", new ImageIcon(image));
		saveDocumentMenuItem = menuItem;
		menuItem.setMnemonic(KeyEvent.VK_S);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		menu.add(menuItem);
		
		// Remember for further use
		JMenuItem saveMenuItem = menuItem;
		
		
		icon = new ImageIcon("images/media-floppy.png");
		image = icon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
		menuItem = new JMenuItem("Save As", new ImageIcon(image));
		menuItem.setMnemonic(KeyEvent.VK_S);
		menuItem.setAccelerator(KeyStroke.getKeyStroke("control shift S"));
		menu.add(menuItem);
		
		// Remember for further use
		final JMenuItem saveAsMenuItem = menuItem;
		
		// Action Listener for "save as"-button
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (chooser == null) {
					chooser = new JFileChooser();
					
					// Set filter for ".properties" files only
					FileFilter filter = new FileFilter() {

						@Override
						public String getDescription() {
							return "Recommender101 Properties File";
						}

						@Override
						public boolean accept(File f) {
							if (f.isDirectory() || f.getName().endsWith(".properties")) {
								return true;
							}

							return false;
						}
					};
					
					chooser.setFileFilter(filter);
					// Set the file chooser dialog to start in the project
					// directory
					String path = getClass().getClassLoader().getResource(".").getPath();
					chooser.setCurrentDirectory(new File(path));
				}

				chooser.setDialogTitle("Save");
				int returnVal = chooser.showOpenDialog(GUI.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					String savePath = chooser.getSelectedFile().getAbsolutePath();
					
					if (!savePath.endsWith(".properties")){
						savePath += ".properties";
					}
					CommonRuntimeData.getPropFile().setFile(new File(savePath));
					PropertiesFileManager.savePropertiesFile(CommonRuntimeData.getPropFile(), savePath);	
				}
			}
		});
		
		
		// Action listener for "save"
		saveMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (CommonRuntimeData.getPropFile().getFile().getPath().equals("< new File >"))
				{
					// New file is to be saved, ask for path
					saveAsMenuItem.getActionListeners()[0].actionPerformed(null);					
				}
				else
				{
					// File has previously been saved, use the same location
					String savePath = CommonRuntimeData.getPropFile().getFile().getAbsolutePath();					
					PropertiesFileManager.savePropertiesFile(CommonRuntimeData.getPropFile(), savePath);
				}
			}
		});
		
		
		menu.addSeparator();

		menuItem = new JMenuItem("Exit", KeyEvent.VK_E);
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// Close GUI
				System.exit(0);				
			}
		});

		menu = new JMenu("Recommender101");
		menu.setMnemonic(KeyEvent.VK_R);
		menuBar.add(menu);

		icon = new ImageIcon("images/media-playback-start.png");
		image = icon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
		menuItem = new JMenuItem("Run configuration", new ImageIcon(image));
		runConfigurationMenuItem = menuItem;
		menuItem.setMnemonic(KeyEvent.VK_R);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
		menu.add(menuItem);
		
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
							
				// Make sure only one experiment is running at a time
				if (CommonRuntimeData.experimentRunning)
				{
					JOptionPane.showMessageDialog(GUI.this,"Please wait until the current run of Recommender101 has finished before starting a new one.", "Experiment running",  JOptionPane.ERROR_MESSAGE);
					return;
				}
							
				FrmConsole currFrame = new FrmConsole(desktop.getSize());
		        currFrame.setVisible(true);
	        
		        desktop.add(currFrame);
		        currFrame.moveToFront();
			}
		});

		
		menu = new JMenu("Settings");
		menu.setMnemonic(KeyEvent.VK_S);
		menuBar.add(menu);
		
		final JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem("Windows resizable / movable");	
		toggleMovementMenuItem = cbMenuItem;
		menu.add(cbMenuItem);
		
		cbMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				CommonRuntimeData.setWindowsResizableMovable(cbMenuItem.isSelected());
				adjustToggleMovementButtonImage();
			}
		});
		cbMenuItem.setSelected(false);
		
		icon = new ImageIcon("images/restore-windows.png");
		image = icon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
		menuItem = new JMenuItem("Restore window positions", new ImageIcon(image));
		restoreWindowsMenuItem = menuItem;
		menu.add(menuItem);
		
		menuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				for (FrmAbstractParentFrame frame:frames)
				{
					frame.resizeWindow(desktop.getSize(), true);
				}
			}
		});
		
		
		menu = new JMenu("?");
		menu.setMnemonic(KeyEvent.VK_H);
		menuBar.add(menu);

		icon = new ImageIcon("images/help-browser.png");
		image = icon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
		menuItem = new JMenuItem("About", new ImageIcon(image));
		menu.add(menuItem);
		
		menuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				java.awt.EventQueue.invokeLater(new Runnable() {
		            public void run() {
		                new AboutDialog().setVisible(true);
		            }
		        });				
			}
		});

		return menuBar;
    }
	
	private void refreshOpenSubMenu() {		
		openSubMenu.removeAll();
		
		if (CommonRuntimeData.lastOpenedFiles.size() == 0)
		{
			JMenuItem disabled = new JMenuItem("< recent files will be shown here >");
			disabled.setEnabled(false);
			openSubMenu.add(disabled);
		}
		else
		{
			for (final String path:CommonRuntimeData.lastOpenedFiles)
			{
				JMenuItem curr = new JMenuItem(path);
				openSubMenu.add(curr);
				curr.addActionListener(new ActionListener() {					
					@Override
					public void actionPerformed(ActionEvent e) {
						openPropFile(path);
					}
				});
			}
		}
		
		openSubMenu.addSeparator();
		
		ImageIcon icon = new ImageIcon("images/document-open.png");
		Image image = icon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
		JMenuItem menuItem = new JMenuItem("Choose file...", new ImageIcon(image));
		openDocumentMenuItem = menuItem;
		menuItem.setMnemonic(KeyEvent.VK_O);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		openSubMenu.add(menuItem);

		// Action Listener for "open"-button
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (chooser == null) {
					chooser = new JFileChooser();
					
					
					// Set filter for ".properties" files only
					FileFilter filter = new FileFilter() {

						@Override
						public String getDescription() {
							return "Recommender101 Properties File";
						}

						@Override
						public boolean accept(File f) {
							if (f.isDirectory() || f.getName().endsWith(".properties")) {
								return true;
							}

							return false;
						}
					};

					
					chooser.setFileFilter(filter);
					// Set the file chooser dialog to start in the project
					// directory
					String path = getClass().getClassLoader().getResource(".").getPath();
					chooser.setCurrentDirectory(new File(path));
				}
				chooser.setDialogTitle("Open");
				
				int returnVal = chooser.showOpenDialog(GUI.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					openPropFile(chooser.getSelectedFile().getAbsolutePath());			
				}				
				
			}

			
		});
	}

	private void openPropFile(String absolutePath) {
		
		// Check if file exists
		if (!(new File(absolutePath)).exists())
		{
			// Show error
			JOptionPane.showMessageDialog(GUI.this,"This file doesn't exist.", "File not found",  JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		InternalR101PropertiesFile f = PropertiesFileManager.getPropertiesFile(absolutePath);
		
		f.setFile(new File(absolutePath));	
		CommonRuntimeData.setPropFile(f);		
		
		// Add to the list of last opened files
		CommonRuntimeData.addToLastOpenedFiles(absolutePath);
		
		refreshOpenSubMenu();
	}
	
	private void adjustToggleMovementButtonImage()
	{
		ImageIcon icon = null;
		if (CommonRuntimeData.isWindowsResizableMovable())
		{
			icon = new ImageIcon("images/windows-move-active.png");
		}
		else
		{
			icon = new ImageIcon("images/windows-move-inactive.png");
		}
		
        Image img = icon.getImage();
        img = img.getScaledInstance( 25, 25,  java.awt.Image.SCALE_SMOOTH );
        toggleMovementButton.setIcon(new ImageIcon(img));	
    }
 
    //React to menu selections.
    public void actionPerformed(ActionEvent e) {
        if ("new".equals(e.getActionCommand())) { //new
            createFrame();
        } else { //quit
            quit();
        }
    }
 
    
    //Create a new internal frame.
    protected void createFrame() {
        /*
        try {
            frame.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {}       
        */
    }
 
    //Quit the application.
    protected void quit() {
        System.exit(0);
    }
 
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
    	try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
    		//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);
 
        //Create and set up the window.
        GUI frame = new GUI();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Display the window.
        frame.setVisible(true);
        
        frame.setIconImage(new ImageIcon("images/rec101icon_small.png").getImage());     
        
        
    }
 
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}