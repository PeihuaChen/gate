/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 28 May 2002
 *
 *  $Id$
 */

package gate.ml;

import weka.core.*;
/**
 */
public interface AttributeDetector{

  /**
   * Gets the definition for the attribute handled by this detector.
   * @return an Attribute object.
   */
  public Attribute getAttribute();


  /**
   * Gets the value detected for the attribute in the current instance.
   * If <code>null</code> is returned, the attribute will be marked as missing.
   * @return An Object value. This will be converted to the according numeric
   * type or to string.
   * @param data contains tha data neede to identify the instance for which the
   * attribute value is requested. The actual type of the value depends on the
   * type of InstanceDetector used.
   */
  public Object getAttributeValue(Object data);

  /**
   * Sets the data collector this data listener lives into.
   * @param collector
   */
  public void setDataCollector(DataCollector collector);
}