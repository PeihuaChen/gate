package gate.learningLightWeight.learners;

import gate.learningLightWeight.SparseFeatureVector;
import gate.learningLightWeight.learners.libSVM.svm_parameter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SvmForExec extends SupervisedLearner{
  /** The uneven margins parameter. */
  private float tau = (float)0.4;
  
  /** The data file for svm learning.*/
  private String svmDat;
  /** The model file for svm learning.*/
  private String modelSVMFile;
  
  /** The kernel type. */
  private int kernelType =0; //linear kernel as default value 
  /** kernel_type */
  public static final int LINEAR = 0;
  public static final int POLY = 1;
  public static final int RBF = 2;
  public static final int SIGMOID = 3;
  public static final int PRECOMPUTED = 4;
  
  /**Parameters in the kernel function.*/
  int paramD = 3;
  double paramG = 1.0;
  double paramR = 0.0;
  
  /**Class constructor without parameter.*/
  public SvmForExec() {
  }
  /**Class constructor with tau parameter.*/
  public SvmForExec(float t) {
    this.tau = t;
  }
  
  /**Class constructor with tau parameter.*/
  public SvmForExec(float t, String command) {
    this.tau = t;
    this.commandLine = command;
  }
 
  /** Get the parameters from the command line.*/
  public void getParametersFromCommmand() {
    if(commandLine == null) {
      System.out.println("Error: no command yet!!");
    } else {
      double coef1=1.0;
      String [] items = commandLine.split(" ");
      int len = items.length;
      modelSVMFile = items[len-1];
      svmDat = items[len-2];
      kernelType = 0;
      for(int i=0; i<len-2; ++i) {
        if(items[i].equals("-t") && i+1<len-2) {
          kernelType = new Integer(items[i+1]).intValue();
        }
        if(items[i].equals("-d") && i+1<len-2) {
          this.paramD = new Integer(items[i+1]).intValue();
        }
        if(items[i].equals("-g") && i+1<len-2) {
          this.paramG = new Double(items[i+1]).doubleValue();
        }
        if(items[i].equals("-r") && i+1<len-2) {
          this.paramR = new Double(items[i+1]).doubleValue();
        }
        if(items[i].equals("-s") && i+1<len-2) {
          coef1 = new Double(items[i+1]).doubleValue();
        }
        if(items[i].equals("-tau") && i+1<len-2) {
          this.tau = new Float(items[i+1]).floatValue();
        }
      }
      if(coef1!=0)
        this.paramR /= coef1;
    }
        
  }
  
  public void training(BufferedWriter modelFile, SparseFeatureVector [] dataLearning, 
          int totalNumFeatures, short [] classLabels, int numTraining) {
    try {
      //Write the data into the data file for svm learning
      BufferedWriter svmDataBuff = new BufferedWriter(new FileWriter(new File(svmDat)));
      for(int i=0; i<numTraining; ++i) {
        if(classLabels[i]>0)
          svmDataBuff.append("1");
        else 
          svmDataBuff.append("-1");
        int [] indexes = dataLearning[i].getIndexes();
        float [] values = dataLearning[i].getValues();
        for(int j=0; j<dataLearning[i].getLen(); ++j)
          svmDataBuff.append(" "+indexes[j]+":"+values[j]);
        svmDataBuff.append("\n");
      }
      svmDataBuff.close();
      
      //Execute the command for the SVM  learning
      //Get the command line for the svm_learn, by getting rid of the tau parameter
      String commandLineSVM = obtainSVMCommandline(commandLine);
      //Run the external svm learn exectuable
      runExternalCommand(commandLineSVM);
      
      //Read the model from the svm results file and write it into our model file
      writeSVMModelIntoFile(modelSVMFile, kernelType, modelFile, totalNumFeatures);
      
      
    } catch(IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }
  
  /** Get the command line for the svm_learn, by getting rid of the tau parameter.*/
  public static String obtainSVMCommandline(String commandLine) {
    StringBuffer commandSVM = new StringBuffer();
    String [] items = commandLine.split("[ \t]+");
    int len=0;
    commandSVM.append(items[len++]);
    while(len<items.length) {
      if(items[len].equalsIgnoreCase("-tau")) {
        ++len;
      } else {
        commandSVM.append(" "+items[len]);
      }
      ++len;
    }
    
    return commandSVM.toString();
  }
  
  public static void writeSVMModelIntoFile(String modelSVMFile, int kernelType, 
          BufferedWriter modelFile, int totalNumFeatures) {
    //Open the svm model file
    BufferedReader svmModelBuff;
    try {
      svmModelBuff = new BufferedReader(new FileReader(new File(modelSVMFile)));
      String line;
      int numSV;
      line = svmModelBuff.readLine();
      while(!line.contains("# number of support vectors plus 1")) {
        line = svmModelBuff.readLine();
      }
      numSV = new Integer(line.substring(0, line.indexOf(" "))).intValue();
      numSV -= 1; //since it's number of SVs plus 1 in svm_light
      line = svmModelBuff.readLine();
      while(!line.contains("# threshold b,")) {
        line = svmModelBuff.readLine();
      }
      float b = new Float(line.substring(0, line.indexOf(" "))).floatValue();
      b = -b; //since the b in the svm model file is -b
      //Convert into primal form, if it is linear kernel
      if(kernelType == 0) {
        //Define the sparse vectors used for SVs
        SparseFeatureVector [] svFVs = new  SparseFeatureVector[numSV];
        double [] alphas = new double[numSV];
        SvmLibSVM.readOneSVMModel(svmModelBuff, numSV, svFVs, alphas);
        //Convert the dual form into primal form
        float [] w = new float[totalNumFeatures];
        for(int i=0; i<numSV; ++i) {
          int [] indexes = svFVs[i].getIndexes();
          float [] values = svFVs[i].getValues();
          for(int j=0; j<svFVs[i].getLen(); ++j)
            w[indexes[j]] += alphas[i]*values[j];
        }
        SvmLibSVM.writeLinearModelIntoFile(modelFile, b, w, totalNumFeatures);
      } else {
        //just write the all the model into file in dual form
        modelFile.append(b+"\n");
        modelFile.append(numSV+"\n");
        for(int i=0; i<numSV; ++i) {
          line = svmModelBuff.readLine();
          modelFile.append(line);
        }
      }
    } catch(FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch(IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  
  public void applying(BufferedReader modelFile, DataForLearning dataFVinDoc, int totalNumFeatures, int numClasses) {
    float optB;
    optB = (1-tau)/(1+tau);
    svm_parameter svmParam = setSvmParamByCommandline();
    SvmLibSVM.svmApplying(modelFile, dataFVinDoc, totalNumFeatures, numClasses, optB, svmParam, this.isUseTauALLCases);
    
  }
  /** Set the svm_parameter.*/
  svm_parameter setSvmParamByCommandline() {
    svm_parameter param = new svm_parameter();
    param.kernel_type = kernelType;
    param.degree = paramD;
    param.gamma = paramG;
    param.coef0 = paramR;
    
    return param;
  }
  

//a class used for execute an external command
  class StreamGobbler extends Thread
  {
      InputStream is;
      String type;
      
      StreamGobbler(InputStream is, String type)
      {
          this.is = is;
          this.type = type;
      }

      public void run()
      {
          try
          {
              InputStreamReader isr = new InputStreamReader(is);
              BufferedReader br = new BufferedReader(isr);
              String line=null;
              while ( (line = br.readLine()) != null)
                  System.out.println(type + ">" + line);    
              } catch (IOException ioe)
                {
                  ioe.printStackTrace();  
                }
      }
  }
  

  public void runExternalCommand(String command)
  {
      
      try
      {            
          String osName = System.getProperty("os.name" );
          String[] cmd = new String[3];

          if( osName.equals( "Windows XP") || osName.equals("Windows NT") )
          {
              cmd[0] = "cmd.exe" ;
              cmd[1] = "/C" ;
              cmd[2] = command;
          }
          else if( osName.equals( "Windows 95" ) )
          {
              cmd[0] = "command.com" ;
              cmd[1] = "/C" ;
              cmd[2] = command;
          } else {
            cmd[0] = " " ;
              cmd[1] = " " ;
              cmd[2] = command;
          }
          
          Runtime rt = Runtime.getRuntime();
          //System.out.println("Execing " + cmd[0] + " " + cmd[1] 
              //               + " " + cmd[2]);
          Process proc = rt.exec(cmd);
          // any error message?
          StreamGobbler errorGobbler = new 
              StreamGobbler(proc.getErrorStream(), "ERROR");            
          
          // any output?
          StreamGobbler outputGobbler = new 
              StreamGobbler(proc.getInputStream(), "OUTPUT");
              
          // kick them off
          errorGobbler.start();
          outputGobbler.start();
                                  
          // any error???
          int exitVal = proc.waitFor();
          //System.out.println("ExitValue: " + exitVal);        
      } catch (Throwable t)
        {
          t.printStackTrace();
        }

  }
  
  
}
