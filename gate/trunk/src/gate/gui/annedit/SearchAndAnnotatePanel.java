/*
 *  Copyright (c) 1998-2007, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  SearchAndAnnotatePanel.java
 *
 *  Valentin Tablan, Sep 10, 2007
 *  Thomas Heitz, Nov 21, 2007
 *
 *  $Id: SchemaAnnotationEditor.java 9221 2007-11-14 17:46:37Z valyt $
 */


package gate.gui.annedit;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import gate.*;
import gate.event.*;
import gate.gui.MainFrame;
import gate.util.*;

public class SearchAndAnnotatePanel extends JPanel {

  /**
   * The annotation editor that use this search and annotate panel.
   */
  private AnnotationEditor annotationEditor;

  /**
   * Window that contains the annotation editor.
   */
  private Window annotationEditorWindow;

  /**
   * Listener for updating the list of searched annotations.
   */
  protected AnnotationSetListener annotationSetListener;
  
  /**
   * The box used to host the search pane.
   */
  protected Box searchBox;
  
  /**
   * The pane containing the UI for search and anootate functionality.
   */
  protected JPanel searchPane;
  
  /**
   * Text field for searching
   */
  protected JTextField searchTextField;
  
  /**
   * Checkbox for enabling RegEx searching 
   */
  protected JCheckBox searchRegExpChk;
  
  /**
   * Checkbox for enabling case sensitive searching 
   */
  protected JCheckBox searchCaseSensChk;
  
  /**
   * Checkbox for showing the search UI.
   */
  protected JCheckBox searchEnabledCheck;

  /**
   * The box used to host the search pane.
   */
  protected Box optionsBox;
  
  /**
   * The pane containing the UI for search and anootate functionality.
   */
  protected JPanel optionsPane;
  
  /**
   * List used to restrict annotations to search for.
   */
  protected JComboBox searchedAnnotationComboBox;
  
  /**
   * Checkbox for showing the options UI.
   */
  protected JCheckBox optionsEnabledCheck;

  /**
   * Shared regex matcher used for search functionality. 
   */
  protected Matcher matcher;
  
  protected FindFirstAction findFirstAction;
  
  protected FindPreviousAction findPreviousAction;

  protected FindNextAction findNextAction;
  
  protected AnnotateMatchAction annotateMatchAction;
  
  protected AnnotateAllMatchesAction annotateAllMatchesAction;
  
  protected UndoAnnotateAllMatchesAction undoAnnotateAllMatchesAction;

  /**
   * Start and end index of the all the matches. 
   */
  protected LinkedList<Vector<Integer>> matchedIndexes;
  
  /**
   * List of annotations ID that have been created
   * by the AnnotateAllMatchesAction. 
   */
  protected LinkedList<Annotation> annotateAllAnnotationsID;
  
  protected SmallButton annotateAllMatchesSmallButton;
  

  
  public SearchAndAnnotatePanel() {
    initGui();
    initListeners();

    // initially searchBox is collapsed
    searchBox.remove(searchPane);

    // initially optionsBox is collapsed
    optionsBox.remove(optionsPane);

    // options box is hidden until the user show the search pane
    optionsBox.setVisible(false);
  }

  /**
   * Build the GUI with JPanel and Box.
   * _
   * V Search & Annotate (searchBox)
   *  _______________________   _              _
   * |V_Searched_Text________| |_| Case sens. |_| Regular exp.
   * 
   * |Previous| |First| |Next| |Annotate|
   * _
   * V Annotate options (optionsBox)
   *  ______________________
   * | Whole text          V| |Annotate & next|
   * | Any annotation       | |Annotate all|
   * | Annotation1          |
   * |_Annotation2__________|
   *
   */
  protected void initGui() {

    // FIXME: how to get the Background color of the Annotator window ?
    setBackground(UIManager.getLookAndFeelDefaults()
                      .getColor("ToolTip.background"));

    JPanel mainPane = new JPanel();
    mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
    mainPane.setBackground(getBackground());

    setLayout(new BorderLayout());
    add(mainPane, BorderLayout.CENTER);

    searchBox = Box.createHorizontalBox();
    searchBox.setBackground(getBackground());
    String aTitle = "Search & Annotate";
    JLabel aLabel = new JLabel(aTitle);
    searchBox.setMinimumSize(new Dimension(aLabel.getPreferredSize().width, 0));    
    searchBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    searchBox.setBorder(BorderFactory.createTitledBorder(aTitle));
      searchEnabledCheck = new JCheckBox("", MainFrame.getIcon("closed"), false);
      searchEnabledCheck.setSelectedIcon(MainFrame.getIcon("expanded"));
      searchEnabledCheck.setAlignmentX(JComponent.LEFT_ALIGNMENT);
      searchEnabledCheck.setAlignmentY(JComponent.TOP_ALIGNMENT);
      searchEnabledCheck.setBackground(getBackground());
    searchBox.add(searchEnabledCheck);
    searchBox.add(Box.createHorizontalGlue());

    mainPane.add(searchBox);

    searchPane = new JPanel();
    searchPane.setAlignmentX(Component.LEFT_ALIGNMENT);
    searchPane.setAlignmentY(Component.TOP_ALIGNMENT);
    searchPane.setLayout(new BoxLayout(searchPane, BoxLayout.Y_AXIS));
    searchPane.setBackground(getBackground());
      Box hBox = Box.createHorizontalBox();
        searchTextField = new JTextField(6);
        searchTextField.setToolTipText("Searched text.");
        //disallow vertical expansion
        searchTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 
            searchTextField.getPreferredSize().height));
      hBox.add(searchTextField);
        searchCaseSensChk = new JCheckBox("Case", true);
        searchCaseSensChk.setToolTipText("Case sensitive.");
        searchCaseSensChk.setBackground(getBackground());
      hBox.add(searchCaseSensChk);
      hBox.add(Box.createHorizontalStrut(5));
        searchRegExpChk = new JCheckBox("Regexp", false);
        searchRegExpChk.setToolTipText("Regular expression.");
        searchRegExpChk.setBackground(getBackground());
      hBox.add(searchRegExpChk);
      hBox.add(Box.createHorizontalGlue());
    searchPane.add(hBox);
      hBox = Box.createHorizontalBox();
        findFirstAction = new FindFirstAction();
      hBox.add(new SmallButton(findFirstAction));
      hBox.add(Box.createHorizontalStrut(5));
        findPreviousAction = new FindPreviousAction();
        findPreviousAction.setEnabled(false);
      hBox.add(new SmallButton(findPreviousAction));
      hBox.add(Box.createHorizontalStrut(5));
        findNextAction = new FindNextAction();
        findNextAction.setEnabled(false);
      hBox.add(new SmallButton(findNextAction));
      hBox.add(Box.createHorizontalStrut(5));
        annotateMatchAction = new AnnotateMatchAction();
        annotateMatchAction.setEnabled(false);
      hBox.add(new SmallButton(annotateMatchAction));
      hBox.add(Box.createHorizontalStrut(5));
    searchPane.add(hBox);
    searchPane.add(Box.createVerticalGlue());
    searchBox.add(searchPane);
    
    mainPane.add(searchPane);

    optionsBox = Box.createHorizontalBox();
    optionsBox.setBackground(getBackground());
    aTitle = "Annotate options";
    aLabel = new JLabel(aTitle);
    optionsBox.setMinimumSize(new Dimension(aLabel.getPreferredSize().width, 0));    
    optionsBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    optionsBox.setBorder(BorderFactory.createTitledBorder(aTitle));
      optionsEnabledCheck = new JCheckBox("", MainFrame.getIcon("closed"), false);
      optionsEnabledCheck.setSelectedIcon(MainFrame.getIcon("expanded"));
      optionsEnabledCheck.setAlignmentX(JComponent.LEFT_ALIGNMENT);
      optionsEnabledCheck.setAlignmentY(JComponent.TOP_ALIGNMENT);
      optionsEnabledCheck.setBackground(getBackground());
    optionsBox.add(optionsEnabledCheck);
    optionsBox.add(Box.createHorizontalGlue());

    mainPane.add(optionsBox);

    optionsPane = new JPanel();
    optionsPane.setAlignmentX(Component.LEFT_ALIGNMENT);
    optionsPane.setAlignmentY(Component.TOP_ALIGNMENT);
    optionsPane.setLayout(new BoxLayout(optionsPane, BoxLayout.Y_AXIS));
    optionsPane.setBackground(getBackground());
      hBox = Box.createHorizontalBox();
        searchedAnnotationComboBox = new JComboBox();
        searchedAnnotationComboBox.setToolTipText(
          "Type of annotation to search for.");
      hBox.add(searchedAnnotationComboBox);
      hBox.add(Box.createHorizontalGlue());
    optionsPane.add(hBox);
      hBox = Box.createHorizontalBox();
        SmallButton annotateMatchSmallButton =
          new SmallButton(findNextAction);
        annotateMatchSmallButton.addActionListener(annotateMatchAction);
        annotateMatchSmallButton.setText("Annotate & next");
        annotateMatchSmallButton.setToolTipText(
          "Annotate the selected annotation and go the next matched annotation.");
      hBox.add(annotateMatchSmallButton);
      hBox.add(Box.createHorizontalStrut(5));
        annotateAllMatchesAction = new AnnotateAllMatchesAction();
        undoAnnotateAllMatchesAction = new UndoAnnotateAllMatchesAction();
        annotateAllMatchesAction.setEnabled(false);
        annotateAllMatchesSmallButton =
          new SmallButton(annotateAllMatchesAction);
      hBox.add(annotateAllMatchesSmallButton);
      hBox.add(Box.createHorizontalGlue());
    optionsPane.add(hBox);
    optionsPane.add(Box.createVerticalGlue());
    optionsBox.add(optionsPane);

    mainPane.add(optionsPane);
    
    //FIXME: how to pack() these component ?
  }

  protected void initListeners() {

    searchEnabledCheck.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (searchEnabledCheck.isSelected()) {
          //add the search box if not already there
          if (!searchBox.isAncestorOf(searchPane)) {
            //if empty, initialise the search field to the text of the current
            //annotation
            String searchText = searchTextField.getText(); 
            if (searchText == null || searchText.trim().length() == 0) {
              if (getAnnotationEditor().getAnnotationCurrentlyEdited() != null
                && getOwner() != null) {
                String annText = getOwner().getDocument().getContent().
                    toString().substring(
                      getAnnotationEditor().getAnnotationCurrentlyEdited()
                        .getStartNode().getOffset().intValue(),
                      getAnnotationEditor().getAnnotationCurrentlyEdited()
                        .getEndNode().getOffset().intValue());
                searchTextField.setText(annText);
              }
            }
            searchBox.add(searchPane);
            optionsBox.setVisible(true);
            getAnnotationEditorWindow().pack();
          }
        } else {
          if(searchBox.isAncestorOf(searchPane)){
            searchBox.remove(searchPane);
            optionsBox.setVisible(false);
            getAnnotationEditorWindow().pack();
          }
        }
      }
    });

    optionsEnabledCheck.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (optionsEnabledCheck.isSelected()) {
          //add the options box if not already there
          if (!optionsBox.isAncestorOf(optionsPane)) {

            if (getOwner() != null && annotationSetListener == null) {
              // add a annotation listener only here because the owner is
              // null when initListeners() is executed
              annotationSetListener = new gate.event.AnnotationSetListener() {
                public void annotationAdded(gate.event.AnnotationSetEvent e) {
                  if (getAnnotationEditor().getAnnotationSetCurrentlyEdited() != null) {
                  for(Annotation ann :
                    getAnnotationEditor().getAnnotationSetCurrentlyEdited()) {
                    searchedAnnotationComboBox.addItem(ann);
                  }
                  }
                  if (getOwner().getDocument().getAnnotations() != null) {
                    searchedAnnotationComboBox.addItem("------------");
                  for (String ann :
                        getOwner().getDocument().getAnnotations().getAllTypes()) {
                    searchedAnnotationComboBox.addItem(ann);
                  }
                  }
                }
                public void annotationRemoved(gate.event.AnnotationSetEvent e) {
                  if (getAnnotationEditor().getAnnotationSetCurrentlyEdited() != null) {
                  for(Annotation ann :
                    getAnnotationEditor().getAnnotationSetCurrentlyEdited()) {
                    searchedAnnotationComboBox.addItem(ann);
                  }
                  }
                  if (getOwner().getDocument().getAnnotations() != null) {
                    searchedAnnotationComboBox.addItem("------------");
                  for (String ann :
                        getOwner().getDocument().getAnnotations().getAllTypes()) {
                    searchedAnnotationComboBox.addItem(ann);
                  }
                  }
                }
            };
            getOwner().getDocument().getAnnotations()
              .addAnnotationSetListener(annotationSetListener);
            }

            optionsBox.add(optionsPane);
            getAnnotationEditorWindow().pack();
          }
        } else {
          if(optionsBox.isAncestorOf(optionsPane)){
            optionsBox.remove(optionsPane);
            getAnnotationEditorWindow().pack();
          }
        }
      }
    });

    searchTextField.getDocument().addDocumentListener(new DocumentListener(){
      public void changedUpdate(DocumentEvent e) {
        enableActions(false);
      }
      public void insertUpdate(DocumentEvent e) {
        enableActions(false);
      }
      public void removeUpdate(DocumentEvent e) {
        enableActions(false);
      }
      
      private void enableActions(boolean state){
        findPreviousAction.setEnabled(state);
        findNextAction.setEnabled(state);
        annotateMatchAction.setEnabled(state);
        annotateAllMatchesAction.setEnabled(state);
      }
    });

  }
  
  protected class FindFirstAction extends AbstractAction{
    public FindFirstAction(){
      super("First");
      super.putValue(SHORT_DESCRIPTION, 
              "Finds the first occurrence.");
    }
    
    public void actionPerformed(ActionEvent evt){
      if(getOwner() == null) return;
      String patternText = searchTextField.getText();
      if(patternText == null) return;
        int flags = 0;
        if(!searchCaseSensChk.isSelected()) flags |= Pattern.CASE_INSENSITIVE;
        if(!searchRegExpChk.isSelected()) flags |= Pattern.LITERAL;
        Pattern pattern = null;
        try {
          pattern = Pattern.compile(patternText, flags);
          String text = getOwner().getDocument().getContent().toString();
          matcher = pattern.matcher(text);
          if(matcher.find()){
            findNextAction.setEnabled(true);
            annotateMatchAction.setEnabled(true);
            annotateAllMatchesAction.setEnabled(true);
            int start = matcher.start();
            int end = matcher.end();
            matchedIndexes = new LinkedList<Vector<Integer>>();
            Vector<Integer> v = new Vector<Integer>(2);
            v.add(new Integer(start));
            v.add(new Integer(end));
            matchedIndexes.add(v);
            //automatically pin the dialog
//            pinnedButton.setSelected(true);
            getOwner().getTextComponent().requestFocus();
            getOwner().getTextComponent().select(start, end);
          }else{
            findNextAction.setEnabled(false);
            annotateMatchAction.setEnabled(false);
          }
          findPreviousAction.setEnabled(false);
        }
        catch(PatternSyntaxException e) {
          e.printStackTrace();
        }
    }
  }
  
  protected class FindPreviousAction extends AbstractAction {
    public FindPreviousAction() {
      super("Previous");
      super.putValue(SHORT_DESCRIPTION, 
              "Finds the previous occurrence.");
    }

    public void actionPerformed(ActionEvent evt) {

      // the first time we invoke previous action we want to go two
      // previous matches back not just one
      matchedIndexes.removeLast();

      Vector<Integer> v;
      if (matchedIndexes.size() == 1) {
        // no more previous annotation
        findPreviousAction.setEnabled(false);
      }
      v = matchedIndexes.getLast();
      int start = (v.firstElement()).intValue();
      int end = (v.lastElement()).intValue();
      getOwner().getTextComponent().requestFocus();
      getOwner().getTextComponent().select(start, end);
//      placeDialog(start, end);
      // reset the matcher for the next FindNextAction
      matcher.find(start);
    }
  }

  protected class FindNextAction extends AbstractAction{
    public FindNextAction(){
      super("Next");
      super.putValue(SHORT_DESCRIPTION, 
              "Finds the next occurrence.");
    }

    public void actionPerformed(ActionEvent evt){
      if(matcher != null){
        if(matcher.find()){
          int start = matcher.start();
          int end = matcher.end();
          Vector<Integer> v = new Vector<Integer>(2);
          v.add(new Integer(start));
          v.add(new Integer(end));
          matchedIndexes.add(v);
          getOwner().getTextComponent().requestFocus();
          getOwner().getTextComponent().select(start, end);
//          getAnnotationEditor().placeDialog(start, end);
          findPreviousAction.setEnabled(true);
        }else{
          //no more matches possible
          findNextAction.setEnabled(false);
          annotateMatchAction.setEnabled(false);
        }
      }else{
        //matcher is not prepared
        new FindFirstAction().actionPerformed(evt);
      }
    }
  }
  
  protected class AnnotateMatchAction extends AbstractAction{
    public AnnotateMatchAction(){
      super("Annotate");
      super.putValue(SHORT_DESCRIPTION, 
              "Annotates the current match.");
    }
    
    public void actionPerformed(ActionEvent evt){
      if(matcher != null){
        int start = matcher.start();
        int end = matcher.end();
        FeatureMap features = Factory.newFeatureMap();
        if(getAnnotationEditor().getAnnotationCurrentlyEdited().getFeatures() != null) 
          features.putAll(getAnnotationEditor().getAnnotationCurrentlyEdited().getFeatures());
        try {
//          Integer id = getAnnotationEditor().getAnnotationSetCurrentlyEdited().add(new Long(start), new Long(end), 
//                  getAnnotationEditor().getAnnotationCurrentlyEdited().getType(), features);
//          Annotation newAnn =
//            getAnnotationEditor().getAnnotationSetCurrentlyEdited().get(id);
          getAnnotationEditor().getAnnotationSetCurrentlyEdited().add(
            new Long(start), new Long(end), getAnnotationEditor()
            .getAnnotationCurrentlyEdited().getType(), features);
          getOwner().getTextComponent().select(start, start);
          annotateAllMatchesAction.setEnabled(true);
        }
        catch(InvalidOffsetException e) {
          //the offsets here should always be valid.
          throw new LuckyException(e);
        }
      }
    }
  }
  
  protected class AnnotateAllMatchesAction extends AbstractAction{
    public AnnotateAllMatchesAction(){
      super("Ann. all");
      super.putValue(SHORT_DESCRIPTION, 
              "Annotates all the following matches.");
    }
    
    public void actionPerformed(ActionEvent evt){
      annotateAllAnnotationsID = new LinkedList<Annotation>();
      //first annotate the current match
      annotateCurrentMatch();
      //next annotate all other matches
      while(matcher.find()){
        annotateCurrentMatch();
      }
      annotateAllMatchesSmallButton
        .removeActionListener(annotateAllMatchesAction);
      annotateAllMatchesSmallButton
        .addActionListener(undoAnnotateAllMatchesAction);
      annotateAllMatchesSmallButton.setText("Undo");
    }

    private void annotateCurrentMatch(){
      if(matcher != null){
        int start = matcher.start();
        int end = matcher.end();
        FeatureMap features = Factory.newFeatureMap();
        features.put("safe.regex", "true");
        if(getAnnotationEditor().getAnnotationCurrentlyEdited().getFeatures() != null) 
          features.putAll(getAnnotationEditor().getAnnotationCurrentlyEdited().getFeatures());
        try {
          Integer id = getAnnotationEditor().getAnnotationSetCurrentlyEdited().add(new Long(start), new Long(end), 
                  getAnnotationEditor().getAnnotationCurrentlyEdited().getType(), features);
          Annotation newAnn = getAnnotationEditor().getAnnotationSetCurrentlyEdited().get(id);
          annotateAllAnnotationsID.add(newAnn);
          getOwner().getTextComponent().select(start, start);
          getAnnotationEditor().editAnnotation(newAnn,
            getAnnotationEditor().getAnnotationSetCurrentlyEdited());
        }
        catch(InvalidOffsetException e) {
          //the offsets here should always be valid.
          throw new LuckyException(e);
        }
      }
    }
  }
  
  protected class AnnotateAllAction extends AbstractAction{
    public AnnotateAllAction(){
      super("Annotate all");
      super.putValue(SHORT_DESCRIPTION, 
              "Annotate all occurrences of this text.");
    }
    
    public void actionPerformed(ActionEvent evt){
      if(getAnnotationEditor().getAnnotationCurrentlyEdited() != null){
        String docText = getOwner().getDocument().getContent().toString();
        String annText = docText.substring(
                getAnnotationEditor().getAnnotationCurrentlyEdited().getStartNode().getOffset().intValue(),
                getAnnotationEditor().getAnnotationCurrentlyEdited().getEndNode().getOffset().intValue());
        Pattern annPattern = Pattern.compile(annText, Pattern.LITERAL);
        Matcher matcher = annPattern.matcher(docText);
        while(matcher.find()){
          int start = matcher.start();
          int end = matcher.end();
          //if there isn't already an annotation of the right type at these
          //offsets, then create one.
          boolean alreadyThere = false;
          AnnotationSet oldAnnots = getAnnotationEditor().getAnnotationSetCurrentlyEdited().get(new Long(start)).
              get(getAnnotationEditor().getAnnotationCurrentlyEdited().getType());
          if(oldAnnots != null && oldAnnots.size() > 0){
            for(Annotation anOldAnn : oldAnnots){
              if(anOldAnn.getStartNode().getOffset().intValue() == start &&
                 anOldAnn.getEndNode().getOffset().intValue() == end &&
                 anOldAnn.getFeatures().subsumes(getAnnotationEditor().getAnnotationCurrentlyEdited().getFeatures())){
                alreadyThere = true;
                break;
              }
            }
          }
          if(!alreadyThere){
            //create the new annotation
            FeatureMap features = Factory.newFeatureMap();
            features.putAll(getAnnotationEditor().getAnnotationCurrentlyEdited().getFeatures());
            try {
              getAnnotationEditor().getAnnotationSetCurrentlyEdited().add(new Long(start), new Long(end), getAnnotationEditor().getAnnotationCurrentlyEdited().getType(), 
                      features);
            }catch(InvalidOffsetException e) {
              //this should not happen as the offsets are obtained from the 
              //text
              throw new LuckyException(e);
            }              
          }
        }
      }
    }
  }
  
  /**
   * Remove the annotations added by the last action that annotate all matches.
   */
  protected class UndoAnnotateAllMatchesAction extends AbstractAction{
    public UndoAnnotateAllMatchesAction(){
      super("Undo");
      super.putValue(SHORT_DESCRIPTION, 
      "Undo annotate all matches.");
    }
    
    public void actionPerformed(ActionEvent evt){

      Iterator<Annotation> it = annotateAllAnnotationsID.iterator();
      while (it.hasNext()) {
        getAnnotationEditor().getAnnotationSetCurrentlyEdited().remove(it.next());
      }

      //clear the dialog
      getAnnotationEditor().editAnnotation(null,
        getAnnotationEditor().getAnnotationSetCurrentlyEdited());

//      if(!pinnedButton.isSelected()){
//        //if not pinned, hide the dialog.
//        dialog.setVisible(false);
//      }

      annotateAllMatchesSmallButton
        .removeActionListener(undoAnnotateAllMatchesAction);
      annotateAllMatchesSmallButton
        .addActionListener(annotateAllMatchesAction);
      annotateAllMatchesSmallButton.setText("Ann. all");
    }
  }

  /**
   * A JButton with content are not filled and border not painted (in order to
   * save screen real estate)
   */  
  protected class SmallButton extends JButton{
    public SmallButton(Action a) {
      super(a);
//      setBorder(null);
      setMargin(new Insets(0, 2, 0, 2));
//      setBorderPainted(false);
//      setContentAreaFilled(false);
    }
  }

  /**
   * @return the owner
   */
  public AnnotationEditorOwner getOwner() {
    return getAnnotationEditor().getOwner();
  }

  /**
   * @return the annotation editor
   */
  public AnnotationEditor getAnnotationEditor() {
    return annotationEditor;
  }

  /**
   * @param annotationEditor annotation editor that use this search
   *  and annotate panel
   */
  public void setAnnotationEditor(AnnotationEditor annotationEditor) {
    this.annotationEditor = annotationEditor;
  }
  
  /**
   * @param window The annotation editor window.
   */
  public void setAnnotationEditorWindow(Window window) {
    annotationEditorWindow = window;
  }

  /**
   * @return The annotation editor window.
   */
  public Window getAnnotationEditorWindow() {
    return annotationEditorWindow;
  }

}
