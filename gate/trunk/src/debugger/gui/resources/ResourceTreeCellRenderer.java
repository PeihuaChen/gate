package debugger.gui.resources;

import debugger.gui.GuiFactory;
import debugger.resources.ResourcesFactory;
import debugger.resources.lr.LRRoot;
import debugger.resources.pr.PRRoot;
import debugger.resources.pr.RuleModel;
import debugger.resources.pr.RuleTrace;
import debugger.resources.pr.TraceContainer;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/)
 * @author Andrey Shafirin, Oleg Mishchenko
 */
public class ResourceTreeCellRenderer extends JPanel implements TreeCellRenderer {
    private static Color RULE_SELECTION_COLOR = new Color(153, 204, 255);
    private static Color RULE_FINISHED_COLOR = new Color(153, 255, 204);
    private static Color RULE_TRIED_COLOR = Color.pink;
    private static Color RULE_OVERRIDED_COLOR = Color.yellow;

    private JCheckBox checkBox;
    private JLabel label;

    private long startIndex = 0;
    private long endIndex = 0;

    private boolean updateNeeded = false;
    private HashMap ruleColors = new HashMap();

    public ResourceTreeCellRenderer() {
        this.setLayout(new GridBagLayout());

        label = new JLabel();
        label.setFont(new Font("Arial", Font.PLAIN, 11));
        label.setForeground(Color.black);

        checkBox = new JCheckBox();
        checkBox.setBackground(Color.white);

        initGui(false);
    }

    public Component getTreeCellRendererComponent(JTree tree, final Object value,
                                                  boolean sel,
                                                  boolean expanded,
                                                  boolean leaf, int row,
                                                  boolean hasFocus) {
        boolean isCheckBoxShown = false;
        if (sel) {
            this.setBackground(RULE_SELECTION_COLOR);
            checkBox.setBackground(RULE_SELECTION_COLOR);
            label.setBackground(RULE_SELECTION_COLOR);
        } else {
            this.setBackground(Color.white);
            checkBox.setBackground(Color.white);
            label.setBackground(Color.white);
        }
        this.setToolTipText("");
        if ((value instanceof ResourceTree.PRTreeNode && ((ResourceTree.PRTreeNode) value).getPRResource() instanceof PRRoot)
                || (value instanceof ResourceTree.LRTreeNode && ((ResourceTree.LRTreeNode) value).getLRResource() instanceof LRRoot)) {
            this.initGui(false);
            isCheckBoxShown = false;
        }
        if (value instanceof ResourceTree.PRTreeNode && ((ResourceTree.PRTreeNode) value).getPRResource() instanceof RuleModel) {
            RuleModel ruleModel = (RuleModel) ((ResourceTree.PRTreeNode) value).getPRResource();
            this.initGui(true);
            isCheckBoxShown = true;
            if (ruleModel.isStopOnMatch()) {
                checkBox.setSelected(true);
            } else {
                checkBox.setSelected(false);
            }
            /*
             * After user has selected new part of text, we select the colors for each rule and put
             * them into ruleColors HashMap, so the iteration doesn't take place on every repaint() of
             * the tree, thus the performance is improved.
             */
            if (updateNeeded == true) {
                ruleColors = new HashMap();
                TraceContainer traceContainer = ResourcesFactory.getPhaseController().getRuleTrace().getTraceByOffset(new Long(startIndex), new Long(endIndex));
                for (Iterator it = traceContainer.iterator(); it.hasNext();) {
                    RuleTrace ruleTrace = (RuleTrace) it.next();
                    RuleModel currentRuleModel = ruleTrace.getRuleModel();
                    TraceContainer currentTraceContainer = traceContainer.getTraceByRuleModel(currentRuleModel);
                    /*
                     * If rule has only one corresponding ruleTrace, the color is set using this ruleTrace;
                     * otherwise, traceContainer with several ruleTraces is put into ruleColors HashMap,
                     * and the exact color is set after call of
                     * GuiFactory.getDebugPanel().getTraceHistoryPanel().getCurrentRuleTrace() so that we know
                     * the exact ruleTrace to use for selection of color.
                     */
                    if (currentTraceContainer.size() == 1) {
                        RuleTrace rTrace = (RuleTrace) currentTraceContainer.iterator().next();
                        if (rTrace.isFinished() && rTrace.getOverrided() == null) {
                            ruleColors.put(currentRuleModel, RULE_FINISHED_COLOR);
                        }
                        if (rTrace.isFinished() && rTrace.getOverrided() != null) {
                            ruleColors.put(currentRuleModel, RULE_OVERRIDED_COLOR);
                        }
                        if (!rTrace.isFinished()) {
                            ruleColors.put(currentRuleModel, RULE_TRIED_COLOR);
                        }
                    } else {
                        ruleColors.put(currentRuleModel, currentTraceContainer);
                    }
                }
                updateNeeded = false;
            }
            /* If update is not needed, use ruleColors HashMap to set the correct color for the rule. */
            else {
                Object color = ruleColors.get(ruleModel);
                if (color instanceof Color) {
                    label.setBackground((Color) color);
                    this.setBackground((Color) color);
                    if (color.equals(RULE_OVERRIDED_COLOR)) {
                        TraceContainer traceContainer = ResourcesFactory.getPhaseController().getRuleTrace().getTraceByOffset(new Long(startIndex), new Long(endIndex));
                        traceContainer = traceContainer.getTraceByRuleModel(ruleModel);
                        if (traceContainer.size() == 1) {
                            RuleTrace ruleTrace = (RuleTrace) traceContainer.iterator().next();
                            this.setToolTipText("Overrided by: " + ruleTrace.getOverrided().getName());
                        }
                    }
                } else if (color instanceof TraceContainer) //awful hack! - one gui class should not call method of another gui class
                {
                    RuleModel rModel = GuiFactory.getDebugPanel().getTraceHistoryPanel().getCurrentRuleModel();
                    if (rModel != null && rModel.equals(ruleModel)) {
                        RuleTrace ruleTrace = GuiFactory.getDebugPanel().getTraceHistoryPanel().getCurrentRuleTrace();
                        if (ruleTrace != null) {
                            if (ruleTrace.isFinished() && ruleTrace.getOverrided() == null) {
                                label.setBackground(RULE_FINISHED_COLOR);
                                this.setBackground(RULE_FINISHED_COLOR);
                            }
                            if (ruleTrace.isFinished() && ruleTrace.getOverrided() != null) {
                                label.setBackground(RULE_OVERRIDED_COLOR);
                                this.setBackground(RULE_OVERRIDED_COLOR);
                                this.setToolTipText("Overrided by: " + ruleTrace.getOverrided().getName());
                            }
                            if (!ruleTrace.isFinished()) {
                                label.setBackground(RULE_TRIED_COLOR);
                                this.setBackground(RULE_TRIED_COLOR);
                            }
                        } else {
                            RuleTrace rTrace = (RuleTrace) ((TraceContainer) color).iterator().next();
                            if (rTrace.isFinished() && rTrace.getOverrided() == null) {
                                label.setBackground(RULE_FINISHED_COLOR);
                                this.setBackground(RULE_FINISHED_COLOR);
                            }
                            if (rTrace.isFinished() && rTrace.getOverrided() != null) {
                                label.setBackground(RULE_OVERRIDED_COLOR);
                                this.setBackground(RULE_OVERRIDED_COLOR);
                                this.setToolTipText("Overrided by: " + rTrace.getOverrided().getName());
                            }
                            if (!rTrace.isFinished()) {
                                label.setBackground(RULE_TRIED_COLOR);
                                this.setBackground(RULE_TRIED_COLOR);
                            }
                        }
                    } else {
                        RuleTrace rTrace = (RuleTrace) ((TraceContainer) color).iterator().next();
                        if (rTrace.isFinished() && rTrace.getOverrided() == null) {
                            label.setBackground(RULE_FINISHED_COLOR);
                            this.setBackground(RULE_FINISHED_COLOR);
                        }
                        if (rTrace.isFinished() && rTrace.getOverrided() != null) {
                            label.setBackground(RULE_OVERRIDED_COLOR);
                            this.setBackground(RULE_OVERRIDED_COLOR);
                            this.setToolTipText("Overrided by: " + rTrace.getOverrided().getName());
                        }
                        if (!rTrace.isFinished()) {
                            label.setBackground(RULE_TRIED_COLOR);
                            this.setBackground(RULE_TRIED_COLOR);
                        }
                    }
                }
            }
        } else {
            this.initGui(false);
            isCheckBoxShown = false;
        }

        label.setText(value.toString());

        int additionalPixels = 0;
        if (isCheckBoxShown)
            additionalPixels = 19;//checkBox.getSize().width;
        setPreferredSize(new Dimension(label.getFontMetrics(label.getFont()).stringWidth(label.getText()) + additionalPixels + 4,
                label.getFontMetrics(label.getFont()).getHeight()));

        revalidate();
        repaint();
        return this;
    }

    private void initGui(boolean withCheckBox) {
        this.removeAll();
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(1, 2, 1, 1);
        if (withCheckBox) {
            this.add(checkBox, c);
            c.gridx = 1;
            this.add(label, c);
            c.gridx = 2;
            c.weightx = 1;
            JPanel panel = new JPanel();
            panel.setOpaque(false);
            this.add(panel, c);
        } else {
            this.add(label, c);
            c.gridx = 1;
            c.weightx = 1;
            JPanel panel = new JPanel();
            panel.setOpaque(false);
            this.add(panel, c);
        }
    }

    public void setIndexes(int startIndex, int endIndex) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        updateNeeded = true;
    }
}
