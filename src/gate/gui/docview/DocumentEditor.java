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

import gate.CreoleRegister;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.Resource;
import gate.creole.AbstractVisualResource;
import gate.creole.ResourceData;
import gate.creole.ResourceInstantiationException;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.*;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.*;

import gate.gui.ActionsPublisher;
import gate.util.GateRuntimeException;
import gate.util.OffsetComparator;
import gate.util.Out;
import javax.swing.event.*;

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


  protected void setTopComponent(Component comp){
    topSplit.setTopComponent(comp);
  }

  protected void setCentralComponent(Component comp){
    topSplit.setBottomComponent(comp);
  }

  protected void setBottomComponent(Component comp){
    bottomSplit.setBottomComponent(comp);
  }

  protected void setRightComponent(Component comp){
    horizontalSplit.setRightComponent(comp);
  }

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
    public ViewButton(DocumentView view, int type){
      super();
      this.view = view;
      if(type == DocumentView.HORIZONTAL)
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
  
  protected List centralViews;
  
  protected List verticalViews;
  
  protected List horizontalViews;

}