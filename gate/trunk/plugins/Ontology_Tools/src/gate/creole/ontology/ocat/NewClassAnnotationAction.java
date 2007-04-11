package gate.creole.ontology.ocat;

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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

/**
 * Action to be taken when user hovers the mouse over the text selected
 * for the new annotation
 */
public class NewClassAnnotationAction extends AbstractAction {

  /**
   * This is used for displaying all available concepts from the
   * ontology.
   */
  private JComboBox ontologyClassesCB;

  /**
   * Button to add Single Annotation, add annotations over all similar
   * text, to cancel the new annotation window.
   */
  private JButton addAnnotation, addAll, cancel;

  /**
   * Serial version ID
   */
  private static final long serialVersionUID = 3256999939094623287L;

  private String field = new String();

  private OntologyTreePanel ontologyTreePanel;

  /**
   * Keeps the record of recently used class.
   */
  private String recentUsedClass = "";

  /**
   * New Annotation window
   */
  private JWindow newClassAnntationWindow;

  /**
   * Constructor
   * 
   */
  public NewClassAnnotationAction(OntologyTreePanel ontoTreePanel) {
    this.ontologyTreePanel = ontoTreePanel;
    ontologyClassesCB = new JComboBox();
    ontologyClassesCB.setMaximumRowCount(5);
    ontologyClassesCB.setEditable(true);

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

    ontologyClassesCB.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent arg0) {
        field = ((JTextComponent)ontologyClassesCB.getEditor()
                .getEditorComponent()).getText();
      }
    });
    ontologyClassesCB.getEditor().getEditorComponent().addKeyListener(
            new KeyAdapter() {
              public void keyReleased(KeyEvent keyevent) {
                String s = ((JTextComponent)ontologyClassesCB.getEditor()
                        .getEditorComponent()).getText();
                TreeSet<String> sortedSet = new TreeSet<String>();
                if(s != null) {
                  Set<String> classes = ontologyTreePanel.getAllClassNames();
                  Iterator<String> classIter = classes.iterator();
                  while(classIter.hasNext()) {
                    String s1 = classIter.next();
                    if(s1.toLowerCase().startsWith(s.toLowerCase())) {
                      sortedSet.add(s1);
                    }
                  }
                }
                DefaultComboBoxModel defaultcomboboxmodel = new DefaultComboBoxModel(
                        sortedSet.toArray());
                ontologyClassesCB.setModel(defaultcomboboxmodel);
                try {
                  ontologyClassesCB.showPopup();
                }
                catch(Exception exception) {
                }
                ((JTextComponent)ontologyClassesCB.getEditor()
                        .getEditorComponent()).setText(s);
              }
            });

  }

  private int textLocation;

  
  public void actionPerformed(ActionEvent e) {
    // the first thing we need to check is if we want to show the
    // newAnnotationWindow
    // check if the instance lookup is enabled
    // if so simply return
    if(ontologyTreePanel.instances.isSelected())
        return;
      
    // the way we do it is
    // step 1 obtain the selected text range
    JTextArea textPane = ontologyTreePanel.ontoViewer.documentTextArea;
    if(textPane.getSelectedText() == null
            || textPane.getSelectedText().length() == 0) return;

    // otherwise
    // check if the mouse is within the range of selected text
    if(textLocation < textPane.getSelectionStart()
            || textLocation > textPane.getSelectionEnd()) {
      return;
    }

    // otherwise we need to show the new annotation window
    // if the window is already being shown then simply change its
    if(ontologyTreePanel.showingNewClassAnnotationWindow) {
      int x1 = textPane.getSelectionStart();
      int y1 = textPane.getSelectionEnd();
      try {
        Rectangle startRect = textPane.modelToView(x1);
        Rectangle endRect = textPane.modelToView(y1);
        Point topLeft = textPane.getLocationOnScreen();
        JTextArea textComp = ontologyTreePanel.ontoViewer.documentTextArea;
        FontMetrics fm = textComp.getFontMetrics(textComp.getFont());
        int charHeight = fm.getAscent() + fm.getDescent();

        int x = topLeft.x + startRect.x;
        int y = topLeft.y + startRect.y + charHeight; // endRect.y +
        // endRect.height;
        if(newClassAnntationWindow.getX() == x && newClassAnntationWindow.getY() == y) {
          // do nothing
          return;
        }
        newClassAnntationWindow.setLocation(x, y);
        newClassAnntationWindow.pack();
        newClassAnntationWindow.setVisible(true);
      }
      catch(Exception e1) {
        e1.printStackTrace();
      }
    }
    else {
      // otherwise create the window and show it
      TreeSet<String> classNames = new TreeSet<String>(ontologyTreePanel.getAllClassNames());
      classNames.remove(ontologyTreePanel.getCurrentOntology().getName());
      ComboBoxModel model = new DefaultComboBoxModel(classNames.toArray());
      ontologyClassesCB.setModel(model);
      if(recentUsedClass.length() > 0) {
        ((JTextComponent)ontologyClassesCB.getEditor().getEditorComponent())
                .setText(recentUsedClass);
      }
      else {
        ((JTextComponent)ontologyClassesCB.getEditor().getEditorComponent())
                .setText("");
      }
      ontologyClassesCB.setBackground(UIManager.getLookAndFeelDefaults()
              .getColor("ToolTip.background"));
      newClassAnntationWindow = new JWindow(
              SwingUtilities
                      .getWindowAncestor(ontologyTreePanel.ontoViewer.documentTextualDocumentView
                              .getGUI()));
      JPanel pane = new JPanel();
      pane.setBackground(UIManager.getLookAndFeelDefaults().getColor(
              "ToolTip.background"));
      pane.setOpaque(true);
      newClassAnntationWindow.setContentPane(pane);
      pane.setLayout(new BorderLayout());
      JPanel subPanel = new JPanel();
      subPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
      subPanel.add(addAnnotation);
      subPanel.add(addAll);
      subPanel.setOpaque(false);
      JPanel labelPanel = new JPanel(new GridLayout(1, 2));
      labelPanel.add(new JLabel("New Annotation"));
      JPanel cancelPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
      cancelPanel.setOpaque(false);
      cancelPanel.add(cancel);
      labelPanel.add(cancelPanel);
      labelPanel.setOpaque(false);
      pane.add(labelPanel, BorderLayout.NORTH);
      pane.add(ontologyClassesCB, BorderLayout.CENTER);
      pane.add(subPanel, BorderLayout.SOUTH);
      pane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
      newClassAnntationWindow.pack();
      newClassAnntationWindow.validate();

      int x1 = textPane.getSelectionStart();
      int y1 = textPane.getSelectionEnd();
      try {
        Rectangle startRect = textPane.modelToView(x1);
        Rectangle endRect = textPane.modelToView(y1);
        Point topLeft = textPane.getLocationOnScreen();
        JTextArea textComp = ontologyTreePanel.ontoViewer.documentTextArea;
        FontMetrics fm = textComp.getFontMetrics(textComp.getFont());
        int charHeight = fm.getAscent() + fm.getDescent();

        int x = topLeft.x + startRect.x;
        int y = topLeft.y + startRect.y + charHeight; // endRect.y +
        // endRect.height;
        newClassAnntationWindow.setLocation(x, y);
        newClassAnntationWindow.pack();
        newClassAnntationWindow.setVisible(true);
        ontologyTreePanel.showingNewClassAnnotationWindow = true;
      }
      catch(BadLocationException ble) {
        throw new GateRuntimeException("Can't show the window ", ble);
      }
    }
  }

  public void hideWindow() {
      if(newClassAnntationWindow != null)
        newClassAnntationWindow.setVisible(false);
  }
  
  public void setTextLocation(int textLocation) {
    this.textLocation = textLocation;
  }

  class AddAnnotationBtnAction extends AbstractAction {
    public AddAnnotationBtnAction(Icon icon) {
      super("", icon);
    }

    public void actionPerformed(ActionEvent ae) {
      ontologyTreePanel.showingNewClassAnnotationWindow = false;
      newClassAnntationWindow.setVisible(false);
      ontologyTreePanel.ontoViewer.documentTextArea.requestFocus();
      if(!ontologyTreePanel.getAllClassNames().contains(
              (String)ontologyClassesCB.getSelectedItem())) {
        JOptionPane.showMessageDialog(null, "Class :\"" + field
                + "\" does not exist!");
        return;
      }
      recentUsedClass = (String)ontologyClassesCB.getSelectedItem();
      ontologyTreePanel.ontoTreeListener.addNewAnnotation(ontologyTreePanel
              .getNode((String)ontologyClassesCB.getSelectedItem()), false,
              null, true);
    }
  }

  class AddAllBtnAction extends AbstractAction {
    public AddAllBtnAction(Icon icon) {
      super("All", icon);
    }

    public void actionPerformed(ActionEvent ae) {
      ontologyTreePanel.showingNewClassAnnotationWindow = false;
      newClassAnntationWindow.setVisible(false);
      ontologyTreePanel.ontoViewer.documentTextArea.requestFocus();
      if(!ontologyTreePanel.getAllClassNames().contains(field)) {
        JOptionPane.showMessageDialog(null, "Class :\"" + field
                + "\" does not exist!");
        return;
      }
      recentUsedClass = field;
      ontologyTreePanel.ontoTreeListener.addNewAnnotation(ontologyTreePanel
              .getNode(field), true, null, true);

    }
  }

  class CancelBtnAction extends AbstractAction {
    public CancelBtnAction(Icon icon) {
      super("", icon);
    }

    public void actionPerformed(ActionEvent ae) {
      ontologyTreePanel.showingNewClassAnnotationWindow = false;
      newClassAnntationWindow.setVisible(false);
      ontologyTreePanel.ontoViewer.documentTextArea.requestFocus();
      ontologyTreePanel.ontoViewer.documentTextArea
              .setSelectionStart(ontologyTreePanel.ontoViewer.documentTextArea
                      .getSelectionStart());
      ontologyTreePanel.ontoViewer.documentTextArea
              .setSelectionEnd(ontologyTreePanel.ontoViewer.documentTextArea
                      .getSelectionStart());

    }
  }

}
