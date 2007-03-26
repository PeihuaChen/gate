/*
 *  ChunkLengthStats.java
 * 
 *  Yaoyong Li 22/03/2007
 *
 *  $Id: ChunkLengthStats.java, v 1.0 2007-03-22 12:58:16 +0000 yaoyong $
 */
package gate.learningLightWeight;

import gate.Annotation;
import gate.AnnotationSet;
import gate.learningLightWeight.DocFeatureVectors.LongCompactor;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/** 
 * Store the length statitics of chunks belonging to one type
 * which will be used by the post-processing procedure.
 */
public class ChunkLengthStats {
  /** Index refer to the length of chunk, and value is the 
   * number of chunks with the length in training data.
   */
  public int[] lenStats;
  /** Maixmal length of a chunk considered. */
  public final static int maxLen = 200;

  /** 
   * Constructor
   * Get an int array with the length pre-defined.
   */
  public ChunkLengthStats() {
    lenStats = new int[maxLen];
  }
  /** Read the chunk length statistics from a file specified. */
  static public void loadChunkLenStats(File parentDir, String filename,
    HashMap chunkLenHash) {
    File file1 = new File(parentDir, filename);
    if(file1.exists()) {
      try {
        BufferedReader in = new BufferedReader(new FileReader(new File(
          parentDir, filename)));
        String line;
        while((line = in.readLine()) != null) {
          line.trim();
          int label;
          int num;
          String[] items = line.split(ConstantParameters.ITEMSEPARATOR);
          label = Integer.parseInt(items[0]);
          num = Integer.parseInt(items[1]);
          ChunkLengthStats chunkLens = new ChunkLengthStats();
          for(int i = 0; i < num; ++i) {
            items = (in.readLine()).split(ConstantParameters.ITEMSEPARATOR);
            chunkLens.lenStats[Integer.parseInt(items[0])] = Integer
              .parseInt(items[1]);
          }
          chunkLenHash.put(label, chunkLens);
        }
        in.close();
      } catch(IOException e) {
      }
    } else {
      if(LogService.debug>0)
      System.out.println("No chunk length statistics list file in initialisation phrase.");
    }
  }
  /** Write the chunk length statistics into a file. */
  static public void writeChunkLensStatsIntoFile(File parentDir,
    String filename, HashMap chunkLenHash) {
    File file1 = new File(parentDir, filename);
    try {
      BufferedWriter out = new BufferedWriter(new FileWriter(new File(
        parentDir, filename)));
      ArrayList labelSet = new ArrayList(chunkLenHash.keySet());
      Collections.sort(labelSet, new LongCompactor());
      for(int i = 0; i < labelSet.size(); ++i) {
        Object obj = labelSet.get(i);
        // if( chunkLenHash.containsKey(obj)) {
        ChunkLengthStats chunkLens = (ChunkLengthStats)chunkLenHash.get(obj);
        int num = 0;
        for(int j = 0; j < ChunkLengthStats.maxLen; ++j)
          if(chunkLens.lenStats[j] > 0) ++num;
        out.append(Integer.parseInt(obj.toString())
          + ConstantParameters.ITEMSEPARATOR + num
          + ConstantParameters.ITEMSEPARATOR + "#label_and_number");
        out.newLine();
        // System.out.println("label=*"+Integer.parseInt(obj.toString())+
        // "* num="+num );
        for(int j = 0; j < ChunkLengthStats.maxLen; ++j)
          if(chunkLens.lenStats[j] > 0) {
            out.append(j + ConstantParameters.ITEMSEPARATOR
              + chunkLens.lenStats[j]);
            out.newLine();
            // System.out.println(" len=*"+j+"*
            // num="+chunkLens.lenStats[j]);
          }
        // }
      }
      out.flush();
      out.close();
    } catch(IOException e) {
    }
  }
  /** 
   * Update the chunk length statistics from the annotations according to
   * data set defintion. 
   */
  public static void updateChunkLensStats(AnnotationSet annotations,
    DataSetDefinition dsd, HashMap chunkLenHash, Label2Id label2Id) {
    AnnotationSet annsC = annotations.get(dsd.classAttribute.getType());
    String classFeat = dsd.classAttribute.getFeature();
    AnnotationSet annsI = annotations.get(dsd.instanceType);
    // For each annotation of class
    for(Object obj : annsC) {
      Annotation annC = (Annotation)obj;
      if(annC.getFeatures().get(classFeat) != null) {
        // Get the label
        String feat = annC.getFeatures().get(classFeat).toString();
        if(label2Id.label2Id.containsKey(feat)) {
          String label = label2Id.label2Id.get(feat).toString();
          int num = 0;
          // For each annotation of instance type
          for(Object objI : annsI)
            if(annC.overlaps((Annotation)objI)) ++num;
          if(num < ChunkLengthStats.maxLen) {
            //Update the chunk length statistics
            if(chunkLenHash.containsKey(label)) {
              ChunkLengthStats chunkLen = (ChunkLengthStats)chunkLenHash
                .get(label);
              chunkLen.lenStats[num] += 1;
              chunkLenHash.put(label, chunkLen);
            } else {
              ChunkLengthStats chunkLen = new ChunkLengthStats();
              chunkLen.lenStats[num] = 1;
              chunkLenHash.put(label, chunkLen);
            }
          }
        }
      }
    }
  }
}
