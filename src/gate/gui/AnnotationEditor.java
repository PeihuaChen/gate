/*
 *  AnnotationEditor.java
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
import gate.util.Gate;
import gate.corpora.TestDocument;
import gate.creole.tokeniser.DefaultTokeniser;
import gate.creole.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.Style;
import javax.swing.border.*;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Component;
import java.beans.*;
import java.util.*;
import java.net.*;
import java.awt.event.*;

public class AnnotationEditor extends AbstractVisualResource{
  //properties
  /**
   * (Set of String) The names of the annotation sets that should be visible.
   */
  private java.util.Set visibleAnnotationSetsNames;
  private transient PropertyChangeSupport propertyChangeListeners =
                                          new PropertyChangeSupport(this);
  private gate.Document document;
  private java.util.Set annotationSchemas;

  protected ColorGenerator colGenerator = new ColorGenerator();

  //GUI components
  JTextPane textPane;
  JScrollPane textScroll;
  JTable annotationsTable;
  AnnotationsTableModel annotationsTableModel;
  JScrollPane tableScroll;
  JSplitPane leftSplit;

  JList annotationSetsList;
  JScrollPane listScroll;
  Box buttonsBox;
  JScrollPane buttonsScroll;
  //JSplitPane filtersSplit;
  Box filtersBox;
  XJTable annotationTypesTable;
  JScrollPane annotationTypesTableScroll;
  TypesTableModel annotationTypesTableModel;

  JTabbedPane rightTabbedpane;
//data members
  /**
   * holds the data for the table: a list of Annotation objects
   */
  List data;

  /**
   * a list containing triplets (Object[3]), where the first field is the
   * AnnotationSet name, and the other two are the start and end ranges in the
   * {@link data} structure.
   */
  List ranges;

  /**
   * Set of AnnotationSet. The annotation sets that are visible.
   */
  Set visibleAnnotationSets;

  /**
   * Maps from annotation type (String) to {@link TypeData}. Holds the display
   * information for the visible annotations types
   */
  Map annotationTypeDataMap;

  /**
   * Maps from annotation type (String) to {@link TypeData}. Holds the display
   * information for the annotations types that have been defined during the
   * life of this AnnotationEditor.
   */
  Map definedTypeDataMap;

  /**
   * List of {@link TypeData}. A list with the data used for dysplaying
   * various annotation types.
   */
  List annotationTypeData;

  //misc members
  /**
   * A set containg the actual annotation sets that should be visible.
   */
  private boolean tableVisible;
  private boolean textVisible;
  private boolean filtersVisible;
  private boolean annotationSetFilterVisible;
  private boolean annotationTypeFilterVisible;

  public AnnotationEditor() {
  }

  public static void main(String[] args) {
    try{
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      Gate.setLocalWebServer(false);
      Gate.setNetConnected(false);
      Gate.init();
      JFrame frame = new JFrame("Annotation Editor Test");
      frame.addWindowListener(new WindowAdapter(){
        public void windowClosing(WindowEvent e){
          System.exit(0);
        }
      });
//      Color col = JColorChooser.showDialog(frame,"Colour",null);

      //get a document
      FeatureMap params = Factory.newFeatureMap();
      params.put("markupAware", new Boolean(true));

      params.put("sourceUrlName",
                 TestDocument.getTestServerName() + "tests/doc0.html");

      Document doc = (Document)Factory.createResource("gate.corpora.DocumentImpl", params);
      //create a default tokeniser
     params.clear();
     DefaultTokeniser tokeniser = (DefaultTokeniser) Factory.createResource(
                            "gate.creole.tokeniser.DefaultTokeniser", params);

      AnnotationSet tokeniserAS = doc.getAnnotations("TokeniserAS");
      tokeniser.setDocument(doc);
      tokeniser.setAnnotationSet(tokeniserAS);
      tokeniser.run();
      //check for exceptions
      tokeniser.check();

      AnnotationEditor editor = new AnnotationEditor();
      editor.setDocument(doc);
      editor.init();
      frame.getContentPane().add(editor);
      frame.pack();
      frame.setVisible(true);
    }catch(Exception e){
      e.printStackTrace(System.err);
    }
  }

  public Resource init(){
    initListeners();
    initLocalData();
    initGuiComponents();

    //data initialisation
    if(document != null){
      //set the text
      textPane.setText(document.getContent().toString());

      //the table and the Visible AnnotationSets List
      HashSet allAnnotationSets = new HashSet(
                                    document.getNamedAnnotationSets().keySet());
      allAnnotationSets.add("<Default>");
      annotationSetsList.setListData(
                            allAnnotationSets.toArray(
                              new String[allAnnotationSets.size()]
                            )
                          );
      if(visibleAnnotationSetsNames == null){
        visibleAnnotationSetsNames = new HashSet();
      }
      Iterator vasIter = visibleAnnotationSetsNames.iterator();
      String asName;
      while(vasIter.hasNext()){
        asName = (String)vasIter.next();
        makeVisible(asName.equals("<Default>") ?document.getAnnotations() :
                                                document.getAnnotations(asName));
      }
    }//if(document != null)
    return this;
  }//public Resource init()

  protected void initListeners(){
    //listen for our own properties change events
    this.addPropertyChangeListener("document", new PropertyChangeListener(){
      public void propertyChange(PropertyChangeEvent evt){
      }
    });
    this.addPropertyChangeListener("visibleAnnotationSets",
                                   new PropertyChangeListener(){
      public void propertyChange(PropertyChangeEvent evt){
      }
    });
    this.addPropertyChangeListener("annotationSchemas",
                                   new PropertyChangeListener(){
      public void propertyChange(PropertyChangeEvent evt){
      }
    });
    //listen for component events
    this.addComponentListener(new ComponentAdapter(){
      public void componentResized(ComponentEvent e) {
        leftSplit.setDividerLocation(leftSplit.getHeight() / 2);
        Dimension size = annotationSetsList.getPreferredSize();
        JScrollBar sb = new JScrollBar(JScrollBar.VERTICAL);
        int barSize = sb.getUI().getMaximumSize(sb).width;
        listScroll.setMinimumSize(new Dimension(size.width +15 ,
                                                  size.height));
        listScroll.setPreferredSize(listScroll.getMinimumSize());
        size = annotationTypesTable.getPreferredSize();
        annotationTypesTableScroll.setMinimumSize(new Dimension(size.width + 15 ,
                                                                  size.height));
        annotationTypesTableScroll.setPreferredSize(annotationTypesTableScroll.getMinimumSize());
        doLayout();
      }

      public void componentShown(ComponentEvent e){
        componentResized(e);
      }
    });
  }//protected void initListeners()

  protected void initLocalData(){
    //init local vars
    data = new ArrayList();
    ranges = new ArrayList();
    visibleAnnotationSets = new HashSet();
    annotationsTableModel = new AnnotationsTableModel();
    annotationTypeData = new ArrayList();
    annotationTypeDataMap = new HashMap();
    definedTypeDataMap = new HashMap();
    annotationTypesTableModel = new TypesTableModel();
  }//protected void initLocalData()

  protected void initGuiComponents(){
    //initialise GUI components
    this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

    //LEFT SPLIT
    textPane = new JTextPane();
    textScroll = new JScrollPane(textPane);

    annotationsTable = new XJTable(annotationsTableModel);
    tableScroll = new JScrollPane(annotationsTable);

    leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                               textScroll, tableScroll);
    leftSplit.setOneTouchExpandable(true);
    leftSplit.setOpaque(true);
    this.add(leftSplit);


    //RIGHT SIDE - JTabbedPane
    rightTabbedpane = new JTabbedPane();

    //FILTERS TAB
    filtersBox = Box.createVerticalBox();

    //Annotation sets list
    annotationSetsList = new JList();
    annotationSetsList.addListSelectionListener(new ListSelectionListener(){
      public void valueChanged(ListSelectionEvent e){
        Object[] selVals = annotationSetsList.getSelectedValues();
        HashSet selValsCol = new HashSet(Arrays.asList(selVals));
        Collection added = (Collection)selValsCol.clone();
        added.removeAll(visibleAnnotationSetsNames);
        Collection removed = (Collection)((HashSet)visibleAnnotationSetsNames).clone();
        removed.removeAll(selValsCol);

        Iterator asIter = added.iterator();
        String name;
        while(asIter.hasNext()){
          name = (String)asIter.next();
          makeVisible(name.equals("<Default>")?document.getAnnotations() :
                                               document.getAnnotations(name));
        }
        asIter = removed.iterator();
        while(asIter.hasNext()){
          name = (String)asIter.next();
          makeInvisible(name.equals("<Default>")?document.getAnnotations() :
                                                 document.getAnnotations(name));
        }
        visibleAnnotationSetsNames = (HashSet)selValsCol.clone();
      }
    });
    listScroll = new JScrollPane(annotationSetsList);
    Border border = listScroll.getBorder();
    if(border == null) border = BorderFactory.createTitledBorder("Sets");
    else border = BorderFactory.createTitledBorder(border, "Sets");
    listScroll.setBorder(border);
    filtersBox.add(listScroll);

    //Annotation types table
    annotationTypesTable = new XJTable(annotationTypesTableModel);
    annotationTypesTable.setSortedColumn(1);
    annotationTypesTable.setTableHeader(null);
    annotationTypesTable.setDefaultRenderer(TypeData.class, new TypeDataRenderer());
    annotationTypesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    annotationTypesTable.setShowVerticalLines(false);
    annotationTypesTableScroll = new JScrollPane(annotationTypesTable);
    annotationTypesTableScroll.setBorder(BorderFactory.createTitledBorder("Types"));
    filtersBox.add(annotationTypesTableScroll);

    rightTabbedpane.add("Filters",filtersBox);

    //TYPES TAB
    buttonsBox = Box.createVerticalBox();
    buttonsScroll = new JScrollPane(buttonsBox);
    rightTabbedpane.add("Types", buttonsScroll);

    this.add(rightTabbedpane);

    leftSplit.setAlignmentY(Component.TOP_ALIGNMENT);
    rightTabbedpane.setAlignmentY(Component.TOP_ALIGNMENT);
  }//protected void initGuiComponents()

  public boolean makeVisible(AnnotationSet as){
    //do not add twice the same annotation set
    if(visibleAnnotationSets.contains(as)) return false;
    visibleAnnotationSets.add(as);
    int firstRange = data.size();
    data.addAll(firstRange, as);
    if(as.getName() == null) ranges.add(new Object[]{
                                            "<Default>",
                                            new Integer(firstRange),
                                            new Integer(firstRange + as.size() - 1 )
                                        });
    else ranges.add(new Object[]{as.getName(),
                                 new Integer(firstRange),
                                 new Integer(firstRange + as.size() - 1)
                                 });
    annotationsTableModel.fireTableDataChanged();
    //take care of the annotation types table
    Set added = (Set)new HashSet(as.getAllTypes()).clone();
    added.removeAll(annotationTypeDataMap.keySet());
    Iterator addedIter = added.iterator();
    String name;
    TypeData tData;
    while(addedIter.hasNext()){
      name = (String)addedIter.next();
      tData = getTypeData(name);
      annotationTypeData.add(tData);
      annotationTypeDataMap.put(name, tData);
    }
    annotationTypesTableModel.fireTableDataChanged();
    return true;
  }

  public boolean makeInvisible(AnnotationSet as){
    //if is not visible bail out
    if(!visibleAnnotationSets.contains(as)) return false;
    visibleAnnotationSets.remove(as);
    Iterator rangesIter = ranges.iterator();
    while(rangesIter.hasNext()){
      Object[] range = (Object[])rangesIter.next();
      if(((String)range[0]).equals(as.getName())||
         (((String)range[0]).equals("<Default>") && as.getName() == null)
         ){
        //Found it!
        //remove the range from data and ranges
        data.subList(((Integer)range[1]).intValue(),
                     ((Integer)range[2]).intValue() + 1
                     ).clear();
        rangesIter.remove();
        //shift left all remaining ranges
        int rangeSize = ((Integer)range[2]).intValue() -
                        ((Integer)range[1]).intValue() + 1;
        while(rangesIter.hasNext()){
          range = (Object[])rangesIter.next();
          range[1] = new Integer(((Integer)range[1]).intValue() - rangeSize);
          range[2] = new Integer(((Integer)range[2]).intValue() - rangeSize);
        }
      }//if
    }//while
    annotationsTableModel.fireTableDataChanged();
    //take care of the annotation types table
    Set removed = (Set)new HashSet(annotationTypeDataMap.keySet()).clone();
    Iterator remainingSetsIter = visibleAnnotationSets.iterator();
    while(remainingSetsIter.hasNext()){
      removed.removeAll(((AnnotationSet)remainingSetsIter.next()).getAllTypes());
    }
    Iterator removedIter = removed.iterator();
    String name;
    TypeData tData;
    while(removedIter.hasNext()){
      name = (String)removedIter.next();
      tData = (TypeData)annotationTypeDataMap.get(name);
      if(tData != null){
        annotationTypeData.remove(tData);
        annotationTypeDataMap.remove(name);
      }
    }
    annotationTypesTableModel.fireTableDataChanged();
    return true;
  }

  protected TypeData getTypeData(String type){
    TypeData res = (TypeData)definedTypeDataMap.get(type);
    if(res == null){
      res = new TypeData(type);
      definedTypeDataMap.put(type, res);
    }
    return res;
  }

  public void setVisibleAnnotationSetsNames(java.util.Set newVisibleAnnotationSetsNames) {
    java.util.Set  oldVisibleAnnotationSetsNames = visibleAnnotationSetsNames;
    propertyChangeListeners.firePropertyChange("visibleAnnotationSetsNames",
                                               oldVisibleAnnotationSetsNames,
                                               newVisibleAnnotationSetsNames);
    visibleAnnotationSetsNames = newVisibleAnnotationSetsNames;
  }

  public java.util.Set getVisibleAnnotationSetsNames() {
    return visibleAnnotationSetsNames;
  }

  public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
    super.removePropertyChangeListener(l);
    propertyChangeListeners.removePropertyChangeListener(l);
  }

  public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
    super.addPropertyChangeListener(l);
    propertyChangeListeners.addPropertyChangeListener(l);
  }

  public void setDocument(gate.Document newDocument) {
    gate.Document  oldDocument = document;
    document = newDocument;
    propertyChangeListeners.firePropertyChange("document", oldDocument,
                                               newDocument);
  }

  public gate.Document getDocument() {
    return document;
  }

  public void setAnnotationSchemas(java.util.Set newAnnotationSchemas) {
    java.util.Set  oldAnnotationSchemas = annotationSchemas;
    annotationSchemas = newAnnotationSchemas;
    propertyChangeListeners.firePropertyChange("annotationSchemas",
                                               oldAnnotationSchemas,
                                               newAnnotationSchemas);
  }

  public java.util.Set getAnnotationSchemas() {
    return annotationSchemas;
  }
  public void setTableVisible(boolean newTableVisible) {
    boolean  oldTableVisible = tableVisible;
    tableVisible = newTableVisible;
    propertyChangeListeners.firePropertyChange("tableVisible", new Boolean(oldTableVisible), new Boolean(newTableVisible));
  }
  public boolean isTableVisible() {
    return tableVisible;
  }
  public void setTextVisible(boolean newTextVisible) {
    boolean  oldTextVisible = textVisible;
    textVisible = newTextVisible;
    propertyChangeListeners.firePropertyChange("textVisible", new Boolean(oldTextVisible), new Boolean(newTextVisible));
  }
  public boolean isTextVisible() {
    return textVisible;
  }
  public void setFiltersVisible(boolean newFiltersVisible) {
    boolean  oldFiltersVisible = filtersVisible;
    filtersVisible = newFiltersVisible;
    propertyChangeListeners.firePropertyChange("filtersVisible", new Boolean(oldFiltersVisible), new Boolean(newFiltersVisible));
  }
  public boolean isFiltersVisible() {
    return filtersVisible;
  }
  public void setAnnotationSetFilterVisible(boolean newAnnotationSetFilterVisible) {
    boolean  oldAnnotationSetFilterVisible = annotationSetFilterVisible;
    annotationSetFilterVisible = newAnnotationSetFilterVisible;
    propertyChangeListeners.firePropertyChange("annotationSetFilterVisible", new Boolean(oldAnnotationSetFilterVisible), new Boolean(newAnnotationSetFilterVisible));
  }
  public boolean isAnnotationSetFilterVisible() {
    return annotationSetFilterVisible;
  }
  public void setAnnotationTypeFilterVisible(boolean newAnnotationTypeFilterVisible) {
    boolean  oldAnnotationTypeFilterVisible = annotationTypeFilterVisible;
    annotationTypeFilterVisible = newAnnotationTypeFilterVisible;
    propertyChangeListeners.firePropertyChange("annotationTypeFilterVisible", new Boolean(oldAnnotationTypeFilterVisible), new Boolean(newAnnotationTypeFilterVisible));
  }
  public boolean isAnnotationTypeFilterVisible() {
    return annotationTypeFilterVisible;
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
      return data.size();
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
        case 2: return Integer.class;
        case 3: return Integer.class;
        case 4: return String.class;
        default:return Object.class;
      }
    }

    public Object getValueAt(int row, int column){
      Annotation ann = (Annotation)data.get(row);
      switch(column){
        case 0:{//Type
          return ann.getType();
        }
        case 1:{//Set
          Iterator rangesIter = ranges.iterator();
          while(rangesIter.hasNext()){
            Object[] range = (Object[])rangesIter.next();
            if(((Integer)range[2]).intValue() >= row)
              return (String)range[0];
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
      return null;
    }
  }//class CustomTableModel extends AbstractTableModel

  /**
   * A custom table model used to render a table containing the annotation types
   *  from the set of existing annotation sets.
   * The columns will be: Visible, Type, Style
   */
  protected class TypesTableModel extends AbstractTableModel{
    public TypesTableModel(){
    }

    public int getRowCount(){
      return annotationTypeData.size();
    }

    public int getColumnCount(){
      return 2;
    }

    public String getColumnName(int column){
      switch(column){
        case 0: return "Visible";
        case 1: return "Type";
        default:return "?";
      }
    }

    public Class getColumnClass(int column){
      switch(column){
        case 0: return Boolean.class;
       // case 1: return String.class;
        case 1: return TypeData.class;
        default:return Object.class;
      }
    }

    public Object getValueAt(int row, int column){
      TypeData tData = (TypeData)annotationTypeData.get(row);
      switch(column){
        case 0:{//visible
          return new Boolean(tData.visible);
        }
        /*
        case 1:{//Type
          return tData.type;
        }
        */
        case 1:{//Type & Style
          return tData;
        }
        default:{
          return null;
        }
      }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex){
      return true;
      //return columnIndex == 0 || columnIndex == 2;
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex){
      TypeData tData = (TypeData)annotationTypeData.get(rowIndex);
      switch(columnIndex){
        case 0:{
          tData.visible = ((Boolean)aValue).booleanValue();
          break;
        }
        case 1:{
          break;
        }
      }
    }
  }//class CustomTableModel extends AbstractTableModel

  class TypeData implements Comparable{
    public TypeData(String type, Color foreground, Color background, Font font,
                    boolean visible){
      this.type = type;
      this.foreground = foreground;
      this.background = background;
      this.visible = visible;
      if(font == null) this.font = textPane.getFont();
      else this.font = font;

    }

    public int compareTo(Object other){
      if(other instanceof TypeData) return type.compareTo(((TypeData)other).type);
      else throw new ClassCastException("Can't compare " + getClass() +
                                        " to " + other.getClass());
    }

    public TypeData(String type){
      this.type = type;
      foreground = colGenerator.getNextColor();
      background = Color.white;
      visible = true;
      font = textPane.getFont();
    }
    String type;
    Color foreground;
    Color background;
    boolean visible;
    Font font;
  }//class TypeData

  class TypeDataRenderer extends DefaultTableCellRenderer{
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column){

      //TypeData tData = (TypeData)table.getValueAt(row, column);
      TypeData tData;
      if(value instanceof TypeData) tData = (TypeData)value;
      else return null;
      setForeground(tData.foreground);
      setBackground(tData.background);
      setFont(tData.font);
      Component res = super.getTableCellRendererComponent(table, tData.type,
                                                          false, false,
                                                          row, column);
      res.setSize(res.getMinimumSize());
      return res;
    }
  }//class TypeDataRenderer
}//class AnnotationEditor