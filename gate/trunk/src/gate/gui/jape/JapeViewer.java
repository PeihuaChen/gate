package gate.gui.jape;

import java.io.*;
import javax.swing.*;
import java.net.*;
import java.awt.*;

import gate.*;
import gate.creole.*;
import gate.event.ProgressListener;
import gate.util.GateRuntimeException;

/**
 * @author niraj
 * @version 1.1
 */

public class JapeViewer extends AbstractVisualResource implements
		ANNIEConstants, ProgressListener {

	public JapeViewer() {
	}

	// GUI components
	/** The text display. */
	protected JTextArea textArea;

	/** Scroller used for the text diaplay */
	protected JScrollPane textScroll;

	/** The toolbar displayed on the top part of the component */
	protected JToolBar toolbar;

	/** Should this component bahave as an editor as well as an viewer */
	private boolean editable = false;

	/** A Button for saving the contents in a Jape file */
	private JButton saveButton;

	/** A Button for reverting contents */
	private JButton revertButton;

	/** A field that holds the jape file name */
	private URL japeFileURL;

	/** a field that holds the jape file contents */
	private String japeFileContents;

	/** Transducer */
	private Transducer transducer;

	/** An Init method */
	public Resource init() {
		initGuiComponents();
		japeFileContents = new String();
		return this;
	}

	private void initGuiComponents() {
		setLayout(new BorderLayout());
		textArea = new JTextArea();
		textArea.setEditable(editable);
		textScroll = new JScrollPane(textArea,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(textScroll, BorderLayout.CENTER);
	}

	public void setTarget(Object target) {
		if (!(target instanceof Transducer)) {
			throw new IllegalArgumentException(
					"The GATE jape editor can only be used with a GATE jape transducer!\n"
							+ target.getClass().toString()
							+ " is not a GATE Jape Transducer!");
		}
    
    if(transducer != null) {
      transducer.removeProgressListener(this);
    }

		this.transducer = (Transducer) target;
		// Transducer inst =
		// (Transducer)((Gate.getCreoleRegister().getPrInstances("gate.creole.Transducer")).get(0));
		japeFileURL = transducer.getGrammarURL();
		// reading japeFile
		readJAPEFileContents();
		((Transducer)target).addProgressListener(this);
	}

	private void readJAPEFileContents() {
		try {
			if (japeFileURL != null) {
        Reader japeReader = null;
        if(transducer.getEncoding() == null) {
          japeReader = new InputStreamReader(japeFileURL.openStream());
        }
        else {
          japeReader = new InputStreamReader(japeFileURL.openStream(),
                  transducer.getEncoding());
        }
				BufferedReader br = new BufferedReader(japeReader);
				String content = br.readLine();
				japeFileContents = "";
				while (content != null) {
					japeFileContents += content + "\n";
					content = br.readLine();
				}
				textArea.setEditable(true);
				textArea.setText(japeFileContents);
				textArea.updateUI();
				textArea.setEditable(false);
				br.close();
			} else {
				textArea
						.setText("The JAPE Transducer Object was loaded from a serialised tranducer and therefore cannot show any text!");
			}
		} catch (IOException ioe) {
			throw new GateRuntimeException(ioe);
		}
	}

	public void processFinished() {
		readJAPEFileContents();
	}

	public void progressChanged(int progress) {

	}
}