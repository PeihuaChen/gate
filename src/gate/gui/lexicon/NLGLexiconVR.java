/*
 *  NLGLexiconVR.java
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
 */
package gate.gui.lexicon;

import gate.creole.*;
import gate.lexicon.*;
import gate.util.GateRuntimeException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.*;
import java.util.*;
import gate.util.*;

public class NLGLexiconVR extends AbstractVisualResource {
  public NLGLexiconVR() {
    initLocalData();
    initGuiComponents();
    initListeners();
  }

  protected void initLocalData(){
    this.addSenseAction = new AddSenseAction();
    this.lookupLemmaAction = new LookupLemmaAction();
  }

  protected void initGuiComponents(){
    sensesListModel = new DefaultListModel();
    sensesList = new JList(sensesListModel);
    synsetListModel = new DefaultListModel();
    synsetEntriesList = new JList(synsetListModel);
    mainBox = Box.createHorizontalBox();
    leftBox = Box.createVerticalBox();
    rightBox = Box.createVerticalBox();
    definitionTextField.setPreferredSize(new Dimension(69, 24));
    definitionTextField.setToolTipText("");
    definitionTextField.setText("");
    definitionTextLabel.setText("Definition");
    lemmaTextLabel.setText("Lemma");
    leftBox.setBackground(SystemColor.control);
    leftBox.setEnabled(true);
    this.setLayout(gridLayout1);
    this.setAlignmentX((float) 0.5);
    this.setDebugGraphicsOptions(0);
    lemmaTextField.setText("");
    SynsetTextLabel.setText("Synset Entries");
    synsetEntriesList.setSelectedIndex(-1);
    synsetEntriesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    POSTextLabel.setText("Part of Speech");
    sensesTextLabel.setText("Senses");
    addSenseButton = new JButton(addSenseAction);
    addSenseButton.setText("Add");

    lookupButton = new JButton(lookupLemmaAction);
    lookupButton.setText("Lookup");

    this.add(mainBox, null);
    mainBox.add(leftBox, null);
    leftBox.add(lemmaTextLabel, null);
    leftBox.add(lemmaTextField, null);
    leftBox.add(lookupButton);
    mainBox.add(rightBox, null);
    rightBox.add(SynsetTextLabel, null);
    rightBox.add(synsetScrollPane, null);
    synsetScrollPane.getViewport().add(synsetEntriesList, null);
    rightBox.add(definitionTextLabel, null);
    rightBox.add(definitionTextField, null);
    rightBox.add(POSTextLabel, null);
    rightBox.add(posString, null);
    leftBox.add(sensesTextLabel, null);
    leftBox.add(sensesScrollPane, null);
    sensesScrollPane.getViewport().add(sensesList, null);
    leftBox.add(addSenseButton, null);

    leftBox.add(Box.createVerticalGlue());
    leftBox.add(Box.createHorizontalGlue());
    mainBox.add(Box.createVerticalGlue());
  }

  protected void initListeners(){

    this.lemmaTextField.addFocusListener(new FocusAdapter() {
      public void focusGained(FocusEvent e) {

      }

      public void focusLost(FocusEvent e) {
      }
    });

  }

  /**
 * Called by the GUI when this viewer/editor has to initialise itself for a
 * specific object.
 * @param target the object (be it a {@link gate.Resource},
 * {@link gate.DataStore} or whatever) this viewer has to display
 */
  public void setTarget(Object target) {
    if(target == null) return;
    if(!(target instanceof gate.lexicon.MutableLexicalKnowledgeBase)){
      throw new GateRuntimeException(this.getClass().getName() +
                                     " can only be used to display " +
                                     MutableLexicalKnowledgeBase.class.getName() +
                                     "\n" + target.getClass().getName() +
                                     " is not a " +
                                     MutableLexicalKnowledgeBase.class.getName() + "!");
    }

    this.lexKB = (MutableLexicalKnowledgeBase)target;
  }

  public void cleanup() {
    super.cleanup();
    lexKB = null;
  }

  public static void main(String[] args) {

  JFrame frame = new JFrame();

  frame.setSize(250, 200);

  frame.setLocation(200, 300);
  frame.getContentPane().add(new NLGLexiconVR());
  frame.pack();

  frame.setVisible(true);

  }//main

  protected void updateLeftGUI(String lemma){
    if (lexKB == null) return;
    java.util.List senses = lexKB.lookupWord(lemma);

    sensesListModel.clear();
    synsetListModel.clear();
    definitionTextField.setText("");
    posString.setText("");
    if (senses == null || senses.isEmpty()) {
    } else {
      for (int i= 0; i < senses.size(); i++) {
        LexKBWordSense sense = (LexKBWordSense) senses.get(i);
        NLGLexiconVR.this.sensesListModel.addElement(lemma + "_" +
            sense.getPOS() + "_" + sense.getSenseNumber());
        if (senses.size() == 1)
          updateRightGUI(sense);
      }//for loop through senses
    }
  }

  protected void updateRightGUI(LexKBWordSense sense) {
    if (lexKB == null) return;
    LexKBSynset synset = sense.getSynset();
    this.definitionTextField.setText(synset.getDefinition());
    this.posString.setText(synset.getPOS().toString());
    this.posString.setEnabled(false);
    for (int i = 0; i < synset.getWordSenses().size(); i++) {
      LexKBWordSense senseI = synset.getWordSense(i);
      synsetListModel.addElement(senseI.getWord().getLemma() + "_"
                                 + senseI.getPOS() + "_" + senseI.getSenseNumber());
    }

  }//updateRightGUI

  /**
   * Adds an element to the list from the editing component located at the top
   * of this dialog.
   */
  protected class AddSenseAction extends AbstractAction{
    AddSenseAction(){
      super("AddSense");
      putValue(SHORT_DESCRIPTION, "Add a sense");
    }
    public void actionPerformed(ActionEvent e){
      if (NLGLexiconVR.this.lexKB == null)
        return;

      String lemma = NLGLexiconVR.this.lemmaTextField.getText();
      //need to check that we need to add a word and not just add a sense to a word
      //that's depending on whether or not the senses list is empty
      MutableWord newWord =
        NLGLexiconVR.this.lexKB.addWord(lemma);

      //to be replaced by the synset management dialogue!!!
      MutableLexKBSynset newSynset = NLGLexiconVR.this.lexKB.addSynset();
      newSynset.setDefinition("test");
      newSynset.setPOS("Noun");

      //now add the new sense for this word given the synset
      newWord.addSense(newSynset);

      NLGLexiconVR.this.updateLeftGUI(lemma);
    }//actionPerformed
  }

  /**
   * Adds an element to the list from the editing component located at the top
   * of this dialog.
   */
  protected class LookupLemmaAction extends AbstractAction{
    LookupLemmaAction(){
      super("LookupLemma");
      putValue(SHORT_DESCRIPTION, "Lookup a lemma");
    }
    public void actionPerformed(ActionEvent e){
      String lemma = lemmaTextField.getText();
      updateLeftGUI(lemma);
    }//actionPerformed
  }

  protected MutableLexicalKnowledgeBase lexKB;
  protected GridLayout gridLayout1 = new GridLayout();
  protected Box mainBox;
  protected Box leftBox;
  protected Box rightBox;
  protected JLabel lemmaTextLabel = new JLabel();
  protected JTextField lemmaTextField = new JTextField();
  protected JLabel SynsetTextLabel = new JLabel();
  protected JScrollPane synsetScrollPane = new JScrollPane();
  protected JLabel definitionTextLabel = new JLabel();
  protected JTextField definitionTextField = new JTextField();
  protected JList synsetEntriesList = new JList();
  protected DefaultListModel synsetListModel;
  protected JLabel POSTextLabel = new JLabel();
  protected JTextField posString = new JTextField();
  protected JLabel sensesTextLabel = new JLabel();
  protected JScrollPane sensesScrollPane = new JScrollPane();
  protected JList sensesList;
  protected DefaultListModel sensesListModel;
  protected JButton addSenseButton;
  /**
    * An action that adds a new sense to the lexicon
    */
   protected Action addSenseAction;

   protected JButton lookupButton;
   /**
     * An action that looks up a lemma in the lexicon
     */
    protected Action lookupLemmaAction;

}

