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

  /** Language code used "aa" */
  public static final Locale AFAR = new Locale("aa");

  /** Language code used "ab" */
  public static final Locale ABKHAZIAN = new Locale("ab");

  /** Language code used "af" */
  public static final Locale AFRIKAANS = new Locale("af");

  /** Language code used "am" */
  public static final Locale AMHARIC = new Locale("am");

  /** Language code used "ar" */
  public static final Locale ARABIC = new Locale("ar");

  /** Language code used "as" */
  public static final Locale ASSAMESE = new Locale("as");

  /** Language code used "ay" */
  public static final Locale AYMARA = new Locale("ay");

  /** Language code used "az" */
  public static final Locale AZERBAIJANI = new Locale("az");

  /** Language code used "ba" */
  public static final Locale BASHKIR = new Locale("ba");

  /** Language code used "be" */
  public static final Locale BYELORUSSIAN = new Locale("be");

  /** Language code used "bg" */
  public static final Locale BULGARIAN = new Locale("bg");

  /** Language code used "bh" */
  public static final Locale BIHARI = new Locale("bh");

  /** Language code used "bi" */
  public static final Locale BISLAMA = new Locale("bi");

  /** Language code used "bn" */
  public static final Locale BENGALI = new Locale("bn");

  /** Language code used "bo" */
  public static final Locale TIBETAN = new Locale("bo");

  /** Language code used "br" */
  public static final Locale BRETON = new Locale("br");

  /** Language code used "ca" */
  public static final Locale CATALAN = new Locale("ca");

  /** Language code used "co" */
  public static final Locale CORSICAN = new Locale("co");

  /** Language code used "cs" */
  public static final Locale CZECH = new Locale("cs");

  /** Language code used "cy" */
  public static final Locale WELSH = new Locale("cy");

  /** Language code used "da" */
  public static final Locale DANISH = new Locale("da");

  /** Language code used "de" */
  public static final Locale GERMAN = new Locale("de");

  /** Language code used "dz" */
  public static final Locale BHUTANI = new Locale("dz");

  /** Language code used "el" */
  public static final Locale GREEK = new Locale("el");

  /** Language code used "en" */
  public static final Locale ENGLISH = new Locale("en");

  /** Language code used "eo" */
  public static final Locale ESPERANTO = new Locale("eo");

  /** Language code used "es" */
  public static final Locale SPANISH = new Locale("es");

  /** Language code used "et" */
  public static final Locale ESTONIAN = new Locale("et");

  /** Language code used "eu" */
  public static final Locale BASQUE = new Locale("eu");

  /** Language code used "fa" */
  public static final Locale PERSIAN = new Locale("fa");

  /** Language code used "fi" */
  public static final Locale FINNISH = new Locale("fi");

  /** Language code used "fj" */
  public static final Locale FIJI = new Locale("fj");

  /** Language code used "fo" */
  public static final Locale FAROESE = new Locale("fo");

  /** Language code used "fr" */
  public static final Locale FRENCH = new Locale("fr");

  /** Language code used "fy" */
  public static final Locale FRISIAN = new Locale("fy");

  /** Language code used "ga" */
  public static final Locale IRISH = new Locale("ga");

  /** Language code used "gd" */
  public static final Locale SCOTS = new Locale("gd");

  /** Language code used "gl" */
  public static final Locale GALICIAN = new Locale("gl");

  /** Language code used "gn" */
  public static final Locale GUARANI = new Locale("gn");

  /** Language code used "gu" */
  public static final Locale GUJARATI = new Locale("gu");

  /** Language code used "ha" */
  public static final Locale HAUSA = new Locale("ha");

  /** Language code used "he" */
  public static final Locale HEBREW = new Locale("he");

  /** Language code used "hi" */
  public static final Locale HINDI = new Locale("hi");

  /** Language code used "hr" */
  public static final Locale CROATIAN = new Locale("hr");

  /** Language code used "hu" */
  public static final Locale HUNGARIAN = new Locale("hu");

  /** Language code used "hy" */
  public static final Locale ARMENIAN = new Locale("hy");

  /** Language code used "ia" */
  public static final Locale INTERLINGUA = new Locale("ia");

  /** Language code used "id" */
  public static final Locale INDONESIAN = new Locale("id");

  /** Language code used "ie" */
  public static final Locale INTERLINGUE = new Locale("ie");

  /** Language code used "ik" */
  public static final Locale INUPIAK = new Locale("ik");

  /** Language code used "is" */
  public static final Locale ICELANDIC = new Locale("is");

  /** Language code used "it" */
  public static final Locale ITALIAN = new Locale("it");

  /** Language code used "iu" */
  public static final Locale INUKTITUT = new Locale("iu");

  /** Language code used "ja" */
  public static final Locale JAPANESE = new Locale("ja");

  /** Language code used "jw" */
  public static final Locale JAVANESE = new Locale("jw");

  /** Language code used "ka" */
  public static final Locale GEORGIAN = new Locale("ka");

  /** Language code used "kk" */
  public static final Locale KAZAKH = new Locale("kk");

  /** Language code used "kl" */
  public static final Locale GREENLANDIC = new Locale("kl");

  /** Language code used "km" */
  public static final Locale CAMBODIAN = new Locale("km");

  /** Language code used "kn" */
  public static final Locale KANNADA = new Locale("kn");

  /** Language code used "ko" */
  public static final Locale KOREAN = new Locale("ko");

  /** Language code used "ks" */
  public static final Locale KASHMIRI = new Locale("ks");

  /** Language code used "ku" */
  public static final Locale KURDISH = new Locale("ku");

  /** Language code used "ky" */
  public static final Locale KIRGHIZ = new Locale("ky");

  /** Language code used "la" */
  public static final Locale LATIN = new Locale("la");

  /** Language code used "ln" */
  public static final Locale LINGALA = new Locale("ln");

  /** Language code used "lo" */
  public static final Locale LAOTHIAN = new Locale("lo");

  /** Language code used "lt" */
  public static final Locale LITHUANIAN = new Locale("lt");

  /** Language code used "lv" */
  public static final Locale LATVIAN = new Locale("lv");

  /** Language code used "mg" */
  public static final Locale MALAGASY = new Locale("mg");

  /** Language code used "mi" */
  public static final Locale MAORI = new Locale("mi");

  /** Language code used "mk" */
  public static final Locale MACEDONIAN = new Locale("mk");

  /** Language code used "ml" */
  public static final Locale MALAYALAM = new Locale("ml");

  /** Language code used "mn" */
  public static final Locale MONGOLIAN = new Locale("mn");

  /** Language code used "mo" */
  public static final Locale MOLDAVIAN = new Locale("mo");

  /** Language code used "mr" */
  public static final Locale MARATHI = new Locale("mr");

  /** Language code used "ms" */
  public static final Locale MALAY = new Locale("ms");

  /** Language code used "mt" */
  public static final Locale MALTESE = new Locale("mt");

  /** Language code used "my" */
  public static final Locale BURMESE = new Locale("my");

  /** Language code used "na" */
  public static final Locale NAURU = new Locale("na");

  /** Language code used "ne" */
  public static final Locale NEPALI = new Locale("ne");

  /** Language code used "nl" */
  public static final Locale DUTCH = new Locale("nl");

  /** Language code used "no" */
  public static final Locale NORWEGIAN = new Locale("no");

  /** Language code used "oc" */
  public static final Locale OCCITAN = new Locale("oc");

  /** Language code used "om" */
  public static final Locale OROMO = new Locale("om");

  /** Language code used "or" */
  public static final Locale ORIYA = new Locale("or");

  /** Language code used "pa" */
  public static final Locale PUNJABI = new Locale("pa");

  /** Language code used "pl" */
  public static final Locale POLISH = new Locale("pl");

  /** Language code used "ps" */
  public static final Locale PASHTO = new Locale("ps");

  /** Language code used "pt" */
  public static final Locale PORTUGUESE = new Locale("pt");

  /** Language code used "qu" */
  public static final Locale QUECHUA = new Locale("qu");

  /** Language code used "rm" */
  public static final Locale RHAETO_ROMANCE = new Locale("rm");

  /** Language code used "rn" */
  public static final Locale KIRUNDI = new Locale("rn");

  /** Language code used "ro" */
  public static final Locale ROMANIAN = new Locale("ro");

  /** Language code used "ru" */
  public static final Locale RUSSIAN = new Locale("ru");

  /** Language code used "rw" */
  public static final Locale KINYARWANDA = new Locale("rw");

  /** Language code used "sa" */
  public static final Locale SANSKRIT = new Locale("sa");

  /** Language code used "sd" */
  public static final Locale SINDHI = new Locale("sd");

  /** Language code used "sg" */
  public static final Locale SANGHO = new Locale("sg");

  /** Language code used "sh" */
  public static final Locale SERBO_CROATIAN = new Locale("sh");

  /** Language code used "si" */
  public static final Locale SINHALESE = new Locale("si");

  /** Language code used "sk" */
  public static final Locale SLOVAK = new Locale("sk");

  /** Language code used "sl" */
  public static final Locale SLOVENIAN = new Locale("sl");

  /** Language code used "sm" */
  public static final Locale SAMOAN = new Locale("sm");

  /** Language code used "sn" */
  public static final Locale SHONA = new Locale("sn");

  /** Language code used "so" */
  public static final Locale SOMALI = new Locale("so");

  /** Language code used "sq" */
  public static final Locale ALBANIAN = new Locale("sq");

  /** Language code used "sr" */
  public static final Locale SERBIAN = new Locale("sr");

  /** Language code used "ss" */
  public static final Locale SISWATI = new Locale("ss");

  /** Language code used "st" */
  public static final Locale SESOTHO = new Locale("st");

  /** Language code used "su" */
  public static final Locale SUNDANESE = new Locale("su");

  /** Language code used "sv" */
  public static final Locale SWEDISH = new Locale("sv");

  /** Language code used "sw" */
  public static final Locale SWAHILI = new Locale("sw");

  /** Language code used "ta" */
  public static final Locale TAMIL = new Locale("ta");

  /** Language code used "te" */
  public static final Locale TELUGU = new Locale("te");

  /** Language code used "tg" */
  public static final Locale TAJIK = new Locale("tg");

  /** Language code used "th" */
  public static final Locale THAI = new Locale("th");

  /** Language code used "ti" */
  public static final Locale TIGRINYA = new Locale("ti");

  /** Language code used "tk" */
  public static final Locale TURKMEN = new Locale("tk");

  /** Language code used "tl" */
  public static final Locale TAGALOG = new Locale("tl");

  /** Language code used "tn" */
  public static final Locale SETSWANA = new Locale("tn");

  /** Language code used "to" */
  public static final Locale TONGA = new Locale("to");

  /** Language code used "tr" */
  public static final Locale TURKISH = new Locale("tr");

  /** Language code used "ts" */
  public static final Locale TSONGA = new Locale("ts");

  /** Language code used "tt" */
  public static final Locale TATAR = new Locale("tt");

  /** Language code used "tw" */
  public static final Locale TWI = new Locale("tw");

  /** Language code used "ug" */
  public static final Locale UIGHUR = new Locale("ug");

  /** Language code used "uk" */
  public static final Locale UKRAINIAN = new Locale("uk");

  /** Language code used "ur" */
  public static final Locale URDU = new Locale("ur");

  /** Language code used "uz" */
  public static final Locale UZBEK = new Locale("uz");

  /** Language code used "vi" */
  public static final Locale VIETNAMESE = new Locale("vi");

  /** Language code used "vo" */
  public static final Locale VOLAPUK = new Locale("vo");

  /** Language code used "wo" */
  public static final Locale WOLOF = new Locale("wo");

  /** Language code used "xh" */
  public static final Locale XHOSA = new Locale("xh");

  /** Language code used "yi" */
  public static final Locale YIDDISH = new Locale("yi");

  /** Language code used "yo" */
  public static final Locale YORUBA = new Locale("yo");

  /** Language code used "za" */
  public static final Locale ZHUANG = new Locale("za");

  /** Language code used "zh" */
  public static final Locale CHINESE = new Locale("zh");

  /** Language code used "zu" */
  public static final Locale ZULU = new Locale("zu");

}
