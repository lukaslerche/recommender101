package org.recommender101.guiconfig;

import java.awt.Dimension;

@SuppressWarnings("serial")
public class FrmDescription extends FrmAbstractParentFrame {

	public FrmDescription(String name, int percentWidth, int percentHeight, Dimension parentSize, int percentOffsetX,
			int percentOffsetY) {
		super(name, percentWidth, percentHeight, parentSize, percentOffsetX, percentOffsetY);
		initComponents();
	}

	/**
	 * Shows a String in the text field
	 * @param text The String to be shown
	 */
	public void showText(String text)
	{
		txt.setText(text);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void newFileLoaded(InternalR101PropertiesFile newPropFile) {
		// Empty the text field
		txt.setText("");
	}

	
	/**
	 * Initializes the GUI components
	 */
	private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        
        txt = new javax.swing.JTextPane();
        

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jScrollPane1.setViewportView(txt);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
        
        txt.setEditable(false);
        
        pack();
    }
	
	// GUI components
	private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane txt;
}
