package gate.util.persistence;

import java.io.*;

/**
 * Title:        Gate2
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      University Of Sheffield
 * @author
 * @version 1.0
 */

public class GateObjectOutputStream extends ObjectOutputStream {

  public GateObjectOutputStream(OutputStream out) throws IOException{
    super();
    this.out = out;
  }

  public void writeObjectOverride(Object obj) throws IOException{
System.out.println("Write Obj");
super.writeObject(obj);
  }
private OutputStream out;	/* Stream to write the data to */
}