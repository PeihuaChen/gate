
//Title:        JapeException.java
//Version:      $Id$
//Copyright:    Copyright (c) 1998
//Author:       Hamish
//Company:      NLP Group, Univ. of Sheffield
//Description:  


package gate.jape;

import gate.annotation.*;
import gate.util.*;

/** Superclass of all JAPE exceptions. */
public class JapeException extends GateException {
  public JapeException(String message) {
    super(message);
  }

  public JapeException() {
    super();
  }
} 
