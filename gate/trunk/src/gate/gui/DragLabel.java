package gate.gui;

import javax.swing.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;

public class DragLabel extends JLabel {
  public DragLabel(String s) {
    super(s);
    dndInit();
  }

  public DragLabel() {
    super();
    dndInit();
  }

  protected void dndInit(){
    this.dragSource = DragSource.getDefaultDragSource();
    this.dgListener = new DGListener();
    this.dsListener = new DSListener();

    // component, action, listener
    this.dragSource.createDefaultDragGestureRecognizer(
      this, DnDConstants.ACTION_COPY_OR_MOVE, this.dgListener );
  }
  private DragSource dragSource;
  private DragGestureListener dgListener;
  private DragSourceListener dsListener;
  private int dragAction = DnDConstants.ACTION_COPY;

  /**
   * DGListener
   * a listener that will start the drag.
   * has access to top level's dsListener and dragSource
   * @see java.awt.dnd.DragGestureListener
   * @see java.awt.dnd.DragSource
   * @see java.awt.datatransfer.StringSelection
   */
  class DGListener implements DragGestureListener {
    /**
     * Start the drag if the operation is ok.
     * uses java.awt.datatransfer.StringSelection to transfer
     * the label's data
     * @param e the event object
     */
    public void dragGestureRecognized(DragGestureEvent e) {

      // if the action is ok we go ahead
      // otherwise we punt
      System.out.println(e.getDragAction());
      if((e.getDragAction() & DragLabel.this.dragAction) == 0) return;
      System.out.println( "kicking off drag");

      // get the label's text and put it inside a Transferable
      // Transferable transferable = new StringSelection( DragLabel.this.getText() );
      Transferable transferable = null;//new StringTransferable( DragLabel.this.getText() );

      // now kick off the drag
      try {
        // initial cursor, transferrable, dsource listener
        e.startDrag(DragSource.DefaultCopyNoDrop,
        transferable,
        DragLabel.this.dsListener);
        // or if dragSource is a variable
        // dragSource.startDrag(e, DragSource.DefaultCopyDrop, transferable, dsListener);
        // or if you'd like to use a drag image if supported
        /*
        if(DragSource.isDragImageSupported() )
        // cursor, image, point, transferrable, dsource listener
        e.startDrag(DragSource.DefaultCopyDrop, image, point, transferable, dsListener);
        */
      }catch( InvalidDnDOperationException idoe ) {
  	    idoe.printStackTrace();
      }
    }
  }


  /**
   * DSListener
   * a listener that will track the state of the DnD operation
   *
   * @see java.awt.dnd.DragSourceListener
   * @see java.awt.dnd.DragSource
   * @see java.awt.datatransfer.StringSelection
   */
  class DSListener implements DragSourceListener {

    /**
     * @param e the event
     */
    public void dragDropEnd(DragSourceDropEvent e) {
      if( e.getDropSuccess() == false ) {
	      System.out.println( "not successful");
	      return;
      }

      /*
       * the dropAction should be what the drop target specified
       * in acceptDrop
       */
      System.out.println( "dragdropend action " + e.getDropAction() );

      // this is the action selected by the drop target
      if(e.getDropAction() == DnDConstants.ACTION_MOVE)
        DragLabel.this.setText("");
    }

    /**
     * @param e the event
     */
    public void dragEnter(DragSourceDragEvent e) {
      System.out.println( "draglabel enter " + e);
      DragSourceContext context = e.getDragSourceContext();
      //intersection of the users selected action, and the source and target actions
      int myaction = e.getDropAction();
      if( (myaction & DragLabel.this.dragAction) != 0) {
	      context.setCursor(DragSource.DefaultCopyDrop);
      }else{
      	context.setCursor(DragSource.DefaultCopyNoDrop);
      }
    }

    /**
     * @param e the event
     */
    public void dragOver(DragSourceDragEvent e) {
      DragSourceContext context = e.getDragSourceContext();
      int sa = context.getSourceActions();
      int ua = e.getUserAction();
      int da = e.getDropAction();
      int ta = e.getTargetActions();
      System.out.println("dl dragOver source actions" + sa);
      System.out.println("user action" + ua);
      System.out.println("drop actions" + da);
      System.out.println("target actions" + ta);
    }
    /**
     * @param e the event
     */
    public void dragExit(DragSourceEvent e) {
      System.out.println( "draglabel exit " + e);
      DragSourceContext context = e.getDragSourceContext();
    }

    /**
     * for example, press shift during drag to change to
     * a link action
     * @param e the event
     */
    public void dropActionChanged (DragSourceDragEvent e) {
      DragSourceContext context = e.getDragSourceContext();
      context.setCursor(DragSource.DefaultCopyNoDrop);
    }
  }
}



