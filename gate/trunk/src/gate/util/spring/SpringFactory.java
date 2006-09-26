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

import gate.Factory;
import gate.FeatureMap;
import gate.util.GateException;
import gate.util.persistence.PersistenceManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import org.springframework.core.io.Resource;

/**
 * This class contains spring-aware factory methods for useful GATE
 * components.
 */
public class SpringFactory {

  /**
   * Creates a feature map from a source map. Any values in the source
   * map that are of type
   * <code>org.springframework.core.io.Resource</code> are converted
   * to their corresponding {@link java.net.URL}. For example:
   * 
   * <pre>
   *     &lt;bean id=&quot;feature-map&quot; class=&quot;gate.util.spring.SpringFactory&quot;
   *           factory-method=&quot;createFeatureMap&quot;&gt;
   *       &lt;constructor-arg&gt;
   *         &lt;map&gt;
   *           &lt;entry key=&quot;inputASName&quot; value=&quot;Extra&quot; /&gt;
   *           &lt;entry key=&quot;config&quot;&gt;
   *             &lt;value type=&quot;org.springframework.core.io.Resource&quot;&gt;path/to/config.xml&lt;/value&gt;
   *           &lt;/entry&gt;
   *         &lt;/map&gt;
   *       &lt;/constructor-arg&gt;
   *     &lt;/bean&gt;
   * </pre>
   * 
   * This is ideal for use in anonymous nested bean definitions to
   * provide, e.g. parameters for gate.Factory.createResource().
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

  /**
   * Loads a saved application state (gapp file) from the given Spring
   * resource. The resource is first looked up as a {@link File}, and
   * if found the application is loaded using
   * {@link PersistenceManager#loadObjectFromFile(File) loadObjectFromFile}.
   * If the resource cannot be accessed as a file it is accessed as a
   * {@link URL} and the application loaded with
   * {@link PersistenceManager#loadObjectFromUrl(URL) loadObjectFromUrl}.
   * This is useful as many PRs either require or function better with
   * file: URLs than with other kinds of URL.
   * 
   * @param res
   * @return
   * @throws GateException
   * @throws IOException
   */
  public static Object loadObjectFromResource(Resource res)
          throws GateException, IOException {
    File resourceFile = null;
    try {
      resourceFile = res.getFile();
    }
    catch(IOException ioe) {
      // OK, so we can't get a file...
    }

    if(resourceFile != null) {
      return PersistenceManager.loadObjectFromFile(resourceFile);
    }
    else {
      URL resourceURL = res.getURL();
      return PersistenceManager.loadObjectFromUrl(resourceURL);
    }
  }
}
