package gate.util.web;

import org.apache.regexp.*;
import java.util.*;

public class TagHighlighter {

    private HashMap tagColors;

    public TagHighlighter () {
        tagColors = new HashMap();
        tagColors.put("Person", "#FFA0FF");
        tagColors.put("Location", "#A0FFFF");
        tagColors.put("Organization", "#FFFFA0");
    }

    public void colorTag(String tag, String color) {
        tagColors.put(tag, color);
    }

    public String getColor(String tag) {
        return (String) tagColors.get(tag);
    }

    public String highlightText(String text) {
        Iterator tags = tagColors.keySet().iterator();
        while (tags.hasNext()) {
            String tag = (String) tags.next();
            String color = (String) tagColors.get(tag);

            try {
                RE r = new RE("(<" + tag + " .*?>)");
                if (r.match(text)) {
                    text = r.subst(text,
                                   "<B style=\"color:black;background-color:" +
                                   color +
                                   "\">" + r.getParen(1));
                }  
                
                r = new RE("(</" + tag + ">)");
                if (r.match(text)) {
                    text = r.subst(text, r.getParen(1) + "</B>");
                }
            } catch (RESyntaxException rese) {
                // log something, I guess
            }
        }

        return text;
    }
}
