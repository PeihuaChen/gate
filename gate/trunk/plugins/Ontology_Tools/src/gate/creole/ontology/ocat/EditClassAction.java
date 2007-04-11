package gate.creole.ontology.ocat;

import gate.Annotation;
import gate.FeatureMap;
import gate.Main;
import gate.creole.ANNIEConstants;
import gate.gui.MainFrame;
import gate.util.GateRuntimeException;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

/**
 * Action to be taken when user hovers the mouse over the selected
 * annotations
 */
public class EditClassAction extends AbstractAction {
  /**
   * Serial Version ID
   */
  private static final long serialVersionUID = 3257006557656004657L;

  private int textLocation;

  private Point mousePoint;

  private OntologyTreePanel ontologyTreePanel;

  /**
   * Keeps the track of the selected index of the class in the new
   * annotation window.
   */
  private int selectedClassIndex = 0;

  /**
   * A window that is shown for changing the type of an existing
   * annotation.
   */
  private JWindow editClassWindow;

  /**
   * Constructor
   * 
   * @param ontoTreePanel
   */
  public EditClassAction(OntologyTreePanel ontoTreePanel) {
    this.ontologyTreePanel = ontoTreePanel;
  }

  public void actionPerformed(ActionEvent e) {
    if(ontologyTreePanel.instances.isSelected()) return;

    int[] range = ontologyTreePanel.ontoTreeListener.annotationRange;
    int index1 = -1;
    ArrayList<Integer> indexes = new ArrayList<Integer>();
    if(range != null) {
      for(int i = 0; i < range.length; i += 2) {
        if(textLocation >= range[i] && textLocation <= range[i + 1]) {
          index1 = (i == 0) ? i : i / 2;
          indexes.add(new Integer(index1));
        }
      }
    }
    final ArrayList<Integer> indexes1 = indexes;

    // yes it is put on highlighted so show the edit class window
    if(range != null && indexes.size() > 0) {
      if(ontologyTreePanel.showingEditOResourceWindow) {
        gate.Annotation annotation = ontologyTreePanel.ontoTreeListener.highlightedAnnotations
                .get(indexes.get(0).intValue());
        try {
          JTextArea textPane = ontologyTreePanel.ontoViewer.documentTextArea;
          Rectangle startRect = textPane.modelToView(annotation.getStartNode()
                  .getOffset().intValue());
          Rectangle endRect = textPane.modelToView(annotation.getEndNode()
                  .getOffset().intValue());
          Point topLeft = textPane.getLocationOnScreen();
          JTextArea textComp = ontologyTreePanel.ontoViewer.documentTextArea;
          FontMetrics fm = textComp.getFontMetrics(textComp.getFont());
          int charHeight = fm.getAscent() + fm.getDescent();

          int x = topLeft.x + startRect.x;
          int y = topLeft.y + startRect.y + charHeight; // endRect.y +
          // endRect.height;

          // int x = topLeft.x + startRect.x;
          // int y = topLeft.y + endRect.y + endRect.height;
          if(editClassWindow.getX() == x && editClassWindow.getY() == y) {
            // do nothing
            return;
          }
          else {
            ontologyTreePanel.showingEditOResourceWindow = false;
            editClassWindow.setVisible(false);
            ontologyTreePanel.ontoViewer.documentTextArea.requestFocus();
          }
        }
        catch(BadLocationException e1) {
          throw new GateRuntimeException("Can't show the popup window", e1);
        }
      }

      editClassWindow = new JWindow(
              SwingUtilities
                      .getWindowAncestor(ontologyTreePanel.ontoViewer.documentTextualDocumentView
                              .getGUI()));
      gate.Annotation annot = ontologyTreePanel.ontoTreeListener.highlightedAnnotations
              .get(indexes.get(0).intValue());
      // ok we need to find out classes
      final ArrayList<String> classValues = new ArrayList<String>();
      for(int i = 0; i < indexes.size(); i++) {
        gate.Annotation tempAnnot = ontologyTreePanel.ontoTreeListener.highlightedAnnotations
                .get(indexes.get(i).intValue());
        classValues.add(Utils.getClassOrInstanceFeatureValue(tempAnnot));
      }

      if(classValues.size() == 1) {
        selectedClassIndex = indexes.get(0).intValue();
        showWindow(classValues.get(0), annot.getFeatures().containsKey(
                ANNIEConstants.LOOKUP_CLASS_FEATURE_NAME));
        return;
      }

      // so before showing window we need to list all the available
      // classes
      selectedClassIndex = 0;
      final JPopupMenu classLists = new JPopupMenu();
      classLists.setLayout(new GridLayout(classValues.size(), 1));
      for(int i = 0; i < classValues.size(); i++) {
        String preText = ontologyTreePanel.ontoTreeListener.highlightedAnnotations
                .get(i).getFeatures().containsKey(
                        ANNIEConstants.LOOKUP_CLASS_FEATURE_NAME)
                ? "Class : "
                : "Instance : ";
        JButton button = new JButton(preText + classValues.get(i));
        classLists.add(button);
        button.setActionCommand("" + i);
        button.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            final int tempIndex = Integer.parseInt(ae.getActionCommand());
            selectedClassIndex = indexes1.get(tempIndex).intValue();
            classLists.setVisible(false);
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                gate.Annotation tempAnnot = ontologyTreePanel.ontoTreeListener.highlightedAnnotations
                        .get(tempIndex);

                showWindow(classValues.get(tempIndex), tempAnnot.getFeatures()
                        .containsKey(ANNIEConstants.LOOKUP_CLASS_FEATURE_NAME));

              }
            });

          }
        });
      }
      // and finally show it
      classLists.show(ontologyTreePanel.ontoViewer.documentTextArea,
              (int)mousePoint.getX(), (int)mousePoint.getY());
    }
  }

  private void showWindow(final String classValue,
          final boolean isClassAnnotation) {
    ontologyTreePanel.showingEditOResourceWindow = true;
    JPanel pane = new JPanel();
    editClassWindow.setContentPane(pane);
    pane.setOpaque(true);
    pane.setLayout(new BorderLayout());
    pane.setBackground(UIManager.getLookAndFeelDefaults().getColor(
            "ToolTip.background"));
    JButton deleteAnnotation = new JButton(MainFrame
            .getIcon("remove-annotation"));
    deleteAnnotation.setBorderPainted(false);
    deleteAnnotation.setContentAreaFilled(false);
    deleteAnnotation.setMargin(new Insets(0, 0, 0, 0));
    deleteAnnotation.setToolTipText("Delete Annotation");

    JButton deleteAllAnnotations = new JButton("All", MainFrame
            .getIcon("remove-annotation"));
    deleteAllAnnotations.setBorderPainted(false);
    deleteAllAnnotations.setContentAreaFilled(false);
    deleteAllAnnotations.setMargin(new Insets(0, 0, 0, 0));
    deleteAllAnnotations.setToolTipText("Delete All Annotation");

    JButton startOffsetExtendLeft = new JButton(MainFrame
            .getIcon("extend-left"));
    startOffsetExtendLeft.setBorderPainted(false);
    startOffsetExtendLeft.setContentAreaFilled(false);
    startOffsetExtendLeft.setMargin(new Insets(0, 0, 0, 0));
    startOffsetExtendLeft.setToolTipText("Extend StartOffset");
    JButton startOffsetExtendRight = new JButton(MainFrame
            .getIcon("extend-right"));
    startOffsetExtendRight.setBorderPainted(false);
    startOffsetExtendRight.setContentAreaFilled(false);
    startOffsetExtendRight.setMargin(new Insets(0, 0, 0, 0));
    startOffsetExtendRight.setToolTipText("Shrink StartOffset");
    JButton endOffsetExtendLeft = new JButton(MainFrame.getIcon("extend-left"));
    endOffsetExtendLeft.setBorderPainted(false);
    endOffsetExtendLeft.setContentAreaFilled(false);
    endOffsetExtendLeft.setMargin(new Insets(0, 0, 0, 0));
    endOffsetExtendLeft.setToolTipText("Shrink EndOffset");
    JButton endOffsetExtendRight = new JButton(MainFrame
            .getIcon("extend-right"));
    endOffsetExtendRight.setBorderPainted(false);
    endOffsetExtendRight.setContentAreaFilled(false);
    endOffsetExtendRight.setMargin(new Insets(0, 0, 0, 0));
    endOffsetExtendRight.setToolTipText("Extend EndOffset");
    // what to do when user selects to remove the annotation
    deleteAnnotation.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          if(ontologyTreePanel.ontologyViewerOptions.getDeleteConfirmation()) {
            Object[] options = new Object[] {"YES", "NO"};
            int confirm = JOptionPane.showOptionDialog(Main.getMainFrame(),
                    "Delete Annotation : Are you sure?",
                    "Delete Annotation Confirmation",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            if(confirm == JOptionPane.YES_OPTION) {
              gate.Annotation annot = (gate.Annotation)ontologyTreePanel.ontoTreeListener.highlightedAnnotations
                      .get(selectedClassIndex);
              if(annot != null) {
                ontologyTreePanel.deleteAnnotation(annot);
              }
              editClassWindow.setVisible(false);
              ontologyTreePanel.ontoViewer.documentTextArea.requestFocus();
            }
            else {
              editClassWindow.setVisible(false);
              ontologyTreePanel.ontoViewer.documentTextArea.requestFocus();
            }
          }
          else {
            gate.Annotation annot = ontologyTreePanel.ontoTreeListener.highlightedAnnotations
                    .get(selectedClassIndex);
            ontologyTreePanel.deleteAnnotation(annot);
            editClassWindow.setVisible(false);
            ontologyTreePanel.ontoViewer.documentTextArea.requestFocus();
          }
        }
        catch(Exception e1) {
          e1.printStackTrace();
        }
      }
    });
    // extend the annotation by one character on left
    startOffsetExtendLeft.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          gate.Annotation annot = ontologyTreePanel.ontoTreeListener.highlightedAnnotations
                  .get(selectedClassIndex);
          int startOffset = annot.getStartNode().getOffset().intValue();
          int endOffset = annot.getEndNode().getOffset().intValue();
          FeatureMap features = annot.getFeatures();
          if(startOffset == 0) return;
          startOffset--;
          ontologyTreePanel.deleteAnnotation(annot);
          ontologyTreePanel.ontoViewer.documentTextArea
                  .setSelectionStart(startOffset);
          ontologyTreePanel.ontoViewer.documentTextArea
                  .setSelectionEnd(endOffset);
          Annotation addedAnnotation = ontologyTreePanel.ontoTreeListener
                  .addNewAnnotation(ontologyTreePanel.getNode(classValue),
                          false, features, isClassAnnotation).get(0);
          selectedClassIndex = ontologyTreePanel.ontoTreeListener.highlightedAnnotations
                  .indexOf(addedAnnotation);
        }
        catch(Exception e1) {
          throw new GateRuntimeException(e1);
        }
      }
    });
    // extend the annotation by one character on left
    startOffsetExtendRight.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {

        gate.Annotation annot = ontologyTreePanel.ontoTreeListener.highlightedAnnotations
                .get(selectedClassIndex);
        int startOffset = annot.getStartNode().getOffset().intValue();
        int endOffset = annot.getEndNode().getOffset().intValue();
        FeatureMap features = annot.getFeatures();
        startOffset++;
        if(startOffset == endOffset) return;
        ontologyTreePanel.deleteAnnotation(annot);
        ontologyTreePanel.ontoViewer.documentTextArea
                .setSelectionStart(startOffset);
        ontologyTreePanel.ontoViewer.documentTextArea
                .setSelectionEnd(endOffset);
        Annotation addedAnnotation = ontologyTreePanel.ontoTreeListener
                .addNewAnnotation(ontologyTreePanel.getNode(classValue), false,
                        features, isClassAnnotation).get(0);
        selectedClassIndex = ontologyTreePanel.ontoTreeListener.highlightedAnnotations
                .indexOf(addedAnnotation);
      }
    });
    // extend the annotation by one character on left
    endOffsetExtendLeft.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          gate.Annotation annot = ontologyTreePanel.ontoTreeListener.highlightedAnnotations
                  .get(selectedClassIndex);
          int startOffset = annot.getStartNode().getOffset().intValue();
          int endOffset = annot.getEndNode().getOffset().intValue();
          FeatureMap features = annot.getFeatures();
          endOffset--;
          if(endOffset == startOffset) return;
          ontologyTreePanel.deleteAnnotation(annot);
          ontologyTreePanel.ontoViewer.documentTextArea
                  .setSelectionStart(startOffset);
          ontologyTreePanel.ontoViewer.documentTextArea
                  .setSelectionEnd(endOffset);
          Annotation addedAnnotation = ontologyTreePanel.ontoTreeListener
                  .addNewAnnotation(ontologyTreePanel.getNode(classValue),
                          false, features, isClassAnnotation).get(0);
          selectedClassIndex = ontologyTreePanel.ontoTreeListener.highlightedAnnotations
                  .indexOf(addedAnnotation);
        }
        catch(Exception e1) {
          throw new GateRuntimeException(e1);
        }
      }
    });
    // extend the annotation by one character on left
    endOffsetExtendRight.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        gate.Annotation annot = ontologyTreePanel.ontoTreeListener.highlightedAnnotations
                .get(selectedClassIndex);
        int startOffset = annot.getStartNode().getOffset().intValue();
        int endOffset = annot.getEndNode().getOffset().intValue();
        FeatureMap features = annot.getFeatures();
        if(ontologyTreePanel.ontoViewer.getDocument().getContent().size()
                .longValue() == endOffset) return;
        endOffset++;
        ontologyTreePanel.deleteAnnotation(annot);
        ontologyTreePanel.ontoViewer.documentTextArea
                .setSelectionStart(startOffset);
        ontologyTreePanel.ontoViewer.documentTextArea
                .setSelectionEnd(endOffset);
        Annotation addedAnnotation = ontologyTreePanel.ontoTreeListener
                .addNewAnnotation(ontologyTreePanel.getNode(classValue), false,
                        features, isClassAnnotation).get(0);
        selectedClassIndex = ontologyTreePanel.ontoTreeListener.highlightedAnnotations
                .indexOf(addedAnnotation);
      }
    });

    deleteAllAnnotations.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(ontologyTreePanel.ontologyViewerOptions.getDeleteConfirmation()) {
          Object[] options = new Object[] {"YES", "NO"};
          int confirm = JOptionPane.showOptionDialog(MainFrame.getInstance(),
                  "Delete Annotation : Are you sure?",
                  "Delete Annotation Confirmation",
                  JOptionPane.YES_NO_CANCEL_OPTION,
                  JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
          if(confirm == JOptionPane.YES_OPTION) {
            // we need to find out all annotations with the
            // same string and the same class
            gate.Annotation annot = ontologyTreePanel.ontoTreeListener.highlightedAnnotations
                    .get(selectedClassIndex);
            ArrayList<Annotation> annotations = getSimilarAnnotations(annot);
            for(int i = 0; i < annotations.size(); i++) {
              ontologyTreePanel.deleteAnnotation(annotations.get(i));
            }
            editClassWindow.setVisible(false);
            ontologyTreePanel.ontoViewer.documentTextArea.requestFocus();
          }
          else {
            editClassWindow.setVisible(false);
            ontologyTreePanel.ontoViewer.documentTextArea.requestFocus();
            return;
          }
        }
        else {
          // we need to find out all annotations with the same
          // string and the same class
          gate.Annotation annot = ontologyTreePanel.ontoTreeListener.highlightedAnnotations
                  .get(selectedClassIndex);
          ArrayList<Annotation> annotations = getSimilarAnnotations(annot);
          for(int i = 0; i < annotations.size(); i++) {
            ontologyTreePanel.deleteAnnotation(annotations.get(i));
          }
          editClassWindow.setVisible(false);
          ontologyTreePanel.ontoViewer.documentTextArea.requestFocus();
        }
      }
    });
    // ok and now we need to add the editing feature
    final JPanel editPanel = new JPanel();
    editPanel.setLayout(new BorderLayout());
    final JPanel editSubPanel = new JPanel();
    editSubPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
    final JComboBox list = new JComboBox();
    final TreeSet<String> sortedSet = new TreeSet<String>(ontologyTreePanel
            .getAllClassNames());
    ComboBoxModel model = new DefaultComboBoxModel(sortedSet.toArray());
    list.setModel(model);
    list.setMaximumRowCount(5);
    if(isClassAnnotation)
      editPanel.add(list, BorderLayout.NORTH);
    final JLabel editField = new JLabel();
    final JButton changeClass = new JButton(MainFrame.getIcon("annotation"));
    changeClass.setBorderPainted(false);
    changeClass.setContentAreaFilled(false);
    changeClass.setMargin(new Insets(0, 0, 0, 0));
    changeClass.setToolTipText("Change");

    final JButton applyToAll = new JButton("All", MainFrame
            .getIcon("annotation"));
    applyToAll.setBorderPainted(false);
    applyToAll.setContentAreaFilled(false);
    applyToAll.setMargin(new Insets(0, 0, 0, 0));
    applyToAll.setToolTipText("Apply To All");

    final JButton closeWindow = new JButton(MainFrame.getIcon("exit"));
    closeWindow.setBorderPainted(false);
    closeWindow.setContentAreaFilled(false);
    closeWindow.setMargin(new Insets(0, 0, 0, 0));
    closeWindow.setToolTipText("Close Window");

    editSubPanel.setBackground(UIManager.getLookAndFeelDefaults().getColor(
            "ToolTip.background"));
    editSubPanel.setOpaque(true);
    if(isClassAnnotation) {
      editSubPanel.add(changeClass);
      editSubPanel.add(applyToAll);
      editSubPanel.add(startOffsetExtendLeft);
      editSubPanel.add(startOffsetExtendRight);
      editSubPanel.add(endOffsetExtendLeft);
      editSubPanel.add(endOffsetExtendRight);
    }
    
    editSubPanel.add(deleteAnnotation);
    editSubPanel.add(deleteAllAnnotations);
    editPanel.add(editSubPanel, BorderLayout.SOUTH);
    changeClass.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        if(!ontologyTreePanel.currentOResource2ColorMap.keySet().contains(
                (String)list.getSelectedItem())) {
          JOptionPane.showMessageDialog(null, (isClassAnnotation
                  ? "Class"
                  : "Instance")
                  + " :\""
                  + (String)list.getSelectedItem()
                  + "\" does not exist!");
          return;
        }
        gate.Annotation annot = ontologyTreePanel.ontoTreeListener.highlightedAnnotations
                .get(selectedClassIndex);
        changeClassName((String)list.getSelectedItem(), annot, false);
      }
    });
    applyToAll.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        if(!ontologyTreePanel.currentOResource2ColorMap.keySet().contains(
                (String)list.getSelectedItem())) {
          JOptionPane.showMessageDialog(null, (isClassAnnotation
                  ? "Class"
                  : "Instance")
                  + " :\""
                  + (String)list.getSelectedItem()
                  + "\" does not exist!");
          return;
        }
        gate.Annotation annot = ontologyTreePanel.ontoTreeListener.highlightedAnnotations
                .get(selectedClassIndex);
        changeClassName((String)list.getSelectedItem(), annot, true);
      }
    });
    closeWindow.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        editClassWindow.setVisible(false);
        ontologyTreePanel.ontoViewer.documentTextArea.requestFocus();
      }
    });
    list.setEditable(true);
    list.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent keyevent) {
        String s = ((JTextComponent)list.getEditor().getEditorComponent())
                .getText();
        TreeSet<String> subSortedSet = new TreeSet<String>();
        if(s != null) {
          Iterator<String> classIter = sortedSet.iterator();
          while(classIter.hasNext()) {
            String s1 = classIter.next();
            if(s1.toLowerCase().startsWith(s.toLowerCase())) {
              subSortedSet.add(s1);
            }
          }
        }
        DefaultComboBoxModel defaultcomboboxmodel = new DefaultComboBoxModel(
                subSortedSet.toArray());
        list.setModel(defaultcomboboxmodel);
        try {
          list.showPopup();
        }
        catch(Exception exception) {
        }
        ((JTextComponent)list.getEditor().getEditorComponent()).setText(s);
      }
    });
    ((JTextComponent)list.getEditor().getEditorComponent()).setText("");
    JLabel classLabel = new JLabel(((isClassAnnotation ? "Class" : "Instance")
            + " : " + classValue));
    JPanel topPanel = new JPanel();
    topPanel.setOpaque(false);
    topPanel.setLayout(new GridLayout(1, 2));
    topPanel.add(classLabel);
    JPanel cancelPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    cancelPanel.setOpaque(false);
    cancelPanel.add(closeWindow);
    topPanel.add(cancelPanel);

    pane.add(topPanel, BorderLayout.CENTER);
    pane.add(editPanel, BorderLayout.SOUTH);
    pane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    JTextArea textPane = ontologyTreePanel.ontoViewer.documentTextArea;
    gate.Annotation annotation = ontologyTreePanel.ontoTreeListener.highlightedAnnotations
            .get(selectedClassIndex);
    try {
      Rectangle startRect = textPane.modelToView(annotation.getStartNode()
              .getOffset().intValue());
      Rectangle endRect = textPane.modelToView(annotation.getEndNode()
              .getOffset().intValue());
      Point topLeft = textPane.getLocationOnScreen();
      JTextArea textComp = ontologyTreePanel.ontoViewer.documentTextArea;
      FontMetrics fm = textComp.getFontMetrics(textComp.getFont());
      int charHeight = fm.getAscent() + fm.getDescent();

      int x = topLeft.x + startRect.x;
      int y = topLeft.y + startRect.y + charHeight; // endRect.y +
      // endRect.height;
      editClassWindow.setLocation(x, y);
      editClassWindow.pack();
      editClassWindow.validate();
      textPane.removeAll();
      editClassWindow.setVisible(true);
    }
    catch(BadLocationException e1) {
      // just ignore this
    }
  }

  /**
   * Given the annotation, this method returns the annotation with same
   * text and same class feature.
   * 
   * @param annot
   * @return
   */
  private ArrayList<Annotation> getSimilarAnnotations(gate.Annotation annot) {
    ArrayList<Annotation> annotations = new ArrayList<Annotation>();
    String classValue = Utils.getClassOrInstanceFeatureValue(annot);
    String annotString = getString(annot);
    ArrayList<Annotation> highlightedAnnotations = ontologyTreePanel.ontoTreeListener.highlightedAnnotations;
    for(int i = 0; i < highlightedAnnotations.size(); i++) {
      gate.Annotation temp = highlightedAnnotations.get(i);
      String tempClass = Utils.getClassOrInstanceFeatureValue(temp);
      String tempString = getString(temp);
      if(ontologyTreePanel.ontologyViewerOptions.isAddAllOptionCaseSensitive()) {
        if(classValue.equals(tempClass) && annotString.equals(tempString)) {
          annotations.add(temp);
        }
      }
      else {
        if(classValue.equalsIgnoreCase(tempClass)
                && annotString.equalsIgnoreCase(tempString)) {
          annotations.add(temp);
        }
      }
    }
    return annotations;
  }

  /**
   * Retrieves the underlying text of the annotation.
   * 
   * @param annot
   * @return
   */
  private String getString(gate.Annotation annot) {
    return ontologyTreePanel.ontoViewer.getDocument().getContent().toString()
            .substring(annot.getStartNode().getOffset().intValue(),
                    annot.getEndNode().getOffset().intValue());
  }

  /**
   * This method is called to change the class type
   * 
   * @param newClassName
   * @param indexOfAnnotation
   * @param all
   */
  private void changeClassName(String newClassName, Annotation annot,
          boolean all) {
    if(ontologyTreePanel.showingEditOResourceWindow) {
      ontologyTreePanel.showingEditOResourceWindow = false;
      editClassWindow.setVisible(false);
      ontologyTreePanel.ontoViewer.documentTextArea.requestFocus();
    }
    int start = annot.getStartNode().getOffset().intValue();
    int end = annot.getEndNode().getOffset().intValue();
    ArrayList<Annotation> annots = new ArrayList<Annotation>();
    // now if all is true then we need to find out all the occurances
    // of same text in the document
    if(all) {
      // so first find out the respective text
      String textToSearchIn = ontologyTreePanel.ontoViewer.getDocument()
              .getContent().toString();
      String textToSearch = textToSearchIn.substring(start, end);
      String classToSearch = Utils.getClassOrInstanceFeatureValue(annot);
      ArrayList<Annotation> annotations = ontologyTreePanel.ontoTreeListener.highlightedAnnotations;
      for(int i = 0; i < annotations.size(); i++) {
        String textToMatch = textToSearchIn.substring(annotations.get(i)
                .getStartNode().getOffset().intValue(), annotations.get(i)
                .getEndNode().getOffset().intValue());
        if(textToSearch.equals(textToMatch)
                && classToSearch.equals(Utils
                        .getClassOrInstanceFeatureValue(annotations.get(i)))) {
          annots.add(annotations.get(i));
        }
      }
    }
    else {
      annots.add(annot);
    }
    for(int i = 0; i < annots.size(); i++) {
      // we need to delete this
      Annotation tempAnnot = (gate.Annotation)annots.get(i);
      ontologyTreePanel.deleteAnnotation(tempAnnot);
      start = tempAnnot.getStartNode().getOffset().intValue();
      end = tempAnnot.getEndNode().getOffset().intValue();
      // and add the new annotation
      ontologyTreePanel.ontoViewer.documentTextArea.setSelectionStart(start);
      ontologyTreePanel.ontoViewer.documentTextArea.setSelectionEnd(end);
      ontologyTreePanel.ontoTreeListener.addNewAnnotation(ontologyTreePanel
              .getNode(newClassName), false, tempAnnot.getFeatures(), true);
      ontologyTreePanel.ontoViewer.documentTextArea.setSelectionEnd(start);
    }
  }

  public void hideWindow() {
    if(editClassWindow != null) editClassWindow.setVisible(false);
  }

  /**
   * Set the Text Location respective to the Mouse position
   * 
   * @param textLocation
   */
  public void setTextLocation(int textLocation) {
    this.textLocation = textLocation;
  }

  /**
   * Sets the mouse point
   * 
   * @param point
   */
  public void setMousePoint(Point point) {
    this.mousePoint = point;
  }
}
