package gate.compound.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.*;
import javax.swing.*;
import gate.*;
import gate.compound.CompoundDocument;
import gate.compound.CompoundDocumentEvent;
import gate.compound.CompoundDocumentListener;
import gate.corpora.DocumentImpl;
import gate.creole.*;
import gate.event.ProgressListener;
import gate.gui.ActionsPublisher;
import gate.gui.Handle;
import gate.gui.MainFrame;
import gate.gui.NameBearerHandle;
import java.io.*;

/**
 * This is an extention of the GATE Document viewer/editor. This class provides
 * the implementation for CompoundDocument Editor. Compound document is a set of
 * multiple documents. this class simply wrapps all document editors for all
 * compound document's member documents under a single component.
 */

public class CompoundDocumentEditor extends AbstractVisualResource implements
		ActionsPublisher, ProgressListener, CompoundDocumentListener {

	private static final long serialVersionUID = -7623216613025540025L;

	private JTabbedPane tabbedPane;

	private HashMap<String, JComponent> documentsMap;

	/**
	 * The document view is just an empty shell. This method publishes the
	 * actions from the contained views.
	 */
	public List getActions() {
		List actions = new ArrayList();
		actions.add(new SaveAllDocuments());
		actions.add(new SwitchDocument());
		return actions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gate.Resource#init()
	 */
	public Resource init() throws ResourceInstantiationException {
		tabbedPane = new JTabbedPane();
		documentsMap = new HashMap<String, JComponent>();
		this.setLayout(new java.awt.BorderLayout());
		this.add(tabbedPane, java.awt.BorderLayout.CENTER);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gate.VisualResource#setTarget(java.lang.Object)
	 */
	public void setTarget(Object target) {
		this.document = (Document) target;
	}

	/**
	 * Used by the main GUI to tell this VR what handle created it. The VRs can
	 * use this information e.g. to add items to the popup for the resource.
	 */
	public void setHandle(Handle handle) {
		super.setHandle(handle);
		Map documents = ((CompoundDocument) this.document).getDocuments();
		((CompoundDocument) this.document).addCompoundDocumentListener(this);

		Iterator iter = documents.values().iterator();
		try {
			while (iter.hasNext()) {
				Document doc = (Document) iter.next();
				NameBearerHandle nbHandle = new NameBearerHandle(doc, Main
						.getMainFrame());
				JComponent largeView = nbHandle.getLargeView();
				if (largeView != null) {
					tabbedPane.addTab(nbHandle.getTitle(), nbHandle.getIcon(),
							largeView, nbHandle.getTooltipText());
					documentsMap.put(doc.getName(), largeView);

				}
				ResourceData rd = (ResourceData) Gate.getCreoleRegister().get(
						doc.getClass().getName());
				if (rd != null)
					rd.removeInstantiation(doc);
				Gate.setHiddenAttribute(doc.getFeatures(), false);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	class SaveAllDocuments extends AbstractAction {

		private static final long serialVersionUID = -1377052643002026640L;

		public SaveAllDocuments() {
			super("Save All Documents As XML!");
		}

		public void actionPerformed(ActionEvent ae) {
			CompoundDocument cd = (CompoundDocument) document;
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			try {
				fileChooser.showSaveDialog(Main.getMainFrame());
				File dir = null;
				if ((dir = fileChooser.getSelectedFile()) == null) {
					return;
				}

				List<String> docIDs = cd.getDocumentIDs();
				for (int i = 0; i < docIDs.size(); i++) {
					Document doc = cd.getDocument(docIDs.get(i));
					File file = new File(doc.getSourceUrl().getFile());
					file = new File(dir.getAbsolutePath() + "/"
							+ file.getName());
					BufferedWriter bw = new BufferedWriter(
							new OutputStreamWriter(new FileOutputStream(file),
									((DocumentImpl) doc).getEncoding()));
					bw.write(doc.toXml());
					bw.flush();
					bw.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	class SwitchDocument extends AbstractAction {

		private static final long serialVersionUID = -1377052643002026640L;

		public SwitchDocument() {
			super("Switch Document");
		}

		public void actionPerformed(ActionEvent ae) {
			CompoundDocument cd = (CompoundDocument) document;
			List<String> docIDs = cd.getDocumentIDs();
			JComboBox box = new JComboBox(docIDs.toArray());
			Object[] options = { "OK", "CANCEL" };
			int reply = JOptionPane.showOptionDialog(MainFrame.getInstance(),
					box,
					"Select the document ID to switch to...",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
					null, options, options[0]);
			if(reply == JOptionPane.OK_OPTION) {
				String documentID = (String) box.getSelectedItem();
				((CompoundDocument) document).setCurrentDocument(documentID);
			}
		}
	}

	protected Document document;

	public void processFinished() {
		((CompoundDocument) this.document).setCurrentDocument("null");
	}

	public void progressChanged(int prgress) {

	}

	public void documentAdded(CompoundDocumentEvent event) {
		try {
			Document doc = event.getSource().getDocument(event.getDocumentID());
			NameBearerHandle nbHandle = new NameBearerHandle(doc, Main
					.getMainFrame());
			JComponent largeView = nbHandle.getLargeView();
			if (largeView != null) {
				tabbedPane.addTab(nbHandle.getTitle(), nbHandle.getIcon(),
						largeView, nbHandle.getTooltipText());
				documentsMap.put(doc.getName(), largeView);
			}
			ResourceData rd = (ResourceData) Gate.getCreoleRegister().get(
					doc.getClass().getName());
			if (rd != null)
				rd.removeInstantiation(doc);
			Gate.setHiddenAttribute(doc.getFeatures(), false);
			tabbedPane.updateUI();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void documentRemoved(CompoundDocumentEvent event) {
		Component cmp = (Component) documentsMap.get(event.getDocumentID());
		if (cmp != null) {
			tabbedPane.remove(cmp);
			tabbedPane.updateUI();
			Factory.deleteResource(event.getSource().getDocument(
					event.getDocumentID()));
		}
	}

}