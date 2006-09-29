// Decompiled by DJ v3.9.9.91 Copyright 2005 Atanas Neshkov  Date: 19/09/2006 10:21:06
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   SubClassAction.java

package gate.gui.ontology;

import gate.creole.ontology.*;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

// Referenced classes of package gate.gui.ontology:
//            TreeNodeSelectionListener, Utils

public class SubClassAction extends AbstractAction implements
		TreeNodeSelectionListener {

	public SubClassAction(String s, Icon icon) {
		super(s, icon);
		nameSpace = new JTextField(20);
		className = new JTextField(20);
		comment = new JTextField(20);
		labelPanel = new JPanel(new GridLayout(3, 1));
		textFieldsPanel = new JPanel(new GridLayout(3, 1));
		panel = new JPanel(new FlowLayout(0));
		panel.add(labelPanel);
		panel.add(textFieldsPanel);
		labelPanel.add(new JLabel("Name Space :"));
		textFieldsPanel.add(nameSpace);
		labelPanel.add(new JLabel("Sub Class Name :"));
		textFieldsPanel.add(className);
		labelPanel.add(new JLabel("Comment :"));
		textFieldsPanel.add(comment);
	}


	public void actionPerformed(ActionEvent actionevent) {
		ArrayList arraylist = new ArrayList();
		for (int i = 0; i < selectedNodes.size(); i++) {
			Object obj = ((DefaultMutableTreeNode) selectedNodes.get(i))
					.getUserObject();
			if (obj instanceof TClass)
				arraylist.add(obj);
		}

		nameSpace.setText(ontology.getDefaultNameSpace());
		int j = JOptionPane.showOptionDialog(null, panel, "Sub Class Action: ",
				2, 3, null, new String[] { "OK", "Cancel" }, "OK");
		if (j == 0) {
			String s = nameSpace.getText();
			if (!Utils.isValidNameSpace(s)) {
				JOptionPane.showMessageDialog(null, (new StringBuilder())
						.append("Invalid NameSpace:").append(s).append(
								"\n example: http://gate.ac.uk/example#")
						.toString());
				return;
			}
			if (!Utils.isValidOntologyResourceName(className.getText())) {
				JOptionPane.showMessageDialog(null, "Invalid Classname");
				return;
			}
			if (ontology.getClassByName(className.getText()) != null) {
				JOptionPane.showMessageDialog(null, (new StringBuilder())
						.append("Class :").append(className.getText()).append(
								" already exists").toString());
				return;
			}
			OClassImpl oclassimpl = new OClassImpl(Calendar.getInstance()
					.getTime().toString(), className.getText(), comment
					.getText(), ontology);
			oclassimpl.setURI((new StringBuilder()).append(nameSpace.getText())
					.append(oclassimpl.getName()).toString());
			for (int k = 0; k < arraylist.size(); k++) {
				oclassimpl.addSuperClass((TClass) arraylist.get(k));
				((TClass) arraylist.get(k)).addSubClass(oclassimpl);
			}

			ontology.addClass(oclassimpl);
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

	protected JTextField nameSpace;

	protected JTextField className;

	protected JTextField comment;

	protected JPanel labelPanel;

	protected JPanel textFieldsPanel;

	protected JPanel panel;

	protected ArrayList selectedNodes;

	protected Ontology ontology;
}
