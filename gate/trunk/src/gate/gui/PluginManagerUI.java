/*
 *  Copyright (c) 1998-2005, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  PluginManagerUI.java
 *
 *  Valentin Tablan, 21-Jul-2004
 *
 *  $Id$
 */

package gate.gui;

import java.awt.*;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import org.jdom.*;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import gate.Gate;
import gate.GateConstants;
import gate.event.CreoleListener;
import gate.swing.XJTable;
import gate.util.*;
import gate.util.Err;
import gate.util.GateRuntimeException;

/**
 * This is the user interface used for plugin management 
 */
public class PluginManagerUI extends JDialog implements GateConstants{
  
  public PluginManagerUI(Frame owner){
    super(owner);
    initLocalData();
    initGUI();
    initListeners();
  }
  
  
  protected void initLocalData(){
    loadNowByURL = new HashMap();
    loadAlwaysByURL = new HashMap();
  }
  
  protected void initGUI(){
    setTitle("Plugin Management Console");
    mainTableModel = new MainTableModel();
    mainTable = new XJTable();
//    mainTable.setSortable(false);
    mainTable.setModel(mainTableModel);
//    mainTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    mainTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    DeleteColumnCellRendererEditor rendererEditor = new DeleteColumnCellRendererEditor();
    mainTable.getColumnModel().getColumn(DELETE_COLUMN).
      setCellEditor(rendererEditor);
    mainTable.getColumnModel().getColumn(DELETE_COLUMN).
      setCellRenderer(rendererEditor);
    
    resourcesListModel = new ResourcesListModel();
    resourcesList = new JList(resourcesListModel);
    resourcesList.setCellRenderer(new ResourcesListCellRenderer());
    resourcesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    //enable tooltips
    ToolTipManager.sharedInstance().registerComponent(resourcesList);
    
    mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
    mainSplit.setResizeWeight(.75);
    JScrollPane scroller = new JScrollPane(mainTable);
    scroller.setVerticalScrollBarPolicy(
        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    scroller.setBorder(BorderFactory.createTitledBorder(
            scroller.getBorder(), 
            "Known CREOLE directories", 
            TitledBorder.LEFT, TitledBorder.ABOVE_TOP));
    mainSplit.setLeftComponent(scroller);
    
    scroller = new JScrollPane(resourcesList);
    scroller.setBorder(BorderFactory.createTitledBorder(
            scroller.getBorder(), 
            "CREOLE resources in directory",
            TitledBorder.LEFT, TitledBorder.ABOVE_TOP));
    mainSplit.setRightComponent(scroller);
    
    getContentPane().setLayout(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.insets = new Insets(2, 2, 2, 2);
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.gridy = 0;
    constraints.weightx = 1;
    constraints.weighty = 1;
    getContentPane().add(mainSplit, constraints);
    
    constraints.gridy = 1;
    constraints.weighty = 0;
    Box hBox = Box.createHorizontalBox();
    hBox.add(new JLabel("You can also "));
    hBox.add(new JButton(new AddCreoleRepositoryAction()));
    hBox.add(Box.createHorizontalGlue());
    getContentPane().add(hBox, constraints);
    
    constraints.gridy = 2;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.fill = GridBagConstraints.NONE;
    hBox = Box.createHorizontalBox();
    hBox.add(new JButton(new OkAction()));
    hBox.add(Box.createHorizontalStrut(20));
    hBox.add(new JButton(new CancelAction()));
    getContentPane().add(hBox, constraints);
  }
  
  protected void initListeners(){
    mainTable.getSelectionModel().addListSelectionListener(
      new ListSelectionListener(){
     public void valueChanged(ListSelectionEvent e){
       resourcesListModel.dataChanged();
     }
    });
    mainSplit.addComponentListener(new ComponentAdapter(){
      public void componentShown(ComponentEvent e){
        //try to honour left component's preferred size 
        mainSplit.setDividerLocation(-100);
      }
    });
  }
  
  protected Boolean getLoadNow(URL url){
    Boolean res = (Boolean)loadNowByURL.get(url);
    if(res == null){
      res = new Boolean(Gate.getCreoleRegister().getDirectories().contains(url));
      loadNowByURL.put(url, res);
    }
    return res;
  }
  
  protected Boolean getLoadAlways(URL url){
    Boolean res = (Boolean)loadAlwaysByURL.get(url);
    if(res == null){
      res = new Boolean(Gate.getAutoloadPlugins().contains(url));
      loadAlwaysByURL.put(url, res);
    }
    return res;
  }
  
  protected class MainTableModel extends AbstractTableModel{
    public MainTableModel(){
      localIcon = MainFrame.getIcon("open-file");
      remoteIcon = MainFrame.getIcon("internet");
      invalidIcon = MainFrame.getIcon("param");
    }
    public int getRowCount(){
      return Gate.getKnownPlugins().size();
    }
    
    public int getColumnCount(){
      return 6;
    }
    
    public String getColumnName(int column){
      switch (column){
        case NAME_COLUMN: return "Name";
        case ICON_COLUMN: return "";
        case URL_COLUMN: return "URL";
        case LOAD_NOW_COLUMN: return "Load now";
        case LOAD_ALWAYS_COLUMN: return "Load always";
        case DELETE_COLUMN: return "Delete";
        default: return "?";
      }
    }
    
    public Class getColumnClass(int columnIndex){
      switch (columnIndex){
        case NAME_COLUMN: return String.class;
        case ICON_COLUMN: return Icon.class;
        case URL_COLUMN: return String.class;
        case LOAD_NOW_COLUMN: return Boolean.class;
        case LOAD_ALWAYS_COLUMN: return Boolean.class;
        case DELETE_COLUMN: return Object.class;
        default: return Object.class;
      }
    }
    
    public Object getValueAt(int row, int column){
      Gate.DirectoryInfo dInfo = Gate.getDirectoryInfo(
              (URL)Gate.getKnownPlugins().get(row));
      switch (column){
        case NAME_COLUMN: return new File(dInfo.getUrl().getFile()).getName();
        case ICON_COLUMN: return
          dInfo.isValid() ? (
            dInfo.getUrl().getProtocol().equalsIgnoreCase("file") ? 
            localIcon : remoteIcon) :
          invalidIcon;
        case URL_COLUMN: return dInfo.getUrl().toString();
        case LOAD_NOW_COLUMN: return  getLoadNow(dInfo.getUrl());
        case LOAD_ALWAYS_COLUMN: return getLoadAlways(dInfo.getUrl());
        case DELETE_COLUMN: return null;
        default: return null;
      }
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex){
      return columnIndex == LOAD_NOW_COLUMN || 
        columnIndex == LOAD_ALWAYS_COLUMN ||
        columnIndex == DELETE_COLUMN;
    }
    
    public void setValueAt(Object aValue, int rowIndex, int columnIndex){
      Boolean valueBoolean = (Boolean)aValue;
      Gate.DirectoryInfo dInfo = Gate.getDirectoryInfo(
              (URL)Gate.getKnownPlugins().get(rowIndex));
      switch(columnIndex){
        case LOAD_NOW_COLUMN: 
          loadNowByURL.put(dInfo.getUrl(), valueBoolean);
          break;
        case LOAD_ALWAYS_COLUMN:
          loadAlwaysByURL.put(dInfo.getUrl(), valueBoolean);
          break;
      }
    }
    
    protected Icon localIcon;
    protected Icon remoteIcon;
    protected Icon invalidIcon;
  }
  
  protected class ResourcesListModel extends AbstractListModel{
    public Object getElementAt(int index){
      int row = mainTable.getSelectedRow();
      if(row == -1) return null;
      row = mainTable.rowViewToModel(row);
      Gate.DirectoryInfo dInfo = Gate.getDirectoryInfo(
              (URL)Gate.getKnownPlugins().get(row));
      return (Gate.ResourceInfo)dInfo.getResourceInfoList().get(index);
    }
    
    public int getSize(){
      
      int row = mainTable.getSelectedRow();
      if(row == -1) return 0;
      row = mainTable.rowViewToModel(row);
      Gate.DirectoryInfo dInfo = Gate.getDirectoryInfo(
              (URL)Gate.getKnownPlugins().get(row));
      return dInfo.getResourceInfoList().size();
    }
    
    public void dataChanged(){
//      fireIntervalRemoved(this, 0, getSize() - 1);
//      fireIntervalAdded(this, 0, getSize() - 1);
      fireContentsChanged(this, 0, getSize() - 1);
    }
  }
  
  /**
   * This class acts both as cell renderer  and editor for all the cells in the 
   * delete column.
   */
  protected class DeleteColumnCellRendererEditor extends AbstractCellEditor 
    implements TableCellRenderer, TableCellEditor{
    
    public DeleteColumnCellRendererEditor(){
      label = new JLabel();
      rendererDeleteButton = new JButton(MainFrame.getIcon("delete"));
      rendererDeleteButton.setMaximumSize(rendererDeleteButton.getPreferredSize());
      rendererDeleteButton.setMargin(new Insets(2, 5, 2, 5));
      rendererBox = new JPanel();
      rendererBox.setLayout(new GridBagLayout());
      rendererBox.setOpaque(false);
      GridBagConstraints constraints = new GridBagConstraints();
      constraints.fill = GridBagConstraints.NONE;
      constraints.gridy = 0;
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.weightx = 1;
      rendererBox.add(Box.createGlue(), constraints);
      constraints.weightx = 0;
      rendererBox.add(rendererDeleteButton, constraints);
      constraints.weightx = 1;
      rendererBox.add(Box.createGlue(), constraints);
      
      editorDeleteButton = new JButton(MainFrame.getIcon("delete"));
      editorDeleteButton.setMargin(new Insets(2, 5, 2, 5));
      editorDeleteButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent evt){
          int row = mainTable.getEditingRow();
          // tell Swing that we aren't really editing this cell, otherwise an
          // exception occurs when Swing tries to stop editing a cell that has
          // been deleted.
          TableCellEditor currentEditor = mainTable.getCellEditor();
          if(currentEditor != null) {
            currentEditor.cancelCellEditing();
          }
          row = mainTable.rowViewToModel(row);
          URL toDelete = (URL)Gate.getKnownPlugins().get(row);
          Gate.removeKnownPlugin(toDelete);
          loadAlwaysByURL.remove(toDelete);
          loadNowByURL.remove(toDelete);
          mainTableModel.fireTableDataChanged();
          resourcesListModel.dataChanged();
        }
      });
    editorDeleteButton.setMaximumSize(editorDeleteButton.getPreferredSize());
    editorBox = new JPanel();
    editorBox.setLayout(new GridBagLayout());
    editorBox.setOpaque(false);
    constraints.weightx = 1;
    editorBox.add(Box.createGlue(), constraints);
    constraints.weightx = 0;
    editorBox.add(editorDeleteButton, constraints);
    constraints.weightx = 1;
    editorBox.add(Box.createGlue(), constraints);
    }
    
    public Component getTableCellRendererComponent(JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column){
//      editorDeleteButton.setSelected(false);
      switch(column){
        case DELETE_COLUMN:
//          return rendererDeleteButton;
          return rendererBox;
        default: return null;
      }
    }
    
    public Component getTableCellEditorComponent(JTable table,
            Object value,
            boolean isSelected,
            int row,
            int column){
      switch(column){
        case DELETE_COLUMN:
          return editorBox;
        default: return null;
      }
    }
    
    public Object getCellEditorValue(){
      return null;
    }
    
    JButton editorDeleteButton;
    JButton rendererDeleteButton;
    JPanel rendererBox;
    JPanel editorBox;
    JLabel label;
  }
  
  protected class ResourcesListCellRenderer extends DefaultListCellRenderer{
    public Component getListCellRendererComponent(JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus){
      Gate.ResourceInfo rInfo = (Gate.ResourceInfo)value;
      //prepare the renderer
      super.getListCellRendererComponent(list, 
              rInfo.getResourceName(), index, isSelected, cellHasFocus);
      //add tooltip text
      setToolTipText(rInfo.getResourceComment());
      return this;
    }
  }
  
  
  protected class OkAction extends AbstractAction {
    public OkAction(){
      super("OK");
    }
    public void actionPerformed(ActionEvent evt){
      setVisible(false);
      //update the data structures to reflect the user's choices
      Iterator pluginIter = loadNowByURL.keySet().iterator();
      while(pluginIter.hasNext()){
        URL aPluginURL = (URL)pluginIter.next();
        boolean load = ((Boolean)loadNowByURL.get(aPluginURL)).booleanValue();
        boolean loaded = Gate.getCreoleRegister().
            getDirectories().contains(aPluginURL); 
        if(load && !loaded){
          //load the directory
          try{
            Gate.getCreoleRegister().registerDirectories(aPluginURL);
          }catch(GateException ge){
            throw new GateRuntimeException(ge);
          }
        }
        if(!load && loaded){
          //remove the directory
          Gate.getCreoleRegister().removeDirectory(aPluginURL);
        }
      }
      
      
      pluginIter = loadAlwaysByURL.keySet().iterator();
      while(pluginIter.hasNext()){
        URL aPluginURL = (URL)pluginIter.next();
        boolean load = ((Boolean)loadAlwaysByURL.get(aPluginURL)).booleanValue();
        boolean loaded = Gate.getAutoloadPlugins().contains(aPluginURL); 
        if(load && !loaded){
          //set autoload top true
          Gate.addAutoloadPlugin(aPluginURL);
        }
        if(!load && loaded){
          //set autoload to false
          Gate.removeAutoloadPlugin(aPluginURL);
        }
      }
      loadNowByURL.clear();
      loadAlwaysByURL.clear();
    }
  }
  
  /**
   * Overridden so we can populate the UI before showing.
   */
  public void setVisible(boolean visible){
    if(visible){
      loadNowByURL.clear();
      loadAlwaysByURL.clear();      
      mainTableModel.fireTableDataChanged();
    }
    super.setVisible(visible);
  }
  protected class CancelAction extends AbstractAction {
    public CancelAction(){
      super("Cancel");
    }
    
    public void actionPerformed(ActionEvent evt){
      setVisible(false);
      loadNowByURL.clear();
      loadAlwaysByURL.clear();      
    }
  }

  protected class AddCreoleRepositoryAction extends AbstractAction {
    public AddCreoleRepositoryAction(){
      super("Add a new CREOLE repository");
      putValue(SHORT_DESCRIPTION,"Load a new CREOLE repository");
    }

    public void actionPerformed(ActionEvent e) {
      Box messageBox = Box.createHorizontalBox();
      Box leftBox = Box.createVerticalBox();
      JTextField urlTextField = new JTextField(20);
      leftBox.add(new JLabel("Type an URL"));
      leftBox.add(urlTextField);
      messageBox.add(leftBox);

      messageBox.add(Box.createHorizontalStrut(10));
      messageBox.add(new JLabel("or"));
      messageBox.add(Box.createHorizontalStrut(10));

      class URLfromFileAction extends AbstractAction{
        URLfromFileAction(JTextField textField){
          super(null, MainFrame.getIcon("open-file"));
          putValue(SHORT_DESCRIPTION,"Click to select a directory");
          this.textField = textField;
        }

        public void actionPerformed(ActionEvent e){
          JFileChooser fileChooser = MainFrame.getFileChooser(); 
          fileChooser.setMultiSelectionEnabled(false);
          fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          fileChooser.setFileFilter(fileChooser.getAcceptAllFileFilter());
          int result = fileChooser.showOpenDialog(PluginManagerUI.this);
          if(result == JFileChooser.APPROVE_OPTION){
            try{
              textField.setText(fileChooser.getSelectedFile().
                                            toURL().toExternalForm());
            }catch(MalformedURLException mue){
              throw new GateRuntimeException(mue.toString());
            }
          }
        }
        JTextField textField;
      };//class URLfromFileAction extends AbstractAction

      Box rightBox = Box.createVerticalBox();
      rightBox.add(new JLabel("Select a directory"));
      JButton fileBtn = new JButton(new URLfromFileAction(urlTextField));
      rightBox.add(fileBtn);
      messageBox.add(rightBox);

      int res = JOptionPane.showOptionDialog(
                            PluginManagerUI.this, messageBox,
                            "Enter an URL to the directory containing the " +
                            "\"creole.xml\" file", JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE, null, null, null);
      if(res == JOptionPane.OK_OPTION){
        try{
          URL creoleURL = new URL(urlTextField.getText());
          Gate.addKnownPlugin(creoleURL);
          mainTableModel.fireTableDataChanged();
        }catch(Exception ex){
          JOptionPane.showMessageDialog(
              PluginManagerUI.this,
              "There was a problem with your selection:\n" +
              ex.toString() ,
              "GATE", JOptionPane.ERROR_MESSAGE);
          ex.printStackTrace(Err.getPrintWriter());
        }
      }
    }
  }//class LoadCreoleRepositoryAction extends AbstractAction

  protected XJTable mainTable;
  protected JSplitPane mainSplit;
  protected MainTableModel mainTableModel;
  protected ResourcesListModel resourcesListModel;
  protected JList resourcesList; 
  
  /**
   * Map from URL to Boolean. Stores temporary values for the loadNow options.
   */
  protected Map loadNowByURL;
  /**
   * Map from URL to Boolean. Stores temporary values for the loadAlways 
   * options.
   */
  protected Map loadAlwaysByURL;
 
  protected static final int ICON_COLUMN = 0;
  protected static final int NAME_COLUMN = 1;
  protected static final int URL_COLUMN = 2;
  protected static final int LOAD_NOW_COLUMN = 3;
  protected static final int LOAD_ALWAYS_COLUMN = 4;
  protected static final int DELETE_COLUMN = 5;
  
}
