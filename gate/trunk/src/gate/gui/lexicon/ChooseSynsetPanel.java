/*
 *  ChooseSynsetPanel.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva, 20/02/2003
 *
 *  $Id$
 */

package gate.gui.lexicon;

import javax.swing.*;
import java.awt.*;
import gate.lexicon.*;
import java.awt.event.*;
import gate.util.GateRuntimeException;
import gate.gui.OkCancelDialog;
import java.util.Vector;
import javax.swing.event.*;

public class ChooseSynsetPanel extends JPanel {

  public ChooseSynsetPanel(LexicalKnowledgeBase theLex, boolean isEdit) {
    if (theLex == null)
      throw new GateRuntimeException("To view/edit synsets please provide a valid lexicon");
    isEditable = isEdit;
    lexKB = theLex;
    initLocalData();
    initGuiComponents();
    initListeners();
  }

  public ChooseSynsetPanel(LexicalKnowledgeBase theLex) {
    if (theLex == null)
      throw new GateRuntimeException("To view/edit synsets please provide a valid lexicon");
    isEditable = true;
    lexKB = theLex;
    initLocalData();
    initGuiComponents();
    initListeners();
  }

  public LexKBSynset getSelectedSynset() {
    return (LexKBSynset) synsetList.getSelectedValue();
  }

  public void addSynsetSelectionListener(ListSelectionListener l) {
    if (l != null)
      synsetList.addListSelectionListener(l);
  }

  public void removeSynsetSelectionListener(ListSelectionListener l) {
    if (l != null)
      synsetList.removeListSelectionListener(l);
  }

  protected void initLocalData(){
    this.addSynsetAction = new AddSynsetAction();
    this.removeSynsetAction = new RemoveSynsetAction();
  }

  protected void initGuiComponents(){
    SynsetTextLabel.setText("Synset Entries");
    synsetListModel = new DefaultListModel();
    synsetList = new JList(synsetListModel);
    synsetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    this.posComboBox = new JComboBox(lexKB.POS_TYPES);
    posComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                                    posComboBox.getPreferredSize().height));
    mainBox = Box.createHorizontalBox();
    leftBox = Box.createVerticalBox();
    rightBox = Box.createVerticalBox();

    definitionTextArea.setText("");
    definitionTextArea.setWrapStyleWord(true);
    definitionTextArea.setEditable(false);
    definitionTextLabel.setText("Definition");

    this.setLayout(gridLayout1);
    this.setAlignmentX((float) 0.5);
    this.setDebugGraphicsOptions(0);

    POSTextLabel.setText("Part of Speech");

    if (isEditable) {
      addSynsetButton = new JButton(addSynsetAction);
      addSynsetButton.setText("Add");

      removeSynsetButton = new JButton(removeSynsetAction);
      removeSynsetButton.setText("Remove");
    }


    this.add(mainBox, null);
    mainBox.add(leftBox, null);

    mainBox.add(rightBox, null);

    leftBox.add(POSTextLabel, null);
    leftBox.add(posComboBox, null);

    leftBox.add(SynsetTextLabel, null);
    leftBox.add(synsetScrollPane, null);
    synsetScrollPane.getViewport().add(synsetList, null);

    rightBox.add(definitionTextLabel, null);
    JScrollPane definitionScroller = new JScrollPane();
    definitionScroller.getViewport().add(definitionTextArea);
    definitionScroller.setPreferredSize(new Dimension(300, 150));
    definitionScroller.setMinimumSize(new Dimension(300, 150));
    rightBox.add(definitionScroller, null);

    if (isEditable) {
      Box buttonBox = Box.createHorizontalBox();
      buttonBox.add(addSynsetButton, null);
      buttonBox.add(Box.createHorizontalStrut(20));
      buttonBox.add(removeSynsetButton, null);
      rightBox.add(buttonBox);
    }

    mainBox.add(Box.createVerticalGlue());

    updateGUI(null);
  }

  protected void initListeners(){
    posComboBox.addItemListener(new ItemListener(){
      public void itemStateChanged(ItemEvent event){
        int state = event.getStateChange();
        Object item = event.getItem();
        if (state == ItemEvent.SELECTED) {
          updateGUI(null);
        }//new POS selected
      }
    });

  }

  protected void updateGUI(LexKBSynset theSynset) {
    synsetListModel.clear();
    definitionTextArea.setText("");

    if (posComboBox.getSelectedItem() != null) {
      java.util.Iterator iter = lexKB.getSynsets(posComboBox.getSelectedItem());
      int selectedIndex = 0;
      while (iter.hasNext()) {
        LexKBSynset nextSynset = (LexKBSynset) iter.next();
        synsetListModel.addElement(nextSynset);
        if (nextSynset.equals(theSynset))
          selectedIndex = synsetListModel.size() - 1;
      }//while
      if (synsetList.getModel().getSize() == 0)
        return;
      synsetList.setSelectedIndex(selectedIndex);
      LexKBSynset selectedSynset = (LexKBSynset) synsetList.getSelectedValue();
      if (selectedSynset != null)
        definitionTextArea.setText(selectedSynset.getDefinition());
    }
  }

  protected void showInputDialog(MutableLexKBSynset theSynset) {
    JPanel inputPanel = new JPanel();
    inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
    JLabel defLabel = new JLabel("Definition");
    JTextArea newDefField = new JTextArea();
    newDefField.setWrapStyleWord(true);
    JScrollPane newDefScroller = new JScrollPane(newDefField);
    newDefScroller.setPreferredSize(new Dimension(400, 150));
    newDefScroller.setMinimumSize(new Dimension(300, 150));

    JLabel defPOS = new JLabel("Part of speech");
    final JComboBox newPOSCombo = new JComboBox(lexKB.getPOSTypes());
    newPOSCombo.setEditable(true);
    newPOSCombo.getEditor().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Object editedItem = newPOSCombo.getEditor().getItem();
        if (! (lexKB instanceof MutableLexicalKnowledgeBase))
          return;
        newPOSCombo.addItem(editedItem);
        newPOSCombo.setSelectedItem(editedItem);
        ((MutableLexicalKnowledgeBase) lexKB).addPOSType(editedItem);
      }
    });
    inputPanel.add(defLabel);
    inputPanel.add(newDefScroller);
    inputPanel.add(defPOS);
    inputPanel.add(newPOSCombo);

    boolean okPressed =
        OkCancelDialog.showDialog(this, inputPanel,
                              "Please provide definition and POS of the new synset");

    if (! okPressed)
      return;

    theSynset.setDefinition(newDefField.getText());
    theSynset.setPOS(newPOSCombo.getSelectedItem());
    if (newPOSCombo.getModel().getSize() != posComboBox.getModel().getSize()) {
      posComboBox.removeAllItems();
      Object[] posTypes = lexKB.getPOSTypes();
      for (int i=0; i< posTypes.length; i++)
      posComboBox.addItem(posTypes[i]);
    }

  }


  /**
   * Adds a synset
   */
  protected class AddSynsetAction extends AbstractAction{
    AddSynsetAction(){
      super("AddSynset");
      putValue(SHORT_DESCRIPTION, "Add a synset to the lexicon");
    }
    public void actionPerformed(ActionEvent e){
      if (lexKB == null ||
          !(lexKB instanceof MutableLexicalKnowledgeBase))
        return;
      MutableLexicalKnowledgeBase theKB = (MutableLexicalKnowledgeBase) lexKB;
      MutableLexKBSynset newSynset = theKB.addSynset();

      showInputDialog(newSynset);
      if (newSynset.getPOS() != null) {
        posComboBox.setSelectedItem(newSynset.getPOS());
        updateGUI(newSynset);
      }
    }//actionPerformed
  }

  /**
   * Removes a synset
   */
  protected class RemoveSynsetAction extends AbstractAction{
    RemoveSynsetAction(){
      super("RemoveSynset");
      putValue(SHORT_DESCRIPTION, "Removes a synset from the lexicon");
    }
    public void actionPerformed(ActionEvent e){
      int result = JOptionPane.showConfirmDialog(ChooseSynsetPanel.this,
        "Deleting the synset will also delete all word senses it contains. Are you sure?",
        "Warning",
        JOptionPane.YES_NO_OPTION);
      if (result == JOptionPane.NO_OPTION)
        return;
      if (lexKB == null ||
          !(lexKB instanceof MutableLexicalKnowledgeBase))
        return;
      MutableLexicalKnowledgeBase theKB = (MutableLexicalKnowledgeBase) lexKB;
      MutableLexKBSynset synset = (MutableLexKBSynset) synsetList.getSelectedValue();
      theKB.removeSynset(synset);

      updateGUI(null);
    }//actionPerformed
  }

  protected LexicalKnowledgeBase lexKB;

  protected GridLayout gridLayout1 = new GridLayout();
  protected Box mainBox;
  protected Box leftBox;
  protected Box rightBox;

  protected JLabel definitionTextLabel = new JLabel();
  protected JTextArea definitionTextArea = new JTextArea();

  protected JLabel POSTextLabel = new JLabel();
  protected JComboBox posComboBox;

  protected JLabel SynsetTextLabel = new JLabel();
  protected JScrollPane synsetScrollPane = new JScrollPane();
  protected JList synsetList;
  protected DefaultListModel synsetListModel;
  protected JButton addSynsetButton;
  /**
    * An action that adds a new synset to the lexicon
    */
   protected Action addSynsetAction;

   protected JButton removeSynsetButton;
   /**
     * An action that removes a synset from the lexicon
     */
   protected Action removeSynsetAction;

   protected boolean isEditable  = true;

   public static void main(String[] args) {

   JFrame frame = new JFrame();

   frame.setSize(250, 200);

   frame.setLocation(200, 300);
   frame.getContentPane().add(new ChooseSynsetPanel(new NLGLexiconImpl(), false));
   frame.pack();

   frame.setVisible(true);

   }//main

}