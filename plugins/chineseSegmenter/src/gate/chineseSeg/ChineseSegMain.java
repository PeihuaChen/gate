package gate.chineseSeg;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.ProcessingResource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.learning.DocFeatureVectors;
import gate.learning.Label2Id;
import gate.learning.LabelsOfFV;
import gate.learning.LabelsOfFeatureVectorDoc;
import gate.learning.LearningEngineSettings;
import gate.learning.LogService;
import gate.learning.NLPFeaturesList;
import gate.learning.SparseFeatureVector;
import gate.learning.learners.MultiClassLearning;
import gate.learning.learners.PostProcessing;
import gate.learning.learners.SupervisedLearner;
import gate.util.Benchmark;
import gate.util.ExtensionFileFilter;
import gate.util.GateException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;

public class ChineseSegMain extends AbstractLanguageAnalyser implements
                                                            ProcessingResource {
  URL modelURL = null;
  URL textFilesURL = null;

  /** Initialise this resource, and return it. */
  public gate.Resource init() throws ResourceInstantiationException {
    return this;
  } // init()

  /**
   * Run the resource.
   * 
   * @throws ExecutionException
   */
  public void execute() throws ExecutionException {

    boolean isUpdateFeatList = true;
    // boolean isTraining = true;
    boolean isTraining = false;

    // load the existing terms and labels
    File wdResults = new File("C:\\yaoyong\\javawk\\chineseSeg\\test\\data\\",
      ConstantParameters.FILENAME_resultsDir);
    if(!wdResults.exists()) wdResults.mkdir();

    File logFile = new File(wdResults, ConstantParameters.FILENAMEOFLOGFILE);
    int verbosityLogService = 1;
    try {
      LogService.init(logFile, true, verbosityLogService);

      // read the feature list from the file
      NLPFeaturesList featuresList = null;
      featuresList = new NLPFeaturesList();
      featuresList.loadFromFile(wdResults, ConstantParameters.FILENAME_TERMS);
      if(!featuresList.featuresList.containsKey(ConstantParameters.NONFEATURE)) {
        int size = featuresList.featuresList.size() + 1;
        featuresList.featuresList.put(ConstantParameters.NONFEATURE,
          new Integer(size));
        featuresList.idfFeatures.put(ConstantParameters.NONFEATURE,
          new Integer(1));
      }

      // read the label list
      Label2Id labelsAndId;
      labelsAndId = new Label2Id();
      labelsAndId.loadLabelAndIdFromFile(wdResults,
        ConstantParameters.FILENAMEOFLabelList);

      // ConstantParameters.FILENAME_TERMS);
      Corpus corpus = Factory.newCorpus("Data-chinese");
      ExtensionFileFilter fileFilter = new ExtensionFileFilter();
      fileFilter.addExtension("txt");
      // corpus.populate(new
      // File("C:\\yaoyong_h\\work\\bnc-kim\\small").toURL(),
      // fileFilter, "UTF-8", false);

      // corpus.populate(new
      // File("C:\\yaoyong\\javawk\\chineseSeg\\test\\data").toURI().toURL(),
      // fileFilter, "UTF-8", false);

      corpus.populate(new File(
        "C:\\yaoyong\\javawk\\chineseSeg\\test\\data\\labels").toURI().toURL(),
        fileFilter, "UTF-8", false);

      // corpus.populate(new
      // File("C:\\yaoyong\\javawk\\chineseSeg\\test\\data\\gb2312").toURI().toURL(),
      // fileFilter, "GB18030", false);

      // corpus.populate(new File("C:\\yaoyong_h\

      System.out.println("number of docs=" + corpus.size());
      BufferedWriter outFeatureVectors = null;
      if(isTraining) {
        outFeatureVectors = new BufferedWriter(new OutputStreamWriter(
          new FileOutputStream(new File(wdResults,
            ConstantParameters.FILENAMEOFFeatureVectorData)), "UTF-8"));
      }

      int totalN = 0;
      int numDocs = 0;
      for(int iIndex = 0; iIndex < corpus.size(); ++iIndex) {
        ++numDocs;
        // read the text from a document
        Document doc = (Document)corpus.get(iIndex);
        System.out.println(iIndex + ", docName=" + doc.getName());
        System.out.println(iIndex + ", content=" + doc.getContent() + "*");
        String text = doc.getContent().toString();
        // convert the document into an array of characters with replacements
        char[] chs = new char[text.length()];
        int num;
        StringBuffer letterNum = new StringBuffer();
        num = convert2Chs(text, chs, letterNum);
        // get the labels
        String[] labels = new String[chs.length];
        if(isTraining) {
          num = obtainLabels(num, chs, labels);
          // update the labels
          labelsAndId.updateMultiLabelFromDoc(labels);
        }

        // for(int j = 0; j < num; ++j) {
        // System.out.println(j + ", ch=*" + chs[j] + "* type="
        // + Character.getType(chs[j]) + ", labels=*" + labels[j] + "*");
        // }
        System.out.println("** LN=" + letterNum);
        // get the features from the array
        String[] termC1 = new String[num + 2]; // with begin and start char
                                                // added
        String[] termC12 = new String[num + 1]; // for the c0c1
        String[] termC13 = new String[num]; // for the c-1c1 feature
        obtainTerms(num, chs, termC1, termC12, termC13);
        // update the feature list
        if(isUpdateFeatList) {
          updateFeatList(featuresList, termC1);
          updateFeatList(featuresList, termC12);
          updateFeatList(featuresList, termC13);
        }
        // get the real features from the terms
        DocFeatureVectors docFV = new DocFeatureVectors();
        docFV.docId = new String(doc.getName());

        // System.out.println("888 feat=旅"+"*,
        // id="+featuresList.featuresList.get("旅").toString()+"*");
        // for(Object obj:featuresList.featuresList.keySet()) {
        // System.out.println("feat="+obj.toString()+",
        // id="+featuresList.featuresList.get(obj).toString());
        // }

        putFeatsIntoDocFV(featuresList, termC1, termC12, termC13, docFV);

        LabelsOfFV[] multiLabels = new LabelsOfFV[num];
        for(int j = 0; j < num; ++j) {
          int[] labelsId = new int[1];
          if(isTraining)
            labelsId[0] = new Integer(labelsAndId.label2Id.get(labels[j])
              .toString()).intValue();
          else labelsId[0] = -1;
          float[] labelPr = new float[1];
          labelPr[0] = 1;
          multiLabels[j] = new LabelsOfFV(1, labelsId, labelPr);
        }

        System.out.println("numInstance=" + docFV.numInstances);

        if(!isTraining) {
          outFeatureVectors = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(new File(wdResults,
              ConstantParameters.FILENAMEOFFeatureVectorData)), "UTF-8"));
        }

        docFV.addDocFVsMultiLabelToFile(iIndex, outFeatureVectors, multiLabels);

        if(!isTraining) {
          outFeatureVectors.flush();
          outFeatureVectors.close();
        }

        if(!isTraining) {
          int[] selectedLabels = null;
          selectedLabels = segementText(wdResults);
          // get back to the replacements for letter, number and newline.
          String[] terms = letterNum.toString().split(
            ConstantParameters.SEPARATTORLN);
          int kk = 0;
          for(int j = 0; j < num; ++j) {
            // System.out.println(j + ", ch=*" + chs[j] + "* type="
            // + Character.getType(chs[j]) + ", labels=*" + selectedLabels[j] +
            // "*");
            String labelC = null;
            String iObj = new Integer(selectedLabels[j]).toString();
            if(labelsAndId.id2Label.containsKey(iObj))
              labelC = labelsAndId.id2Label.get(iObj).toString();

            if(chs[j] == ConstantParameters.REPLACEMENT_Digit
              || chs[j] == ConstantParameters.REPLACEMENT_Letter
              || chs[j] == ConstantParameters.NEWLINE_Char) {
              System.out.print(terms[kk++] + "(" + labelC + ") ");
            }
            else System.out.print(chs[j] + "(" + labelC + ") ");
          }
        }

      }
      if(isTraining) {
        outFeatureVectors.flush();
        outFeatureVectors.close();
      }

      if(isUpdateFeatList) {
        featuresList.writeListIntoFile(wdResults,
          ConstantParameters.FILENAME_TERMS);
      }
      // write the labels into the file
      if(isTraining) {
        labelsAndId.writeLabelAndIdToFile(wdResults,
          ConstantParameters.FILENAMEOFLabelList);
      }

      if(isTraining) learningNewModel(wdResults, numDocs);

      System.out.println("totalN=" + totalN);
      System.out.println("Finished!");
    }
    catch(IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch(ResourceInstantiationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch(GateException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  static void learningNewModel(File wdResults, int numDocs)
    throws GateException {

    String fvFileName = wdResults.toString() + File.separator
      + ConstantParameters.FILENAMEOFFeatureVectorData;
    File dataFile = new File(fvFileName);
    String modelFileName = wdResults.toString() + File.separator
      + ConstantParameters.FILENAMEOFModels;
    File modelFile = new File(modelFileName);

    String learningCommand = "  ";
    String dataSetFile = null;
    SupervisedLearner paumLearner = MultiClassLearning.obtainLearnerFromName(
      "PAUM", learningCommand, dataSetFile);
    paumLearner.setLearnerExecutable("");
    String learningParas = " -p 20 -n 1 -optB 0.0 ";
    paumLearner.setLearnerParams(learningParas);

    MultiClassLearning chunkLearning = new MultiClassLearning(
      LearningEngineSettings.OneVSOtherMode);

    // read data
    File tempDataFile = new File(wdResults,
      ConstantParameters.TempFILENAMEofFVData);
    boolean isUsingTempDataFile = false;
    if(paumLearner.getLearnerName().equals("SVMExec"))
      isUsingTempDataFile = true; // using the temp data file
    chunkLearning.getDataFromFile(numDocs, dataFile, isUsingTempDataFile,
      tempDataFile);

    // training
    // using different method for one thread or multithread
    // if(engineSettings.numThreadUsed >1 )//for using thread
    // chunkLearning.training(paumLearner, modelFile);
    // else //for not using thread
    chunkLearning.trainingNoThread(paumLearner, modelFile, isUsingTempDataFile,
      tempDataFile);
  }

  /**
   * segement the text using the learned model.
   * 
   * @throws GateException
   */
  static int[] segementText(File wdResults) throws GateException {
    int numDocs = 1;
    String fvFileName = wdResults.toString() + File.separator
      + ConstantParameters.FILENAMEOFFeatureVectorData;
    File dataFile = new File(fvFileName);
    String modelFileName = wdResults.toString() + File.separator
      + ConstantParameters.FILENAMEOFModels;
    File modelFile = new File(modelFileName);

    String learningCommand = "  ";
    String dataSetFile = null;
    SupervisedLearner paumLearner = MultiClassLearning.obtainLearnerFromName(
      "PAUM", learningCommand, dataSetFile);
    paumLearner.setLearnerExecutable("");
    String learningParas = " -p 50 -n 5 -optB 0.0 ";
    paumLearner.setLearnerParams(learningParas);

    MultiClassLearning chunkLearning = new MultiClassLearning(
      LearningEngineSettings.OneVSOtherMode);

    // read data
    File tempDataFile = new File(wdResults,
      ConstantParameters.TempFILENAMEofFVData);
    boolean isUsingTempDataFile = false;
    if(paumLearner.getLearnerName().equals("SVMExec"))
      isUsingTempDataFile = true; // using the temp data file
    chunkLearning.getDataFromFile(numDocs, dataFile, isUsingTempDataFile,
      tempDataFile);

    chunkLearning.applyNoThread(paumLearner, modelFile);
    LabelsOfFeatureVectorDoc[] labelsFVDoc = null;
    labelsFVDoc = chunkLearning.dataFVinDoc.labelsFVDoc;
    int numClasses = chunkLearning.numClasses;

    // applying to text
    // String featName = engineSettings.datasetDefinition.arrs.classFeature;
    // String instanceType = engineSettings.datasetDefinition.getInstanceType();
    Label2Id labelsAndId = new Label2Id();
    labelsAndId.loadLabelAndIdFromFile(wdResults,
      ConstantParameters.FILENAMEOFLabelList);
    // post-processing and add new annotation to the text
    float boundaryP = 0;
    float entityP = 0;
    float thresholdClassificaton = -999;
    PostProcessing postPr = new PostProcessing(boundaryP, entityP,
      thresholdClassificaton);

    int[] selectedLabels = new int[labelsFVDoc[0].multiLabels.length];
    float[] valuesLabels = new float[labelsFVDoc[0].multiLabels.length];
    postPr.postProcessingClassification((short)3, labelsFVDoc[0].multiLabels,
      selectedLabels, valuesLabels);

    return selectedLabels;

  }

  /**
   * convert a text into an array of characters with replacements of letters and
   * numbers.
   */
  public static int convert2Chs(String text, char[] chs, StringBuffer letterNum) {
    int num = 0;
    boolean isL = false;
    boolean isN = false;
    boolean isR = false;
    for(int ind = 0; ind < text.length(); ++ind) {
      Character ch = text.charAt(ind);
      if(isDelim(ch)) continue; // not use blank
      int tc = Character.getType(ch);
      if(tc == Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING) continue;
      if(tc == Character.DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE) {
        letterNum.append(ch);
        if(!isR) {
          if(isL || isN) letterNum.append(ConstantParameters.SEPARATTORLN);
          chs[num] = ConstantParameters.NEWLINE_Char;
          ++num;
          isR = true;
          isN = false;
          isL = false;
        }
      }
      else if(tc == Character.LOWERCASE_LETTER
        || tc == Character.UPPERCASE_LETTER || tc == Character.TITLECASE_LETTER) {
        letterNum.append(ch);
        if(!isL) {
          if(isN || isR) letterNum.append(ConstantParameters.SEPARATTORLN);
          chs[num] = ConstantParameters.REPLACEMENT_Letter;
          ++num;
          isL = true;
          isN = false;
          isR = false;
        }
      }
      else if(Character.isDigit(ch)) {
        letterNum.append(ch);
        if(!isN) {
          chs[num] = ConstantParameters.REPLACEMENT_Digit;
          ++num;
          if(isL || isR) letterNum.append(ConstantParameters.SEPARATTORLN);
          isN = true;
          isL = false;
          isR = false;
        }
      }
      else {
        if(isL || isN || isR) {
          letterNum.append(ConstantParameters.SEPARATTORLN);
          isL = false;
          isN = false;
          isR = false;
        }
        chs[num] = ch;
        ++num;
      }
    }
    return num;
  }

  /** Obtain the labels for the text, i.e. l, m, r and s */
  public static int obtainLabels(int numkk, char[] chs, String[] labels) {
    // first add the space to the non-letter
    int num = 0;
    char[] chsSim11 = new char[3 * numkk + 1];
    for(int id = 0; id < numkk; ++id) {
      if(Character.isLetterOrDigit(chs[id]) || isDelim(chs[id])) {
        chsSim11[num++] = chs[id];
      }
      else {
        chsSim11[num++] = ConstantParameters.SEPARATTOR_BLANK;
        chsSim11[num++] = chs[id];
        chsSim11[num++] = ConstantParameters.SEPARATTOR_BLANK;
      }
    }
    // Then remove the duplicate spaces
    char[] chsSim = new char[3 * numkk + 1];
    boolean[] isDelA = new boolean[3 * numkk + 1];
    int num11 = 0;
    boolean isD = false;
    for(int id = 0; id < num; ++id) {
      // if(Character.isWhitespace(chsSim11[id])) {
      if(isDelim(chsSim11[id])) {
        if(!isD) {
          isD = true;
          chsSim[num11] = chsSim11[id];
          isDelA[num11] = true;
          ++num11;
        }
      }
      else {
        isD = false;
        chsSim[num11] = chsSim11[id];
        isDelA[num11] = false;
        ++num11;
      }
    }
    isDelA[num11++] = true;

    // then get the labels for each character
    int wS = 0;
    int lenW = 0;
    num = 0;
    for(int id = 0; id < num11; ++id) {
      if(isDelA[id]) { // if the current character is delimiter
        lenW = id - wS;
        if(lenW == 1) { // a word with single character
          labels[num] = ConstantParameters.LABEL_S; // "4";
          chs[num] = chsSim[id - 1];
          ++num;
          wS = id + 1;
        }
        else if(lenW > 1) { // a word with multiple characters
          labels[num] = ConstantParameters.LABEL_L; // "1";
          chs[num] = chsSim[id - lenW];
          for(int i = 1; i < lenW - 1; ++i) {
            labels[num + i] = ConstantParameters.LABEL_M; // "2";
            chs[num + i] = chsSim[id - lenW + i];
          }
          labels[num + lenW - 1] = ConstantParameters.LABEL_R; // "3";
          chs[num + lenW - 1] = chsSim[id - 1];
          num += lenW;
          wS = id + 1;
        }
      }

    }

    System.out.println("num=" + num + ", num11=" + num11);

    return num;
  }

  static boolean isDelim(char ch) {
    if(ch == ConstantParameters.SEPARATTOR_BLANK) return true;
    if(ch == ConstantParameters.SEPARATTOR_BLANK_wide) return true;
    return false;
  }

  /** obtain the terms from the list of characters for the text */
  static void obtainTerms(int num00, char[] chs, String[] termC1,
    String[] termC12, String[] termC13) {
    // first the single character term
    termC1[0] = new Character(ConstantParameters.BEGIN_Char).toString();
    for(int i = 1; i <= num00; ++i)
      termC1[i] = new String(chs, i - 1, 1);
    termC1[num00 + 1] = new Character(ConstantParameters.END_Char).toString();
    // then the two terms, one following another, like c1c2
    for(int i = 0; i <= num00; ++i)
      termC12[i] = termC1[i] + termC1[i + 1];
    // finally the two terms, separated by one term, like c-1c1
    for(int i = 0; i < num00; ++i) {
      termC13[i] = termC1[i] + termC1[i + 2];
    }
  }

  /** using the terms to update the feature list. */
  static void updateFeatList(NLPFeaturesList featuresList, String[] terms) {
    int size = featuresList.featuresList.size();
    for(int ind = 0; ind < terms.length; ++ind) {
      // If the featureName is not in the feature list
      if(size < ConstantParameters.MAXIMUMFEATURES) {
        if(!featuresList.featuresList.containsKey(terms[ind])) {
          ++size;
          // features is from 1 (not zero), in the SVM-light
          // format
          featuresList.featuresList.put(terms[ind], new Long(size));
          featuresList.idfFeatures.put(terms[ind], new Long(1));
        }
        else {
          featuresList.idfFeatures.put(terms[ind], new Long(
            (new Long(featuresList.idfFeatures.get(terms[ind]).toString()))
              .longValue() + 1));
        }
      }
      else {
        System.out
          .println("There are more NLP features from the training docuemnts");
        System.out.println(" than the pre-defined maximal number"
          + new Long(ConstantParameters.MAXIMUMFEATURES));
        return;
      }
    }
  }

  /** add the feature into docFV */
  static void putFeatsIntoDocFV(NLPFeaturesList featuresList, String[] termC1,
    String[] termC12, String[] termC13, DocFeatureVectors docFV) {
    int num = termC1.length - 2; // all the characters in the text
    int num11 = termC1.length;
    docFV.numInstances = num;
    docFV.fvs = new SparseFeatureVector[docFV.numInstances];
    for(int ind = 0; ind < num; ++ind) {
      String[] feats = new String[10];
      // the single character feature
      feats[0] = termC1[ind + 1]; // c0
      feats[1] = termC1[ind]; // c-1
      if(ind - 1 >= 0)
        feats[2] = termC1[ind - 1]; // c-2
      else feats[2] = ConstantParameters.NONFEATURE;
      feats[3] = termC1[ind + 2]; // c1
      if(ind + 3 < termC1.length)
        feats[4] = termC1[ind + 3]; // c2
      else feats[4] = ConstantParameters.NONFEATURE;
      // the two-character feature
      feats[5] = termC12[ind + 1]; // c0c1
      feats[6] = termC12[ind]; // c-1c0
      if(ind - 1 >= 0)
        feats[7] = termC12[ind - 1]; // c-2c-1
      else feats[7] = ConstantParameters.NONFEATURE;
      if(ind + 2 < termC12.length)
        feats[8] = termC12[ind + 2]; // c1c2
      else feats[8] = ConstantParameters.NONFEATURE;
      ;
      feats[9] = termC13[ind];

      /*
       * System.out.print(ind+", feat="); for(int i=0; i<10; ++i)
       * System.out.print(feats[i]+"("+i+") "); System.out.println();
       */
      // get the features by using feature list
      // StringBuffer fv = new StringBuffer();
      docFV.fvs[ind] = new SparseFeatureVector(10);
      for(int i = 0; i < 10; i++) {
        docFV.fvs[ind].nodes[i].index = new Integer(featuresList.featuresList
          .get(feats[i]).toString()).intValue()
          + i * ConstantParameters.MAXIMUMFEATURES;
        docFV.fvs[ind].nodes[i].value = 1;
      }
    }
  }

  public void setModelURL(URL modelU) {
    this.modelURL = modelU;
  }

  public URL getModelURL() {
    return this.modelURL;
  }

  public void setTextFilesURL(URL modelU) {
    this.textFilesURL = modelU;
  }

  public URL getTextFilesURL() {
    return this.textFilesURL;
  }

}
