/*
 *  Copyright (c) 1998-2004, The University of Sheffield.
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import gate.Gate;
import gate.GateConstants;
import gate.swing.XJTable;
import gate.util.GateRuntimeException;

/**
 * This is the user interface used for plugin management 
 */
public class PluginManagerUI extends JPanel implements GateConstants{
  
  public PluginManagerUI(){
    initLocalData();
    initGUI();
    initListeners();
  }
  
  protected void initLocalData(){
    rows = new ArrayList();
    String pluginPath = Gate.getUserConfig().getString(KNOWN_PLUGIN_PATH_KEY);
    if(pluginPath == null || pluginPath.length() == 0){
      //value unset -> initialise it to the locally installed plugins.
      pluginPath = "";
      File pluginDir = new File(System.getProperty(GATE_HOME_SYSPROP_KEY), 
              "plugins");
      File[] files = pluginDir.listFiles();
      for(int i = 0; i < files.length; i++){
        if(files[i].isDirectory() && new File(files[i], "creole.xml").exists()){
          if(pluginPath.length() > 0) pluginPath += ";";
          try{
            pluginPath += files[i].toURL();
          }catch(MalformedURLException mue){
            throw new GateRuntimeException(mue);
          }
        }
      }
    }
    
    //get the list of autoloading plugins
    String loadPluginPath = Gate.getUserConfig().getString(LOAD_PLUGIN_PATH_KEY);
    List loadURLs = new ArrayList();
    StringTokenizer strTok = new StringTokenizer(loadPluginPath, ";", false);
    while(strTok.hasMoreTokens()){
      loadURLs.add(strTok.nextToken());
    }
    
    strTok = new StringTokenizer(pluginPath, ";", false);
    while(strTok.hasMoreTokens()){
      String aPluginString = strTok.nextToken();
      try{
        DirectoryHandler dHandler = new DirectoryHandler(new URL(aPluginString),
                loadURLs.indexOf(aPluginString) >= 0);
        rows.add(dHandler);
      }catch(MalformedURLException mue){
        //ignore wrong URLs
      }
    }
  }
  
  protected void initGUI(){
    mainTableModel = new MainTableModel();
    mainTable = new XJTable(mainTableModel);
    setLayout(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridy = 0;
    add(new JScrollPane(mainTable), constraints);
    
    
  }
  
  protected void initListeners(){
    
  }
  
  protected class MainTableModel extends AbstractTableModel{
    public int getRowCount(){
      return rows.size();
    }
    
    public int getColumnCount(){
      return 4;
    }
    
    public String getColumnName(int column){
      switch (column){
        case ICON_COLUMN: return "";
        case NAME_COLUMN: return "URL";
        case LOAD_COLUMN: return "Load";
        case DELETE_COLUMN: return "Delete";
        default: return null;
      }
    }
    
    public Class getColumnClass(int columnIndex){
      switch (columnIndex){
        case ICON_COLUMN: return Icon.class;
        case NAME_COLUMN: return String.class;
        case LOAD_COLUMN: return Boolean.class;
        case DELETE_COLUMN: return Object.class;
        default: return null;
      }
    }
    
    public Object getValueAt(int row, int column){
      Object rowValue = rows.get(row);
      if(rowValue instanceof DirectoryHandler){
        DirectoryHandler dHandler = (DirectoryHandler)rowValue;
        switch (column){
          case ICON_COLUMN: return dHandler.icon;
          case NAME_COLUMN: return dHandler.url.toString();
          case LOAD_COLUMN: return new Boolean(dHandler.load);
          case DELETE_COLUMN: return null;
          default: return null;
        }
      }else{
        String resName = (String)rowValue;
        switch (column){
          case ICON_COLUMN: return null;
          case NAME_COLUMN: return resName;
          case LOAD_COLUMN: return null;
          case DELETE_COLUMN: return null;
          default: return null;
        }
      }
    }
    
    protected static final int ICON_COLUMN = 0;
    protected static final int NAME_COLUMN = 1;
    protected static final int LOAD_COLUMN = 2;
    protected static final int DELETE_COLUMN = 3;
  }
  
  protected class DirectoryHandler{
    public DirectoryHandler(URL url, boolean load){
      this.url = url;
      this.load = load;
      icon = url.getProtocol().startsWith("file") ? 
        MainFrame.getIcon("loadFile.gif") :
        MainFrame.getIcon("internet.gif");
    }
    
    URL url;
    boolean load;
    Icon icon;
  }
  

  protected XJTable mainTable;
  protected MainTableModel mainTableModel;
  protected List rows;
}
