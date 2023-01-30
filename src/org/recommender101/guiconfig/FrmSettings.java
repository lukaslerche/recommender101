package org.recommender101.guiconfig;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import org.recommender101.gui.annotations.R101Setting.SettingsType;

/**
 * This frame shows the settings of the currently selected item in FrmCurrentClasses
 *
 */
@SuppressWarnings("serial")
public class FrmSettings extends FrmAbstractParentFrame {

	
	
	public FrmSettings(String name, int percentWidth, int percentHeight, Dimension parentSize, int percentOffsetX,
			int percentOffsetY) {
		super(name, percentWidth, percentHeight, parentSize, percentOffsetX, percentOffsetY);
		initComponents();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void newFileLoaded(InternalR101PropertiesFile newPropFile) {
		/*JToggleButton activeButton = btnShowSelectedClassSettings;
	    if (btnShowOtherSettings.isSelected())
		{
			activeButton = btnShowOtherSettings;
		}
		
		showTab(activeButton);*/

		showTab(lastButton);
	}

	
	/**
	 * Initializes the GUI components
	 */
	private void initComponents() {

		btnShowSelectedClassSettings = new javax.swing.JToggleButton();
        btnShowOtherSettings = new javax.swing.JToggleButton();
        lastButton = btnShowSelectedClassSettings;
        
        btnShowSelectedClassSettings.setText("Selected class");
        btnShowSelectedClassSettings.setToolTipText("Shows the settings for the class that is currently selected in the above window.");
        btnShowOtherSettings.setText("General settings");
        btnShowOtherSettings.setToolTipText("Shows other settings that can be used to configure Recommender101.");
        
        jTable1 = new JTableX();
        jTable1.setFillsViewportHeight(true);
        jTable1.setRowHeight(25);
        jTable1.setRowSelectionAllowed(false);
        jScrollPane1 = new javax.swing.JScrollPane();
        jScrollPane1.setViewportView(jTable1);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);


        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnShowSelectedClassSettings)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnShowOtherSettings)
                        ))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnShowSelectedClassSettings)
                    .addComponent(btnShowOtherSettings))
                .addGap(10, 10, 10)
                .addComponent(jScrollPane1)
                )
        );

                     
        // ActionListener for "tab" buttons
        ActionListener a = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (arg0.getSource() instanceof JToggleButton)
				{
					lastButton = (JToggleButton)arg0.getSource(); 
					showTab((JToggleButton)arg0.getSource());
				}
			}
		};
		
		btnShowSelectedClassSettings.addActionListener(a);
        btnShowOtherSettings.addActionListener(a);
        
        showTab(btnShowSelectedClassSettings);
               
        
        pack();
    }
	
	/**
	 * Switches the "tab" accordingly to the clicked button
	 * Note: This method is also being called from the "ClassTree" window in order to show a tab after adding.
	 * @param clickedButton The "tab" / button that has been clicked
	 */
	public void showTab(JToggleButton clickedButton)
	{
		if (clickedButton == null)
		{
			return;
		}
		
		btnShowSelectedClassSettings.setSelected(false);
        btnShowOtherSettings.setSelected(false);
        
        clickedButton.setSelected(true);  
        lastButton = clickedButton;
        
        if (CommonRuntimeData.getPropFile() == null)
        {
        	return;
        }
               
        // Select appropriate data source
        InternalR101Class dataSource = this.currSettingClass;
        if (clickedButton == btnShowOtherSettings)
        {        	        	
        	dataSource = CommonRuntimeData.getPropFile().getOtherSettings();     	
        }
               
        
        if (dataSource == null)
        {
        	jTable1.setModel(new DefaultTableModel());
        	return;
        }
               
        jTable1.setModel(new R101TableModel(dataSource));
        addEditorsToTable(jTable1, dataSource);
        jTable1.getColumnModel().getColumn(0).setPreferredWidth(300);
        jTable1.getColumnModel().getColumn(1).setPreferredWidth(300);
        jTable1.getColumnModel().getColumn(2).setPreferredWidth(400);
        
        
        // Add Listener
        jTable1.getModel().addTableModelListener(new TableModelListener() {			
			@Override
			public void tableChanged(TableModelEvent arg0) {				
				if (arg0.getColumn() < 0)
				{
					//discard
					return;
				}
				CommonRuntimeData.notifyWindows(FrmSettings.this);				
				//System.out.println(arg0.getColumn()+" "+arg0.getFirstRow()+" "+arg0.getLastRow()+" "+(arg0.getType()));
			}
		});
        
        // Set the listening events for the delete-row function     
        if (lastMouseAdapter != null)
        {
        	// Remove the last table listener
        	jTable1.removeMouseListener(lastMouseAdapter);
        }
        lastMouseAdapter = new R101MouseAdapter(dataSource);
        jTable1.addMouseListener(lastMouseAdapter);
	}
	
	private R101MouseAdapter lastMouseAdapter = null;
	
	private class R101MouseAdapter extends java.awt.event.MouseAdapter {
		
		private InternalR101Class dataSource;
		R101MouseAdapter(InternalR101Class dataSource)
		{
			this.dataSource = dataSource;
		}
		
        @Override
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            int row = jTable1.rowAtPoint(evt.getPoint());
            int col = jTable1.columnAtPoint(evt.getPoint());
            if (col == 3 && row < jTable1.getRowCount()-1) {
                //System.out.println("Delete row "+row);
            	//InternalR101Setting set = currSettingClass.getActiveSettings().get(row);
            	InternalR101Setting set = dataSource.getActiveSettings().get(row);
            	set.setSettingEnabled(false);
            	if (set.isCustomSetting())
            	{
            		// Delete setting
            		dataSource.getSettings().remove(set);
            		//currSettingClass.getSettings().remove(set);
            	}                	
            	//showSettings(currSettingClass);
            	jTable1.removeMouseListener(this);
            	CommonRuntimeData.notifyWindows(FrmSettings.this);
            }
        }
    }
	
	private InternalR101Class currSettingClass = null;
		
	/**
	 * Called from another window. Shows the settings of a given InternalR101Class
	 * @param c InternalR101Class which settings should be shown
	 */
	public void showSettings(InternalR101Class c)
	{
		this.currSettingClass = c;
		showTab(btnShowSelectedClassSettings);
	}
	
	/**
	 * This empties the table
	 */
	public void emptySettingView()
	{
		this.currSettingClass = null;
		// Only refresh if it is currently being shown
		if (btnShowSelectedClassSettings.isSelected())
		{
			showTab(btnShowSelectedClassSettings);
		}
	}

	/**
	 * Adds the appropriate CellEditors to the table
	 * 
	 * @param table
	 *            A JTableX instance
	 */
	private void addEditorsToTable(JTableX table, InternalR101Class c) {
		// Reset the RowEditorModel
		RowEditorModel rm = new RowEditorModel();
		table.setRowEditorModel(rm);

		ArrayList<TableCellEditor> editors = createTableCellEditors(c);

		for (int i = 0; i < editors.size(); i++) {
			rm.addEditorForRow(i, editors.get(i));
		}
	}

	/**
	 * Creates all needed TableCellEditor objects for the given class *
	 * 
	 * @param c
	 *            An InternalR101Class object containing the settings
	 * @return An ArrayList of TableCellEditor containing an editor for each
	 *         setting
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ArrayList<TableCellEditor> createTableCellEditors(InternalR101Class c) {
		
		ArrayList<TableCellEditor> list = new ArrayList<TableCellEditor>();

		//ArrayList<InternalR101Setting> settings = c.getSettings(); commented out 04.2014
		ArrayList<InternalR101Setting> settings = c.getActiveSettings();
		for (InternalR101Setting s : settings) {
			switch (s.getType()) {
			case FILE:
				list.add(new FileChooserCellEditor());
				break;
			case ARRAY:
				// Create a DefaultCellEditor containing a ComboBox
				JComboBox b = new JComboBox();
				for (String value : s.getValues()) {
					b.addItem(value);
				}
				list.add(new DefaultCellEditor(b));
				break;
			case BOOLEAN:
				// Create a DefaultCellEditor containing a ComboBox, values true
				// and false
				JComboBox b2 = new JComboBox();

				b2.addItem("true");
				b2.addItem("false");

				list.add(new DefaultCellEditor(b2));
				break;
			case INTEGER:
				DefaultCellEditor e1 = new DefaultCellEditor(new JFormattedTextField(NumberFormat.getIntegerInstance()));
				e1.setClickCountToStart(1);
				list.add(e1);
				break;
			case DOUBLE:
				DefaultCellEditor e2 = new DefaultCellEditor(new JFormattedTextField(NumberFormat.getInstance()));
				e2.setClickCountToStart(1);
				list.add(e2);
				break;
			case TEXT:
			default:
				// As fallback, create a normal textfield
				DefaultCellEditor e3 = new DefaultCellEditor(new JTextField());
				e3.setClickCountToStart(1);
				list.add(e3);
			}

		}
		
		// Add dropdown editor for the add dropdown box 04/2014
		final JComboBox b = new JComboBox();
		b.setEditable(true);
		b.getEditor().getEditorComponent().addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {				
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				b.getEditor().setItem("");
			}
		});
		for (InternalR101Setting curr : c.getSettings()) {
			if (!settings.contains(curr)){
				// This setting is currently not active (= not shown in the list) and should be shown in the dropdown
				b.addItem(curr.getDisplayName());
			}
			
		}
	
		list.add(new DefaultCellEditor(b));
		

		return list;
	}

	private class R101TableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		private InternalR101Class c;
		private ImageIcon delIcon;
		
		
		
		private String[] columnNames = { "Name", "Value", "Description", "Remove" };

		public R101TableModel(InternalR101Class c) {
			this.c = c;
			delIcon = new ImageIcon("images/delete.png");
			Image img = delIcon.getImage();
	        img = img.getScaledInstance( 20, 20,  java.awt.Image.SCALE_SMOOTH );
	        delIcon = new ImageIcon(img);	        
	        
		}

		
		
		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (aValue == null) {
				return;
			}

			// 04/2014 added following if
			if ( rowIndex == getRowCount()-1 && columnIndex == 0)
			{
				if (aValue.equals("") || aValue.equals("Click here to add setting"))
				{
					return;
				}
				
				// Search if there is an disabled setting with the selected name, otherwise create a custom setting				
				boolean found = false;
				for (InternalR101Setting curr : c.getSettings()) {
					if (!curr.isSettingEnabled()){
						if (curr.getDisplayName().equals(aValue))
						{
							curr.setSettingEnabled(true);
							found = true;
						}
					}					
				}
				if (!found)
				{
					InternalR101Setting set = new InternalR101Setting();
					set.setSettingName(aValue.toString());
					set.setDisplayName(aValue.toString());
					set.setSettingEnabled(true);
					c.getSettings().add(set);
				}
				
				//System.out.println("Selected "+aValue);
				//checken ob in general settings jetzt was spinnt
			}
			
			
			else if (columnIndex == 0) {
				// A custom setting's title has been edited
				//InternalR101Setting s = c.getSettings().get(rowIndex); 23.04.2013 commented out
				InternalR101Setting s = c.getActiveSettings().get(rowIndex);
				s.setSettingName(aValue.toString());
				s.setDisplayName(aValue.toString());
			} 
			/*else if (columnIndex == 3) commented out 04.2014 (this column now contains a delete icon)
			{
				// Setting enabled or disabled
				// TO DO "remove line", which is setting the setting disabled
				InternalR101Setting s = c.getSettings().get(rowIndex);
				s.setSettingEnabled((boolean)aValue);
			}*/
			else if (columnIndex == 1) {
				// Fetch the corresponding setting object
				//InternalR101Setting s = c.getSettings().get(rowIndex); commented out 23.04.2013
				InternalR101Setting s = c.getActiveSettings().get(rowIndex);
				if (s.getType() == SettingsType.BOOLEAN) {
					s.setCurrentValue(Boolean.parseBoolean(aValue.toString()));
					//Debug.log("New boolean value: " + s.getCurrentValue().toString());
				} else {
					// If the setting is of a number type, discard the changes
					// if the value is out of bounds
					if (s.getType() == SettingsType.DOUBLE || s.getType() == SettingsType.INTEGER) {
						double num = 0;
						try {
							num = Double.parseDouble((String) aValue);
						} catch (NumberFormatException e) {
							return;
						}
						if (num < s.getMinValue() || num > s.getMaxValue()) {
							return;
						}
					}
					s.setCurrentValue(aValue);
					//Debug.log("New non-boolean value: " + s.getCurrentValue().toString());
				}

			}

			// Fire listeners
			fireTableCellUpdated(rowIndex, columnIndex);
			fireTableRowsUpdated(rowIndex, rowIndex);
			fireTableDataChanged();
			
			super.setValueAt(aValue, rowIndex, columnIndex);
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public int getRowCount() {
			//return c.getSettings().size();
			// 04.2014 changed to only show active settings for the new layout
			
			// Add 1 for the line with the dropdown to choose setting to add
			return c.getActiveSettings().size()+1;
		}

		@Override
		public String getColumnName(int col) {
			return columnNames[col];
		}

			
		
		@Override
		public Object getValueAt(int row, int col) {
			// Last line contains the "add" dropdown 04.2014
			if (row == c.getActiveSettings().size())
			{
				if (col > 0)
				{
					return null;
				}
				
				return "Click here to add setting";
			}
			
			
			switch (col) {
			case 0:
				//return c.getSettings().get(row).getDisplayName();
				return c.getActiveSettings().get(row).getDisplayName();
			case 2:
				//return c.getSettings().get(row).getDescription();
				return c.getActiveSettings().get(row).getDescription();
			case 1:
				/*if (c.getSettings().get(row).getCurrentValue() != null) {
					return c.getSettings().get(row).getCurrentValue().toString();
				}*/
				if (c.getActiveSettings().get(row).getCurrentValue() != null) {
					return c.getActiveSettings().get(row).getCurrentValue().toString();
				}
			case 3: // Enabled checkbox
				InternalR101Setting s = c.getActiveSettings().get(row);
				if (s != null && !s.getSettingName().equals(""))
					return delIcon;
				return null;				
				//return c.getSettings().get(row).isSettingEnabled();
			default:
				return null;
			}

		}

		@Override
		public boolean isCellEditable(int row, int col) {
			
			// First 2 ifs added 04/2014
			if (row == c.getActiveSettings().size() && col == 0)
			{
				return true;
			}
			else if (row == c.getActiveSettings().size() && col > 0)
			{
				return false;
			}
			//if (c.getSettings().get(row).isCustomSetting() && col == 0) { 04/2014
		    else if (c.getActiveSettings().get(row).isCustomSetting() && col == 0) {
				return true;
			}				
			else if (col == 1){// || col == 3){ // changed for enabled checkbox to || 3
				// 04.2014: deactivated for col 3 for the new delete-row icon
				return true;
			}
			
			return false;
		}



		@Override
		public Class<?> getColumnClass(int columnIndex) {
			
			if (columnIndex == 3)
			{
				return ImageIcon.class;
				//return Boolean.class;
			}
			
			return super.getColumnClass(columnIndex);
		}
	}
	
	// GUI components
	public javax.swing.JToggleButton btnShowSelectedClassSettings;
	public javax.swing.JToggleButton btnShowOtherSettings;
    private JTableX jTable1;
    private javax.swing.JScrollPane jScrollPane1;
    
    private JToggleButton lastButton = btnShowSelectedClassSettings;
}
