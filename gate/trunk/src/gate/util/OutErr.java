/*
 * OutErr.java
 *
 * Oana Hamza, 22 September 2000
 *
 * $Id$
 */

package gate.util;

import java.io.*;

/** Shorthand for the <CODE> System.out.print and System.err println</CODE>
  * methods.
  */
public class OutErr extends PrintStream {

  /** The constructor is private because the user is not interested
    * how an object belonging to this class is build
    */
  private OutErr(OutputStream out) {
    super(out, true);
  }// OutErr

  /** Return an OutErr object based on System.err */
  public static OutErr getMeAnErr() {
    return new OutErr(System.err);
  }// getMeAnErr

  /** Return an OutErr object based on the OutputStream provided by the user */
  public static OutErr getMePrintStream(OutputStream out) {
    return new OutErr(out);
  }// getMePrintStream

  /** Return an OutErr object based on System.out */
  public static OutErr getMeAnOut() {
    return new OutErr(System.out);
  }// getMeAnOut

  /** @see java.io.PrintStream#print(boolean) */
  public void pr(boolean b) {
    print(b);
  }

  /** @see java.io.PrintStream#print(char) */
  public void pr(char c) {
    print(c);
  }

  /** @see java.io.PrintStream#print(int) */
  public void pr(int i) {
    print(i);
  }

  /** @see java.io.PrintStream#print(long) */
  public void pr(long l) {
    print(l);
  }

  /** @see java.io.PrintStream#print(float) */
  public void pr(float f) {
    print(f);
  }

  /** @see java.io.PrintStream#print(double) */
  public void pr(double d) {
    print(d);
  }

  /** @see java.io.PrintStream#print(char[]) */
  public void pr(char s[]) {
    print(s);
  }

  /** @see java.io.PrintStream#print(java.lang.String) */
  public void pr(String s) {
    print(s);
  }

  /** @see java.io.PrintStream#print(java.lang.Object) */
  public void pr(Object obj) {
    print(obj);
  }

  /** @see java.io.PrintStream#println() */
  public void prln() {
    println();
  }

  /** @see java.io.PrintStream#println(boolean) */
  public void prln(boolean x) {
    println(x);
  }

  /** @see java.io.PrintStream#println(char) */
  public void prln(char x) {
    println(x);
  }

  /** @see java.io.PrintStream#println(int) */
  public void prln(int x) {
    println(x);
  }

  /** @see java.io.PrintStream#println(long) */
  public void prln(long x) {
    println(x);
  }

  /** @see java.io.PrintStream#println(float) */
  public void prln(float x) {
    println(x);
  }

  /** @see java.io.PrintStream#println(double) */
  public void prln(double x) {
    println(x);
  }

  /** @see java.io.PrintStream#println(char[]) */
  public void prln(char x[]) {
    println(x);
  }

  /** @see java.io.PrintStream#println(java.lang.String) */
  public void prln(String x) {
    println(x);
  }

  /** @see java.io.PrintStream#println(java.lang.Object) */
  public void prln(Object x) {
    println(x);
  }

  /** Char to pad with. */
  private char padChar = ' ';

  /** Set the value of the padChar */
  public void setPadChar(char aPadChar) {
    padChar = aPadChar;
  }

  /** Get the value of the padChar */
  public char getPadChar(){
    return padChar;
  }

  /** Print padding followed by String s. */
  public void padPr(String s, int padding) {

    for(int i=0; i<padding; i++) print(padChar);
    print(s);

  } // padPr(String,int)

} // OutErr


