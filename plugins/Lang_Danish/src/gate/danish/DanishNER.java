/*
 * Copyright (c) 1995-2016, The University of Sheffield. See the file
 * COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 * 
 * This file is part of GATE (see http://gate.ac.uk/), and is free software,
 * licenced under the GNU Library General Public License, Version 2, June 1991
 * (in the distribution as file licence.html, and also available at
 * http://gate.ac.uk/gate/licence.html).
 * 
 * Mark A. Greenwood, 2/02/2016
 */

package danish;

import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.stanford.NER;

import java.net.URL;

@CreoleResource(name = "Danish NER",
    comment = "Stanford NER for Danish")
public class DanishNER extends NER {

  private static final long serialVersionUID = 5657607888874448666L;

  @Override
  @CreoleParameter(comment = "Path to the tagger's model file",
  	defaultValue = "resources/ner/da01.model.gz")
  public void setModelFile(URL modelFile) {
    super.setModelFile(modelFile);
  }
}
