/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, 22 March 2004
 *
 *  $Id$
 */
package gate.gui.docview;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.*;

import javax.swing.*;

import gate.*;
import gate.creole.*;
import gate.gui.ActionsPublisher;
import gate.util.GateRuntimeException;
import gate.util.Out;

/**
 * This is the GATE Document viewer/editor. This class is only the shell of the
 * main document VR, which gets populated with views (objects that implement
 * the {@link DocumentView} interface.
 */

public class DocumentEditor extends AbstractVisualResource
                            implements ActionsPublisher {

  public List getActions() {
    return new ArrayList();
  }

  /* (non-Javadoc)
   * @see gate.Resource#cleanup()
   */
  public void cleanup() {
    // TODO Auto-generated method stub
    super.cleanup();
  }

  /* (non-Javadoc)
   * @see gate.Resource#init()
   */
  public Resource init() throws ResourceInstantiationException {
    addComponentListener(new ComponentAdapter() {
      public void componentHidden(ComponentEvent e) {
      }
      public void componentMoved(ComponentEvent e) {
      }
      public void componentResized(ComponentEvent e) {
      }
      //lazily build the GUI only when needed
      public void componentShown(ComponentEvent e) {
Out.prln("Docedit shown");
        initViews();
      }
    });

    return this;
  }

  /**
   * Gets the currently showing top view
   * @return a {@link DocumentView} object.
   */
  protected DocumentView getTopView(){
    if(topViewIdx == -1) return null;
    else return(DocumentView)horizontalViews.get(topViewIdx);
  }
  
  /**
   * Shows a new top view based on an index in the {@link #horizontalViews}
   * list.
   * @param index the index in {@link #horizontalViews} list for the new
   * view to be shown.
   */
  protected void setTopView(int index){
    //deactivate current view
    DocumentView oldView = getTopView();
    if(oldView != null){
      oldView.setActive(false);
    }
    topViewIdx = index;
    DocumentView newView = (DocumentView)horizontalViews.get(topViewIdx);
    //hide if shown at the bottom
    if(bottomViewIdx == topViewIdx){
      setBottomView(null);
      bottomViewIdx  = -1;
    }
    //show the new view
    setTopView(newView);
    //activate if necessary
    if(!newView.isActive()){
      newView.setActive(true);
    }
  }

  /**
   * Sets a new UI component in the top location. This method is intended to 
   * only be called from {@link #setTopView(int)}.
   * @param view the new view to be shown.
   */
  protected void setTopView(DocumentView view){
    topSplit.setTopComponent(view.getGUI());
  }

  /**
   * Gets the currently showing central view
   * @return a {@link DocumentView} object.
   */
  protected DocumentView getCentralView(){
    if(centralViewIdx == -1) return null;
    else return(DocumentView)centralViews.get(centralViewIdx);
  }
  
  /**
   * Shows a new central view based on an index in the {@link #centralViews}
   * list.
   * @param index the index in {@link #centralViews} list for the new
   * view to be shown.
   */
  protected void setCentralView(int index){
    //deactivate current view
    DocumentView oldView = getCentralView();
    if(oldView != null){
      oldView.setActive(false);
    }
    centralViewIdx = index;
    DocumentView newView = (DocumentView)centralViews.get(centralViewIdx);
    //show the new view
    setCentralView(newView);
    //activate if necessary
    if(!newView.isActive()){
      newView.setActive(true);
    }
  }

  /**
   * Sets a new UI component in the central location. This method is intended to 
   * only be called from {@link #setCentralView(int)}.
   * @param view the new view to be shown.
   */
  protected void setCentralView(DocumentView view){
    topSplit.setBottomComponent(view.getGUI());
  }
  
  
  /**
   * Gets the currently showing bottom view
   * @return a {@link DocumentView} object.
   */
  protected DocumentView getBottomView(){
    if(bottomViewIdx == -1) return null;
    else return(DocumentView)horizontalViews.get(bottomViewIdx);
  }
  
  /**
   * Shows a new bottom view based on an index in the {@link #horizontalViews}
   * list.
   * @param index the index in {@link #horizontalViews} list for the new
   * view to be shown.
   */
  protected void setBottomView(int index){
    //deactivate current view
    DocumentView oldView = getBottomView();
    if(oldView != null){
      oldView.setActive(false);
    }
    bottomViewIdx = index;
    DocumentView newView = (DocumentView)horizontalViews.get(bottomViewIdx);
    //hide if shown at the top
    if(topViewIdx == bottomViewIdx){
      setTopView(null);
      topViewIdx  = -1;
    }
    //show the new view
    setBottomView(newView);
    //activate if necessary
    if(!newView.isActive()){
      newView.setActive(true);
    }
  }

  /**
   * Sets a new UI component in the top location. This method is intended to 
   * only be called from {@link #setBottomView(int)}.
   * @param view the new view to be shown.
   */
  protected void setBottomView(DocumentView view){
    bottomSplit.setBottomComponent(view.getGUI());
  }
  
  
  /**
   * Gets the currently showing right view
   * @return a {@link DocumentView} object.
   */
  protected DocumentView getRightView(){
    if(rightViewIdx == -1) return null;
    else return(DocumentView)verticalViews.get(rightViewIdx);
  }
  
  /**
   * Shows a new right view based on an index in the {@link #verticalViews}
   * list.
   * @param index the index in {@link #verticalViews} list for the new
   * view to be shown.
   */
  protected void setRightView(int index){
    //deactivate current view
    DocumentView oldView = getRightView();
    if(oldView != null){
      oldView.setActive(false);
    }
    rightViewIdx = index;
    DocumentView newView = (DocumentView)verticalViews.get(rightViewIdx);
    //show the new view
    setRightView(newView);
    //activate if necessary
    if(!newView.isActive()){
      newView.setActive(true);
    }
  }

  /**
   * Sets a new UI component in the right hand side location. This method is 
   * intended to only be called from {@link #setRightView(int)}.
   * @param view the new view to be shown.
   */
  protected void setRightView(DocumentView view){
    horizontalSplit.setRightComponent(view.getGUI());
  }  
  
//  protected void setCentralView(DocumentView view){
//    topSplit.setBottomComponent(view.getGUI());
//  }
//
//  protected void setBottomView(DocumentView view){
//    bottomSplit.setBottomComponent(view.getGUI());
//  }
//
//  protected void setRightView(DocumentView view){
//    horizontalSplit.setRightComponent(view.getGUI());
//  }

  /* (non-Javadoc)
   * @see gate.VisualResource#setTarget(java.lang.Object)
   */
  public void setTarget(Object target) {
    this.document = (Document)target;
  }

  protected void initViews(){
    //start building the UI
    setLayout(new BorderLayout());
    JProgressBar progressBar = new JProgressBar();
    progressBar.setStringPainted(true);
    progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, progressBar.getPreferredSize().height));
    add(progressBar, BorderLayout.CENTER);

    progressBar.setString("Building views");
    progressBar.setValue(10);

    //create the skeleton UI
    topSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, null, null);
    bottomSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSplit, null);
    horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, bottomSplit, null);

    //create the bars
    topBar = new JToolBar(JToolBar.HORIZONTAL);
    topBar.setFloatable(false);
    add(topBar, BorderLayout.NORTH);

    bottomBar = new JToolBar(JToolBar.HORIZONTAL);
    bottomBar.setFloatable(false);
    add(bottomBar, BorderLayout.SOUTH);

    leftBar = new JToolBar(JToolBar.VERTICAL);
    leftBar.setFloatable(false);
    add(leftBar, BorderLayout.WEST);

    rightBar = new JToolBar(JToolBar.VERTICAL);
    rightBar.setFloatable(false);
    add(rightBar, BorderLayout.EAST);

    progressBar.setValue(40);
    //parse all Creole resources and look for document views
    Set vrSet = Gate.getCreoleRegister().getVrTypes();
    Iterator vrIter = vrSet.iterator();
    views = new ArrayList();
    centralViews = new ArrayList();
    verticalViews = new ArrayList();
    horizontalViews = new ArrayList();

    while(vrIter.hasNext()){
      ResourceData rData = (ResourceData)Gate.getCreoleRegister().
                           get(vrIter.next());
      try{
        if(DocumentView.class.isAssignableFrom(rData.getResourceClass())){
          //create the resource
          DocumentView aView = (DocumentView)Factory.
                               createResource(rData.getClassName());
          aView.setTarget(document);
          views.add(aView);
          //add the view
          switch(aView.getType()){
            case DocumentView.CENTRAL :
              centralViews.add(aView);
//              setCentralComponent(aView.getGUI());
              break;
            case DocumentView.VERTICAL :
              verticalViews.add(aView);
              break;
            case DocumentView.HORIZONTAL :
              horizontalViews.add(aView);
            default :
              throw new GateRuntimeException(getClass().getName() +  ": Invalid view type");
          }
        }
      }catch(ClassNotFoundException cnfe){
        cnfe.printStackTrace();
      }catch(ResourceInstantiationException rie){
            rie.printStackTrace();
      }

    }

    //populate the main VIEW
    remove(progressBar);
    progressBar = null;
    add(horizontalSplit, BorderLayout.CENTER);

  }

  protected static class ViewButton extends JButton{
    public ViewButton(DocumentView view){
      super();
      this.view = view;
      if(view.getType() == DocumentView.HORIZONTAL){
      }
    }
    protected DocumentView view;
  }

  protected JSplitPane horizontalSplit;
  protected JSplitPane topSplit;
  protected JSplitPane bottomSplit;

  protected JToolBar topBar;
  protected JToolBar rightBar;
  protected JToolBar leftBar;
  protected JToolBar bottomBar;

  protected Document document;

  /**
   * A list of {@link DocumentView} objects representing the components
   */
  protected List views;

  /**
   * A list of {@link DocumentView} objects of type {@link DocumentView#CENTRAL}
   */
  protected List centralViews;
  
  /**
   * A list of {@link DocumentView} objects of type 
   * {@link DocumentView#VERTICAL}
   */
  protected List verticalViews;

  /**
   * A list of {@link DocumentView} objects of type 
   * {@link DocumentView#HORIZONTAL}
   */
  protected List horizontalViews;

  /**
   * The index in {@link #centralViews} of the currently active central view.
   * <code>-1</code> if none is active.
   */
  protected int centralViewIdx = -1;

  /**
   * The index in {@link #verticalViews} of the currently active right view.
   * <code>-1</code> if none is active.
   */
  protected int rightViewIdx = -1;
  
  /**
   * The index in {@link #horizontalViews} of the currently active top view.
   * <code>-1</code> if none is active.
   */
  protected int topViewIdx = -1;
  
  /**
   * The index in {@link #horizontalViews} of the currently active bottom view.
   * <code>-1</code> if none is active.
   */
  protected int bottomViewIdx = -1;

}