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

import javax.swing.*;
import javax.swing.event.*;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.table.*;
import java.beans.*;
import java.util.*;
import java.net.*;

public class AnnotationEditor extends JPanel implements VisualResource {

  public AnnotationEditor() {
  }

  public static void main(String[] args) {
    try{
      Gate.init();
      JFrame frame = new JFrame("Annotation Editor Test");
      frame.addWindowListener(new WindowAdapter(){
        public void windowClosing(WindowEvent e){
          System.exit(0);
        }
      });
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
      frame.getContentPane().add(editor.getGUI());
      frame.pack();
      frame.setVisible(true);
    }catch(Exception e){
      e.printStackTrace(System.err);
    }
  }

  public Resource init(){
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
    //initialise GUI components
    this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    textPane = new JTextPane();
    textScroll = new JScrollPane(textPane);

    annotationsTableSorter = new TableSorter();
    annotationsTable = new JTable(annotationsTableSorter);
    annotationsTableSorter.registerJTable(annotationsTable);
    tableScroll = new JScrollPane(annotationsTable);

    leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                               textScroll, tableScroll);
    leftSplit.setOneTouchExpandable(true);
    this.add(leftSplit);

    annotationSetsList = new JList();
    annotationSetsList.addListSelectionListener(new ListSelectionListener(){
      public void valueChanged(ListSelectionEvent e){
        Object[] selVals = annotationSetsList.getSelectedValues();
        HashSet selValsCol = new HashSet(Arrays.asList(selVals));
        Collection added = (Collection)selValsCol.clone();
        added.removeAll(visibleAnnotationSets);
        Collection removed = (Collection)((HashSet)visibleAnnotationSets).clone();
        removed.removeAll(selValsCol);

        Iterator asIter = added.iterator();
        String name;
        while(asIter.hasNext()){
          name = (String)asIter.next();
          ((CustomTableModel)annotationsTableSorter.getModel()).addAS(
                  name.equals("<Default>")?document.getAnnotations() :
                                           document.getAnnotations(name));
        }
        asIter = removed.iterator();
        while(asIter.hasNext()){
          name = (String)asIter.next();
          ((CustomTableModel)annotationsTableSorter.getModel()).removeAS(
                  name.equals("<Default>")?document.getAnnotations() :
                                           document.getAnnotations(name));
        }
        visibleAnnotationSets = (HashSet)selValsCol.clone();
      }
    });
    listScroll = new JScrollPane(annotationSetsList);
    buttonsBox = Box.createVerticalBox();
    buttonsScroll = new JScrollPane(buttonsBox);

    rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                listScroll, buttonsScroll);
    this.add(rightSplit);

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
      annotationSetsList.setSize(annotationSetsList.getMinimumSize());
      if(visibleAnnotationSets == null){
        visibleAnnotationSets = new HashSet();
      }
      Iterator vasIter = visibleAnnotationSets.iterator();
      String asName;
      vAS = new HashSet();
      while(vasIter.hasNext()){
        asName = (String)vasIter.next();
        if(asName.equals("<Default>")) vAS.add(document.getAnnotations());
        else vAS.add(document.getAnnotations(asName));
      }
      annotationsTableSorter.setModel(new CustomTableModel(vAS));


    }//if(document != null)

    return this;
  }

  public JComponent getGUI(){
    return this;
  }

  //no doc required: javadoc will copy it from the interface
  public FeatureMap getFeatures(){
    return features;
  }

  public void setFeatures(FeatureMap features){
    this.features = features;
  }

  public void setVisibleAnnotationSets(java.util.Set newVisibleAnnotationSets) {
    java.util.Set  oldVisibleAnnotationSets = visibleAnnotationSets;
    visibleAnnotationSets = newVisibleAnnotationSets;
    propertyChangeListeners.firePropertyChange("visibleAnnotationSets",
                                               oldVisibleAnnotationSets,
                                               newVisibleAnnotationSets);
  }

  public java.util.Set getVisibleAnnotationSets() {
    return visibleAnnotationSets;
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

  //properties
  protected FeatureMap features;
  //set of String
  /**
   * The names of the annotation sets that should be visible.
   */
  private java.util.Set visibleAnnotationSets;
  private transient PropertyChangeSupport propertyChangeListeners =
                                          new PropertyChangeSupport(this);
  private gate.Document document;
  private java.util.Set annotationSchemas;

  //GUI components
  JTextPane textPane;
  JScrollPane textScroll;
  TableSorter annotationsTableSorter;
  JTable annotationsTable;
  JScrollPane tableScroll;
  JSplitPane leftSplit;

  JList annotationSetsList;
  JScrollPane listScroll;
  Box buttonsBox;
  JScrollPane buttonsScroll;
  JSplitPane rightSplit;

  //misc members
  /**
   * A set containg the actual annotation sets that should be visible.
   */
  protected Set vAS;

  //inner classes
  /**
   * A custom table model used to render a table containing the annotations from
   * a set of annotation sets.
   * The columns will be: Type, Set, Start, End, Features
   */
  protected class CustomTableModel extends AbstractTableModel{
    public CustomTableModel(Set aSets){
      data = new ArrayList();
      ranges = new ArrayList();
      annotationSets = new HashSet();
      Iterator asIter = aSets.iterator();
      while(asIter.hasNext()) addAS((AnnotationSet)asIter.next());
    }

    public boolean addAS(AnnotationSet as){
      //do not add twice the same annotation set
      if(annotationSets.contains(as)) return false;
      annotationSets.add(as);
      int firstRange = data.size();
      data.addAll(firstRange, as);
      if(as.getName() == null) ranges.add(new Object[]{
                                              "<Default>",
                                              new Integer(firstRange),
                                              new Integer(firstRange + as.size() -1)
                                          });
      else ranges.add(new Object[]{as.getName(),
                                   new Integer(firstRange),
                                   new Integer(firstRange + as.size() -1)
                                   });
      super.fireTableDataChanged();
System.out.println("Add: " + as.getName() + "," + firstRange + "," + (firstRange + as.size() -1));
      return true;
    }

    public boolean removeAS(AnnotationSet as){
      //if is not a member bail out
      if(!annotationSets.contains(as)) return false;
      annotationSets.remove(as);
      Iterator rangesIter = ranges.iterator();
      while(rangesIter.hasNext()){
        Object[] range = (Object[])rangesIter.next();
        if(((String)range[0]).equals(as.getName())){
          //Found it!
          //remove the range from data and ranges
System.out.println("Remove: " + range[0] + "," + range[1] + "," + range[2]);
          data.subList(((Integer)range[1]).intValue(),
                       ((Integer)range[2]).intValue()
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
      super.fireTableDataChanged();
      return true;
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
    /**
     * The annotation sets that are to be rendered by this table model
     */
    Set annotationSets;
    /**
     * holds the data for the table: a bunch of Annotation objects
     */
    ArrayList data;

    /**
     * a list containing triplets (Object[3]), where the first field is the
     * AnnotationSet name, and the other two are the start and end ranges in the
     * {@link data} structure.
     */
    List ranges;

  }//class CustomTableModel extends AbstractTableModel
}//class AnnotationEditor