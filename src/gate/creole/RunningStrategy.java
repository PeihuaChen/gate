/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 11 Apr 2002
 *
 *  $Id$
 */
package gate.creole;


import gate.*;

/**
 * Base interface for objects that are used to decide whether a PR member of a
 * {@link ConditionalController} needs to be run.
 */

public interface RunningStrategy{
  /**
   * Returns true if the associated PR should be run.
   * @return a boolean value.
   */
  public boolean shouldRun();

  /**
   * Returns the run mode (see {@link RUN_ALWAYS}, {@link RUN_NEVER},
   * {@link RUN_CONDITIONAL}).
   * @return and int value.
   */
  public int getRunMode();

  /**
   * Gets the associated ProcessingResource.
   * @return a {@link ProcessingResource} value.
   */
  public ProcessingResource getPR();


  /**
   * Run mode constant meaning the associated PR should be run regardless of
   * what the {@link #shouldRun()} method returns.
   */
  public static final int RUN_ALWAYS = 1;

  /**
   * Run mode constant meaning the associated PR should NOT be run regardless of
   * what the {@link #shouldRun()} method returns.
   */
  public static final int RUN_NEVER = 2;

  /**
   * Run mode constant meaning the associated PR should be run only if the
   * {@link #shouldRun()} method returns true.
   */
  public static final int RUN_CONDITIONAL = 4;

  public static class RunAlwaysStrategy implements RunningStrategy{
    public RunAlwaysStrategy(ProcessingResource pr){
      this.pr = pr;
    }
    public boolean shouldRun(){return true;}

    public int getRunMode(){return RUN_ALWAYS;}

    public ProcessingResource getPR(){return pr;}

    ProcessingResource pr;
  }
}