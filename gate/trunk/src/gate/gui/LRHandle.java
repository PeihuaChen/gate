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

public class LRHandle extends ResourceHandle {

  public LRHandle(LanguageResource res, String title) {
    super(res, title);
    setSmallIcon(new ImageIcon(
           getClass().getResource("/gate/resources/img/lr.gif")));
    popup = new JPopupMenu();
    popup.add(new CloseLRAction());
  }

  class CloseLRAction extends AbstractAction{
    public CloseLRAction(){
      super("Close");
    }

    public void actionPerformed(ActionEvent e){
    }
  }
}