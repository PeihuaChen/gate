/*
	Scratch.java

	Hamish Cunningham, 22/03/00

	$Id$
*/


package gate.util;

import java.util.*;
import java.awt.datatransfer.*;

import gate.*;
import gate.jape.*;
import org.w3c.www.mime.*;
/*
import java.io.IOException;
import java.net.URL;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.FileWriter;
import org.w3c.tidy.Tidy;
*/

import java.io.*;

import javax.swing.text.rtf.*;
import javax.swing.text.*;

/**
  * A scratch pad for experimenting.
  */
public class Scratch
{


  public static void main(String args[]) {
    Scratch app = new Scratch();

    app.loadDoc("d:/tmp/clangref.rtf");

  } // main

  public void loadDoc(String rtfDocName){
    RTFEditorKit kit = new RTFEditorKit();
    DefaultStyledDocument doc = new DefaultStyledDocument();
    try{
      FileInputStream file = new FileInputStream(rtfDocName);
      kit.read(file,doc,0);
      System.out.println("**************************************************");
      System.out.println(doc.getText(0,doc.getLength()));
    } catch (Exception e){
      e.printStackTrace(System.err);
    }
    
  }

} // class Scratch


