package gate.creole.ontology.ocat;

import gate.creole.ontology.OClass;
import gate.creole.ontology.OConstants;
import gate.creole.ontology.OInstance;
import gate.creole.ontology.OResource;
import gate.creole.ontology.OntologyUtilities;
import gate.creole.ontology.URI;
import gate.gui.MainFrame;
import gate.gui.ontology.ValuesSelectionAction;
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
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import com.ontotext.gate.vr.ClassNode;

/**
 * Action to be taken when user hovers the mouse over the text selected
 * for the new Instance annotation
 */
public class NewInstanceAnnotationAction extends AbstractAction {

  /**
   * Button to select ontology classes to whom the instance will belong,
   * add Single Annotation, add annotations over all similar text, to
   * cancel the new annotation window.
   */
  private JButton selectClasses, addAnnotation, addAll, cancel;

  /**
   * Serial version ID
   */
  private static final long serialVersionUID = 3256999939094623287L;

  private String field = new String();

  private OntologyTreePanel ontologyTreePanel;

  /**
   * New Annotation window
   */
  private JWindow newInstanceAnntationWindow;

  private String newInstanceString = null;

  /**
   * Constructor
   * 
   */
  public NewInstanceAnnotationAction(OntologyTreePanel ontoTreePanel) {
    this.ontologyTreePanel = ontoTreePanel;

    selectClasses = new JButton("Instance Classes");
    addAnnotation = new JButton(new AddAnnotationBtnAction(MainFrame
            .getIcon("annotation")));
    addAnnotation.setBorderPainted(false);
    addAnnotation.setContentAreaFilled(false);
    addAnnotation.setMargin(new Insets(0, 0, 0, 0));
    addAnnotation.setToolTipText("Apply");

    addAll = new JButton(new AddAllBtnAction(MainFrame.getIcon("annotation")));
    addAll.setBorderPainted(false);
    addAll.setContentAreaFilled(false);
    addAll.setMargin(new Insets(0, 0, 0, 0));
    addAll.setToolTipText("Apply To All");

    cancel = new JButton(new CancelBtnAction(MainFrame.getIcon("exit")));
    cancel.setBorderPainted(false);
    cancel.setContentAreaFilled(false);
    cancel.setMargin(new Insets(0, 0, 0, 0));
    cancel.setToolTipText("Close Window");
    classAction = new ValuesSelectionAction();

    selectClasses.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        TreeSet<String> allClasses = new TreeSet<String>(ontologyTreePanel
                .getAllClassNames());
        allClasses.removeAll(classes);
        classAction.showGUI("Select Instance Classes", allClasses
                .toArray(new String[allClasses.size()]), classes
                .toArray(new String[classes.size()]));
      }
    });
  }

  private int textLocation;

  private int startOffset;

  private int endOffset;

  private TreeSet<String> classes;

  private ValuesSelectionAction classAction;

  public void actionPerformed(ActionEvent e) {
    // the first thing we need to check is if we want to show the
    // newAnnotationWindow
    // check if the instance lookup is enabled
    // if so simply return
    if(!ontologyTreePanel.instances.isSelected()) return;

    JTextArea textPane = ontologyTreePanel.ontoViewer.documentTextArea;

    // now we need to show the new instance annotation window
    // if the window is already being shown then simply change its
    if(ontologyTreePanel.showingNewInstanceAnnotationWindow) {
      try {
        if(textPane.getSelectedText() != null
                && textPane.getSelectedText().length() > 0) {
          if(startOffset == textPane.getSelectionStart()
                  && endOffset == textPane.getSelectionEnd()) {
            newInstanceString = textPane.getSelectedText();
            return;
          }
        }

        Rectangle startRect = textPane.modelToView(textLocation);
        Point topLeft = textPane.getLocationOnScreen();
        JTextArea textComp = ontologyTreePanel.ontoViewer.documentTextArea;
        FontMetrics fm = textComp.getFontMetrics(textComp.getFont());
        int charHeight = fm.getAscent() + fm.getDescent();

        int x = topLeft.x + startRect.x;
        int y = topLeft.y + startRect.y + charHeight; // endRect.y +
        // endRect.height;
        if(newInstanceAnntationWindow.getX() == x
                && newInstanceAnntationWindow.getY() == y) {
          // do nothing
          return;
        }
        newInstanceAnntationWindow.setLocation(x, y);
        newInstanceAnntationWindow.pack();
        newInstanceAnntationWindow.setVisible(true);
      }
      catch(Exception e1) {
        e1.printStackTrace();
        return;
      }
    }
    else {

      if(newInstanceAnntationWindow != null
              && newInstanceAnntationWindow.isVisible()) {
        return;
      }

      if(textPane.getSelectedText() != null
              && textPane.getSelectedText().length() > 0) {
        startOffset = textPane.getSelectionStart();
        endOffset = textPane.getSelectionEnd();
        newInstanceString = textPane.getSelectedText();
      }
      else {
        // he/she is also allowed to create an instance from the
        // selected
        // text
        int[] offsets = ontologyTreePanel.instanceLookupAction
                .getOffsets(textLocation);
        if(offsets == null) {
          return;
        }

        newInstanceString = ontologyTreePanel.instanceLookupAction
        .lookupValue(textLocation);
        startOffset = offsets[0];
        endOffset = offsets[1];
        ontologyTreePanel.ontoViewer.documentTextArea
                .setSelectionStart(startOffset);
        ontologyTreePanel.ontoViewer.documentTextArea
                .setSelectionEnd(endOffset);
      }

      ClassNode aNode = ontologyTreePanel.getNode(newInstanceString.replaceAll(" ","_"));
      if(aNode != null && aNode.getSource() instanceof OClass) {
        JOptionPane.showMessageDialog(MainFrame.getInstance(), "There exists an ontology class with the name \""+newInstanceString+"\"\n Please annotate it as a class annotation!");
        return;
      }

      // otherwise create the window and show it
      classes = new TreeSet<String>();
      if(ontologyTreePanel.currentPropValuesAndInstances2ClassesMap
              .containsKey(newInstanceString)) {
        Set<OClass> oclasses = ontologyTreePanel.currentPropValuesAndInstances2ClassesMap
                .get(newInstanceString);
        classes.clear();
        for(OClass aClass : oclasses) {
          classes.add(aClass.getName());
        }
      }

      newInstanceAnntationWindow = new JWindow(
              SwingUtilities
                      .getWindowAncestor(ontologyTreePanel.ontoViewer.documentTextualDocumentView
                              .getGUI()));
      JPanel pane = new JPanel();
      pane.setBackground(UIManager.getLookAndFeelDefaults().getColor(
              "ToolTip.background"));
      pane.setOpaque(true);
      newInstanceAnntationWindow.setContentPane(pane);
      pane.setLayout(new BorderLayout());
      JPanel subPanel = new JPanel();
      subPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
      subPanel.add(selectClasses);
      subPanel.add(addAnnotation);
      subPanel.add(addAll);
      subPanel.setOpaque(false);
      JPanel labelPanel = new JPanel(new GridLayout(1, 2));
      labelPanel.add(new JLabel("New Instance Annotation"));
      JPanel cancelPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
      cancelPanel.setOpaque(false);
      cancelPanel.add(cancel);
      labelPanel.add(cancelPanel);
      labelPanel.setOpaque(false);
      pane.add(labelPanel, BorderLayout.NORTH);
      pane.add(subPanel, BorderLayout.SOUTH);
      pane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
      newInstanceAnntationWindow.pack();
      newInstanceAnntationWindow.validate();

      try {
        Rectangle startRect = textPane.modelToView(startOffset);
        Point topLeft = textPane.getLocationOnScreen();
        JTextArea textComp = ontologyTreePanel.ontoViewer.documentTextArea;
        FontMetrics fm = textComp.getFontMetrics(textComp.getFont());
        int charHeight = fm.getAscent() + fm.getDescent();

        int x = topLeft.x + startRect.x;
        int y = topLeft.y + startRect.y + charHeight; // endRect.y +
        // endRect.height;
        newInstanceAnntationWindow.setLocation(x, y);
        newInstanceAnntationWindow.pack();
        newInstanceAnntationWindow.setVisible(true);
        ontologyTreePanel.showingNewInstanceAnnotationWindow = true;
      }
      catch(BadLocationException ble) {
        throw new GateRuntimeException("Can't show the window ", ble);
      }
    }
  }

  public void hideWindow() {
    if(newInstanceAnntationWindow != null)
      newInstanceAnntationWindow.setVisible(false);
  }

  public void setTextLocation(int textLocation) {
    this.textLocation = textLocation;
  }

  class AddAnnotationBtnAction extends AbstractAction {
    public AddAnnotationBtnAction(Icon icon) {
      super("", icon);
    }

    public void actionPerformed(ActionEvent ae) {

      // lets check if the resource exists with the newInstanceName
      newInstanceString = newInstanceString.replaceAll(" ", "_");
      
      
      ClassNode aNode = ontologyTreePanel.getNode(newInstanceString);
      
      newInstanceString = aNode == null ? newInstanceString : aNode.toString();
      
      URI aURI = OntologyUtilities.createURI(ontologyTreePanel
              .getCurrentOntology(), newInstanceString, false);

      if(aNode == null || (!(aNode.getSource() instanceof OClass))) {
        for(String aClassName : classAction.getSelectedValues()) {
          OResource aResource = ontologyTreePanel.getCurrentOntology()
                  .getOResourceByName(aClassName);
          if(aResource != null && aResource instanceof OClass) {
            ontologyTreePanel.getCurrentOntology().addOInstance(aURI,
                    (OClass)aResource);
          }
        }
      }

      if(aNode == null) {
        aNode = ontologyTreePanel.getNode(newInstanceString);
      }

      if(aNode != null) {
        ontologyTreePanel.ontoTreeListener.addNewAnnotation(aNode, false, null,
                false);
      }

      ontologyTreePanel.showingNewInstanceAnnotationWindow = false;
      newInstanceAnntationWindow.setVisible(false);
      ontologyTreePanel.ontoViewer.documentTextArea.requestFocus();

    }
  }

  class AddAllBtnAction extends AbstractAction {
    public AddAllBtnAction(Icon icon) {
      super("All", icon);
    }

    public void actionPerformed(ActionEvent ae) {

      // lets check if the resource exists with the newInstanceName
      newInstanceString = newInstanceString.replaceAll(" ", "_");
      
      ClassNode aNode = ontologyTreePanel.getNode(newInstanceString);
      newInstanceString = aNode == null ? newInstanceString : aNode.toString();
      
      URI aURI = OntologyUtilities.createURI(ontologyTreePanel
              .getCurrentOntology(), newInstanceString, false);

      if(aNode == null || (!(aNode.getSource() instanceof OClass))) {
        for(String aClassName : classAction.getSelectedValues()) {
          OResource aResource = ontologyTreePanel.getCurrentOntology()
                  .getOResourceByName(aClassName);
          if(aResource != null && aResource instanceof OClass) {
            ontologyTreePanel.getCurrentOntology().addOInstance(aURI,
                    (OClass)aResource);
          }
        }
      }

      if(aNode == null) {
        aNode = ontologyTreePanel.getNode(newInstanceString);
      }

      if(aNode != null) {
        ontologyTreePanel.ontoTreeListener.addNewAnnotation(aNode, true, null,
                false);
      }

      ontologyTreePanel.showingNewInstanceAnnotationWindow = false;
      newInstanceAnntationWindow.setVisible(false);
      ontologyTreePanel.ontoViewer.documentTextArea.requestFocus();

    }

  }

  class CancelBtnAction extends AbstractAction {
    public CancelBtnAction(Icon icon) {
      super("", icon);
    }

    public void actionPerformed(ActionEvent ae) {
      ontologyTreePanel.ontoViewer.documentTextArea
              .setSelectionStart(ontologyTreePanel.ontoViewer.documentTextArea
                      .getSelectionStart());
      ontologyTreePanel.ontoViewer.documentTextArea
              .setSelectionEnd(ontologyTreePanel.ontoViewer.documentTextArea
                      .getSelectionStart());
      ontologyTreePanel.showingNewInstanceAnnotationWindow = false;
      newInstanceAnntationWindow.setVisible(false);
      ontologyTreePanel.ontoViewer.documentTextArea.requestFocus();
    }
  }
}
