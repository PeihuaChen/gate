package shef.nlp.buchart.category;

import shef.nlp.buchart.utils.*;
import shef.nlp.buchart.BuChartRecord;
import java.util.*;
import gate.util.*;

public class InitialEdge extends Edge
{
	public InitialEdge(Long From, Long To, Category Cat, Long Begin, Long End, Long Id, int Level)
	{
		super();

		from = From;
		to = To;
		cat = Cat;
		begin = Begin;
		end = End;
		id = Id;
		cat_list = new ArrayList();
		creator_ID = -1;
		child_list = new ArrayList();
	}

	public String toBuchartFormat()
	{
		String output = "edge(";
		output += from.toString()+",";
		output += to.toString()+",";

		try
		{
			output += cat.toBuchartFormat()+",";
		}
		catch(Exception efm)
		{
			Err.println("Empty FM in Category: " + efm);
		}
		output += "[],[],[],";
		output += "1,";
		output += begin.toString()+",";
		output += end.toString()+",";
		output += id.toString()+")";

		output = output.replace('\r', ' ');
		output = output.replace('\n', ' ');

		return output;
	}

 public InitialEdge(Long From,
  Long To,
  BuChartRecord rec,

  Long Begin,
  Long End,
  Long Id,
  int Level
  )

  {

   super();

   from = From;
   to = To;
   record = rec;
   begin = Begin;
   end = End;
   id = Id;
   cat_list = new ArrayList();
   creator_ID = -1;
   child_list = new ArrayList();
  }



public String toBuChartFormat(Hashtable conversion) {
   StringBuffer output=new StringBuffer();
   String category=record.getCategory();
   ArrayList featureList;
   /* feature list for this category */
   if(conversion.containsKey(category)) {
     featureList=(ArrayList)conversion.get(category);
   } else {
     featureList=new ArrayList();
   }
   /* formatting */
   output.append("edge(");
   output.append(from.toString()+",");
   output.append(to.toString()+",");
   output.append(record.toBuChart(featureList)+",");
   output.append("[],[],[],");
   output.append("1,");
   output.append(begin.toString()+",");
   output.append(end.toString()+",");
   output.append(id.toString()+")");
   return output.toString();
 }


}
