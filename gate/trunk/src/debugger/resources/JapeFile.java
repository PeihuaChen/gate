package debugger.resources;

import java.io.*;
import java.net.URL;

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/) <br>
 * @author Andrey Shafirin
 */

public class JapeFile {
    private URL url;
    private File file;
    //String encoding;

    public JapeFile(URL url) {
        this.url = url;
        //this.encoding = encoding;
        if (this.url.getFile().indexOf('!') == -1) {
            // avaliable as File
            file = new File(this.url.getFile());
        }
    }

    public Reader getReader() throws IOException {
        if (null != file) {
            return new FileReader(file);
        } else {
            return new InputStreamReader(url.openStream());
        }
    }

    public File getFile() {
        return file;
    }

    public boolean canWrite() {
        return (null != file) ? file.canWrite() : false;
    }
}
