package debugger.gui.debugging;

import debugger.gui.GuiFactory;
import debugger.gui.debugging.debugviews.PrimaryTextPanel;
import debugger.resources.ResourcesFactory;
import debugger.resources.pr.PhaseModel;
import debugger.resources.pr.RuleModel;
import debugger.resources.pr.RuleTrace;
import debugger.resources.pr.TraceContainer;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.FeatureMap;
import gate.util.InvalidOffsetException;
import gate.util.OffsetComparator;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import javax.swing.text.StyledEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/) <br>
 * This class creates GUI of debugging (now TraceHistory) part of JAPEDebugger.
 * @author Andrey Shafirin, Vladimir Karasev
 */

public class TraceHistoryPanel extends JPanel {
    private JPanel rulePanel;
    private JLabel phaseName;
    private JLabel ruleName;
    private JTextPane selectedTextPane;
    private JTextPane rulePane;
    private JButton nextAnnotationButton;
    private JButton previousAnnotationButton;

    private PhaseModel currentPhaseModel;
    private RuleModel currentRuleModel;
    private TraceContainer traceContainer;
    private Document document;
    private int currentStartOffset;
    private int currentEndOffset;
    private TraceContainer currentTraces;
    private RuleTrace currentRuleTrace;
    private AnnotationSet currentAnnotationCut;

    public TraceHistoryPanel() {
        initGui();
    }

    private void initGui() {
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        this.add(getUpperPanel(), c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.BOTH;

        JScrollPane ruleScrollPane = new JScrollPane(getRulePane(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        ruleScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Rule Text"));

        JScrollPane panelScrollPane = new JScrollPane(getRulePanel(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panelScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Rule"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelScrollPane, ruleScrollPane);
        splitPane.setDividerLocation(200);
//        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPane, getSelectedTextPane());
//        sp.setDividerLocation(300);
//        this.add(sp, c);
        this.add(splitPane, c);
        c.gridy = 2;
        c.weighty = 0;
        c.fill = GridBagConstraints.NONE;
//        this.add(getSelectedTextPane(), c);
    }

    private JTextPane getRulePane() {
        if (rulePane == null) {
            rulePane = new JTextPane();
            rulePane.setEditorKit(new StyledEditorKit());
            rulePane.setDocument(new SyntaxDocument());
            rulePane.setEditable(false);
            // prevents autoscrolling of the caret
            rulePane.setCaret(new DefaultCaret() {
                protected void adjustVisibility(Rectangle nloc) {
                }
            });
        }
        return rulePane;
    }

    private JComponent getSelectedTextPane() {
        selectedTextPane = new JTextPane();
        selectedTextPane.setEditable(false);
        // prevents autoscrolling of the caret
        selectedTextPane.setCaret(new DefaultCaret() {
            protected void adjustVisibility(Rectangle nloc) {
            }
        });
        nextAnnotationButton = new JButton("Next");
        previousAnnotationButton = new JButton("Previous");
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
//        panel.add(selectedTextPane, c);
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;//1;
        c.weightx = 0;
        c.weighty = 0;
        panel.add(previousAnnotationButton, c);
        previousAnnotationButton.setEnabled(false);
        previousAnnotationButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (currentTraces != null && currentRuleModel != null && currentRuleTrace != null) {
                    int index = currentTraces.lastIndexOf(currentRuleTrace);
                    if (index > 0) {
                        updateRulePanel(currentRuleModel, (RuleTrace) currentTraces.get(index - 1));
                        currentRuleTrace = (RuleTrace) currentTraces.get(index - 1);
                    }
                    if (index - 1 == 0) {
                        previousAnnotationButton.setEnabled(false);
                    } else {
                        previousAnnotationButton.setEnabled(true);
                    }
                    if (index < currentTraces.size() && currentTraces.size() > 1) {
                        nextAnnotationButton.setEnabled(true);
                    }
                }
                GuiFactory.getResourceView().repaint();
            }
        });
        c.gridx = 1;//2;
        panel.add(nextAnnotationButton, c);
        nextAnnotationButton.setEnabled(false);
        nextAnnotationButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (currentTraces != null && currentRuleModel != null && currentRuleTrace != null) {
                    int index = currentTraces.lastIndexOf(currentRuleTrace);
                    if (index + 1 < currentTraces.size()) {
                        updateRulePanel(currentRuleModel, (RuleTrace) currentTraces.get(index + 1));
                        currentRuleTrace = (RuleTrace) currentTraces.get(index + 1);
                    }
                    if (index + 2 >= currentTraces.size()) {
                        nextAnnotationButton.setEnabled(false);
                    } else {
                        nextAnnotationButton.setEnabled(true);
                    }
                    if (index + 1 > 0) {
                        previousAnnotationButton.setEnabled(true);
                    }
                    GuiFactory.getResourceView().repaint();
                }
            }
        });
        return panel;
    }

    private JComponent getRulePanel() {
        if (rulePanel == null) {
            rulePanel = new JPanel();
        }
        return rulePanel;
    }

    private JComponent getUpperPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 4, 0, 0);
        panel.add(new JLabel("Rule: "), c);

        c.gridx = 1;
        ruleName = new JLabel();
        panel.add(ruleName, c);

        c.gridx = 2;
        c.weightx = 1;
        panel.add(new JPanel(), c);
        c.gridx = 3;
        c.weightx = 0;
        panel.add(new JPanel(), c);
        c.gridx = 3;
        c.gridwidth = 2;
        panel.add(new JPanel(), c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        c.gridwidth = 3;
        panel.add(new JPanel(), c);

        c.gridwidth = 1;
        c.gridx = 3;
        c.weightx = 0;
        c.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Phase: "), c);

        c.gridx = 4;
        phaseName = new JLabel();
        panel.add(phaseName, c);

        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 4, 0);
        panel.add(getSelectedTextPane(), c);

        return panel;
    }

    /**
     * This method is called from <code>ShowResultAction</code> action, when user selects
     * text, on which he'd like to see the results of matching the rules.
     * @param startOffset
     * @param endOffset
     * @param document
     */
    public void setText(int startOffset, int endOffset, Document document) {
        TraceContainer traceContainer = ResourcesFactory.getPhaseController().getRuleTrace();
        traceContainer = traceContainer.getTraceByOffset(new Long(startOffset), new Long(endOffset));
        try {
            this.selectedTextPane.setText(
            		document.getContent().getContent(new Long(min(startOffset,
            				document.getContent().size().longValue() - 1L)), 
            				new Long(min(endOffset, 
            						document.getContent().size().longValue() - 1L)))
							.toString());
        } catch (InvalidOffsetException e) {
        	System.out.println("Start offset = "+startOffset+
        			"End offset = "+endOffset);
        	System.out.println("Document size = "+document.getContent().size());
            e.printStackTrace();
        }
        this.traceContainer = traceContainer;
        this.document = document;
        this.currentStartOffset = startOffset;
        this.currentEndOffset = endOffset;
        //////
        this.updateRulePanel(null, null);
        //////
        this.repaint();
    }

    /**
     * Return whichever of two longs has the smallest value.
     * 
     * @param a The first of the longs.
     * @param b The second of the longs.
     * @return The value of the long that's smallest.
     */
    private long min(long a, long b) {
    	if (a < b) 
    		return a;
    	else
    		return b;
    }
    
    /**
     * This method creates a panel with annotations of the given input type, which are
     * contained in a selected interval. All of the annotations which matched in the
     * current selected rule are highlighted. (Input type is one of the inputs in the phase, to
     * which current rule belongs.)
     * @param annotations <code>AnnotationSet</code> with annotations of the given input type
     *        from a selected interval (offsets are <code>currentStartOffset</code> and
     *        <code>currentEndOffset</code>)
     * @param annotationsType type of annotations, i.e. Lookup, Morph
     * @param ruleTrace <code>RuleTrace</code> of currently selected rule, can be null,
     *        if no rule is selected
     * @param withHighlighting whether highlighting is on/off
     * @return
     */
    private JComponent createAnnotationPanel(AnnotationSet annotations, String annotationsType, RuleTrace ruleTrace, boolean withHighlighting) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;

        // Matched annotations
        AnnotationSet annotationsToHighlight = null;
        if (ruleTrace != null) {
            annotationsToHighlight = ruleTrace.getAnnotations();
        }

        long lastNodeOffset = 0;
        if (annotationsToHighlight != null) {
            lastNodeOffset = annotationsToHighlight.lastNode().getOffset().longValue();
        }

        // Annotations to display
        HashSet<Annotation> annotationsToShowSet = new HashSet<Annotation>();
        if (annotations != null) {
            annotationsToShowSet.addAll(annotations);
        }

        // Add some Token annotations to displayed annotations
        HashSet<Annotation> tokensNotToBeIncluded = new HashSet<Annotation>();
        for (Iterator<Annotation> it = annotationsToShowSet.iterator(); it.hasNext();) {
            Annotation currentAnnotation = it.next();
            AnnotationSet containedTokens = currentAnnotationCut.getContained(currentAnnotation.getStartNode().getOffset(), currentAnnotation.getEndNode().getOffset()).get("Token");
            if (containedTokens != null) {
                tokensNotToBeIncluded.addAll(containedTokens);
            }
        }
        AnnotationSet tokenAnnotationSet = currentAnnotationCut.get("Token", new Long(currentStartOffset), new Long(currentEndOffset));
        if (tokenAnnotationSet != null) {
            for (Iterator<Annotation> it = tokenAnnotationSet.iterator(); it.hasNext();) {
                Annotation currentAnnotation = it.next();
                if (!tokensNotToBeIncluded.contains(currentAnnotation)) {
                    annotationsToShowSet.add(currentAnnotation);
                }
            }
        }
        // end of adding tokens

        // Now let's sort all the annotations we'd like to display
        ArrayList<Annotation> list = new ArrayList<Annotation>(annotationsToShowSet);
        Collections.sort(list, new OffsetComparator());
        /*
         * Offset comparator doesn't always work as
         * we need - the longest annotation with
         * the same start offset should ALWAYS be first.
         * So I have to use my own comparator.
         */
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                Annotation a1 = (Annotation) o1;
                Annotation a2 = (Annotation) o2;
                long offset1 = a1.getStartNode().getOffset().longValue();
                long offset2 = a2.getStartNode().getOffset().longValue();
                long length1 = a1.getEndNode().getOffset().longValue() - offset1;
                long length2 = a2.getEndNode().getOffset().longValue() - offset2;
                if (offset1 > offset2) {
                    return 1;
                }
                if (offset1 < offset2) {
                    return -1;
                }
                if (offset1 == offset2) {
                    if (length1 > length2)
                        return -1;
                    else if (length1 < length2)
                        return 1;
                    else
                        return 0;
                }
                return 0;
            }
        });
        // end of sorting

        // Annotations of the same type, which should not be displayed -
        // for example, if we have several lookups - only one lookup should be displayed
        ArrayList<Annotation> annotationsToIgnore = new ArrayList<Annotation>();
        boolean isRedSet = false;

        int x = 0;
        for (Iterator<Annotation> it = list.iterator(); it.hasNext();) {
            Annotation currentAnnotation = it.next();

            // Get annotations of the same type with the same offsets and leave only one to display
            AnnotationSet containedAnnotationsOfTheSameType = currentAnnotationCut.getContained(currentAnnotation.getStartNode().getOffset(), currentAnnotation.getEndNode().getOffset()).get(annotationsType);
            if (containedAnnotationsOfTheSameType != null) {
                for (Iterator<Annotation> iter = containedAnnotationsOfTheSameType.iterator(); iter.hasNext();) {
                    Annotation ann = iter.next();
                    if (!ann.equals(currentAnnotation)) {
                        annotationsToIgnore.add(ann);
                    }
                }
            }

            // I get text to display from the Token annotations
            AnnotationSet containedTokens = currentAnnotationCut.getContained(currentAnnotation.getStartNode().getOffset(), currentAnnotation.getEndNode().getOffset()).get("Token");
            ArrayList<Annotation> containedTokensList = null;
            if (containedTokens != null) {
                containedTokensList = new ArrayList<Annotation>(containedTokens);
            } else {
                containedTokensList = new ArrayList<Annotation>();
            }
            Collections.sort(containedTokensList, new OffsetComparator());

            // This variable shows the number of the created panel in a row
            int order = 0;
            if (!annotationsToIgnore.contains(currentAnnotation)) {
                // At last we create the panels with (or without) text on them
                for (Iterator<Annotation> iter = containedTokensList.iterator(); iter.hasNext();) {
                    Annotation currentTokenAnnotation = iter.next();
                    String text = (String) currentTokenAnnotation.getFeatures().get("string");
                    PrimaryTextPanel textPanel = new PrimaryTextPanel(text, false, PrimaryTextPanel.OPEN_NONE);
                    if (containedAnnotationsOfTheSameType != null) {
                        textPanel.setAnnotations(new ArrayList(containedAnnotationsOfTheSameType));
                    } else {
                        ArrayList arr = new ArrayList();
                        arr.add(currentAnnotation);
                        textPanel.setAnnotations(arr);
                    }
                    if (containedTokens.size() == 1) {
                        textPanel.setOpenMode(PrimaryTextPanel.OPEN_NONE);
                    } else if (order == 0 && containedTokens.size() > 1) {
                        textPanel.setOpenMode(PrimaryTextPanel.OPEN_RIGHT);
                    } else if (order == (containedTokens.size() - 1)) {
                        textPanel.setOpenMode(PrimaryTextPanel.OPEN_LEFT);
                    } else {
                        textPanel.setOpenMode(PrimaryTextPanel.OPEN_BOTH);
                    }

                    // highlighting
                    if (currentTokenAnnotation.equals(currentAnnotation) && !annotationsType.equals("Token")) {
                        textPanel.setTextVisible(false);
                    }
                    if (annotationsToHighlight != null && annotationsToHighlight.contains(currentAnnotation) && textPanel.isTextVisible() && withHighlighting) {
                        textPanel.setHighlighted(true);
                    }
                    if (containedAnnotationsOfTheSameType != null) {
                        for (Iterator<Annotation> iterator = containedAnnotationsOfTheSameType.iterator(); iterator.hasNext();) {
                            Annotation a = iterator.next();
                            if (annotationsToHighlight != null && annotationsToHighlight.contains(a) && textPanel.isTextVisible()) {
                                int startOffset = a.getStartNode().getOffset().intValue();
                                int endOffset = a.getEndNode().getOffset().intValue();
                                int tokenStartOffset = currentTokenAnnotation.getStartNode().getOffset().intValue();
                                int tokenEndOffset = currentTokenAnnotation.getEndNode().getOffset().intValue();
                                if (startOffset <= tokenStartOffset && tokenEndOffset <= endOffset && endOffset >= tokenStartOffset && withHighlighting) {
                                    textPanel.setHighlighted(true);
                                }
                            }
                        }
                    }
                    if (ruleTrace != null && !ruleTrace.isFinished() && currentAnnotation.getStartNode().getOffset().longValue() >= lastNodeOffset
                            && !isRedSet && textPanel.isTextVisible() && withHighlighting) {
                        textPanel.setRed(true);
                        if (textPanel.getOpenMode() == PrimaryTextPanel.OPEN_NONE || textPanel.getOpenMode() == PrimaryTextPanel.OPEN_LEFT) {
                            isRedSet = true;
                        }
                    }
                    // end of highlighting

                    // Set standard annotation tooltip - annotation's features
                    String toolTipText = "";
                    FeatureMap featureMap = null;
                    if (ruleTrace != null) {
                        featureMap = ruleTrace.getPattern(currentAnnotation);
                    }
                    if (featureMap == null && containedAnnotationsOfTheSameType != null) {
                        if (ruleTrace != null) {
                            for (Iterator iterator = containedAnnotationsOfTheSameType.iterator(); iterator.hasNext();) {
                                featureMap = ruleTrace.getPattern((Annotation) iterator.next());
                                if (featureMap != null)
                                    break;
                            }
                        }
                    }
                    if (featureMap != null) {
                        for (Iterator i = featureMap.keySet().iterator(); i.hasNext();) {
                            toolTipText = toolTipText + " " + annotationsType + ".";
                            Object key = i.next();
                            toolTipText = toolTipText + key + "=" + featureMap.get(key);
                        }
                        if (featureMap.keySet().isEmpty()) {
                            toolTipText = annotationsType;
                        }
                    }
                    if (featureMap != null && textPanel.isTextVisible() && (textPanel.isHighlighted() || textPanel.isRed())
                            && withHighlighting) {
                        textPanel.setToolTipText(toolTipText);
                    }
                    // end of setting tooltip

                    c.gridx = x;
                    panel.add(textPanel, c);
                    order++;
                    x++;
                }
            }
        }
        return panel;
    }

    /**
     * Creates panels with input annotations.
     * @param ruleModel
     * @param currentTrace
     */
    public void updateRulePanel(RuleModel ruleModel, RuleTrace currentTrace) {
        if (ruleModel != null) {
            rulePane.setText(ruleModel.getRuleText());
        }
        RuleTrace ruleTrace = null;
        if (currentTrace == null && traceContainer != null) {
            if (traceContainer != null) {
                TraceContainer traces = traceContainer.getTraceByRuleModel(ruleModel);
                if (traces.size() > 0) {
                    ruleTrace = (RuleTrace) traces.iterator().next();
                }
                previousAnnotationButton.setEnabled(false);
                nextAnnotationButton.setEnabled(false);
                if (traces.size() > 1) {
                    nextAnnotationButton.setEnabled(true);
                }
                currentTraces = traces;
                currentRuleTrace = ruleTrace;
            }
        } else {
            ruleTrace = currentTrace;
            currentRuleTrace = ruleTrace;
        }
        if (traceContainer != null) {
            currentAnnotationCut = traceContainer.getPhaseCut(currentPhaseModel);
            JPanel panel = new JPanel();
            panel.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            int i = 0;
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets(2, 4, 2, 4);
            // Create input panels
            if (currentPhaseModel != null) {
                for (Iterator it = currentPhaseModel.getInput().iterator(); it.hasNext();) {
                    String input = (String) it.next();
                    c.fill = GridBagConstraints.NONE;
                    c.gridx = 0;
                    c.weightx = 0;
                    c.gridy = i++;
                    panel.add(new JLabel(input), c);
                    c.gridx = 1;
                    if (currentAnnotationCut != null) {
                        panel.add(createAnnotationPanel(currentAnnotationCut.get(input, new Long(currentStartOffset), new Long(currentEndOffset)), input, ruleTrace, true), c);
                    } else {
                        panel.add(new JLabel("No annotations of type " + input + " available"), c);
                    }
                    c.gridx = 2;
                    c.weightx = 1;
                    c.fill = GridBagConstraints.HORIZONTAL;
                    panel.add(new JPanel(), c);
                }
            }
            //end of creating input panels

            // Add divider
            c.gridx = 0;
            c.gridy = i;
            c.weightx = 0;
            c.weighty = 0;
            c.gridwidth = 2;
            panel.add(new JPanel() {
                {
                    this.setBackground(Color.black);
                }

                public Dimension getPreferredSize() {
                    return getMaximumSize();
                }

                public Dimension getMaximumSize() {
                    return new Dimension(20, 2);
                }
            },
                    c);
            c.gridx = 1;
            c.weightx = 1;
            c.gridy = i++;
            panel.add(new JPanel(), c);
            // end of creating divider

            // Create additional token panel
            c.fill = GridBagConstraints.NONE;
            c.gridx = 0;
            c.weightx = 0;
            c.gridy = i++;
            panel.add(new JLabel(""), c);
            c.gridx = 1;
            if (document.getAnnotations().getContained(new Long(currentStartOffset), new Long(currentEndOffset)).get("Token") != null) {
                if (currentAnnotationCut == null) {
                    currentAnnotationCut = document.getAnnotations().getContained(new Long(currentStartOffset), new Long(currentEndOffset)).get("Token");
                }
                panel.add(createAnnotationPanel(document.getAnnotations().getContained(new Long(currentStartOffset), new Long(currentEndOffset)).get("Token"), "Token", ruleTrace, false), c);
            } else {
                panel.add(new JLabel("No annotations of type Token available"), c);
            }
            c.gridx = 2;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            panel.add(new JPanel(), c);
            // end of creating additional token panel

            // this panel is added to keep annotation panels on the top
            c.gridx = 0;
            c.gridy = i++;
            c.weightx = 0;
            c.weighty = 1;
            panel.add(new JPanel(), c);

            // save changes
            getRulePanel().removeAll();
            getRulePanel().setLayout(new BorderLayout());
            getRulePanel().add(panel, BorderLayout.CENTER);
            this.revalidate();

//            int startOffset = (int)ruleTrace.getAnnotations().firstNode().getOffset().longValue() - currentStartOffset;
//            int length = (int)ruleTrace.getAnnotations().lastNode().getOffset().longValue() -
//                    (int)ruleTrace.getAnnotations().firstNode().getOffset().longValue();
//            MutableAttributeSet attributeSet = new SimpleAttributeSet();
//            StyleConstants.setBackground(attributeSet, Color.white);
//            if(startOffset < 0) startOffset = 0;
// some bugs here...
//            ((DefaultStyledDocument) selectedTextPane.getDocument()).setCharacterAttributes(0, selectedTextPane.getText().length(), attributeSet, false);
//            StyleConstants.setBackground(attributeSet, Color.lightGray);
//            ((DefaultStyledDocument) selectedTextPane.getDocument()).setCharacterAttributes(0, startOffset, attributeSet, true);
//            StyleConstants.setBackground(attributeSet, Color.cyan);
//            ((DefaultStyledDocument) selectedTextPane.getDocument()).setCharacterAttributes(startOffset, length, attributeSet, false);
        }
//        else
//        {
//            getRulePanel().removeAll();
//            getRulePanel().add(new JLabel("No annotations available"));
//            this.revalidate();
//        }
    }

    /**
     * Updates panel after
     * user has selected a rule in the ResourceTree.
     * @param ruleModel <code>RuleModel</code> which user has selected in the resources tree
     * @see RuleModel
     * @see debugger.gui.resources.ResourceTree
     */
    public void setCurrentRule(RuleModel ruleModel) {
        currentRuleModel = ruleModel;
        currentPhaseModel = ruleModel.getParentPhase();
        phaseName.setText(currentPhaseModel.getName() + "   Control = " + currentPhaseModel.getControl());
        ruleName.setText(ruleModel.getName());
    }

    /**
     * Updates panel after
     * user has selected a phase in the ResourceTree.
     * @param phaseModel <code>PhaseModel</code> which user has selected in the resources tree
     * @see PhaseModel
     * @see debugger.gui.resources.ResourceTree
     */
    public void setCurrentPhase(PhaseModel phaseModel) {
        currentPhaseModel = phaseModel;
        phaseName.setText(currentPhaseModel.getName() + "   Control = " + currentPhaseModel.getControl());
        if (currentRuleModel != null && !currentRuleModel.getParentPhase().equals(currentPhaseModel)) {
            getRulePanel().removeAll();
            getRulePanel().add(new JLabel("No rule currently selected."));
        }
        ruleName.setText("");
    }

    /**
     * This method should be deleted later - it violates the architecture.
     * @return
     */
    public RuleTrace getCurrentRuleTrace() {
        return currentRuleTrace;
    }

    /**
     * This method should be deleted later - it violates the architecture.
     * @return
     */
    public RuleModel getCurrentRuleModel() {
        return currentRuleModel;
    }
}
