package gate.learningLightWeight;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

public class Label2Id {

  public Hashtable label2Id; // from label to id, for learning

  public Hashtable id2Label; // from id to lable, for application

  public Label2Id() {
    label2Id = new Hashtable();
    id2Label = new Hashtable();
  }

  public void loadLabelAndIdFromFile(File parentDir, String filename) {

    File file1 = new File(parentDir, filename);

    if(file1.exists()) {

      try {
        BufferedReader in = new BufferedReader(new FileReader(new File(
                parentDir, filename)));

        String line;
        while((line = in.readLine()) != null) {
          line.trim();
          int p = line.indexOf(' ');
          label2Id.put(line.substring(0, p).trim(), line.substring(p + 1)
                  .trim());
          id2Label.put(line.substring(p + 1).trim(), line.substring(0, p)
                  .trim());
        }

        in.close();
      }
      catch(IOException e) {
      }
    }
    else {
      System.out.println("No label list file in initialisation phrase.");
    }
  }

  public void writeLabelAndIdToFile(File parentDir, String filename) {
    try {
      PrintWriter out = new PrintWriter(new FileWriter(new File(parentDir,
              filename)));

      List keys = new ArrayList(label2Id.keySet());
      Collections.sort(keys);
      Iterator iterator = keys.iterator();
      while(iterator.hasNext()) {
        Object key = iterator.next();
        out.println(key + " " + label2Id.get(key));
      }

      out.close();
    }
    catch(IOException e) {
    }
  }

  public void updateLabelFromDoc(String[] className) {
    int baseId = label2Id.size();
    for(int i = 0; i < className.length; ++i) {
      if(className[i] instanceof String) {
        if(!label2Id.containsKey(className[i])) {
          ++baseId;
          label2Id.put(className[i], new Integer(baseId));
          id2Label.put(new Integer(baseId), className[i]);
        }
      }
    }
  }

  public void updateMultiLabelFromDoc(String[] className) {
    int baseId = label2Id.size();
    for(int i = 0; i < className.length; ++i) {
      if(className[i] instanceof String) {
        String[] items = className[i].split(ConstantParameters.ITEMSEPARATOR);
        // System.out.println("i="+i+", className="+className[i]+"*");
        for(int j = 0; j < items.length; ++j) {
          if(items[j].endsWith(ConstantParameters.SUFFIXSTARTTOKEN))
            items[j] = items[j].substring(0, items[j]
                    .lastIndexOf(ConstantParameters.SUFFIXSTARTTOKEN));

          if(!label2Id.containsKey(items[j])) {
            // System.out.println("i="+i+"j, items[j]="+items[j]+"*");
            ++baseId;
            label2Id.put(items[j], new Integer(baseId));
            id2Label.put(new Integer(baseId), items[j]);
          }
        }
      }
    }
  }

  public void clearAllData() {
    this.label2Id.clear();
    this.id2Label.clear();
  }

}
