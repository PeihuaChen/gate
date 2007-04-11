package gate.creole.ontology.ocat;

import gate.creole.ontology.OClass;
import gate.gui.MainFrame;
import gate.util.GateRuntimeException;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.text.DefaultHighlighter;

import com.ontotext.gate.vr.ClassNode;

/**
 * This class looks up in the text for possible strings that can be
 * added as instances to the ontology. It highlights such strings and
 * allows users to add new instances.
 * 
 * @author niraj
 * 
 */
public class InstanceLookupAction extends AbstractAction {

  private OntologyTreePanel ontoTreePanel;

  protected int[] offsets;

  protected ArrayList<String> stringValues;

  protected ArrayList<Object> highlights;

  /**
   * Constructor
   */
  public InstanceLookupAction(OntologyTreePanel ontoTreePanel) {
    this.ontoTreePanel = ontoTreePanel;
    stringValues = new ArrayList<String>();
    highlights = new ArrayList<Object>();
    offsets = new int[0];
  }

  public void actionPerformed(ActionEvent ae) {

    if(!ontoTreePanel.instances.isSelected()) {
      // lets remove all highlights
      for(Object highlight : highlights) {
          ontoTreePanel.ontoTreeListener.highlighter.removeHighlight(highlight);
      }
      highlights.clear();
      stringValues.clear();
      //ontoTreePanel.ontoViewer.documentTextArea.setEditable(true);
      ontoTreePanel.ontoViewer.documentTextArea.requestFocus(true);
      return;
    }

    if(ontoTreePanel.currentPropValuesAndInstances2ClassesMap == null) {
      JOptionPane.showMessageDialog(MainFrame.getInstance(), "Please load ontology and then try to use this feature.");
      ontoTreePanel.instances.setSelected(false);
      return;
    }
        
    // in this mode we disable document editing
    //ontoTreePanel.ontoViewer.documentTextArea.setEditable(false);
    

    Iterator<String> stringValIter = ontoTreePanel.currentPropValuesAndInstances2ClassesMap
            .keySet().iterator();
    String documentContent = ontoTreePanel.ontoViewer.getDocument()
            .getContent().toString().toLowerCase();
    long contentLength = documentContent.length();
    ArrayList<Integer> offsets = new ArrayList<Integer>();

    while(stringValIter.hasNext()) {
      String aValue = stringValIter.next();
      ClassNode aNode = ontoTreePanel.getNode(aValue);
      if(aNode != null && aNode.getSource() instanceof OClass)
        continue;
      
      int index = documentContent.indexOf(aValue.toLowerCase());
      while(index > -1) {
        if(index > 0
                && Character.isLetterOrDigit(documentContent.charAt(index - 1))) {
          index = documentContent.indexOf(aValue.toLowerCase(), index + 1);
          continue;
        }

        int end = index + aValue.length();

        if(end < contentLength
                && Character.isLetterOrDigit(documentContent.charAt(end))) {
          index = documentContent.indexOf(aValue.toLowerCase(), index + 1);
          continue;
        }

        stringValues.add(aValue);
        offsets.add(new Integer(index));
        offsets.add(new Integer(end));

        // we also need to add a highlight to it
        try {
          Color color = ontoTreePanel.getColor("instance*highlights*");
          Object tag = ontoTreePanel.ontoTreeListener.highlighter.addHighlight(
                  index, end, new DefaultHighlighter.DefaultHighlightPainter(
                          color));
          highlights.add(tag);
        }
        catch(javax.swing.text.BadLocationException e) {
          throw new GateRuntimeException(e);
        }

        index = documentContent.indexOf(aValue.toLowerCase(), index + 1);
      }
    }

    this.offsets = new int[offsets.size()];
    int i = 0;
    for(Integer offset : offsets) {
      this.offsets[i] = offset.intValue();
      i++;
    }
    offsets = null;
    ontoTreePanel.ontoViewer.documentTextArea.requestFocus(true);
  }

  public void updateOffsets(int insertOffset, int increment) {
    
  }
  
  /**
   * Given an offset this method returns the instance value that can be
   * looked up in currentPVnInstClassesMap to obtain the classes.
   * 
   * @param offset
   * @return
   */
  public String lookupValue(int offset) {
    // here we search within the offsets
    for(int i = 0; i < offsets.length - 1; i += 2) {
      if(offset >= offsets[i] && offset <= offsets[i + 1]) {
        return stringValues.get((int)i / 2);
      }
    }
    return null;
  }

  public Object getHighlight(int offset) {
    // here we search within the offsets
    for(int i = 0; i < offsets.length - 1; i += 2) {
      if(offset >= offsets[i] && offset <= offsets[i + 1]) {
        return highlights.get((int)i / 2);
      }
    }
    return null;
  }
  
  /**
   * Given an offset, this method returns the highlight boundaries
   * 
   * @param offset
   * @return
   */
  public int[] getOffsets(int offset) {
    for(int i = 0; i < offsets.length - 1; i += 2) {
      if(offset >= offsets[i] && offset <= offsets[i + 1]) {
        return new int[] {offsets[i], offsets[i + 1]};
      }
    }
    return null;
  }

  /**
   * Given a string value, this method returns the offsets for the
   * highlight.
   * 
   * @param value
   * @return
   */
  public int[] getOffsets(String value) {
    int index = stringValues.indexOf(value);
    if(index < 0) return null;

    return new int[] {offsets[index * 2], offsets[index * 2 + 1]};
  }
  
  
}
