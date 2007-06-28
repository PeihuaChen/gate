package debugger.gui.resources;

import debugger.resources.ResourcesFactory;
import gate.*;
import gate.event.CorpusEvent;
import gate.event.CorpusListener;
import gate.event.CreoleEvent;
import gate.event.CreoleListener;

import java.util.Iterator;

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/) <br>
 * This class contains methods that update resources tree
 * after user has loaded/unloaded/renamed language or processing
 * resources in GATE.
 * @author Oleg Mishchenko, Andrey Shafirin
 */

public class CreoleListenerImpl implements CreoleListener, CorpusListener {
    private static CreoleListenerImpl ourInstance;
    private static ResourceTree resourceTree;

    public synchronized static CreoleListenerImpl getInstance(ResourceTree rt) {
        if (ourInstance == null) {
            ourInstance = new CreoleListenerImpl();
            resourceTree = rt;
        }
        return ourInstance;
    }

    private CreoleListenerImpl() {
        // we should add corpus listener to all corpuses already loaded in GATE
        for (Iterator<LanguageResource> it = Gate.getCreoleRegister().getLrInstances().iterator(); it.hasNext();) {
            LanguageResource lr = it.next();
            if (lr instanceof Corpus) {
                ((Corpus) lr).addCorpusListener(this);
            }
        }
    }

    // implementation of CreoleListener methods
    public void resourceLoaded(CreoleEvent event) {
        // we don't see document in the tree if it is not added to any corpus,
        // so on the creation of a document nothing is done
        if (event.getResource() instanceof ProcessingResource || event.getResource() instanceof Corpus) {
            ResourcesFactory.updateRoots();
            resourceTree.refresh();
            if (event.getResource() instanceof Corpus) {
                ((Corpus) event.getResource()).addCorpusListener(this);
            }
        }
    }

    public void resourceUnloaded(CreoleEvent event) {
        if (event.getResource() instanceof ProcessingResource || event.getResource() instanceof LanguageResource) {
            ResourcesFactory.updateRoots();
            resourceTree.refresh();
        }
    }

    public void datastoreOpened(CreoleEvent event) {
    }

    public void datastoreCreated(CreoleEvent event) {
    }

    public void datastoreClosed(CreoleEvent event) {
    }

    public void resourceRenamed(Resource resource, String s, String s1) {
        if (resource instanceof ProcessingResource || resource instanceof LanguageResource) {
            ResourcesFactory.updateRoots();
            resourceTree.refresh();
        }
    }

    // implementation of CorpusListener methods
    public void documentAdded(CorpusEvent event) {
        ResourcesFactory.updateRoots();
        resourceTree.refresh();
    }

    public void documentRemoved(CorpusEvent event) {
        ResourcesFactory.updateRoots();
        resourceTree.refresh();
    }
}

