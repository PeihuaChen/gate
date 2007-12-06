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
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuListener;

import gate.*;
import gate.event.*;
import gate.gui.MainFrame;
import gate.util.*;

/**
 * Build a GUI for searching and annotating annotations in a text.
 * It needs to be called from a gate.gui.docview.AnnotationEditor.
 * 
 * <p>Here is an example how to add it to a JPanel panel.
 *
 * <pre>
 * SearchAndAnnotatePanel searchPanel =
 *           new SearchAndAnnotatePanel(panel.getBackground(), this, window);
 * panel.add(searchPanel);
 * </pre>
 */
public class SearchAndAnnotatePanel extends JPanel {

  private static final long serialVersionUID = 1L;

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
   * The pane containing the UI for search and annotate functionality.
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
   * The pane containing the UI for search and annotate functionality.
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
  
  protected SmallButton firstSmallButton;

  protected SmallButton annotateAllMatchesSmallButton;
  


  public SearchAndAnnotatePanel(Color color,
          AnnotationEditor annotationEditor, Window window) {

    this.annotationEditor = annotationEditor;
    annotationEditorWindow = window;

    initGui(color);

    // initially searchBox is collapsed
    searchBox.remove(searchPane);

    // initially optionsBox is collapsed
    optionsBox.remove(optionsPane);

    // options box is hidden until the user show the search pane
    optionsBox.setVisible(false);

    initListeners();
  }

  /**
   * Build the GUI with JPanels and Boxes.
   *
   * @param color Color of the background.
   * _
   * V Search & Annotate (searchBox)
   *  _______________________   _        _
   * |V_Searched_Expression__| |_| Case |_| Regexp
   * 
   *         |Previous| |First| |Next| |Annotate|
   * _
   * V Annotate options (optionsBox)
   *  ______________________
   * | Any annotation      V| |Ann. & next| |Ann. all|
   * | Annotation1          |
   * | Annotation2          |
   * |_Annotation3__________|
   *
   */
  protected void initGui(Color color) {

    JPanel mainPane = new JPanel();
    mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
    mainPane.setBackground(color);
    mainPane.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

    setLayout(new BorderLayout());
    add(mainPane, BorderLayout.CENTER);

    searchBox = Box.createVerticalBox();
    String aTitle = "Search & Annotate";
    JLabel aLabel = new JLabel(aTitle);
    searchBox.setMinimumSize(
      new Dimension(aLabel.getPreferredSize().width, 0));    
    searchBox.setAlignmentX(Component.LEFT_ALIGNMENT);
      searchEnabledCheck = new JCheckBox(aTitle, MainFrame.getIcon("closed"), false);
      searchEnabledCheck.setSelectedIcon(MainFrame.getIcon("expanded"));
      searchEnabledCheck.setAlignmentX(JComponent.LEFT_ALIGNMENT);
      searchEnabledCheck.setAlignmentY(JComponent.TOP_ALIGNMENT);
      searchEnabledCheck.setBackground(color);
    searchBox.add(searchEnabledCheck);
    searchBox.add(Box.createHorizontalGlue());

    searchPane = new JPanel();
    searchPane.setAlignmentX(Component.LEFT_ALIGNMENT);
    searchPane.setAlignmentY(Component.TOP_ALIGNMENT);
    searchPane.setLayout(new BoxLayout(searchPane, BoxLayout.Y_AXIS));
    searchPane.setBackground(color);
      Box hBox = Box.createHorizontalBox();
      hBox.add(Box.createHorizontalStrut(5));
        searchTextField = new JTextField(10);
        searchTextField.setToolTipText("Searched expression.");
        //disallow vertical expansion
        searchTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 
            searchTextField.getPreferredSize().height));
      hBox.add(searchTextField);
        searchCaseSensChk = new JCheckBox("Case", true);
        searchCaseSensChk.setToolTipText("Case sensitive search.");
        searchCaseSensChk.setBackground(color);
      hBox.add(searchCaseSensChk);
        searchRegExpChk = new JCheckBox("Regexp", false);
        searchRegExpChk.setToolTipText("Regular expression search.");
        searchRegExpChk.setBackground(color);
      hBox.add(searchRegExpChk);
      hBox.add(Box.createHorizontalGlue());
    searchPane.add(hBox);
      hBox = Box.createHorizontalBox();
      hBox.add(Box.createHorizontalStrut(5));
        findFirstAction = new FindFirstAction();
        firstSmallButton = new SmallButton(findFirstAction);
      hBox.add(firstSmallButton);
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

    searchBox.add(searchPane);
    mainPane.add(searchBox);

    optionsBox = Box.createVerticalBox();
    aTitle = "Annotate options";
    aLabel = new JLabel(aTitle);
    optionsBox.setMinimumSize(
      new Dimension(aLabel.getPreferredSize().width, 0));    
    optionsBox.setAlignmentX(Component.LEFT_ALIGNMENT);
      optionsEnabledCheck = new JCheckBox(aTitle, MainFrame.getIcon("closed"), false);
      optionsEnabledCheck.setSelectedIcon(MainFrame.getIcon("expanded"));
      optionsEnabledCheck.setAlignmentX(JComponent.LEFT_ALIGNMENT);
      optionsEnabledCheck.setAlignmentY(JComponent.TOP_ALIGNMENT);
      optionsEnabledCheck.setBackground(color);
    optionsBox.add(optionsEnabledCheck);
    optionsBox.add(Box.createHorizontalGlue());

    optionsPane = new JPanel();
    optionsPane.setAlignmentX(Component.LEFT_ALIGNMENT);
    optionsPane.setAlignmentY(Component.TOP_ALIGNMENT);
    optionsPane.setLayout(new BoxLayout(optionsPane, BoxLayout.Y_AXIS));
    optionsPane.setBackground(color);
      hBox = Box.createHorizontalBox();
      hBox.add(Box.createHorizontalStrut(5));
        searchedAnnotationComboBox = new JComboBox();
        searchedAnnotationComboBox.setToolTipText(
          "Restrict search on this annotation type.");
        searchedAnnotationComboBox.setMaximumRowCount(12);
      hBox.add(searchedAnnotationComboBox);
      hBox.add(Box.createHorizontalStrut(5));
        SmallButton annotateMatchSmallButton = new SmallButton(findNextAction);
        // second Action for the same button
        // and we pray for this one is executed first = ) 
        annotateMatchSmallButton.addActionListener(annotateMatchAction);
        annotateMatchSmallButton.setText("Ann. & next");
        annotateMatchSmallButton.setToolTipText(
          "Annotates the current match and finds the next occurrence.");
        annotateMatchSmallButton.setMnemonic(KeyEvent.VK_X);
      hBox.add(annotateMatchSmallButton);
      hBox.add(Box.createHorizontalStrut(5));
        annotateAllMatchesAction = new AnnotateAllMatchesAction();
        undoAnnotateAllMatchesAction = new UndoAnnotateAllMatchesAction();
        annotateAllMatchesSmallButton =
          new SmallButton(annotateAllMatchesAction);
        annotateAllMatchesAction.setEnabled(false);
        undoAnnotateAllMatchesAction.setEnabled(false);
      hBox.add(annotateAllMatchesSmallButton);
      hBox.add(Box.createHorizontalStrut(5));
    optionsPane.add(hBox);

    optionsBox.add(optionsPane);
    mainPane.add(optionsBox);
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
          if (!optionsBox.isAncestorOf(optionsPane)) {
            // add the options box if not already there
            optionsBox.add(optionsPane);
            updateSearchedAnnotationComboBox();
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

    this.addAncestorListener(new AncestorListener() {
      public void ancestorAdded(AncestorEvent event) {
        // do nothing
      }
      public void ancestorRemoved(AncestorEvent event) {
        // if the editor window is closed
        enableActions(false);
      }
      public void ancestorMoved(AncestorEvent event) {
        // do nothing
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
    });

    searchCaseSensChk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        enableActions(false);
      }
    });

    searchRegExpChk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        enableActions(false);
      }
    });

    searchedAnnotationComboBox.addPopupMenuListener(new PopupMenuListener() {
      public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {
        // do nothing
      }
      public void popupMenuWillBecomeInvisible(
              javax.swing.event.PopupMenuEvent e) {
        // do nothing
      }
      public void popupMenuWillBecomeVisible(
              javax.swing.event.PopupMenuEvent e) {
        updateSearchedAnnotationComboBox();
        searchedAnnotationComboBox.validate();
      }
    });
  }

  private void enableActions(boolean state){
    findPreviousAction.setEnabled(state);
    findNextAction.setEnabled(state);
    annotateMatchAction.setEnabled(state);
    annotateAllMatchesAction.setEnabled(state);
    if (annotateAllMatchesSmallButton.getAction()
            .equals(undoAnnotateAllMatchesAction)) {
      annotateAllMatchesSmallButton.setAction(annotateAllMatchesAction);
    }
  }

  private void updateSearchedAnnotationComboBox() {

    Object lastSelectedAnnotationName =
      searchedAnnotationComboBox.getSelectedItem();

    searchedAnnotationComboBox.removeAllItems();
    searchedAnnotationComboBox.addItem("Any type");
    searchedAnnotationComboBox.addItem("------------");

    TreeSet<String> treeSet;
    if (getAnnotationEditor().getAnnotationSetCurrentlyEdited() != null) {
      treeSet = new TreeSet<String>(getAnnotationEditor()
              .getAnnotationSetCurrentlyEdited().getAllTypes());
      // add all annotation names from the currently edited annotation set
      for(String annotationType : treeSet) {
        searchedAnnotationComboBox.addItem(annotationType);
      }
    }
    if (getOwner().getDocument().getAnnotationSetNames().size() > 0) {
      // add all annotation names from all annotations set
      for (Object setname :
              getOwner().getDocument().getAnnotationSetNames()) {
        if (getAnnotationEditor().getAnnotationSetCurrentlyEdited() != null
               && ((String)setname).equals(getAnnotationEditor()
                .getAnnotationSetCurrentlyEdited().getName())) { continue; }
        treeSet = new TreeSet<String>(getOwner().getDocument()
                .getAnnotations((String)setname).getAllTypes());
        searchedAnnotationComboBox.addItem("------------");
        for (String annotationType : treeSet) {
          searchedAnnotationComboBox.addItem(annotationType);
        }
      }
    }

    searchedAnnotationComboBox.setSelectedItem(lastSelectedAnnotationName);
  }

  protected class FindFirstAction extends AbstractAction{

    private static final long serialVersionUID = 1L;

    public FindFirstAction(){
      super("First");
      super.putValue(SHORT_DESCRIPTION, "Finds the first occurrence.");
      super.putValue(MNEMONIC_KEY, KeyEvent.VK_F);
    }

    public void actionPerformed(ActionEvent evt){
      if(getOwner() == null) { return; }
      String patternText = searchTextField.getText();
      if(patternText == null) { return; }
      int flags = 0;
      if(!searchCaseSensChk.isSelected()) {
        flags |= Pattern.CASE_INSENSITIVE; }
      if(!searchRegExpChk.isSelected()) {
        flags |= Pattern.LITERAL; }
      Pattern pattern = null;

      try {
        pattern = Pattern.compile(patternText, flags);
        String text = getOwner().getDocument().getContent().toString();
        matcher = pattern.matcher(text);

        boolean found = matcher.find();
        if (searchedAnnotationComboBox.getSelectedIndex() > 0) {
          // while there is no annotation of the selected type
          // that contains the searched text 
          while (found
                 && !getAnnotationEditor().getAnnotationSetCurrentlyEdited()
                .get(new Long(matcher.start()), new Long(matcher.end()))
                .getAllTypes().contains(
                        searchedAnnotationComboBox.getSelectedItem())) {
            found = matcher.find();
          }
        }
        if (found) {
          findNextAction.setEnabled(true);
          annotateMatchAction.setEnabled(true);
          annotateAllMatchesAction.setEnabled(false);
          int start = matcher.start();
          int end = matcher.end();
          matchedIndexes = new LinkedList<Vector<Integer>>();
          Vector<Integer> v = new Vector<Integer>(2);
          v.add(new Integer(start));
          v.add(new Integer(end));
          matchedIndexes.add(v);
          getOwner().getTextComponent().requestFocus();
          getOwner().getTextComponent().select(start, end);
          // FIXME: put the focus back on the "First" button
//          firstSmallButton.requestFocusInWindow();
          searchPane.requestFocus();

        } else {
          // no match found
          findNextAction.setEnabled(false);
          annotateMatchAction.setEnabled(false);
        }
        findPreviousAction.setEnabled(false);

      } catch(PatternSyntaxException e) {
        // FIXME: put this error dialog in front of the editor dialog
        // when the dialog is a JWindow
//        getAnnotationEditorWindow().setAlwaysOnTop(false);
//          getAnnotationEditorWindow().toBack();
        JOptionPane errorOptionsPane = new JOptionPane(
                "Invalid pattern!\n" + e.toString(),
                JOptionPane.ERROR_MESSAGE);
        JDialog errorDialog = errorOptionsPane
                .createDialog(getAnnotationEditorWindow(), "GATE");
//          errorDialog.setAlwaysOnTop(true);
        errorDialog.setVisible(true);
//          errorDialog.toFront();
      }
    }
  }
  
  protected class FindPreviousAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    public FindPreviousAction() {
      super("Previous");
      super.putValue(SHORT_DESCRIPTION, "Finds the previous occurrence.");
      super.putValue(MNEMONIC_KEY, KeyEvent.VK_P);
    }

    public void actionPerformed(ActionEvent evt) {

      // the first time we invoke previous action we want to go two
      // previous matches back not just one
      matchedIndexes.removeLast();

      Vector<Integer> v;
      if (matchedIndexes.size() == 1) {
        // no more previous annotation, disable the action
        findPreviousAction.setEnabled(false);
      }
      v = matchedIndexes.getLast();
      int start = (v.firstElement()).intValue();
      int end = (v.lastElement()).intValue();
      getOwner().getTextComponent().select(start, end);
      getAnnotationEditor().placeDialog(start, end);
      // reset the matcher for the next FindNextAction
      matcher.find(start);
      findNextAction.setEnabled(true);
      annotateMatchAction.setEnabled(true);
    }
  }

  protected class FindNextAction extends AbstractAction{

    private static final long serialVersionUID = 1L;

    public FindNextAction(){
      super("Next");
      super.putValue(SHORT_DESCRIPTION, "Finds the next occurrence.");
      super.putValue(MNEMONIC_KEY, KeyEvent.VK_N);
    }

    public void actionPerformed(ActionEvent evt){
      if(matcher != null){
        boolean found = matcher.find();
        if (searchedAnnotationComboBox.getSelectedIndex() > 0) {
          // while there is no annotation of the selected type
          // that contains the searched text 
          while (found
                 && !getAnnotationEditor().getAnnotationSetCurrentlyEdited()
                .get(new Long(matcher.start()), new Long(matcher.end()))
                .getAllTypes().contains(
                        searchedAnnotationComboBox.getSelectedItem())) {
            found = matcher.find();
          }
        }
        if (found) {
          int start = matcher.start();
          int end = matcher.end();
          Vector<Integer> v = new Vector<Integer>(2);
          v.add(new Integer(start));
          v.add(new Integer(end));
          matchedIndexes.add(v);
          getOwner().getTextComponent().select(start, end);
          getAnnotationEditor().placeDialog(start, end);
          findPreviousAction.setEnabled(true);
        } else {
          //no more matches possible
          findNextAction.setEnabled(false);
          annotateMatchAction.setEnabled(false);
        }

      } else {
        //matcher is not prepared
        new FindFirstAction().actionPerformed(evt);
      }
    }
  }
  
  protected class AnnotateMatchAction extends AbstractAction{

    private static final long serialVersionUID = 1L;

    public AnnotateMatchAction(){
      super("Annotate");
      super.putValue(SHORT_DESCRIPTION, "Annotates the current match.");
      super.putValue(MNEMONIC_KEY, KeyEvent.VK_A);
    }
    
    public void actionPerformed(ActionEvent evt){
      if(matcher != null){
        int start = matcher.start();
        int end = matcher.end();
        FeatureMap features = Factory.newFeatureMap();
        if(getAnnotationEditor().getAnnotationCurrentlyEdited().getFeatures() != null) 
          features.putAll(getAnnotationEditor().getAnnotationCurrentlyEdited().getFeatures());
        try {
          Integer id =
            getAnnotationEditor().getAnnotationSetCurrentlyEdited().add(
              new Long(start), new Long(end), 
              getAnnotationEditor().getAnnotationCurrentlyEdited().getType(),
              features);
          Annotation newAnn =
            getAnnotationEditor().getAnnotationSetCurrentlyEdited().get(id);
          getOwner().getTextComponent().select(start, start);
          getAnnotationEditor().editAnnotation(newAnn,
            getAnnotationEditor().getAnnotationSetCurrentlyEdited());
          annotateAllMatchesAction.setEnabled(true);
          if (annotateAllMatchesSmallButton.getAction()
                  .equals(undoAnnotateAllMatchesAction)) {
            annotateAllMatchesSmallButton.setAction(annotateAllMatchesAction);
          }
        }
        catch(InvalidOffsetException e) {
          //the offsets here should always be valid.
          throw new LuckyException(e);
        }
      }
    }
  }
  
  protected class AnnotateAllMatchesAction extends AbstractAction{

    private static final long serialVersionUID = 1L;

    public AnnotateAllMatchesAction(){
      super("Ann. all");
      super.putValue(SHORT_DESCRIPTION, "Annotates all the following matches.");
      super.putValue(MNEMONIC_KEY, KeyEvent.VK_L);
    }

    public void actionPerformed(ActionEvent evt){
      annotateAllAnnotationsID = new LinkedList<Annotation>();
      boolean found = matcher.find();
      while(found) {
        if (searchedAnnotationComboBox.getSelectedIndex() > 0) {
          // while there is no annotation of the selected type
          // that contains the searched text 
          while (found
               && !getAnnotationEditor().getAnnotationSetCurrentlyEdited()
              .get(new Long(matcher.start()), new Long(matcher.end()))
              .getAllTypes().contains(
                      searchedAnnotationComboBox.getSelectedItem())) {
            found = matcher.find();
          }
          if (!found) { break; }
        }
        annotateCurrentMatch();
        found = matcher.find();
      }

      annotateAllMatchesSmallButton.setAction(undoAnnotateAllMatchesAction);
      undoAnnotateAllMatchesAction.setEnabled(true);
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
          Integer id = getAnnotationEditor().getAnnotationSetCurrentlyEdited().add(
            new Long(start), new Long(end), 
            getAnnotationEditor().getAnnotationCurrentlyEdited().getType(),
            features);
          Annotation newAnn =
            getAnnotationEditor().getAnnotationSetCurrentlyEdited().get(id);
          annotateAllAnnotationsID.add(newAnn);
        }
        catch(InvalidOffsetException e) {
          //the offsets here should always be valid.
          throw new LuckyException(e);
        }
      }
    }
  }
  
  /**
   * Remove the annotations added by the last action that annotate all matches.
   */
  protected class UndoAnnotateAllMatchesAction extends AbstractAction{

    private static final long serialVersionUID = 1L;

    public UndoAnnotateAllMatchesAction(){
      super("Undo");
      super.putValue(SHORT_DESCRIPTION, "Undo annotate all matches.");
      super.putValue(MNEMONIC_KEY, KeyEvent.VK_U);
    }
    
    public void actionPerformed(ActionEvent evt){

      Iterator<Annotation> it = annotateAllAnnotationsID.iterator();
      while (it.hasNext()) {
        getAnnotationEditor().getAnnotationSetCurrentlyEdited().remove(it.next());
      }

      // just hide the editor to avoid editing null annotation
      getAnnotationEditorWindow().setVisible(false);

      annotateAllMatchesSmallButton.setAction(annotateAllMatchesAction);
      annotateAllMatchesAction.setEnabled(false);
    }
  }

  /**
   * A smaller JButton with less margins.
   */
  protected class SmallButton extends JButton{

    private static final long serialVersionUID = 1L;

    public SmallButton(Action a) {
      super(a);
      setMargin(new Insets(0, 2, 0, 2));
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
   * @return The annotation editor window.
   */
  public Window getAnnotationEditorWindow() {
    return annotationEditorWindow;
  }

}
