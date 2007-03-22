package gate.learningLightWeight.learners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import gate.learningLightWeight.ChunkLengthStats;
import gate.learningLightWeight.LabelsOfFV;
import gate.learningLightWeight.LabelsOfFeatureVectorDoc;

public class PostProcessing {
  
  double boundaryProb = 0.42;
  double entityProb = 0.2;
  
  double thresholdC = 0.5;
  
  public PostProcessing(float boundaryP, float entityP, float thresholdClassificaton) {
    this.boundaryProb = boundaryP;
    this.entityProb = entityP;
    this.thresholdC = thresholdClassificaton;
  }
  public PostProcessing() {
  }
  
  
  public void postProcessingChunk(short stage, LabelsOfFV [] multiLabels, 
          int numClasses, HashSet chunks, HashMap chunkLenHash) {
    int num = multiLabels.length;
    HashMap tempChunks = new HashMap();
    for(int j=0; j<numClasses; j+=2) { //for start and end token
      ChunkLengthStats chunkLen;
      String labelS = new Integer(j/2+1).toString();
      if(chunkLenHash.get(labelS)!= null)
        chunkLen = (ChunkLengthStats)chunkLenHash.get(labelS);
      else chunkLen = new ChunkLengthStats();
      for(int i=0; i<num; ++i) {
        if(multiLabels[i].probs[j]>boundaryProb) {
          for(int i1=i; i1<num; ++i1)
            //Use the boundary probability and the length of chunk statistics 
            if(multiLabels[i1].probs[j+1]>boundaryProb && i1-i+1<ChunkLengthStats.maxLen && chunkLen.lenStats[i1-i+1]>0) {
            //if(multiLabels[i1].probs[j+1]>boundaryProb) {
              ChunkOrEntity chunk = new ChunkOrEntity(i, i1);
              chunk.prob = multiLabels[i].probs[j]*multiLabels[i1].probs[j+1];
              chunk.name = j/2+1;
              tempChunks.put(chunk, "1");
              //System.out.println("class="+j +" i="+i+" i1="+i1+" prob="+chunk.prob+ " name="+chunk.name);
              //chunks.add(chunk);
              break;
            }
        }
      }//End of loop for each instance (i)
    }
    
    //Solve the overlap case so that every entity has just one label
    System.out.println("*** numberinTempChunks="+tempChunks.size());
    HashMap mapChunks = new HashMap();
    for(Object obj:tempChunks.keySet()) {
      ChunkOrEntity entity = (ChunkOrEntity)obj;
      mapChunks.put(entity.start+"_"+entity.end+"_"+entity.name, entity);
    }
    List indexes = new ArrayList(mapChunks.keySet()); 
    //LongCompactor c = new LongCompactor();
    Collections.sort(indexes);
    for(int i1=0; i1<indexes.size(); ++i1) {
    //for(Object ob1:tempChunks.keySet() ) {
      Object ob1 = mapChunks.get(indexes.get(i1));
      if(tempChunks.get(ob1).toString().equals("1")) {
        ChunkOrEntity chunk1 = (ChunkOrEntity)ob1;
        for(int j1=i1+1; j1<indexes.size(); ++j1) {
          Object ob2 = mapChunks.get(indexes.get(j1));
        //for(Object ob2:tempChunks.keySet()) {
          if(tempChunks.get(ob2).toString().equals("1")) {
            ChunkOrEntity chunk2 = (ChunkOrEntity)ob2;
            if(chunk2.start!=chunk1.start || chunk2.end!=chunk1.end || chunk2.name!= chunk1.name) {
              //if the two entities overlap
              if((chunk1.start>=chunk2.start && chunk1.start<=chunk2.end) ||
                      (chunk1.end<=chunk2.end && chunk1.end>=chunk2.start)) {
                if(chunk1.prob>chunk2.prob)
                  tempChunks.put(ob2, "0");
                else {
                  tempChunks.put(ob1, "0");
                  break; //break the inner loop (ob2)
                }
              }
            }
          }
        }//end of the inner loop
      }
    }//end of the outer loop
    
    //System.out.println("numberinTempChunks="+tempChunks.size());
    for(Object ob1:tempChunks.keySet() ) {
      //System.out.println("tempChunks key.start="+((ChunkOrEntity)ob1).start+ " end="+((ChunkOrEntity)ob1).end+" value="+tempChunks.get(ob1).toString());
      if(tempChunks.get(ob1).toString().equals("1"))
        chunks.add(ob1);
    }
    //System.out.println("numberinChunks="+chunks.size());
  }
  
  public void postProcessingClassification(short stage, LabelsOfFV [] multiLabels, 
          int [] selectedLabels, float [] valueLabels) {
    int num = multiLabels.length;
    float maxValue;
    int maxLabel;
    for(int i=0; i<num; ++i) { //for each instance
      maxValue = (float)thresholdC;
      maxLabel = -1;
      for(int j=0; j<multiLabels[i].num; ++j) { //for each class
        if(multiLabels[i].probs[j]>maxValue ) {
          maxValue = multiLabels[i].probs[j];
          maxLabel = j;
        }
        selectedLabels[i] = maxLabel;
        valueLabels[i] = maxValue;
    //System.out.println("numberinChunks="+chunks.size());
      }
    }
  
  }
  
  //For the training data with null label
  public void voteForOneVSAnotherNull(DataForLearning dataFVinDoc, int numClasses) {
    
    for(int i=0; i<dataFVinDoc.getNumTrainingDocs(); ++i) { //for each document
      LabelsOfFeatureVectorDoc labelFVsDoc = dataFVinDoc.labelsFVDoc[i];
      for(int j=0; j<labelFVsDoc.multiLabels.length; ++j) { //for each instance
        LabelsOfFV multiLabel0 = dataFVinDoc.labelsFVDoc[i].multiLabels[j];
        int [] voteResults = new int[numClasses];
        int voteNull;
        int kk=0;
        //for the null
        voteNull = 0;
        for(int j1=0; j1<numClasses; ++j1) {
          if(multiLabel0.probs[kk]>thresholdC)
            voteResults[j1]++;
          else voteNull++;
          ++kk;
        }
        //for other  label
        for(int i1=0; i1<numClasses; ++i1)
          for(int j1=i1+1; j1<numClasses; ++j1) {
            if(multiLabel0.probs[kk]>thresholdC)
              voteResults[i1]++;
            else voteResults[j1]++;
            ++kk;
          }
        
        //Convert the vote results into label
        int maxVote = voteNull;
        kk=-1;
        for(int i1=0; i1<numClasses; ++i1)
          if(maxVote<voteResults[i1]) {
            maxVote = voteResults[i1];
            kk = i1;
          }
        LabelsOfFV multiLabel = new LabelsOfFV(numClasses);
        multiLabel.probs = new float[multiLabel.num];
        if(kk>=0)
          multiLabel.probs[kk] = (float)1.0;
        /*int totalNum=0;
        for(int i1=0; i1<numClasses; ++i1)
          totalNum += voteResults[i1];
        for(int i1=0; i1<numClasses; ++i1)
          multiLabel.probs[i1] = voteResults[i1]/(float)totalNum;*/
        
        dataFVinDoc.labelsFVDoc[i].multiLabels[j] = multiLabel;
        
      }
    }
        
  }
  //For the data without null label
  public void voteForOneVSAnother(DataForLearning dataFVinDoc, int numClasses) {
    
    for(int i=0; i<dataFVinDoc.getNumTrainingDocs(); ++i) { //for each document
      LabelsOfFeatureVectorDoc labelFVsDoc = dataFVinDoc.labelsFVDoc[i];
      for(int j=0; j<labelFVsDoc.multiLabels.length; ++j) { //for each instance
        LabelsOfFV multiLabel0 = dataFVinDoc.labelsFVDoc[i].multiLabels[j];
        int [] voteResults = new int[numClasses];
        int kk=0;
        //for other  label
        for(int i1=0; i1<numClasses; ++i1)
          for(int j1=i1+1; j1<numClasses; ++j1) {
            if(multiLabel0.probs[kk]>thresholdC)
              voteResults[i1]++;
            else voteResults[j1]++;
            ++kk;
          }
        
        //Convert the vote results into label
        int maxVote = -1;
        kk=-1;
        for(int i1=0; i1<numClasses; ++i1)
          if(maxVote<voteResults[i1]) {
            maxVote = voteResults[i1];
            kk = i1;
          }
        LabelsOfFV multiLabel = new LabelsOfFV(numClasses);
        multiLabel.probs = new float[multiLabel.num];
        if(kk>=0)
          multiLabel.probs[kk] = (float)1.0;
        /*int totalNum=0;
        for(int i1=0; i1<numClasses; ++i1)
          totalNum += voteResults[i1];
        for(int i1=0; i1<numClasses; ++i1)
          multiLabel.probs[i1] = voteResults[i1]/(float)totalNum;*/
        
        dataFVinDoc.labelsFVDoc[i].multiLabels[j] = multiLabel;
        
      }
    }
        
  }
}
