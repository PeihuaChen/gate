package debugger.gui.debugging;

import debugger.resources.ResourcesFactory;
import gate.Annotation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Iterator;

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/) <br>
 * @author Vladimir Karasev, Andrey Shafirin, Oleg Mishenko
 */

public class RulePanel extends JPanel {
    private JPanel LHS;
    private JPanel RHS;

    private JLabel jlbRuleName;
    private debugger.gui.debugging.debugviews.LHSModel lhsModel;
    private debugger.gui.debugging.debugviews.RHSModel rhsModel;
    private JTable jtLHS;
    private JTable jtRHS;

    public RulePanel() {
        super(new BorderLayout());
        JPanel inner = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        jlbRuleName = new JLabel("Undefined rule");
        this.add(jlbRuleName, BorderLayout.NORTH);

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.BOTH;
        LHS = createLHSView();
        JScrollPane lhsScrollPane = new JScrollPane(LHS);
        lhsScrollPane.getVerticalScrollBar().setUnitIncrement(30);
        lhsScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Left Hand Side"));
        inner.add(lhsScrollPane, c);

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.BOTH;
        RHS = createRHSView();
        JScrollPane rhsScrollPane = new JScrollPane(RHS);
        rhsScrollPane.getVerticalScrollBar().setUnitIncrement(30);
        rhsScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Created Annotations"));
        //temporarily commented by Oleg
        inner.add(rhsScrollPane, c);

        this.add(inner, BorderLayout.CENTER);
    }

    public void ruleSelected() {
        if (null != ResourcesFactory.getCurrentRuleModel()) {
            this.jlbRuleName.setText(ResourcesFactory.getCurrentRuleModel().getName());
            jtLHS.updateUI();
            jtRHS.updateUI();
        } else {
            return;
        }
    }

    public JPanel createLHSView() {
        JPanel panel = new JPanel(new BorderLayout());
        lhsModel = new debugger.gui.debugging.debugviews.LHSModel();
        jtLHS = new JTable(lhsModel);
        jtLHS.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {

                String preparedAnnotation = "";
                int line = jtLHS.rowAtPoint(e.getPoint());
                Annotation ann = (Annotation) ResourcesFactory.getCurrentRuleModel().getMatchedAnnotations().get(line);
                // translate Annotation to String
                if (null != ann) {
                    preparedAnnotation += "{";
                    String annType = ann.getType();
                    gate.FeatureMap fm = ann.getFeatures();
                    Iterator iter = fm.keySet().iterator();
                    if (!iter.hasNext())
                        preparedAnnotation += ann.getType() + ", ";
                    while (iter.hasNext()) {
                        String key = (String) iter.next();
                        preparedAnnotation += annType + "." + key + "=" + fm.get(key) + ", ";
                    }
                    preparedAnnotation = preparedAnnotation.substring(0, preparedAnnotation.length() - 2);
                    preparedAnnotation += "}";
                    jtLHS.setToolTipText(preparedAnnotation);
                } else
                    jtLHS.setToolTipText("");
                //System.out.println("clicked = " + line);
            }
        }
        );
        //jtLHS.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        //panel.add(jlbRuleName, BorderLayout.NORTH);
        panel.add(jtLHS.getTableHeader(), BorderLayout.NORTH);
        panel.add(jtLHS, BorderLayout.SOUTH);
        return panel;
    }

    public JPanel createRHSView() {
        JPanel panel = new JPanel(new BorderLayout());
        rhsModel = new debugger.gui.debugging.debugviews.RHSModel();
        jtRHS = new JTable(rhsModel);
        jtRHS.getColumnModel().getColumn(0).setPreferredWidth(40);
        jtRHS.getColumnModel().getColumn(1).setPreferredWidth(80);
        jtRHS.getColumnModel().getColumn(2).setPreferredWidth(100);
        panel.add(jtRHS.getTableHeader(), BorderLayout.NORTH);
        panel.add(jtRHS, BorderLayout.CENTER);
        return panel;
    }
}
