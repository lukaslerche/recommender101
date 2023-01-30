package org.recommender101.guiconfig;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

/**
 * This frame shows the currently selected recommenders, metrics etc.
 *
 */
@SuppressWarnings("serial")
public class FrmCurrentClasses extends FrmAbstractParentFrame {

	public FrmCurrentClasses(String name, int percentWidth, int percentHeight, Dimension parentSize, int percentOffsetX,
			int percentOffsetY) {
		super(name, percentWidth, percentHeight, parentSize, percentOffsetX, percentOffsetY);
		initComponents();
	}
	
	/**
	 * The currently active "tab" is saved in this variable
	 */
	private JToggleButton activeTab = null;

	/**
	 * Current data source for the table model
	 */
	private ArrayList<InternalR101Class> currentDataSource = null;


	/**
	 * Needed to prevent the settings window from switching the tab when general settings are being edited
	 */
	private boolean currentlyProcessingNewFileLoaded = false;
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void newFileLoaded(InternalR101PropertiesFile newPropFile) {	
		currentlyProcessingNewFileLoaded = true;
		int prevSelection = jTable1.getSelectedRow();
		showTab(activeTab);	
		if (jTable1.getRowCount() > prevSelection)
		{
			jTable1.getSelectionModel().setSelectionInterval(prevSelection, prevSelection);
		}
		currentlyProcessingNewFileLoaded = false;
	}

	
	/**
	 * Initializes the GUI components
	 */
	private void initComponents() {

		toolBar = new javax.swing.JToolBar();
		btnShowRecommenders = new javax.swing.JToggleButton();
		btnShowRecommenders.setToolTipText("Shows all recommenders in the current properties file. To add more, double-click an item in the window on the left.");
        
		btnShowMetrics = new javax.swing.JToggleButton();
        btnShowMetrics.setToolTipText("Shows all metrics in the current properties file. To add more, double-click an item in the window on the left.");
        
        btnShowDataSplitter = new javax.swing.JToggleButton();
        btnShowDataSplitter.setToolTipText("Shows the currently selected data splitter. To change it, double-click an item in the window on the left.");
        
        btnShowDataLoader = new javax.swing.JToggleButton();
        btnShowDataLoader.setToolTipText("Shows the currently selected data loader. To change it, double-click an item in the window on the left.");
        
        btnShowRecommenders.setText("Recommenders");
        btnShowMetrics.setText("Metrics");
        btnShowDataSplitter.setText("Data Splitter");
        btnShowDataLoader.setText("Data Loader");
        
        jTable1 = new javax.swing.JTable();
        jTable1.setFillsViewportHeight(true);
        jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1 = new javax.swing.JScrollPane();
        jScrollPane1.setViewportView(jTable1);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        toolBar.setRollover(true);
        toolBar.setFloatable(false);
        toolBar.add(Box.createHorizontalGlue());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolBar)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnShowRecommenders)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnShowMetrics)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnShowDataLoader)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnShowDataSplitter)
                        ))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnShowRecommenders)
                    .addComponent(btnShowMetrics)
                    .addComponent(btnShowDataLoader)
                    .addComponent(btnShowDataSplitter))
                .addGap(10, 10, 10)
                .addComponent(jScrollPane1)
                )
        );

        JButton btnSort = CommonRuntimeData.makeButton("sortABC.png", "SORT", "Sorts the currently shown items alphabetically.", "Sort alphabetically", null);
        toolBar.add(btnSort);
        btnSort.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				ClassScanner.sortClassesAlphabetically(currentDataSource);
				showTab(activeTab);
			}
		});
        
        final JButton btnMoveUp = CommonRuntimeData.makeButton("arrow_up.png", "MOVEUP", "Moves the currently selected item up. (ALT + UP-ARROW)", "Move up", null);
        toolBar.add(btnMoveUp);
        
        // Key Binding
        btnMoveUp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.ALT_MASK), "action");
        btnMoveUp.getActionMap().put("action", new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        	  for (ActionListener a:btnMoveUp.getActionListeners())
          	  {
          		  a.actionPerformed(e);
          	  } 
        	}
        });	
        
        btnMoveUp.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentDataSource.size() < 2 || jTable1.getSelectedRow() == -1)
				{
					return;
				}
				
				int row = jTable1.getSelectedRow();
				InternalR101Class c = currentDataSource.get(row);
				currentDataSource.remove(row);
				
				// We want to move up, so insert 1 index before
				int newRow = row-1;
				if (newRow < 0)
				{
					newRow = 0;
				}
				currentDataSource.add(newRow, c);

				// Reload tab
				showTab(activeTab);
				
				// Keep selection
				jTable1.getSelectionModel().setSelectionInterval(newRow, newRow);
				CommonRuntimeData.notifyWindows(FrmCurrentClasses.this);
			}
		});
        
        final JButton btnMoveDown = CommonRuntimeData.makeButton("arrow_down.png", "MOVEDOWN", "Moves the currently selected item down. (ALT + DOWN-ARROW)", "Move down", null);
        toolBar.add(btnMoveDown);
        
        // Key Binding
        btnMoveDown.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.ALT_MASK), "action");
        btnMoveDown.getActionMap().put("action", new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        	  for (ActionListener a:btnMoveDown.getActionListeners())
          	  {
          		  a.actionPerformed(e);
          	  } 
        	}
        });	
        
        btnMoveDown.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentDataSource.size() < 2 || jTable1.getSelectedRow() == -1)
				{
					return;
				}
				
				int row = jTable1.getSelectedRow();
				InternalR101Class c = currentDataSource.get(row);
				currentDataSource.remove(row);
				
				// We want to move up, so insert 1 index before	
				int newRow = row+1;
				if (newRow > currentDataSource.size())
				{
					newRow = currentDataSource.size();
				}
				
				currentDataSource.add(newRow, c);

				// Reload tab
				showTab(activeTab);
				
				// Keep selection
				jTable1.getSelectionModel().setSelectionInterval(newRow, newRow);
				CommonRuntimeData.notifyWindows(FrmCurrentClasses.this);
			}
		});
        
        final JButton btnDelete = CommonRuntimeData.makeButton("delete.png", "DELETE", "Deletes the current selection.", "Delete", null);
        toolBar.add(btnDelete);
        
        // Key Binding
        btnDelete.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "action");
        btnDelete.getActionMap().put("action", new AbstractAction() {
        	public void actionPerformed(ActionEvent e) {
        	  for (ActionListener a:btnDelete.getActionListeners())
        	  {
        		  a.actionPerformed(e);
        	  }
        	}
        });	
        btnDelete.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				if (currentDataSource.size() <1 || jTable1.getSelectedRow() == -1)
				{
					return;
				}
				
				// if data splitter oder data loader tab is selected, it's a special case
				if (activeTab == btnShowDataLoader)
				{
					CommonRuntimeData.getPropFile().setDataloader(null);
					showTab(btnShowDataLoader);
				}
				else if (activeTab == btnShowDataSplitter)
				{
					CommonRuntimeData.getPropFile().setDatasplitter(null);
					showTab(btnShowDataSplitter);
				}
				else
				{
					int selRow = jTable1.getSelectedRow();
					currentDataSource.remove(selRow);
					showTab(activeTab);
					
					if (selRow > currentDataSource.size()-1)
					{
						selRow = currentDataSource.size()-1;
					}
					jTable1.getSelectionModel().setSelectionInterval(selRow, selRow);
				}
				CommonRuntimeData.notifyWindows(FrmCurrentClasses.this);
			}
		});
        
        // ActionListener for "tab" buttons
        ActionListener a = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (arg0.getSource() instanceof JToggleButton)
				{
					showTab((JToggleButton)arg0.getSource());
				}
			}
		};
		
		btnShowRecommenders.addActionListener(a);
        btnShowMetrics.addActionListener(a);
        btnShowDataSplitter.addActionListener(a);
        btnShowDataLoader.addActionListener(a);
        
        showTab(btnShowRecommenders);

        // Selection Listener for Table
        jTable1.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				
				 if (arg0.getValueIsAdjusting()) 
				 {
				        return;
				 }
				int row = jTable1.getSelectedRow();
				
				if (!currentlyProcessingNewFileLoaded)
				{
					
				if (row < 0 || currentDataSource == null)
				{
					// Nothing selected
					CommonRuntimeData.frmSettings.emptySettingView();
					return;
				}
				
				
					CommonRuntimeData.frmSettings.showSettings(currentDataSource.get(row));
				}
			}
		});        
        pack();
    }
	
	
	/**
	 * Switches the "tab" accordingly to the clicked button
	 * Note: This method is also being called from the "ClassTree" window in order to show a tab after adding.
	 * @param clickedButton The "tab" / button that has been clicked
	 */
	public void showTab(JToggleButton clickedButton)
	{
		btnShowRecommenders.setSelected(false);
        btnShowMetrics.setSelected(false);
        btnShowDataSplitter.setSelected(false);
        btnShowDataLoader.setSelected(false);
        
        clickedButton.setSelected(true);        
        this.activeTab = clickedButton;
        
        if (CommonRuntimeData.getPropFile() == null)
        {
        	return;
        }
        
        // Label Buttons
        btnShowRecommenders.setText("Recommenders (" + CommonRuntimeData.getPropFile().getRecommenders().size() + ")");
        btnShowMetrics.setText("Metrics (" + CommonRuntimeData.getPropFile().getMetrics().size()+ ")");
        
        // Select appropriate data source
        ArrayList<InternalR101Class> dataSource = CommonRuntimeData.getPropFile().getRecommenders();
        if (clickedButton == btnShowMetrics)
        {
        	dataSource = CommonRuntimeData.getPropFile().getMetrics();
        }
        else if (clickedButton == btnShowDataSplitter)
        {        	        	
        	dataSource = new ArrayList<InternalR101Class>();        	
        	dataSource.add(CommonRuntimeData.getPropFile().getDatasplitter());	        	
        }
        else if (clickedButton == btnShowDataLoader)
        {       	        	
        	dataSource = new ArrayList<InternalR101Class>();
        	dataSource.add(CommonRuntimeData.getPropFile().getDataloader());	       	
        }
        
        
        this.currentDataSource = dataSource;
        jTable1.setModel(new R101TableModel(dataSource));
        jTable1.getColumnModel().getColumn(0).setPreferredWidth(300);
        jTable1.getColumnModel().getColumn(1).setPreferredWidth(700); 
        
	}
	
	private class R101TableModel extends AbstractTableModel
	{		
		private static final long serialVersionUID = 1L;
		private ArrayList<InternalR101Class> dataSource;
		
		public R101TableModel(ArrayList<InternalR101Class> dataSource)
		{
			this.dataSource = dataSource;
		}
		
		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public int getRowCount() {
			return dataSource.size();
		}

		@Override
		public Object getValueAt(int row, int column) {
			InternalR101Class obj = dataSource.get(row);
			if (obj == null)
			{
				return null;
			}
			if (column == 0)
			{
				return obj.getDisplayName();
			}
			else
			{
				return obj.getPrettySettingsString();
			}
		}

		@Override
		public String getColumnName(int column) {
			if (column == 0)
			{
				return "Class";
			}
			else
			{
				return "Settings";
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {			
			return false;
		}
		
	}
		
	
	// GUI components
	public javax.swing.JToggleButton btnShowDataLoader;
	public javax.swing.JToggleButton btnShowDataSplitter;
	public javax.swing.JToggleButton btnShowRecommenders;
	public javax.swing.JToggleButton btnShowMetrics;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JTable jTable1;
    private javax.swing.JScrollPane jScrollPane1;
}
