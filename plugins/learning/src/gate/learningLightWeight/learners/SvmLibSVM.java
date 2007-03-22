package gate.learningLightWeight.learners;

import gate.learningLightWeight.LabelsOfFV;
import gate.learningLightWeight.SparseFeatureVector;
import gate.learningLightWeight.UsefulFunctions;
import gate.learningLightWeight.learners.libSVM.svm;
import gate.learningLightWeight.learners.libSVM.svm_node;
import gate.learningLightWeight.learners.libSVM.svm_parameter;
import gate.learningLightWeight.learners.libSVM.svm_problem;
import gate.learningLightWeight.learners.libSVM.svm_train;
import gate.learningLightWeight.learners.libSVM.svm.decision_function;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class SvmLibSVM extends SupervisedLearner{
  /** The uneven margins parameter. */
  private float tau = (float)0.4;
  
  /** The kernel type. */
  //private int kernelType =0; //linear kernel as default value 
  /** The SVM type. */
  private int svmType =0; //classification as default value 
  
  /** Define a new svm_train mainly for parameter. */
  svm_train svmTrain;
  
  /**Class constructor without parameter.*/
  public SvmLibSVM() {
    svmTrain = new svm_train();
  }
  /**Class constructor with tau parameter.*/
  public SvmLibSVM(float t) {
    this.tau = t;
    svmTrain = new svm_train();
  }
  /** Get the parameters from the command line.*/
  public void getParametersFromCommmand() {
    svmTrain.param = new svm_parameter();
    if(commandLine == null) {
      System.out.println("Error: no command yet!!");
    } else {
      String [] items = commandLine.split("[ \t]+");
      //Get the tau value
      for(int i=0; i<items.length; ++i)
        if(items[i].equalsIgnoreCase("tau") && i+1<items.length)
          this.tau = new Float(items[i+1]).floatValue();
      
      
      commandLine.concat("  ");
      
      String commandLineSVM = SvmForExec.obtainSVMCommandline(commandLine);
      
      System.out.println("**commandLineSVM="+commandLineSVM);
      
      svmTrain.parse_command_line(commandLineSVM.split("[ \t]+"));
    }
  }
  /** Using the methods of libSVM to training the SVM model.*/
  public void training(BufferedWriter modelFile, 
          SparseFeatureVector[] dataLearning, int totalNumFeatures, short [] classLabels, int numTraining) {
    
      //Set the svm problem for libsvm
      svm_problem svmProb = new svm_problem();
      svmProb.l = numTraining; //set the number of FVs
      svmProb.y = new double[svmProb.l];
      for(int i=0; i<numTraining; ++i)
        svmProb.y[i] = classLabels[i]; //set the label
      svmProb.x = new svm_node[svmProb.l][];
      //System.out.println("probL="+svmProb.l);
      //Vector vx = new Vector();
      for(int i=0; i<numTraining; ++i) {
        int len = dataLearning[i].getLen();
        //System.out.println("i="+i+" len="+len);
        svmProb.x[i] = new svm_node[len];
        for(int j=0; j<len; ++j) { //set each FV
          //System.out.println("j="+j + " index="+dataLearning[i].indexes[j]);
          svmProb.x[i][j] = new svm_node();
          svmProb.x[i][j].index = dataLearning[i].indexes[j];
          svmProb.x[i][j].value = dataLearning[i].values[j];
        }
      }
      //We got the parameter setting already in svmTrain
      //Call the svm_train_one to train a binary classification
      //set the weight for two class as 1.0, namely j=1 in svm_light
       decision_function decisionFunc = 
         svm.svm_train_one(svmProb, svmTrain.param, 1.0, 1.0);
       //decisionFunc includes the alphas (with *y) and rho (=-b)
       //Write the svm model into the model file
       try {
         float b = -1*(float)decisionFunc.rho;
         if(svmTrain.param.kernel_type == svm_parameter.LINEAR) {
           //Convert the dual form into primal form
           float [] w = new float[totalNumFeatures];
           for(int i=0; i<svmProb.l; ++i) {
             if(Math.abs(decisionFunc.alpha[i])>0)
               for(int j=0; j<svmProb.x[i].length; ++j)
                 w[svmProb.x[i][j].index] += svmProb.x[i][j].value*decisionFunc.alpha[i];
           }
           writeLinearModelIntoFile(modelFile, b, w, totalNumFeatures);
         } else {
           modelFile.append(b+"\n");
           int numSV = 0;
           for(int i=0; i<svmProb.l; ++i)
             if(Math.abs(decisionFunc.alpha[i])>0)
               ++numSV;
           modelFile.append(numSV+"\n");
           for(int i=0; i<svmProb.l; ++i) {
             if(Math.abs(decisionFunc.alpha[i])>0) {
               modelFile.append(new Double(decisionFunc.alpha[i]).toString());
               for(int j=0; j<svmProb.x[i].length; ++j)
                 modelFile.append(" "+svmProb.x[i][j].index+":"+svmProb.x[i][j].value);
               modelFile.append(" #\n");//in order to keep the same as model file in svm_light
             }
           }
         }
       } catch(IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
       }    
    }
    
    public void applying(BufferedReader modelFile, DataForLearning dataFVinDoc, 
            int totalNumFeatures, int numClasses) {
      float optB;
      if(isUseTau)
        optB= (1-tau)/(1+tau);
      else optB= 0;
      svmApplying(modelFile, dataFVinDoc, totalNumFeatures, numClasses, optB, svmTrain.param, this.isUseTauALLCases);
    }
    
    public static void svmApplying(BufferedReader modelFile, DataForLearning dataFVinDoc, 
            int totalNumFeatures, int numClasses, float optB, svm_parameter svmParam, boolean isUseTauAll) {
      if(svmParam.kernel_type == svm_parameter.LINEAR) { //if it is linear kernel
        applyLinearModel(modelFile, dataFVinDoc, totalNumFeatures, numClasses, optB, isUseTauAll);
      } else {
        //System.out.println("Non-linear kernel.");
        applyingDualFormModel(modelFile, dataFVinDoc, numClasses, optB, svmParam, isUseTauAll);
        
      }
    }
    
    public static void applyingDualFormModel(BufferedReader modelFile, 
            DataForLearning dataFVinDoc, int numClasses, float optB, svm_parameter svmParam, boolean isUseTauAll) {
      try {
        //set the multi class number and allocate the memory
        for(int i=0; i<dataFVinDoc.getNumTrainingDocs(); ++i)
          for(int j=0; j<dataFVinDoc.trainingFVinDoc[i].getNumInstances(); ++j) {
            dataFVinDoc.labelsFVDoc[i].multiLabels[j] = new LabelsOfFV(numClasses);
            dataFVinDoc.labelsFVDoc[i].multiLabels[j].probs = new float[numClasses];
          }
        //for each class
        System.out.println("****  numClasses="+numClasses);
        System.out.println("  d="+svmParam.degree+", g="+svmParam.gamma+ ", r="+svmParam.coef0);
        for(int iClass=0; iClass<numClasses; ++iClass) {
          float b;
          String [] items = modelFile.readLine().split(" ");//Read the class label line
          
          //Get the number of positive examples and negative examples
          int [] instDist = new int[2]; //instDist[0]=numPos, instDist[1]=numNeg;
          obtainInstDist(items, instDist);
    
          b = new Float(modelFile.readLine()).floatValue();
          if(isUseTauAll)
            b += optB;
          else {
            //if((instDist[0]<10 && instDist[0]>0 && instDist[1]/instDist[0]>10) || (instDist[0]>0 && instDist[1]/instDist[0]>20))
            if(instDist[0]>0 && instDist[1]/instDist[0]>10)
              b += optB;
            if(instDist[1]>0 && instDist[0]/instDist[1]>10)
              b -= optB;
          }
          
          int numSV;
          numSV = new Integer(modelFile.readLine()).intValue();
          SparseFeatureVector [] svFVs = new SparseFeatureVector[numSV];
          double [] alphas = new double [numSV];
          for(int i=0; i<numSV; ++i) {
            alphas[i] = readOneSV(modelFile.readLine(), svFVs, i);
          }
          
          for(int i=0; i<dataFVinDoc.getNumTrainingDocs(); ++i) {
            SparseFeatureVector [] fvs = dataFVinDoc.trainingFVinDoc[i].getFvs();
            for(int j=0; j<dataFVinDoc.trainingFVinDoc[i].getNumInstances(); ++j) {
              double sum = 0.0;
              for(int j1=0; j1<numSV; ++j1){
                sum += alphas[j1]*kernel_function(fvs[j], svFVs[j1], svmParam);
              }
              sum += b;
              
              sum = UsefulFunctions.sigmoid(sum);
              dataFVinDoc.labelsFVDoc[i].multiLabels[j].probs[iClass] = (float)sum;
            
            }
          }
        }
      } catch(NumberFormatException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch(IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    

    public static void readOneSVMModel(BufferedReader svmModelBuff, int numSV, 
            SparseFeatureVector [] svFVs, double [] alphas) throws IOException {
      for(int i=0; i<numSV; ++i) {
        alphas[i] = readOneSV(svmModelBuff.readLine(), svFVs, i);
      }
    }
    public static double readOneSV(String line, SparseFeatureVector []svFVs, int i) {
      String [] items;
      items = line.split(" ");
      double alpha = new Double(items[0]).doubleValue();
      int len = items.length -2; //since it contains alpha and the # at the end
      svFVs[i] = new SparseFeatureVector(len);
      for(int j=0; j<len; ++j) {
        String [] indexValue = items[j+1].split(":");
        svFVs[i].indexes[j] = new Integer(indexValue[0]).intValue();
        svFVs[i].values[j]  = new Float(indexValue[1]).floatValue();
      }
      
      return alpha;
    }
    
    public static double kernel_function(SparseFeatureVector x, SparseFeatureVector y, svm_parameter param) {
      switch(param.kernel_type) {
        case svm_parameter.LINEAR:
          return dot(x, y);
        case svm_parameter.POLY:
          return powi(param.gamma*dot(x,y)+param.coef0,param.degree);
        case svm_parameter.RBF:
        {
          double sum = 0;
          int xlen = x.getLen();
          int ylen = y.getLen();
          int i = 0;
          int j = 0;
          while(i < xlen && j < ylen) {
            if(x.indexes[i] == y.indexes[j]) {
              double d = x.values[i++] - y.values[j++];
              sum += d*d;
            }
            else if(x.indexes[i] > y.indexes[i]) {
              sum += y.values[j] * y.values[j];
              ++j;
            }
            else {
              sum += x.values[i] * x.values[i];
              ++i;
            }
          }
          while(i < xlen) {
            sum += x.values[i] * x.values[i];
            ++i;
          }
          while(j < ylen){
            sum += y.values[j] * y.values[j];
            ++j;
          }
          return Math.exp(-param.gamma*sum);
        }
        case svm_parameter.SIGMOID:
          return tanh(param.gamma*dot(x,y)+param.coef0);
        //case svm_parameter.PRECOMPUTED:
          //return  x[(int)(y[0].value)].value;
        default:
          return 0.0;
      }
    }
      
    private static double tanh(double x)
    {
      double e = Math.exp(x);
      return 1.0-2.0/(e*e+1);
    }
    private static double powi(double base, int times) {
      double tmp = base, ret = 1.0;
      for(int t=times; t>0; t/=2) {
        if(t%2==1) 
          ret*=tmp;
        tmp = tmp * tmp;
      }
      return ret;
    }

    static double dot(SparseFeatureVector x, SparseFeatureVector y) {
      double sum = 0;
      int xlen = x.getLen();
      int ylen = y.getLen();
      int i = 0;
      int j = 0;
      while(i < xlen && j < ylen)
      {
      if(x.indexes[i] == y.indexes[j])
        sum += x.values[i++] * y.values[j++];
      else
      {
        if(x.indexes[i] > y.indexes[j])
          ++j;
        else
          ++i;
      }
    }
    return sum;
  }

    public static void writeLinearModelIntoFile(BufferedWriter modelFile, float b, float [] w, 
      int totalFeatures) throws IOException {
      float verySmallFloat = (float)0.000000001;
      modelFile.append(b+"\n");
      int num;
      num=0;
      for(int i=0; i<totalFeatures; ++i)
        if(Math.abs(w[i])>verySmallFloat)
          ++num;
      modelFile.append(num+"\n");
      for(int i=0; i<totalFeatures; ++i)
        if(Math.abs(w[i])>verySmallFloat)
          modelFile.append(i + " "+w[i]+"\n");

      return;
    }

    public static float readWeightVectorFromFile(BufferedReader modelFile, float [] w) throws NumberFormatException, IOException {
      float b;
      b = new Float(modelFile.readLine()).floatValue();
      int num;
      num = new Integer(modelFile.readLine()).intValue();
      String [] lineItems;
      int index;
      float value;
      for(int i=0; i<num; ++i) {
        lineItems = modelFile.readLine().split(" ");
        index = new Integer(lineItems[0]).intValue();
        value = new Float(lineItems[1]).floatValue();
        w[index] = value;
      }
      return b;
    }

    public static void applyLinearModel(BufferedReader modelFile, DataForLearning dataFVinDoc, 
      int totalNumFeatures, int numClasses, float optB, boolean isUseTauAll) {
      try {
        //set the multi class number and allocate the memory
        for(int i=0; i<dataFVinDoc.getNumTrainingDocs(); ++i)
          for(int j=0; j<dataFVinDoc.trainingFVinDoc[i].getNumInstances(); ++j) {
            dataFVinDoc.labelsFVDoc[i].multiLabels[j] = new LabelsOfFV(numClasses);
            dataFVinDoc.labelsFVDoc[i].multiLabels[j].probs = new float[numClasses];
          }
        //for each class
        //System.out.println("****  numClasses="+numClasses);
        for(int iClass=0; iClass<numClasses; ++iClass) {
          float b;
          float [] w = new float [totalNumFeatures];
          String items [] = modelFile.readLine().split(" ");//Read the class label line
          //Get the number of positive examples and negative examples
          int [] instDist = new int[2]; //instDist[0]=numPos, instDist[1]=numNeg;
          obtainInstDist(items, instDist);
    
          b = readWeightVectorFromFile(modelFile, w);
          //modify the b by using the uneven margins parameter tau
          if(isUseTauAll)
            b+= optB;
          else {
            if(instDist[0]>0 && instDist[1]/instDist[0]>10)
              b += optB;
            if(instDist[1]>0 && instDist[0]/instDist[1]>10)
              b -= optB;
          }
          
          for(int i=0; i<dataFVinDoc.getNumTrainingDocs(); ++i) {
            SparseFeatureVector [] fvs = dataFVinDoc.trainingFVinDoc[i].getFvs();
            for(int j=0; j<dataFVinDoc.trainingFVinDoc[i].getNumInstances(); ++j) {
              int [] index = fvs[j].getIndexes();
              float [] value = fvs[j].getValues();
              double sum = 0.0;
              for(int j1=0; j1<fvs[j].getLen(); ++j1)
                sum += value[j1]*w[index[j1]];
              sum += b;
              sum = UsefulFunctions.sigmoid(sum);
              dataFVinDoc.labelsFVDoc[i].multiLabels[j].probs[iClass] = (float)sum;
        
              //if(sum>0.1) System.out.println("iclass="+iClass+ " i="+ i+ " j=" + j +" sum="+sum);
        
            }
          }
        } 
      } catch(NumberFormatException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch(IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    static void obtainInstDist(String []items, int [] instDist) {
      for(int i=0; i<items.length; ++i) {
        if(items[i].startsWith("numTraining="))
          instDist[1] = Integer.parseInt(items[i].substring(items[i].indexOf("=")+1));
        if(items[i].startsWith("numPos="))
          instDist[0] = Integer.parseInt(items[i].substring(items[i].indexOf("=")+1));
      }
      instDist[1] -= instDist[0];
    }
    
}
