// By http://www.javaworld.com/javatips/jw-javatip102.html
// (modified by timkraemer)

package org.recommender101.guiconfig;

import javax.swing.*;
import javax.swing.table.*;

import java.util.Vector;

@SuppressWarnings("serial")
public class JTableX extends JTable {
	protected RowEditorModel rm;

	public JTableX() {
		super();
		rm = null;
	}

	public JTableX(TableModel tm) {
		super(tm);
		rm = null;
	}

	public JTableX(TableModel tm, TableColumnModel cm) {
		super(tm, cm);
		rm = null;
	}

	public JTableX(TableModel tm, TableColumnModel cm, ListSelectionModel sm) {
		super(tm, cm, sm);
		rm = null;
	}

	public JTableX(int rows, int cols) {
		super(rows, cols);
		rm = null;
	}

	@SuppressWarnings("rawtypes") 
	public JTableX(final Vector rowData, final Vector columnNames) {
		super(rowData, columnNames);
		rm = null;
	}

	public JTableX(final Object[][] rowData, final Object[] colNames) {
		super(rowData, colNames);
		rm = null;
	}

	// new constructor
	public JTableX(TableModel tm, RowEditorModel rm) {
		super(tm, null, null);
		this.rm = rm;
	}

	public void setRowEditorModel(RowEditorModel rm) {
		this.rm = rm;
	}

	public RowEditorModel getRowEditorModel() {
		return rm;
	}

	public TableCellEditor getCellEditor(int row, int col) {

		// Following if added 04/2014
		if (row == this.getRowCount()-1)
		{
			if (col > 0)
			{
				return super.getCellEditor(row, col);
			}
			
			rm.getEditor(row);
		}
		
		if (col != 1 && row < this.getRowCount()-1)
		{
			return super.getCellEditor(row, col);
		}
		
		TableCellEditor tmpEditor = null;
		if (rm != null)
			tmpEditor = rm.getEditor(row);
		if (tmpEditor != null)
			return tmpEditor;
		return super.getCellEditor(row, col);
	}
}