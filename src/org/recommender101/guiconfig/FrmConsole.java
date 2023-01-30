package org.recommender101.guiconfig;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.security.Permission;
import java.util.List;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.recommender101.Recommender101;
import org.recommender101.eval.interfaces.EvaluationResult;

@SuppressWarnings("serial")
public class FrmConsole extends JInternalFrame {
		
	private int fontsize = 11;
	
	// Each new console window is placed a little bit more to the lower right
	private static int count = 0;
	
	public FrmConsole(Dimension parentSize) {
		super("Console Output #"+(++count), 
	              true, //resizable
	              true, //closable
	              true, //maximizable
	              true);//iconifiable
		txt = new JTextPane();
		
		initComponents();
		
		// Position and resize window
		double percentWidth = (double)50/100;
		double  percentHeight = (double)50/100;
        
		double percentOffsetX = (double)(20+count)/100;
		double percentOffsetY = (double)(20+count*2)/100;
		
		
		setSize((int)((double)parentSize.width*percentWidth), 
    			(int)((double)parentSize.height*percentHeight));   
    	setLocation((int)((double)parentSize.width*percentOffsetX), 
    			(int)((double)parentSize.height*percentOffsetY));
    	
    	
    	
		
		// Start R101
		// Redirect Console output
		txt.setText("");
		CommonRuntimeData.experimentRunning = true;
		redirectSystemStreams();
				
		// Run R101 with currently loaded configuration
		final Properties runProps = new Properties();
		try {
			runProps.load(new StringReader(CommonRuntimeData.getPropFile().toString()));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		final Thread r101 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// Recommender101's calls of system.exit() should be discarded
				SecurityManager oldManager = System.getSecurityManager();
				System.setSecurityManager(new SecurityManager(){
					@Override public void checkExit(int status) {
					    throw new SecurityException();
					  }

					@Override
					public void checkPermission(Permission perm, Object context) {
						return; // Allow
					}

					@Override
					public void checkPermission(Permission perm) {
						return; // Allow
					}				
				});
				
				try {					
					Recommender101 run = new Recommender101(runProps);
					run.runExperiments();
					
					// Show results
					List<EvaluationResult> finalResult = run.getLastResults();
					run.printSortedEvaluationResults(finalResult);			
					
					
				} catch (Exception e1) {
					if (e1 instanceof SecurityException)
					{
						// Rec101 has called system.exit()
						System.out.println("*** ERROR *** Recommender101 has called System.exit()!");
					}
					else
					{
						e1.printStackTrace();
					}
				}
				finally {
					// Restore output streams
					System.setOut(CommonRuntimeData.stdout);
					System.setErr(CommonRuntimeData.stderr);
					
					// Restore security manager
					System.setSecurityManager(oldManager);
					
					CommonRuntimeData.experimentRunning = false;

					btnStatus.setEnabled(false);
					btnStatus.setText("Status: Not running");
			     	btnStatus.setForeground(Color.BLACK);
				}
			}
		});
		
		r101.start();
		
		btnStatus.addActionListener(new ActionListener() {
			
			@SuppressWarnings("deprecation")
			@Override
			public void actionPerformed(ActionEvent e) {
				r101.stop();
				btnStatus.setEnabled(false);
				btnStatus.setText("Status: Not running");
		     	btnStatus.setForeground(Color.BLACK);
			}
		});
		
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addInternalFrameListener(new javax.swing.event.InternalFrameAdapter() {
		      @SuppressWarnings("deprecation")
			public void internalFrameClosing(InternalFrameEvent e) {
		        if (r101.isAlive())
		        {
		        	int reply = JOptionPane.showConfirmDialog(null, "Recommender101 is still running. +" +
		        			"By closing this window, it will be stopped. Are you sure?", "Recommender101 still running",
		        			JOptionPane.YES_NO_OPTION);
		            if (reply == JOptionPane.YES_OPTION) {
		            	r101.stop();
		            	FrmConsole.this.dispose();
		            }
		        }
		        else
			      {
			    	  FrmConsole.this.dispose();
			      }
		      }
		      
		});
	}
	
	/**
	  * Redirects output to txt swing component inside the window
	  */
	 private void redirectSystemStreams() {
		    OutputStream out = new OutputStream() {
		      @Override
		      public void write(final int b) throws IOException {
		    	  updateConsoleTextPane(String.valueOf((char) b));
		      }

		      @Override
		      public void write(byte[] b, int off, int len) throws IOException {
		    	  updateConsoleTextPane(new String(b, off, len));
		      }

		      @Override
		      public void write(byte[] b) throws IOException {
		        write(b, 0, b.length);
		      }
		    };

		    System.setOut(new PrintStream(out, true));
		    System.setErr(new PrintStream(out, true));
		  }

	 
	 	/**
		 * Appends text to txt
		 * @param text The text to be appended
		 */
		 private void updateConsoleTextPane(final String text) {
		    SwingUtilities.invokeLater(new Runnable() {
		      public void run() {
		        Document doc = txt.getDocument();
		        try {
		          doc.insertString(doc.getLength(), text, null);
		        } catch (BadLocationException e) {
		          throw new RuntimeException(e);
		        }
		        txt.setCaretPosition(doc.getLength() - 1);
		      }
		    });
		 }
	
	
	
	
	/**
	 * Initializes the GUI components
	 */
	private void initComponents() {
		toolBar = new javax.swing.JToolBar();
        jScrollPane1 = new javax.swing.JScrollPane();
        
        toolBar.setRollover(true);
        toolBar.setFloatable(false);
        toolBar.add(Box.createHorizontalGlue());
        

        

        jScrollPane1.setViewportView(txt);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolBar)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE))
        );
        
        // Preview text area
     	txt.setFont(new Font("Consolas", Font.PLAIN, fontsize));
     	
     	
     	btnStatus = new JButton("Status: Running (click here to stop)");
     	toolBar.add(btnStatus);
     	btnStatus.setForeground(Color.decode("#218606"));	
     	
     	toolBar.addSeparator();
     	
		JButton btnFontUp = CommonRuntimeData.makeButton("fontsizeup.png", "FONTUP", "Increases the font size.", "Font size up", null);
		toolBar.add(btnFontUp);
		 
		JButton btnFontDown = CommonRuntimeData.makeButton("fontsizedown.png", "FONTDOWN", "Decreases the font size.", "Font size down", null);
		toolBar.add(btnFontDown);
		
		ActionListener a = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("FONTUP"))
				{
					changeFontSize(fontsize+1);
				}
				else if (e.getActionCommand().equals("FONTDOWN"))
				{
					changeFontSize(fontsize-1);
				}
			}
		};
		
		btnFontUp.addActionListener(a);
		btnFontDown.addActionListener(a);
		
		txt.setEditable(false);
		
        pack();
    }
	
	
	private void changeFontSize(int newSize)
	{
		this.fontsize = newSize;
		txt.setFont(new Font("Consolas", Font.PLAIN, fontsize));
	}
	


	// GUI components
	private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane txt;
    private javax.swing.JToolBar toolBar;
    private JButton btnStatus;
}
