/*
 *  Copyright (c) 1998-2008, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, 16 Jan 2008
 *
 *  $Id$
 */
package gate.swing;

import java.io.File;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

/**
 * This class is used to demonstrate the functionality of {@link JTreeTable}.
 */
public class TestJTreeTable {
  
  private JFrame mainFrame;
  
  private JTreeTable treeTable;
  
  private TreeTableModel treeTableModel;
  
  private class FileTTModel extends AbstractTreeTableModel{
    private File root;
    private String[] columnNames = {"NAME", "SIZE", "DATE"};
    private static final int NAME_COLUMN = 0;
    private static final int SIZE_COLUMN = 1;
    private static final int DATE_COLUMN = 2;
    
    public FileTTModel(File root){
      super(root);
      this.root = root;
    }
    
    @Override
    public Object getChild(Object parent, int index) {
      if(parent instanceof File){
        File parentFile = (File)parent;
        if(parentFile.isDirectory()){
          File[] children = parentFile.listFiles();
          if(children != null && children.length > index){
            return children[index];
          }else{
            return null;
          }
        }else{
          throw new RuntimeException("Not a directory!");
        }
      }else{
        throw new RuntimeException("Not a file!");
      }
    }

    /* (non-Javadoc)
     * @see gate.swing.AbstractTreeTableModel#getChildCount(java.lang.Object)
     */
    @Override
    public int getChildCount(Object parent) {
      if(parent instanceof File){
        File parentFile = (File)parent;
        if(parentFile.isDirectory()){
          File[] children = parentFile.listFiles();
          return children == null ? 0 : children.length;
        }else{
          return 0;
        }
      }else{
        throw new RuntimeException("Not a file!");
      }
    }

    @Override
    public Class getColumnClass(int column) {
      return String.class;
    }

    @Override
    public int getColumnCount() {
      return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
      return columnNames[column];
    }

    @Override
    public Object getValueAt(Object node, int column) {
      if(node instanceof File){
        File nodeFile = (File)node;
        switch(column) {
          case NAME_COLUMN: return nodeFile.getName();
          case SIZE_COLUMN: return nodeFile.length();
          case DATE_COLUMN: return new Date(nodeFile.lastModified()).toString();
          default: return "";
        }
      }else{
        throw new RuntimeException("Not a file!");
      }
    }

    @Override
    public boolean isCellEditable(Object node, int column) {
      return false;
    }
    
  }
  
  public TestJTreeTable(){
    mainFrame = new JFrame("JTreeTable");
    mainFrame.setSize(800, 600);
    mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    treeTableModel = new FileTTModel(new File("/"));
    treeTable = new JTreeTable(treeTableModel);
    mainFrame.getContentPane().add(new JScrollPane(treeTable));
  }
  
  public static void main(String[] args) {
    TestJTreeTable tester = new TestJTreeTable();
    tester.mainFrame.setVisible(true);
  }

}
