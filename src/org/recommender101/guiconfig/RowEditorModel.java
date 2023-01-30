// By http://www.javaworld.com/javatips/jw-javatip102.html

package org.recommender101.guiconfig;

import java.util.Hashtable;

import javax.swing.table.TableCellEditor;

public class RowEditorModel {
	private Hashtable<Integer, TableCellEditor> data;

	public RowEditorModel() {
		data = new Hashtable<>();
	}

	public void addEditorForRow(int row, TableCellEditor e) {
		data.put(new Integer(row), e);
	}

	public void removeEditorForRow(int row) {
		data.remove(new Integer(row));
	}

	public TableCellEditor getEditor(int row) {
		return (TableCellEditor) data.get(new Integer(row));
	}
}