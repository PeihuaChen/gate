package debugger.resources.lr;

import java.util.Collection;

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/) <br>
 * @author Andrey Shafirin, Vladimir Karasev
 */

public class CorpusModel {
    private Collection lrModels;
    private String name;

    public CorpusModel(Collection documents, String name) {
        this.lrModels = documents;
        this.name = name;
    }

    public Collection getLrModels() {
        return lrModels;
    }

    public String toString() {
        return name;
    }
}
