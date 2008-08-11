/*
 *  Copyright (c) 1998-2007, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, 22 March 2004
 *
 *  $Id$
 */
package gate.gui.docview;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;

import gate.*;
import gate.creole.*;
import gate.gui.ActionsPublisher;
import gate.gui.MainFrame;
import gate.gui.annedit.AnnotationData;
import gate.swing.VerticalTextIcon;
import gate.util.GateRuntimeException;
import gate.util.LuckyException;

/**
 * This is the GATE Document viewer/editor. This class is only the shell of the
 * main document VR, which gets populated with views (objects that implement
 * the {@link DocumentView} interface.
 */

public class DocumentEditor extends AbstractVisualResource
                            implements ActionsPublisher {

  /**
   * The document view is just an empty shell. This method publishes the actions
   * from the contained views. 
   */
  public List getActions() {
    List actions = new ArrayList();
    Iterator viewIter;
    if(getCentralViews() != null){
      viewIter = getCentralViews().iterator();
      while(viewIter.hasNext()){
        actions.addAll(((DocumentView)viewIter.next()).getActions());
      }
    }
    if(getHorizontalViews() != null){
      viewIter = getHorizontalViews().iterator();
      while(viewIter.hasNext()){
        actions.addAll(((DocumentView)viewIter.next()).getActions());
      }
    }
    if(getVerticalViews() != null){
      viewIter = getVerticalViews().iterator();
      while(viewIter.hasNext()){
        actions.addAll(((DocumentView)viewIter.next()).getActions());
      }
    }
    return actions;
  }


  /* (non-Javadoc)
   * @see gate.Resource#init()
   */
  public Resource init() throws ResourceInstantiationException {
    addComponentListener(new ComponentAdapter() {
      public void componentHidden(ComponentEvent e) {
      }
      public void componentMoved(ComponentEvent e) {
      }
      public void componentResized(ComponentEvent e) {
        if(!viewsInited) initViews();
      }
      //lazily build the GUI only when needed
      public void componentShown(ComponentEvent e) {
        if(!viewsInited) initViews();
      }
    });

    return this;
  }
  
  public void cleanup(){
    Iterator viewsIter;
    if(centralViews != null){ 
      viewsIter= centralViews.iterator();
      while(viewsIter.hasNext()) ((Resource)viewsIter.next()).cleanup();
      centralViews.clear();
    }
    if(horizontalViews != null){
      viewsIter = horizontalViews.iterator();
      while(viewsIter.hasNext()) ((Resource)viewsIter.next()).cleanup();
      horizontalViews.clear();
    }
    if(verticalViews != null){
      viewsIter = verticalViews.iterator();
      while(viewsIter.hasNext()) ((Resource)viewsIter.next()).cleanup();
      verticalViews.clear();
    }
  }
  
  protected void initViews(){
    viewsInited = true;
    //start building the UI
    setLayout(new BorderLayout());
    JProgressBar progressBar = new JProgressBar();
    progressBar.setStringPainted(true);
    progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, progressBar.getPreferredSize().height));
    add(progressBar, BorderLayout.CENTER);

    progressBar.setString("Building views");
    progressBar.setValue(10);

    //create the skeleton UI
    topSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, null, null);
    topSplit.setResizeWeight(0.3);
    bottomSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSplit, null);
    bottomSplit.setResizeWeight(0.7);
    horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, bottomSplit, null);
    horizontalSplit.setResizeWeight(0.7);

    //create the bars
    topBar = new JToolBar(JToolBar.HORIZONTAL);
    topBar.setFloatable(false);
    add(topBar, BorderLayout.NORTH);

//    bottomBar = new JToolBar(JToolBar.HORIZONTAL);
//    bottomBar.setFloatable(false);
//    add(bottomBar, BorderLayout.SOUTH);

//    leftBar = new JToolBar(JToolBar.VERTICAL);
//    leftBar.setFloatable(false);
//    add(leftBar, BorderLayout.WEST);

//    rightBar = new JToolBar(JToolBar.VERTICAL);
//    rightBar.setFloatable(false);
//    add(rightBar, BorderLayout.EAST);

    progressBar.setValue(40);

    
    centralViews = new ArrayList();
    verticalViews = new ArrayList();
    horizontalViews = new ArrayList();

    //parse all Creole resources and look for document views
    Set vrSet = Gate.getCreoleRegister().getVrTypes();
    List viewTypes = new ArrayList();
    Iterator vrIter = vrSet.iterator();
    while(vrIter.hasNext()){
      ResourceData rData = (ResourceData)Gate.getCreoleRegister().
                           get(vrIter.next());
      try{
        if(DocumentView.class.isAssignableFrom(rData.getResourceClass())){
          viewTypes.add(rData);
        }
      }catch(ClassNotFoundException cnfe){
        cnfe.printStackTrace();
      }
    }
    //sort view types by label
    Collections.sort(viewTypes, new Comparator(){
      public int compare(Object o1, Object o2){
        ResourceData rd1 = (ResourceData)o1;
        ResourceData rd2 = (ResourceData)o2;
        return rd1.getName().compareTo(rd2.getName());
      }
    });
    Iterator viewIter = viewTypes.iterator();
    while(viewIter.hasNext()){
      ResourceData rData = (ResourceData)viewIter.next();
      try{
        //create the resource
        DocumentView aView = (DocumentView)Factory.
                             createResource(rData.getClassName());
        aView.setTarget(document);
        aView.setOwner(this);
        //add the view
        addView(aView, rData.getName());
      }catch(ResourceInstantiationException rie){
            rie.printStackTrace();
      }
    }
    //select the main central view only
    if(centralViews.size() > 0) setCentralView(0);
    
    //populate the main VIEW
    remove(progressBar);
    add(horizontalSplit, BorderLayout.CENTER);
    topBar.addSeparator();
    Action searchAction = new SearchAction();
    searchAction.putValue(Action.SHORT_DESCRIPTION,
      "<html>"+searchAction.getValue(Action.SHORT_DESCRIPTION)
      +"&nbsp;&nbsp;<font color=#667799><small>Ctrl-F"
      +"&nbsp;&nbsp;</small></font></html>");
    topBar.add(searchAction);
    // add a key binding for the search function
    getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
      KeyStroke.getKeyStroke("control F"), "Search in text");
    getActionMap().put("Search in text", searchAction);
    
    
    validate();
  }
  
  public List getCentralViews(){
  	return centralViews == null ? null : 
      Collections.unmodifiableList(centralViews);
  }
  
  public List getHorizontalViews(){
    return horizontalViews == null ? null : 
      Collections.unmodifiableList(horizontalViews);
  }
  
  public List getVerticalViews(){
    return verticalViews == null ? null : 
      Collections.unmodifiableList(verticalViews);
  }
  

  /**
   * Registers a new view by adding it to the right list and creating the 
   * activation button for it.
   * @param view
   */
  protected void addView(DocumentView view, String name){
    topBar.add(Box.createHorizontalStrut(5));
    final ViewButton viewButton = new ViewButton(view, name);
    switch(view.getType()){
      case DocumentView.CENTRAL :
        centralViews.add(view);
//      	leftBar.add(new ViewButton(view, name));
        topBar.add(viewButton);
        break;
      case DocumentView.VERTICAL :
        verticalViews.add(view);
//      	rightBar.add(new ViewButton(view, name));
        topBar.add(viewButton);
        break;
      case DocumentView.HORIZONTAL :
        horizontalViews.add(view);
      	topBar.add(viewButton);
//      	bottomBar.add(new ViewButton(view, name));
      	break;
      default :
        throw new GateRuntimeException(getClass().getName() +  ": Invalid view type");
    }

    // binds a F-key to each view toggle button
    // avoid the F-Key F1,2,6,8,10 because already used
    if ((numberOfTheFKeyforLastView == 5)
     || (numberOfTheFKeyforLastView == 7)
     || (numberOfTheFKeyforLastView == 9)) {
      numberOfTheFKeyforLastView++;
    }
    getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
      .put(KeyStroke.getKeyStroke("F"+(numberOfTheFKeyforLastView+1)),
      "Shows view "+numberOfTheFKeyforLastView);
    getActionMap().put("Shows view "+numberOfTheFKeyforLastView,
        new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
              viewButton.doClick();
            }
        }
    );
    viewButton.setToolTipText("<html>Toggle the view of "+name
      +"&nbsp;&nbsp;<font color=#667799><small>F"
      +(numberOfTheFKeyforLastView+1)
      +"&nbsp;&nbsp;</small></font></html>");
    numberOfTheFKeyforLastView++;
  }

  /**
   * Gets the currently showing top view
   * @return a {@link DocumentView} object.
   */
  protected DocumentView getTopView(){
    if(topViewIdx == -1) return null;
    else return(DocumentView)horizontalViews.get(topViewIdx);
  }
  
  /**
   * Shows a new top view based on an index in the {@link #horizontalViews}
   * list.
   * @param index the index in {@link #horizontalViews} list for the new
   * view to be shown.
   */
  public void setTopView(int index){
    //deactivate current view
    DocumentView oldView = getTopView();
    if(oldView != null){
      oldView.setActive(false);
    }
    topViewIdx = index;
    if(topViewIdx == -1) setTopView(null);
    else{
	    DocumentView newView = (DocumentView)horizontalViews.get(topViewIdx);
	    //hide if shown at the bottom
	    if(bottomViewIdx == topViewIdx){
	      setBottomView(null);
	      bottomViewIdx  = -1;
	    }
	    //activate if necessary
	    if(!newView.isActive()){
	      newView.setActive(true);
	    }
	    //show the new view
	    setTopView(newView);
    }
  }

  /**
   * Sets a new UI component in the top location. This method is intended to 
   * only be called from {@link #setTopView(int)}.
   * @param view the new view to be shown.
   */
  protected void setTopView(DocumentView view){
    topSplit.setTopComponent(view == null ? null : view.getGUI());
    topSplit.resetToPreferredSizes();
    updateBar(topBar);
    validate();
  }

  /**
   * Gets the currently showing central view
   * @return a {@link DocumentView} object.
   */
  protected DocumentView getCentralView(){
    if(centralViewIdx == -1) return null;
    else return(DocumentView)centralViews.get(centralViewIdx);
  }
  
  /**
   * Shows a new central view based on an index in the {@link #centralViews}
   * list.
   * @param index the index in {@link #centralViews} list for the new
   * view to be shown.
   */
  public void setCentralView(int index){
    //deactivate current view
    DocumentView oldView = getCentralView();
    if(oldView != null){
      oldView.setActive(false);
    }
    centralViewIdx = index;
    if(centralViewIdx == -1) setCentralView(null);
    else{
	    DocumentView newView = (DocumentView)centralViews.get(centralViewIdx);
	    //activate if necessary
	    if(!newView.isActive()){
	      newView.setActive(true);
	    }
	    //show the new view
	    setCentralView(newView);
    }
  }

  /**
   * Sets a new UI component in the central location. This method is intended to 
   * only be called from {@link #setCentralView(int)}.
   * @param view the new view to be shown.
   */
  protected void setCentralView(DocumentView view){
    topSplit.setBottomComponent(view == null ? null : view.getGUI());
    topSplit.resetToPreferredSizes();
//    updateBar(leftBar);
    updateBar(topBar);
    validate();
  }
  
  
  /**
   * Gets the currently showing bottom view
   * @return a {@link DocumentView} object.
   */
  protected DocumentView getBottomView(){
    if(bottomViewIdx == -1) return null;
    else return(DocumentView)horizontalViews.get(bottomViewIdx);
  }
  
  /**
   * Shows a new bottom view based on an index in the {@link #horizontalViews}
   * list.
   * @param index the index in {@link #horizontalViews} list for the new
   * view to be shown.
   */
  public void setBottomView(int index){
    //deactivate current view
    DocumentView oldView = getBottomView();
    if(oldView != null){
      oldView.setActive(false);
    }
    bottomViewIdx = index;
    if(bottomViewIdx == -1){
      setBottomView(null);
    }else{
	    DocumentView newView = (DocumentView)horizontalViews.get(bottomViewIdx);
	    //hide if shown at the top
	    if(topViewIdx == bottomViewIdx){
	      setTopView(null);
	      topViewIdx  = -1;
	    }
	    //activate if necessary
	    if(!newView.isActive()){
	      newView.setActive(true);
	    }
	    //show the new view
	    setBottomView(newView);
    }
  }

  /**
   * Sets a new UI component in the top location. This method is intended to 
   * only be called from {@link #setBottomView(int)}.
   * @param view the new view to be shown.
   */
  protected void setBottomView(DocumentView view){
    bottomSplit.setBottomComponent(view == null ? null : view.getGUI());
    bottomSplit.resetToPreferredSizes();
//    updateBar(bottomBar);
    updateBar(topBar);
    validate();
  }
  
  
  /**
   * Gets the currently showing right view
   * @return a {@link DocumentView} object.
   */
  protected DocumentView getRightView(){
    if(rightViewIdx == -1) return null;
    else return(DocumentView)verticalViews.get(rightViewIdx);
  }
  
  /**
   * Shows a new right view based on an index in the {@link #verticalViews}
   * list.
   * @param index the index in {@link #verticalViews} list for the new
   * view to be shown.
   */
  public void setRightView(int index){
    //deactivate current view
    DocumentView oldView = getRightView();
    if(oldView != null){
      oldView.setActive(false);
    }
    rightViewIdx = index;
    if(rightViewIdx == -1) setRightView(null);
    else{
	    DocumentView newView = (DocumentView)verticalViews.get(rightViewIdx);
	    //activate if necessary
	    if(!newView.isActive()){
	      newView.setActive(true);
	    }
	    //show the new view
	    setRightView(newView);
    }
  }

  /**
   * Sets a new UI component in the right hand side location. This method is 
   * intended to only be called from {@link #setRightView(int)}.
   * @param view the new view to be shown.
   */
  protected void setRightView(DocumentView view){
    horizontalSplit.setRightComponent(view == null ? null : view.getGUI());
//    updateBar(rightBar);
    updateBar(topBar);
    validate();
  }  
  
  /**
   * Called to change the set of selected annotations. This new value will be 
   * sent to all active constituent views.
   * @param selectedAnnots
   */
  public void setSelectedAnnotations(List<AnnotationData> selectedAnnots){
    selectedAnnotations.clear();
    selectedAnnotations.addAll(selectedAnnots);
    //notify all active views
    for(DocumentView aView : centralViews){
      if(aView.isActive()) aView.setSelectedAnnotations(selectedAnnotations);
    }
    for(DocumentView aView : horizontalViews){
      if(aView.isActive()) aView.setSelectedAnnotations(selectedAnnotations);
    }
    for(DocumentView aView : verticalViews){
      if(aView.isActive()) aView.setSelectedAnnotations(selectedAnnotations);
    }
  }
  
  /**
   * Gets the current set of selected annotations.
   * @return
   */
  public List<AnnotationData> getSelectedAnnotations(){
    return selectedAnnotations;
  }
  
  protected void updateSplitLocation(JSplitPane split, int foo){
    Component left = split.getLeftComponent();
    Component right = split.getRightComponent();
    if(left == null){
      split.setDividerLocation(0);
      return;
    }
    if(right == null){ 
      split.setDividerLocation(1);
      return;
    }
    Dimension leftPS = left.getPreferredSize();
    Dimension rightPS = right.getPreferredSize();
    double location = split.getOrientation() == JSplitPane.HORIZONTAL_SPLIT ? 
      (double)leftPS.width / (leftPS.width + rightPS.width) :
      (double)leftPS.height / (leftPS.height + rightPS.height);
    split.setDividerLocation(location);
  }
  
  /* (non-Javadoc)
   * @see gate.VisualResource#setTarget(java.lang.Object)
   */
  public void setTarget(Object target) {
    this.document = (Document)target;
  }

  /**
   * Updates the selected state of the buttons on one of the toolbars. 
   * @param toolbar
   */
  protected void updateBar(JToolBar toolbar){
    Component btns[] = toolbar.getComponents();
    if(btns != null){
      for(int i = 0; i < btns.length; i++){
        if(btns[i] instanceof ViewButton) 
          ((ViewButton)btns[i]).updateSelected();
      }
    }
  }

  /**
   * Code taken from gate.gui.DocumentEditor, the old DocumentEditor.
   * Modified to work with gate 3.1.
   */
  protected class SearchAction extends AbstractAction {

    public SearchAction() {
      super("Search text", MainFrame.getIcon("search"));
      putValue(SHORT_DESCRIPTION, "Search within the text");
    }

    public void actionPerformed(ActionEvent evt) {
      if (searchDialog == null) {
        Window parent =
          SwingUtilities.getWindowAncestor(DocumentEditor.this);
        searchDialog = (parent instanceof Dialog)?
          new SearchDialog((Dialog)parent):new SearchDialog((Frame)parent);
        searchDialog.pack();
        searchDialog.setLocationRelativeTo(DocumentEditor.this);
        searchDialog.setResizable(false);
        MainFrame.getGuiRoots().add(searchDialog);
      }

      // FIXME: that's ugly !!
      javax.swing.text.JTextComponent textPane =
        (javax.swing.text.JTextComponent)
        ((javax.swing.JViewport)
                ((JScrollPane)getCentralView().getGUI())
                .getViewport()).getView();

      // if the user never gives the focus to the textPane then
      // there will never be any selection in it so we force it
      textPane.requestFocusInWindow();

      // put the selection of the document into the search text field
      if (textPane.getSelectedText() != null) {
        searchDialog.patternTextField.setText(textPane.getSelectedText());
      }

      if (searchDialog.isVisible()) {
        searchDialog.toFront();
      } else {
        searchDialog.setVisible(true);
      }
      searchDialog.patternTextField.selectAll();
      searchDialog.patternTextField.requestFocus();
    }
  }

  protected class SearchDialog extends JDialog {

    SearchDialog(Frame owner) {
      super(owner, false);
      setTitle("Find in \"" + document.getName() + "\"");
      initLocalData();
      initGuiComponents();
      initListeners();
    }

    SearchDialog(Dialog owner) {
      super(owner, false);
      setTitle("Find in \"" + document.getName() + "\"");
      initLocalData();
      initGuiComponents();
      initListeners();
    }

    protected void initLocalData() {
      pattern = null;
      nextMatchStartsFrom = 0;
      content = document.getContent().toString();

      findFirstAction = new AbstractAction("Find first") {
        {
          putValue(SHORT_DESCRIPTION, "Finds first match");
          putValue(MNEMONIC_KEY, KeyEvent.VK_F);
        }

        public void actionPerformed(ActionEvent evt) {
          //needed to create the right RE
          refresh();
          if(!validateRE()) return;
          //remove selection
          textPane.setCaretPosition(textPane.getCaretPosition());
          boolean found = false;
          int start = -1;
          int end = -1;
          nextMatchStartsFrom = 0;

          Matcher matcher = pattern.matcher(content);
          while (matcher.find(nextMatchStartsFrom) && !found) {
            start = matcher.start();
            end = matcher.end();
            found = false;
            if (highlightsChk.isSelected()) {
              javax.swing.text.Highlighter.Highlight[] highlights =
                textPane.getHighlighter().getHighlights();
              for (javax.swing.text.Highlighter.Highlight h : highlights) {
                if (h.getStartOffset() <= start && h.getEndOffset() >= end) {
                  found = true;
                  break;
                }
              }
            } else {
              found = true;
            }
            nextMatchStartsFrom = end;
          }

          if (found) {
            //display the result
            textPane.setCaretPosition(start);
            textPane.moveCaretPosition(end);

          } else {
            JOptionPane.showMessageDialog(searchDialog, "String not found!",
              "GATE", JOptionPane.INFORMATION_MESSAGE);
          }
        }};

      findNextAction = new AbstractAction("Find next") {
        {
          putValue(SHORT_DESCRIPTION, "Finds next match");
          putValue(MNEMONIC_KEY, KeyEvent.VK_N);
        }
        public void actionPerformed(ActionEvent evt) {
          //needed to create the right RE
          refresh();
          if(!validateRE()) return;
          //remove selection
          textPane.setCaretPosition(textPane.getCaretPosition());
          boolean found = false;
          int start = -1;
          int end = -1;
          nextMatchStartsFrom = textPane.getCaretPosition();

          Matcher matcher = pattern.matcher(content);
          while (matcher.find(nextMatchStartsFrom) && !found) {
            start = matcher.start();
            end = matcher.end();
            found = false;
            if (highlightsChk.isSelected()) {
              javax.swing.text.Highlighter.Highlight[] highlights =
                textPane.getHighlighter().getHighlights();
              for (javax.swing.text.Highlighter.Highlight h : highlights) {
                if (h.getStartOffset() <= start && h.getEndOffset() >= end) {
                  found = true;
                  break;
                }
              }
            } else {
              found = true;
            }
            nextMatchStartsFrom = end;
          }

          if (found) {
            //display the result
            textPane.setCaretPosition(start);
            textPane.moveCaretPosition(end);

          } else {
            JOptionPane.showMessageDialog(searchDialog, "String not found!",
              "GATE", JOptionPane.INFORMATION_MESSAGE);
          }
        }};

      cancelAction = new AbstractAction("Cancel") {
        {
          putValue(SHORT_DESCRIPTION, "Cancel");
        }
        public void actionPerformed(ActionEvent evt){
          searchDialog.setVisible(false);
        }
      };
    }

    protected void initGuiComponents() {
      getContentPane().setLayout(new BoxLayout(getContentPane(),
              BoxLayout.Y_AXIS));

      getContentPane().add(Box.createVerticalStrut(5));

      Box hBox = Box.createHorizontalBox();
      hBox.add(Box.createHorizontalStrut(5));
      hBox.add(new JLabel("Find what:"));
      hBox.add(Box.createHorizontalStrut(5));
      hBox.add(patternTextField = new JTextField(20));
      hBox.add(Box.createHorizontalStrut(2));
      JButton helpRegExpButton = new JButton("?");
      helpRegExpButton.setMargin(new Insets(0, 2, 0, 2));
      helpRegExpButton.setToolTipText("Predefined search expressions.");
      helpRegExpButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent arg0) {
            String[] values1 = {
              "Number",
              "Person"
            };
            String[] values2 = {
              "Any character",
              "The beginning of a line",
              "The end of a line",
              "All letters",
              "Letter uppercase",
              "Letter lowercase",
              "Letter titlecase",
              "Letter modifier",
              "Letter other",
              "All Numbers",
              "Number decimal digit",
              "Number letter",
              "Number other",
              "All punctuations",
              "Punctuation connector",
              "Punctuation dash",
              "Punctuation open",
              "Punctuation close",
              "Punctuation initial quote",
              "Punctuation final quote",
              "Punctuation other",
              "All symbols",
              "Symbol math",
              "Symbol currency",
              "Symbol modifier",
              "Symbol other",
              "All separators",
              "Separator space",
              "Separator line",
              "Separator paragraph",
              "All Marks",
              "Mark nonspacing",
              "Mark spacing combining",
              "Mark enclosing",
              "All others",
              "Other control",
              "Other format",
              "Other surrogate",
              "Other private use",
              "Other not assigned",
              "Any character except Category",
              "Category1 and/or Category2",
              "Category1 and Category2"
            };
            String[] values3 = {
              "Either the selection or X",
              "Once or not at all",
              "Zero or more times",
              "One or more times",
              "Capturing group",
              "Non-capturing group"
            };
            JPanel vspace1 = new JPanel();
            vspace1.setSize(0, 5);
            final JList list1 = new JList(values1);
            list1.setVisibleRowCount(Math.min(10, values1.length));
            list1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane jsp1 = new JScrollPane(list1);
            final JButton b1 = new JButton("Replace search expression");
            b1.setEnabled(false);
            JPanel vspace2 = new JPanel();
            vspace2.setSize(0, 5);
            final JList list2 = new JList(values2);
            list2.setVisibleRowCount(Math.min(10, values2.length));
            list2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane jsp2 = new JScrollPane(list2);
            final JButton b2 = new JButton("Insert at the caret position");
            b2.setEnabled(false);
            JPanel vspace3 = new JPanel();
            vspace3.setSize(0, 5);
            final JList list3 = new JList(values3);
            list3.setVisibleRowCount(Math.min(10, values3.length));
            list3.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane jsp3 = new JScrollPane(list3);
            final JButton b3 = new JButton("Modify the selection");
            b3.setEnabled(false);
            if (patternTextField.getSelectedText() == null) {
              list3.setEnabled(false);
            }
            Object[] messageObjects = {
              "Choose a predefined search:",
              vspace1, jsp1, b1, vspace2, jsp2, b2, vspace3, jsp3, b3
            };
            String options[] = {"Cancel"};
            final JOptionPane optionPane = new JOptionPane(
              messageObjects,
              JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
              null, options, "Cancel");
            b1.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                if (list1.getSelectedValue() != null) {
                  optionPane.setValue(list1.getSelectedValue().toString());
                  optionPane.setVisible(false);
                } else {
                  optionPane.setValue("");
                }
              }
            });
            list1.addMouseListener(new MouseAdapter() {
              public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() == 2) {
                  optionPane.setValue(list1.getSelectedValue().toString());
                  optionPane.setVisible(false);
                }
              }
            });
            list1.addListSelectionListener(new ListSelectionListener() {
              public void valueChanged(ListSelectionEvent e) {
                if (list1.getSelectedValue() != null) {
                  b1.setEnabled(true);
                } else {
                  b1.setEnabled(false);
                }
              }
            });
            b2.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                if (list2.getSelectedValue() != null) {
                  optionPane.setValue(list2.getSelectedValue().toString());
                  optionPane.setVisible(false);
                } else {
                  optionPane.setValue("");
                }
              }
            });
            list2.addMouseListener(new MouseAdapter() {
              public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() == 2) {
                  optionPane.setValue(list2.getSelectedValue().toString());
                  optionPane.setVisible(false);
                }
              }
            });
            list2.addListSelectionListener(new ListSelectionListener() {
              public void valueChanged(ListSelectionEvent e) {
                if (list2.getSelectedValue() != null) {
                  b2.setEnabled(true);
                } else {
                  b2.setEnabled(false);
                }
              }
            });
            b3.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                if (list3.getSelectedValue() != null) {
                  optionPane.setValue(list3.getSelectedValue().toString());
                  optionPane.setVisible(false);
                } else {
                  optionPane.setValue("");
                }
              }
            });
            list3.addMouseListener(new MouseAdapter() {
              public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() == 2) {
                  optionPane.setValue(list3.getSelectedValue().toString());
                  optionPane.setVisible(false);
                }
              }
            });
            list3.addListSelectionListener(new ListSelectionListener() {
              public void valueChanged(ListSelectionEvent e) {
                if (list3.getSelectedValue() != null) {
                  b3.setEnabled(true);
                } else {
                  b3.setEnabled(false);
                }
              }
            });
            JDialog optionDialog = optionPane.createDialog(
              gate.gui.MainFrame.getInstance(), "GATE");
            optionDialog.setVisible(true);
            Object selectedValue = optionPane.getValue();
            if (selectedValue == null
             || !(selectedValue instanceof String)
             || selectedValue.equals("Cancel")) {
              return;
            } else {
              ignoreCaseChk.setSelected(false);
              regularExpressionChk.setSelected(true);
              wholeWordsChk.setSelected(false);
            }
            int p = patternTextField.getCaretPosition();
            int s1 = patternTextField.getSelectionStart();
            int s2 = patternTextField.getSelectionEnd();
            try {
            if (selectedValue.equals("Number")) {
              patternTextField.setText("\\b[\\p{N}][\\p{N},.]*\\b");
            } else if (selectedValue.equals("Person")) {
              patternTextField.setText("\\p{Lu}\\p{L}+, \\p{Lu}\\.(?: \\p{Lu}\\.)*");
            } else if (selectedValue.equals("Either the selection or X")) {
              patternTextField.getDocument().insertString(s1, "(?:", null);
              patternTextField.getDocument().insertString(s2+3, ")|(?:X)", null);
            } else if (selectedValue.equals("Once or not at all")) {
              patternTextField.getDocument().insertString(s1, "(?:", null);
              patternTextField.getDocument().insertString(s2+3, ")?", null);
            } else if (selectedValue.equals("Zero or more times")) {
              patternTextField.getDocument().insertString(s1, "(?:", null);
              patternTextField.getDocument().insertString(s2+3, ")*", null);
            } else if (selectedValue.equals("One or more times")) {
              patternTextField.getDocument().insertString(s1, "(?:", null);
              patternTextField.getDocument().insertString(s2+3, ")+", null);
            } else if (selectedValue.equals("Capturing group")) {
              patternTextField.getDocument().insertString(s1, "(?:", null);
              patternTextField.getDocument().insertString(s2+3, ")", null);
            } else if (selectedValue.equals("Non-capturing group")) {
              patternTextField.getDocument().insertString(s1, "(?:", null);
              patternTextField.getDocument().insertString(s2+3, ")", null);
            } else if (selectedValue.equals("Any character")) {
              patternTextField.getDocument().insertString(p, ".", null);
            } else if (selectedValue.equals("The beginning of a line")) {
              patternTextField.getDocument().insertString(p, "^", null);
            } else if (selectedValue.equals("The end of a line")) {
              patternTextField.getDocument().insertString(p, "$", null);
            } else if (selectedValue.equals("Any character except Category")) {
              patternTextField.getDocument().insertString(p, "\\P{Category}", null);
            } else if (selectedValue.equals("Category1 and/or Category2")) {
              patternTextField.getDocument().insertString(p, "[\\p{Category1}\\p{Category2}]", null);
            } else if (selectedValue.equals("Category1 and Category2")) {
              patternTextField.getDocument().insertString(p, "[\\p{Category1}&&\\p{Category2}]", null);
            } else if (selectedValue.equals("All letters")) {
              patternTextField.getDocument().insertString(p, "\\p{L}", null);
            } else if (selectedValue.equals("Letter uppercase")) {
              patternTextField.getDocument().insertString(p, "\\p{Lu}", null);
            } else if (selectedValue.equals("Letter lowercase")) {
              patternTextField.getDocument().insertString(p, "\\p{Ll}", null);
            } else if (selectedValue.equals("Letter titlecase")) {
              patternTextField.getDocument().insertString(p, "\\p{Lt}", null);
            } else if (selectedValue.equals("Letter modifier")) {
              patternTextField.getDocument().insertString(p, "\\p{Lm}", null);
            } else if (selectedValue.equals("Letter other")) {
              patternTextField.getDocument().insertString(p, "\\p{Lo}", null);
            } else if (selectedValue.equals("All Marks")) {
              patternTextField.getDocument().insertString(p, "\\p{M}", null);
            } else if (selectedValue.equals("Mark nonspacing")) {
              patternTextField.getDocument().insertString(p, "\\p{Mn}", null);
            } else if (selectedValue.equals("Mark spacing combining")) {
              patternTextField.getDocument().insertString(p, "\\p{Mc}", null);
            } else if (selectedValue.equals("Mark enclosing")) {
              patternTextField.getDocument().insertString(p, "\\p{Me}", null);
            } else if (selectedValue.equals("All Numbers")) {
              patternTextField.getDocument().insertString(p, "\\p{N}", null);
            } else if (selectedValue.equals("Number decimal digit")) {
              patternTextField.getDocument().insertString(p, "\\p{Nd}", null);
            } else if (selectedValue.equals("Number letter")) {
              patternTextField.getDocument().insertString(p, "\\p{Nl}", null);
            } else if (selectedValue.equals("Number other")) {
              patternTextField.getDocument().insertString(p, "\\p{No}", null);
            } else if (selectedValue.equals("All separators")) {
              patternTextField.getDocument().insertString(p, "\\p{Z}", null);
            } else if (selectedValue.equals("Separator space")) {
              patternTextField.getDocument().insertString(p, "\\p{Zs}", null);
            } else if (selectedValue.equals("Separator line")) {
              patternTextField.getDocument().insertString(p, "\\p{Zl}", null);
            } else if (selectedValue.equals("Separator paragraph")) {
              patternTextField.getDocument().insertString(p, "\\p{Zp}", null);
            } else if (selectedValue.equals("All others")) {
              patternTextField.getDocument().insertString(p, "\\p{C}", null);
            } else if (selectedValue.equals("Other control")) {
              patternTextField.getDocument().insertString(p, "\\p{Cc}", null);
            } else if (selectedValue.equals("Other format")) {
              patternTextField.getDocument().insertString(p, "\\p{Cf}", null);
            } else if (selectedValue.equals("Other surrogate")) {
              patternTextField.getDocument().insertString(p, "\\p{Cs}", null);
            } else if (selectedValue.equals("Other private use")) {
              patternTextField.getDocument().insertString(p, "\\p{Co}", null);
            } else if (selectedValue.equals("Other not assigned")) {
              patternTextField.getDocument().insertString(p, "\\p{Cn}", null);
            } else if (selectedValue.equals("All punctuations")) {
              patternTextField.getDocument().insertString(p, "\\p{P}", null);
            } else if (selectedValue.equals("Punctuation connector")) {
              patternTextField.getDocument().insertString(p, "\\p{Pc}", null);
            } else if (selectedValue.equals("Punctuation dash")) {
              patternTextField.getDocument().insertString(p, "\\p{Pd}", null);
            } else if (selectedValue.equals("Punctuation open")) {
              patternTextField.getDocument().insertString(p, "\\p{Ps}", null);
            } else if (selectedValue.equals("Punctuation close")) {
              patternTextField.getDocument().insertString(p, "\\p{Pe}", null);
            } else if (selectedValue.equals("Punctuation initial quote")) {
              patternTextField.getDocument().insertString(p, "\\p{Pi}", null);
            } else if (selectedValue.equals("Punctuation final quote")) {
              patternTextField.getDocument().insertString(p, "\\p{Pf}", null);
            } else if (selectedValue.equals("Punctuation other")) {
              patternTextField.getDocument().insertString(p, "\\p{Po}", null);
            } else if (selectedValue.equals("All symbols")) {
              patternTextField.getDocument().insertString(p, "\\p{S}", null);
            } else if (selectedValue.equals("Symbol math")) {
              patternTextField.getDocument().insertString(p, "\\p{Sm}", null);
            } else if (selectedValue.equals("Symbol currency")) {
              patternTextField.getDocument().insertString(p, "\\p{Sc}", null);
            } else if (selectedValue.equals("Symbol modifier")) {
              patternTextField.getDocument().insertString(p, "\\p{Sk}", null);
            } else if (selectedValue.equals("Symbol other")) {
              patternTextField.getDocument().insertString(p, "\\p{So}", null);
            }
            } catch (BadLocationException e) {
              // should never happend
              throw new LuckyException(e);
            }
          }
        });
      hBox.add(helpRegExpButton);
      hBox.add(Box.createHorizontalGlue());

      hBox.add(Box.createHorizontalStrut(5));
      hBox.add(Box.createHorizontalGlue());
      getContentPane().add(hBox);

      getContentPane().add(Box.createVerticalStrut(5));

      hBox = Box.createHorizontalBox();
      hBox.add(Box.createHorizontalStrut(5));
      hBox.add(ignoreCaseChk = new JCheckBox("Ignore case", true));
      hBox.add(Box.createHorizontalStrut(5));
      hBox.add(wholeWordsChk = new JCheckBox("Whole word", false));
      hBox.add(Box.createHorizontalStrut(5));
      hBox.add(regularExpressionChk =
        new JCheckBox("Regular Exp.", false));
      regularExpressionChk.setToolTipText(
        "<html>Regular expression search."+
        "<br>See java.util.regex.Pattern.</html>");
      hBox.add(Box.createHorizontalStrut(5));
      hBox.add(highlightsChk = new JCheckBox("Highlights", false));
      highlightsChk.setToolTipText(
        "Restrict the search on the highlighted annotations.");
      hBox.add(Box.createHorizontalStrut(5));
      hBox.add(Box.createHorizontalGlue());
      getContentPane().add(hBox);

      getContentPane().add(Box.createVerticalStrut(5));

      hBox = Box.createHorizontalBox();
      hBox.add(Box.createHorizontalGlue());
      JButton findFirstButton = new JButton(findFirstAction);
      hBox.add(findFirstButton);
      hBox.add(Box.createHorizontalStrut(5));
      hBox.add(new JButton(findNextAction));
      hBox.add(Box.createHorizontalStrut(5));
      hBox.add(new JButton(cancelAction));
      hBox.add(Box.createHorizontalGlue());
      getContentPane().add(hBox);

      getContentPane().add(Box.createVerticalStrut(5));

      getRootPane().setDefaultButton(findFirstButton);
    }

    protected void initListeners() {

      addComponentListener(new ComponentAdapter() {
        public void componentHidden(ComponentEvent e) {
        }

        public void componentMoved(ComponentEvent e) {
        }

        public void componentResized(ComponentEvent e) {
        }

        public void componentShown(ComponentEvent e) {
          refresh();
        }
      });

      patternTextField.getDocument().addDocumentListener(
              new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event
                        .DocumentEvent e) {
                  refresh();
                }

                public void removeUpdate(javax.swing.event
                        .DocumentEvent e) {
                  refresh();
                }

                public void changedUpdate(javax.swing.event
                        .DocumentEvent e) {
                  refresh();
                }
              });

      ((JComponent)getContentPane())
      .getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
      put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancelAction");
      ((JComponent)getContentPane())
      .getActionMap().put("cancelAction", cancelAction);
    }

    /**
     * Validates the regular expression before use.
     * @return true if the regular expression is valid, false otherwise.
     */
    protected boolean validateRE(){
      String patternText = patternTextField.getText();
      boolean res = true;
      //update patternRE
      try {
        String prefixPattern = wholeWordsChk.isSelected() ? "\\b":"";
        prefixPattern += regularExpressionChk.isSelected() ? "":"\\Q";
        String suffixPattern = regularExpressionChk.isSelected() ? "":"\\E";
        suffixPattern += wholeWordsChk.isSelected() ? "\\b":"";
        patternText = prefixPattern + patternText + suffixPattern;
        pattern = ignoreCaseChk.isSelected() ?
                  Pattern.compile(patternText, Pattern.CASE_INSENSITIVE) :
                  Pattern.compile(patternText);

      } catch (Exception ree) {
        JOptionPane.showMessageDialog(searchDialog,
                "Invalid pattern!\n" + ree.toString(), "GATE",
                JOptionPane.ERROR_MESSAGE);
        res = false;
      }
      return res;
    }

    protected void refresh() {
      String patternText = patternTextField.getText();

      if (patternText != null && patternText.length() > 0) {
        //update actions state
        findFirstAction.setEnabled(true);
        findNextAction.setEnabled(true);
      } else {
        findFirstAction.setEnabled(false);
        findNextAction.setEnabled(false);
      }

      if (pattern == null) {}
    }

    // FIXME: that's ugly !!
    javax.swing.text.JTextComponent textPane =
      (javax.swing.text.JTextComponent)
      ((javax.swing.JViewport)
              ((JScrollPane)getCentralView().getGUI())
              .getViewport()).getView();

    JTextField patternTextField;
    JCheckBox ignoreCaseChk;
    JCheckBox wholeWordsChk;
    JCheckBox regularExpressionChk;
    JCheckBox highlightsChk;
    Pattern pattern;
    int nextMatchStartsFrom;
    String content;
    Action findFirstAction;
    Action findNextAction;
    Action cancelAction;

  } // end of class SearchDialog

  protected class ViewButton extends JToggleButton{
    public ViewButton(DocumentView aView, String name){
      super();
      setSelected(false);
//      setBorder(null);
      this.view = aView;
      setText(name);
      
//      if(aView.getType() == DocumentView.HORIZONTAL){
//        setText(name);
//      }else if(aView.getType() == DocumentView.CENTRAL){
//        setIcon(new VerticalTextIcon(this, name, VerticalTextIcon.ROTATE_LEFT));
//      }else if(aView.getType() == DocumentView.VERTICAL){
//        setIcon(new VerticalTextIcon(this, name, 
//                										 VerticalTextIcon.ROTATE_RIGHT));
//      }
      
      addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent evt){
          if(isSelected()){
            //show this new view
            switch(view.getType()){
              case DocumentView.CENTRAL:
                setCentralView(centralViews.indexOf(view));
                break;
              case DocumentView.VERTICAL:
                setRightView(verticalViews.indexOf(view));
                break;
              case DocumentView.HORIZONTAL:
//                if(ViewButton.this.getParent() == topBar){
//                  setTopView(horizontalViews.indexOf(view));
//                }else{
                  setBottomView(horizontalViews.indexOf(view));
//                }
                break;
            }
          }else{
            //hide this view
            switch(view.getType()){
              case DocumentView.CENTRAL:
                setCentralView(-1);
                break;
              case DocumentView.VERTICAL:
                setRightView(-1);
                break;
              case DocumentView.HORIZONTAL:
//                if(ViewButton.this.getParent() == topBar){
//                  setTopView(-1);
//                }else{
                  setBottomView(-1);
//                }
                break;
            }
          }
        }
      });
    }
    
    public void updateSelected(){
      switch(view.getType()){
        case DocumentView.CENTRAL:
          setSelected(getCentralView() == view);
          break;
        case DocumentView.VERTICAL:
          setSelected(getRightView() == view);
          break;
        case DocumentView.HORIZONTAL:
//          if(ViewButton.this.getParent() == topBar){
//            setSelected(getTopView() == view);
//          }else{
            setSelected(getBottomView() == view);
//          }
          break;
      }
    }
    DocumentView view;
  }

  protected JSplitPane horizontalSplit;
  protected JSplitPane topSplit;
  protected JSplitPane bottomSplit;

  /** The dialog used for text search */
  private SearchDialog searchDialog;

  /**
   * Cahced value for the selected annotations.
   */
  private List<AnnotationData> selectedAnnotations = new ArrayList<AnnotationData>();
  
  protected JToolBar topBar;
//  protected JToolBar rightBar;
//  protected JToolBar leftBar;
//  protected JToolBar bottomBar;

  protected Document document;


  /**
   * A list of {@link DocumentView} objects of type {@link DocumentView#CENTRAL}
   */
  protected List<DocumentView> centralViews;
  
  /**
   * A list of {@link DocumentView} objects of type 
   * {@link DocumentView#VERTICAL}
   */
  protected List<DocumentView> verticalViews;

  /**
   * A list of {@link DocumentView} objects of type 
   * {@link DocumentView#HORIZONTAL}
   */
  protected List<DocumentView> horizontalViews;

  /**
   * The index in {@link #centralViews} of the currently active central view.
   * <code>-1</code> if none is active.
   */
  protected int centralViewIdx = -1;

  /**
   * The index in {@link #verticalViews} of the currently active right view.
   * <code>-1</code> if none is active.
   */
  protected int rightViewIdx = -1;
  
  /**
   * The index in {@link #horizontalViews} of the currently active top view.
   * <code>-1</code> if none is active.
   */
  protected int topViewIdx = -1;
  
  /**
   * The index in {@link #horizontalViews} of the currently active bottom view.
   * <code>-1</code> if none is active.
   */
  protected int bottomViewIdx = -1;
  
  protected boolean viewsInited = false;

  /**
   * Used to know the last F-key used when adding a new view.
   */
  protected int numberOfTheFKeyforLastView = 2;
}