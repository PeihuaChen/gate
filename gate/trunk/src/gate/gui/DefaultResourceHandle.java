/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 23/01/2001
 *
 *  $Id$
 *
 */

package gate.gui;

import javax.swing.*;
import java.util.*;
import java.net.*;
import java.awt.Component;

import gate.*;
import gate.util.*;
import gate.creole.*;

/**
 * Class used to store the information about an open resource.
 * Such information will include icon to be used for tree components,
 * popup menu for right click events, etc.
 */
class DefaultResourceHandle implements ResourceHandle{

  public DefaultResourceHandle(FeatureBearer res){
    this.resource = res;
    rData = (ResourceData)Gate.getCreoleRegister().
                                            get(resource.getClass().getName());
    if(rData != null){
      String iconName = rData.getIcon();
      if(iconName == null){
        if(resource instanceof LanguageResource) iconName = "lr.gif";
        else if(resource instanceof ProcessingResource) iconName = "pr.gif";
      }
      try{
        this.icon = new ImageIcon(new URL("gate:/img/" + iconName));
      }catch(MalformedURLException mue){
        mue.printStackTrace(Err.getPrintWriter());
      }
    }else{
      try{
        this.icon = new ImageIcon(new URL("gate:/img/lr.gif"));
      }catch(MalformedURLException mue){
        mue.printStackTrace(Err.getPrintWriter());
      }
    }

    popup = null;
    title = (String)resource.getFeatures().get("gate.NAME");
    shown = false;
    buildViews();
  }

  public Icon getIcon(){
    return icon;
  }

  public void setIcon(Icon icon){
    this.icon = icon;
  }

  public String getTitle(){
    return title;
  }

  public void setTitle(String newTitle){
    this.title = newTitle;
  }

  /**
   * Returns a GUI component to be used as a small viewer/editor, e.g. below
   * the main tree in the Gate GUI for the selected resource
   */
  public JComponent getSmallView(){
    return smallView;
  }

  /**
   * Returns the large view for this resource. This view will go into the main
   * display area.
   */
  public JComponent getLargeView(){
    return largeView;
  }

  public JPopupMenu getPopup(){
    return popup;
  }

  public void setPopup(JPopupMenu popup){
    this.popup = popup;
  }

  public void setShown(boolean visible){
    shown = visible;
  }

  public boolean isShown(){
    return shown;
  }

  public String getTooltipText(){
    return tooltipText;
  }

  public void setTooltipText(String text){
    this.tooltipText = text;
  }

  public Resource getResource(){
    if(resource instanceof Resource) return (Resource)resource;
    else return null;
  }

  public FeatureBearer getFeatureBearer(){
    return resource;
  }

  protected void buildViews(){
    //build the large views
    JTabbedPane view = new JTabbedPane(JTabbedPane.BOTTOM);

    /* Fancy discovery code goes here
    ...
    ...
    ...
    */

    /* Not so fancy hardcoded views build */
    //Language Resources
    if(resource instanceof gate.corpora.DocumentImpl){
      try{
        FeatureMap params = Factory.newFeatureMap();
        params.put("document", resource);
        view.add("Annotations",
                 (JComponent)Factory.createResource("gate.gui.AnnotationEditor",
                                                    params)
                );
      }catch(ResourceInstantiationException rie){
        rie.printStackTrace(Err.getPrintWriter());
      }
    }else if(resource instanceof LanguageResource){
      //catch all unknown LR's
    }else if(resource instanceof ProcessingResource){
      //catch all unknown PR's
    }

    FeaturesEditor fEdt = new FeaturesEditor();
    fEdt.setFeatureBearer(resource);
    view.add("Features", fEdt);
    largeView = view;
    smallView = null;
  }

  public String toString(){ return title;}

  JPopupMenu popup;
  String title;
  String tooltipText;
  FeatureBearer resource;
  ResourceData rData;
  Icon icon;
  boolean shown;
  JComponent smallView;
  JComponent largeView;

}
