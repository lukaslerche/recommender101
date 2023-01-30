// This is a heavily modified version of
// http://stackoverflow.com/questions/3590897/jtable-design-to-synchronize-with-back-end-data-structure/3591230#3591230

package org.recommender101.guiconfig;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

/*
 * The editor button that brings up the dialog.
 */
//public class TablePopupEditor extends AbstractCellEditor
@SuppressWarnings("serial")
public class FileChooserCellEditor extends DefaultCellEditor implements TableCellEditor {
	private PopupDialog popup;
	private String currentText = "";
	private JButton editorComponent;

	public FileChooserCellEditor() {
		super(new JTextField());

		setClickCountToStart(1);

		// Use a JButton as the editor component

		editorComponent = new JButton();
		editorComponent.setBackground(Color.white);
		editorComponent.setBorderPainted(false);
		editorComponent.setContentAreaFilled(false);

		// Set up the dialog where we do the actual editing

		popup = new PopupDialog();
	}

	public Object getCellEditorValue() {
		return currentText;
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

		SwingUtilities.invokeLater(new Runnable() {
			@SuppressWarnings("deprecation")
			public void run() {
				popup.setText(currentText);
				popup.setSize(600, 150);
				popup.setLocationRelativeTo( editorComponent );
				//Point p = editorComponent.getLocationOnScreen();
				//popup.setLocation(p.x, p.y + editorComponent.getSize().height);
				popup.show();
				fireEditingStopped();
			}
		});

		if (value != null)
		{
			currentText = value.toString();
		}
		else
		{
			currentText = "";
		}
		editorComponent.setText(currentText);
		return editorComponent;
	}

	/*
	 * Simple dialog containing the actual editing component
	 */
	class PopupDialog extends JDialog implements ActionListener {
		private JTextField textArea;
		private JFileChooser chooser;
		
		public PopupDialog() {
			super((Frame) null, "Set file path", true);

			textArea = new JTextField(40);
			KeyStroke keyStroke = KeyStroke.getKeyStroke("ENTER");
			textArea.getInputMap().put(keyStroke, "none");
			JPanel scrollPane = new JPanel();

			JButton filechooser = new JButton("Browse...");			
			filechooser.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {		
					if (chooser == null)
					{
						chooser = new JFileChooser();
						
						// Set the file chooser dialog to start in the project directory
						String path = getClass().getClassLoader().getResource(".").getPath();
						chooser.setCurrentDirectory(new File(path));
					}
					
					int returnVal = chooser.showOpenDialog(popup);
				    if(returnVal == JFileChooser.APPROVE_OPTION) {
				      textArea.setText(chooser.getSelectedFile().getAbsolutePath().replace("\\", "\\\\"));
				    }
				}
			});
			
			scrollPane.add(textArea);
			scrollPane.add(filechooser);

			getContentPane().add(scrollPane);

			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(this);
			JButton ok = new JButton("Ok");
			ok.setPreferredSize(cancel.getPreferredSize());
			ok.addActionListener(this);

			JPanel buttons = new JPanel();
			buttons.add(ok);
			buttons.add(cancel);
			getContentPane().add(buttons, BorderLayout.SOUTH);
			pack();

			getRootPane().setDefaultButton(ok);
		}

		public void setText(String text) {
			textArea.setText(text);
		}

		/*
		 * Save the changed text before hiding the popup
		 */
		public void actionPerformed(ActionEvent e) {
			if ("Ok".equals(e.getActionCommand())) {
				currentText = textArea.getText();
			}

			textArea.requestFocusInWindow();
			setVisible(false);
		}
	}

}