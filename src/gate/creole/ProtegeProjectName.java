/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Angel Kirilov 18/04/2002
 *
 *  $Id$
 *
 */
package gate.creole;

import gate.*;


/** Dummy Protege LR. Just keep the Protege project file name */
public class ProtegeProjectName extends AbstractLanguageResource 
                                implements ProtegeProject {

  private String projectName;

  public ProtegeProjectName() {
    projectName = null;
  }
  
  public void setProjectName(String name) {
    projectName = name;
  } // setProjectName(String name)
  
  public String getProjectName() {
    return projectName;
  } // getProjectName()
} // class ProtegeProjectName extends AbstractLanguageResource