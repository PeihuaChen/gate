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
import java.net.URL;
import java.awt.Component;

import gate.*;
import gate.util.*;
import gate.creole.*;

/**
 * Class used to store the information about an open resource.
 * Such information will include icon to be used for tree components,
 * popup menu for right click events, etc.
 */
class DefaultResourceHandle implements IResourceHandle{
  public DefaultResourceHandle(Resource res){
    this.resource = res;
    rData = (ResourceData)Gate.getCreoleRegister().
                                            get(resource.getClass().getName());
//    this.icon = new ImageIcon(new URL("gate://" + rData.getIcon()));
    popup = null;
    title = (String)resource.getFeatures().get("NAME");
    shown = false;
    buildViews();
  }
/*
  public DefaultResourceHandle(Resource resource, ProjectData project){
    this.resource = resource;
    this.title = (String)resource.getFeatures().get("NAME");
    this.project = project;
    buildViews();
    myself = this;
  }
*/
/*
  public DefaultResourceHandle(String title, ProjectData project){
    this.resource = null;
    this.title = title;
    this.project = project;
    largeView = null;
    smallView = null;
    myself = this;
  }
*/

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
    return resource;
  }

  protected void buildViews(){
    //build the large views
    JTabbedPane view = new JTabbedPane(JTabbedPane.BOTTOM);
    List views = rData.getAllViews();
    Iterator viewsIter = views.iterator();
    while(viewsIter.hasNext()){
      FeatureMap viewFm = (FeatureMap)viewsIter.next();
      FeatureMap empty = Factory.newFeatureMap();
      String type = (String)viewFm.get("TYPE");
    }

    FeaturesEditor fEdt = new FeaturesEditor();
    fEdt.setResource(resource);
    view.add("Features", fEdt);
    largeView = view;
    smallView = null;
  }

  public String toString(){ return title;}

  JPopupMenu popup;
  String title;
  String tooltipText;
  Resource resource;
  ResourceData rData;
  Icon icon;
  boolean shown;
  JComponent smallView;
  JComponent largeView;

}
