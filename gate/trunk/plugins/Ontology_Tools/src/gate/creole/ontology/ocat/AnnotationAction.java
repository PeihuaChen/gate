/*
 *  AnnotationAction.java
 *
 *  Niraj Aswani, 12/March/07
 *
 *  $Id: AnnotationAction.html,v 1.0 2007/03/12 16:13:01 niraj Exp $
 */
package gate.creole.ontology.ocat;

import javax.swing.event.MouseInputAdapter;
import java.awt.event.*;

/**
 * This class provides the GUI implementation for
 * creating/changing/deleting annotations from the text. It uses
 * OntologyTreePanel to display the list of available classes in the
 * ontology.
 * 
 * @author niraj
 */
public class AnnotationAction extends MouseInputAdapter {

  /**
   * Reference to the main OntologyTreePanel object
   */
  private OntologyTreePanel ontologyTreePanel;

  /**
   * Timer object
   */
  private javax.swing.Timer newClassAnnotationWindowTimer;
  
  /**
   * An instance of editClassWindowTimer, that is used for waiting for
   * sometime.
   */
  private javax.swing.Timer editClassWindowTimer;

  /**
   * An instance of editClassWindowTimer, that is used for waiting for
   * sometime.
   */
  private javax.swing.Timer newInstanceAnnotationWindowTimer;

  /**
   * How long we should wait before showing a new annotation/change
   * annotation window.
   */
  private final int DELAY = 500;

  /**
   * Action that is performed when user decides to create a new
   * annotation.
   */
  private NewClassAnnotationAction newClassAnnotationAction;
  
  /**
   * Action that tells what to do whan a mouse is moved.
   */
  private EditClassAction editClassAction;

  /**
   * Action that is performed when user decides to create a new
   * Instance annotation.
   */
  private NewInstanceAnnotationAction newInstanceAnnotationAction;



  /**
   * Constructor
   * 
   * @param ontologyTreePanel the instance this instance uses to obtain
   *          the information about ontology
   */
  public AnnotationAction(OntologyTreePanel ontoTreePanel) {
    this.ontologyTreePanel = ontoTreePanel;
    editClassAction = new EditClassAction(ontoTreePanel);
    editClassWindowTimer = new javax.swing.Timer(DELAY, editClassAction);
    editClassWindowTimer.setRepeats(false);
    newClassAnnotationAction = new NewClassAnnotationAction(ontoTreePanel);
    newClassAnnotationWindowTimer = new javax.swing.Timer(DELAY, newClassAnnotationAction);
    newClassAnnotationWindowTimer.setRepeats(false);
    newInstanceAnnotationAction = new NewInstanceAnnotationAction(ontoTreePanel);
    newInstanceAnnotationWindowTimer = new javax.swing.Timer(DELAY, newInstanceAnnotationAction);
    newInstanceAnnotationWindowTimer.setRepeats(false);
  }


  /**
   * Grabs the current location of mouse pointers
   * 
   * @param e
   */
  public void mousePressed(MouseEvent e) {
    // if mouse is pressed anywhere, we simply hide all the windows
    hideAllWindows();
  }

  /**
   * This method to hide all the popup windows
   */
  public void hideAllWindows() {
    if(ontologyTreePanel.showingNewClassAnnotationWindow) {
      ontologyTreePanel.showingNewClassAnnotationWindow = false;
      newClassAnnotationAction.hideWindow();
      ontologyTreePanel.ontoViewer.documentTextArea.requestFocus();
    }
    if(ontologyTreePanel.showingEditOResourceWindow) {
      ontologyTreePanel.showingEditOResourceWindow = false;
      editClassAction.hideWindow();
      ontologyTreePanel.ontoViewer.documentTextArea.requestFocus();
    }
    if(ontologyTreePanel.showingNewInstanceAnnotationWindow) {
      ontologyTreePanel.showingNewInstanceAnnotationWindow = false;
      newInstanceAnnotationAction.hideWindow();
      ontologyTreePanel.ontoViewer.documentTextArea.requestFocus();
    }
  }

  /**
   * Invoked when Mouse hovers over the document
   * 
   * @param e
   */
  public void mouseMoved(MouseEvent e) {
    // mouse is moved so simply activate the timer
    newClassAnnotationAction
            .setTextLocation(ontologyTreePanel.ontoViewer.documentTextArea
                    .viewToModel(e.getPoint()));
    newClassAnnotationWindowTimer.restart();
    editClassAction
            .setTextLocation(ontologyTreePanel.ontoViewer.documentTextArea
                    .viewToModel(e.getPoint()));
    editClassAction.setMousePoint(e.getPoint());
    editClassWindowTimer.restart();
    newInstanceAnnotationAction
    .setTextLocation(ontologyTreePanel.ontoViewer.documentTextArea
          .viewToModel(e.getPoint()));
    newInstanceAnnotationWindowTimer.restart();
  }

  /**
   * Invoked when mouse is dragged
   */
  public void mouseDragged(MouseEvent e) {
    mouseMoved(e);
  }
}
