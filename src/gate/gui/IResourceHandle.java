package gate.gui;


import javax.swing.*;
import java.util.*;

import gate.*;

/**
 * Interface for classes used to store the information about an open resource.
 * Such information will include icon to be used for tree components,
 * popup menu for right click events, etc.
 */
public interface IResourceHandle {

  public Icon getIcon();

  public String getTitle();

  /**
   * Returns a GUI component to be used as a small viewer/editor, e.g. below
   * the main tree in the Gate GUI for the selected resource
   */
  public JComponent getSmallView();

  /**
   * Returns the large view for this resource. This view will go into the main
   * display area.
   */
  public JComponent getLargeView();

  public JPopupMenu getPopup();

  public boolean isShown();

  public String getTooltipText();

  public Resource getResource();
}