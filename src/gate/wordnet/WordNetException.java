package gate.wordnet;

import gate.util.GateException;

/**
 * Title:        Gate2
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      University Of Sheffield
 * @author
 * @version 1.0
 */

public class WordNetException extends GateException {

  public WordNetException() {
    super();
  }

  public WordNetException(String s) {
    super(s);
  }

  public WordNetException(Throwable e) {
    super(e.toString());
    this.e = e;
  }

}