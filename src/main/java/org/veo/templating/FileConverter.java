package org.veo.templating;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface FileConverter {

    String getInputType();

    String getOutputType();

    void convert(InputStream input, OutputStream output) throws IOException;

}
