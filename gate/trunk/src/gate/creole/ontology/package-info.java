/**
 * The GATE ontology API.
 * This package contains all the interfaces, classes and constants for
 * using an ontology language resource and the objects that can be
 * added or retrieved from an ontology language resource.
 * <p>
 * The actual implementation for this API is available in the form of two
 * plugins:
 * <ul>
 * <li>the <a href="../../../../../plugins/Ontology_OWLIM2/doc/javadoc/index.html" target="_parent">Ontology_OWLIM2</a>
 * plugin:
 * this plugin contains the implementation
 * that was part of the GATE core previous to version 5.1. This implementation
 * is deprecated and exists for backwards-compatility and support of
 * existing applications only. New applications should never make use
 * of this implementation. This implementation provides the
 * {@link gate.creole.ontology.owlim.OWLIMOntologyLR} ontology language
 * resource.
 * <li>the <a href="../../../../../plugins/Ontology/doc/javadoc/index.html" target="_parent">Ontology</a>
 * plugin: this plugin contains the maintained implementation
 * which does not provide some of the methods and objects that were present
 * in the old implementation any more and provides support for a number of
 * new methods and features (see below). This implementation provides the
 * following language resources: {@link gate.creole.ontology.impl.sesame.OWLIMOntology},
 * {@link gate.creole.ontology.impl.sesame.CreateSesameOntology}, and
 * {@link gate.creole.ontology.impl.sesame.ConnectSesameOntology}
 * </ul>
 *
 * The documentation for all the classes and interfaces in this package
 * refers to the intended behavior of the maintained implementation in plugin
 * <a href="../../../../../plugins/Ontology/doc/javadoc/index.html" target="_parent">Ontology</a>
 * by default.
 * <p>
 * Which implementation your code will run with simply depends  on
 * which plugin is loaded prior to when your code is run.
 * To programmatically load the new implementation plugin, use the following
 * code:
 * <pre>
 * File pluginHome = new File(new File(Gate.getGateHome(), "plugins"), "Ontology");
 * Gate.getCreoleRegister().registerDirectories(pluginHome.toURI().toURL());
 * </pre>
 *
 *
 */
@javax.xml.bind.annotation.XmlSchema(
    namespace = "http://gate.ac.uk/ns/ontology"
)
package gate.creole.ontology;
