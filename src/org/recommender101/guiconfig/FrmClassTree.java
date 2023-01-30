package org.recommender101.guiconfig;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * This is the "class library" window
 * @author timkraemer
 *
 */
@SuppressWarnings("serial")
public class FrmClassTree extends FrmAbstractParentFrame{
	
	/**
	 * Indicates whether the classes have already been loaded
	 */
	boolean classesLoaded = false;
	private ArrayList<InternalR101Class> recommender = null;
	private ArrayList<InternalR101Class> metrics = null;
	private ArrayList<InternalR101Class> datasplitter = null;
	private ArrayList<InternalR101Class> dataloader = null;
	
	/**
	 * Default text for the quicksearch box
	 */
	private final String defaultText = "Quicksearch (Ctrl+F)";
	
	public FrmClassTree(String name, int percentWidth, int percentHeight, Dimension parentSize, int percentOffsetX,
			int percentOffsetY) {
		super(name, percentWidth, percentHeight, parentSize, percentOffsetX, percentOffsetY);		
		// Init own components
		initComponents();
				
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// Load Java classes
				ClassScanner scanner = new ClassScanner();
				final ArrayList<InternalR101Class> recommender = scanner.getAnnotatedRecommenders();
				final ArrayList<InternalR101Class> metrics = scanner.getAnnotatedMetrics();
				final ArrayList<InternalR101Class> datasplitter = scanner.getDataSplitter();
				final ArrayList<InternalR101Class> dataloader = scanner.getDataLoader();
				
				// The GUI update itself has to run in an additional separate Thread because of race conditions
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						FrmClassTree.this.recommender = recommender;
						FrmClassTree.this.metrics = metrics;
						FrmClassTree.this.datasplitter = datasplitter;
						FrmClassTree.this.dataloader = dataloader;
						updateJTree(classTree, recommender, metrics, datasplitter, dataloader);
						classesLoaded = true;					
						CommonRuntimeData.searchBox.setEnabled(true);

				        CommonRuntimeData.searchBox.requestFocusInWindow();
					}
				});
				
			}
		}).start();
		
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void newFileLoaded(InternalR101PropertiesFile newPropFile) {
		// Delete selection in classTree
		classTree.getSelectionModel().clearSelection();
	}
	
	private void updateJTree(JTree tree, ArrayList<InternalR101Class> recommender,
			ArrayList<InternalR101Class> metrics, ArrayList<InternalR101Class> datasplitter,
			ArrayList<InternalR101Class> dataloader)
	{
		updateJTree(tree, recommender, metrics, datasplitter, dataloader, "");
	}
	
	/**
	 * Updates a JTree's model according to the given ArrayLists	
	 */
	private void updateJTree(JTree tree, ArrayList<InternalR101Class> recommender, ArrayList<InternalR101Class> metrics,
			ArrayList<InternalR101Class> datasplitter, ArrayList<InternalR101Class> dataloader, String searchString)
	{		
		if (searchString.equals(defaultText))
		{
			searchString = "";
		}
		searchString = searchString.toLowerCase();
		
		DefaultMutableTreeNode category = null;
	    DefaultMutableTreeNode book = null;
		
		DefaultMutableTreeNode top =
		        new DefaultMutableTreeNode("Recommender101");
		
		// DataLoader
		// TO DO: Implement with search
		category = new DefaultMutableTreeNode("Data Loader");
		// Do not add category until it contains at least 1 item - that's what this boolean var is for
	    boolean added = false;
	    
	    for (InternalR101Class curr:dataloader)	
	    {
	    	if (!searchString.equals("") && !curr.getDisplayName().toLowerCase().contains(searchString)){
		    	// Don't show because it doesn't match the current search
	    		continue;
	    	}
	    	if (!added)
 	    	{
 	    		top.add(category);
 	    	}
	       	book = new DefaultMutableTreeNode(curr);
	       	category.add(book);
	    }  
	    	
		
		// DataSplitter
	    category = new DefaultMutableTreeNode("Data Splitter");
	    // Do not add category until it contains at least 1 item - that's what this boolean var is for
	    added = false;
	    
	    for (InternalR101Class curr:datasplitter)	
	    {
	    	if (!searchString.equals("") && !curr.getDisplayName().toLowerCase().contains(searchString)){
		    	// Don't show because it doesn't match the current search
	    		continue;
	    	}
	    	if (!added)
 	    	{
 	    		top.add(category);
 	    	}
	       	book = new DefaultMutableTreeNode(curr);
	       	category.add(book);
	    }  
		
		// Recommenders
		category = new DefaultMutableTreeNode("Recommenders");
	    
	    // Do not add category until it contains at least 1 item - that's what this boolean var is for
	    added = false;
	    
	    for (InternalR101Class curr:recommender)	
	    {
	    	if (!searchString.equals("") && !curr.getDisplayName().toLowerCase().contains(searchString)){
		    	// Don't show because it doesn't match the current search
	    		continue;
	    	}
	    	if (!added)
 	    	{
 	    		top.add(category);
 	    	}
	       	book = new DefaultMutableTreeNode(curr);
	       	category.add(book);
	    }    
	    
	    // Metrics
 		category = new DefaultMutableTreeNode("Metrics");
 	    
 		// Do not add category until it contains at least 1 item - that's what this boolean var is for
 		added = false;
 		 	    
 	    for (InternalR101Class curr:metrics)	
 	    {
 	    	if (!searchString.equals("") && !curr.getDisplayName().toLowerCase().contains(searchString)){
	    		// Don't show because it doesn't match the current search
	    		continue;
	    	}
 	    	if (!added)
 	    	{
 	    		top.add(category);
 	    	}
 	       	book = new DefaultMutableTreeNode(curr);
 	       	category.add(book);
 	    }   
	    	    
 	    DefaultTreeModel mod = new DefaultTreeModel(top);
	    tree.setModel(mod);
	    tree.setRootVisible(false);
	   
	   	    
	    // Expand all rows
	    for (int i = 0; i < tree.getRowCount(); i++) {
	         tree.expandRow(i);
	    }
	    
	    // Select the first Recommender101 class item
	    DefaultMutableTreeNode firstLeaf = ((DefaultMutableTreeNode)tree.getModel().getRoot()).getFirstLeaf();
	    tree.setSelectionPath(new TreePath(firstLeaf.getPath()));
	}
	
	/**
	 * Initializes the GUI components
	 */
	private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        toolBar = new javax.swing.JToolBar();
        toolBar.setRollover(true);
        toolBar.setFloatable(false);
             
        // Set up class tree
        DefaultMutableTreeNode top =
                new DefaultMutableTreeNode("Loading Java Classes...");   
        classTree = new javax.swing.JTree(top){

        	// Override toText method in order to display a class's prettier display name
			@Override
			public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row,
					boolean hasFocus) {
				Object newVal = ((DefaultMutableTreeNode)value).getUserObject();
				if (newVal instanceof InternalR101Class)
				{
					return ((InternalR101Class)newVal).getDisplayName();
				}
				return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
			}       	
        };
        classTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jScrollPane1.setViewportView(classTree);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
            .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE))
        );
        
        // Add a selection listener that updates the description if possible
        classTree.addTreeSelectionListener(new TreeSelectionListener() {			
			@Override
			public void valueChanged(TreeSelectionEvent e) {				
				if (classTree.isSelectionEmpty()) 
				{
					return;	
				}				
				
				TreePath path = classTree.getSelectionPath();				
				DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();	
				
				Object userObject = selectedNode.getUserObject();
				if (userObject instanceof InternalR101Class)
				{
					CommonRuntimeData.showDescription(((InternalR101Class)userObject).getDescription());
				}
				else
				{
					CommonRuntimeData.showDescription("");
				}
			}
		});

        // Set up tool bar        
        CommonRuntimeData.searchBox = new JTextField(defaultText);
        final JTextField searchBox = CommonRuntimeData.searchBox;
                
        searchBox.setColumns(15);
        searchBox.setEnabled(false);
        
        // Focus listener for the standard quicksearch text
        searchBox.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent arg0) {
				if (searchBox.getText().equals(""))
				{
					searchBox.setText(defaultText);
				}
			}
			
			@Override
			public void focusGained(FocusEvent arg0) {
				if (searchBox.getText().equals(defaultText))
				{
					searchBox.setText("");
				}
			}
		});
        
        
        // Document listener to invoke search
        searchBox.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				change();
			}
			
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				change();
			}
			
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				change();
			}
			
			private void change()
			{
				if (!classesLoaded)
				{
					return;
				}
				
				updateJTree(classTree, recommender, metrics, datasplitter, dataloader, searchBox.getText());
			}
		});
	

        toolBar.add(searchBox);        
        toolBar.addSeparator();
        
        JButton btnConfirm = new JButton("Add");
        btnConfirm.setToolTipText("Adds the selected class to current properties file.");
        
        btnConfirm.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				addSelectedClass();
			}
		});
        
        toolBar.add(btnConfirm);
        
        // Double click listener
        MouseListener ml = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int selRow = classTree.getRowForLocation(e.getX(), e.getY());
                //TreePath selPath = classTree.getPathForLocation(e.getX(), e.getY());
                if(selRow != -1) {
                    if(e.getClickCount() == 2) {
                        addSelectedClass();
                    }
                }
            }
        };
        classTree.addMouseListener(ml);
        
        // Enter key listener
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                "addSelection");
        getActionMap().put("addSelection",new AbstractAction() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				addSelectedClass();
			}
		});
        
        pack();
    }
	
	/**
	 * Adds the selected item to the current properties file
	 */
	private void addSelectedClass()
	{
		if (classTree.isSelectionEmpty()) 
		{
			return;	
		}				
		
		TreePath path = classTree.getSelectionPath();				
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();	
		
		Object userObject = selectedNode.getUserObject();
		if (userObject instanceof InternalR101Class)
		{
			InternalR101PropertiesFile p = CommonRuntimeData.getPropFile();
			FrmCurrentClasses frm = CommonRuntimeData.frmCurrentClasses;
			
			// Recommender, Metric, DataSplitter or Dataloader?
			if (recommender.contains(userObject))
			{
				p.getRecommenders().add(((InternalR101Class) userObject).getDeepCopy());
				frm.showTab(frm.btnShowRecommenders);
			}
			else if (metrics.contains(userObject))
			{
				CommonRuntimeData.getPropFile().getMetrics().add(((InternalR101Class) userObject).getDeepCopy());
				frm.showTab(frm.btnShowMetrics);
			}
			else if (dataloader.contains(userObject))
			{
				frm.showTab(frm.btnShowDataLoader);
				p.setDataloader(((InternalR101Class) userObject).getDeepCopy());
				//p.addOtherSetting(p.NAME_OF_DATALOADER_SETTING, ((InternalR101Class) userObject).getDeepCopy().getClassObject().getName(), new ArrayList<String>());
			}
			else if (datasplitter.contains(userObject))
			{
				frm.showTab(frm.btnShowDataSplitter);
				p.setDatasplitter(((InternalR101Class) userObject).getDeepCopy());
				//p.addOtherSetting(p.NAME_OF_DATASPLITTER_SETTING, ((InternalR101Class) userObject).getDeepCopy().getClassObject().getName(), new ArrayList<String>());				
			}
			CommonRuntimeData.notifyWindows(FrmClassTree.this);
			classTree.setSelectionPath(path);
		}			
	}
	
	// GUI components
	private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTree classTree;
    private javax.swing.JToolBar toolBar;
	
	
}
