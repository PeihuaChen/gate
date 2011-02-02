MetaMap 
=================================

MetaMap, from the National Library of Medicine (NLM), maps biomedical text to 
the UMLS Metathesaurus and allows Metathesaurus concepts to be discovered in a 
text corpus.

The MetaMap plugin for GATE wraps the MetaMap Java API client to allow GATE to 
communicate with a remote (or local) MetaMap PrologBeans mmserver and MetaMap 
distribution. This allows the content of specified annotations (or the entire 
document content) to be processed by MetaMap and the results converted to GATE 
annotations and features.

To use this plugin, you will need access to a remote MetaMap server, or install 
one locally by downloading and installing the complete distribution:

http://metamap.nlm.nih.gov/

and Java PrologBeans mmserver

http://metamap.nlm.nih.gov/README_javaapi.html



Parameters
==========

 - Initialisation-time
---------------------------

excludeSemanticTypes: list of MetaMap semantic types that should be excluded 
from the final output annotation set. Useful for reducing the number of generic 
annotations output (e.g. for qualitative and temporal concepts).

restrictSemanticTypes: list of MetaMap semantic types that should be the only 
types from which output annotations are created. E.g. if bpoc (Body Part Organ 
or Organ Component) is specified here, only MetaMap matches of these concepts 
will result in GATE annotations being created for these matches, and no others. 
Overrides the excludeSemanticTypes parameter.

mmServerHost: name or IP address of the server on which the MetaMap terminology 
server (skrmedpostctl), disambiguation server (wsdserverctl) and API 
PrologServer (mmserver09) are running. Default is localhost.

NB: all three need to be on the same server, unless you wish to recompile 
mmserver09 from source and add functionality to point to different terminology 
and word-sense disambiguation server locations.

mmServerPort: port number of the mmserver09 PrologServer running on 
mmServerHost. Default is 8066

mmServerTimeout: milliseconds to wait for PrologServer. Default is 150000

outputASType: output annotation name to be used for all MetaMap annotations


- Run-time
----------------
annotatePhrases: set to true to output MetaMap phrase-level annotations (generally noun-phrase chunks). Only phrases containing a MetaMap mapping will be annotated. Can be useful for post-coordination of phrase-level terms that do not exist in a pre-coordinated form in UMLS.

inputASName: input Annotation Set name. Use in conjunction with inputASTypes 
(see below). Unless specified, the entire document content will be sent to 
MetaMap. 

inputASTypes: only send the content of the named annotations listed here, within
inputASName, to MetaMap. Unless specified, the entire document content will be 
sent to MetaMap.

metaMapOptions: set parameter-less MetaMap options here. Default is -Xt 
(truncate Candidates mappings and do not use full text parsing). See 
http://metamap.nlm.nih.gov/README_javaapi.html for more details. NB: only set the -y parameter (word-sense disambiguation) if wsdserverctl is 
running. Running the Java MetaMap API with a large corpus with word-sense 
disambiguation can cause a 'too many open files' error - this appears to be a PrologServer bug.

outputASName: output Annotation Set name.

outputMode: determines which mappings are output as annotations in the GATE 
document:
- MappingsOnly: only annotate the final MetaMap Mappings. This will result in 
  fewer annotations with higher precision (e.g. for 'lung cancer' only the 
  complete phrase will be annotated as neop)
- CandidatesOnly: annotate only Candidate mappings and not the final Mappings. 
  This will result in more annotations with less precision (e.g. for 'lung 
  cancer' both 'lung' (bpoc) and 'lung cancer' (neop) will be annotated).
- CandidatesAndMappings: annotate both Candidate and final mappings. This will 
  usually result in multiple, overlapping annotations for each term/phrase

scoreThreshold: set from 0 to 1000. The lower the threshold, the greater the 
number of annotations but the lower precision. Default is 500.

useNegEx: set this to true to add NegEx features to annotations (NegExType and 
NegExTrigger). See http://www.dbmi.pitt.edu/chapman/NegEx.html for more 
information on NegEx
