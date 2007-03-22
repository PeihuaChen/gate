/*
 *  OntologyTreePanel.java
 *
 *  Niraj Aswani, 12/March/07
 *
 *  $Id: OntologyTreePanel.html,v 1.0 2007/03/12 16:13:01 niraj Exp $
 */
package gate.creole.ontology.ocat;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import gate.*;
import gate.creole.ontology.Ontology;
import com.ontotext.gate.vr.ClassNode;
import com.ontotext.gate.vr.OntoTreeModel;
import gate.swing.*;
import java.util.*;
import com.ontotext.gate.vr.IFolder;
import gate.gui.docview.*;

/**
 * This class provides a GUI frame for the OCAT tool, where one of the
 * components is the OntologyTree, the other one is Ontology Options and so on.
 * 
 * @author niraj
 */
public class OntologyTreePanel extends JPanel {

	/**
	 * Serial version ID
	 */
	private static final long serialVersionUID = 3618419328190592304L;

	/** Instance of JTree used to store information about ontology classes */
	protected JTree currentOntologyTree;

	/** The current currentOntologyTreeModel */
	private OntoTreeModel currentOntologyTreeModel;
	
	/** ToolBars that displays the different options */
	private JToolBar leftToolBar;

	/** Toggle Buttons used to display instances and the attributes */
	private JToggleButton instances, attributes;

	/** OntologyViewerOptions instance */
	protected OntologyViewerOptions ontologyViewerOptions;

	/** Stores all the various ontologyTreeModels for different ontologies */
	protected HashMap<String, OntoTreeModel> ontologyTreeModels;

	/** Stores various color schemes for different ontology classes */
	protected HashMap<String, HashMap<String, Color>> ontologyName2ColorSchemesMap;

	/** Current ontologyColorScheme */
	protected HashMap<String, Color> currentClass2ColorMap;

	/** This stores Class selection map for each individual loaded ontology */
	protected HashMap<String, HashMap<String, Boolean>> ontologyName2ClassSelectionMap;

	/** Class Selection map for the current ontology */
	protected HashMap<String, Boolean> currentClass2IsSelectedMap;

	/** Central Textual Document View */
	private TextualDocumentView textView;

	/**
	 * Current Annotation Map that stores the annotation in arraylist for each
	 * concept
	 */
	protected HashMap<String, ArrayList<Annotation>> currentClassName2AnnotationsListMap;

	/** Instance of colorGenerator */
	private ColorGenerator colorGenerator;

	/** Current Ontology */
	private Ontology currentOntology;

	/** OntologyTreeListener that listens to the selection of ontology classes */
	protected OntologyTreeListener ontoTreeListener;

	/** Instance of ontology Viewer */
	protected OntologyViewer ontoViewer;

	/** Constructor */
	public OntologyTreePanel(OntologyViewer ontoViewer) {
		this.ontoViewer = ontoViewer;
		this.textView = ontoViewer.documentTextualDocumentView;
		this.ontologyViewerOptions = new OntologyViewerOptions(this);

		ontologyName2ColorSchemesMap = new HashMap<String, HashMap<String, Color>>();

		ontologyTreeModels = new HashMap<String, OntoTreeModel>();
		currentClass2ColorMap = new HashMap<String, Color>();
		ontologyName2ClassSelectionMap = new HashMap<String, HashMap<String, Boolean>>();
		currentClass2IsSelectedMap = new HashMap<String, Boolean>();
		colorGenerator = new ColorGenerator();
		initGUI();
	}

	/** Deletes the Annotations from the document */
	public void deleteAnnotation(Annotation annot) {
		// and now removing from the actual document
		AnnotationSet set = ontoViewer.getDocument().getAnnotations();
		if (!(set.remove(annot))) {
			Map annotSetMap = ontoViewer.getDocument().getNamedAnnotationSets();
			if (annotSetMap != null) {
				java.util.List<String> setNames = new ArrayList<String>(annotSetMap.keySet());
				Collections.sort(setNames);
				Iterator<String> setsIter = setNames.iterator();
				while (setsIter.hasNext()) {
					set = ontoViewer.getDocument().getAnnotations(setsIter.next());
					if (set.remove(annot)) {
						return;
					}
				}
			}
		}
	}

	/** Returns the current ontology */
	public Ontology getCurrentOntology() {
		return currentOntology;
	}

	/** Returns the instance of highlighter */
	public javax.swing.text.Highlighter getHighlighter() {
		return ((JTextArea) ((JScrollPane) textView.getGUI()).getViewport()
				.getView()).getHighlighter();
	}

	/** Returns the associated color for the given class */
	public Color getHighlightColor(String classVal) {
		return (Color) currentClass2ColorMap.get(classVal);
	}

	/** Initialize the GUI */
	private void initGUI() {
		currentOntologyTree = new JTree();

		ToolTipManager.sharedInstance().registerComponent(currentOntologyTree);
		currentOntologyTree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		this.setLayout(new BorderLayout());
		this.add(new JScrollPane(currentOntologyTree), BorderLayout.CENTER);

		leftToolBar = new JToolBar(JToolBar.VERTICAL);
		instances = new JToggleButton();
		attributes = new JToggleButton();
		instances.setIcon(new VerticalTextIcon(instances, "Instances",
				VerticalTextIcon.ROTATE_LEFT));
		instances.setSelected(false);
		attributes.setIcon(new VerticalTextIcon(instances, "Attributes",
				VerticalTextIcon.ROTATE_LEFT));
		attributes.setSelected(false);
		attributes.setVisible(false);
		ButtonActions toggleButtonListener = new ButtonActions();
		instances.addActionListener(toggleButtonListener);
		attributes.addActionListener(toggleButtonListener);
		leftToolBar.setFloatable(false);
		leftToolBar.add(instances);
		leftToolBar.add(attributes);
		// this.add(leftToolBar,BorderLayout.WEST);

		ontoTreeListener = new OntologyTreeListener(this);
		currentOntologyTree.addMouseListener(ontoTreeListener);

		CheckRenderer cellRenderer = new CheckRenderer(this);
		currentOntologyTree.setCellRenderer(cellRenderer);
	}

	/** A method to show an empty ontology tree */
	public void showEmptyOntologyTree() {
		currentOntology = null;
		currentOntologyTreeModel = null;
		currentClass2ColorMap = null;
		currentClass2IsSelectedMap = null;
		currentOntologyTree.setVisible(false);
		ontoTreeListener.removeHighlights();
	}

	/**
	 * This method is called to remove the stored ontology model and free up the
	 * memory with other resources occupied by the removed ontology
	 */
	public void removeOntologyTreeModel(Ontology ontology, boolean wasCurrentlySelected) {
		this.ontologyTreeModels.remove(ontology.toString());
		this.ontologyName2ColorSchemesMap.remove(ontology.toString());
		this.ontologyName2ClassSelectionMap.remove(ontology.toString());
		if (ontologyTreeModels == null || ontologyTreeModels.size() == 0) {
			showEmptyOntologyTree();
		}
	}

	/**
	 * This method is used to plot the ontology on the tree and generate/load
	 * the respective data in the memory
	 * 
	 * @param ontology -
	 *            the ontology to be ploted
	 * @param currentClassName2AnnotationsListMap -
	 *            the annotationMap which contains Key=concept(String)
	 *            Value=annotations(ArrayList)
	 */
	public void showOntologyInOntologyTreeGUI(Ontology ontology,
			HashMap<String, ArrayList<Annotation>> annotMap) {

		this.currentClassName2AnnotationsListMap = annotMap;
		if (currentOntology != null && currentClass2ColorMap != null)
			ontologyName2ColorSchemesMap
					.put(currentOntology.toString(), currentClass2ColorMap);
		if (currentOntology != null && currentClass2IsSelectedMap != null)
			ontologyName2ClassSelectionMap.put(currentOntology.toString(),
					currentClass2IsSelectedMap);
		if (currentOntology != null && currentOntologyTreeModel != null
				&& ontologyTreeModels.containsKey(currentOntology.toString()))
			ontologyTreeModels.put(currentOntology.toString(), currentOntologyTreeModel);

		currentOntology = ontology;
		ClassNode root = null;
		// lets create the new model for this new selected ontology
		if (ontologyTreeModels != null
				&& ontologyTreeModels.containsKey(ontology.toString())) {
			currentOntologyTreeModel = ontologyTreeModels.get(ontology.toString());
			currentClass2ColorMap = ontologyName2ColorSchemesMap.get(ontology.toString());
			currentClass2IsSelectedMap = ontologyName2ClassSelectionMap.get(ontology.toString());
		} else {
			root = ClassNode.createRootNode(ontology, false);
			HashMap<String, Color> newColorScheme = new HashMap<String, Color>();
			setColorScheme(root, newColorScheme);
			currentClass2ColorMap = newColorScheme;
			ontologyName2ColorSchemesMap.put(ontology.toString(), newColorScheme);
			currentOntologyTreeModel = new OntoTreeModel(root);
			ontologyTreeModels.put(ontology.toString(), currentOntologyTreeModel);
			HashMap<String, Boolean> newClassSelection = new HashMap<String, Boolean>();
			setOntoTreeClassSelection(root, newClassSelection);
			currentClass2IsSelectedMap = newClassSelection;
			ontologyName2ClassSelectionMap.put(ontology.toString(), newClassSelection);
		}
		currentOntologyTree.setModel(currentOntologyTreeModel);
		// update the GUI part of the Tree
		currentOntologyTree.invalidate();
	}
	
	/**
	 * For every ontology it generates the colors only once at the begining
	 * which should remain same throughout the programe
	 * 
	 * @param root -
	 *            the root (top class) of the ontology
	 * @param colorScheme -
	 *            and the colorScheme hashmap Key=conceptName, Value:associated
	 *            color map. if provided as a new fresh instance of hashmap with
	 *            size zero, it parses through the whole ontology and generate
	 *            the random color instances for all the classes and stores them
	 *            in the provided colorScheme hashmap
	 */
	private void setColorScheme(IFolder root, HashMap<String, Color> colorScheme) {
		if (!colorScheme.containsKey(root.toString())) {
			colorScheme.put(root.toString(), getColor(root.toString()));
			Iterator children = root.getChildren();
			while (children.hasNext()) {
				setColorScheme((IFolder) children.next(), colorScheme);
			}
		}
	}

	/**
	 * This method uses the java.util.prefs.Preferences and get the color for
	 * particular selectedAnnotationType.. This color could have been saved by the
	 * AnnotationSetsView
	 * 
	 * @param selectedAnnotationType
	 * @return
	 */
	private Color getColor(String className) {
		java.util.prefs.Preferences prefRoot = null;
		try {
			prefRoot = java.util.prefs.Preferences.userNodeForPackage(Class
					.forName("gate.creole.ontology.ocat.OntologyTreePanel"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		int rgba = prefRoot.getInt(className, -1);
		Color colour;
		if (rgba == -1) {
			// initialise and save
			float components[] = colorGenerator.getNextColor().getComponents(
					null);
			colour = new Color(components[0], components[1], components[2],
					0.5f);
			int rgb = colour.getRGB();
			int alpha = colour.getAlpha();
			rgba = rgb | (alpha << 24);
			prefRoot.putInt(className, rgba);
		} else {
			colour = new Color(rgba, true);
		}
		return colour;
	}

	/**
	 * This is to initialise the classSelection as false to all the classes
	 * 
	 * @param root
	 * @param classSelection
	 */
	private void setOntoTreeClassSelection(IFolder root, HashMap<String, Boolean> classSelection) {
		if (!classSelection.containsKey(root.toString())) {
			classSelection.put(root.toString(), new Boolean(false));
			Iterator children = root.getChildren();
			while (children.hasNext()) {
				setOntoTreeClassSelection((IFolder) children.next(),
						classSelection);
			}
		}
	}

	/**
	 * returns the currentOntologyTree Panel
	 * 
	 * @return
	 */
	public Component getGUI() {
		return this;
	}


	/**
	 * This method select/deselect the classes in the classSelectionMap
	 * 
	 * @param className
	 * @param value
	 */
	public void setSelected(String className, boolean value) {
		currentClass2IsSelectedMap.put(className, new Boolean(value));
		ontologyName2ClassSelectionMap.put(currentOntology.toString(),
				currentClass2IsSelectedMap);
	}

	public void setColor(String className, Color col) {
		currentClass2ColorMap.put(className, col);
		ontologyName2ColorSchemesMap.put(currentOntology.toString(), currentClass2ColorMap);
	}

	/** Internal class to implement the actions of various buttons */
	private class ButtonActions implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			if (ae.getSource() == instances) {
				if (instances.isSelected()) {
					attributes.setVisible(true);
				} else {
					attributes.setVisible(false);
				}
				leftToolBar.updateUI();
			}
		}
	}
}
