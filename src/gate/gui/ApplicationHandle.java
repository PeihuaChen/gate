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

class ApplicationHandle extends ResourceHandle {

  public ApplicationHandle(SerialController controller, ProjectData project) {
    super(controller, project);
    setSmallIcon(new ImageIcon(
           getClass().getResource("/gate/resources/img/application.gif")));
    popup = new JPopupMenu();
    popup.add(new DesignApplicationAction());
    popup.add(new RunApplicationAction());

    largeView = super.getLargeView();
    if(largeView instanceof JTabbedPane){
      viewer = new ApplicationViewer((SerialController)resource, project);
      largeView.add(viewer, "Design");
      ((JTabbedPane)largeView).setSelectedComponent(viewer);
    }
  }

  ApplicationViewer viewer;

  class RunApplicationAction extends AbstractAction{
    public RunApplicationAction(){
      super("Run");
    }

    public void actionPerformed(ActionEvent e){
    }
  }

  class DesignApplicationAction extends AbstractAction{
    public DesignApplicationAction(){
      super("Design");
    }

    public void actionPerformed(ActionEvent e){
    }
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