package gate.alignment.gui;

import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import gate.*;
import gate.alignment.*;
import gate.composite.CompositeDocument;
import gate.compound.CompoundDocument;
import gate.creole.*;
import gate.gui.MainFrame;
import gate.swing.ColorGenerator;
import gate.util.GateRuntimeException;
import gate.util.InvalidOffsetException;

/**
 * This class is the extension of the GATE Document viewer/editor. It provides a
 * editor for aligning texts in a compound document.
 */
public class AlignmentEditor extends AbstractVisualResource implements
		ActionListener {

	private static final long serialVersionUID = -2867467022258265114L;

	// mainPanel is a container for all, paramPanel is a container for user
	// parameters and waPanel contains JEditorPanes for each document
	private JPanel mainPanel, paramPanel, waPanel;

	// TextFields for users to provide parameter
	private JTextField inputASName, parentOfUnitOfAlignment, unitOfAlignment;

	private JButton populate, next, previous, align, resetAlignment;

	private JEditorPane[] tas;

	private CompoundDocument document;

	private List<String> languages;

	private AlignmentFactory alignFactory;

	private Alignment alignment;

	private HashMap<JEditorPane, HashMap<Annotation, AnnotationHighlight>> highlights;

	private HashMap<JEditorPane, List<Annotation>> latestAlignedAnnotations;

	private Color color, unitColor;

	private ColorGenerator colorGenerator = new ColorGenerator();

	private HashMap<String, Annotation> lastUsedSettings;

	public static final int TEXT_SIZE = 20;

	/*
	 * (non-Javadoc)
	 * 
	 * @see gate.Resource#init()
	 */
	public Resource init() throws ResourceInstantiationException {
		highlights = new HashMap<JEditorPane, HashMap<Annotation, AnnotationHighlight>>();
		latestAlignedAnnotations = new HashMap<JEditorPane, List<Annotation>>();
		alignment = new Alignment();
		return this;
	}

	/**
	 * Initialize the GUI
	 */
	private void initGui() {
		mainPanel = new JPanel(new BorderLayout());
		paramPanel = new JPanel(new GridLayout(4, 1));

		waPanel = new JPanel(new GridLayout(languages.size(), 1));

		inputASName = new JTextField(40);
		parentOfUnitOfAlignment = new JTextField(40);
		unitOfAlignment = new JTextField(40);
		populate = new JButton("Populate");
		JPanel temp1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel temp2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel temp3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel temp6 = new JPanel(new FlowLayout(FlowLayout.LEFT));

		temp1.add(new JLabel("Input Annotation Set Name:"));
		temp1.add(inputASName);
		temp2.add(new JLabel("Parent of Unit of Alignment (e.g. Sentence):"));
		temp2.add(parentOfUnitOfAlignment);
		parentOfUnitOfAlignment.setText("Sentence");
		temp3.add(new JLabel("Unit of Alignment (e.g. Token):"));
		temp3.add(unitOfAlignment);
		unitOfAlignment.setText("Token");
		temp6.add(populate);
		paramPanel.add(temp1);
		paramPanel.add(temp2);
		paramPanel.add(temp3);
		paramPanel.add(temp6);

		mainPanel.add(paramPanel, BorderLayout.NORTH);
		populate.addActionListener(this);

		previous = new JButton("< Previous");
		previous.addActionListener(this);
		next = new JButton("Next >");
		next.addActionListener(this);
		align = new JButton("Align");
		align.addActionListener(this);
		resetAlignment = new JButton("Reset Alignment");
		resetAlignment.addActionListener(this);
		temp6.add(previous);
		temp6.add(next);
		temp6.add(align);
		temp6.add(resetAlignment);

		tas = new JEditorPane[languages.size()];
		// now we need to create TextArea for each language
		for (int i = 0; i < languages.size(); i++) {
			tas[i] = new JEditorPane();
			tas[i].setBorder(new TitledBorder((String) languages.get(i)));
			waPanel.add(new JScrollPane(tas[i]));
			tas[i].setFont(new Font(tas[i].getFont().getName(), Font.PLAIN,
					TEXT_SIZE));
			tas[i].addMouseListener(new MouseActionListener());
			tas[i].addMouseMotionListener(new MouseActionListener());
			tas[i].setEditable(false);
		}
		mainPanel.add(waPanel);
		this.setLayout(new BorderLayout());
		this.add(mainPanel, BorderLayout.CENTER);
		unitColor = getColor(new Color(234, 245, 246));
		color = getColor(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gate.VisualResource#setTarget(java.lang.Object)
	 */
	public void setTarget(Object target) {
		this.document = (CompoundDocument) target;
		this.languages = new ArrayList<String>(this.document.getDocumentIDs());
		this.languages.remove(CompositeDocument.COMPOSITE_DOC_NAME);
		if (this.document.getFeatures().get(
				AlignmentFactory.ALIGNMENT_FEATURE_NAME) != null) {
			this.alignment = (Alignment) this.document.getFeatures().get(
					AlignmentFactory.ALIGNMENT_FEATURE_NAME);
		} else {
			this.document.getFeatures().put(
					AlignmentFactory.ALIGNMENT_FEATURE_NAME, alignment);
		}
		initGui();
	}

	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == populate) {
			try {
				AlignmentFactory af = new AlignmentFactory(document,
						inputASName.getText(), unitOfAlignment.getText(),
						parentOfUnitOfAlignment.getText(),
						"gate.util.OffsetComparator");
				// if there were no errors
				alignFactory = af;
				nextAction();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (ae.getSource() == next) {
			nextAction();
		} else if (ae.getSource() == previous) {
			previousAction();
		} else if (ae.getSource() == resetAlignment) {
			int input = JOptionPane.showConfirmDialog(MainFrame.getInstance(),
					"This will remove all alignments of this document");
			if (input == JOptionPane.YES_OPTION) {
				alignment = new Alignment();
				this.document.getFeatures().put(
						AlignmentFactory.ALIGNMENT_FEATURE_NAME, alignment);
				if (lastUsedSettings != null)
					updateGUI(lastUsedSettings);
			}

		} else if (ae.getSource() == align) {
			color = getColor(null);
			for (int i = 0; i < this.languages.size(); i++) {
				String srcLang = languages.get(i);
				List<Annotation> srcAnnotations = latestAlignedAnnotations
						.get(tas[i]);
				if (srcAnnotations == null || srcAnnotations.isEmpty())
					continue;
				for (int j = 0; j < tas.length; j++) {
					if (i == j)
						continue;

					String targetLang = languages.get(j);
					List<Annotation> targetAnnotations = latestAlignedAnnotations
							.get(tas[j]);
					if (targetAnnotations == null
							|| targetAnnotations.isEmpty())
						continue;
					alignment.align(srcAnnotations, this.document
							.getDocument(srcLang), targetAnnotations,
							this.document.getDocument(targetLang));
				}
			}
			latestAlignedAnnotations.clear();
		}
	}

	private void nextAction() {
		if (alignFactory.hasNext()) {
			updateGUI(alignFactory.next());
		}
	}

	/**
	 * This method updates the GUI.
	 * 
	 * @param langAndAnnots
	 */
	private void updateGUI(HashMap<String, Annotation> langAndAnnots) {
		// before refreshing, we remove all the highlights
		lastUsedSettings = langAndAnnots;
		latestAlignedAnnotations.clear();
		this.highlights.clear();

		// first we show all the annotations and then highlight each unit using
		// a default highlight color
		// landAndAnnots has a language (e.g. en or hi) as key and the parent of
		// the unit of alignment (e.g. Sentence) as the value.
		for (String lang : langAndAnnots.keySet()) {
			Annotation annot = langAndAnnots.get(lang);
			// find out the index of jeditor pane (each lang has a different
			// editor pane associtated to it)
			int index = languages.indexOf(lang);

			// gets the annotation's underlying text (e.g. full sentence)
			String text = alignFactory.getText(annot, lang);

			// remove all the highlights
			tas[index].getHighlighter().removeAllHighlights();

			// set the text
			tas[index].setEditable(true);
			tas[index].setText(text);
			tas[index].setEditable(false);
			tas[index].updateUI();

			// we need to highlight the unit type
			AnnotationSet set = alignFactory.getUnderlyingAnnotations(annot,
					lang);

			// if there are not underlying annotations, just return
			if (set == null) {
				return;
			}

			// for each underlying unit of alignment, we create a default
			// annotation highlight highlight object contains the offsets, the
			// source annotation and the color object used highlighting the
			// annotation
			HashMap<Annotation, AnnotationHighlight> highlights = new HashMap<Annotation, AnnotationHighlight>();
			Iterator subiter = set.iterator();
			while (subiter.hasNext()) {
				Annotation annot1 = (Annotation) subiter.next();
				int start = annot1.getStartNode().getOffset().intValue();
				int end = annot1.getEndNode().getOffset().intValue();

				// readjust the offset with respect to the offsets of the parent
				// of unit of alignment annotation
				start -= annot.getStartNode().getOffset().intValue();
				end -= annot.getStartNode().getOffset().intValue();

				// and finally add a new highlight
				try {
					AnnotationHighlight ah = new AnnotationHighlight(unitColor,
							start, end, annot1);
					highlights.put(annot1, ah);
					tas[index].getHighlighter().addHighlight(start, end, ah);
				} catch (BadLocationException ble) {
					throw new GateRuntimeException(ble);
				}
			}

			this.highlights.put(tas[index], highlights);
		}

		// now we need to highlight the aligned annotations if there are any
		Set<Annotation> setOfalignedAnnotations = alignment
				.getAlignedAnnotations();
		// we keep record of which annotations are already highlighted in order
		// to not to highlight them again
		Set<Annotation> highlightedAnnotations = new HashSet<Annotation>();

		// one annotation at a time
		for (Annotation srcAnnotation : setOfalignedAnnotations) {

			// if already highlighted, don't do it again
			if (highlightedAnnotations.contains(srcAnnotation))
				continue;

			// find out the language/id of the document
			String language = alignment.getDocument(srcAnnotation).getName();

			// find out the editor pane
			JEditorPane pane = tas[this.languages.indexOf(language)];

			// and find out the default annotation highlight
			AnnotationHighlight ah = getHighlight(pane, srcAnnotation);
			if (ah == null)
				continue;

			// ok we need to generate a new color
			Color newColor = getColor(null);

			// and we add the highlight
			try {
				Object obj = pane.getHighlighter()
						.addHighlight(
								ah.start,
								ah.end,
								new DefaultHighlighter.DefaultHighlightPainter(
										newColor));
				ah.setAlignedHighlight(obj, newColor);
				ah.aligned = true;
				highlightedAnnotations.add(srcAnnotation);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// find out the annotations that are aligned to the srcAnnotation
			Set<Annotation> alignedAnnotations = alignment
					.getAlignedAnnotations(srcAnnotation);
			boolean firstTime = true;
			for (Annotation targetAnnotation : alignedAnnotations) {

				// already highlighted??
				if (highlightedAnnotations.contains(targetAnnotation))
					continue;

				String targetAnnLang = alignment.getDocument(targetAnnotation)
						.getName();

				// find out text editor
				pane = tas[this.languages.indexOf(targetAnnLang)];
				ah = getHighlight(pane, targetAnnotation);
				if (ah == null || ah.aligned)
					continue;

				try {
					Object obj = pane.getHighlighter().addHighlight(
							ah.start,
							ah.end,
							new DefaultHighlighter.DefaultHighlightPainter(
									newColor));
					ah.setAlignedHighlight(obj, newColor);
					highlightedAnnotations.add(targetAnnotation);
					ah.aligned = true;
				} catch (Exception e) {
					e.printStackTrace();
				}

				// the following loop is necessary
				// e.g. h1, h2, h3 are aligned with e1, e2 and e3
				// h1 is the srcAnnotation and under consideration
				// in the inner loop we are iterating over e1, e2 and e3
				// say we are looking at the e1
				// so the following loop will find out the e1 is aligned with h2
				// and h3 and will highlight them.
				// This helps as we want to use the
				// same color for all h1, h2, h3, e1, e2 and e3
				Set<Annotation> alignedAlignedAnnotations = alignment
						.getAlignedAnnotations(targetAnnotation);

				for (Annotation targetTargetAnnotation : alignedAlignedAnnotations) {
					if (highlightedAnnotations.contains(targetTargetAnnotation))
						continue;

					String targetTargetAnnLang = alignment.getDocument(
							targetTargetAnnotation).getName();

					// find out text editor
					pane = tas[this.languages.indexOf(targetTargetAnnLang)];
					ah = getHighlight(pane, targetTargetAnnotation);
					if (ah == null || ah.aligned)
						continue;

					try {
						Object obj = pane.getHighlighter().addHighlight(
								ah.start,
								ah.end,
								new DefaultHighlighter.DefaultHighlightPainter(
										newColor));
						ah.setAlignedHighlight(obj, newColor);
						highlightedAnnotations.add(targetTargetAnnotation);
						ah.aligned = true;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Find out the annotation highlight for the given annotation
	 * 
	 * @param pane
	 * @param annot
	 * @return
	 */
	private AnnotationHighlight getHighlight(JEditorPane pane, Annotation annot) {
		HashMap<Annotation, AnnotationHighlight> highlights = this.highlights
				.get(pane);
		if (highlights == null || highlights.isEmpty())
			return null;
		return highlights.get(annot);
	}

	private void previousAction() {
		if (alignFactory.hasPrevious()) {
			updateGUI(alignFactory.previous());
		}
	}

	public void processFinished() {
		this.document.setCurrentDocument("null");
	}

	public void progressChanged(int prgress) {
	}

	protected class AnnotationHighlight extends
			DefaultHighlighter.DefaultHighlightPainter {
		int start;

		int end;

		boolean aligned = false;

		Object alignedHighlight;

		Color color;

		Annotation annotation;

		public AnnotationHighlight(Color color, int start, int end,
				Annotation annot) {
			super(color);
			this.start = start;
			this.end = end;
			this.annotation = annot;
		}

		public void setAligned(boolean val) {
			this.aligned = val;
		}

		public void setAlignedHighlight(Object ah, Color color) {
			this.alignedHighlight = ah;
			this.color = color;
		}

		public Object getAlignedHighlight() {
			return this.alignedHighlight;
		}

		public boolean isWithinBoundaries(int location) {
			return location >= start && location <= end;
		}
	}

	protected class MouseActionListener extends MouseInputAdapter {
		JPopupMenu popup = new JPopupMenu();

		public void mouseClicked(MouseEvent me) {
			JEditorPane ta = (JEditorPane) me.getSource();

			// firstly obtain all highlights for this editor pane
			// it contains an annotationHighlight for each annotation
			HashMap<Annotation, AnnotationHighlight> hilights = highlights
					.get(ta);

			// and iterate through each annotation highlight
			// and find out where did the user click
			ArrayList<AnnotationHighlight> hilites = new ArrayList<AnnotationHighlight>(
					hilights.values());

			for (int i = 0; i < hilites.size(); i++) {
				AnnotationHighlight ah = hilites.get(i);
				Point pt = me.getPoint();
				int location = ta.viewToModel(pt);

				// did user click on this annotation highlight?
				if (ah.isWithinBoundaries(location)) {

					// was this highlight aligned?
					// if yes, unalign it
					if (ah.aligned) {
						// try to obtain the highlight
						if (ah.color.equals(color)) {
							Object ah1 = ah.getAlignedHighlight();
							if (ah1 != null) {
								ta.getHighlighter().removeHighlight(ah1);
							}
							ah.aligned = false;
							ah.setAlignedHighlight(null, null);

							// we stored the recent clicks in
							// latestAlignedAnnotations
							// sch that when user clicks on the align button
							// we align the annotations from this hashmap
							if (latestAlignedAnnotations == null)
								latestAlignedAnnotations = new HashMap<JEditorPane, List<Annotation>>();

							List<Annotation> annotations = latestAlignedAnnotations
									.get(ta);
							if (annotations == null) {
								annotations = new ArrayList<Annotation>();
							}

							annotations.remove(ah.annotation);
							latestAlignedAnnotations.put(ta, annotations);
						}
					} else {
						try {
							Color toUse = color;
							if (ah.color != null) {
								toUse = ah.color;
							}
							Object obj = ta
									.getHighlighter()
									.addHighlight(
											ah.start,
											ah.end,
											new DefaultHighlighter.DefaultHighlightPainter(
													toUse));
							ah.setAlignedHighlight(obj, toUse);
							ah.aligned = true;
							if (latestAlignedAnnotations == null)
								latestAlignedAnnotations = new HashMap<JEditorPane, List<Annotation>>();

							List<Annotation> annotations = latestAlignedAnnotations
									.get(ta);
							if (annotations == null) {
								annotations = new ArrayList<Annotation>();
							}

							if (!annotations.contains(ah.annotation)) {
								annotations.add(ah.annotation);
							}

							latestAlignedAnnotations.put(ta, annotations);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					return;
				}
			}
		}

		public void mouseMoved(MouseEvent me) {
			// if (popup.isVisible()) {
			// popup.setVisible(false);
			// }
			// JEditorPane ta = (JEditorPane) me.getSource();
			// HashMap<Annotation, AnnotationHighlight> hilights =
			// highlights.get(ta);
			// if (hilights == null || hilights.isEmpty())
			// return;
			// ArrayList<AnnotationHighlight> hilites = new
			// ArrayList<AnnotationHighlight>(hilights.values());
			// for (int i = 0; i < hilites.size(); i++) {
			// AnnotationHighlight ah = hilites.get(i);
			// Point pt = me.getPoint();
			// int location = ta.viewToModel(pt);
			// if (ah.isWithinBoundaries(location)) {
			// if (ah.aligned) {
			// popup.removeAll();
			// Iterator iter = ah.annotation.getFeatures().keySet()
			// .iterator();
			// while (iter.hasNext()) {
			// Object key = iter.next();
			// String keyV = key.toString();
			// String valueV = ah.annotation.getFeatures()
			// .get(key).toString();
			// popup.add(new JLabel(keyV + ":" + valueV));
			// }
			// popup.show(ta, pt.x + 5, pt.y + 10);
			// popup.setVisible(true);
			// }
			// }
			// }
		}
	}

	private Color getColor(Color c) {
		float components[] = null;
		if (c == null)
			components = colorGenerator.getNextColor().getComponents(null);
		else
			components = c.getComponents(null);

		Color colour = new Color(components[0], components[1], components[2],
				0.5f);
		int rgb = colour.getRGB();
		int alpha = colour.getAlpha();
		int rgba = rgb | (alpha << 24);
		colour = new Color(rgba, true);
		return colour;
	}

	/**
	 * This class provides various methods that help in alignment process.
	 * 
	 * @author niraj
	 */
	private class AlignmentFactory {

		public static final String ALIGNMENT_FEATURE_NAME = "alignment";

		protected CompoundDocument compoundDocument;

		protected String inputAS;

		protected String tokenAnnotationType;

		protected String comparatorClass;

		protected Comparator comparator;

		private String unitAnnotationType;

		private List<String> documentIDs;

		private HashMap<String, AASequence> asMap;

		/**
		 * AlignmentFactory makes alignment easier
		 * 
		 * @param compoundDocument ->
		 *            document where we want to achieve alignment
		 * @param inputAS ->
		 *            name of the inputAnnotationSet
		 * @param tokenAnnotationType ->
		 *            the level at what we want to achieve alignment (e.g. Token
		 *            or may be some other annotation type)
		 * @param unitAnnotationType ->
		 *            AlignedParentAnnotationType (e.g. if sentences are already
		 *            aligned)
		 * @throws Exception
		 */
		public AlignmentFactory(CompoundDocument alignedDocument,
				String inputAS, String tokenAnnotationType,
				String unitAnnotationType, String comparatorClass)
				throws Exception {

			this.compoundDocument = alignedDocument;
			this.inputAS = inputAS;
			this.tokenAnnotationType = tokenAnnotationType;
			this.unitAnnotationType = unitAnnotationType;
			this.comparatorClass = comparatorClass;
			init();
		}

		private void init() throws ClassNotFoundException,
				IllegalAccessException, InstantiationException {
			comparator = (Comparator) Class.forName(comparatorClass)
					.newInstance();
			documentIDs = compoundDocument.getDocumentIDs();
			asMap = new HashMap<String, AASequence>();
			for (int i = 0; i < documentIDs.size(); i++) {
				String lang = documentIDs.get(i);
				Document doc = compoundDocument.getDocument(lang);
				if (doc instanceof CompositeDocument) {
					documentIDs.remove(i);
					i--;
					continue;
				}

				AnnotationSet as = inputAS == null
						|| inputAS.trim().length() == 0 ? doc.getAnnotations()
						: doc.getAnnotations(inputAS);
				AASequence aas = new AASequence(doc, as);
				asMap.put(lang, aas);
			}
		}

		public String getText(Annotation annot, String language) {
			AASequence seq = asMap.get(language);
			if (seq == null) {
				return null;
			}

			try {
				return seq.getText(annot);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		public AnnotationSet getAnnotationSet(String language) {
			AASequence seq = asMap.get(language);
			if (seq == null) {
				return null;
			}
			return seq.set;
		}

		public AnnotationSet getUnderlyingAnnotations(Annotation annot,
				String language) {
			AASequence seq = asMap.get(language);
			if (seq == null) {
				return null;
			}

			try {
				return seq.getUnderlyingAnnotations(annot, tokenAnnotationType);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		/**
		 * The method returns a hashmap that has the following format e.g. en ->
		 * english sentence (if document is sentence algined and user wants to
		 * perform word alignment) e.g. hi -> hindi sentence
		 * 
		 * @return
		 */
		public HashMap<String, Annotation> next() {
			HashMap<String, Annotation> annotations = new HashMap<String, Annotation>();
			for (String lang : documentIDs) {
				annotations.put(lang, ((AASequence) asMap.get(lang)).next());
			}
			return annotations;
		}

		public HashMap<String, Annotation> previous() {
			HashMap<String, Annotation> annotations = new HashMap<String, Annotation>();
			for (String lang : documentIDs) {
				annotations
						.put(lang, ((AASequence) asMap.get(lang)).previous());
			}
			return annotations;
		}

		public boolean hasNext() {
			boolean available = true;
			for (int i = 0; i < documentIDs.size(); i++) {
				String lang = (String) documentIDs.get(i);
				available = (available && ((AASequence) asMap.get(lang))
						.hasNext());
			}
			return available;
		}

		public boolean hasPrevious() {
			boolean available = true;
			for (int i = 0; i < documentIDs.size(); i++) {
				String lang = (String) documentIDs.get(i);
				available = (available && ((AASequence) asMap.get(lang))
						.hasPrevious());
			}
			return available;
		}

		public List<String> getDocumentIDs() {
			return documentIDs;
		}

		class AASequence {
			Document document;

			AnnotationSet set;

			List<Annotation> annotations;

			int counter = -1;

			public AASequence(Document doc, AnnotationSet set) {
				this.document = doc;
				this.set = set;
				// collecting all sentences for example
				annotations = new ArrayList<Annotation>(set
						.get(unitAnnotationType));
				Collections.sort(annotations, comparator);
			}

			public boolean hasNext() {
				if (counter + 1 < annotations.size()) {
					return true;
				} else {
					return false;
				}
			}

			// return next sentence
			public Annotation next() {
				counter++;
				return annotations.get(counter);
			}

			public Annotation previous() {
				counter--;
				return annotations.get(counter);
			}

			public boolean hasPrevious() {
				if (counter - 1 >= 0) {
					return true;
				}
				return false;
			}

			public void reset() {
				counter = -1;
			}

			public AnnotationSet getUnderlyingAnnotations(
					Annotation parentAnnot, String annotationType) {
				return set.getContained(parentAnnot.getStartNode().getOffset(),
						parentAnnot.getEndNode().getOffset()).get(
						annotationType);
			}

			public String getText(Annotation ann) throws InvalidOffsetException {
				return document.getContent().getContent(
						ann.getStartNode().getOffset(),
						ann.getEndNode().getOffset()).toString();
			}
		}
	}
}