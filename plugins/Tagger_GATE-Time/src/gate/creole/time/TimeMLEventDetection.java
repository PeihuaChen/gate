/*
 *  TimeMLEventDetection.java
 *
 * Copyright (c) 2016, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 3, 29 June 2007.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 * Mark A. Greenwood, 09/05/2016
 */

package gate.creole.time;

import gate.creole.PackagedController;
import gate.creole.metadata.AutoInstance;
import gate.creole.metadata.AutoInstanceParam;
import gate.creole.metadata.CreoleResource;

@CreoleResource(name = "TimeML Event Detection", autoinstances = @AutoInstance(parameters = {
  @AutoInstanceParam(name="pipelineURL", value="resources/applications/tml-events-ml-application.gapp"),
  @AutoInstanceParam(name="menu", value="TimeML")}),
    comment = "TimeML Event Detection Application")
public class TimeMLEventDetection extends PackagedController {

  private static final long serialVersionUID = 2014950451164916200L;

}
