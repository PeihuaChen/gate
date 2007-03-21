package gate.learningLightWeight.learners;

import gate.learningLightWeight.SparseFeatureVector;
import java.io.BufferedReader;
import java.io.BufferedWriter;

public abstract class SupervisedLearner {
  
  private static String learnerName;
  private String learnerExecutable;
  private String learnerParams; 
  public String commandLine;
  
  //Does use the parameter tau to adjust the numeric output of svm.  
  boolean isUseTau = true;
  
//Does use the parameter tau to adjust the numeric output of svm.  
  boolean isUseTauALLCases = true;
  
  public abstract void getParametersFromCommmand();
  public abstract void training(BufferedWriter modelFile, SparseFeatureVector [] dataLearning, 
          int totalNumFeatures, short [] classLabels, int numTraining);
  public abstract void applying(BufferedReader modelFile, DataForLearning dataLearning, int totalNumFeatures, int numClasses);
  
  public void setCommandLine(String command) {
    this.commandLine = command;
    commandLine = commandLine.replaceAll(" +", " ");
  }
  public String getCommandLine() {
    return this.commandLine;
  }
  public final String getLearnerName() {
    return learnerName;
  }
  public final void setLearnerName(String name) {
    learnerName = name;
  }
  public final String getLearnerExecutable() {
    return learnerExecutable;
  }
  public final void setLearnerExecutable(String name) {
    learnerExecutable = name;
  }
  public final String getLearnerParams() {
    return learnerParams;
  }
  public final void setLearnerParams(String name) {
    learnerParams = name;
  }
  
  public final void setUseTau(boolean choice) {
    this.isUseTau = choice;
  }
}
