package shef.nlp.buchart.category;

import java.util.*;
import shef.nlp.buchart.BuChartRecord;

public class Edge
{
	public Long from;
	public Long to;
	public Category cat;
 /* to replace cat */
    public BuChartRecord record;
	public Long begin;
	public Long end;
	public Long id;
	public int level;
	public ArrayList cat_list;
	public int creator_ID;
	public ArrayList child_list;

	public Edge() { }

	public boolean equals(Object o)
	{
		if (!(o instanceof Edge)) return false;

		Edge e = (Edge)o;

		return begin.equals(e.begin) && end.equals(e.end) && cat.equals(e.cat);
	}
}
