/*  AnnotationEditor.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 13/11/2000
 *
 *  $Id$
 *
 */
package gate.gui;

import gate.*;
import gate.util.*;
import gate.corpora.TestDocument;
import gate.creole.tokeniser.DefaultTokeniser;
import gate.creole.*;
import gate.event.*;
import gate.swing.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;
import javax.swing.tree.*;
import javax.swing.border.*;

import java.awt.*;
import java.awt.font.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import java.beans.*;
import java.util.*;
import java.net.*;
import java.io.*;

/**
 * This class implements a viewer/editor for the annotations on a document.
 * As a viewer, this visual resource will display all the annotations found on
 * the document but in order for the editor to work it needs to be provided
 * with a set of annotation schemas.
 */
public class AnnotationEditor extends AbstractVisualResource {
  //properties
  private transient PropertyChangeSupport propertyChangeListeners =
                                          new PropertyChangeSupport(this);
  /**
   * The {@link gate.Document} currently displayed.
   */
  private gate.Document document;

  /**
   * A set of {@link gate.annotation.AnnotationSchema} objects describing the
   * types of annotations that this editor should be aware of.
   */

  /**
   * A random colour generator used to generate initial default colours for
   * highlighting various types of annotations.
   */
  protected ColorGenerator colGenerator = new ColorGenerator();

  //GUI components
  /** The text display.*/
  JTextPane textPane;

  /** Scroller used for the text diaplay*/
  JScrollPane textScroll;

  /** The table placed below the text display used for showing annotations*/
  XJTable annotationsTable;

  /**Model for the annotations table*/
  AnnotationsTableModel annotationsTableModel;

  /** Scroller for the annotations table*/
  JScrollPane tableScroll;

  /*The split that contains the text(top) and the annotations table(bottom)*/
  JSplitPane leftSplit;

  /**
   * The right hand side tree with all  the annotation sets and types of
   * annotations
   */
  JTree stylesTree;

  /**Scroller for the styles tree*/
  JScrollPane stylesTreeScroll;

  /**The root for the styles tree*/
  DefaultMutableTreeNode stylesTreeRoot;

  /**The model for the styles tree*/
  DefaultTreeModel stylesTreeModel;

  /**The dialog used for editing the styles used to highlight annotations*/
  TextAttributesChooser styleChooser;

  /**The dialog used for editing/adding annotations*/
  AnnotationEditDialog annotationEditDialog;

  /**
   * A box containing a {@link javax.swing.JProgressBar} used to keep the user
   * entertained while the text display is being updated
   */
  Box progressBox;

  /**The progress bar used during updating the text*/
  JProgressBar progressBar;

  /**The highlighter used for the selecting annotations that overlap*/
  Highlighter highlighter;

  /**The highlighter used for the marking the selected annotations */
  Highlighter selectionHighlighter;

  /**The highlights painter used for the marking the selected annotations */
  Highlighter.HighlightPainter selectionHighlighterPainter;

//data members
  /**
   * holds the data for the  annotations table: a list of Annotation objects
   */
  java.util.List data;

  /**
   * a list containing {@link Range} objects. These are the ranges in the
   * {@link #data} structure. A range is a bunch of annotations belonging to the
   * same annotation set that are contiguous in the {@link #data} structure.
   */
  java.util.List ranges;

  /**
   * A composed map used to get the metadata for an annotation type starting
   * from the annotation set name and the type name.
   * Annotation set name -> Annotation type -> {@link #TypeData}
   * Maps from String to Map to Map.
   */
  Map typeDataMap;

  /**
   * The listener for the evnts coming from the document (annotations and
   * annotation sets added or removed). In order to keep the display updated in
   * an efficient manner these events are processed in sets after a short delay
   * and not one by one as they occur. This is based on the assumption that
   * these kinds of events tend to occur in groups (e.g. when a processing
   * resource runs over a document it is likely to generate more than one new
   * annotation).
   */
  DelayedListener eventHandler;

  /**
   * Thread used for updating the text. This object is also used as a lock so
   * two updates do not happen in the same time which would produce a lot of
   * garbage in the display.
   */
  Thread guiUpdateThread;
  //misc members

  /**Should the table be visible*/
  private boolean tableVisible;

  /**Should the text be visible*/
  private boolean textVisible;

  /**
   * Should the right hand side tree be visible. That tree is used to select
   * what types of annotations are visible in the text display, hence the name
   * filters.
   */
  private boolean filtersVisible;

  /**Should this component bahave as an editor as well as an viewer*/
  private boolean editable = true;


  /**
   * Default constructor. Creats all the components and initialises all the
   * internal data to default values where possible.
   */
  public AnnotationEditor() {
    initLocalData();
    initGuiComponents();
    initListeners();
  }

  /** Test code*/
  public static void main(String[] args) {
    try{
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      Gate.setLocalWebServer(false);
      Gate.setNetConnected(false);
      Gate.init();
      JFrame frame = new JFrame("Gate Document Editor Test");
      frame.addWindowListener(new WindowAdapter(){
        public void windowClosing(WindowEvent e){
          System.exit(0);
        }
      });

      //get a document
      FeatureMap params = Factory.newFeatureMap();
      params.put("markupAware", new Boolean(true));

      params.put("sourceUrl",
                 "file:///d:/tmp/help-doc.html");
                 //"file:///d:/tmp/F7V.xml");
                 //"http://redmires.dcs.shef.ac.uk/admin/index.html");
                 //"http://redmires.dcs.shef.ac.uk/java1.3docs/api/javax/
                 //                                       swing/Action.html");
                 //"http://redmires.dcs.shef.ac.uk/java1.3docs/api/java/awt
                 ///AWTEventMulticaster.html");
      gate.Document doc = (gate.Document)Factory.createResource(
                                          "gate.corpora.DocumentImpl", params);
      //create a default tokeniser
     params.clear();
     params.put("rulesResourceName", "creole/tokeniser/DefaultTokeniser.rules");
     DefaultTokeniser tokeniser = (DefaultTokeniser) Factory.createResource(
                            "gate.creole.tokeniser.DefaultTokeniser", params);

      tokeniser.setDocument(doc);
      tokeniser.setAnnotationSetName("TokeniserAS");
      tokeniser.run();
      //check for exceptions
      tokeniser.check();

      AnnotationEditor editor = new AnnotationEditor();
      frame.getContentPane().add(editor);
      frame.pack();
      frame.setVisible(true);
      editor.setDocument(doc);

      //get the annotation schemas
      params =  Factory.newFeatureMap();
      params.put("xmlFileUrl", AnnotationEditor.class.getResource(
                              "/gate/resources/creole/schema/PosSchema.xml"));

      AnnotationSchema annotSchema = (AnnotationSchema)
         Factory.createResource("gate.creole.AnnotationSchema", params);
      Set annotationSchemas = new HashSet();
      annotationSchemas.add(annotSchema);

    }catch(Exception e){
      e.printStackTrace(Err.getPrintWriter());
    }
  }

  /**
   * Initialises all the listeners that this component has to register with
   * other classes.
   */
  protected void initListeners() {
    //listen for our own properties change events
    this.addPropertyChangeListener("visibleAnnotationSets",
                                   new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt){
      }
    });
    this.addPropertyChangeListener("annotationSchemas",
                                   new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt){
      }
    });
    //listen for component events
    this.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
      }

      public void componentShown(ComponentEvent e) {

        Enumeration enum = stylesTreeRoot.depthFirstEnumeration();
        while(enum.hasMoreElements()){
          DefaultMutableTreeNode node =
            (DefaultMutableTreeNode)enum.nextElement();
          stylesTreeModel.nodeChanged(node);
        }
/*
        //expand all the nodes in the tree
        if(stylesTreeRoot.getChildCount() > 0){
          DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                    ((DefaultMutableTreeNode)stylesTreeRoot).getFirstChild();
          while(node != null){
            stylesTree.expandPath(new TreePath(node.getPath()));
            node = node.getNextSibling();
          }
          //stylesTreeModel.reload();
        }
        stylesTree.paintImmediately(stylesTree.getBounds());
*/
        //set the slider location
        leftSplit.setDividerLocation(leftSplit.getHeight() / 2);
      }
    });

    stylesTree.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if(SwingUtilities.isLeftMouseButton(e)){
          //where inside the tree?
          int x = e.getX();
          int y = e.getY();
          TreePath path = stylesTree.getPathForLocation(x, y);
          if(path != null){
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.
                                         getLastPathComponent();
            TypeData nData = (TypeData)(node).getUserObject();
            //where inside the cell?
            Rectangle cellRect = stylesTree.getPathBounds(path);
            x -= cellRect.x;
            y -= cellRect.y;
            Component cellComp = stylesTree.getCellRenderer().
                                 getTreeCellRendererComponent(stylesTree,
                                                              node, false,
                                                              false, false,
                                                              0, false);
            //cellComp.setSize(cellComp.getPreferredSize());
            cellComp.setBounds(cellRect);
            Component clickedComp = cellComp.getComponentAt(x, y);

            if(clickedComp instanceof JCheckBox){
              nData.setVisible(! nData.getVisible());
              stylesTreeModel.nodeChanged(node);
            }else if(clickedComp instanceof JTextComponent &&
                     e.getClickCount() == 2){
              if(styleChooser == null){
                Window parent = SwingUtilities.getWindowAncestor(
                                  AnnotationEditor.this);
                styleChooser = parent instanceof Frame ?
                               new TextAttributesChooser((Frame)parent,
                                                         "Please select your options",
                                                         true) :
                               new TextAttributesChooser((Dialog)parent,
                                                         "Please select your options",
                                                         true);

              }

              styleChooser.setLocationRelativeTo(stylesTree);
              nData.setAttributes(
                    styleChooser.show(nData.getAttributes().copyAttributes()));
              stylesTreeModel.nodeChanged(node);
            }
          }
        }
      }
    });

    stylesTree.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        updateTreeSize();
      }
      public void componentShown(ComponentEvent e) {
        updateTreeSize();
      }
    });

    annotationsTable.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        int row = annotationsTable.rowAtPoint(e.getPoint());
        Annotation ann = (Annotation)annotationsTable.getModel().
                                                      getValueAt(row, -1);
        //find the annotation set
        String setName = (String)annotationsTable.getModel().
                                                    getValueAt(row, 1);
        AnnotationSet set = setName.equals("<Default>")?
                            document.getAnnotations() :
                            document.getAnnotations(setName);

        EditAnnotationAction editAnnAct = new EditAnnotationAction(ann,set);
        if(SwingUtilities.isLeftMouseButton(e)){
          if(e.getClickCount() == 1){
            //single left click ->highlight the annotation

            int start =
              ((Long)annotationsTable.getModel().getValueAt(row, 2)).intValue();
            int end =
              ((Long)annotationsTable.getModel().getValueAt(row, 3)).intValue();
            try{
              textPane.scrollRectToVisible(textPane.modelToView(start));
              annotationsTable.requestFocus();
            }catch(BadLocationException ble){
              throw new GateRuntimeException(ble.toString());
            }
          }else if(e.getClickCount() == 2){
            //double left click -> edit the annotation
            editAnnAct.actionPerformed(null);
          }
        } else if(SwingUtilities.isRightMouseButton(e)) {
          //right click
          //add delete option
          JPopupMenu popup = new JPopupMenu();
          class DeleteAnnotationAction extends AbstractAction{
            public DeleteAnnotationAction(){
              super("Delete selected");
            }

            public void actionPerformed(ActionEvent e){
              int[] rows = annotationsTable.getSelectedRows();
              annotationsTable.clearSelection();
              for(int i = 0; i < rows.length; i++){
                int row = rows[i];
                //find the annotation
                Annotation ann = (Annotation)annotationsTable.
                                    getModel().getValueAt(row, -1);
                //find the annotation set
                String setName = (String)annotationsTable.getModel().
                                                            getValueAt(row, 1);
                AnnotationSet set = setName.equals("<Default>")?
                                    document.getAnnotations() :
                                    document.getAnnotations(setName);
                set.remove(ann);
              }//for(int i = 0; i < rows.length; i++)
            }//public void actionPerformed(ActionEvent e)
          }//class DeleteAnnotationAction extends AbstractAction

          popup.add(new DeleteAnnotationAction());
          popup.add(editAnnAct);
          popup.show(annotationsTable, e.getX(), e.getY());
        }
      }
    });

    //takes care of highliting the selected annotations
    annotationsTable.getSelectionModel().addListSelectionListener(
      new ListSelectionListener(){
        public void valueChanged(ListSelectionEvent e){
          int[] rows = annotationsTable.getSelectedRows();
          synchronized(selectionHighlighter){
            selectionHighlighter.removeAllHighlights();
            for(int i = 0; i < rows.length; i++){
              int start = ((Long)annotationsTable.getModel().
                           getValueAt(rows[i], 2)
                          ).intValue();
              int end = ((Long)annotationsTable.getModel().
                         getValueAt(rows[i], 3)
                        ).intValue();
              try{
                selectionHighlighter.addHighlight(start, end,
                                                  selectionHighlighterPainter);
              }catch(BadLocationException ble){
                throw new GateRuntimeException(ble.toString());
              }
            }//for(int i = 0; i < rows.length; i++)
          }//synchronized(highlighter)
        }
      });


    textPane.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if(SwingUtilities.isRightMouseButton(e)){
          int position = textPane.viewToModel(e.getPoint());
          if(textPane.getSelectionStart() ==  textPane.getSelectionEnd()){
            //no selection -> select an annotation
            JPopupMenu popup = new JPopupMenu("Select:");
            //find annotations at this position
            Iterator annIter = document.getAnnotations().
                                        get(new Long(position),
                                            new Long(position)
                                        ).iterator();
            if(annIter.hasNext()){
              JMenu menu = new JMenu("<Default>");
              popup.add(menu);
              while(annIter.hasNext()){
                Annotation ann = (Annotation)annIter.next();
                JMenuItem item = new SelectAnnotationPopupItem(ann,
                                                                  "<Default>");
                menu.add(item);
              }
            }
            Map namedASs = document.getNamedAnnotationSets();
            if(namedASs != null){
              Iterator namedASiter = namedASs.values().iterator();
              while(namedASiter.hasNext()){
                //find annotations at this position
                AnnotationSet set = (AnnotationSet)namedASiter.next();
                annIter = set.get(new Long(position), new Long(position)).
                              iterator();
                if(annIter.hasNext()){
                  JMenu menu = new JMenu(set.getName());
                  popup.add(menu);
                  while(annIter.hasNext()){
                    Annotation ann = (Annotation)annIter.next();
                    JMenuItem item = new SelectAnnotationPopupItem(ann,
                                                                set.getName());
                    menu.add(item);
                  }
                }
              }
            }
            popup.show(textPane, e.getPoint().x, e.getPoint().y);
          } else {
            //there is selected text -> create a new annotation
            int start = textPane.getSelectionStart();
            int end = textPane.getSelectionEnd();
            if(getAnnotationSchemas() != null &&
               !getAnnotationSchemas().isEmpty()){
              JPopupMenu popup = new JPopupMenu();
              //Add to the default AnnotationSet
              JMenu menu = new JMenu("Add to <Default>");

              menu.add(new NewCustomAnnotationPopupItem(
                                                 start,
                                                 end,
                                                 document.getAnnotations()));

              menu.addSeparator();
              Iterator schemasIter = getAnnotationSchemas().iterator();
              while(schemasIter.hasNext()){
                AnnotationSchema schema = (AnnotationSchema)schemasIter.next();
                menu.add(new NewAnnotationPopupItem(start, end, schema,
                         document.getAnnotations()));
              }
              popup.add(menu);

              //Add to a named AnnotationSet
              Map namedASs = document.getNamedAnnotationSets();
              if(namedASs != null && !namedASs.isEmpty()){
                Iterator asIter = namedASs.values().iterator();
                while(asIter.hasNext()){
                  AnnotationSet as = (AnnotationSet)asIter.next();
                  menu = new JMenu("Add to " + as.getName());
                  schemasIter = getAnnotationSchemas().iterator();
                  while(schemasIter.hasNext()){
                    AnnotationSchema schema =
                                          (AnnotationSchema)schemasIter.next();
                    menu.add(new NewAnnotationPopupItem(start, end, schema, as));
                  }
                  popup.add(menu);
                }

              }

              //Add to a new AnnotationSet
              menu = new JMenu("Add to new annotation set");
              menu.add(new NewCustomAnnotationPopupItem(
                                                 start,
                                                 end,
                                                 null));

              menu.addSeparator();
              schemasIter = getAnnotationSchemas().iterator();
              while(schemasIter.hasNext()){
                AnnotationSchema schema = (AnnotationSchema)schemasIter.next();
                menu.add(new NewAnnotationPopupItem(start, end, schema, null));
              }
              popup.add(menu);

              //show the popup
              popup.show(textPane, e.getPoint().x, e.getPoint().y);
            }
          }
        }
      }

      public void mousePressed(MouseEvent e) {
      }

      public void mouseReleased(MouseEvent e) {
      }

      public void mouseEntered(MouseEvent e) {
      }

      public void mouseExited(MouseEvent e) {
      }
    });

  }//protected void initListeners()

  /**
   * Initialises the local variables to their default values
   */
  protected void initLocalData(){
    //init local vars
    data = Collections.synchronizedList(new ArrayList());
    //dataAsAS = new gate.annotation.AnnotationSetImpl(document);
    ranges = new ArrayList();

    typeDataMap = new HashMap();

    eventHandler = new DelayedListener();
    new Thread(Thread.currentThread().getThreadGroup(), eventHandler).start();

  }//protected void initLocalData()

  /**Builds all the graphical components*/
  protected void initGuiComponents(){
    //initialise GUI components
    this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

    //LEFT SPLIT
    textPane = new JTextPane();
    textPane.setEditable(false);
    textPane.setEnabled(true);
    textPane.setEditorKit(new CustomStyledEditorKit());
    Style defaultStyle = textPane.getStyle("default");
    StyleConstants.setBackground(defaultStyle, Color.white);
    StyleConstants.setFontFamily(defaultStyle, "Arial Unicode MS");
    textScroll = new JScrollPane(textPane);

    annotationsTableModel = new AnnotationsTableModel();
    annotationsTable = new XJTable(annotationsTableModel);
    annotationsTable.setIntercellSpacing(new Dimension(10, 5));

    tableScroll = new JScrollPane(annotationsTable);
    tableScroll.setOpaque(true);

    leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                               textScroll, tableScroll);
    leftSplit.setOneTouchExpandable(true);
    leftSplit.setOpaque(true);
    leftSplit.setAlignmentY(Component.TOP_ALIGNMENT);
    this.add(leftSplit);

    //RIGHT SIDE - the big tree
    stylesTreeRoot = new DefaultMutableTreeNode(null, true);
    stylesTreeModel = new DefaultTreeModel(stylesTreeRoot, true);
    stylesTree = new JTree(stylesTreeModel);
    stylesTree.setRootVisible(false);
    stylesTree.setCellRenderer(new NodeRenderer());
    //TIP: setting rowHeight to 0 tells the tree to query its renderer for each
    //row's size
    stylesTree.setRowHeight(0);
    stylesTree.setShowsRootHandles(true);
    stylesTree.setToggleClickCount(0);
    stylesTreeScroll = new JScrollPane(stylesTree);
    stylesTreeScroll.setAlignmentY(Component.TOP_ALIGNMENT);
    stylesTreeScroll.setHorizontalScrollBarPolicy(
                                      JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    this.add(stylesTreeScroll);

    /*
    //RIGHT SIDE - FILTERS BOX
    filtersPane = new JPanel();
    filtersPane.setLayout(new BoxLayout(filtersPane, BoxLayout.Y_AXIS));

    //Annotation sets table
    setsTable = new XJTable(setsTableModel);
    setsTable.setSortedColumn(1);
    setsTable.setTableHeader(null);
    setsTable.setShowGrid(false);
    setsTable.setDefaultRenderer(Boolean.class,
                                 new PlainRenderer(
                                    setsTable.getDefaultRenderer(Boolean.class)
                                 )
                                 );
    setsTable.setDefaultRenderer(String.class,
                                 new PlainRenderer(
                                    setsTable.getDefaultRenderer(String.class)
                                 )
                                 );

    setsScroll = new JScrollPane(setsTable);
    setsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    Border border = setsScroll.getBorder();
    if(border == null) border = BorderFactory.createTitledBorder("Sets");
    else border = BorderFactory.createTitledBorder(border, "Sets");
    setsScroll.setBorder(border);
    filtersPane.add(setsScroll);

    //Annotation types table
    typesTable = new XJTable(typesTableModel);
    typesTable.setSortedColumn(1);
    typesTable.setTableHeader(null);
    typesTable.setShowGrid(false);
    typesTable.setDefaultRenderer(TypeData.class, new TypeDataRenderer());
    typesTable.setDefaultRenderer(Boolean.class,
            new PlainRenderer(typesTable.getDefaultRenderer(Boolean.class)));
    typesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    typesScroll = new JScrollPane(typesTable);
    typesScroll.setHorizontalScrollBarPolicy(
                                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    typesScroll.setBorder(BorderFactory.createTitledBorder("Types"));
    filtersPane.add(typesScroll);
    filtersPane.setAlignmentY(Component.TOP_ALIGNMENT);
    this.add(filtersPane);
    */

    //Extra Stuff
    annotationEditDialog = new AnnotationEditDialog();

    progressBox = new Box(BoxLayout.X_AXIS);
    progressBox.add(Box.createHorizontalStrut(5));
    progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
    progressBox.add(progressBar);
    progressBox.add(Box.createHorizontalStrut(5));

    highlighter = textPane.getHighlighter();

    selectionHighlighter = new DefaultHighlighter();
//    ((DefaultHighlighter)selectionHighlighter).setDrawsLayeredHighlights(true);
    selectionHighlighter.install(textPane);

    selectionHighlighterPainter =
      new DefaultHighlighter.DefaultHighlightPainter(
            textPane.getSelectionColor());

    Thread thread  = new Thread(Thread.currentThread().getThreadGroup(),
                                new SelectionBlinker());

    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
  }//protected void initGuiComponents()

  /**Updates the size of the styles tree so it gets all the width it needs*/
  protected void updateTreeSize(){
    int width = stylesTree.getPreferredScrollableViewportSize().width +
                stylesTreeScroll.getInsets().left +
                stylesTreeScroll.getInsets().right;

    JComponent comp = stylesTreeScroll.getVerticalScrollBar();
    if(comp.isVisible()) width += comp.getPreferredSize().width;

    int height = stylesTree.getPreferredScrollableViewportSize().height +
                 stylesTreeScroll.getInsets().top +
                 stylesTreeScroll.getInsets().bottom;

    Dimension dim = new Dimension(width, height);
    stylesTreeScroll.setMinimumSize(dim);
    stylesTreeScroll.setPreferredSize(dim);
    stylesTreeScroll.invalidate();
    validate();
  }

  protected Set getAnnotationSchemas(){
    Set result = new HashSet();
    ResourceData rData = (ResourceData)Gate.getCreoleRegister().
                                            get("gate.creole.AnnotationSchema");
    if(rData != null){
      result.addAll(rData.getInstantiations());
    }
    return result;
  }

  public synchronized void removePropertyChangeListener(
                                                    PropertyChangeListener l) {
    super.removePropertyChangeListener(l);
    propertyChangeListeners.removePropertyChangeListener(l);
  }

  public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
    super.addPropertyChangeListener(l);
    propertyChangeListeners.addPropertyChangeListener(l);
  }

  public synchronized void addPropertyChangeListener(String propertyName,
                                                     PropertyChangeListener l) {
    super.addPropertyChangeListener(propertyName, l);
    propertyChangeListeners.addPropertyChangeListener(propertyName, l);
  }

  /**
   * Sets the document to be displayed
   * @param newDocument a {@link gate.Document}
   */
  public void setDocument(gate.Document newDocument) {
    gate.Document  oldDocument = document;
    document = newDocument;
    //this needs to be executed even if the new document equals(oldDocument)
    //in order to update the pointers
    if(oldDocument != document) this_documentChanged();

    propertyChangeListeners.firePropertyChange("document", oldDocument,
                                               newDocument);
  }

  /**
   * Gets the currently displayed document
   * @return a {@link gate.Document}
   */
  public gate.Document getDocument() {
    return document;
  }

  /**
   * Sets the set of annotation schemas. The annotation schemas are used by
   * this editor for editing or adding new annotations.
   * @param a {@link java.util.Set} of {@link gate.creole.AnnotationSchema}s
   */

  /**
   * Gets the current set of known annotation schemas.
   * @return a {@link java.util.Set} of {@link gate.creole.AnnotationSchema}s
   */

  /**
   * If set to true the annotations table will be shown. The default value is
   * <b>true</b>
   */
  public void setTableVisible(boolean newTableVisible) {
    boolean  oldTableVisible = tableVisible;
    tableVisible = newTableVisible;
    propertyChangeListeners.firePropertyChange("tableVisible",
                                               new Boolean(oldTableVisible),
                                               new Boolean(newTableVisible));
  }

  /**
   * Is the annotations table shown?
   */
  public boolean isTableVisible() {
    return tableVisible;
  }

  /**
   * If set to true the text display will be shown. Default value is <b>true</b>
   */
  public void setTextVisible(boolean newTextVisible) {
    boolean  oldTextVisible = textVisible;
    textVisible = newTextVisible;
    propertyChangeListeners.firePropertyChange("textVisible",
                    new Boolean(oldTextVisible), new Boolean(newTextVisible));
  }

  /**
   * Is the text display shown?
   */
  public boolean isTextVisible() {
    return textVisible;
  }

  /**
   * If set to true the right hand side tree will be displayed. Default value
   * is <b>true</b>
   */
  public void setFiltersVisible(boolean newFiltersVisible) {
    boolean  oldFiltersVisible = filtersVisible;
    filtersVisible = newFiltersVisible;
    propertyChangeListeners.firePropertyChange("filtersVisible",
              new Boolean(oldFiltersVisible), new Boolean(newFiltersVisible));
  }

  /**
   * Is the right hand side tree shown?
   */
  public boolean isFiltersVisible() {
    return filtersVisible;
  }

  /**
   * Updates this component when the underlying document is changed. This method
   * is only triggered when the document is changed to a new one and not when
   * the internal data from the document changes. For the document internal
   * events {@see #DelayedListener}.
   */
  protected void this_documentChanged(){
    Runnable runnable = new Runnable(){
      public void run(){
        initLocalData();
        annotationsTableModel.fireTableDataChanged();
        Enumeration enum = stylesTreeRoot.children();
        while(enum.hasMoreElements()){
          stylesTreeModel.removeNodeFromParent((DefaultMutableTreeNode)
                                               enum.nextElement());
        }
        if(document == null) return;
        //speed things up by hiding the text display
        SwingUtilities.invokeLater(new Runnable(){
          public void run(){
            progressBar.setValue(0);
            textScroll.getViewport().setView(progressBox);
          }
        });
        //register the for this new document's events
        document.addGateListener(eventHandler);
        textPane.setText(document.getContent().toString());
        //add the default annotation set
        Map namedASs = document.getNamedAnnotationSets();
        AnnotationSet currentAS = document.getAnnotations();
        int size = (namedASs == null) ? 1 : (namedASs.size() + 1);
        int oneASprogress = 100 / size;
        if(currentAS != null){
          addAnnotationSet(currentAS, 0, oneASprogress);
        }
        //add all the other annotation sets
        if(namedASs != null){
          int cnt = 1;
          Iterator setsIter = namedASs.values().iterator();
          while(setsIter.hasNext()){
            currentAS = (AnnotationSet)setsIter.next();
            if(currentAS != null){
              addAnnotationSet(currentAS,
                               cnt * oneASprogress,
                               (cnt + 1) * oneASprogress);
            }
            cnt ++;
          }
        }
        //restore the text display
        SwingUtilities.invokeLater(new Runnable(){
          public void run(){
            textPane.select(0, 0);
            textScroll.getViewport().setView(textPane);
          }
        });
      }
    };
    Thread thread = new Thread(Thread.currentThread().getThreadGroup(),
                               runnable);
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
  }

  /**
   * Used to register with the GUI a new annotation set on the current document.
   */
  protected void addAnnotationSet(AnnotationSet as, int progressStart,
                                  int progressEnd){
    as.addGateListener(eventHandler);
    String setName = as.getName();
    if(setName == null) setName = "<Default>";
    TypeData setData = new TypeData(setName, null, false);
    setData.setAnnotations(as);
    DefaultMutableTreeNode setNode = new DefaultMutableTreeNode(setData, true);
    stylesTreeModel.insertNodeInto(setNode, stylesTreeRoot,
                                   stylesTreeRoot.getChildCount());
    stylesTree.expandPath(new TreePath(new Object[]{stylesTreeRoot, setNode}));
    //((DefaultMutableTreeNode)stylesTreeRoot).add(setNode);
    ArrayList typesLst = new ArrayList(as.getAllTypes());
    Collections.sort(typesLst);
    int size = typesLst.size();
    int cnt = 0;
    int value = 0;
    int lastValue = 0;
    Iterator typesIter = typesLst.iterator();
    while(typesIter.hasNext()){
      String type = (String)typesIter.next();
      TypeData typeData = new TypeData(setName, type, false);
      AnnotationSet sameType = as.get(type);
      typeData.setAnnotations(sameType);
      DefaultMutableTreeNode typeNode = new DefaultMutableTreeNode(typeData,
                                                                   false);
      stylesTreeModel.insertNodeInto(typeNode, setNode,
                                     setNode.getChildCount());
      //setNode.add(typeNode);
      value = progressStart +  (progressEnd - progressStart)* cnt/size;
      if(value - lastValue >= 5){
        progressBar.setValue(value);
        progressBar.paintImmediately(progressBar.getBounds());
        lastValue = value;
      }
      cnt ++;
    }
    /*
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        stylesTreeModel.reload();
      }
    });
    */
  }

  /**
   * Gets the metadata for a given annotation type.
   * An annotation type is uniquely identified by the name of its AnnotationSet
   * and the name of the type.
   * For the default annotation set of a document (which has no name) the
   * &quot;&lt;Default&gt;&quot; value is used.
   * @param setName a {@link java.lang.String}, the name of the annotation set
   * @param type a {@link java.lang.String}, the name of the type.
   */
  protected TypeData getTypeData(String setName, String type){
    Map setMap = (Map)typeDataMap.get(setName);
    if(setMap != null) return (TypeData)setMap.get(type);
    else return null;
  }


  /**
   * Repaints the per-annotation-type highlighting in the text display.
   */
  protected void showHighlights(AnnotationSet annotations, AttributeSet style) {
    //store the state of the text display
    int selStart = textPane.getSelectionStart();
    int selEnd = textPane.getSelectionEnd();
    final int position = textPane.viewToModel(
                            textScroll.getViewport().getViewPosition());
    //hide the text
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        progressBar.setValue(0);
        //progressBar.setMaximumSize(new Dimension(textScroll.getWidth(),20));
        textScroll.getViewport().setView(progressBox);
        textScroll.paintImmediately(textScroll.getBounds());
      }
    });

    //highlight the annotations
    int size = annotations.size();
    int i = 0;
    int lastValue = 0;
    int value;
    Iterator annIter = annotations.iterator();
    while(annIter.hasNext()){
      Annotation ann = (Annotation)annIter.next();
      textPane.select(ann.getStartNode().getOffset().intValue(),
                      ann.getEndNode().getOffset().intValue());
      textPane.setCharacterAttributes(style, true);
      value = i * 100 / size;
      if(value - lastValue >= 5){
        progressBar.setValue(value);
        progressBar.paintImmediately(progressBar.getBounds());
        lastValue = value;
      }
      i++;
    }
    //restore the state
    textPane.select(selStart, selEnd);
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        //show the text
        textScroll.getViewport().setView(textPane);
        try{
          textScroll.getViewport().setViewPosition(
                                  textPane.modelToView(position).getLocation());
          textScroll.paintImmediately(textScroll.getBounds());
        }catch(BadLocationException ble){
        }
      }
    });
  }//protected void showHighlights()

  /**
   * Updates the GUI when the user has selected an annotation e.g. by using the
   * right click popup.
   */
  protected void selectAnnotation(String set, Annotation ann) {
    TypeData tData = getTypeData(set, ann.getType());
    if(!tData.getVisible()){
      tData.setVisible(true);
      //sleep a while so the gui updater thread has time to start
      try{
        Thread.sleep(100);
      }catch(InterruptedException ie){}
      synchronized(Thread.class){
        while(guiUpdateThread != null && guiUpdateThread.isAlive()) {
          try{
            Thread.sleep(100);
          } catch(InterruptedException ie) {}
        }
        //refresh the display for the type (the checkbox has to be shown selected)
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                                      ((DefaultMutableTreeNode)stylesTreeRoot).
                                      getFirstChild();
        while(node != null &&
              !((TypeData)node.getUserObject()).getSet().equals(set))
          node = node.getNextSibling();
        if(node != null){
          node = (DefaultMutableTreeNode)node.getFirstChild();
          String type = ann.getType();
          while(node != null &&
                !((TypeData)node.getUserObject()).getType().equals(type))
            node = node.getNextSibling();
          if(node != null) stylesTreeModel.nodeChanged(node);
        }
      }
    }
    int position = -1;
    synchronized(data){
      position = data.indexOf(ann);
      data.notifyAll();
    };
    if(position != -1){
      position = annotationsTable.getTableRow(position);
      if(position != -1){
        annotationsTable.clearSelection();
        annotationsTable.addRowSelectionInterval(position, position);
        annotationsTable.scrollRectToVisible(
              annotationsTable.getCellRect(position, 0, true));
      }
    }
  }

  /**Should the editor functionality of this component be enabled*/
  public void setEditable(boolean newEditable) {
    editable = newEditable;
  }

  /**Is the editor functionality enabled*/
  public boolean isEditable() {
    return editable;
  }

  //inner classes
  /**
   * A custom table model used to render a table containing the annotations from
   * a set of annotation sets.
   * The columns will be: Type, Set, Start, End, Features
   */
  protected class AnnotationsTableModel extends AbstractTableModel{
    public AnnotationsTableModel(){
    }

    public int getRowCount(){
      synchronized(data){
        return data.size();
      }
    }

    public int getColumnCount(){
      return 5;
    }

    public String getColumnName(int column){
      switch(column){
        case 0: return "Type";
        case 1: return "Set";
        case 2: return "Start";
        case 3: return "End";
        case 4: return "Features";
        default:return "?";
      }
    }

    public Class getColumnClass(int column){
      switch(column){
        case 0: return String.class;
        case 1: return String.class;
        case 2: return Long.class;
        case 3: return Long.class;
        case 4: return String.class;
        default:return Object.class;
      }
    }

    public Object getValueAt(int row, int column){
      Annotation ann;
      synchronized(data){
        ann = (Annotation)data.get(row);
        switch(column){
          case -1:{//The actual annotation
            return ann;
          }
          case 0:{//Type
            return ann.getType();
          }
          case 1:{//Set
            Iterator rangesIter = ranges.iterator();
            while(rangesIter.hasNext()){
              Range range = (Range)rangesIter.next();
              if(range.start <= row && row < range.end) return range.setName;
            }
            return "?";
          }
          case 2:{//Start
            return ann.getStartNode().getOffset();
          }
          case 3:{//End
            return ann.getEndNode().getOffset();
          }
          case 4:{//Features
            return ann.getFeatures().toString();
          }
          default:{
          }
        }
      }//synchronized(data)
      return null;
    }
  }//class AnnotationsTableModel extends AbstractTableModel


  /**
   * Displays an entry in the right hand side tree.
   * <strong><a name="override">Implementation Note:</a></strong>
   * This class overrides
   * <code>revalidate</code>,
   * <code>repaint</code>,
   * and
   * <code>firePropertyChange</code>
   * solely to improve performance.
   * If not overridden, these frequently called methods would execute code paths
   * that are unnecessary for a tree cell renderer.
   */
  class NodeRenderer extends JPanel implements TreeCellRenderer{

    public NodeRenderer(){
//      icon = new ImageIcon();
      visibleChk = new JCheckBox("",false);
      visibleChk.setOpaque(false);
//      label = new JLabel(icon);
      textComponent = new JTextPane();
      selectedBorder = BorderFactory.createLineBorder(Color.blue);
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      setOpaque(false);
    }

    public Component getTreeCellRendererComponent(JTree tree,
                                              Object value,
                                              boolean selected,
                                              boolean expanded,
                                              boolean leaf,
                                              int row,
                                              boolean hasFocus){
      removeAll();
      //the text pane needs to be sized for modelToView() to work
      textComponent.setSize(10, 10);
      if(value instanceof DefaultMutableTreeNode){
        TypeData nData = (TypeData)
                              ((DefaultMutableTreeNode)value).getUserObject();
        if(nData != null) {
          textComponent.selectAll();
          textComponent.replaceSelection(nData.getTitle());
          textComponent.selectAll();
          textComponent.setCharacterAttributes(nData.getAttributes(), true);

          if(nData.getType() != null) {
            visibleChk.setSelected(nData.getVisible());
            add(visibleChk);
          }
          if(selected) setBorder(selectedBorder);
          else setBorder(null);
        }
      } else {
        textComponent.selectAll();
        textComponent.replaceSelection(value.toString());
      }
//      textComponent.setPreferredSize(null);
      //textComponent.validate();
      //textComponent.setSize(textComponent.getPreferredSize());

/*
      try{

        textComponent.setPreferredSize(null);
        textComponent.setSize(textComponent.getPreferredSize());

        Rectangle rect = textComponent.modelToView(
                            textComponent.getDocument().getLength()-1);
        int height = rect.y + rect.height;
        int width = rect.x + rect.width;
        Dimension dim = new Dimension(width, height);
//        textComponent.setSize(dim);
        textComponent.setPreferredSize(dim);
//        textComponent.setMinimumSize(dim);
//        textComponent.setMaximumSize(dim);
      }catch(BadLocationException ble){
        //allow the component to compute its own preferred size
//        textComponent.setPreferredSize(null);
      }
*/
      textComponent.setPreferredSize(null);
      textComponent.setSize(textComponent.getPreferredSize());
      add(textComponent);
      setPreferredSize(null);
      setSize(getPreferredSize());
      return this;
    }

    /**
     * Overrides <code>JComponent.getPreferredSize</code> to
     * return slightly wider preferred size value.
     */
/*
    public Dimension getPreferredSize() {
      Dimension retDimension = super.getPreferredSize();
      Insets borderInsets = selectedBorder.getBorderInsets(this);
      if(retDimension != null) {
          retDimension = new Dimension(retDimension.width + 3 +
                                       borderInsets.left +
                                       borderInsets.right,
                                       retDimension.height +
                                       borderInsets.top +
                                       borderInsets.bottom);
      }
      return retDimension;
    }
*/
   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void revalidate() {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void repaint(long tm, int x, int y, int width, int height) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void repaint(Rectangle r) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    protected void firePropertyChange(String propertyName, Object oldValue,
                                                            Object newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, byte oldValue,
                                                              byte newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, char oldValue,
                                                              char newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, short oldValue,
                                                            short newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, int oldValue,
                                                              int newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, long oldValue,
                                                              long newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, float oldValue,
                                                              float newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, double oldValue,
                                                            double newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, boolean oldValue,
                                                            boolean newValue) {}

    Border selectedBorder;
//    ImageIcon icon;
//    JLabel label;
    JCheckBox visibleChk;
    JTextPane textComponent;
  }

  /**
   * Holds the GUI metadata for a given annotation type. An annotation type is
   * uniquely identified by the name of its AnnotationSet and the name of the
   * type.
   * For the default annotation set of a document (which has no name) the
   * &quot;&lt;Default&gt;&quot; value is used.
   * The GUI metadata contains, amongst other thiungs, the style used for
   * highlighting the annotations of this type.
   * These styles are cascading styles (there is a relation of inheritance
   * between them) so the annotation type style inherits the characteristics
   * from the style associated with the annotation set it belongs to.
   *
   * For eficiency reasons there are some intermediary styles between a parent
   * and a child style that used for changing the display in one operation.
   */
  public class TypeData {

    public TypeData(String set, String type, boolean visible){
      this.set = set;
      this.type = type;
      this.visible = visible;
      Map setMap = (Map)typeDataMap.get(set);
      if(setMap == null){
        setMap = new HashMap();
        typeDataMap.put(set, setMap);
      }
      if(type == null) {
        //this node represents a Set
        style = textPane.addStyle(set, textPane.getStyle("default"));
      } else {
        style = textPane.addStyle(set + "." + type, textPane.getStyle(set));
        StyleConstants.setBackground(style,
                                        colGenerator.getNextColor().brighter());
        //add an intermediary style that will be used for the actual display
        textPane.addStyle("_" + set + "." + type, style);
        //add the style that will be used for the actual display
        textPane.addStyle("_" + set + "." + type + "_",
                          textPane.getStyle("_" + set + "." + type));
        setMap.put(type, this);
      }
    }

    public String getSet() { return set;}
    public void setSet(String set) {this.set = set;}

    public String getType() {return type;}

    public String getTitle() {return (type == null) ? set + " annotations" :
                                                      type;}
    public boolean getVisible() {return visible;}

    public void setVisible(boolean isVisible) {
      if(this.visible == isVisible) return;
      this.visible = isVisible;
      //this is most likely called from the SWING thread so we want to get
      //out of here as quickly as possible. We'll start a new thread that will
      //do all that needs doing
      Runnable runnable = new Runnable() {
        public void run() {
          //define the runnable for the guiUpdateThread
          Runnable guiUpdater = new Runnable() {
            public void run() {
              if(visible) {
                  //make the corresponding range visible
                  //update the annotations table
                  synchronized(data) {
                    range = new Range(set, type, data.size(),
                                      data.size() + annotations.size());
                    ranges.add(range);
                    data.addAll(annotations);
                    SwingUtilities.invokeLater(new Runnable() {
                      public void run() {
                        annotationsTableModel.fireTableDataChanged();
                      }
                    });
                  }

                  //update the text display
                  Style actualStyle = textPane.getStyle("_" + set + "." + type);
                  actualStyle.setResolveParent(style);
                  showHighlights(annotations, textPane.getStyle("_" + set + "."
                                                                + type + "_"));
                } else {
                  //hide the corresponding range
                  //update the annotations table
                  synchronized(data) {
                    Collections.sort(ranges);
                    Iterator rangesIter = ranges.iterator();
                    while(rangesIter.hasNext()) {
                      //find my range
                      Range aRange = (Range)rangesIter.next();
                      if(aRange == range){
                        rangesIter.remove();
                        int size = range.end - range.start;
                        //remove the elements from Data
                        data.subList(range.start, range.end).clear();
                        //shift back all the remaining ranges
                        while(rangesIter.hasNext()) {
                          aRange = (Range)rangesIter.next();
                          aRange.start -= size;
                          aRange.end -= size;
                        }
                      }
                    }
                    range = null;
                    SwingUtilities.invokeLater(new Runnable() {
                      public void run() {
                        annotationsTableModel.fireTableDataChanged();
                      }
                    });
                  }//synchronized(data)
                  //update the text display
                  Style actualStyle = textPane.getStyle("_" + set + "." + type);
                  actualStyle.setResolveParent(textPane.getStyle("default"));
                  //restoreText();
                }
            }
          };

          //wait for the guiUpdateThread to finish if it's doing anything
          //and then start it with the new runnable
          synchronized(Thread.class) {
            while(guiUpdateThread != null && guiUpdateThread.isAlive()){
              try {
                Thread.sleep(100);
              } catch(InterruptedException ie){}
            }
            guiUpdateThread = new Thread(Thread.currentThread().getThreadGroup(),
                                         guiUpdater);
            guiUpdateThread.setPriority(Thread.MIN_PRIORITY);
            guiUpdateThread.start();
          }//synchronized(Thread.class)
        }//runnable.run()
      };//Runnable runnable = new Runnable()
      Thread thread = new Thread(Thread.currentThread().getThreadGroup(),
                                 runnable);
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }//public void setVisible(boolean isVisible)

    public AttributeSet getAttributes() { return style;}

    public void setAttributes(AttributeSet newAttributes) {
      style.removeAttributes(style.copyAttributes());
      style.addAttributes(newAttributes);
    }


    public void setAnnotations(AnnotationSet as) {
      this.annotations = as;
    }

    public AnnotationSet getAnnotations() {
      return annotations;
    }

    public String toString() {return getTitle();}

    private String set;
    private String type;
    private boolean visible;
    private Style style;
    private AnnotationSet annotations = null;
    private Range range = null;
  }//class TypeData


  /**
   * Describes a range in the {@link data} structure. A range is a bunch of
   * annotations belonging to the same annotation set that are contiguous in
   * the {@link #data} structure.
   */
  class Range implements Comparable {
    public Range(String setName, String type, int start, int end) {
      this.setName = setName;
      this.type = type;
      this.start = start;
      this.end = end;
    }

    public String toString() {
      return setName +  ", " + type + " (" + start + ", " + end + ")";
    }

    public int compareTo(Object other) {
      if(other instanceof Range) return start - ((Range)other).start;
      else throw new ClassCastException("Can't compare a " +
                                         other.getClass() + " to a " +
                                         getClass() + "!");
    }

    String setName;
    String type;
    int start;
    int end;
  }//class Range

  /**
   * Used to process a bunch of events after they happened. E.g. when a
   * processing resource runs over a document it is likely to generate a set of
   * new annotations. We don't want the interface to be updated for each of
   * them but rather to be updated from time to time with a bunch of
   * annnotations at a time.
   * This listener runs in its own thread that mostly sleeps and only wakes up
   * when there are unprocessed events after a given time interval
   * ({@link #sleepInterval}) has passed from the last event occured. When the
   * thread wakes it will process <strong>all</strong> the pending events and
   * then will go back to sleep.
   */
  class DelayedListener implements GateListener, Runnable {
    public DelayedListener() {
      eventQueue = Collections.synchronizedList(new ArrayList());
    }

    public void processGateEvent(GateEvent e) {
      synchronized(eventQueue) {
        eventQueue.add(e);
        lastEvent = System.currentTimeMillis();
      }
    }

    protected void processEventQueue(){
      synchronized(eventQueue) {
        GateEvent currentEvent;
        boolean tableChanged = false;
        while(! eventQueue.isEmpty()) {
          currentEvent = (GateEvent)eventQueue.remove(0);
          //process the current event
          if(currentEvent instanceof gate.event.DocumentEvent) {
            //document event
            gate.event.DocumentEvent docEvt =
              (gate.event.DocumentEvent)currentEvent;
            if(docEvt.getType() == docEvt.ANNOTATION_SET_REMOVED) {
              throw new UnsupportedOperationException(
                "DocumentEditor -> Annotation set removed");
            }else if(docEvt.getType() == docEvt.ANNOTATION_SET_ADDED){
              addAnnotationSet(document.getAnnotations(
                                      docEvt.getAnnotationSetName()),0,0);
            }
          }else if(currentEvent instanceof AnnotationSetEvent){
            //annotation set event
            AnnotationSetEvent asEvt = (AnnotationSetEvent)currentEvent;
            AnnotationSet set = (AnnotationSet)asEvt.getSource();
            String setName = set.getName();
            if(setName == null) setName = "<Default>";
            Annotation ann = asEvt.getAnnotation();
            String type = ann.getType();
            TypeData tData = getTypeData(setName, type);

            if(asEvt.getType() == asEvt.ANNOTATION_ADDED){
              if(tData != null){
//                tData.annotations.add(ann);
                if(tData.getVisible()){
                  //update the table
                  data.add(tData.range.end, ann);
                  tData.range.end++;
                  Iterator rangesIter = ranges.
                                        subList(
                                            ranges.indexOf(tData.range) + 1,
                                                ranges.size()).
                                        iterator();
                  while(rangesIter.hasNext()){
                    Range aRange = (Range) rangesIter.next();
                    aRange.start++;
                    aRange.end++;
                  }//while(rangesIter.hasNext())
                  tableChanged = true;

                  //update the text
                  textPane.select(ann.getStartNode().getOffset().intValue(),
                                  ann.getEndNode().getOffset().intValue());
                  textPane.setCharacterAttributes(
                              textPane.getStyle("_" + setName + "." +
                                                type + "_"), true);
                }
              } else {
                //new type
                Map setMap = (Map)typeDataMap.get(setName);
                if(setMap == null){
                  setMap = new HashMap();
                  typeDataMap.put(setName, setMap);
                }
                tData = new TypeData(setName, type, false);
                tData.setAnnotations(set.get(type));
                setMap.put(type, tData);
                DefaultMutableTreeNode typeNode =
                          new DefaultMutableTreeNode(tData, false);

                DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                  ((DefaultMutableTreeNode)stylesTreeRoot).getFirstChild();
                while(node != null &&
                  !((TypeData)node.getUserObject()).getSet().equals(setName))
                  node = node.getNextSibling();
                //we have to add typeNode to node
                //find the right place
                int i = 0;
                while (i < node.getChildCount() &&
                      ((TypeData)
                        ((DefaultMutableTreeNode)node.getChildAt(i)).
                        getUserObject()
                      ).getType().compareTo(tData.getType())<0) i++;
                stylesTreeModel.insertNodeInto(typeNode, node, i);
              }
            } else if(asEvt.getType() == asEvt.ANNOTATION_REMOVED){

//              tData.annotations.remove(ann);
              if(tData.getVisible()){
                //update the annotations table
                data.remove(ann);
                //shorten the range conatining the annotation
                tData.range.end--;
                //shift all the remaining ranges
                Iterator rangesIter = ranges.
                                    subList(ranges.indexOf(tData.range) + 1,
                                    ranges.size()).
                                      iterator();
                while(rangesIter.hasNext()){
                  Range aRange = (Range) rangesIter.next();
                  aRange.start--;
                  aRange.end--;
                }//while(rangesIter.hasNext())
                tableChanged = true;
                //update the text
                //hide the highlight
                int selStart = textPane.getSelectionStart();
                int selEnd = textPane.getSelectionEnd();
                textPane.select(ann.getStartNode().getOffset().intValue(),
                                ann.getEndNode().getOffset().intValue());
                textPane.setCharacterAttributes(
                          textPane.getStyle("default"), true);
                textPane.select(selStart, selEnd);
              }//if(tData.getVisible())
              if(tData.annotations.isEmpty()){
                //no more annotations of this type -> delete the node
                //first find the set
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                  ((DefaultMutableTreeNode)stylesTreeRoot).getFirstChild();
                while(node != null &&
                  !((TypeData)node.getUserObject()).getSet().equals(setName))
                  node = node.getNextSibling();
                if(node != null){
                  node = (DefaultMutableTreeNode)node.getFirstChild();
                  while(node != null &&
                    !((TypeData)node.getUserObject()).getType().equals(type))
                    node = node.getNextSibling();
                  if(node != null){
                    stylesTreeModel.removeNodeFromParent(node);
                  }
                }
              }//if(tData.getAnnotations().isEmpty())
            }//if(asEvt.getType() == asEvt.ANNOTATION_REMOVED)
          } else {
            //unknown event type
          }
        }//while(! eventQueue.isEmpty())
        if(tableChanged) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run(){
              if(annotationsTableModel != null){
                annotationsTableModel.fireTableDataChanged();
              }
            }
          });
          tableChanged = false;
        }
      }//synchronized(eventQueue)
    }//protected processEventQueue()

    public void run() {
      while(!stop){
        if((System.currentTimeMillis() - lastEvent) > sleepInterval){
          //process IN THE GUI THREAD the events queued so far
          Runnable runnable = new Runnable(){
            public void run(){
              processEventQueue();
            }
          };
          SwingUtilities.invokeLater(runnable);
        }

        //take a break now...
        try {
          Thread.sleep(sleepInterval);
        } catch(InterruptedException ie) {
        }
      }
    }

    int sleepInterval = 500;
    boolean stop = false;
    protected java.util.List eventQueue;
    protected long lastEvent = 0;
  }//class DelayedListener

  class SelectionBlinker implements Runnable{
    public void run(){
      while(true){
        synchronized(selectionHighlighter){
          SwingUtilities.invokeLater(new Runnable(){
            public void run(){
              showHighlights();
            }
          });
          try{
            Thread.sleep(400);
          }catch(InterruptedException ie){
            ie.printStackTrace(Err.getPrintWriter());
          }
          SwingUtilities.invokeLater(new Runnable(){
            public void run(){
              hideHighlights();
            }
          });
        }//synchronized(selectionHighlighter)

        try{
          Thread.sleep(600);
        }catch(InterruptedException ie){
          ie.printStackTrace(Err.getPrintWriter());
        }
      }//while(true)
    }//run()

    protected void showHighlights(){
      Highlighter.Highlight[] highligts = selectionHighlighter.getHighlights();
      actualHighlights.clear();
      try{
        for(int i = 0; i < highligts.length; i++){
          actualHighlights.add(highlighter.addHighlight(highligts[i].getStartOffset(),
                                   highligts[i].getEndOffset(),
                                   highligts[i].getPainter()));
        }
      }catch(BadLocationException ble){
        ble.printStackTrace(Err.getPrintWriter());
      }
    }

    protected void hideHighlights(){
      Iterator hIter = actualHighlights.iterator();
      while(hIter.hasNext()) highlighter.removeHighlight(hIter.next());
    }

    ArrayList actualHighlights = new ArrayList();
  }//class SelectionBlinker implements Runnable

  /**
   * Fixes the <a
   * href="http://developer.java.sun.com/developer/bugParade/bugs/4406598.html">
   * 4406598 bug</a> in swing text components.
   * The bug consists in the fact that the Background attribute is ignored by
   * the text component whent it is defined in a style from which the current
   * style inherits.
   */
  public class CustomLabelView extends javax.swing.text.LabelView {
    public CustomLabelView(Element elem) {
      super(elem);
    }

    public Color getBackground() {
      AttributeSet attr = getAttributes();
      if (attr != null) {
        javax.swing.text.Document d = super.getDocument();
        if (d instanceof StyledDocument){
          StyledDocument doc = (StyledDocument) d;
          return doc.getBackground(attr);
        }else{
          return null;
        }
      }
      return null;
    }
  }

  /**
   * The popup menu items used to select annotations at right click.
   * Apart from the normal {@link javax.swing.JMenuItem} behaviour, this menu
   * item also highlits the annotation which it would select if pressed.
   */
  protected class SelectAnnotationPopupItem extends JMenuItem {
    public SelectAnnotationPopupItem(Annotation ann, String setName) {
      super(ann.getType());
      setToolTipText("<html><b>Features:</b><br>" +
                     ann.getFeatures().toString() + "</html>");
      annotation = ann;
      start = ann.getStartNode().getOffset().intValue();
      end = ann.getEndNode().getOffset().intValue();
      set = setName;
      this.addMouseListener(new MouseAdapter() {
        public void mouseEntered(MouseEvent e) {
          try {
            highlighter.removeAllHighlights();
            highlighter.addHighlight(start, end,
                                            DefaultHighlighter.DefaultPainter);
          }catch(BadLocationException ble){
            throw new GateRuntimeException(ble.toString());
          }
        }

        public void mouseExited(MouseEvent e) {
          highlighter.removeAllHighlights();
        }
      });

      this.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Runnable runnable = new Runnable(){
            public void run(){
             highlighter.removeAllHighlights();
             selectAnnotation(set, annotation);
            }
          };
          Thread thread = new Thread(Thread.currentThread().getThreadGroup(),
                                     runnable);
          thread.start();
        }
      });
    }

    int start;
    int end;
    String set;
    Annotation annotation;
  }

  /**
   * The action that is fired when the user wants to edit an annotation.
   * This will show a {@link gate.gui.AnnotationEditDialog} to allow the user
   * to do the editing.
   */
  protected class EditAnnotationAction extends AbstractAction {
    public EditAnnotationAction(Annotation ann, AnnotationSet set){
      super("Edit");
      this.ann = ann;
      this.set = set;
    }

    public void actionPerformed(ActionEvent e){
      if(!editable) return;
      //find an appropiate schema
      Set annotationSchemas = getAnnotationSchemas();
      if(annotationSchemas != null && !annotationSchemas.isEmpty()){
        Iterator schemasIter = annotationSchemas.iterator();
        boolean done = false;
        while(!done && schemasIter.hasNext()){
          AnnotationSchema schema = (AnnotationSchema)schemasIter.next();
          if(schema.getAnnotationName().equalsIgnoreCase(ann.getType())){
            FeatureMap features = ann.getFeatures();
            annotationEditDialog.setLocationRelativeTo(annotationsTable);
            FeatureMap newFeatures = annotationEditDialog.show(features,
                                                               schema);
            if(newFeatures != null){
              features.clear();
              features.putAll(newFeatures);
              int tableRow = data.indexOf(ann);
              annotationsTableModel.fireTableRowsUpdated(tableRow, tableRow);
            }// End if
            done = true;
          }// End if
        }// End while
        if (!done ){
          // Edit the annotation without a schema and stuff
          editWithCustomEditor();
        }
      }else{
        // Edit the annotation without a schema and stuff
        editWithCustomEditor();
      }// End if
    }//public void actionPerformed(ActionEvent e)

    private void editWithCustomEditor(){
      CustomAnnotationEditDialog customAnnotEditor =
                                        new CustomAnnotationEditDialog(
                                            getAnnotationSchemas());
      // Creates a new annotation
      if (customAnnotEditor.show(ann) == JFileChooser.APPROVE_OPTION){
        String annotType = customAnnotEditor.getAnnotType();
        FeatureMap annotFeat = customAnnotEditor.getFeatures();
        Node startNode = ann.getStartNode();
        Node endNode = ann.getEndNode();

        //Remove the OLD annot form the set
        set.remove(ann);

        // Add the new one
        set.add(startNode,endNode,annotType, annotFeat);
        SwingUtilities.invokeLater(new Runnable(){
          public void run(){
            annotationsTableModel.fireTableDataChanged();
          }
        });
      }// End if
    }// editWithCustomEditor()

    Annotation ann = null;
    AnnotationSet set = null;
  }//class EditAnnotationAction

  /**
   * The menu items used for creating a new annotation from the right click
   * popup menu.
   */
  protected class NewAnnotationPopupItem extends JMenuItem {
    public NewAnnotationPopupItem(int aStart, int anEnd,
                                  AnnotationSchema aSchema,
                                  AnnotationSet aTargetAS) {


      super(aSchema.getAnnotationName());

      this.start = aStart;
      this.end = anEnd;
      this.schema = aSchema;
      this.targetAS = aTargetAS;

      this.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String newASName = "foo";
          if(targetAS == null){
            Object answer = JOptionPane.showInputDialog(
                            textPane,
                            "Please provide a name for the new annotation set:",
                            "Gate",
                            JOptionPane.QUESTION_MESSAGE);
            if(answer == null) return;
            newASName = (String)answer;
          }// End if

          FeatureMap features = annotationEditDialog.show(schema);
          if(features != null){
            if(targetAS == null){
              targetAS = document.getAnnotations(newASName);
            }
            try{
              targetAS.add(new Long(start), new Long(end),
                           schema.getAnnotationName(), features);
              SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                  annotationsTableModel.fireTableDataChanged();
                }
              });
            }catch(InvalidOffsetException ioe){
              JOptionPane.showMessageDialog(textPane,
                                            "Invalid input!\n" + ioe.toString(),
                                            "Gate", JOptionPane.ERROR_MESSAGE);
            }
          }
        }
      });
    }

    int start;
    int end;
    AnnotationSchema schema;
    AnnotationSet targetAS;
  }// End class NewAnnotationPopupItem


  /**
   * The menu items used for creating a new custom annotation from the right click
   * popup menu.
   */
  protected class NewCustomAnnotationPopupItem extends JMenuItem {

    public NewCustomAnnotationPopupItem(int aStart,
                                        int anEnd,
                                        AnnotationSet aTargetAS){


      super("Create a custom annotation");

      this.start = aStart;
      this.end = anEnd;
      this.targetAS = aTargetAS;

      this.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String newASName = "foo";
          if(targetAS == null){
            Object answer = JOptionPane.showInputDialog(
                            textPane,
                            "Please provide a name for the new annotation set:",
                            "Gate",
                            JOptionPane.QUESTION_MESSAGE);
            if(answer == null) return;
            newASName = (String)answer;
          }// End if

          CustomAnnotationEditDialog customAnnotEditor =
                                        new CustomAnnotationEditDialog(
                                                  getAnnotationSchemas());
          // Creates a new annotation
          if (customAnnotEditor.show(null) == JFileChooser.APPROVE_OPTION){
            String annotType = customAnnotEditor.getAnnotType();
            FeatureMap annotFeat = customAnnotEditor.getFeatures();
            if(targetAS == null){
                targetAS = document.getAnnotations(newASName);
            }// End if
            try{
              targetAS.add(new Long(start),new Long(end),annotType, annotFeat);
              SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                  annotationsTableModel.fireTableDataChanged();
                }
              });
            }catch(InvalidOffsetException ioe){
              JOptionPane.showMessageDialog(textPane,
                                            "Invalid input!\n" + ioe.toString(),
                                            "Gate", JOptionPane.ERROR_MESSAGE);
            }// End try
          }// End if
        }//actionPerformed();
      });// End new ActionListener
    }// End addActionListener();

    int start;
    int end;
    AnnotationSchema schema;
    AnnotationSet targetAS;
  }// End class NewCustomAnnotationPopupItem

  /**
   * Fixes the <a
   * href="http://developer.java.sun.com/developer/bugParade/bugs/4406598.html">
   * 4406598 bug</a> in swing text components.
   * The bug consists in the fact that the Background attribute is ignored by
   * the text component whent it is defined in a style from which the current
   * style inherits.
   */
  public class CustomStyledEditorKit extends StyledEditorKit{
    private final ViewFactory defaultFactory = new CustomStyledViewFactory();
    public ViewFactory getViewFactory() {
      return defaultFactory;
    }

    /**
      * Inserts content from the given stream, which will be
      * treated as plain text.
      * This insertion is done without checking \r or \r \n sequence.
      * It takes the text from the Reader and place it into Document at position
      * pos
      */
    public void read(Reader in, javax.swing.text.Document doc, int pos)
                throws IOException, BadLocationException {

      char[] buff = new char[65536];
      int charsRead = 0;
      while ((charsRead = in.read(buff, 0, buff.length)) != -1) {
            doc.insertString(pos, new String(buff, 0, charsRead), null);
            pos += charsRead;
      }// while
    }// read
  }

  /**
   * Fixes the <a
   * href="http://developer.java.sun.com/developer/bugParade/bugs/4406598.html">
   * 4406598 bug</a> in swing text components.
   * The bug consists in the fact that the Background attribute is ignored by
   * the text component whent it is defined in a style from which the current
   * style inherits.
   */
  public class CustomStyledViewFactory implements ViewFactory{
    public View create(Element elem) {
      String kind = elem.getName();
      if (kind != null) {
        if (kind.equals(AbstractDocument.ContentElementName)) {
          return new CustomLabelView(elem);
        }else if (kind.equals(AbstractDocument.ParagraphElementName)) {
          return new ParagraphView(elem);
        }else if (kind.equals(AbstractDocument.SectionElementName)) {
          return new BoxView(elem, View.Y_AXIS);
        }else if (kind.equals(StyleConstants.ComponentElementName)) {
          return new ComponentView(elem);
        }else if (kind.equals(StyleConstants.IconElementName)) {
          return new IconView(elem);
        }
      }
      // default to text display
      return new CustomLabelView(elem);
    }
  }
  }//class AnnotationEditor