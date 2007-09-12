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
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import gate.*;
import gate.creole.*;
import gate.event.CreoleEvent;
import gate.event.CreoleListener;
import gate.gui.docview.TextualDocumentView;
import gate.util.GateException;
import gate.util.GateRuntimeException;

public class SchemaAnnotationEditor extends JPanel implements AnnotationEditor{

  private class TypeButtonListener implements ActionListener{
    public void actionPerformed(ActionEvent e) {
      String annType = ((AbstractButton)e.getSource()).getText();
      //find the right editor
      SchemaFeaturesEditor aFeaturesEditor = featureEditorsByType.get(annType);
      if(aFeaturesEditor != null){
        featuresBox.removeAll();
        featuresBox.add(aFeaturesEditor);
        SchemaAnnotationEditor.this.revalidate();
        if(dialog != null) dialog.pack();
      }
    }
  }
  
  /**
   * A panel with a flow layout used for flows of buttons.
   */
  private class FlowPanel extends JPanel{
    public FlowPanel(){
      setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize() {
      Dimension size = super.getPreferredSize();
      if(size.width > MAX_WIDTH){
        setSize(MAX_WIDTH, Integer.MAX_VALUE);
        doLayout();
        int compCnt = getComponentCount();
        if(compCnt > 0){
          Component lastComp = getComponent(compCnt -1);
          Point compLoc = lastComp.getLocation();
          Dimension compSize = lastComp.getSize();
          size.width = MAX_WIDTH;
          size.height = compLoc.y + compSize.height;
        }
      }
      return size;
    }
    
    /**
     * An arbitrary value for the maximum width.
     */
    private static final int MAX_WIDTH = 500;
  }
  
  /* (non-Javadoc)
   * @see gate.gui.annedit.AnnotationEditor#editAnnotation(gate.Annotation, gate.AnnotationSet)
   */
  public void editAnnotation(Annotation ann, AnnotationSet set) {
    this.annotation = ann;
    this.annSet = set;
//System.out.println("Editing: " + ann.getType() + ", id: " + ann.getId());    
    String annType = ann.getType();
    Enumeration<AbstractButton> typBtnEnum = annTypesBtnGroup.getElements();
    while(typBtnEnum.hasMoreElements()){
      AbstractButton aBtn = typBtnEnum.nextElement();
      if(aBtn.getText().equals(annType)){
        aBtn.doClick();
        break;
      }
    }
    SchemaFeaturesEditor fEdit = featureEditorsByType.get(annType);
    if(fEdit != null) fEdit.editFeatureMap(ann.getFeatures());
    if(dialog != null){
      placeDialog();
    }
  }

  /**
   * Finds the best location for the editor dialog
   */
  protected void placeDialog(){
    //calculate position
    try{
      Rectangle startRect = textPane.modelToView(annotation.getStartNode().
        getOffset().intValue());
      Rectangle endRect = textPane.modelToView(annotation.getEndNode().
            getOffset().intValue());
      Point topLeft = textPane.getLocationOnScreen();
      int x = topLeft.x + startRect.x;
      int y = topLeft.y + endRect.y + endRect.height;

      //make sure the window doesn't start lower 
      //than the end of the visible rectangle
      Rectangle visRect = textPane.getVisibleRect();
      int maxY = topLeft.y + visRect.y + visRect.height;      
      
      //make sure window doesn't get off-screen
      dialog.pack();
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      boolean revalidate = false;
      if(dialog.getSize().width > screenSize.width){
        dialog.setSize(screenSize.width, dialog.getSize().height);
        revalidate = true;
      }
      if(dialog.getSize().height > screenSize.height){
        dialog.setSize(dialog.getSize().width, screenSize.height);
        revalidate = true;
      }
      
      if(revalidate) dialog.validate();
      //calculate max X
      int maxX = screenSize.width - dialog.getSize().width;
      //calculate max Y
      if(maxY + dialog.getSize().height > screenSize.height){
        maxY = screenSize.height - dialog.getSize().height;
      }
      
      //correct position
      if(y > maxY) y = maxY;
      if(x > maxX) x = maxX;
      dialog.setLocation(x, y);
      if(!dialog.isVisible()) dialog.setVisible(true);
    }catch(BadLocationException ble){
      //this should never occur
      throw new GateRuntimeException(ble);
    }
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
  
  protected JTextComponent textPane;
  
  /**
   * The dialog used to show this annotation editor.
   */
  protected JDialog dialog;
  
  /**
   * The common listener for all type buttons.
   */
  protected TypeButtonListener typeButtonListener;
  
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
  

  protected Box featuresBox;
  
  public SchemaAnnotationEditor(TextualDocumentView textView){
    super();
    this.textView = textView;
    initData();
    initGui();
    
//    JToolBar tBar = new JToolBar("Annotation Editor", JToolBar.HORIZONTAL);
//    tBar.add(this);
//    tBar.setFloatable(true);
//    if(textView != null && textView.getOwner() != null){
//      textView.getOwner().add(tBar, BorderLayout.SOUTH);
//    }
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
//    setLayout(new FlowLayout());
//    setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
    annTypesBtnGroup = new ButtonGroup();
    featureEditorsByType = new HashMap<String, SchemaFeaturesEditor>();
    typeButtonListener = new TypeButtonListener();
    
    JPanel buttonsPane = new FlowPanel();
    buttonsPane.setAlignmentX(Component.LEFT_ALIGNMENT);
    //for each schema we need to create a type button and a features editor
    for(String annType : schemasByType.keySet()){
      JToggleButton aTypeBtn = new JToggleButton(annType);
      aTypeBtn.addActionListener(typeButtonListener);
      annTypesBtnGroup.add(aTypeBtn);
      buttonsPane.add(aTypeBtn);
      
      AnnotationSchema annSchema = schemasByType.get(annType);
      SchemaFeaturesEditor aFeaturesEditor = new SchemaFeaturesEditor(annSchema);
      featureEditorsByType.put(annType, aFeaturesEditor);
    }
    add(buttonsPane);
    featuresBox = Box.createVerticalBox();
    String boxTitle = "Features "; 
    featuresBox.setBorder(BorderFactory.createTitledBorder(boxTitle));
    featuresBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    JLabel aLabel = new JLabel(boxTitle);
    featuresBox.setMinimumSize(new Dimension(aLabel.getPreferredSize().width,
            Integer.MAX_VALUE));
    add(featuresBox);
    
    //make the dialog
    if(textView != null){
      textPane = textView.getTextView();
      Window parentWindow = SwingUtilities.windowForComponent(textView.getGUI());
      if(parentWindow != null){
        dialog = parentWindow instanceof Frame ?
                new JDialog((Frame)parentWindow, 
                "Annotation Editor Dialog", false) :
                  new JDialog((Dialog)parentWindow, 
                          "Annotation Editor Dialog", false);
        dialog.setFocusableWindowState(false);
        dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        dialog.setResizable(false);
        dialog.add(this);
        dialog.pack();
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
      aFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      
      JDialog annDialog = new JDialog(aFrame, "Annotation Editor Dialog", false);
      annDialog.setFocusableWindowState(false);
//      annDialog.setResizable(false);
//      annDialog.setUndecorated(true);
      
      SchemaAnnotationEditor pane = new SchemaAnnotationEditor(null);
      annDialog.add(pane);
      annDialog.pack();
      
//      JToolBar tBar = new JToolBar("Annotation Editor", JToolBar.HORIZONTAL);
//      tBar.setLayout(new BorderLayout());
//      tBar.setMinimumSize(tBar.getPreferredSize());
//      tBar.add(pane);
//      aFrame.getContentPane().add(tBar, BorderLayout.NORTH);
      
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
System.out.println("Window up");
      annDialog.setVisible(true);
      System.out.println("Dialog up");      
      
    }catch(HeadlessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch(GateException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * @return the dialog
   */
  public JDialog getDialog() {
    return dialog;
  }
}
