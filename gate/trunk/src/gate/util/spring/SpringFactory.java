/*
 *  Init.java
 *
 *  Copyright (c) 1998-2006, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Ian Roberts, 07/Oct/2006
 *
 *  $Id$
 */

package gate.util.spring;

import gate.FeatureMap;
import gate.Factory;

import java.io.IOException;

import java.util.Map;
import java.util.Iterator;

import org.springframework.core.io.Resource;

/**
 * This class contains spring-aware factory methods for useful GATE components.
 */
public class SpringFactory {

  /**
   * Creates a feature map from a source map.  Any values in the source map
   * that are of type <code>org.springframework.core.io.Resource</code> are
   * converted to their corresponding {@link java.net.URL}.  For example:
   *
   * <pre>
   * &lt;bean id="feature-map" class="gate.util.spring.SpringFactory"
   *       factory-method="createFeatureMap"&gt;
   *   &lt;constructor-arg&gt;
   *     &lt;map&gt;
   *       &lt;entry key="inputASName" value="Extra" /&gt;
   *       &lt;entry key="config"&gt;
   *         &lt;value type="org.springframework.core.io.Resource"&gt;path/to/config.xml&lt;/value&gt;
   *       &lt;/entry&gt;
   *     &lt;/map&gt;
   *   &lt;/constructor-arg&gt;
   * &lt;/bean&gt;
   * </pre>
   *
   * This is ideal for use in anonymous nested bean definitions to provide,
   * e.g. parameters for gate.Factory.createResource().
   */
  public static FeatureMap createFeatureMap(Map sourceMap) throws IOException {
    FeatureMap fm = Factory.newFeatureMap();
    Iterator sourceEntries = sourceMap.entrySet().iterator();
    while(sourceEntries.hasNext()) {
      Map.Entry entry = (Map.Entry)sourceEntries.next();
      Object key = entry.getKey();
      Object value = entry.getValue();

      // convert Spring resources to URLs
      if(value instanceof Resource) {
        value = ((Resource)value).getURL();
      }

      fm.put(key, value);
    }

    return fm;
  }
}
