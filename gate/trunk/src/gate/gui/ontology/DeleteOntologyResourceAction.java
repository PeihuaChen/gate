/*
 *  DeleteOntologyResourceAction.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: DeleteOntologyResourceAction.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
 */
package gate.gui.ontology;

import gate.creole.ontology.*;
import gate.gui.MainFrame;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Action to delete a resource from ontology.
 * @author niraj
 *
 */
public class DeleteOntologyResourceAction extends AbstractAction implements
                                                                TreeNodeSelectionListener {
  private static final long serialVersionUID = 3257289136439439920L;

  public DeleteOntologyResourceAction(String caption, Icon icon) {
    super(caption, icon);
    selectedNodes = new ArrayList<DefaultMutableTreeNode>();
  }

  public void actionPerformed(ActionEvent actionevent) {
    ArrayList<DefaultMutableTreeNode> selectedNodes = new ArrayList<DefaultMutableTreeNode>(
            this.selectedNodes);

    int i = JOptionPane.showConfirmDialog(MainFrame.getInstance(), "Are you sure?",
            "Resource deleting action", JOptionPane.YES_NO_OPTION);
    if(i != 0) return;
    for(int j = 0; j < selectedNodes.size(); j++) {
      DefaultMutableTreeNode defaultmutabletreenode = (DefaultMutableTreeNode)selectedNodes
              .get(j);
      Object obj = defaultmutabletreenode.getUserObject();
      try {
        if(obj instanceof OClass) {
          if(ontology.containsOClass(((OClass)obj).getURI()))
            ontology.removeOClass((OClass)obj);
          continue;
        }
        if(obj instanceof OInstance) {
          if(ontology.getOInstance(((OInstance)obj).getURI()) != null)
            ontology.removeOInstance((OInstance)obj);
          continue;
        }
        if((obj instanceof RDFProperty)
                && ontology.getProperty(((RDFProperty)obj).getURI()) != null)
          ontology.removeProperty((RDFProperty)obj);
      }
      catch(Exception re) {
        JOptionPane.showMessageDialog(MainFrame.getInstance(), "The Resource "
                + ((OResource)obj).getURI() + " could not be deleted \n"
                + "because : \n" + "It is not an explicit resource!");
      }
    }
  }

  public Ontology getOntology() {
    return ontology;
  }

  public void setOntology(Ontology ontology1) {
    ontology = ontology1;
  }

  public void selectionChanged(ArrayList<DefaultMutableTreeNode> arraylist) {
    selectedNodes = arraylist;
  }

  protected Ontology ontology;

  protected ArrayList<DefaultMutableTreeNode> selectedNodes;
}
