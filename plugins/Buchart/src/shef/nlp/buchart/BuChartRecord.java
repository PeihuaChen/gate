package shef.nlp.buchart;

//gate stuff
import gate.*;
import gate.util.*;

//java
import java.util.*;

public class BuChartRecord
{
   String category;
   Long start;
   Long end;
   FeatureMap features;

   public BuChartRecord(String cat, Long s, Long e, FeatureMap fm)
   {
      category=cat;
      start=s;
      end=e;
      features=fm;
   }

   public Long getStart() { return start;}
   public Long getEnd()   { return end;}
   public String getCategory() { return category;}
   public FeatureMap getFeatures() { return features;}

   /** creates a buchart category output cat(f1:v1,f2:v2,...) where f1,... are
    * extracted from outputFeatures
    **/
   public String toBuChart(ArrayList outputFeatures)
   {
      StringBuffer output=new StringBuffer();
      String fname;
      String fvalue;
      FeatureMap fm=getFeatures();
      String catName=getCategory();
      output.append(catName+"(");
      /* put all features an values required */
      for(int f=0;f<outputFeatures.size()-1;f++)
      {
         fname=(String)outputFeatures.get(f);
         if(fm.containsKey(fname))
         {
            fvalue=(String)fm.get(fname);
         }
         else
         {
            fvalue="_";
         }
         output.append(fname+":"+quoteValue(fvalue)+",");
      }
      /* put last feature and value */
      fname=(String)outputFeatures.get(outputFeatures.size()-1);
      if(fm.containsKey(fname))
      {
         fvalue=(String)fm.get(fname);
      }
      else
      {
         fvalue="_";
      }
      output.append(fname+":"+quoteValue(fvalue)+")");

      return output.toString();
   }

   public static String quoteValue(String value)
   {
      String output = "";
      output = "\'";
      int len = value.length();
      for(int i=0; i<len; i++)
      {
         if(value.charAt(i)=='\'')
         {
            output += "''";
         }
         else if(value.charAt(i)=='\n')
         {
            output += " ";
         }
         else
            output +=  value.charAt(i);
      }
      output += '\'';
      return output;
   }

   public static Comparator BuChartRecordComparator()
   {
      Comparator comp = new Comparator()
      {
         public int compare(Object o1, Object o2)
         {
            BuChartRecord a1 = (BuChartRecord) o1;

            BuChartRecord a2 = (BuChartRecord) o2;

            return a1.getStart().compareTo(a2.getStart());

         }
      };
      return comp;
   }
}