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

import gate.*;

import javax.swing.*;
import java.awt.event.*;

public class PRHandle extends ResourceHandle {

  public PRHandle(ProcessingResource res, ProjectData project) {
    super(res, project);
    if(res instanceof gate.creole.tokeniser.DefaultTokeniser){
      setSmallIcon(new ImageIcon(
             getClass().getResource("/gate/resources/img/shefTokeniser.gif")));
    }else if(res instanceof gate.creole.gazetteer.DefaultGazetteer){
      setSmallIcon(new ImageIcon(
             getClass().getResource("/gate/resources/img/shefGazetteer.gif")));
    }else{
      setSmallIcon(new ImageIcon(
             getClass().getResource("/gate/resources/img/genericPr.gif")));
    }
    popup = new JPopupMenu();
    popup.add(new ClosePRAction());
  }



  class ClosePRAction extends AbstractAction{
    public ClosePRAction(){
      super("Close");
    }

    public void actionPerformed(ActionEvent e){
      project.remove(myself);
    }
  }
}