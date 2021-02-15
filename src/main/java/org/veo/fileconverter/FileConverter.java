package org.veo.fileconverter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface FileConverter {

    void convert(InputStream input, String inputType, OutputStream output, String outputType)
            throws IOException;

}