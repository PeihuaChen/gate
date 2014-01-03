/*
 *  DocumentJsonUtils.java
 *
 *  Copyright (c) 1995-2012, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Ian Roberts, 20/Dec/2013
 *
 *  $Id$
 */
package gate.corpora;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import gate.Annotation;
import gate.Document;
import gate.util.GateRuntimeException;
import gate.util.InvalidOffsetException;

/**
 * This class contains utility methods to output GATE documents in a
 * JSON format which is (deliberately) close to the format used by
 * Twitter to represent entities such as user mentions and hashtags in
 * Tweets.
 * 
 * @author ian
 * 
 */
public class DocumentJsonUtils {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  /**
   * Write a GATE document to the specified JsonGenerator. The document
   * text will be written as a property named "text" and the specified
   * annotations will be written as "entities".
   * 
   * @param doc the document to write
   * @param annotationsMap annotations to write.
   * @param json the {@link JsonGenerator} to write to.
   * @throws JsonGenerationException if a problem occurs while
   *           generating the JSON
   * @throws IOException if an I/O error occurs.
   */
  public static void writeDocument(Document doc,
          Map<String, Collection<Annotation>> annotationsMap, JsonGenerator json)
          throws JsonGenerationException, IOException {
    try {
      writeDocument(doc, 0L, doc.getContent().size(), annotationsMap, json);
    } catch(InvalidOffsetException e) {
      // shouldn't happen
      throw new GateRuntimeException(
              "Got invalid offset exception when passing "
                      + "offsets that are known to be valid");
    }
  }

  /**
   * Write a substring of a GATE document to the specified
   * JsonGenerator. The specified window of document text will be
   * written as a property named "text" and the specified annotations
   * will be written as "entities", with their offsets adjusted to be
   * relative to the specified window.
   * 
   * @param doc the document to write
   * @param start the start offset of the segment to write
   * @param end the end offset of the segment to write
   * @param annotationsMap annotations to write.
   * @param json the {@link JsonGenerator} to write to.
   * @throws JsonGenerationException if a problem occurs while
   *           generating the JSON
   * @throws IOException if an I/O error occurs.
   */
  public static void writeDocument(Document doc, Long start, Long end,
          Map<String, Collection<Annotation>> annotationsMap, JsonGenerator json)
          throws JsonGenerationException, IOException, InvalidOffsetException {
    writeDocument(doc, start, end, annotationsMap, null, json);
  }

  /**
   * Write a substring of a GATE document to the specified
   * JsonGenerator. The specified window of document text will be
   * written as a property named "text" and the specified annotations
   * will be written as "entities", with their offsets adjusted to be
   * relative to the specified window.
   * 
   * @param doc the document to write
   * @param start the start offset of the segment to write
   * @param end the end offset of the segment to write
   * @param annotations annotations to write.
   * @param extraFeatures additional properties to add to the generated
   *          JSON. If the map includes a "text" key this will be
   *          ignored, and if it contains a key "entities" whose value
   *          is a map then these entities will be merged with the
   *          generated ones derived from the annotationsMap. This would
   *          typically be used for documents that were originally
   *          derived from Twitter data, to re-create the original JSON.
   * @param json the {@link JsonGenerator} to write to.
   * @throws JsonGenerationException if a problem occurs while
   *           generating the JSON
   * @throws IOException if an I/O error occurs.
   */
  public static void writeDocument(Document doc, Long start, Long end,
          Map<String, Collection<Annotation>> annotationsMap,
          Map<?, ?> extraFeatures, JsonGenerator json)
          throws JsonGenerationException, IOException, InvalidOffsetException {

    ObjectWriter writer = MAPPER.writer();

    json.writeStartObject();
    json.writeStringField("text", doc.getContent().getContent(start, end)
            .toString());
    json.writeFieldName("entities");
    json.writeStartObject();
    // if the extraFeatures already includes entities, merge them with
    // the new ones we create
    Object entitiesExtraFeature = extraFeatures.get("entities");
    Map<?, ?> entitiesMap = null;
    if(entitiesExtraFeature instanceof Map) {
      entitiesMap = (Map<?, ?>)entitiesExtraFeature;
    }
    for(Map.Entry<String, Collection<Annotation>> annsByType : annotationsMap
            .entrySet()) {
      String annotationType = annsByType.getKey();
      Collection<Annotation> annotations = annsByType.getValue();
      json.writeFieldName(annotationType);
      json.writeStartArray();
      for(Annotation a : annotations) {
        json.writeStartObject();
        // indices:[start, end], corrected to match the sub-range of
        // text we're writing
        json.writeArrayFieldStart("indices");
        json.writeNumber(a.getStartNode().getOffset() - start);
        json.writeNumber(a.getEndNode().getOffset() - start);
        json.writeEndArray(); // end of indices
        // other features
        for(Map.Entry<?, ?> feature : a.getFeatures().entrySet()) {
          json.writeFieldName(String.valueOf(feature.getKey()));
          writer.writeValue(json, feature.getValue());
        }
        json.writeEndObject(); // end of annotation
      }
      // add any entities from the extraFeatures map
      if(entitiesMap != null
              && entitiesMap.get(annotationType) instanceof Collection) {
        for(Object ent : (Collection<?>)entitiesMap.get(annotationType)) {
          writer.writeValue(json, ent);
        }
      }
      json.writeEndArray();
    }
    if(entitiesMap != null) {
      for(Map.Entry<?, ?> entitiesEntry : entitiesMap.entrySet()) {
        if(!annotationsMap.containsKey(entitiesEntry.getKey())) {
          // not an entity type we've already seen
          json.writeFieldName(String.valueOf(entitiesEntry.getKey()));
          writer.writeValue(json, entitiesEntry.getValue());
        }
      }
    }

    json.writeEndObject(); // end of entities

    if(extraFeatures != null) {
      for(Map.Entry<?, ?> feature : extraFeatures.entrySet()) {
        if("text".equals(feature.getKey())
                || "entities".equals(feature.getKey())) {
          // already dealt with text and entities
          continue;
        }
        json.writeFieldName(String.valueOf(feature.getKey()));
        writer.writeValue(json, feature.getValue());
      }
    }
    json.writeEndObject(); // end of document
  }
}
