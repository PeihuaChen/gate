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
import java.awt.event.*;
import java.awt.Component;

import java.util.*;

import gate.*;
import gate.util.*;
import gate.creole.*;

class ApplicationHandle extends DefaultResourceHandle {

  public ApplicationHandle(SerialController controller) {
    super(controller);

    largeView = super.getLargeView();
    if(largeView instanceof JTabbedPane){
      try{
        FeatureMap params = Factory.newFeatureMap();
        params.put("controller", resource);
        appView =(ApplicationViewer)Factory.createResource(
                                      "gate.gui.ApplicationViewer",
                                      params);
        largeView.add("Design", appView);
        ((JTabbedPane)largeView).setSelectedComponent(appView);
      }catch(ResourceInstantiationException rie){
        rie.printStackTrace(Err.getPrintWriter());
      }
    }
    viewPopupElements = new MenuElement[]{};
  }

  ApplicationViewer appView;
  MenuElement[] viewPopupElements;

  public JPopupMenu getPopup(){
    //clear the local elements from popup
    for(int i = 0; i< viewPopupElements.length; i++)
        popup.remove((JComponent)viewPopupElements[i]);
    //update the popup with new updated components
    viewPopupElements = appView.getPopupElements();
    for(int i = 0; i< viewPopupElements.length; i++)
        popup.add((JComponent)viewPopupElements[i]);
    return popup;
  }

}