package gate.gui.ontology;

import gate.creole.ontology.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class DeleteOntologyResourceAction extends AbstractAction implements
		TreeNodeSelectionListener {

	public DeleteOntologyResourceAction(String caption, Icon icon) {
		super(caption, icon);
		selectedNodes = new ArrayList();
	}


	public void actionPerformed(ActionEvent actionevent) {
		int i = JOptionPane.showConfirmDialog(null, "Are you sure?");
		if (i != 0)
			return;
		for (int j = 0; j < selectedNodes.size(); j++) {
			DefaultMutableTreeNode defaultmutabletreenode = (DefaultMutableTreeNode) selectedNodes
					.get(j);
			Object obj = defaultmutabletreenode.getUserObject();
			if (obj instanceof TClass) {
				if (ontology.containsClassByName(((TClass) obj).getName()))
					ontology.removeClass((TClass) obj);
				continue;
			}
			if (obj instanceof OInstance) {
				if (ontology.getInstanceByName(((OInstance) obj).getName()) != null)
					ontology.removeInstance((OInstance) obj);
				continue;
			}
			if ((obj instanceof Property)
					&& ontology.getPropertyDefinitionByName(((Property) obj)
							.getName()) != null)
				ontology.removePropertyDefinition((Property) obj);
		}

	}


	public Ontology getOntology() {
		return ontology;
	}


	public void setOntology(Ontology ontology1) {
		ontology = ontology1;
	}


	public void selectionChanged(ArrayList arraylist) {
		selectedNodes = arraylist;
	}

	protected Ontology ontology;
	protected ArrayList selectedNodes;
}