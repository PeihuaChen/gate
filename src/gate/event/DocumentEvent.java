/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 12/12/2000
 *
 *  $Id$
 */
package gate.event;

import gate.*;

import java.util.EventObject;

/**
 * This class models events fired by an {@link gate.Document}.
 */
public class DocumentEvent extends GateEvent {

  /**Event type used to mark the addition of an {@link gate.AnnotationSet}*/
  public static int ANNOTATION_SET_ADDED = 101;

  /**Event type used to mark the removal of an {@link gate.AnnotationSet}*/
  public static int ANNOTATION_SET_REMOVED = 102;

  /**
   * Constructor.
   * @param source the document that has been changed
   * @param type the type of the event
   * @param setName the name of the {@link gate.AnnotationSet} that has been
   * added or removed.
   */
  public DocumentEvent(Document source, int type, String setName) {
    super(source, type);
    this.annotationSetName = setName;
  }

  /**
   * Gets the name of the {@link gate.AnnotationSet} that has been added or
   * removed.
   */
  public String getAnnotationSetName() {
    return annotationSetName;
  }

  private String annotationSetName;

}