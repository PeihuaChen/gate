package shef.nlp.buchart;

/* test for the wrapper */

import gate.*;
import gate.util.*;
import gate.creole.*;
import java.io.*;

import java.net.*;

public class TestSuite
{

   public static void main(String[] args)
   {

      String creoleURL=args[0];
      String configURL=args[1];
      String tableURL=args[2];
      String parserFile=args[3];
      String implementation=args[4];

      File f = null;

      System.out.println("Testing wrapper " + implementation + "...");
      System.out.println(
         configURL + " \n" +
         tableURL + "\n" +
         parserFile
      );

      try
      {
         Gate.init();
         CreoleRegister reg=Gate.getCreoleRegister();
         f=new File(creoleURL);
         reg.registerDirectories(f.toURL());

         if (gate.Main.version.startsWith("3"))
         {
            f = new File(System.getProperty("gate.home"),"plugins/Tools/");
            reg.registerDirectories(f.toURL());
         }

         SerialAnalyserController controller = (SerialAnalyserController)Factory.createResource("gate.creole.SerialAnalyserController");

         /** tokeniser **/
         System.out.print("Loading Tokeniser...");
         FeatureMap fm_tokens = Factory.newFeatureMap();
         try
         {
            ProcessingResource tokens = (ProcessingResource) Factory.createResource("gate.creole.tokeniser.DefaultTokeniser",
            fm_tokens,
            Factory.newFeatureMap());
            controller.add(tokens);
            System.out.println(" Done");
         }
         catch (ResourceInstantiationException rie)
         {
            System.out.println(" FAILED");
            rie.printStackTrace();
         }

         /* sentence splitter */
         System.out.print("Loading Sentence Splitter...");
         FeatureMap fm_splitter=Factory.newFeatureMap();
         try
         {
            ProcessingResource splitter = (ProcessingResource) Factory.createResource("gate.creole.splitter.SentenceSplitter",
            fm_splitter,
            Factory.newFeatureMap());
            controller.add(splitter);
            System.out.println(" Done");
         }
         catch(ResourceInstantiationException rie)
         {
            System.out.println(" FAILED");
            rie.printStackTrace();
         }

         /* POS tagger */
         System.out.print("Loading POS Tagger...");
         FeatureMap fm_pos=Factory.newFeatureMap();
         try
         {
            ProcessingResource tagger = (ProcessingResource) Factory.createResource("gate.creole.POSTagger",
            fm_pos,
            Factory.newFeatureMap());
            controller.add(tagger);
            System.out.println(" Done");
         }
         catch(ResourceInstantiationException rie)
         {
            System.out.println(" FAILED");
            rie.printStackTrace();
         }

         if (gate.Main.version.startsWith("3"))
         {
            /* Morphology */
            System.out.print("Loading Morphological Analyzer...");
            try
            {
               ProcessingResource morphology = (ProcessingResource) Factory.createResource("gate.creole.morph.Morph",
               Factory.newFeatureMap());
               controller.add(morphology);
               System.out.println(" Done");
            }
            catch(ResourceInstantiationException rie)
            {
               System.out.println(" FAILED");
               rie.printStackTrace();
            }
         }


         /* Name Entity Recogniser */
         System.out.print("Loading NE Tagger...");
         try
         {
            ProcessingResource list= (ProcessingResource) Factory.createResource("gate.creole.gazetteer.DefaultGazetteer", Factory.newFeatureMap());
            controller.add(list);

            ProcessingResource ne= (ProcessingResource) Factory.createResource("gate.creole.ANNIETransducer");
            controller.add(ne);

            System.out.println(" Done");
         }
         catch(ResourceInstantiationException rie)
         {
            System.out.println(" FAILED");
            rie.printStackTrace();
         }

         /* Bottom-Up Chart Parser */
         System.out.print("Loading BuChart...");
         try
         {
            FeatureMap fm=Factory.newFeatureMap();
            fm.put(Buchart.CONFIG_FILE_PAR,(new File(configURL)).toURL());
            fm.put(Buchart.FEATURE_FILE_PAR,(new File(tableURL)).toURL());
            fm.put("buchartFile",new File(parserFile));
            fm.put("prologImplementation",implementation);

            ProcessingResource buchart=(ProcessingResource)Factory.createResource("shef.nlp.buchart.Buchart",fm);
            controller.add(buchart);

            System.out.println(" Done");
         }
         catch(ResourceInstantiationException rie)
         {
            System.out.println(" FAILED");
            rie.printStackTrace();
         }

         Document doc=Factory.newDocument("2 October 2004: this is a sentence. This is another sentence.");

         Corpus corpus=Factory.newCorpus("");
         corpus.add(doc);

         System.out.print("\nParsing document...");
         controller.setCorpus(corpus);
         controller.execute();
         System.out.println(" Done\n");

         System.out.println(doc.getAnnotations());
      }
      catch(GateException ge)
      {
         ge.printStackTrace();
      }
      catch(MalformedURLException murle)
      {
         murle.printStackTrace();
      }
   }
}