/*
 *  Copyright (c) 1998-2005, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 17/05/2002
 *
 *  $Id$
 *
 */
package gate.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

import javax.swing.*;

import gate.Gate;
import gate.creole.ir.IREngine;

/**
 * Provides a gui for creating a IR index on a corpus.
 */
public class CreateIndexGUI extends JPanel {

  public CreateIndexGUI() {
    initLocalData();
    initGUIComponents();
    initListeners();
  }

  protected void initLocalData(){
    featuresList = new ArrayList();
    engineByName = new TreeMap();
  }

  protected void initGUIComponents(){
    setLayout(new GridBagLayout());

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.anchor = GridBagConstraints.WEST;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.insets = new Insets(2, 5, 2, 5);

    //first line
    constraints.gridy = 0;
    constraints.gridwidth = 2;
    add(new JLabel("IR Engine type:"), constraints);
    constraints.gridwidth = 4;

    irEngineCombo = new JComboBox();
    add(irEngineCombo, constraints);

    //second line
    constraints.gridy = 1;
    constraints.gridwidth = 2;
    add(new JLabel("Index location:"), constraints);
    constraints.gridwidth = 4;
    indexLocationTextField = new JTextField(40);
    add(indexLocationTextField, constraints);
    constraints.gridwidth = 1;
    add(new JButton(new SelectDirAction()), constraints);

    //third line
    constraints.gridy =2;
    constraints.gridwidth = 2;
    add(new JLabel("Features to index:"), constraints);
    featuresListTextField = new JTextField(40);
    featuresListTextField.setEditable(false);
    constraints.gridwidth = 4;
    add(featuresListTextField, constraints);
    constraints.gridwidth = 1;
    add(new JButton(new EditFeatureListAction()), constraints);

    //fourth line
    constraints.gridy = 3;
    constraints.gridwidth = 4;
    useContentChk = new JCheckBox("Use document content", true);
    add(useContentChk, constraints);

    //populate engine names combo
    String oldIREngineName = (String)irEngineCombo.getSelectedItem();

    List irEngines = new ArrayList(Gate.getRegisteredIREngines());
    engineByName.clear();
    for(int i = 0; i < irEngines.size(); i++){
      String anIREngineClassName = (String)irEngines.get(i);
      try{
        Class aClass =
          Class.forName(anIREngineClassName, true, Gate.getClassLoader());
        IREngine engine = (IREngine)aClass.newInstance();
        engineByName.put(engine.getName(), engine);
      }catch(ClassNotFoundException cnfe){
      }catch(IllegalAccessException iae){
      }catch(InstantiationException ie){
      }
    }

    String[] names = new String[engineByName.size()];
    int i = 0;
    Iterator namesIter = engineByName.keySet().iterator();
    while(namesIter.hasNext()){
      names[i++] = (String)namesIter.next();
    }
    irEngineCombo.setModel(new DefaultComboBoxModel(names));
    if(oldIREngineName != null && engineByName.containsKey(oldIREngineName)){
      irEngineCombo.setSelectedItem(oldIREngineName);
    }else if(engineByName.size() > 0) irEngineCombo.setSelectedIndex(0);
  }

  protected void initListeners(){
  }


  protected class SelectDirAction extends AbstractAction{
    public SelectDirAction(){
      super(null, MainFrame.getIcon("loadFile.gif"));
      putValue(SHORT_DESCRIPTION, "Click to open a file chooser!");
    }

    public void actionPerformed(ActionEvent e){
      JFileChooser fileChooser = MainFrame.getFileChooser();
      fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      fileChooser.setDialogTitle("Select a directory for the index files");
      int res = fileChooser.showOpenDialog(CreateIndexGUI.this);
      if(res == JFileChooser.APPROVE_OPTION) indexLocationTextField.
                                            setText(fileChooser.
                                            getSelectedFile().toString());
    }
  }

  protected class EditFeatureListAction extends AbstractAction{
    public EditFeatureListAction(){
      super(null, MainFrame.getIcon("editList.gif"));
      putValue(SHORT_DESCRIPTION, "Click to edit list!");
    }

    public void actionPerformed(ActionEvent e){
      ListEditorDialog listEditor = new ListEditorDialog(CreateIndexGUI.this,
                                                         featuresList,
                                                         "java.lang.String");
      List result = listEditor.showDialog();
      if(result != null){
        featuresList.clear();
        featuresList.addAll(result);
        if(featuresList.size() > 0){
          String text = "[" + featuresList.get(0).toString();
          for(int j = 1; j < featuresList.size(); j++){
            text += ", " + featuresList.get(j).toString();
          }
          text += "]";
          featuresListTextField.setText(text);
        }else{
          featuresListTextField.setText("");
        }
      }
    }
  }

  public boolean getUseDocumentContent(){
    return useContentChk.isSelected();
  }

  public List getFeaturesList(){
    return featuresList;
  }

  public String getIndexLocation(){
    return indexLocationTextField.getText();
  }

  public IREngine getIREngine(){
    return (IREngine)engineByName.get(irEngineCombo.getSelectedItem());
  }

  /**
   * Combobox for selecting IR engine.
   */
  JComboBox irEngineCombo;

  /**
   * Text field for the location of the index.
   */
  JTextField indexLocationTextField;

  /**
   * Checkbox for content used.
   */
  JCheckBox useContentChk;

  /**
   * Text field for the list of features.
   */
  JTextField featuresListTextField;

  /**
   * The list of features.
   */
  List featuresList;

  /**
   * A map from IREngine name to IREngine class name.
   */
  SortedMap engineByName;

}
