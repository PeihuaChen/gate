/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 22/01/2001
 *
 *  $Id$
 *
 */

package gate.gui;

import java.io.*;
import java.util.*;

public class ProjectData {

  public ProjectData(File projectFile) {
    this.projectFile = projectFile;
    this.projectFileName = projectFile.getAbsolutePath();
    appList = new ArrayList();
    dsList = new ArrayList();
    lrList = new ArrayList();
    prList = new ArrayList();
  }

  public String toString(){
    return projectFile.getName();
  }

  public void addApplication(ApplicationHandle newApp){
    appList.add(newApp);
  }

  List getApplicationsList(){
    return appList;
  }

  public void addLR(LRHandle newLR){
    lrList.add(newLR);
  }
  List getLRList(){
    return lrList;
  }

  public void addPR(PRHandle newPR){
    prList.add(newPR);
  }
  List getPRList(){
    return prList;
  }

  transient File projectFile;
  String projectFileName;
  /**
   * list of ResourceHandler
   */
  List lrList;
  List prList;
  List appList;
  List dsList;
}