/*
 *  Copyright (c) 1998-2004, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 27 Sep 2001
 *
 *  $Id$
 */
package gate.creole;

/**
 * Thrown by {@link gate.Executable}s after they have stopped their execution
 * as a result of a call to their interrupt() method.
 */
public class ExecutionInterruptedException extends ExecutionException {
  public ExecutionInterruptedException(String message){
    super(message);
  }

  public ExecutionInterruptedException(){
  }
}