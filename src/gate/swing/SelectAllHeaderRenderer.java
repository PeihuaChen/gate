/*
 * SelectAllHeaderRenderer.java
 * 
 * Copyright (c) 2011, The University of Sheffield. See the file COPYRIGHT.txt
 * in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 * 
 * This file is part of GATE (see http://gate.ac.uk/), and is free software,
 * licenced under the GNU Library General Public License, Version 2, June 1991
 * (in the distribution as file licence.html, and also available at
 * http://gate.ac.uk/gate/licence.html).
 * 
 * Mark A. Greenwood, 7/12/2011
 */

package gate.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public class SelectAllHeaderRenderer extends DefaultTableCellRenderer {

  public enum Status {
    SELECTED, DESELECTED, INDETERMINATE
  }

  protected JCheckBox checkBox = new JCheckBox();

  protected JTable table;

  protected Icon icon = new Icon() {

    @Override
    public int getIconWidth() {
      return checkBox.getPreferredSize().width;
    }

    @Override
    public int getIconHeight() {
      return checkBox.getPreferredSize().height;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      SwingUtilities.paintComponent(g, checkBox, (Container)c, x, y,
              getIconWidth(), getIconHeight());
    }
  };

  protected int column;
  
  private volatile boolean updating = false;

  public void update() {
    
    if (updating) return;
    
    updating = true;
          
    TableColumn col = table.getColumnModel().getColumn(table.convertColumnIndexToView((column)));

    int selected = 0;
    TableModel m = table.getModel();
    for(int i = 0; i < m.getRowCount(); i++) {
      if(Boolean.TRUE.equals(m.getValueAt(i, column))) {
        selected++;
      }
    }

    if(selected == 0) {
      col.setHeaderValue(SelectAllHeaderRenderer.Status.DESELECTED);
    } else if(selected == table.getRowCount()) {
      col.setHeaderValue(SelectAllHeaderRenderer.Status.SELECTED);
    } else {
      col.setHeaderValue(SelectAllHeaderRenderer.Status.INDETERMINATE);
    }

    table.getTableHeader().repaint();
    
    updating = false;

  }
  
  public void clicked(MouseEvent e) {
    JTableHeader header = (JTableHeader)e.getSource();

    TableColumnModel columnModel = table.getColumnModel();
    int viewColumn = columnModel.getColumnIndexAtX(e.getX());
    int modelColumn = table.convertColumnIndexToModel(viewColumn);
    if(modelColumn == column) {
      if(e.getX() <= header.getHeaderRect(viewColumn).x + icon.getIconWidth()) {
        TableColumn column = columnModel.getColumn(viewColumn);
        Object v = column.getHeaderValue();
        boolean b = Status.DESELECTED.equals(v) ? true : false;
        TableModel m = table.getModel();
        for(int i = 0; i < m.getRowCount(); i++)
          m.setValueAt(b, i, modelColumn);
        column.setHeaderValue(b ? Status.SELECTED : Status.DESELECTED);
        header.repaint();
      }
    }
  }

  public SelectAllHeaderRenderer(final JTable table, final int column) {
    super();
    this.column = column;
    this.table = table;

    // force the text to be to the right of the icon
    this.setHorizontalTextPosition(TRAILING);

    setOpaque(true);

    table.getTableHeader().addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        clicked(e);
      }
    });

    table.getModel().addTableModelListener(new TableModelListener() {
      @Override
      public void tableChanged(TableModelEvent e) {
        update();
      }
    });

    update();
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object val,
          boolean isSelected, boolean hasFocus, int row, int col) {
    super.getTableCellRendererComponent(table, table.getModel()
            .getColumnName(column), isSelected, hasFocus, row, col);

    JTableHeader header = table.getTableHeader();
    if(header != null) {
      Color fgColor = null;
      Color bgColor = null;
      if(hasFocus) {
        fgColor = UIManager.getColor("TableHeader.focusCellForeground");
        bgColor = UIManager.getColor("TableHeader.focusCellBackground");
      }
      if(fgColor == null) {
        fgColor = header.getForeground();
      }
      if(bgColor == null) {
        bgColor = header.getBackground();
      }
      setForeground(fgColor);
      setBackground(bgColor);

      setFont(header.getFont());
    }

    if(val instanceof Status) {
      switch((Status)val){
        case SELECTED:
          checkBox.setSelected(true);
          checkBox.setEnabled(true);
          break;
        case DESELECTED:
          checkBox.setSelected(false);
          checkBox.setEnabled(true);
          break;
        case INDETERMINATE:
          checkBox.setSelected(true);
          checkBox.setEnabled(false);
          break;
      }
    } else {
      checkBox.setSelected(true);
      checkBox.setEnabled(false);
    }

    setIcon(icon);

    Border border = null;
    if(hasFocus) {
      border = UIManager.getBorder("TableHeader.focusCellBorder");
    }
    if(border == null) {
      border = UIManager.getBorder("TableHeader.cellBorder");
    }

    setBorder(border);

    return this;
  }
}