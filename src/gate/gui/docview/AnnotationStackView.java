/*
 *  Copyright (c) 1998-2009, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Thomas Heitz - 7 July 2009
 *
 *  $Id$
 */

package gate.gui.docview;

import gate.event.AnnotationListener;
import gate.event.AnnotationEvent;
import gate.gui.annedit.AnnotationData;
import gate.*;
import gate.util.InvalidOffsetException;
import gate.util.OffsetComparator;
import gate.gui.docview.AnnotationSetsView.*;
import gate.gui.docview.AnnotationStack.*;
import gate.gui.MainFrame;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.Timer;

/**
 * Show a stack view of highlighted annotations in the document
 * centred on the first selected annotation.
 *
 * When double clicked, an annotation is copied to another set in order
 * to create a gold standard set from several annoatator sets.
 *
 * You can choose to display features with annotations by clicking
 * the first column rectangles.
 */
public class AnnotationStackView  extends AbstractDocumentView
  implements AnnotationListener {

  public AnnotationStackView() {
    typesFeatures = new HashMap<String,String>();
  }

  public void cleanup() {
    super.cleanup();
    textView = null;
  }

  protected void initGUI() {

    //get a pointer to the text view used to display
    //the selected annotations
    Iterator centralViewsIter = owner.getCentralViews().iterator();
    while(textView == null && centralViewsIter.hasNext()){
      DocumentView aView = (DocumentView) centralViewsIter.next();
      if(aView instanceof TextualDocumentView)
        textView = (TextualDocumentView) aView;
    }
    // find the annotation set view associated with the document
    Iterator verticalViewsIter = owner.getVerticalViews().iterator();
    while(annotationSetsView == null && verticalViewsIter.hasNext()){
      DocumentView aView = (DocumentView) verticalViewsIter.next();
      if(aView instanceof AnnotationSetsView)
        annotationSetsView = (AnnotationSetsView) aView;
    }
    document = textView.getDocument();

    mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout());

    // toolbar with previous and next annotation buttons
    JToolBar toolBar = new JToolBar();
    toolBar.setFloatable(false);
    toolBar.add(setLabel = new JLabel());
    toolBar.addSeparator();
    toolBar.add(previousAnnotationAction = new PreviousAnnotationAction());
    previousAnnotationAction.setEnabled(false);
    toolBar.add(nextAnnotationAction = new NextAnnotationAction());
    nextAnnotationAction.setEnabled(false);
    toolBar.addSeparator();
    toolBar.add(destinationLabel = new JLabel());
    destinationLabel.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        askDestinationSet();
      }
    });
    destinationLabel.setToolTipText("Click to change it.");
    mainPanel.add(toolBar, BorderLayout.NORTH);

    stackPanel = new AnnotationStack(75, 20);
    scroller = new JScrollPane(stackPanel);
    scroller.getViewport().setOpaque(true);
    mainPanel.add(scroller, BorderLayout.CENTER);

    initListeners();
  }

  public Component getGUI(){
    return mainPanel;
  }

  protected void initListeners(){

    stackPanel.addAncestorListener(new AncestorListener() {
      public void ancestorAdded(AncestorEvent event) {
        // when the view becomes visible
        updateStackView(owner.getSelectedAnnotations());
      }
      public void ancestorMoved(AncestorEvent event) {
      }
      public void ancestorRemoved(AncestorEvent event) {
      }
    });
  }

  class PreviousAnnotationAction extends AbstractAction {
    public PreviousAnnotationAction() {
      putValue(SMALL_ICON, MainFrame.getIcon("left"));
      putValue(SHORT_DESCRIPTION, "Previous Annotation");
    }
    public void actionPerformed(ActionEvent e) {
      nextAnnotationAction.setEnabled(true);
      SortedSet<Annotation> set =
        new TreeSet<Annotation>(new OffsetComparator());
      set.addAll(selectedAnnotationData.getAnnotationSet().get(
        selectedAnnotationData.getAnnotation().getType()));
      set = set.headSet(selectedAnnotationData.getAnnotation());
      if (set.size() > 0) {
        annotationSetsView.selectAnnotation(set.last(),
          selectedAnnotationData.getAnnotationSet());
      }
      setEnabled(set.size() > 1);
    }
  }

  class NextAnnotationAction extends AbstractAction {
    public NextAnnotationAction() {
      putValue(SMALL_ICON, MainFrame.getIcon("right"));
      putValue(SHORT_DESCRIPTION, "Next Annotation");
    }
    public void actionPerformed(ActionEvent e) {
      previousAnnotationAction.setEnabled(true);
      SortedSet<Annotation> set = new TreeSet<Annotation>(
        Collections.reverseOrder(new OffsetComparator()));
      set.addAll(selectedAnnotationData.getAnnotationSet().get(
        selectedAnnotationData.getAnnotation().getType()));
      set = set.headSet(selectedAnnotationData.getAnnotation());
      if (set.size() > 0) {
        annotationSetsView.selectAnnotation(set.last(),
          selectedAnnotationData.getAnnotationSet());
      }
      setEnabled(set.size() > 1);
    }
  }

  protected void registerHooks() { /* do nothing */ }

  protected void unregisterHooks() { /* do nothing */ }

  public int getType() {
    return HORIZONTAL;
  }

  public void annotationUpdated(AnnotationEvent e) {
    updateStackView(owner.getSelectedAnnotations());
  }

  public void setSelectedAnnotations(List<AnnotationData> selectedAnnots) {
    if (!previousAnnotationAction.isEnabled()
     && !nextAnnotationAction.isEnabled()) {
      previousAnnotationAction.setEnabled(true);
      nextAnnotationAction.setEnabled(true);
    }
    updateStackView(selectedAnnots);
    SwingUtilities.invokeLater(new Runnable() {
    public void run() {
      textView.scrollAnnotationToVisible(
        selectedAnnotationData.getAnnotation());
    }});
  }

  void updateStackView(List<AnnotationData> selectedAnnots) {
    if (selectedAnnots == null || selectedAnnots.isEmpty()) {
      previousAnnotationAction.setEnabled(false);
      nextAnnotationAction.setEnabled(false);
      return;
    }

    // get the first selected annotation
    // as this view focus only on one annotation
    selectedAnnotationData = selectedAnnots.get(0);
    Annotation selectedAnnotation = selectedAnnotationData.getAnnotation();
    AnnotationSet set = selectedAnnotationData.getAnnotationSet();
    setLabel.setText("Selection: " + set.getName() +
      "#" + selectedAnnotation.getType());

    // get the context around the annotation
    int context = 30;
    int startOffset = selectedAnnotation.getStartNode().getOffset().intValue();
    int endOffset = selectedAnnotation.getEndNode().getOffset().intValue();
    String text = "";
    try {
      text = document.getContent().getContent(
        Math.max(0l, startOffset - context),
        Math.min(document.getContent().size(), endOffset + context))
        .toString();
    } catch (InvalidOffsetException e) {
      e.printStackTrace();
    }

    // initialise the annotation stack
    stackPanel.setText(text);
    stackPanel.setStartOffset(startOffset);
    stackPanel.setEndOffset(endOffset);
    stackPanel.setContextSize(context);
    stackPanel.setAnnotationMouseListener(new AnnotationMouseListener());
    stackPanel.setHeaderMouseListener(new HeaderMouseListener());
    stackPanel.clearAllRows();

//    // move the destination set to the end of the list
    List<AnnotationSetsView.SetHandler> sets = annotationSetsView.setHandlers;
//    if (destinationSet != null) {
//      for(SetHandler setHandler : new ArrayList<SetHandler>(sets)) {
//        if (setHandler.set.getName() != null
//         && setHandler.set.getName().equals(destinationSet)) {
//          sets.remove(setHandler);
//          sets.add(setHandler);
//          break;
//        }
//      }
//    }

    // add stack rows and annotations for each selected annotation set
    // in the annotation sets view
    for(SetHandler setHandler : sets) {
      for(TypeHandler typeHandler: setHandler.typeHandlers) {
        if (setHandler.set.getName().equals("")) { continue; }
        if (typeHandler.isSelected()) {
          String feature = (typesFeatures.get(typeHandler.name) == null) ?
            "" : typesFeatures.get(typeHandler.name);
          stackPanel.addRow(typeHandler.name, feature, setHandler.set.getName()
            + "#" + typeHandler.name + (feature.equals("")?"":".") + feature,
            null, AnnotationStack.CROP_MIDDLE);
          Set<Annotation> annotations = setHandler.set.get(typeHandler.name)
            .getContained((long)startOffset-context, (long)endOffset+context);
          for (Annotation annotation : annotations) {
            stackPanel.addAnnotation(annotation);
          }
        }
      }
    }

    stackPanel.drawStack();
  }

  /** @return true if the user input a valid annotation set */
  boolean askDestinationSet() {
    Set<String> sets = new HashSet<String>(document.getAnnotationSetNames());
    JList list = new JList(sets.toArray());
    list.setVisibleRowCount(Math.min(10, sets.size()));
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane scroll = new JScrollPane(list);
    JPanel vspace = new JPanel();
    vspace.setSize(0, 5);
    final JTextField setsTextField = new JTextField("consensus", 15);
    list.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        JList list = (JList) e.getSource();
        if (list.getSelectedValue() != null) {
          setsTextField.setText((String) list.getSelectedValue());
        }
      }
    });
    list.setSelectedValue(destinationSet, true);
    Object[] messageObjects = { "Existing annotation sets:",
      scroll, vspace, "Destination annotation set:", setsTextField };
    String options[] = { "Copy to this destination", "Cancel" };
    JOptionPane optionPane = new JOptionPane(
      messageObjects, JOptionPane.QUESTION_MESSAGE,
      JOptionPane.YES_NO_OPTION, null, options, "Cancel");
    JDialog optionDialog = optionPane.createDialog(
      owner, "Copy annotation to another set");
    setsTextField.requestFocus();
    optionDialog.setVisible(true);
    Object selectedValue = optionPane.getValue();
    if (selectedValue == null
     || selectedValue.equals("Cancel")
     || setsTextField.getText().trim().length() == 0) {
      return false;
    }
    destinationSet = setsTextField.getText();
    destinationLabel.setText("Destination: " + destinationSet);
    return true;
  }

  class AnnotationMouseListener extends StackMouseListener {

    public AnnotationMouseListener() {
    }

    public AnnotationMouseListener(String annotationId) {
      for (String setName : document.getAnnotationSetNames()) {
        AnnotationSet set = document.getAnnotations(setName);
        annotation = set.get(Integer.valueOf(annotationId));
        if (annotation != null) {
          break;
        }
      }
    }

    public MouseInputAdapter createListener(String... parameters) {
      switch(parameters.length) {
        case 2:
          return new AnnotationMouseListener(parameters[1]);
        case 4:
          return new AnnotationMouseListener(parameters[3]);
        default:
          return null;
      }
    }

    public void mouseClicked(MouseEvent e) {
      if (e.getButton() != MouseEvent.BUTTON1
       || e.getClickCount() != 2) { return; }

      if (destinationSet == null) {
        if (!askDestinationSet()) { return; }
      }

      // copy the annotation to the destination annotation set
      document.getAnnotations(destinationSet).add(annotation);

      SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        // wait some time
        Date timeToRun = new Date(System.currentTimeMillis() + 1000);
        Timer timer = new Timer("Annotation stack view timer", true);
        timer.schedule(new TimerTask() {
          public void run() {
            // select the annotation type in the destination set
            annotationSetsView.setTypeSelected(
              destinationSet, annotation.getType(), true);
            // select the new annotation and update the stack view
            annotationSetsView.selectAnnotation(annotation,
              document.getAnnotations(destinationSet));
          }
        }, timeToRun);
      }});
    }

    public void mouseEntered(MouseEvent e) {
      dismissDelay = toolTipManager.getDismissDelay();
      initialDelay = toolTipManager.getInitialDelay();
      reshowDelay = toolTipManager.getReshowDelay();
      enabled = toolTipManager.isEnabled();
      Component component = e.getComponent();
      if (!isTooltipSet && component instanceof JLabel) {
        isTooltipSet = true;
        JLabel label = (JLabel) component;
        String toolTip = (label.getToolTipText() == null) ?
          "" : label.getToolTipText();
        toolTip = toolTip.replaceAll("</?html>", "");
        toolTip = "<html>" + (toolTip.length() == 0 ? "" : toolTip + "<br>")
          + "Double click to copy this annotation.</html>";
        label.setToolTipText(toolTip);
      }
      // make the tooltip indefinitely shown when the mouse is over
      toolTipManager.setDismissDelay(Integer.MAX_VALUE);
      toolTipManager.setInitialDelay(0);
      toolTipManager.setReshowDelay(0);
      toolTipManager.setEnabled(true);
    }

    public void mouseExited(MouseEvent e) {
      toolTipManager.setDismissDelay(dismissDelay);
      toolTipManager.setInitialDelay(initialDelay);
      toolTipManager.setReshowDelay(reshowDelay);
      toolTipManager.setEnabled(enabled);
    }

    ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
    int dismissDelay, initialDelay, reshowDelay;
    boolean enabled;
    Annotation annotation;
    boolean isTooltipSet = false;
  }

  protected class HeaderMouseListener extends StackMouseListener {

    public HeaderMouseListener() {
    }

    public HeaderMouseListener(String type, String feature) {
      this.type = type;
      this.feature = feature;
      init();
    }

    public HeaderMouseListener(String type) {
      this.type = type;
      init();
    }

    void init() {
      mainPanel.addAncestorListener(new AncestorListener() {
        public void ancestorMoved(AncestorEvent event) {}
        public void ancestorAdded(AncestorEvent event) {}
        public void ancestorRemoved(AncestorEvent event) {
          // no parent so need to be disposed explicitly
          if (popupWindow != null) { popupWindow.dispose(); }
        }
      });
    }

    public MouseInputAdapter createListener(String... parameters) {
      switch(parameters.length) {
        case 1:
          return new HeaderMouseListener(parameters[0]);
        case 2:
          return new HeaderMouseListener(parameters[0], parameters[1]);
        default:
          return null;
      }
    }

    // when double clicked shows a list of features for this annotation type
    public void mouseClicked(MouseEvent e) {
      if (popupWindow != null && popupWindow.isVisible()) {
        popupWindow.dispose();
        return;
      }
      if (e.getButton() != MouseEvent.BUTTON1
       || e.getClickCount() != 2) { return; }
      // get a list of features for the current annotation type
      TreeSet<String> features = new TreeSet<String>();
      for (String setName : document.getAnnotationSetNames()) {
        int count = 0;
        for (Annotation annotation :
          document.getAnnotations(setName).get(type)) {
          for (Object feature : annotation.getFeatures().keySet()) {
            features.add((String) feature);
          }
          count++; // checks only the 50 first annotations per set
          if (count == 50) { break; } // to avoid slowing down
        }
      }
      features.add(" ");
      // create the list component
      final JList list = new JList(features.toArray());
      list.setVisibleRowCount(Math.min(8, features.size()));
      list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      list.setBackground(Color.WHITE);
      list.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          if (e.getClickCount() == 1) {
            String feature = (String) list.getSelectedValue();
            if (feature.equals(" ")) {
              typesFeatures.remove(type);
            } else {
              typesFeatures.put(type, feature);
            }
            popupWindow.setVisible(false);
            popupWindow.dispose();
            updateStackView(owner.getSelectedAnnotations());
          }
        }
      });
      // create the window that will contain the list
      popupWindow = new JWindow();
      popupWindow.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
          if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            popupWindow.setVisible(false);
            popupWindow.dispose();
          }
        }
      });
      popupWindow.add(new JScrollPane(list));
      Component component = e.getComponent();
      popupWindow.setBounds(
        component.getLocationOnScreen().x,
        component.getLocationOnScreen().y + component.getHeight(),
        component.getWidth(),
        Math.min(8*component.getHeight(), features.size()*component.getHeight()));
      popupWindow.pack();
      popupWindow.setVisible(true);
      SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        String newFeature = typesFeatures.get(type);
        if (newFeature == null) { newFeature = " "; }
        list.setSelectedValue(newFeature, true);
        popupWindow.requestFocusInWindow();
      }});
    }

    public void mouseEntered(MouseEvent e) {
      Component component = e.getComponent();
      if (component instanceof JLabel
      && ((JLabel)component).getToolTipText() == null) {
        ((JLabel)component).setToolTipText("Double click to choose a feature.");
      }
    }

    String type;
    String feature;
    JWindow popupWindow;
  }

  JLabel setLabel;
  JLabel destinationLabel;
  AnnotationStack stackPanel;
  JScrollPane scroller;
  JPanel mainPanel;
  TextualDocumentView textView;
  AnnotationSetsView annotationSetsView;
  String destinationSet;
  Document document;
  AnnotationData selectedAnnotationData;
  PreviousAnnotationAction previousAnnotationAction;
  NextAnnotationAction nextAnnotationAction;
  Map<String,String> typesFeatures;
}
