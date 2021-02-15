package org.veo.fileconverter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ConversionHandler {

    String getInputType();

    String getOutputType();

    void convert(InputStream input, OutputStream output) throws IOException;

}
