package gate.learningLightWeight;

import java.io.File;
import java.io.FilenameFilter;

/**
 * filter out all but *.html files
 */
class SaveFilter implements FilenameFilter {
  /**
   * Select only *.html files.
   * 
   * @param dir the directory in which the file was found.
   * 
   * @param name the name of the file
   * 
   * @return true if and only if the name should be included in the file
   *         list; false otherwise.
   */
  public boolean accept(File dir, String name) {
    if(new File(dir, name).isDirectory()) {
      return false;
    }
    // name = name.toLowerCase();
    return name.endsWith(".save");
  }
}
