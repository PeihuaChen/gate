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
import java.net.*;
import java.util.*;

import gate.*;
import gate.util.*;
import gate.creole.*;
import gate.event.*;

class ApplicationHandle extends DefaultResourceHandle {

  public ApplicationHandle(SerialController controller,
                           StatusListener sListener,
                           ProgressListener pListener) {
    super(controller);
    try {
      try {
        this.icon = new ImageIcon(new URL("gate:/img/application.gif"));
      } catch(MalformedURLException mue){
        mue.printStackTrace(Err.getPrintWriter());
      }
      FeatureMap params = Factory.newFeatureMap();
      params.put("controller", controller);
      appView = (ApplicationViewer)Factory.createResource(
                            "gate.gui.ApplicationViewer", params);
      appView.setHandle(this);
      appView.addStatusListener(sListener);
      appView.addProgressListener(pListener);
      JTabbedPane view = (JTabbedPane)super.getLargeView();
      view.add("Design", appView);
      view.setSelectedComponent(appView);
    } catch(ResourceInstantiationException rie) {
      rie.printStackTrace(Err.getPrintWriter());
    }

    viewPopupElements = new MenuElement[]{};
  }//ApplicationHandle

  ApplicationViewer appView;
  MenuElement[] viewPopupElements;

  public JPopupMenu getPopup() {
    //clear the local elements from popup
    for(int i = 0; i< viewPopupElements.length; i++)
        popup.remove((JComponent)viewPopupElements[i]);
    //update the popup with new updated components
    viewPopupElements = appView.getPopupElements();
    for(int i = 0; i< viewPopupElements.length; i++)
        popup.add((JComponent)viewPopupElements[i]);
    return popup;
  }//getPopup

}//ApplicationHandle class