/*
 * Copyright 2000 Computing Research Labs, New Mexico State University
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
 * THE COMPUTING RESEARCH LAB OR NEW MEXICO STATE UNIVERSITY BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * $Id$
 */

package gate.demo;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.HashSet;

import guk.pcffont.*;
import guk.minput.*;
import guk.mtext.*;

import gate.gui.*;
import gate.*;
import gate.util.*;


public class GukAppletExample extends JApplet implements MUTTManagerObject {
    /**
     * @serial The virtual keyboard.
     */
    private MUTTKeyboard kbd = null;

    /**
     * @serial The text field.
     */
    private MUTTTextField tf = null;

    /**
     * @serial List of input method families.
     */
    private JList families;

    /**
     * @serial List of input method layouts.
     */
    private JList layouts;

    /**
     * @serial List of available input methods.
     */
    private MUTTInputMethodList iml = null;

    /**
     * @serial Tracks the ID of the last family chosen.
     */
    private int last_family = -1;

    /**
     * @serial Tracks the ID of the last layout chosen.
     */
    private int last_layout = -1;

    /**
     * @serial Specifies the font used to draw the text.
     */
    private PCFUnicodeFontSet font;

    /**
     * @serial Nominal/national digit toggle.
     */
    private JButton digit_button;

    private SyntaxTreeViewer tree;

    private Object loadFont(String font_file, boolean font_set) {
//        URL mimurl = null;
        InputStream is = null;
        PCFUnicodeFont font = null;
        PCFUnicodeFontSet fs = null;
//XXX
/*
        try {
            mimurl = new URL("fonts/"+font_file);
        } catch (MalformedURLException ue1) {
            try {
                mimurl = new URL(getCodeBase(), "fonts/"+font_file);
            } catch (MalformedURLException ue2) {
                showStatus("Bad font file name: " + font_file);
                Toolkit.getDefaultToolkit().beep();
                Toolkit.getDefaultToolkit().beep();
                //
                // Nothing can be done without a font for drawing.
                //
                return null;
            }
        }
*/
        //
        // Actually get the font set or font.
        //
        if (font_set)
          showStatus("Loading font set "+font_file+"...");
        else
          showStatus("Loading font "+font_file+"...");
/*
        try {
            is = mimurl.openStream();
        } catch (IOException bad_open) {
            return null;
        }
*/

//Out.println("URL:" +ClassLoader.getSystemResource("fonts/" + font_file));
        is = ClassLoader.getSystemResourceAsStream("fonts/" + font_file);
        if(is==null){
          showStatus("Bad font file name: " + font_file);
//Out.println("Bad font file name: " + font_file);
          Toolkit.getDefaultToolkit().beep();
          Toolkit.getDefaultToolkit().beep();
          //
          // Nothing can be done without a font for drawing.
          //
          return null;
        }
        try {
            if (font_set)
              fs = new PCFUnicodeFontSet(is,"fonts/");
            else
              font = new PCFUnicodeFont(is);
        } catch (IOException ioe) {
            showStatus("Unable to load the font: " + font_file);
            Toolkit.getDefaultToolkit().beep();
            Toolkit.getDefaultToolkit().beep();
            if (is != null) {
                try {
                    is.close();
                } catch (IOException bad_close1) {
                }
            }
            //
            // Return if the font cannot be loaded.
            //
            return null;
        }
        try {
            is.close();
        } catch (IOException bad_close2) {
        }

        if (font_set)
          showStatus("Loading font set "+font_file+"... Done.");
        else
          showStatus("Loading font "+font_file+"... Done.");

        return (fs != null) ? (Object) fs : (Object) font;
    }

    public void init(){
      try{
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }catch(Exception e){
      }
        gate.util.Tools.setUnicodeEnabled(true);
        URL mimurl;
        boolean unix, pc = getParameter("geometry").equals("pc");

        getContentPane().setLayout(new BoxLayout(getContentPane(),
                                                 BoxLayout.Y_AXIS));
        //create the checkboxes panel
        ButtonGroup cbg = new ButtonGroup();
        JRadioButton ch1 = new JRadioButton("Unix Keyboard", !pc);
        cbg.add(ch1);
        JRadioButton ch2 = new JRadioButton("PC Keyboard", pc);
        cbg.add(ch2);
        ch1.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED)
                  return;
                kbd.changeGeometry(MUTTKeyboard.UNIX_GEOMETRY);
            }
        });
        ch2.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED)
                  return;
                kbd.changeGeometry(MUTTKeyboard.PC_GEOMETRY);
            }
        });

        Box secondLevelBox = Box.createHorizontalBox();
        families = new JList(new DefaultListModel());
        families.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane jsp = new JScrollPane(families);
        jsp.setMaximumSize(new Dimension(150, 150));
        jsp.setPreferredSize(new Dimension(150, 150));
        secondLevelBox.add(jsp);
        secondLevelBox.add(Box.createHorizontalStrut(10));


        layouts = new JList(new DefaultListModel());
        layouts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jsp = new JScrollPane(layouts);
        jsp.setMaximumSize(new Dimension(150, 150));
        jsp.setPreferredSize(new Dimension(150, 150));
        secondLevelBox.add(jsp);
        secondLevelBox.add(Box.createHorizontalStrut(10));

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setMaximumSize(new Dimension(150, 150));
        buttonsPanel.setPreferredSize(new Dimension(150, 150));
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel,BoxLayout.Y_AXIS));
//        Box buttonsBox = Box.createVerticalBox();
        JButton b = new JButton("Reset");
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //
                // Reset command.
                //
                kbd.reset();
            }
        });
        b.setPreferredSize(new Dimension(150,30));
        b.setMaximumSize(new Dimension(150,30));
        buttonsPanel.add(b);

        digit_button = new JButton("Toggle Digits");
        digit_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String s = "Toggle";
                switch (kbd.toggleDigitForms()) {
                  case MUTTInputMethod.NOMINAL_DIGITS:
                    s = "National"; break;
                  case MUTTInputMethod.NATIONAL_DIGITS:
                    s = "Nominal"; break;
                }
                digit_button.setText(s + " Digits");
            }
        });
        digit_button.setPreferredSize(new Dimension(150,30));
        digit_button.setMaximumSize(new Dimension(150,30));
        buttonsPanel.add(digit_button);
        buttonsPanel.add(Box.createGlue());
        buttonsPanel.add(ch1);
        buttonsPanel.add(ch2);
        secondLevelBox.add(buttonsPanel);
        secondLevelBox.add(Box.createGlue());
        getContentPane().add(secondLevelBox);
        getContentPane().add(Box.createVerticalStrut(10));

        String mim = getParameter("input-method-list");
//        mimurl = null;

        //
        // Check to see if a specific input method list was specified in the
        // HTML file.  Use that if it exists.
        //
/*
        try {
            mimurl = new URL(mim);
        } catch (MalformedURLException e) {
            try {
                //
                // Insert an additional path that appears in the distribution.
                //
                mimurl = new URL(getCodeBase(), "im/" + mim);
            } catch (MalformedURLException ue) {
                showStatus("Invalid mim.dir URL.");
                Toolkit.getDefaultToolkit().beep();
                mimurl = null;
            }
        }
*/
//        mimurl = ClassLoader.getSystemResource("im/" + mim);
        String dfam = getParameter("input-method-family");
        String dlay = getParameter("input-method-layout");
        String dfile = null;

        if (mim != null) {
            //
            // Set the default input method family and layout strings to
            // something reasonable if they are not specified in the
            // parameters.
            //

            if (dfam == null || dlay == null) {
                dfam = "English";
                dlay = "ASCII";
            }

            try {
                MUTTInputMethod.setInputMethodList("im/" + mim);
                iml = MUTTInputMethod.getInputMethodList();

                //
                // Add the input methods to the lists and select the default
                // while doing so.
                //
                DefaultListModel familiesModel = (DefaultListModel)families.getModel();
                for (int j = 0; j < iml.families.length; j++) {
                    familiesModel.add(familiesModel.size(),
                                      iml.families[j].family);
                    if (iml.families[j].family.equals(dfam)) {
                        last_family = j;
                        families.setSelectedIndex(j);
                        families.ensureIndexIsVisible(j);
                        DefaultListModel layoutsModel =
                                (DefaultListModel)layouts.getModel();
                        for (int i = 0; i < iml.families[j].layouts.length;
                             i += 2) {
                            layoutsModel.add(layoutsModel.size(),
                                             iml.families[j].layouts[i]);
                            if (iml.families[j].layouts[i].equals(dlay)) {
                                last_layout = i >> 1;
                                layouts.setSelectedIndex(last_layout);
                                layouts.ensureIndexIsVisible(last_layout);
                                dfile = iml.families[j].layouts[i+1];
                            }
                        }
                    }
                }
            } catch (IOException e) {
                showStatus("Unable to open: " + mim);
                Toolkit.getDefaultToolkit().beep();
                mimurl = null;
            }
        }

        families.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                int j, which;
                which = families.getSelectedIndex();

                //
                // Erase the layout list and add the layouts for the
                // new family.
                //
                DefaultListModel layoutsModel =
                                          (DefaultListModel)layouts.getModel();
                if (layoutsModel.size() > 0) layoutsModel.clear();
                for (j = 0; j < iml.families[which].layouts.length; j += 2) {
                    layoutsModel.add(layoutsModel.size(),
                                     iml.families[which].layouts[j]);
                    if (which == last_family && j == last_layout)
                      layouts.setSelectedIndex(last_layout);
                      layouts.ensureIndexIsVisible(last_layout);
                }
            }
        });

        layouts.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                int j, which;
                String file, fam, lay;
                URL url = null;

//                if (e.getStateChange() == ItemEvent.DESELECTED)
//                  return;

//                which = ((Integer)e.getItem()).intValue();
                which = layouts.getSelectedIndex();
                //
                // Avoid loading the same input method multiple times.
                //
                if (last_family == families.getSelectedIndex() &&
                    which == last_layout)
                  return;

                last_family = families.getSelectedIndex();
                last_layout = which;

                fam = iml.families[last_family].family;
                lay = iml.families[last_family].layouts[last_layout<<1];
                file = iml.families[last_family].layouts[(last_layout<<1) + 1];

                //
                // Try to set up the URL.
                //
/*
                try {
                    url = new URL(file);
                } catch (MalformedURLException ure) {
                    try {
                        //
                        // Insert an additional path that appears in the
                        // distribution.
                        //
                        url = new URL(getCodeBase(), "im/" + file);
                    } catch (MalformedURLException ue) {
                        showStatus("Invalid URL for input method:" +
                                   fam + "-" + lay + ".");
                        Toolkit.getDefaultToolkit().beep();
                        url = null;
                    }
                }
*/
                url = ClassLoader.getSystemResource("im/" + file);
//                if (url != null) {
                    try {
                        kbd.setInputMethod(file, fam, lay, "im/" + file, true);
                        tf.setInputMethod(kbd.getInputMethod());
                    } catch (MUTTInputMethodException me) {
                        showStatus(me.toString());
                        Toolkit.getDefaultToolkit().beep();
                    }
//                }

                if (kbd.hasNationalDigits()) {
                    String s = "Toggle";
                    switch (kbd.digitForms()) {
                      case MUTTInputMethod.NATIONAL_DIGITS:
                        s = "Nominal"; break;
                      case MUTTInputMethod.NOMINAL_DIGITS:
                        s = "National"; break;
                    }
                    digit_button.setText(s + " Digits");
                    digit_button.setEnabled(true);
                } else
                  digit_button.setEnabled(false);
            }
        });

        //
        // Initialize the Unicode character data.
        //
        showStatus("Loading character data...");
        UCData.ucdata_load("fonts/", UCData.UCDATA_CTYPE|UCData.UCDATA_RECOMP);
        showStatus("Loading character data...done.");

        //
        // Load the engraving font for the keyboard.  This is hard-coded
        // at the moment.
        //
        PCFUnicodeFont efont = (PCFUnicodeFont) loadFont("5x8.pcf", false);

        //
        // Load the font for the keyboard.
        //
        String font_file;
        if ((font_file = getParameter("unicode-font")) == null)
          font_file = "basic.fst";

        font = (PCFUnicodeFontSet) loadFont(font_file, true);
//Out.println(font);
        kbd = (pc) ?
            new MUTTKeyboard(MUTTKeyboard.PC_GEOMETRY, getSize(),
                             efont, font, null, Color.blue) :
            new MUTTKeyboard(MUTTKeyboard.UNIX_GEOMETRY, getSize(),
                             efont, font, null, Color.blue);

        mimurl = null;
        if (dfile != null) {
            //
            // Have the keyboard load the default input method at this point.
            //
/*
            try {
                mimurl = new URL(dfile);
            } catch (MalformedURLException e) {
                try {
                    //
                    // Insert an additional path that appears in the
                    // distribution.
                    //
                    mimurl = new URL(getCodeBase(), "im/" + dfile);
                } catch (MalformedURLException ue) {
                    showStatus("Invalid URL for input method: " +
                               dfam + "-" + dlay + ".");
                    Toolkit.getDefaultToolkit().beep();
                    mimurl = null;
                }
            }
*/
        }
//        mimurl = ClassLoader.getSystemResource("im/" + dfile);
//        if (mimurl != null) {
            //
            // Construct the URL that points to the input method.
            //
            try {
                kbd.setInputMethod(dfile, dfam, dlay, "im/" + dfile, false);
            } catch (MUTTInputMethodException e) {
                showStatus(e.toString());
                Toolkit.getDefaultToolkit().beep();
            }
//        }

        //
        // Initialize the label on the digit toggle.
        //
        if (kbd.hasNationalDigits()) {
            String s = "Toggle";
            switch (kbd.digitForms()) {
              case MUTTInputMethod.NATIONAL_DIGITS: s = "Nominal"; break;
              case MUTTInputMethod.NOMINAL_DIGITS: s = "National"; break;
            }
            digit_button.setText(s + " Digits");
            digit_button.setEnabled(true);
        } else
          digit_button.setEnabled(false);
        Box kbdBox = Box.createHorizontalBox();
        kbdBox.add(kbd);
        kbdBox.add(Box.createGlue());
        getContentPane().add(kbdBox);
        getContentPane().add(Box.createVerticalStrut(10));

        Box tfBox = Box.createHorizontalBox();
        tf = new MUTTTextField(font, null, this, 40, null);
        tfBox.add(tf);
        tfBox.add(Box.createHorizontalStrut(5));
        kbd.setClient(tf.getClient());
        Box btnBox = Box.createVerticalBox();
        b = new JButton("Erase");
        b.setPreferredSize(new Dimension(100,30));
        b.setMaximumSize(new Dimension(100,30));
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tf.erase();
            }
        });
        btnBox.add(b);
        b = new JButton("Transfer");
        b.setPreferredSize(new Dimension(100,30));
        b.setMaximumSize(new Dimension(100,30));
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                transferData();
            }
        });
        btnBox.add(b);
        tfBox.add(btnBox);
        tfBox.add(Box.createHorizontalGlue());
        getContentPane().add(tfBox);
        //create the tree viewer and add it
        tree = new SyntaxTreeViewer("SyntaxTreeNode");
        tree.setPreferredSize(new Dimension(getWidth() - 30,250));
        JScrollPane scroller = new JScrollPane(tree);
//        scroller.setPreferredSize(new Dimension(getWidth(),300));
        getContentPane().add(scroller);
    }

    protected void transferData(){
        String text = tf.getText();
//        String text = "\u0915\u0932\u094d\u0907\u0928\u0643\u0637\u0628 \u041a\u0430\u043b\u0438\u043d\u0430 Kalina";
        Document doc = null;
        try{

          doc = Factory.newDocument(text);

          FeatureMap attrs = Factory.newFeatureMap();
          attrs.put("time", new Long(0));

          attrs.put("text", doc.getContent().toString());

          doc.getAnnotations().add( new Long(0), new Long(doc.getContent().toString().length()),
                                  "utterance", attrs);

        }catch(IOException ioe){

          ioe.printStackTrace();

        }catch(InvalidOffsetException ioe2){

          ioe2.printStackTrace();

        }

        HashSet set = new HashSet();

        set.add("utterance");

        set.add("SyntaxTreeNode");

        AnnotationSet annots = doc.getAnnotations().get(set);

        tree.setTreeAnnotations(annots);

    }

    //
    // For other applets in the same AppletContext, this class must implement
    // the MUTTManagerObject interface to allow other applets to use the
    // input method facilities available from the central MUTTKeyboard object.
    //

    /**
     * Implements the MUTTManagerObject interface.
     */
    public boolean filterKeyEvent(KeyEvent k) throws MUTTInputMethodException {
        return kbd.filterKeyEvent(k);
    }

    /**
     * Implements the MUTTManagerObject interface.
     */
    public MUTTInputMethodClient setClient(MUTTInputMethodClient client) {
        return kbd.setClient(client);
    }

    /**
     * Implements the MUTTManagerObject interface.
     */
    public MUTTInputMethod getInputMethod() {
        return kbd.getInputMethod();
    }
}
