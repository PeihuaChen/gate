/*
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *  Copyright (c) 2009, Ontotext AD.
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
import java.awt.event.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.Timer;
import java.util.List;
import java.text.Collator;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.table.*;
import gate.Gate;
import gate.GateConstants;
import gate.swing.XJTable;
import gate.swing.XJFileChooser;
import gate.util.*;

/**
 * This is the user interface used for plugins management.
 */
public class PluginManagerUI extends JDialog implements GateConstants{
  
  public PluginManagerUI(Frame owner){
    super(owner);
    initLocalData();
    initGUI();
    initListeners();
  }
  
  
  protected void initLocalData(){
    loadNowByURL = new HashMap<URL, Boolean>();
    loadAlwaysByURL = new HashMap<URL, Boolean>();
    visibleRows = new ArrayList<URL>(Gate.getKnownPlugins());
  }
  
  protected void initGUI(){
    setTitle("Plugin Management Console");
    JPanel leftPanel = new JPanel(new BorderLayout());

    JPanel leftTopPanel = new JPanel(new BorderLayout());
    JLabel titleLabel = new JLabel("Known CREOLE directories");
    titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 40));
    leftTopPanel.add(titleLabel, BorderLayout.WEST);
    JPanel leftTopCenterPanel = new JPanel(new BorderLayout());
    leftTopCenterPanel.add(new JLabel("Filter:"), BorderLayout.WEST);
    filterTextField = new JTextField();
    filterTextField.setToolTipText("Type some text to filter the table rows.");
    leftTopCenterPanel.add(filterTextField, BorderLayout.CENTER);
    JButton clearFilterButton = new JButton(
      new AbstractAction("", MainFrame.getIcon("exit.gif")) {
      { this.putValue(MNEMONIC_KEY, KeyEvent.VK_BACK_SPACE);
        this.putValue(SHORT_DESCRIPTION, "Clear text field"); }
      public void actionPerformed(ActionEvent e) {
        filterTextField.setText("");
        filterTextField.requestFocusInWindow();
      }
    });
    clearFilterButton.setBorder(BorderFactory.createEmptyBorder());
    clearFilterButton.setIconTextGap(0);
    leftTopCenterPanel.add(clearFilterButton, BorderLayout.EAST);
    leftTopPanel.add(leftTopCenterPanel, BorderLayout.CENTER);
    leftPanel.add(leftTopPanel, BorderLayout.NORTH);

    mainTableModel = new MainTableModel();
    mainTable = new XJTable();
    mainTable.setTabSkipUneditableCell(true);
    mainTable.setModel(mainTableModel);
    mainTable.setSortedColumn(NAME_COLUMN);
    Collator collator = Collator.getInstance(Locale.ENGLISH);
    collator.setStrength(Collator.TERTIARY);
    mainTable.setComparator(NAME_COLUMN, collator);
    mainTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    DeleteColumnCellRendererEditor rendererEditor =
      new DeleteColumnCellRendererEditor();
    mainTable.getColumnModel().getColumn(DELETE_COLUMN).
      setCellEditor(rendererEditor);
    mainTable.getColumnModel().getColumn(DELETE_COLUMN).
      setCellRenderer(rendererEditor);
    mainTable.getColumnModel().getColumn(ICON_COLUMN).
      setCellRenderer(new IconTableCellRenderer());

    resourcesListModel = new ResourcesListModel();
    resourcesList = new JList(resourcesListModel);
    resourcesList.setCellRenderer(new ResourcesListCellRenderer());
    resourcesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    //enable tooltips
    ToolTipManager.sharedInstance().registerComponent(resourcesList);
    
    mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
    mainSplit.setResizeWeight(0.70);
    mainSplit.setContinuousLayout(true);
    JScrollPane scroller = new JScrollPane(mainTable);
    leftPanel.add(scroller, BorderLayout.CENTER);
    mainSplit.setLeftComponent(leftPanel);
    
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
    hBox.add(new JButton(new AddCreoleRepositoryAction()));
    hBox.add(Box.createHorizontalGlue());
    getContentPane().add(hBox, constraints);
    
    constraints.gridy = 2;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.fill = GridBagConstraints.NONE;
    hBox = Box.createHorizontalBox();
    JButton okButton = new JButton(new OkAction());
    hBox.add(okButton);
    hBox.add(Box.createHorizontalStrut(5));
    hBox.add(new JButton(new CancelAction()));
    hBox.add(Box.createHorizontalStrut(5));
    hBox.add(new JButton(new HelpAction()));
    constraints.insets = new Insets(2, 2, 8, 2);
    getContentPane().add(hBox, constraints);
    getRootPane().setDefaultButton(okButton);
  }
  
  protected void initListeners(){
    mainTable.getSelectionModel().addListSelectionListener(
      new ListSelectionListener(){
     public void valueChanged(ListSelectionEvent e){
       if (!e.getValueIsAdjusting()) {
        resourcesListModel.dataChanged();
       }
     }
    });

    // when typing a character in the table, use it for filtering
    mainTable.addKeyListener(new KeyAdapter() {
      public void keyTyped(KeyEvent e) {
        if (e.getKeyChar() != KeyEvent.VK_TAB
         && e.getKeyChar() != KeyEvent.VK_SPACE
         && e.getKeyChar() != KeyEvent.VK_BACK_SPACE
         && e.getKeyChar() != KeyEvent.VK_DELETE) {
          filterTextField.requestFocusInWindow();
          filterTextField.setText(String.valueOf(e.getKeyChar()));
        }
      }
    });

    addComponentListener(new ComponentAdapter(){
      public void componentShown(ComponentEvent e){
        SwingUtilities.invokeLater(new Runnable() { public void run() {
          mainSplit.setDividerLocation(0.8);
        }});
      }
    });

    // show only the rows containing the text from filterTextField
    filterTextField.getDocument().addDocumentListener(new DocumentListener() {
      private Timer timer = new Timer("Plugin manager table rows filter", true);
      private TimerTask timerTask;
      public void changedUpdate(DocumentEvent e) { /* do nothing */ }
      public void insertUpdate(DocumentEvent e) { update(); }
      public void removeUpdate(DocumentEvent e) { update(); }
      private void update() {
        if (timerTask != null) { timerTask.cancel(); }
        Date timeToRun = new Date(System.currentTimeMillis() + 300);
        timerTask = new TimerTask() { public void run() {
          filterRows(filterTextField.getText());
        }};
        // add a delay
        timer.schedule(timerTask, timeToRun);
      }
    });

    // Up/Down key events in filterTextField are transferred to the table
    filterTextField.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP
         || e.getKeyCode() == KeyEvent.VK_DOWN
         || e.getKeyCode() == KeyEvent.VK_PAGE_UP
         || e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
          mainTable.dispatchEvent(e);
        }
      }
    });

    // disable Enter key in the table so this key will confirm the dialog
    InputMap inputMap = mainTable.getInputMap(
      JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    inputMap.put(enter, "none");

    // define keystrokes action bindings at the level of the main window
    inputMap = ((JComponent)this.getContentPane())
      .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    ActionMap actionMap = ((JComponent)this.getContentPane()).getActionMap();
    inputMap.put(KeyStroke.getKeyStroke("ENTER"), "Apply");
    actionMap.put("Apply", new OkAction());
    inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "Cancel");
    actionMap.put("Cancel", new CancelAction());
    inputMap.put(KeyStroke.getKeyStroke("F1"), "Help");
    actionMap.put("Help", new HelpAction());
  }

  private void filterRows(String rowFilter) {
    final String filter = rowFilter.trim().toLowerCase();
    final String previousURL = mainTable.getSelectedRow() == -1 ? "" :
      (String) mainTable.getValueAt(mainTable.getSelectedRow(),
        mainTable.convertColumnIndexToView(URL_COLUMN));
    ArrayList<URL> previousVisibleRows = new ArrayList<URL>(visibleRows);
    if (filter.length() < 2) {
      // one character or less, don't filter rows
      visibleRows = new ArrayList<URL>(Gate.getKnownPlugins());
    } else {
      // filter rows case insensitively on each plugin URL and its resources
      visibleRows.clear();
      for (int i = 0; i < Gate.getKnownPlugins().size(); i++) {
        Gate.DirectoryInfo dInfo = Gate.getDirectoryInfo(
          Gate.getKnownPlugins().get(i));
        String url = dInfo.getUrl().toString();
        String resources = "";
        for (int j = 0; j < dInfo.getResourceInfoList().size(); j++) {
          resources += dInfo.getResourceInfoList().get(j).getResourceName()
            + " ";
        }
        if (url.toLowerCase().contains(filter)
         || resources.toLowerCase().contains(filter)) {
          visibleRows.add(Gate.getKnownPlugins().get(i));
        }
      }
    }
    if (!previousVisibleRows.equals(visibleRows)) {
      mainTableModel.fireTableDataChanged();
    }
    if (mainTable.getRowCount() > 0) {
      SwingUtilities.invokeLater(new Runnable() { public void run() {
      mainTable.setRowSelectionInterval(0, 0);
      if (filter.length() < 2
       && previousURL != null
       && !previousURL.equals("")) {
        // reselect the last selected row based on its name and url values
        for (int row = 0; row < mainTable.getRowCount(); row++) {
          String url = (String) mainTable.getValueAt(
            row, mainTable.convertColumnIndexToView(URL_COLUMN));
          if (url.equals(previousURL)) {
            mainTable.setRowSelectionInterval(row, row);
            mainTable.scrollRectToVisible(
              mainTable.getCellRect(row, 0, true));
            break;
          }
        }
      }
      }});
    }
  }

  protected Boolean getLoadNow(URL url){
    Boolean res = loadNowByURL.get(url);
    if(res == null){
      res = Gate.getCreoleRegister().getDirectories().contains(url);
      loadNowByURL.put(url, res);
    }
    return res;
  }
  
  protected Boolean getLoadAlways(URL url){
    Boolean res = loadAlwaysByURL.get(url);
    if(res == null){
      res = Gate.getAutoloadPlugins().contains(url);
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
      return visibleRows.size();
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
      Gate.DirectoryInfo dInfo = Gate.getDirectoryInfo(visibleRows.get(row));
      if (dInfo == null) { return null; }
      switch (column){
        case NAME_COLUMN: return new File(dInfo.getUrl().getFile()).getName();
        case ICON_COLUMN: return
          dInfo.isValid() ? (
            dInfo.getUrl().getProtocol().equalsIgnoreCase("file") ? 
            localIcon : remoteIcon) :
          invalidIcon;
        case URL_COLUMN: return dInfo.getUrl().toString();
        case LOAD_NOW_COLUMN: return getLoadNow(dInfo.getUrl());
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
      Gate.DirectoryInfo dInfo =
        Gate.getDirectoryInfo(visibleRows.get(rowIndex));
      if (dInfo == null) { return; }
      switch(columnIndex){
        case LOAD_NOW_COLUMN: 
          loadNowByURL.put(dInfo.getUrl(), valueBoolean);
          // for some reason the focus is sometime lost after editing
          // however it is needed for Enter key to execute OkAction
          mainTable.requestFocusInWindow();
          break;
        case LOAD_ALWAYS_COLUMN:
          loadAlwaysByURL.put(dInfo.getUrl(), valueBoolean);
          mainTable.requestFocusInWindow();
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
      Gate.DirectoryInfo dInfo = Gate.getDirectoryInfo(visibleRows.get(row));
      return dInfo.getResourceInfoList().get(index);
    }
    
    public int getSize(){
      int row = mainTable.getSelectedRow();
      if(row == -1) return 0;
      row = mainTable.rowViewToModel(row);
      Gate.DirectoryInfo dInfo = Gate.getDirectoryInfo(visibleRows.get(row));
      if (dInfo == null) { return 0; }
      return dInfo.getResourceInfoList().size();
    }
    
    public void dataChanged(){
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
          int rowModel = mainTable.rowViewToModel(row);
          URL toDelete = visibleRows.get(rowModel);
          Gate.removeKnownPlugin(toDelete);
          loadAlwaysByURL.remove(toDelete);
          loadNowByURL.remove(toDelete);
          // redisplay the table with the current filter
          filterRows(filterTextField.getText());
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
      switch(column){
        case DELETE_COLUMN:
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
  
  protected class IconTableCellRenderer extends DefaultTableCellRenderer{
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      if(value instanceof Icon){
        super.getTableCellRendererComponent(table, "", 
                isSelected, hasFocus, row, column);
        setIcon((Icon)value);
        return this;
      }else{
        return super.getTableCellRendererComponent(table, value, 
                isSelected, hasFocus, row, column);
      }
    }
    
  }
  
  protected class ResourcesListCellRenderer extends DefaultListCellRenderer{
    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus){
      Gate.ResourceInfo rInfo = (Gate.ResourceInfo)value;
      //prepare the renderer
      String filter = filterTextField.getText().trim().toLowerCase();
      if (filter.length() > 1
      && rInfo.getResourceName().toLowerCase().contains(filter)) {
        isSelected = true; // select resource if matching table row filter
      }
      super.getListCellRendererComponent(list, rInfo.getResourceName(),
                                         index, isSelected, cellHasFocus);
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
        boolean load = loadNowByURL.get(aPluginURL);
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
        boolean load = loadAlwaysByURL.get(aPluginURL);
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
      if (mainTable.getRowCount() > 0) {
        // select the first row
        mainTable.setRowSelectionInterval(0, 0);
        mainTable.scrollRectToVisible(
          mainTable.getCellRect(0, 0, true));
      }
    } else {
      // clear the filter
      filterTextField.setText("");
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

  protected class HelpAction extends AbstractAction {
    public HelpAction() {
      super("Help");
    }
    public void actionPerformed(ActionEvent evt) {
      MainFrame.getInstance().showHelpFrame(
        "sec:howto:plugins", "gate.gui.PluginManagerUI");
    }
  }

  protected class AddCreoleRepositoryAction extends AbstractAction {
    public AddCreoleRepositoryAction(){
      super("Add a CREOLE repository",
        MainFrame.getIcon("crystal-clear-action-edit-add.png"));
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
          XJFileChooser fileChooser = MainFrame.getFileChooser();
          fileChooser.setMultiSelectionEnabled(false);
          fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          fileChooser.setFileFilter(fileChooser.getAcceptAllFileFilter());
          fileChooser.setResource("gate.CreoleRegister");
          int result = fileChooser.showOpenDialog(PluginManagerUI.this);
          if(result == JFileChooser.APPROVE_OPTION){
            try{
              textField.setText(fileChooser.getSelectedFile().
                                            toURI().toURL().toExternalForm());
            }catch(MalformedURLException mue){
              throw new GateRuntimeException(mue.toString());
            }
          }
        }
        JTextField textField;
      } //class URLfromFileAction extends AbstractAction

      Box rightBox = Box.createVerticalBox();
      rightBox.add(new JLabel("Select a directory"));
      JButton fileBtn = new JButton(new URLfromFileAction(urlTextField));
      rightBox.add(fileBtn);
      messageBox.add(rightBox);

      int res = JOptionPane.showOptionDialog(PluginManagerUI.this, messageBox,
        "Enter an URL to the directory containing the \"creole.xml\" file",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
      if(res == JOptionPane.OK_OPTION){
        try{
          final URL creoleURL = new URL(urlTextField.getText());
          Gate.addKnownPlugin(creoleURL);
          mainTable.clearSelection();
          // redisplay the table without filtering
          filterRows("");
          // clear the filter text field
          filterTextField.setText("");
          // select the new plugin row
          SwingUtilities.invokeLater(new Runnable() { public void run() {
            for (int row = 0; row < mainTable.getRowCount(); row++) {
              String url = (String) mainTable.getValueAt(
                row, mainTable.convertColumnIndexToView(URL_COLUMN));
              if (url.equals(creoleURL.toString())) {
                mainTable.setRowSelectionInterval(row, row);
                mainTable.scrollRectToVisible(
                  mainTable.getCellRect(row, 0, true));
                break;
              }
            }
          }});
          mainTable.requestFocusInWindow();
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
  /** Contains the URLs from Gate.getKnownPlugins() that satisfy the filter
   * filterTextField for the plugin URL and the plugin resources names */
  protected List<URL> visibleRows;
  protected JSplitPane mainSplit;
  protected MainTableModel mainTableModel;
  protected ResourcesListModel resourcesListModel;
  protected JList resourcesList; 
  protected JTextField filterTextField;

  /**
   * Map from URL to Boolean. Stores temporary values for the loadNow options.
   */
  protected Map<URL, Boolean> loadNowByURL;
  /**
   * Map from URL to Boolean. Stores temporary values for the loadAlways 
   * options.
   */
  protected Map<URL, Boolean> loadAlwaysByURL;
 
  protected static final int ICON_COLUMN = 0;
  protected static final int NAME_COLUMN = 1;
  protected static final int URL_COLUMN = 2;
  protected static final int LOAD_NOW_COLUMN = 3;
  protected static final int LOAD_ALWAYS_COLUMN = 4;
  protected static final int DELETE_COLUMN = 5;
}
