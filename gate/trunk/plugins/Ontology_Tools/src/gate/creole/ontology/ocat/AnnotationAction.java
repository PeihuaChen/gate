/*
 *  AnnotationAction.java
 *
 *  Niraj Aswani, 12/March/07
 *
 *  $Id: AnnotationAction.html,v 1.0 2007/03/12 16:13:01 niraj Exp $
 */
package gate.creole.ontology.ocat;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import gate.*;
import gate.gui.MainFrame;
import gate.util.GateRuntimeException;

/**
 * This class provides the GUI implementation for creating/changing/deleting
 * annotations from the text. It uses OntologyTreePanel to display the list of
 * available classes in the ontology.
 * 
 * @author niraj
 */
public class AnnotationAction extends MouseInputAdapter {

	/**
	 * Reference to the main OntologyTreePanel object
	 */
	private OntologyTreePanel ontologyTreePanel;

	/**
	 * Reference to the main OntologyTreeListener object
	 */
	private OntologyTreeListener ontologyTreeListener;

	/**
	 * Indicates whether to show a new annotation window or not. This is
	 * generally activated when user put his/her mouse on top of the text that
	 * is not annotated before for at least few seconds.
	 */
	private boolean showNewAnnotationWindow = false;

	/**
	 * A part of the new Annotation window
	 */
	private JWindow bottomWindow;

	/**
	 * Indicates whether the new annotation windows is being shown or not.
	 */
	private boolean windowShowing = false;

	/**
	 * This is used for displaying all available concepts from the ontology.
	 */
	private JComboBox combo;

	String field = new String();

	/**
	 * Button to add Single Annotation, add annotations over all similar text,
	 * to cancel the new annotation window.
	 */
	private JButton addAnnotation, addAll, cancel;

	/**
	 * Helper variables, used to keep the information about where exactly the
	 * mouse is and whether we need to start a timer or not.
	 */
	private int scX, scY, ecX, ecY, pscX, pscY, pecX, pecY;

	/**
	 * Indicates if the mouse is being dragged.
	 */
	private boolean mouseDragged = false;

	/**
	 * An instance of timer, that is used for waiting for sometime.
	 */
	private javax.swing.Timer timer;

	/**
	 * How long we should wait before showing a new annotation/change annotation
	 * window.
	 */
	private final int DELAY = 500;

	/**
	 * Action that tells what to do whan a mouse is moved.
	 */
	private MouseMovedAction mouseMovedAction;

	/**
	 * A window that is shown for changing the type of an existing annotation.
	 */
	private JWindow editClassWindow;

	/**
	 * Indicates whether the editClasswindow is being shown or not.
	 */
	private boolean showingEditClassWindow = false;

	/**
	 * Action that is performed when user decides to create a new annotation.
	 */
	private NewAnnotationAction newAnnotationAction;

	/**
	 * Indicates whether the newAnnotaitonWindow is being shown.
	 */
	private boolean showingBottomWindow = false;

	/**
	 * Timer object
	 */
	private javax.swing.Timer bottomWindowTimer;

	/**
	 * Keeps the record of recently used class.
	 */
	private String recentUsedClass = "";

	/**
	 * Keeps the track of the selected index of the class in the new annotation
	 * window.
	 */
	private int selectedIndex = 0;

	/**
	 * Constructor
	 * 
	 * @param ontologyTreePanel
	 *            the instance this instance uses to obtain the information
	 *            about ontology
	 */
	public AnnotationAction(OntologyTreePanel ontoTreePanel) {
		this.ontologyTreePanel = ontoTreePanel;
		ontologyTreeListener = ontoTreePanel.ontoTreeListener;
		combo = new JComboBox();
		combo.setMaximumRowCount(5);
		combo.setEditable(true);
		addAnnotation = new JButton(MainFrame.getIcon("annotation"));
		addAnnotation.setBorderPainted(false);
		addAnnotation.setContentAreaFilled(false);
		addAnnotation.setMargin(new Insets(0, 0, 0, 0));
		addAnnotation.setToolTipText("Apply");

		addAll = new JButton("All", MainFrame.getIcon("annotation"));
		addAll.setBorderPainted(false);
		addAll.setContentAreaFilled(false);
		addAll.setMargin(new Insets(0, 0, 0, 0));
		addAll.setToolTipText("Apply To All");

		cancel = new JButton(MainFrame.getIcon("exit"));
		cancel.setBorderPainted(false);
		cancel.setContentAreaFilled(false);
		cancel.setMargin(new Insets(0, 0, 0, 0));
		cancel.setToolTipText("Close Window");

		ComboListener listener = new ComboListener();
		combo.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				field = ((JTextComponent) combo.getEditor()
						.getEditorComponent()).getText();
			}
		});
		combo.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent keyevent) {
				String s = ((JTextComponent) combo.getEditor()
						.getEditorComponent()).getText();
				TreeSet<String> sortedSet = new TreeSet<String>();
				if (s != null) {
					Set<String> classes = ontologyTreePanel.currentClass2ColorMap
							.keySet();
					Iterator<String> classIter = classes.iterator();
					while (classIter.hasNext()) {
						String s1 = classIter.next();
						if (s1.toLowerCase().startsWith(s.toLowerCase())) {
							sortedSet.add(s1);
						}
					}
				}
				DefaultComboBoxModel defaultcomboboxmodel = new DefaultComboBoxModel(
						sortedSet.toArray());
				combo.setModel(defaultcomboboxmodel);
				try {
					combo.showPopup();
				} catch (Exception exception) {
				}
				((JTextComponent) combo.getEditor().getEditorComponent())
						.setText(s);
			}
		});
		addAnnotation.addActionListener(listener);
		addAll.addActionListener(listener);
		cancel.addActionListener(listener);
		mouseMovedAction = new MouseMovedAction();
		timer = new javax.swing.Timer(DELAY, mouseMovedAction);
		timer.setRepeats(false);
		newAnnotationAction = new NewAnnotationAction();
		bottomWindowTimer = new javax.swing.Timer(DELAY, newAnnotationAction);
		bottomWindowTimer.setRepeats(false);
	}

	/**
	 * updates the caret position and start the timer if needed
	 */
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2 || e.getClickCount() == 3) {
			// so find out the pscX, pscY and pecX, pecY
			JTextArea textPane = ontologyTreePanel.ontoViewer.documentTextArea;
			try {
				Rectangle start = textPane.modelToView(textPane
						.getSelectionStart());
				Rectangle end = textPane
						.modelToView(textPane.getSelectionEnd());
				pscX = (int) start.getX();
				pscY = (int) start.getY();
				pecX = (int) end.getX();
				pecY = (int) end.getY();
				mouseDragged = false;
				caretUpdate(e);
				if (showNewAnnotationWindow
						&& isWithinRange(e.getX(), e.getY())) {
					newAnnotationAction
							.setTextLocation(ontologyTreePanel.ontoViewer.documentTextArea
									.viewToModel(e.getPoint()));
					newAnnotationAction.setMouseEvent(e);
					bottomWindowTimer.restart();
				}
			} catch (BadLocationException e1) {
				// we ignore this exception
			}
		}
	}

	/**
	 * Grabs the current location of mouse pointers
	 * 
	 * @param e
	 */
	public void mousePressed(MouseEvent e) {
		scX = e.getX();
		scY = e.getY();
		if (showingEditClassWindow) {
			showingEditClassWindow = false;
			editClassWindow.setVisible(false);
			ontologyTreePanel.ontoViewer.documentTextArea.requestFocus();
		}
		if (windowShowing) {
			windowShowing = false;
			bottomWindow.setVisible(false);
			ontologyTreePanel.ontoViewer.documentTextArea.requestFocus();
		}
	}

	/**
	 * What to do when user releases the mouse. It stores the locations of text
	 * selection if user did so with left click, otherwise updates the caret and
	 * see if it needs to show popup for new annotation window
	 * 
	 * @param e
	 */
	public void mouseReleased(MouseEvent e) {
		ecX = e.getX();
		ecY = e.getY();
		// if user has dragged the mouse find out the location where it
		// released the mouse button
		if (mouseDragged) {
			mouseDragged = false;
			pecX = ecX;
			pecY = ecY;
			if (pecX < pscX) {
				int temp = pecX;
				pecX = pscX;
				pscX = temp;
			}
			if (pecY < pscY) {
				int temp = pecY;
				pecY = pscY;
				pscY = temp;
			}
		}
		// we need to call new Annotation window only on right click
		// otherwise we update the caret position
		if (!SwingUtilities.isRightMouseButton(e)) {
			showNewAnnotationWindow = false;
			caretUpdate(e);
		} else {
			showNewAnnotationWindow(e);
		}
	}

	/**
	 * Invoked when user draggs the mouse to select the text
	 * 
	 * @param e
	 */
	public void mouseDragged(MouseEvent e) {
		if (!SwingUtilities.isRightMouseButton(e) && !mouseDragged) {
			pscX = scX;
			pscY = scY;
			mouseDragged = true;
		}
	}

	/**
	 * This method to hide all the popup windows
	 */
	public void hideAllWindows() {
		if (windowShowing) {
			windowShowing = false;
			bottomWindow.setVisible(false);
			ontologyTreePanel.ontoViewer.documentTextArea.requestFocus();
		}
		if (showingEditClassWindow) {
			showingEditClassWindow = false;
			editClassWindow.setVisible(false);
			ontologyTreePanel.ontoViewer.documentTextArea.requestFocus();
		}
	}

	/**
	 * Invoked when Mouse hovers over the document
	 * 
	 * @param e
	 */
	public void mouseMoved(MouseEvent e) {
		if (showNewAnnotationWindow && isWithinRange(e.getX(), e.getY())) {
			newAnnotationAction
					.setTextLocation(ontologyTreePanel.ontoViewer.documentTextArea
							.viewToModel(e.getPoint()));
			newAnnotationAction.setMouseEvent(e);
			bottomWindowTimer.restart();
		}
		mouseMovedAction
				.setTextLocation(ontologyTreePanel.ontoViewer.documentTextArea
						.viewToModel(e.getPoint()));
		mouseMovedAction.setMousePointer(e.getPoint());
		timer.restart();
	}

	/**
	 * Action to be taken when user hovers the mouse over the text selected for
	 * the new annotation
	 */
	private class NewAnnotationAction extends AbstractAction {

		/**
		 * Serial version ID
		 */
		private static final long serialVersionUID = 3256999939094623287L;

		private int textLocation;

		private MouseEvent event;

		public void actionPerformed(ActionEvent e) {
			if (showingEditClassWindow) {
				showingEditClassWindow = false;
				editClassWindow.setVisible(false);
				ontologyTreePanel.ontoViewer.documentTextArea.requestFocus();
			}
			showNewAnnotationWindow(event);
		}

		public void setTextLocation(int textLocation) {
			this.textLocation = textLocation;
		}

		public void setMouseEvent(MouseEvent me) {
			this.event = me;
		}
	}

	/**
	 * Action to be taken when user hovers the mouse over the selected
	 * annotations
	 */
	private class MouseMovedAction extends AbstractAction {
		/**
		 * Serial Version ID
		 */
		private static final long serialVersionUID = 3257006557656004657L;

		private int textLocation;

		private Point mousePoint;

		private ArrayList<Annotation> getSimilarAnnotations(
				gate.Annotation annot) {
			ArrayList<Annotation> annotations = new ArrayList<Annotation>();
			String classValue = Utils.getClassFeatureValue(annot);
			String annotString = getString(annot);
			ArrayList<Annotation> highlightedAnnotations = ontologyTreeListener.highlightedAnnotations;
			for (int i = 0; i < highlightedAnnotations.size(); i++) {
				gate.Annotation temp = highlightedAnnotations.get(i);
				String tempClass = Utils.getClassFeatureValue(temp);
				String tempString = getString(temp);
				if (classValue.equals(tempClass)
						&& annotString.equals(tempString)) {
					annotations.add(temp);
				}
			}
			return annotations;
		}

		private String getString(gate.Annotation annot) {
			return ontologyTreePanel.ontoViewer.getDocument().getContent()
					.toString().substring(
							annot.getStartNode().getOffset().intValue(),
							annot.getEndNode().getOffset().intValue());
		}

		public void actionPerformed(ActionEvent e) {
			int[] range = ontologyTreeListener.annotationRange;
			int index1 = -1;
			ArrayList<Integer> indexes = new ArrayList<Integer>();
			if (range != null) {
				for (int i = 0; i < range.length; i += 2) {
					if (textLocation >= range[i]
							&& textLocation <= range[i + 1]) {
						index1 = (i == 0) ? i : i / 2;
						indexes.add(new Integer(index1));
					}
				}
			}
			final ArrayList<Integer> indexes1 = indexes;
			// yes it is put on highlighted so show the respective class
			if (range != null && indexes.size() > 0) {
				if (showingEditClassWindow) {
					gate.Annotation annotation = ontologyTreeListener
							.highlightedAnnotations.get(
									indexes.get(0).intValue());
					try {
						JTextArea textPane = ontologyTreePanel.ontoViewer.documentTextArea;
						Rectangle startRect = textPane.modelToView(annotation
								.getStartNode().getOffset().intValue());
						Rectangle endRect = textPane.modelToView(annotation
								.getEndNode().getOffset().intValue());
						Point topLeft = textPane.getLocationOnScreen();
						int x = topLeft.x + startRect.x;
						int y = topLeft.y + endRect.y + endRect.height;
						if (editClassWindow.getX() == x
								&& editClassWindow.getY() == y) {
							// do nothing
							return;
						} else {
							showingEditClassWindow = false;
							editClassWindow.setVisible(false);
							ontologyTreePanel.ontoViewer.documentTextArea
									.requestFocus();
						}
					} catch (BadLocationException e1) {
						throw new GateRuntimeException(
								"Can't show the popup window", e1);
					}
				}
				editClassWindow = new JWindow(
						SwingUtilities
								.getWindowAncestor(ontologyTreePanel.ontoViewer.documentTextualDocumentView
										.getGUI()));
				gate.Annotation annot = ontologyTreeListener
						.highlightedAnnotations.get(
								indexes.get(0).intValue());
				// ok we need to find out classes
				final ArrayList<String> classValues = new ArrayList<String>();
				for (int i = 0; i < indexes.size(); i++) {
					gate.Annotation tempAnnot = ontologyTreeListener
							.highlightedAnnotations.get(
									indexes.get(i).intValue());
					classValues.add(Utils.getClassFeatureValue(tempAnnot));
				}

				if (classValues.size() == 1) {
					selectedIndex = indexes.get(0).intValue();
					showWindow(classValues.get(0));
					return;
				}

				// so before showing window we need to list all the available
				// classes
				selectedIndex = -1;
				final JPopupMenu classLists = new JPopupMenu();
				classLists.setLayout(new GridLayout(classValues.size(), 1));
				for (int i = 0; i < classValues.size(); i++) {
					JButton button = new JButton(classValues.get(i));
					classLists.add(button);
					button.setActionCommand("" + i);
					button.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ae) {
							final int tempIndex = Integer.parseInt(ae
									.getActionCommand());
							selectedIndex = indexes1.get(tempIndex).intValue();
							classLists.setVisible(false);
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									showWindow(classValues.get(tempIndex));
									// and finally show it
									classLists.show(ontologyTreePanel.ontoViewer.documentTextArea,
											(int) mousePoint.getX(), (int) mousePoint.getY());
									
								}
							});
							
						}
					});
				}
			}
		}

		private void showWindow(final String classValue) {
			showingEditClassWindow = true;
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
			JButton endOffsetExtendLeft = new JButton(MainFrame
					.getIcon("extend-left"));
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
						if (ontologyTreePanel.ontologyViewerOptions
								.getDeleteConfirmation()) {
							Object[] options = new Object[] { "YES", "NO" };
							int confirm = JOptionPane.showOptionDialog(Main
									.getMainFrame(),
									"Delete Annotation : Are you sure?",
									"Delete Annotation Confirmation",
									JOptionPane.YES_NO_CANCEL_OPTION,
									JOptionPane.QUESTION_MESSAGE, null,
									options, options[0]);
							if (confirm == JOptionPane.YES_OPTION) {
								gate.Annotation annot = (gate.Annotation) ontologyTreeListener
										.highlightedAnnotations.get(
												selectedIndex);
								if (annot != null) {
									ontologyTreePanel.deleteAnnotation(annot);
								}
								editClassWindow.setVisible(false);
								ontologyTreePanel.ontoViewer.documentTextArea
										.requestFocus();
							} else {
								editClassWindow.setVisible(false);
								ontologyTreePanel.ontoViewer.documentTextArea
										.requestFocus();
							}
						} else {
							gate.Annotation annot = ontologyTreeListener
									.highlightedAnnotations.get(
											selectedIndex);
							ontologyTreePanel.deleteAnnotation(annot);
							editClassWindow.setVisible(false);
							ontologyTreePanel.ontoViewer.documentTextArea
									.requestFocus();
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			});
			// extend the annotation by one character on left
			startOffsetExtendLeft.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						gate.Annotation annot = ontologyTreeListener
								.highlightedAnnotations.get(selectedIndex);
						int startOffset = annot.getStartNode().getOffset()
								.intValue();
						int endOffset = annot.getEndNode().getOffset()
								.intValue();
						FeatureMap features = annot.getFeatures();
						if (startOffset == 0)
							return;
						startOffset--;
						ontologyTreePanel.deleteAnnotation(annot);
						ontologyTreePanel.ontoViewer.documentTextArea
								.setSelectionStart(startOffset);
						ontologyTreePanel.ontoViewer.documentTextArea
								.setSelectionEnd(endOffset);
						Annotation addedAnnotation = ontologyTreePanel.ontoTreeListener
								.addNewAnnotation(classValue, false, features)
								.get(0);
						selectedIndex = ontologyTreeListener
								.highlightedAnnotations.indexOf(
										addedAnnotation);
					} catch (Exception e1) {
						throw new GateRuntimeException(e1);
					}
				}
			});
			// extend the annotation by one character on left
			startOffsetExtendRight.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					gate.Annotation annot = ontologyTreeListener
							.highlightedAnnotations.get(selectedIndex);
					int startOffset = annot.getStartNode().getOffset()
							.intValue();
					int endOffset = annot.getEndNode().getOffset().intValue();
					FeatureMap features = annot.getFeatures();
					startOffset++;
					if (startOffset == endOffset)
						return;
					ontologyTreePanel.deleteAnnotation(annot);
					ontologyTreePanel.ontoViewer.documentTextArea
							.setSelectionStart(startOffset);
					ontologyTreePanel.ontoViewer.documentTextArea
							.setSelectionEnd(endOffset);
					Annotation addedAnnotation = ontologyTreePanel.ontoTreeListener
							.addNewAnnotation(classValue, false, features).get(
									0);
					selectedIndex = ontologyTreeListener
							.highlightedAnnotations.indexOf(
									addedAnnotation);
				}
			});
			// extend the annotation by one character on left
			endOffsetExtendLeft.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						gate.Annotation annot = ontologyTreeListener
								.highlightedAnnotations.get(selectedIndex);
						int startOffset = annot.getStartNode().getOffset()
								.intValue();
						int endOffset = annot.getEndNode().getOffset()
								.intValue();
						FeatureMap features = annot.getFeatures();
						endOffset--;
						if (endOffset == startOffset)
							return;
						ontologyTreePanel.deleteAnnotation(annot);
						ontologyTreePanel.ontoViewer.documentTextArea
								.setSelectionStart(startOffset);
						ontologyTreePanel.ontoViewer.documentTextArea
								.setSelectionEnd(endOffset);
						Annotation addedAnnotation = ontologyTreePanel.ontoTreeListener
								.addNewAnnotation(classValue, false, features)
								.get(0);
						selectedIndex = ontologyTreeListener
								.highlightedAnnotations.indexOf(
										addedAnnotation);
					} catch (Exception e1) {
						throw new GateRuntimeException(e1);
					}
				}
			});
			// extend the annotation by one character on left
			endOffsetExtendRight.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					gate.Annotation annot = ontologyTreeListener
							.highlightedAnnotations.get(selectedIndex);
					int startOffset = annot.getStartNode().getOffset()
							.intValue();
					int endOffset = annot.getEndNode().getOffset().intValue();
					FeatureMap features = annot.getFeatures();
					if (ontologyTreePanel.ontoViewer.getDocument().getContent()
							.size().longValue() == endOffset)
						return;
					endOffset++;
					ontologyTreePanel.deleteAnnotation(annot);
					ontologyTreePanel.ontoViewer.documentTextArea
							.setSelectionStart(startOffset);
					ontologyTreePanel.ontoViewer.documentTextArea
							.setSelectionEnd(endOffset);
					Annotation addedAnnotation = ontologyTreePanel.ontoTreeListener
							.addNewAnnotation(classValue, false, features).get(
									0);
					selectedIndex = ontologyTreeListener
							.highlightedAnnotations.indexOf(
									addedAnnotation);
				}
			});

			deleteAllAnnotations.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (ontologyTreePanel.ontologyViewerOptions
							.getDeleteConfirmation()) {
						Object[] options = new Object[] { "YES", "NO" };
						int confirm = JOptionPane.showOptionDialog(MainFrame
								.getInstance(),
								"Delete Annotation : Are you sure?",
								"Delete Annotation Confirmation",
								JOptionPane.YES_NO_CANCEL_OPTION,
								JOptionPane.QUESTION_MESSAGE, null, options,
								options[0]);
						if (confirm == JOptionPane.YES_OPTION) {
							// we need to find out all annotations with the
							// same string and the same class
							gate.Annotation annot = ontologyTreeListener
									.highlightedAnnotations.get(
											selectedIndex);
							ArrayList<Annotation> annotations = getSimilarAnnotations(annot);
							for (int i = 0; i < annotations.size(); i++) {
								ontologyTreePanel.deleteAnnotation(annotations
										.get(i));
							}
							editClassWindow.setVisible(false);
							ontologyTreePanel.ontoViewer.documentTextArea
									.requestFocus();
						} else {
							editClassWindow.setVisible(false);
							ontologyTreePanel.ontoViewer.documentTextArea
									.requestFocus();
							return;
						}
					} else {
						// we need to find out all annotations with the same
						// string and the same class
						gate.Annotation annot = ontologyTreeListener
								.highlightedAnnotations.get(selectedIndex);
						ArrayList<Annotation> annotations = getSimilarAnnotations(annot);
						for (int i = 0; i < annotations.size(); i++) {
							ontologyTreePanel.deleteAnnotation(annotations
									.get(i));
						}
						editClassWindow.setVisible(false);
						ontologyTreePanel.ontoViewer.documentTextArea
								.requestFocus();
					}
				}
			});
			// ok and now we need to add the editing feature
			final JPanel editPanel = new JPanel();
			editPanel.setLayout(new BorderLayout());
			final JPanel editSubPanel = new JPanel();
			editSubPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			final JComboBox list = new JComboBox();
			final TreeSet<String> sortedSet = new TreeSet<String>(
					ontologyTreePanel.currentClass2IsSelectedMap.keySet());
			ComboBoxModel model = new DefaultComboBoxModel(sortedSet.toArray());
			list.setModel(model);
			list.setMaximumRowCount(5);
			editPanel.add(list, BorderLayout.NORTH);
			final JLabel editField = new JLabel();
			final JButton changeClass = new JButton(MainFrame
					.getIcon("annotation"));
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

			editSubPanel.setBackground(UIManager.getLookAndFeelDefaults()
					.getColor("ToolTip.background"));
			editSubPanel.setOpaque(true);
			editSubPanel.add(changeClass);
			editSubPanel.add(applyToAll);
			editSubPanel.add(startOffsetExtendLeft);
			editSubPanel.add(startOffsetExtendRight);
			editSubPanel.add(deleteAnnotation);
			editSubPanel.add(deleteAllAnnotations);
			editSubPanel.add(endOffsetExtendLeft);
			editSubPanel.add(endOffsetExtendRight);
			editPanel.add(editSubPanel, BorderLayout.SOUTH);
			changeClass.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					if (!ontologyTreePanel.currentClass2ColorMap.keySet()
							.contains((String) list.getSelectedItem())) {
						JOptionPane.showMessageDialog(null, "Class :\"" + field
								+ "\" does not exist!");
						return;
					}
					gate.Annotation annot = (gate.Annotation) ontologyTreeListener
							.highlightedAnnotations.get(selectedIndex);
					changeClassName((String) list.getSelectedItem(), annot,
							false);
				}
			});
			applyToAll.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					if (!ontologyTreePanel.currentClass2ColorMap.keySet()
							.contains((String) list.getSelectedItem())) {
						JOptionPane.showMessageDialog(null, "Class :\"" + field
								+ "\" does not exist!");
						return;
					}
					gate.Annotation annot = (gate.Annotation) ontologyTreeListener
							.highlightedAnnotations.get(selectedIndex);
					changeClassName((String) list.getSelectedItem(), annot,
							true);
				}
			});
			closeWindow.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					editClassWindow.setVisible(false);
					ontologyTreePanel.ontoViewer.documentTextArea
							.requestFocus();
				}
			});
			list.setEditable(true);
			list.getEditor().getEditorComponent().addKeyListener(
					new KeyAdapter() {
						public void keyReleased(KeyEvent keyevent) {
							String s = ((JTextComponent) list.getEditor()
									.getEditorComponent()).getText();
							TreeSet<String> subSortedSet = new TreeSet<String>();
							if (s != null) {
								Iterator<String> classIter = sortedSet
										.iterator();
								while (classIter.hasNext()) {
									String s1 = classIter.next();
									if (s1.toLowerCase().startsWith(
											s.toLowerCase())) {
										subSortedSet.add(s1);
									}
								}
							}
							DefaultComboBoxModel defaultcomboboxmodel = new DefaultComboBoxModel(
									subSortedSet.toArray());
							list.setModel(defaultcomboboxmodel);
							try {
								list.showPopup();
							} catch (Exception exception) {
							}
							((JTextComponent) list.getEditor()
									.getEditorComponent()).setText(s);
						}
					});
			((JTextComponent) list.getEditor().getEditorComponent())
					.setText("");
			JLabel classLabel = new JLabel(("Class : " + classValue));
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
			gate.Annotation annotation = ontologyTreeListener
					.highlightedAnnotations.get(selectedIndex);
			try {
				Rectangle startRect = textPane.modelToView(annotation
						.getStartNode().getOffset().intValue());
				Rectangle endRect = textPane.modelToView(annotation
						.getEndNode().getOffset().intValue());
				Point topLeft = textPane.getLocationOnScreen();
				int x = topLeft.x + startRect.x;
				int y = topLeft.y + endRect.y + endRect.height;
				editClassWindow.setLocation(x, y);
				editClassWindow.pack();
				editClassWindow.validate();
				textPane.removeAll();
				editClassWindow.setVisible(true);
			} catch (BadLocationException e1) {
				// just ignore this
			}
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
			if (showingEditClassWindow) {
				showingEditClassWindow = false;
				editClassWindow.setVisible(false);
				ontologyTreePanel.ontoViewer.documentTextArea.requestFocus();
			}
			int start = annot.getStartNode().getOffset().intValue();
			int end = annot.getEndNode().getOffset().intValue();
			ArrayList<Annotation> annots = new ArrayList<Annotation>();
			// now if all is true then we need to find out all the occurances
			// of same text in the document
			if (all) {
				// so first find out the respective text
				String textToSearchIn = ontologyTreePanel.ontoViewer
						.getDocument().getContent().toString();
				String textToSearch = textToSearchIn.substring(start, end);
				String classToSearch = Utils.getClassFeatureValue(annot);
				ArrayList<Annotation> annotations = ontologyTreeListener
						.highlightedAnnotations;
				for (int i = 0; i < annotations.size(); i++) {
					String textToMatch = textToSearchIn.substring(annotations
							.get(i).getStartNode().getOffset().intValue(),
							annotations.get(i).getEndNode().getOffset()
									.intValue());
					if (textToSearch.equals(textToMatch)
							&& classToSearch.equals(Utils
									.getClassFeatureValue(annotations.get(i)))) {
						annots.add(annotations.get(i));
					}
				}
			} else {
				annots.add(annot);
			}
			for (int i = 0; i < annots.size(); i++) {
				// we need to delete this
				Annotation tempAnnot = (gate.Annotation) annots.get(i);
				ontologyTreePanel.deleteAnnotation(tempAnnot);
				start = tempAnnot.getStartNode().getOffset().intValue();
				end = tempAnnot.getEndNode().getOffset().intValue();
				// and add the new annotation
				ontologyTreePanel.ontoViewer.documentTextArea
						.setSelectionStart(start);
				ontologyTreePanel.ontoViewer.documentTextArea
						.setSelectionEnd(end);
				ontologyTreePanel.ontoTreeListener.addNewAnnotation(
						newClassName, false, tempAnnot.getFeatures());
				ontologyTreePanel.ontoViewer.documentTextArea
						.setSelectionEnd(start);
			}
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
		 * Set the MousePointer Location
		 * 
		 * @param point
		 */
		public void setMousePointer(Point point) {
			this.mousePoint = point;
		}
	}

	/**
	 * This method shows the new annotation window when right clicked on the
	 * selected text
	 * 
	 * @param e
	 */
	public void showNewAnnotationWindow(MouseEvent e) {
		// only show when user had clicked on the selected text and that is
		// not
		// already annoted
		if (showNewAnnotationWindow && isWithinRange(e.getX(), e.getY())) {
			// if the window is already being shown then simply change its
			// location
			if (windowShowing) {
				JTextArea textPane = ontologyTreePanel.ontoViewer.documentTextArea;
				int x1 = textPane.getSelectionStart();
				int y1 = textPane.getSelectionEnd();
				try {
					Rectangle startRect = textPane.modelToView(x1);
					Rectangle endRect = textPane.modelToView(y1);
					Point topLeft = textPane.getLocationOnScreen();
					int x = topLeft.x + startRect.x;
					int y = topLeft.y + endRect.y + endRect.height;
					if (bottomWindow.getX() == x && bottomWindow.getY() == y) {
						// do nothing
						return;
					}
					bottomWindow.setLocation(x, y);
					bottomWindow.pack();
					bottomWindow.setVisible(true);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			} else {
				// otherwise create the window and show it
				TreeSet<String> classNames = new TreeSet<String>(
						ontologyTreePanel.currentClass2ColorMap.keySet());
				classNames.remove(ontologyTreePanel.getCurrentOntology()
						.getName());
				ComboBoxModel model = new DefaultComboBoxModel(classNames
						.toArray());
				combo.setModel(model);
				if (recentUsedClass.length() > 0) {
					((JTextComponent) combo.getEditor().getEditorComponent())
							.setText(recentUsedClass);
				} else {
					((JTextComponent) combo.getEditor().getEditorComponent())
							.setText("");
				}
				combo.setBackground(UIManager.getLookAndFeelDefaults()
						.getColor("ToolTip.background"));
				bottomWindow = new JWindow(
						SwingUtilities
								.getWindowAncestor(ontologyTreePanel.ontoViewer.documentTextualDocumentView
										.getGUI()));
				JPanel pane = new JPanel();
				pane.setBackground(UIManager.getLookAndFeelDefaults().getColor(
						"ToolTip.background"));
				pane.setOpaque(true);
				bottomWindow.setContentPane(pane);
				pane.setLayout(new BorderLayout());
				JPanel subPanel = new JPanel();
				subPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
				subPanel.add(addAnnotation);
				subPanel.add(addAll);
				subPanel.setOpaque(false);
				JPanel labelPanel = new JPanel(new GridLayout(1, 2));
				labelPanel.add(new JLabel("New Annotation"));
				JPanel cancelPanel = new JPanel(new FlowLayout(
						FlowLayout.TRAILING));
				cancelPanel.setOpaque(false);
				cancelPanel.add(cancel);
				labelPanel.add(cancelPanel);
				labelPanel.setOpaque(false);
				pane.add(labelPanel, BorderLayout.NORTH);
				pane.add(combo, BorderLayout.CENTER);
				pane.add(subPanel, BorderLayout.SOUTH);
				pane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
				bottomWindow.pack();
				bottomWindow.validate();
				JTextArea textPane = ontologyTreePanel.ontoViewer.documentTextArea;
				int x1 = textPane.getSelectionStart();
				int y1 = textPane.getSelectionEnd();
				try {
					Rectangle startRect = textPane.modelToView(x1);
					Rectangle endRect = textPane.modelToView(y1);
					Point topLeft = textPane.getLocationOnScreen();
					int x = topLeft.x + startRect.x;
					int y = topLeft.y + endRect.y + endRect.height;
					bottomWindow.setLocation(x, y);
					bottomWindow.pack();
					bottomWindow.setVisible(true);
					windowShowing = true;
				} catch (BadLocationException ble) {
					throw new GateRuntimeException("Can't show the window ",
							ble);
				}
			}
		} else {
			// if user clicks in some outer range areas, it should close the
			// showing window
			if (windowShowing) {
				windowShowing = false;
				if (bottomWindow != null) {
					bottomWindow.setVisible(false);
					ontologyTreePanel.ontoViewer.documentTextArea
							.requestFocus();
				}
			}
		}
	}

	/** Method that tells if the user has clicked on the selected text */
	public boolean isWithinRange(int ccX, int ccY) {
		JTextArea textPane = ontologyTreePanel.ontoViewer.documentTextArea;
		int fHeight = textPane.getFontMetrics(textPane.getFont()).getHeight();
		// ok so now we have these figures
		// posibilities
		// 1. Text selected on the same line
		// scX < ecX && scY == ecY
		if (pscX < pecX && Math.abs(pscY - pecY) <= fHeight) {
			// in this case simply check for the scX <= ccX <= ecX and scY <=
			// ccY <= ecY
			if (pscX <= ccX && ccX <= pecX && pscY - fHeight <= ccY
					&& ccY <= pecY + fHeight) {
				return true;
			} else {
				return false;
			}
		}
		// 2. Text selected on one line but ended on the second or
		// subsequent lines
		// scY != ecY
		if (pscY != pecY) {
			// in this case check the following
			// if(scY <= ccY <= scY + font height) this is the first row so
			// ccX >= scX
			if (pscY - fHeight <= ccY && ccY <= pscY + fHeight) {
				if (ccX >= pscX) {
					return true;
				}
			}
			// if(ecY <= ccY <= ecY + font height) this is the last row so ccX
			// <= ecX
			if (pecY <= ccY && ccY <= pecY + fHeight) {
				if (ccX <= pecX) {
					return true;
				}
			}
			// if(scY + font height <= ccY <= ecY) in between no check for X
			if (pscY + fHeight <= ccY && ccY <= pecY) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Inner class that specifies what to do when user click on any of the
	 * newAnnotationWindowButtons
	 * 
	 * @author niraj
	 */
	private class ComboListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == addAnnotation) {
				windowShowing = false;
				bottomWindow.setVisible(false);
				ontologyTreePanel.ontoViewer.documentTextArea.requestFocus();
				showNewAnnotationWindow = false;
				ontologyTreeListener.newAnnotationMode = false;
				if (!ontologyTreePanel.currentClass2ColorMap.keySet().contains(
						(String) combo.getSelectedItem())) {
					JOptionPane.showMessageDialog(null, "Class :\"" + field
							+ "\" does not exist!");
					return;
				}
				recentUsedClass = (String) combo.getSelectedItem();
				ontologyTreePanel.ontoTreeListener.addNewAnnotation(
						(String) combo.getSelectedItem(), false, null);
			} else if (e.getSource() == cancel) {
				windowShowing = false;
				bottomWindow.setVisible(false);
				ontologyTreePanel.ontoViewer.documentTextArea.requestFocus();
				showNewAnnotationWindow = false;
				ontologyTreeListener.newAnnotationMode = false;
				ontologyTreePanel.ontoViewer.documentTextArea
						.setSelectionStart(ontologyTreePanel.ontoViewer.documentTextArea
								.getSelectionStart());
				ontologyTreePanel.ontoViewer.documentTextArea
						.setSelectionEnd(ontologyTreePanel.ontoViewer.documentTextArea
								.getSelectionStart());
			} else if (e.getSource() == addAll) {
				windowShowing = false;
				bottomWindow.setVisible(false);
				ontologyTreePanel.ontoViewer.documentTextArea.requestFocus();
				showNewAnnotationWindow = false;
				ontologyTreeListener.newAnnotationMode = false;
				if (!ontologyTreePanel.currentClass2ColorMap.keySet().contains(
						field)) {
					JOptionPane.showMessageDialog(null, "Class :\"" + field
							+ "\" does not exist!");
					return;
				}
				recentUsedClass = field;
				ontologyTreePanel.ontoTreeListener.addNewAnnotation(field,
						true, null);
			}
		}
	}

	/**
	 * sets the new annotation window showing status
	 * 
	 * @param value
	 */
	public void setNewAnnotationWindowShowing(boolean value) {
		windowShowing = value;
		showNewAnnotationWindow = value;
	}

	/**
	 * Method gets executed whenever the caret changes its position
	 */
	public void caretUpdate(MouseEvent e) {
		if (windowShowing) {
			windowShowing = false;
			bottomWindow.setVisible(false);
			ontologyTreePanel.ontoViewer.documentTextArea.requestFocus();
		}
		// find out the start and end selection points
		int startSelection = ontologyTreePanel.ontoViewer.documentTextArea
				.getSelectionStart();
		int endSelection = ontologyTreePanel.ontoViewer.documentTextArea
				.getSelectionEnd();
		// see if it is put on the highlighted annotation
		int[] range = ontologyTreeListener.annotationRange;
		int index1 = -1;
		if (range != null) {
			for (int i = 0; i < range.length; i += 2) {
				if (startSelection >= range[i] && endSelection <= range[i + 1]) {
					index1 = (i == 0) ? i : i / 2;
					break;
				}
			}
		}
		final int index = index1;
		// yes it is put on highlighted so show the respective class
		if (range != null && index < range.length && index >= 0) {
			// do nothing
		} else {
			if (startSelection == endSelection)
				return;
			// we want to add the new annotation
			showNewAnnotationWindow = true;
			ontologyTreeListener.newAnnotationMode = true;
		}
	}
}
