package com.ontotext.gate.vr;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;

import java.util.*;
import java.net.*;
import java.io.*;

import gate.creole.ontology.*;
import gate.creole.ResourceInstantiationException;
import gate.util.Err;
import gate.gui.MainFrame;
import javax.swing.tree.DefaultTreeCellRenderer;

import gate.util.Out;

/** Main frame class of the ontology editor */
public class OEMainPanel extends JPanel  {

  /** flag indicating whether the ontology editor is in extended [ with onto list ] or
   *  simple/linear mode.
   */
  boolean withOntoList = true;

  protected JSplitPane splitPane = new JSplitPane();
  protected JPanel oPanel = new JPanel();
  protected BorderLayout borderLayout2 = new BorderLayout();
  protected JLabel oLabel = new JLabel();
  public    JScrollPane oScrollPane = new JScrollPane();
  public    JTree oTree = new JTree();
  protected JPanel listPanel = new JPanel();
  protected BorderLayout borderLayout1 = new BorderLayout();
  protected BorderLayout borderLayout3 = new BorderLayout();
  protected JLabel jLabel1 = new JLabel();
  protected JList oList = new JList();
  protected JMenuBar menuBar = new JMenuBar();
  protected JMenu fileMenu = new JMenu();
  protected JMenuItem fileOpen = new JMenuItem();
  protected JMenuItem fileSave = new JMenuItem();
  protected JMenuItem fileExit = new JMenuItem();
  /** a popup menu over the ontologies list */
  protected JPopupMenu listPopup = new JPopupMenu();
  protected JMenuItem saveItem;
  protected JMenuItem saveAsItem;
  protected JMenuItem renameItem;
  protected JMenuItem deleteItem;
  protected JMenuItem editURIItem;
  protected JMenuItem closeItem;

  /**reference to the ontology editor agregating this main frame*/
  private OntologyEditor editor;
  protected JMenu View = new JMenu();
  protected JMenuItem viewRefresh = new JMenuItem();
  protected JMenu helpMenu = new JMenu();
  protected JMenuItem helpHelp = new JMenuItem();
  protected JMenuItem helpAbout = new JMenuItem();
  protected JMenuItem fileClose = new JMenuItem();
  protected JMenuItem fileNew = new JMenuItem();
  protected JMenuItem fileSaveAs = new JMenuItem();

  public OEMainPanel() {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  private void jbInit() throws Exception {
    this.setLayout(borderLayout3);
    oPanel.setLayout(borderLayout2);
    oLabel.setText("Ontology");
    listPanel.setLayout(borderLayout1);
    jLabel1.setText("Ontologies List");
    splitPane.setMaximumSize(new Dimension(400, 400));
    splitPane.setMinimumSize(new Dimension(400, 400));
    splitPane.setPreferredSize(new Dimension(400,400));
    splitPane.setDividerSize(3);
    splitPane.setResizeWeight(0.0);
    oList.setBorder(BorderFactory.createEtchedBorder());
    oList.setToolTipText("");
    fileMenu.setText("File");
    fileOpen.setText("Open");
    fileSave.setText("Save");
    fileExit.setText("Exit");
    View.setText("View");
    viewRefresh.setText("Refresh");
    helpMenu.setText("Help");
    helpHelp.setText("Help");
    helpAbout.setText("About");
    fileClose.setText("Close");
    fileNew.setText("New");
    fileSaveAs.setText("Save As");
    this.add(splitPane, BorderLayout.CENTER);
    splitPane.add(oPanel, JSplitPane.RIGHT);
    oPanel.add(oLabel, BorderLayout.NORTH);
    oPanel.add(oScrollPane, BorderLayout.CENTER);
    splitPane.add(listPanel, JSplitPane.LEFT);
    listPanel.add(jLabel1, BorderLayout.NORTH);
    listPanel.add(oList, BorderLayout.CENTER);
    oScrollPane.getViewport().add(oTree, null);
    menuBar.add(fileMenu);
    menuBar.add(View);
    menuBar.add(helpMenu);
    fileMenu.add(fileNew);
    fileMenu.add(fileOpen);
    fileMenu.add(fileSave);
    fileMenu.add(fileSaveAs);
    fileMenu.add(fileClose);
    fileMenu.addSeparator();
    fileMenu.add(fileExit);
    View.add(viewRefresh);
    splitPane.setDividerLocation(160);

    /* setting the ontologies list popup menu */
    saveItem = new JMenuItem("Save");
    saveAsItem = new JMenuItem("Save as ...");
    renameItem = new JMenuItem("Rename");
//    deleteItem = new JMenuItem("delete");
    editURIItem = new JMenuItem("Edit URI");
    closeItem = new JMenuItem("Close");

    listPopup.add(saveItem);
    listPopup.add(saveAsItem);
    listPopup.addSeparator();
    listPopup.add(renameItem);
    listPopup.add(editURIItem);
    listPopup.addSeparator();
    listPopup.add(closeItem);

    /* this line should be commented if working with the jbuilder designer */
    this.add(menuBar,borderLayout1.BEFORE_FIRST_LINE);
    helpMenu.add(helpHelp);
    helpMenu.add(helpAbout);

    /*init listeners*/
    AssociateListeners();

  }// jbInit();

  /**Sets the ontology editor which agregates this main frame
   * @param oe the ontology editor be set   */
  public void setOntologyEditor(OntologyEditor oe) {
    editor = oe;
  }

  /**Gets the Ontology Editor.
   * @return the ontology editor   */
  public OntologyEditor getOntologyEditor() {
    return editor;
  }

  /**Sets list of ontologies to be displayed
   * @param list */
  public void setOntologyList(Vector list) {
      int index = oList.getAnchorSelectionIndex();
      index--;
      if ( index < 0 ) index = 0;

      oList.setListData(list);
      oList.validate();
  }

  /**Sets the tree to be displayed.see also setOntology.
   * @param tree  */
  public void setOntoTree(JTree tree) {
    if (null != oTree) {
      oScrollPane.getViewport().remove(oTree);
    }
    oTree = tree;
    oTree.validate();
    oScrollPane.getViewport().add(oTree,null);
  }// setOntoTree();

  /**Same as setOntoTree but builds the tree from an ontology
   * @param o an ontology    */
  public void buildOntoTree(Taxonomy o) {
    boolean includeInstances = o instanceof Ontology;
    ClassNode root = ClassNode.createRootNode(o, includeInstances);
    OntoTreeModel model = new OntoTreeModel(root);
    EditableTreeView view = new EditableTreeView(model);
    KnowledgeBaseTreeCellRenderer kbTreeCellRenderer =
                              new KnowledgeBaseTreeCellRenderer();
    view.setCellRenderer(kbTreeCellRenderer);
    view.setMainPanel(this);
    this.setOntoTree(view);
  }//buildOntoTree();

  /**Associates listeners with components   */
  private void AssociateListeners() {
    oList.addListSelectionListener(new LSListener());

    saveItem.addActionListener(new SaveListener());
    saveAsItem.addActionListener(new SaveAsListener());
    renameItem.addActionListener(new RenameListener());
//    deleteItem.addActionListener(new DeleteListener());
    editURIItem.addActionListener(new EditURIListener() );

    closeItem.addActionListener(new CloseListener());

    oList.addMouseListener(new ListPopupListener());

    fileExit.addActionListener(new FileExitListener());
    fileOpen.addActionListener(new FileOpenListener());
    fileSave.addActionListener(new FileSaveListener());
    fileSaveAs.addActionListener(new FileSaveAsListener());
    fileClose.addActionListener(new FileCloseListener());
    fileNew.addActionListener(new FileNewListener());


    viewRefresh.addActionListener(new ViewRefreshListener());

  } // AssociateListeners()


  /* private classes */
  /**LSListener - a list selection listener.*/
  private class LSListener implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent e) {
      if (0 < oList.getModel().getSize()) {

        Object obj = oList.getModel().getElementAt(oList.getAnchorSelectionIndex());
        if ( obj instanceof Taxonomy ) {
          OEMainPanel.this.editor.ontologySelected((Taxonomy) obj);
        } // only if ontology
      } // size > 0
    } // valueChanged();
  } // class LSListner

/** Listener for right click on the ontologies list */
private class ListPopupListener extends MouseAdapter {
  public void mouseClicked(MouseEvent e) {
    if(SwingUtilities.isRightMouseButton(e)){
      OEMainPanel.this.listPopup.show(OEMainPanel.this.oList,e.getX(),e.getY());
    } // if right button
  } // mouse clicked
} // class listPopupListener



/**Listener for choosing [save] from the popup menu */
private class SaveListener implements ActionListener{
  public void actionPerformed(ActionEvent e) {
      try {
        Set selset = new HashSet();

        selset.add(
          oList.getModel().getElementAt(
            oList.getAnchorSelectionIndex()
          )
        );


        Object[] selecto = oList.getSelectedValues();
        for ( int i = 0 ; i < selecto.length ; i++) {
          selset.add(selecto[i]);
        } // for i

        Iterator seti = selset.iterator();

        while (seti.hasNext()) {
          OEMainPanel.this.editor.saveOntology((Taxonomy) seti.next());
        } // while set iter

      } catch (ResourceInstantiationException x) {
        x.printStackTrace(Err.getPrintWriter());
      }

  } // actionPerformed
} //class SaveListener

/**Listener for choosing [save As] from the popup menu */
private class SaveAsListener implements ActionListener{
  public void actionPerformed(ActionEvent e) {
    try {
      Object o = oList.getModel().getElementAt(
            oList.getAnchorSelectionIndex());
      if ( o instanceof Taxonomy) {
        OEMainPanel.this.editor.saveAsOntology((Taxonomy)o,
        (int)OEMainPanel.this.oList.getLocation().getX(),
        (int)OEMainPanel.this.oList.getLocation().getY());
      }
    } catch (ResourceInstantiationException x) {
      x.printStackTrace(Err.getPrintWriter());
    }
  } // actionPerformed
} //class SaveAsListener

/**Listener for choosing [rename] from the popup menu */
private class RenameListener implements ActionListener{
  public void actionPerformed(ActionEvent e) {
    Object o = oList.getModel().getElementAt(
          oList.getAnchorSelectionIndex());
    if ( o instanceof Taxonomy) {
      OEMainPanel.this.editor.renameOntology((Taxonomy)o,
      (int)OEMainPanel.this.oList.getLocation().getX(),
      (int)OEMainPanel.this.oList.getLocation().getY());
    }
  } // actionPerformed
} //class RenameListener

/**Listener for choosing [delete] from the popup menu */
private class DeleteListener implements ActionListener{
  public void actionPerformed(ActionEvent e) {
    try  {
      Object o = oList.getModel().getElementAt(
            oList.getAnchorSelectionIndex());
      if ( o instanceof Taxonomy) {
        OEMainPanel.this.editor.deleteOntology((Taxonomy)o,
        (int)OEMainPanel.this.oList.getLocation().getX(),
        (int)OEMainPanel.this.oList.getLocation().getY());
      }
    } catch (ResourceInstantiationException x) {
      x.printStackTrace(Err.getPrintWriter());
    }
  } // actionPerformed
} //class DeleteListener

/**Listener for choosing [edit URI] from the popup menu */
private class EditURIListener implements ActionListener{
  public void actionPerformed(ActionEvent e) {
    Object o = oList.getModel().getElementAt(
          oList.getAnchorSelectionIndex());
    if ( o instanceof Taxonomy) {
      OEMainPanel.this.editor.editURI((Taxonomy)o,
      (int)OEMainPanel.this.oList.getLocation().getX(),
      (int)OEMainPanel.this.oList.getLocation().getY());
    }
  } // actionPerformed
} //class DeleteListener


/**Listener for choosing [close] from the popup menu */
private class CloseListener implements ActionListener{
  public void actionPerformed(ActionEvent e) {
    try {
      Object o = oList.getModel().getElementAt(
            oList.getAnchorSelectionIndex());
      if ( o instanceof Taxonomy) {
        OEMainPanel.this.editor.closeOntology((Taxonomy)o,
        (int)OEMainPanel.this.oList.getLocation().getX(),
        (int)OEMainPanel.this.oList.getLocation().getY());
      }
    } catch (ResourceInstantiationException x) {
      x.printStackTrace(Err.getPrintWriter());
    }

  } // actionPerformed
} //class CloseListener

/*------- menu bar items listeners ---------------*/

/** File Exit Listener */
private class FileExitListener implements ActionListener {
  public void actionPerformed(ActionEvent e) {
    OEMainPanel.this.editor.fileExit();
  } // actionPerformed
} // class FileExitListener

/** File Open Listener */
private class FileOpenListener implements ActionListener {
  public void actionPerformed(ActionEvent e) {
    try {
      OEMainPanel.this.editor.fileOpen(
        (int)OEMainPanel.this.oList.getLocation().getX(),
        (int)OEMainPanel.this.oList.getLocation().getY());
    } catch (ResourceInstantiationException x) {
      x.printStackTrace(Err.getPrintWriter());
    }
  } // actionPerformed
} // class FileOpenListener

/** File Save Listener */
private class FileSaveListener implements ActionListener {
  public void actionPerformed(ActionEvent e) {

    OEMainPanel.this.editor.fileSave(
      0,0,
      OEMainPanel.this.editor.getModifiedOntologies());

  } // actionPerformed
} // class FileSaveListener

/** File Save As Listener */
private class FileSaveAsListener implements ActionListener {
  public void actionPerformed(ActionEvent e) {
    try {
      JFileChooser chooser = gate.gui.MainFrame.getFileChooser();
      chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
      int result = chooser.showSaveDialog(OEMainPanel.this);
      if ( result == JFileChooser.APPROVE_OPTION ) {
        File selected = chooser.getSelectedFile();
        URL lurl;
          lurl = new URL("file:///"+selected.getAbsolutePath());
          editor.getOntology().setURL(lurl);
          editor.getOntology().store();
          JOptionPane.showMessageDialog(
            OEMainPanel.this,
            "Ontology saved sucessfuly.\n"
            +lurl,
            "Ontology Save As",
            JOptionPane.PLAIN_MESSAGE);

      } // approve
    } catch (Exception xx) {
      JOptionPane.showMessageDialog(OEMainPanel.this,
      "The Ontology Save As operation failed.\n"+
      "Due to: "+xx.getClass() + xx.getMessage(),
      "Ontology Save As Failure",JOptionPane.ERROR_MESSAGE);
    }
  } // actionPerformed
} // class FileSaveListener


/** File Close Listener */
private class FileCloseListener implements ActionListener {
  public void actionPerformed(ActionEvent e) {

    OEMainPanel.this.editor.fileClose(
      0,0,
      OEMainPanel.this.editor.getOntologyList());

  } // actionPerformed
} // class FileSaveListener

/** File New Listener */
private class FileNewListener implements ActionListener {
  public void actionPerformed(ActionEvent e) {

    OEMainPanel.this.editor.fileNew(
      0,0);
  } // actionPerformed
} // class FileSaveListener



/** View Refresh Listener */
private class ViewRefreshListener implements ActionListener {
  public void actionPerformed(ActionEvent e) {
    OEMainPanel.this.update(OEMainPanel.this.getGraphics());
  } // actionPerformed
} // class ViewRefreshListener

protected class KnowledgeBaseTreeCellRenderer extends DefaultTreeCellRenderer {
  public KnowledgeBaseTreeCellRenderer() {
  }
  public Component getTreeCellRendererComponent(JTree tree,
                                            Object value,
                                            boolean sel,
                                            boolean expanded,
                                            boolean leaf,
                                            int row,
                                            boolean hasFocus){
    super.getTreeCellRendererComponent(tree, value, sel, expanded,
                                       leaf, row, hasFocus);
    if (! (value instanceof ClassNode))
      return this;
    ClassNode theNode = (ClassNode) value;
    if(theNode.getSource() instanceof OClass) {
      setIcon(MainFrame.getIcon("Class"));
    } else if(theNode.getSource() instanceof OInstance) {
      setIcon(MainFrame.getIcon("Instance"));
    }
    return this;
  }
}


} //class OEMainPanel













