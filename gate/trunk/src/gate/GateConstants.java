/*
 *  GateConstants.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Cristian URSU, 8/Nov/2001
 *
 *  $Id$
 */

package gate;

/** Interface used to hold different GATE constants */
public interface GateConstants {

  /** The name of config data files (<TT>gate.xml</TT>). */
  public static final String GATE_DOT_XML = "gate.xml";

  /** The name of session state data files (<TT>gate.session</TT>). */
  public static final String GATE_DOT_SER = "gate.session";

  /** The name of the site config property (<TT>gate.config</TT>). */
  public static final String GATE_CONFIG_PROPERTY = "gate.config";

  /** The name of the annotation set storing original markups in a document */
  public static final String
    ORIGINAL_MARKUPS_ANNOT_SET_NAME = "Original markups";


  /** The look and feel option name*/
  public static final String LOOK_AND_FEEL = "Look_and_Feel";

  /** The key for the font used for text components*/
  public static final String TEXT_COMPONENTS_FONT = "Text_components_font";

  /** The key for the font used for menus*/
  public static final String MENUS_FONT = "Menus_font";

  /** The key for the font used for other GUI components*/
  public static final String OTHER_COMPONENTS_FONT = "Other_components_font";

  /** The key for the main window width*/
  public static final String MAIN_FRAME_WIDTH = "Main_frame_width";

  /** The key for the main window height*/
  public static final String MAIN_FRAME_HEIGHT = "Main_frame_height";

  /** The key for the save options on exit value*/
  public static final String SAVE_OPTIONS_ON_EXIT = "Save_options_on_exit";

  /** The key for the save session on exit value*/
  public static final String SAVE_SESSION_ON_EXIT = "Save_session_on_exit";

  /** The key for the feature keeping the original content of the document */
  public static final String
   ORIGINAL_DOCUMENT_CONTENT_FEATURE_NAME = "Original_document_content_on_load";

  /** The key for the feature keeping the repositioning information
   *  between original and displayed content of the document*/
  public static final String
    DOCUMENT_REPOSITIONING_INFO_FEATURE_NAME = "Document_repositioning_info";

} // GateConstants
