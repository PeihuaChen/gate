/*
 *  Copyright (c) 1998-2007, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  AnnotationEditor.java
 *
 *  Valentin Tablan, Sep 10, 2007
 *
 *  $Id$
 */


package gate.gui.annedit;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.List;

import javax.swing.*;

import gate.*;
import gate.creole.*;
import gate.event.CreoleEvent;
import gate.event.CreoleListener;
import gate.gui.docview.TextualDocumentView;
import gate.util.GateException;

public class SchemaAnnotationEditor extends JPanel implements AnnotationEditor{

  /* (non-Javadoc)
   * @see gate.gui.annedit.AnnotationEditor#editAnnotation(gate.Annotation, gate.AnnotationSet)
   */
  public void editAnnotation(Annotation ann, AnnotationSet set) {
    this.annotation = ann;
    this.annSet = set;
  }

  /**
   * The annotation currently being edited.
   */
  protected Annotation annotation;
  
  /**
   * The annotation set containing the currently edited annotation. 
   */
  protected AnnotationSet annSet;
  
  /**
   * The textual view controlling this annotation editor.
   */
  protected TextualDocumentView textView;
  
  protected CreoleListener creoleListener;
  /**
   * Stores the Annotation schema objects available in the system.
   * The annotation types are used as keys for the map.
   */
  protected Map<String, AnnotationSchema> schemasByType;
  
  /**
   * Caches the features editor for each annotation type.
   */
  protected Map<String, SchemaFeaturesEditor> featureEditorsByType;
  
  /**
   * Button group for selecting the annotation type.
   */
  protected ButtonGroup annTypesBtnGroup;
  

  
  public SchemaAnnotationEditor(TextualDocumentView textView){
    super();
    this.textView = textView;
    initData();
    initGui();
  }
  
  protected void initData(){
    schemasByType = new TreeMap<String, AnnotationSchema>();
    for(LanguageResource aSchema : Gate.getCreoleRegister().
        getLrInstances("gate.creole.AnnotationSchema")){
      schemasByType.put(((AnnotationSchema)aSchema).getAnnotationName(), 
              (AnnotationSchema)aSchema);
    }

    creoleListener = new CreoleListener(){
      public void resourceLoaded(CreoleEvent e){
        Resource newResource =  e.getResource();
        if(newResource instanceof AnnotationSchema){
          AnnotationSchema aSchema = (AnnotationSchema)newResource;
          schemasByType.put(aSchema.getAnnotationName(), aSchema);
        }
      }
      
      public void resourceUnloaded(CreoleEvent e){
        Resource newResource =  e.getResource();
        if(newResource instanceof AnnotationSchema){
          AnnotationSchema aSchema = (AnnotationSchema)newResource;
          if(schemasByType.containsValue(aSchema)){
            schemasByType.remove(aSchema.getAnnotationName());
          }
        }
      }
      
      public void datastoreOpened(CreoleEvent e){
        
      }
      public void datastoreCreated(CreoleEvent e){
        
      }
      public void datastoreClosed(CreoleEvent e){
        
      }
      public void resourceRenamed(Resource resource,
                              String oldName,
                              String newName){
      }  
    };
    Gate.getCreoleRegister().addCreoleListener(creoleListener); 
  }  
  
  public void cleanup(){
    Gate.getCreoleRegister().removeCreoleListener(creoleListener);
  }
  
  protected void initGui(){
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    annTypesBtnGroup = new ButtonGroup();
    featureEditorsByType = new HashMap<String, SchemaFeaturesEditor>();
    
    ActionListener typeButtonsListener = new ActionListener(){

      public void actionPerformed(ActionEvent e) {
        String annType = ((AbstractButton)e.getSource()).getText();
System.out.println(annType + " selected!");
        //find the right editor
        SchemaFeaturesEditor aFeaturesEditor = featureEditorsByType.get(annType);
        if(aFeaturesEditor != null){
          remove(1);
          add(aFeaturesEditor);
          SchemaAnnotationEditor.this.validate();
          SchemaAnnotationEditor.this.setMinimumSize(getPreferredSize());

          SchemaAnnotationEditor.this.invalidate();
          SchemaAnnotationEditor.this.getParent().doLayout();
//          invalidate();
//          validate();
        }
      }
    };
    
    JPanel buttonsPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    //for each schema we need to create a type button and a features editor
    for(String annType : schemasByType.keySet()){
      JToggleButton aTypeBtn = new JToggleButton(annType);
      aTypeBtn.addActionListener(typeButtonsListener);
      annTypesBtnGroup.add(aTypeBtn);
      buttonsPane.add(aTypeBtn);
      
      AnnotationSchema annSchema = schemasByType.get(annType);
      SchemaFeaturesEditor aFeaturesEditor = new SchemaFeaturesEditor(annSchema);
      featureEditorsByType.put(annType, aFeaturesEditor);
    }
    add(buttonsPane);
    if(annTypesBtnGroup.getButtonCount() > 0){
      AbstractButton aTypeBtn = annTypesBtnGroup.getElements().nextElement();
      SchemaFeaturesEditor aFeaturesEditor = 
        featureEditorsByType.get(aTypeBtn.getText());
      if(aFeaturesEditor != null){
        add(aFeaturesEditor);
        aTypeBtn.setSelected(true);
      }
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    try {
      Gate.init();
      
      
      JFrame aFrame = new JFrame("New Annotation Editor");
      aFrame.setSize( 800, 600);
      
      SchemaAnnotationEditor pane = new SchemaAnnotationEditor(null);
      
      JToolBar tBar = new JToolBar("Annotation Editor", JToolBar.HORIZONTAL);
      tBar.setLayout(new BorderLayout());
      tBar.setMinimumSize(tBar.getPreferredSize());
      tBar.add(pane);
      aFrame.getContentPane().add(tBar, BorderLayout.NORTH);
      
      StringBuffer strBuf = new StringBuffer();
      for(int i = 0; i < 100; i++){
        strBuf.append("The quick brown fox jumped over the lazy dog.\n");
      }
      JTextArea aTextPane = new JTextArea(strBuf.toString());
      JScrollPane scroller = new JScrollPane(aTextPane);
      aFrame.getContentPane().add(scroller, BorderLayout.CENTER);
      
//    Box aBox = Box.createVerticalBox();
//    aFrame.getContentPane().add(aBox);
//    
//    FeatureEditor aFeatEditor = new FeatureEditor("F-nominal-small",
//            FeatureType.nominal, "val1");
//    aFeatEditor.setValues(new String[]{"val1", "val2", "val3"});
//    aBox.add(aFeatEditor.getGui());
//    
//    aFeatEditor = new FeatureEditor("F-nominal-large",
//            FeatureType.nominal, "val1");
//    aFeatEditor.setValues(new String[]{"val1", "val2", "val3", "val4", "val5", 
//            "val6", "val7", "val8", "val9"});
//    aBox.add(aFeatEditor.getGui());
//    
//    aFeatEditor = new FeatureEditor("F-boolean-true",
//            FeatureType.bool, "true");
//    aBox.add(aFeatEditor.getGui());    
//    
//    aFeatEditor = new FeatureEditor("F-boolean-false",
//            FeatureType.bool, "false");
//    aBox.add(aFeatEditor.getGui());
      
      aFrame.setVisible(true);
      
    }catch(HeadlessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch(GateException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
