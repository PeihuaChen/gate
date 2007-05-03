/*
 *  OConstants.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: OConstants.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
 */
package gate.creole.ontology;

import java.util.Locale;

/**
 * This interface holds some constants used by several other intrfaces
 * and classes in the GATE ontology API.
 * 
 * @author Niraj Aswani
 */
public interface OConstants {
  /** denotes a direct closure(no transitivity) */
  public static final byte DIRECT_CLOSURE = 0;

  /** denotes atransitive closure */
  public static final byte TRANSITIVE_CLOSURE = 1;

  /**
   * denotes the rdf property
   */
  public static final byte RDF_PROPERTY = 0;

  /**
   * denotes the object property.
   */
  public static final byte OBJECT_PROPERTY = 1;

  /**
   * denotes the datatype property.
   */
  public static final byte DATATYPE_PROPERTY = 2;

  /**
   * denotes the symmetric property.
   */
  public static final byte SYMMETRIC_PROPERTY = 3;

  /**
   * denotes the transitive property.
   */
  public static final byte TRANSITIVE_PROPERTY = 4;

  /**
   * denotes the annotation property.
   */
  public static final byte ANNOTATION_PROPERTY = 5;

  /**
   * denotes the N3 ontology format
   */
  public static final byte ONTOLOGY_FORMAT_N3 = 0;

  /**
   * denotes the NTRIPLES ontology format
   */
  public static final byte ONTOLOGY_FORMAT_NTRIPLES = 1;

  /**
   * denotes the RDFXML ontology format
   */
  public static final byte ONTOLOGY_FORMAT_RDFXML = 2;

  /**
   * denotes the TURTLE ontology format
   */
  public static final byte ONTOLOGY_FORMAT_TURTLE = 3;

  /**
   * Name of the anonymouse class
   */
  public static final String ANONYMOUS_CLASS_NAME = "Anonymous";

  /**
   * denotes the addition of sub class event
   */
  public static final int SUB_CLASS_ADDED_EVENT = 0;

  /**
   * denotes the removal of sub class event
   */
  public static final int SUB_CLASS_REMOVED_EVENT = 2;

  /**
   * denotes the event of two classes set as equivalent
   */
  public static final int EQUIVALENT_CLASS_EVENT = 4;

  /**
   * denotes the event when a comment on a resource is changed
   */
  public static final int COMMENT_CHANGED_EVENT = 5;

  /**
   * denotes the event when a label on a resource is changed
   */
  public static final int LABEL_CHANGED_EVENT = 6;

  /**
   * denotes the event when an annotation property is assigned to a
   * resource with some compatible value
   */
  public static final int ANNOTATION_PROPERTY_VALUE_ADDED_EVENT = 7;

  /**
   * denotes the event when a datatype property is assigned to a
   * resource with some compatible value
   */
  public static final int DATATYPE_PROPERTY_VALUE_ADDED_EVENT = 8;

  /**
   * denotes the event when an object property is assigned to a resource
   * with some compatible value
   */
  public static final int OBJECT_PROPERTY_VALUE_ADDED_EVENT = 9;

  /**
   * denotes the event when an rdf property is assigned to a resource
   * with some compatible value
   */
  public static final int RDF_PROPERTY_VALUE_ADDED_EVENT = 10;

  /**
   * denotes the event when an annotation property value is removed from
   * the resource
   */
  public static final int ANNOTATION_PROPERTY_VALUE_REMOVED_EVENT = 11;

  /**
   * denotes the event when a datatype property value is removed from
   * the resource
   */
  public static final int DATATYPE_PROPERTY_VALUE_REMOVED_EVENT = 12;

  /**
   * denotes the event when an object property value is removed from the
   * resource
   */
  public static final int OBJECT_PROPERTY_VALUE_REMOVED_EVENT = 13;

  /**
   * denotes the event when an rdf property value is removed from the
   * resource
   */
  public static final int RDF_PROPERTY_VALUE_REMOVED_EVENT = 14;

  /**
   * denotes the event when two instances are set to be different from
   * each other
   */
  public static final int DIFFERENT_INSTANCE_EVENT = 15;

  /**
   * denotes the event when two instances are set to be same instances
   */
  public static final int SAME_INSTANCE_EVENT = 16;

  /**
   * denotes the event when two properties are set to be equivalent
   */
  public static final int EQUIVALENT_PROPERTY_EVENT = 17;

  /**
   * denotes the event when a sub property is added to an existing
   * property
   */
  public static final int SUB_PROPERTY_ADDED_EVENT = 18;

  /**
   * denotes the event when a sub property is removed from an existing
   * property
   */
  public static final int SUB_PROPERTY_REMOVED_EVENT = 20;

  /** Constants for different language locals */
  public static final Locale AFAR = new Locale("aa");
  public static final Locale ABKHAZIAN = new Locale("ab");
  public static final Locale AFRIKAANS = new Locale("af");
  public static final Locale AMHARIC = new Locale("am");
  public static final Locale ARABIC = new Locale("ar");
  public static final Locale ASSAMESE = new Locale("as");
  public static final Locale AYMARA = new Locale("ay");
  public static final Locale AZERBAIJANI = new Locale("az");
  public static final Locale BASHKIR = new Locale("ba");
  public static final Locale BYELORUSSIAN = new Locale("be");
  public static final Locale BULGARIAN = new Locale("bg");
  public static final Locale BIHARI = new Locale("bh");
  public static final Locale BISLAMA = new Locale("bi");
  public static final Locale BENGALI = new Locale("bn");
  public static final Locale TIBETAN = new Locale("bo");
  public static final Locale BRETON = new Locale("br");
  public static final Locale CATALAN = new Locale("ca");
  public static final Locale CORSICAN = new Locale("co");
  public static final Locale CZECH = new Locale("cs");
  public static final Locale WELSH = new Locale("cy");
  public static final Locale DANISH = new Locale("da");
  public static final Locale GERMAN = new Locale("de");
  public static final Locale BHUTANI = new Locale("dz");
  public static final Locale GREEK = new Locale("el");
  public static final Locale ENGLISH = new Locale("en");
  public static final Locale ESPERANTO = new Locale("eo");
  public static final Locale SPANISH = new Locale("es");
  public static final Locale ESTONIAN = new Locale("et");
  public static final Locale BASQUE = new Locale("eu");
  public static final Locale PERSIAN = new Locale("fa");
  public static final Locale FINNISH = new Locale("fi");
  public static final Locale FIJI = new Locale("fj");
  public static final Locale FAROESE = new Locale("fo");
  public static final Locale FRENCH = new Locale("fr");
  public static final Locale FRISIAN = new Locale("fy");
  public static final Locale IRISH = new Locale("ga");
  public static final Locale SCOTS = new Locale("gd");
  public static final Locale GALICIAN = new Locale("gl");
  public static final Locale GUARANI = new Locale("gn");
  public static final Locale GUJARATI = new Locale("gu");
  public static final Locale HAUSA = new Locale("ha");
  public static final Locale HEBREW = new Locale("he");
  public static final Locale HINDI = new Locale("hi");
  public static final Locale CROATIAN = new Locale("hr");
  public static final Locale HUNGARIAN = new Locale("hu");
  public static final Locale ARMENIAN = new Locale("hy");
  public static final Locale INTERLINGUA = new Locale("ia");
  public static final Locale INDONESIAN = new Locale("id");
  public static final Locale INTERLINGUE = new Locale("ie");
  public static final Locale INUPIAK = new Locale("ik");
  public static final Locale ICELANDIC = new Locale("is");
  public static final Locale ITALIAN = new Locale("it");
  public static final Locale INUKTITUT = new Locale("iu");
  public static final Locale JAPANESE = new Locale("ja");
  public static final Locale JAVANESE = new Locale("jw");
  public static final Locale GEORGIAN = new Locale("ka");
  public static final Locale KAZAKH = new Locale("kk");
  public static final Locale GREENLANDIC = new Locale("kl");
  public static final Locale CAMBODIAN = new Locale("km");
  public static final Locale KANNADA = new Locale("kn");
  public static final Locale KOREAN = new Locale("ko");
  public static final Locale KASHMIRI = new Locale("ks");
  public static final Locale KURDISH = new Locale("ku");
  public static final Locale KIRGHIZ = new Locale("ky");
  public static final Locale LATIN = new Locale("la");
  public static final Locale LINGALA = new Locale("ln");
  public static final Locale LAOTHIAN = new Locale("lo");
  public static final Locale LITHUANIAN = new Locale("lt");
  public static final Locale LATVIAN = new Locale("lv");
  public static final Locale MALAGASY = new Locale("mg");
  public static final Locale MAORI = new Locale("mi");
  public static final Locale MACEDONIAN = new Locale("mk");
  public static final Locale MALAYALAM = new Locale("ml");
  public static final Locale MONGOLIAN = new Locale("mn");
  public static final Locale MOLDAVIAN = new Locale("mo");
  public static final Locale MARATHI = new Locale("mr");
  public static final Locale MALAY = new Locale("ms");
  public static final Locale MALTESE = new Locale("mt");
  public static final Locale BURMESE = new Locale("my");
  public static final Locale NAURU = new Locale("na");
  public static final Locale NEPALI = new Locale("ne");
  public static final Locale DUTCH = new Locale("nl");
  public static final Locale NORWEGIAN = new Locale("no");
  public static final Locale OCCITAN = new Locale("oc");
  public static final Locale OROMO = new Locale("om");
  public static final Locale ORIYA = new Locale("or");
  public static final Locale PUNJABI = new Locale("pa");
  public static final Locale POLISH = new Locale("pl");
  public static final Locale PASHTO = new Locale("ps");
  public static final Locale PORTUGUESE = new Locale("pt");
  public static final Locale QUECHUA = new Locale("qu");
  public static final Locale RHAETO_ROMANCE = new Locale("rm");
  public static final Locale KIRUNDI = new Locale("rn");
  public static final Locale ROMANIAN = new Locale("ro");
  public static final Locale RUSSIAN = new Locale("ru");
  public static final Locale KINYARWANDA = new Locale("rw");
  public static final Locale SANSKRIT = new Locale("sa");
  public static final Locale SINDHI = new Locale("sd");
  public static final Locale SANGHO = new Locale("sg");
  public static final Locale SERBO_CROATIAN = new Locale("sh");
  public static final Locale SINHALESE = new Locale("si");
  public static final Locale SLOVAK = new Locale("sk");
  public static final Locale SLOVENIAN = new Locale("sl");
  public static final Locale SAMOAN = new Locale("sm");
  public static final Locale SHONA = new Locale("sn");
  public static final Locale SOMALI = new Locale("so");
  public static final Locale ALBANIAN = new Locale("sq");
  public static final Locale SERBIAN = new Locale("sr");
  public static final Locale SISWATI = new Locale("ss");
  public static final Locale SESOTHO = new Locale("st");
  public static final Locale SUNDANESE = new Locale("su");
  public static final Locale SWEDISH = new Locale("sv");
  public static final Locale SWAHILI = new Locale("sw");
  public static final Locale TAMIL = new Locale("ta");
  public static final Locale TELUGU = new Locale("te");
  public static final Locale TAJIK = new Locale("tg");
  public static final Locale THAI = new Locale("th");
  public static final Locale TIGRINYA = new Locale("ti");
  public static final Locale TURKMEN = new Locale("tk");
  public static final Locale TAGALOG = new Locale("tl");
  public static final Locale SETSWANA = new Locale("tn");
  public static final Locale TONGA = new Locale("to");
  public static final Locale TURKISH = new Locale("tr");
  public static final Locale TSONGA = new Locale("ts");
  public static final Locale TATAR = new Locale("tt");
  public static final Locale TWI = new Locale("tw");
  public static final Locale UIGHUR = new Locale("ug");
  public static final Locale UKRAINIAN = new Locale("uk");
  public static final Locale URDU = new Locale("ur");
  public static final Locale UZBEK = new Locale("uz");
  public static final Locale VIETNAMESE = new Locale("vi");
  public static final Locale VOLAPUK = new Locale("vo");
  public static final Locale WOLOF = new Locale("wo");
  public static final Locale XHOSA = new Locale("xh");
  public static final Locale YIDDISH = new Locale("yi");
  public static final Locale YORUBA = new Locale("yo");
  public static final Locale ZHUANG = new Locale("za");
  public static final Locale CHINESE = new Locale("zh");
  public static final Locale ZULU = new Locale("zu");

  
  
  
}
