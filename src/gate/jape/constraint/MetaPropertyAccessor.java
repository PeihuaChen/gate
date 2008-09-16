/*
 *  Copyright (c) 1998-2008, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Eric Sword, 09/03/08
 *
 *  $Id$
 */

package gate.jape.constraint;

import gate.AnnotationSet;
import gate.Document;

import org.apache.log4j.Logger;

/**
 * Accessor which returns a particular property or meta-property of an
 * annotation, such as length or string.
 *
 * @version $Revision$
 * @author esword
 */
public abstract class MetaPropertyAccessor implements AnnotationAccessor {
  protected static final Logger log = Logger.getLogger(MetaPropertyAccessor.class);

  public MetaPropertyAccessor() {
    super();
  }

  @Override
  public int hashCode() {
    return this.getClass().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if(obj == null) return false;
    if(obj == this) return true;
    if(!(this.getClass().equals(obj.getClass()))) return false;

    return true;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName();
  }

  /**
   * Returns the context if it is a Document, or, if the context is
   * an AnnotationSet, returns the Document from it.
   *
   * @param context
   * @return
   */
  protected Document getDocument(Object context) {
    Document doc = null;
    if (context instanceof Document)
      doc = (Document)context;
    else if (context instanceof AnnotationSet) {
      doc = ((AnnotationSet)context).getDocument();
    }
    else
      throw new IllegalArgumentException("Context must be a Document or an AnnotationSet, not: "
            + (context != null ? context.getClass() : "null"));
    return doc;
  }

  public void setKey(Object key) {
    if(key != null || !(key.equals("")))
      log.warn(this.getClass().getName() + " doesn't use key values.  Key was: " + key);
  }

  /**
   * Sub-classes should return the name of the meta-property which they implement.
   */
  public abstract Object getKey();
}
