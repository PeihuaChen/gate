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
  public ResourceHandle(Resource resource, String title){
    this.resource = resource;
    this.title = title;
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
    return null;
  }

  /**
   * Returns a list of GUI components capable of diaplaying this resource.
   * These components will be used as various views of the resource.
   */
  public java.util.List getViewers(){
    return new ArrayList();
  }

  public JPopupMenu getPopup(){
    return popup;
  }

  public void setPopup(JPopupMenu popup){
    this.popup = popup;
  }

  JPopupMenu popup;
  String title;
  Resource resource;
  Icon smallIcon;
}
