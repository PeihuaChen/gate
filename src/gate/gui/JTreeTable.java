package gate.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.table.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;


import java.awt.Dimension;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.util.EventObject;


/**
 * A TreeTable component. That is a component that looks like a table apart
 * from the first column that contains a tree.
 */
public class JTreeTable extends XJTable {

  /**The tree used to render the first column*/
  protected CustomJTree tree;

  /**The model for this component*/
  protected TreeTableModel treeTableModel;

  /**
   * Constructs a JTreeTable from a model
   */
  public JTreeTable(TreeTableModel model) {
  	super();
    this.treeTableModel = model;

    // Create the tree. It will be used by the table renderer to generate
    //nice pictures
    tree = new CustomJTree();
    tree.setModel(treeTableModel);
    tree.setEditable(false);
    // Install a tableModel representing the visible rows in the tree.
    super.setModel(new TreeTableModelAdapter(treeTableModel, tree));

    // Force the JTable and JTree to share their row selection models.
    tree.setSelectionModel(new DefaultTreeSelectionModel() {
      //extend the constructor
      {
		    setSelectionModel(listSelectionModel);
	    }
	  });

    //Install the renderer
    //getColumnModel().getColumn(0).setCellRenderer(new TreeTableCellRenderer());
    //getColumnModel().getColumn(0).setCellEditor(new TreeTableCellEditor());

    //getColumn(getColumnName(0)).setCellRenderer(new TreeTableCellRenderer());
    setDefaultRenderer(TreeTableModel.class, new TreeTableCellRenderer());
    setDefaultEditor(TreeTableModel.class, new TreeTableCellEditor());
    //setDefaultEditor(TreeTableModel.class, new TreeTableCellEditor());
    //install the mouse listener that will forward the mouse events to the tree
    addMouseListener(new MouseHandler());

    setShowGrid(false);

    super.setSortable(false);
  }


  /**
   * Overrides the setSortable() method from {@link XJtable} so the table is NOT
   * sortable. In a tree-table component the ordering for the rows is given by
   * the structure of the tree and they cannot be reordered.
   */
  public void setSortable(boolean b){
    throw new UnsupportedOperationException(
          "A JTreeTable component cannot be sortable!\n" +
          "The rows order is defined by the tree structure.");
  }

  public JTree getTree(){
    return tree;
  }
/*
  public void setTreeCellRenderer(TreeCellRenderer renderer){
    tree.setCellRenderer(new SmartTreeCellRenderer(renderer));
  }
*/

  /**
   * The renderer used to display the table cells containing tree nodes.
   * Will use the {@link tree} to paint the nodes.
   */
  public class TreeTableCellRenderer extends DefaultTableCellRenderer{
/*
    public TreeTableCellRenderer() {

      icon = new ImageIcon();
      setText(null);
      setIcon(icon);
      setIconTextGap(0);
      setHorizontalTextPosition(RIGHT);
      setHorizontalAlignment(LEFT);
    }
*/
    public Component getTableCellRendererComponent(JTable table,
                     Object value,
                     boolean isSelected,
                     boolean hasFocus,
                     int row, int column) {

      tree.setBackground(table.getBackground());
      tree.setSize(tree.getPreferredSize());
      visibleRow = row;

      Rectangle rect = tree.getRowBounds(row);
      tree.setPreferredSize(null);
      this.setPreferredSize(new Dimension(tree.getPreferredSize().width,
                                          rect.height));
/*
      BufferedImage image = (BufferedImage)table.createImage(tree.getPreferredSize().width,
                                                             rect.height);
      if(image != null){
        Graphics graphics = image.getGraphics();
        if(graphics != null) {
          graphics.translate(0, -rect.y);
          tree.paint(graphics);
        }
        icon.setImage(image);
      }
      Component comp = tree.getCellRenderer().
                       getTreeCellRendererComponent(tree, value, isSelected,
                                                    false, false,row,hasFocus);
      if(comp != null && comp instanceof JComponent){
        setToolTipText(((JComponent)comp).getToolTipText());
      }else{
        setToolTipText(null);
      }
      */
      return this;
    }

    public void paint(Graphics g){
      Rectangle rect = tree.getRowBounds(visibleRow);
      g.translate(0, -rect.y);
      g.setClip(0, rect.y, bounds.width, rect.height);
      tree.paint(g);
    }

    public void setBounds(Rectangle bounds){
      this.bounds = bounds;
    }

    public void setBounds(int x, int y, int w, int h){
      this.bounds = new Rectangle(x, y, w, h);
    }


    int visibleRow;
    Rectangle bounds;
    //ImageIcon icon;
  }


  /**
   * The editor used to edit the nodes in the tree. It only forwards the
   * requests to the tree's editor.
   */
  class TreeTableCellEditor extends DefaultCellEditor
                            implements TableCellEditor {
    TreeTableCellEditor(){
      super(new JTextField());
      //placeHolder = new PlaceHolder();
      editor = tree.getCellEditor();
    }

    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int row,
                                                 int column){
      editor = tree.getCellEditor();
      editorComponent = editor.getTreeCellEditorComponent(
                    tree, tree.getPathForRow(row).getLastPathComponent(),
                    isSelected, tree.isExpanded(row),
                    tree.getModel().isLeaf(
                      tree.getPathForRow(row).getLastPathComponent()
                    ),
                    row);
      //return placeHolder;
      return editorComponent;
    }

    public Object getCellEditorValue(){
      return editor == null ? null : editor.getCellEditorValue();
    }
/*
    public boolean isCellEditable(EventObject anEvent){
      return editor == null ? false : editor.isCellEditable(anEvent);
    }

    public boolean shouldSelectCell(EventObject anEvent){
      return editor == null ? false : editor.shouldSelectCell(anEvent);
    }

    public boolean stopCellEditing(){
      return editor == null ? true : editor.stopCellEditing();
    }

    public void cancelCellEditing(){
      if(editor != null) editor.cancelCellEditing();
    }

    public void addCellEditorListener(CellEditorListener l){
      if(editor != null) editor.addCellEditorListener(l);
    }

    public void removeCellEditorListener(CellEditorListener l){
      if(editor != null) editor.removeCellEditorListener(l);
    }
*/
/*
    class PlaceHolder extends Component{
      public void setBounds(Rectangle rect){
        editorComponent.setBounds(rect);
      }

      public void setBounds(int x, int y, int w, int h){
        editorComponent.setBounds(x, y, w, h);
      }

      public void paint(Graphics g){
        editorComponent.paint(g);
      }
    }//class PlaceHolder extends Component
*/
    TreeCellEditor editor;
    Component editorComponent;
//    PlaceHolder placeHolder;
  }

  /**
   * Class used to convert the mouse events from the JTreeTable component space
   * into the JTree space. It is used to forward the mouse events to the tree
   * if they occured in the space used by the tree.
   */
  class MouseHandler extends MouseAdapter{
    public void mousePressed(MouseEvent e) {
      if(columnAtPoint(e.getPoint()) == 0){
        tree.dispatchEvent(convertEvent(e));
      }
    }

    public void mouseReleased(MouseEvent e) {
      if(columnAtPoint(e.getPoint()) == 0){
        tree.dispatchEvent(convertEvent(e));
      }
    }

    public void mouseClicked(MouseEvent e) {
      if(columnAtPoint(e.getPoint()) == 0){
        tree.dispatchEvent(convertEvent(e));
      }
    }


    public void mouseEntered(MouseEvent e) {
      if(columnAtPoint(e.getPoint()) == 0){
        tree.dispatchEvent(convertEvent(e));
      }
    }

    public void mouseExited(MouseEvent e) {
      if(columnAtPoint(e.getPoint()) == 0){
        tree.dispatchEvent(convertEvent(e));
      }
    }

    protected MouseEvent convertEvent(MouseEvent e){
      int column = 0;
      int row = rowAtPoint(e.getPoint());

      //move the event from table to tree coordinates
      Rectangle tableCellRect = getCellRect(row, column, false);
      Rectangle treeCellRect = tree.getRowBounds(row);
      int dx = 0;
      if(tableCellRect != null) dx = -tableCellRect.x;
      int dy = 0;
      if(tableCellRect !=null && treeCellRect != null)
        dy = treeCellRect.y -tableCellRect.y;
      e.translatePoint(dx, dy);


      return new MouseEvent(
        tree, e.getID(), e.getWhen(), e.getModifiers(),
        e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger()
      );
    }
  }

  /**
   * A wrapper that reads a TreeTableModel and behaves as a TableModel
   */
  class TreeTableModelAdapter extends AbstractTableModel{
    public TreeTableModelAdapter(TreeTableModel treeTableModel, JTree tree) {
//      this.tree = tree;
//      this.treeTableModel = treeTableModel;

      tree.addTreeExpansionListener(new TreeExpansionListener() {
        // Don't use fireTableRowsInserted() here;
        // the selection model would get  updated twice.
        public void treeExpanded(TreeExpansionEvent event) {
          fireTableDataChanged();
        }
        public void treeCollapsed(TreeExpansionEvent event) {
          fireTableDataChanged();
        }
      });
      tree.getModel().addTreeModelListener(new TreeModelListener(){
        public void treeNodesChanged(TreeModelEvent e){
          fireTableDataChanged();
        }
        public void treeNodesInserted(TreeModelEvent e){
          fireTableDataChanged();
        }
        public void treeNodesRemoved(TreeModelEvent e){
          fireTableDataChanged();
        }
        public void treeStructureChanged(TreeModelEvent e){
          fireTableDataChanged();
        }
      });
    }



    // Wrappers, implementing TableModel interface.
    public int getColumnCount() {
      return treeTableModel.getColumnCount();
    }

    public String getColumnName(int column) {
      return treeTableModel.getColumnName(column);
    }

    public Class getColumnClass(int column) {
      if(column == 0) return TreeTableModel.class;
      else return treeTableModel.getColumnClass(column);
    }

    public int getRowCount() {
      return tree.getRowCount();
    }

    protected Object nodeForRow(int row) {
      TreePath treePath = tree.getPathForRow(row);
      return treePath.getLastPathComponent();
    }

    public Object getValueAt(int row, int column) {
      if(column == 0) return treeTableModel;
      else return treeTableModel.getValueAt(nodeForRow(row), column);
    }

    public boolean isCellEditable(int row, int column) {
      return treeTableModel.isCellEditable(nodeForRow(row), column);
    }

    public void setValueAt(Object value, int row, int column) {
      treeTableModel.setValueAt(value, nodeForRow(row), column);
    }
  }//class TreeTableModelAdapter extends AbstractTableModel

  /**
   * The JTree used for rendering the first column.
   */
  class CustomJTree extends JTree{
    public void setEditable(boolean editable){
      super.setEditable(false);
    }
  }

/*
  class SmartTreeCellRenderer implements TreeCellRenderer{

    SmartTreeCellRenderer(TreeCellRenderer renderer){
      originalRenderer = renderer;
    }

    public Component getTreeCellRendererComponent(JTree tree,
                                              Object value,
                                              boolean selected,
                                              boolean expanded,
                                              boolean leaf,
                                              int row,
                                              boolean hasFocus){
      Component comp = originalRenderer.getTreeCellRendererComponent(
                       tree, value, selected, expanded, leaf, row, hasFocus);
      if(comp instanceof JComponent &&
         comp.getPreferredSize().height < getRowHeight(row)){
        ((JComponent)comp).setPreferredSize(
            new Dimension(comp.getPreferredSize().width,
            getRowHeight(row))
        );
      }
      return comp;
    }

    public TreeCellRenderer getOriginalRenderer(){
      return originalRenderer;
    }

    TreeCellRenderer originalRenderer;
  }
*/
}//public class JTreeTable extends XJTable
