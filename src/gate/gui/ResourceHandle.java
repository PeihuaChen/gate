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

/**
 * Title:        Gate2
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      University Of Sheffield
 * @author Hamish, Kalina, Valy, Cristi
 * @version 1.0
 */
import javax.swing.*;
import java.util.*;

import gate.*;



/**
 * Class used to store the information about an open resource.
 * Such information will include icon to be used for tree components,
 * popup menu for right click events, etc.
 */
class ResourceHandle{
  public ResourceHandle(){}

  public ResourceHandle(Resource resource, ProjectData project){
    this.resource = resource;
    this.title = (String)resource.getFeatures().get("NAME");
    this.project = project;
    buildViews();
    myself = this;
  }

  public ResourceHandle(String title, ProjectData project){
    this.resource = null;
    this.title = title;
    this.project = project;
    largeView = null;
    smallView = null;
    myself = this;
  }

  public Icon getSmallIcon(){
    return smallIcon;
  }

  public void setSmallIcon(Icon icon){
    this.smallIcon = icon;
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
    JTabbedPane view = new JTabbedPane(JTabbedPane.BOTTOM);
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
  Icon smallIcon;
  boolean shown;
  JComponent smallView;
  JComponent largeView;

  ResourceHandle myself;
  ProjectData project;
}
