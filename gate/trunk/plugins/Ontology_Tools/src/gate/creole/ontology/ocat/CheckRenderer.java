/*
 *  CheckRenderer.java
 *
 *  Niraj Aswani, 12/March/07
 *
 *  $Id: CheckRenderer.html,v 1.0 2007/03/12 16:13:01 niraj Exp $
 */
package gate.creole.ontology.ocat;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
import com.ontotext.gate.vr.IFolder;

/**
 * Description: This class provides the renderer for the Ontology Tree Nodes.
 * @author Niraj Aswani
 * @version 1.0
 */
public class CheckRenderer extends JPanel implements TreeCellRenderer {

	/**
	 * Serial Version ID
	 */
	private static final long serialVersionUID = 3257004371551204912L;

	/**
	 * Allows user to select/deselect class in the ontology Tree
	 */
	private JCheckBox check;

	/**
	 * Class label is shown using this label
	 */
	private JLabel label;

	/**
	 * The instance of ontologyTreePanel
	 */
	private OntologyTreePanel ontologyTreePanel;

	/**
	 * Constructor
	 * 
	 * @param owner
	 */
	public CheckRenderer(OntologyTreePanel owner) {
		this.ontologyTreePanel = owner;
		check = new JCheckBox();
		check.setBackground(Color.white);
		label = new JLabel();
		setLayout(new BorderLayout(5, 10));
		add(check, BorderLayout.WEST);
		add(label, BorderLayout.CENTER);
	}

	/**
	 * Renderer method
	 */
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean isSelected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		Object userObject = value;

		if (!(userObject instanceof IFolder)) {
			label.setBackground(Color.white);
			return this;
		}

		IFolder node = (IFolder) userObject;
		String conceptName = node.toString();
		if (row == 0) {
			// this is the ontology name
			check.setVisible(false);
			this.setBackground(Color.white);
			label.setText(conceptName);
			return this;
		} else {
			check.setVisible(true);
		}

		// if node should be selected
		boolean selected = ontologyTreePanel.currentClass2IsSelectedMap.get(conceptName).booleanValue();
		check.setSelected(selected);

		label.setText(conceptName);
		label.setFont(tree.getFont());

		// We assign the automatically generated random colors to the concept,
		// but randomly generation of colors for different classes takes place
		// only once when that ontology is loaded for the first time
		if (ontologyTreePanel.currentClass2ColorMap.containsKey(conceptName)) {
			Color color = (Color) ontologyTreePanel.currentClass2ColorMap.get(
					conceptName);
			this.setBackground(color);
			check.setBackground(Color.white);
		}
		return this;
	}
}