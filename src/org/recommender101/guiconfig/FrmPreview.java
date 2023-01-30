package org.recommender101.guiconfig;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

@SuppressWarnings("serial")
public class FrmPreview extends FrmAbstractParentFrame {
	private int fontsize = 11;
	
	/**
	 * Name of the window
	 */
	private String name = "";
	
	private Font btnApplyFont1;
	private Font btnApplyFont2;
	
	private JButton btnApply;
	
	public FrmPreview(String name, int percentWidth, int percentHeight, Dimension parentSize, int percentOffsetX,
			int percentOffsetY) {
		super(name, percentWidth, percentHeight, parentSize, percentOffsetX, percentOffsetY);
		this.name = name;
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
		txt.setText(CommonRuntimeData.getPropFile().toString());
		txt.setCaretPosition(0);
		resetApplyButton();
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
        
        txt=new JTextPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

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
     	StyledDocument doc = txt.getStyledDocument();
		((AbstractDocument)doc).setDocumentFilter(new R101DocumentFilter(doc));
     	
		
		// Apply changes button
		this.btnApply = new JButton("Apply changes");
		
		
		this.btnApplyFont1 = btnApply.getFont();
		this.btnApplyFont2 = new Font(btnApply.getFont().getName(),Font.BOLD,btnApply.getFont().getSize());
		

		toolBar.add(btnApply);
		
		btnApply.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				// Commit changes
				CommonRuntimeData.setPropFile(PropertiesFileManager.parsePropertiesFile(txt.getText()));
				
				resetApplyButton();
			}
		});
		
		txt.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				updateApplyButton();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateApplyButton();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				updateApplyButton();
			}		
			
		});
		
     	
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
		
        pack();
        
    }
	
	private void resetApplyButton()
	{
		setTitle(name);
		
		// Old font
		btnApply.setFont(btnApplyFont1);
		btnApply.setForeground(Color.BLACK);
	}
	
	private void updateApplyButton()
	{
		setTitle(name+" (unsaved changes)");
		
		btnApply.setFont(btnApplyFont2);
		btnApply.setForeground(Color.decode("#218606"));	
	}
	
	private void changeFontSize(int newSize)
	{
		this.fontsize = newSize;
		txt.setFont(new Font("Consolas", Font.PLAIN, fontsize));
	}
	
	    
    private class R101DocumentFilter extends DocumentFilter
	{
		private StyledDocument doc = null;
		Style propName,  error, comment, setting, propValue;
		
		public R101DocumentFilter(StyledDocument doc)
		{
			this.doc = doc;
			
			// Style for property names
			propName = txt.addStyle("propName", null);
			StyleConstants.setForeground(propName, new Color(149,0,85));
			StyleConstants.setBold(propName, true);
			
			setting = txt.addStyle("setting", null);
			StyleConstants.setForeground(setting, new Color(58,0,213));
			
			propValue = txt.addStyle("propValue", null);
			StyleConstants.setForeground(propValue, Color.DARK_GRAY);
			StyleConstants.setBold(propValue, true);
			
			
			//StyleConstants.setItalic(settingValue, true);
			
			comment = txt.addStyle("comment", null);
			StyleConstants.setForeground(comment, new Color(63,127,95));
			StyleConstants.setItalic(comment, true);
			
			
			error = txt.addStyle("error",null);
			StyleConstants.setBackground(error, Color.red);
			StyleConstants.setForeground(error, Color.white);
			
		}

		@Override
		public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
				throws BadLocationException {	
			super.insertString(fb, offset, string, attr);
			insertUpdate();
		}

		@Override
		public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
			super.remove(fb, offset, length);
			insertUpdate();
		}

		@Override
		public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
				throws BadLocationException {
			super.replace(fb, offset, length, text, attrs);
			insertUpdate();
		}
		
		
		public void insertUpdate() {				
			String[] text = txt.getText().split("\r\n");

			boolean lastLineEscapedLinebreak = false;
			boolean lastLineWasError = false;
		
			// First, reset all styles			
			doc.setCharacterAttributes(0, txt.getText().length(), txt.getStyle("common"), true);
			
			
			int CurrPos = 0;
			for (String line : text) {
				String check = line.trim();
				// Quickly determine how large the white space that has been trimmed is using this excellent code
				int num = 0;
				String test="";
				while(line.startsWith(test))
				{
					test+=" ";
					num++;
				}
				
				int pos = CurrPos+num-1;
				
				if (check.startsWith("#") || check.startsWith("!"))
				{
					// Line is a comment
					doc.setCharacterAttributes(pos, line.length(), txt.getStyle("comment"), true);
				}
				else if (check.contains("=") && (check.indexOf("=") < check.indexOf(":") || check.indexOf(":") == -1))
				{
					// Line contains a new property definition					
					doc.setCharacterAttributes(pos, check.indexOf("="), txt.getStyle("propName"), true);
					lastLineWasError = false;
					
					// Remove the property name and colorize the properties
					int offset = check.indexOf("=")+1;
					String cut = check.substring(offset);
					
					int endIndex = check.length()-offset;
					if (cut.contains(":"))
					{
						endIndex = cut.indexOf(":");
					}
					
					doc.setCharacterAttributes(pos+offset, endIndex, txt.getStyle("propValue"), true);
					
					// If there are settings, colorize them properly
					/*if (cut.contains(":"))
					{
						offset = offset+cut.indexOf(":")+1;
						cut = cut.substring(cut.indexOf(":")+1);
													
						if (cut.endsWith("\\"))
						{
							cut = cut.substring(0, cut.length()-1);
							// TO DO -1 or -2?
						}
						
						// Iterate over each setting
						String[] cutSettings = cut.split("\\|");
						for (int i=0; i<cutSettings.length; i+=1)
						{
							String curr = cutSettings[i];
							endIndex = curr.length();
							
							// If setting has a value, only colorize until the =
							if (curr.contains("="))
							{
								endIndex = curr.indexOf("=");
							}
							doc.setCharacterAttributes(pos+offset, endIndex, txt.getStyle("setting"), true);
							
							if (endIndex < curr.length())
							{
								doc.setCharacterAttributes(pos+offset+endIndex+1, curr.length()-endIndex, txt.getStyle("settingValue"), true);
							}
							
							offset += curr.length()+1;
						}
						
						// Colorize the rest as a setting name
						if (offset < line.length())
						{
							doc.setCharacterAttributes(pos+offset, line.length(), txt.getStyle("setting"), true);							
						}
					}*/
				}				
				else if (lastLineEscapedLinebreak && !lastLineWasError)
				{
					// This line will be parsed as one single line with the above and is therefore not faulty
					
					lastLineWasError = false;
					String cut = check;
					
					int endIndex = check.length();
					if (cut.contains(":"))
					{
						endIndex = cut.indexOf(":");
					}
					
					doc.setCharacterAttributes(pos, endIndex, txt.getStyle("propValue"), true);
					
					// If there are settings, colorize them properly
					/*if (cut.contains(":"))
					{
						int offset = cut.indexOf(":")+1;
						cut = cut.substring(cut.indexOf(":")+1);
													
						if (cut.endsWith("\\"))
						{
							cut = cut.substring(0, cut.length()-1);
							// TO DO -1 or -2?
						}
						
						// Iterate over each setting
						String[] cutSettings = cut.split("\\|");
						for (int i=0; i<cutSettings.length; i+=1)
						{
							String curr = cutSettings[i];
							endIndex = curr.length();
							
							// If setting has a value, only colorize until the =
							if (curr.contains("="))
							{
								endIndex = curr.indexOf("=");
							}
							doc.setCharacterAttributes(pos+offset, endIndex, txt.getStyle("setting"), true);
							
							if (endIndex < curr.length())
							{
								doc.setCharacterAttributes(pos+offset+endIndex+1, curr.length()-endIndex, txt.getStyle("settingValue"), true);
							}
							
							offset += curr.length()+1;
						}
						
						// Colorize the rest as a setting name
						if (offset < line.length())
						{
							doc.setCharacterAttributes(pos+offset, line.length(), txt.getStyle("setting"), true);							
						}
					}*/
				}				
				else
				{
					// Line not recognized
					doc.setCharacterAttributes(pos, line.length(), txt.getStyle("error"), true);
					lastLineWasError = true;
				}
							
				// If line wasn't a comment and is non-empty, save new value for escaping
				if (!(check.startsWith("#") || check.startsWith("!")))
				{
					lastLineEscapedLinebreak = line.endsWith("\\");
					
					if (check.endsWith("\\"))
					{
						doc.setCharacterAttributes(pos+check.length()-1, line.length(), txt.getStyle("common"), true);
					}
				}
				CurrPos += line.length()+1;
			}
					
		}
		
	}
    

	// GUI components
	private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane txt;
    private javax.swing.JToolBar toolBar;
}
