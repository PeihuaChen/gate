/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 07/12/2001
 *
 *  $Id$
 *
 */
package gate.print;

import java.awt.*;
import java.awt.print.*;
import javax.swing.*;
import javax.swing.text.*;

import gate.event.*;
import gate.util.*;
import gate.gui.MainFrame;


/**
 * Will scale the component so it fits on a page horizontally
 */
public class JComponentPrinter implements Pageable{

  public JComponentPrinter(JComponent component, PageFormat format){
    this.component = component;
    this.pageFormat = format;
    //find the scale factor; we will not enlarge as it would look ugly
    Rectangle componentBounds = component.getBounds(null);
    scaleFactor = Math.min(format.getImageableWidth() /componentBounds.width,
                           1);

    //calculate the pages count
    pageCount = (int)((componentBounds.height * scaleFactor +
                       pageFormat.getImageableHeight() - 1) /
                       pageFormat.getImageableHeight());
  }

  /**
   * Returns the number of pages over which the canvas
   * will be drawn.
   */
  public int getNumberOfPages() {
    return pageCount;
  }


  /**
   * Returns the PageFormat of the page specified by
   * pageIndex. The PageFormat is the same for all pages.
   *
   * @param pageIndex the zero based index of the page whose
   * PageFormat is being requested
   * @return the PageFormat describing the size and
   * orientation.
   * @exception IndexOutOfBoundsException
   * the Pageable  does not contain the requested
   * page.
   */
  public PageFormat getPageFormat(int pageIndex)
         throws IndexOutOfBoundsException {
    if (pageIndex >= pageCount) throw new IndexOutOfBoundsException();
    return pageFormat;
  }


  /**
   * Returns the <code>Printable</code> instance responsible for
   * rendering the page specified by <code>pageIndex</code>.
   *
   * @param pageIndex the zero based index of the page whose
   * Printable is being requested
   * @return the Printable that renders the page.
   * @exception IndexOutOfBoundsException
   * the Pageable does not contain the requested
   * page.
   */
  public Printable getPrintable(int pageIndex)
         throws IndexOutOfBoundsException {
    if (pageIndex >= pageCount)throw new IndexOutOfBoundsException();

    double originY = pageIndex * pageFormat.getImageableHeight() / scaleFactor;
    if(component instanceof JTextComponent){
      JTextComponent tComp = (JTextComponent)component;
      //move the origin up towards the first inter-row space
      int location = tComp.viewToModel(new Point(0, (int)originY));
      try{
         tComp.getText(location, 20)+ "\"");
        Rectangle rect = tComp.modelToView(location);
        originY = rect.y + rect.height - 1;
      }catch(BadLocationException ble){
        ble.printStackTrace(Err.getPrintWriter());
      }
    }

    return new TranslatedPrintable(originY);
  }


/**
   * This inner class's sole responsibility is to translate
   * the coordinate system before invoking a canvas's
   * painter. The coordinate system is translated in order
   * to get the desired portion of a canvas to line up with
   * the top of a page.
   */
  public class TranslatedPrintable implements Printable {
    public TranslatedPrintable(double originY){
      this.originY = originY;
    }

    /**
     * Prints the page at the specified index into the specified
     * {@link Graphics} context in the specified
     * format. A PrinterJob calls the
     * Printableinterface to request that a page be
     * rendered into the context specified by
     * graphics. The format of the page to be drawn is
     * specified by pageFormat. The zero based index
     * of the requested page is specified by pageIndex.
     * If the requested page does not exist then this method returns
     * NO_SUCH_PAGE; otherwise PAGE_EXISTS is returned.
     * The Graphics class or subclass implements the
     * {@link PrinterGraphics} interface to provide additional
     * information. If the Printable object
     * aborts the print job then it throws a {@link PrinterException}.
     * @param graphics the context into which the page is drawn
     * @param pageFormat the size and orientation of the page being drawn
     * @param pageIndex the zero based index of the page to be drawn
     * @return PAGE_EXISTS if the page is rendered successfully
     * or NO_SUCH_PAGE if pageIndex specifies a
     * non-existent page.
     * @exception java.awt.print.PrinterException
     * thrown when the print job is terminated.
     */
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
               throws PrinterException {

      Rectangle componentBounds = component.getBounds(null);
      Graphics2D g2 = (Graphics2D) graphics;
      g2.translate(pageFormat.getImageableX() - componentBounds.x,
                   pageFormat.getImageableY() - originY - componentBounds.y);
      g2.scale(scaleFactor, scaleFactor);

      if(component instanceof JTextComponent){
        JTextComponent tComp = (JTextComponent)component;
        double nextOriginY = (pageIndex + 1) * pageFormat.getImageableHeight() /
                             scaleFactor;
        int location = tComp.viewToModel(new Point(0, (int)nextOriginY));
        try{
          Rectangle rect = tComp.modelToView(location);
          nextOriginY = rect.y;
        }catch(BadLocationException ble){
          ble.printStackTrace(Err.getPrintWriter());
        }
        Rectangle clip = g2.getClip().getBounds();
        clip.setSize((int)clip.getWidth(), (int)(nextOriginY - originY) - 1);
        g2.setClip(clip);
      }

      boolean wasBuffered = component.isDoubleBuffered();
      component.paint(g2);
      component.setDoubleBuffered(wasBuffered);

      //fire the events
      StatusListener sListener = (StatusListener)MainFrame.getListeners().
                                 get("gate.event.StatusListener");
      if(sListener != null){
        sListener.statusChanged("Printing page " + (pageIndex + 1) +
                                "/" + pageCount);
      }

      return PAGE_EXISTS;
    }

    double originY;
  }


  JComponent component;
  PageFormat pageFormat;
  int pageCount;
  double scaleFactor;
}