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

import java.util.*;

import gate.*;
import gate.creole.*;

class ApplicationHandle extends CustomResourceHandle {

  public ApplicationHandle(SerialController controller, ProjectData project) {
    super(controller, project);
    setIcon(new ImageIcon(
           getClass().getResource("/gate/resources/img/application.gif")));
    popup = new JPopupMenu();

    largeView = super.getLargeView();
    if(largeView instanceof JTabbedPane){
      viewer = new ApplicationViewer((SerialController)resource, this);
      largeView.add(viewer, "Design");
      ((JTabbedPane)largeView).setSelectedComponent(viewer);
    }
  }

  ApplicationViewer viewer;

  public JPopupMenu getPopup(){
    return viewer.getPopup();
  }

  class CloseAction extends AbstractAction{
    public CloseAction(){
      super("Close");
    }

    public void actionPerformed(ActionEvent e){
      project.remove(myself);
    }
  }

}