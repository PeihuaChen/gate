package debugger.gui.resources;

import debugger.gui.GuiFactory;
import debugger.gui.actions.resources.LrResourceSelectedAction;
import debugger.resources.ResourcesFactory;
import debugger.resources.lr.CorpusModel;
import debugger.resources.lr.LRRoot;
import debugger.resources.lr.LrModel;
import debugger.resources.pr.PRRoot;
import debugger.resources.pr.PhaseModel;
import debugger.resources.pr.PrModel;
import debugger.resources.pr.RuleModel;
import gate.Gate;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/) <br>
 * This class represents all processing and language resources
 * being loaded in GATE as a tree.
 * It uses <code>CreoleListenerImpl</code> class
 * to dynamically change the tree after user
 * has loaded/unloaded/renamed any resource in GATE.
 * @author Andrey Shafirin, Oleg Mishchenko
 */

public class ResourceTree extends JComponent {
    private JTree tree;

    public ResourceTree() {
        init();
        initListeners();
    }

    public JTree getTree() {
        return tree;
    }

    public void refresh() {
        this.removeAll();
        init();
        initListeners();
        this.revalidate();
    }

    public PRTreeNode getNode(RuleModel rm) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        for (int i = 0; i < root.getChildCount(); i++) {
            TreeNode tn = root.getChildAt(i);
            if (tn instanceof PRTreeNode) {
                return ((PRTreeNode) tn).getNode(rm);
            }
        }
        return null;
    }

    private void init() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode() {
            {
                children = new Vector();
                add(new PRTreeNode(ResourcesFactory.getPrRoot()));
                add(new LRTreeNode(ResourcesFactory.getLrRoot()));
            }

            public String toString() {
                return "Resources";
            }
        };
        tree = new JTree(root);
        tree.setCellRenderer(new ResourceTreeCellRenderer());
        tree.putClientProperty("JTree.lineStyle", "Angled");
        ToolTipManager.sharedInstance().registerComponent(tree);
        this.setLayout(new BorderLayout());
        this.add(new JScrollPane(tree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        //TODO: (by Andrey Shafirin) logical inaccuracy "CreoleListenerImpl.getInstance(this)" parameter ignored
        // for already initialised CreoleListenerImpl
        Gate.addCreoleListener(CreoleListenerImpl.getInstance(this));
    }

    private void initListeners() {
        tree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                TreePath treePath = tree.getPathForLocation(me.getX(), me.getY());
                Object lastPathComponent = null;
                if (treePath != null) {
                    lastPathComponent = treePath.getLastPathComponent();
                    Rectangle cellRectangle = tree.getPathBounds(treePath);
                    Component cellComponent = tree.getCellRenderer().getTreeCellRendererComponent(tree, lastPathComponent, true, false, true, 0, true);
                    cellComponent.setBounds(cellRectangle);
                    Component clickedComponent = cellComponent.getComponentAt(me.getX() - cellRectangle.x + 5, me.getY() - cellRectangle.y);
                    if (lastPathComponent instanceof ResourceTree.PRTreeNode && clickedComponent instanceof JCheckBox) {
                        RuleModel ruleModel = (RuleModel) ((ResourceTree.PRTreeNode) lastPathComponent).getPRResource();
                        if (ruleModel.isStopOnMatch()) {
                            ruleModel.setStopOnMatch(false);
                        } else {
                            ruleModel.setStopOnMatch(true);
                        }
                    }
                    if (lastPathComponent instanceof LRTreeNode && ((LRTreeNode) lastPathComponent).getLRResource() instanceof LrModel) {
                        LrResourceSelectedAction.getInstance().actionPerformed((LrModel) ((LRTreeNode) lastPathComponent).getLRResource());
                    }
                    if (lastPathComponent instanceof PRTreeNode && ((PRTreeNode) lastPathComponent).getPRResource() instanceof RuleModel) {
                        ResourcesFactory.setCurrentRuleModel((RuleModel) ((PRTreeNode) lastPathComponent).getPRResource());
                    }
                    if (lastPathComponent instanceof PRTreeNode && ((PRTreeNode) lastPathComponent).getPRResource() instanceof PhaseModel) {
                        GuiFactory.getDebugPanel().getTraceHistoryPanel().setCurrentPhase((PhaseModel) ((PRTreeNode) lastPathComponent).getPRResource());
                    }
                }
                repaint();
            }
        });
    }

    public class PRTreeNode extends DefaultMutableTreeNode {
        private Object prResource;

        public PRTreeNode(Object prResource) {
            this.prResource = prResource;
            children = new Vector();
            getChildren();
//            children = getChildren();
        }

        public Object getPRResource() {
            return prResource;
        }

        public PRTreeNode getNode(RuleModel rm) {
            if (prResource.equals(rm)) return this;
            for (Iterator itr = children.iterator(); itr.hasNext();) {
                PRTreeNode node = (PRTreeNode) itr.next();
                PRTreeNode prtNode = node.getNode(rm);
                if (null != prtNode) return prtNode;
            }
            return null;
        }

        public String toString() {
            if (prResource instanceof PRRoot)
                return "Processing Resources";

            else if (prResource instanceof PrModel)
                return ((PrModel) prResource).getName();

            else if (prResource instanceof PhaseModel)
                return ((PhaseModel) prResource).getName();

            else if (prResource instanceof RuleModel)
                return ((RuleModel) prResource).getName();

            else
                return "";
        }

        private Vector getChildren() {
            if (prResource instanceof PRRoot) {
                return new Vector(createNodesFromChildren(((PRRoot) prResource).getPRs()));
            } else if (prResource instanceof PrModel) {
                return new Vector(createNodesFromChildren(((PrModel) prResource).getPhases()));
            } else if (prResource instanceof PhaseModel) {
                return new Vector(createNodesFromChildren(((PhaseModel) prResource).getRules()));
            } else
                return new Vector();
        }

        private Collection createNodesFromChildren(Collection children) {
            List nodes = new ArrayList();
            for (Iterator it = children.iterator(); it.hasNext();) {
//                nodes.add(new PRTreeNode(it.next()));
                add(new PRTreeNode(it.next()));
            }
            return nodes;
        }


    }

    class LRTreeNode extends DefaultMutableTreeNode {
        private Object languageResource;

        public LRTreeNode(Object languageResource) {
            this.languageResource = languageResource;
            children = getChildren();
        }

        public String toString() {
            if (languageResource instanceof LRRoot)
                return "Language Resources";
            else if (languageResource instanceof CorpusModel)
                return languageResource.toString();
            else if (languageResource instanceof LrModel)
                return languageResource.toString();
            else
                return "";
        }

        public Object getLRResource() {
            return languageResource;
        }

        private Vector getChildren() {
            if (languageResource instanceof LRRoot) {
                Vector children = new Vector();
                for (Iterator it = ((LRRoot) languageResource).getCorpora().iterator(); it.hasNext();) {
                    CorpusModel corpusModel = (CorpusModel) it.next();
                    children.add(new LRTreeNode(corpusModel));
                }
                return children;
            } else if (languageResource instanceof CorpusModel) {
                Vector children = new Vector();
                for (Iterator it = ((CorpusModel) languageResource).getLrModels().iterator(); it.hasNext();) {
                    LrModel currentModel = (LrModel) it.next();
                    children.add(new LRTreeNode(currentModel));
                }
                return children;
            } else {
                return new Vector();
            }
        }
    }

    public ResourceTreeCellRenderer getResourceTreeCellRenderer() {
        return (ResourceTreeCellRenderer) tree.getCellRenderer();
    }
}