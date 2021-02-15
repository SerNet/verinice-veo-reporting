package org.veo.templating.converters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class ComposedConversionHandler implements ConversionHandler {

    private final ConversionHandler firstHandler;
    private final ConversionHandler secondHandler;
    private final String inputType;
    private final String outputType;

    public ComposedConversionHandler(ConversionHandler firstHandler,
            ConversionHandler secondHandler) {
        this.firstHandler = firstHandler;
        this.secondHandler = secondHandler;
        this.inputType = firstHandler.getInputType();
        this.outputType = secondHandler.getOutputType();
    }

    @Override
    public String getInputType() {
        return inputType;
    }

    @Override
    public String getOutputType() {
        return outputType;
    }

    @Override
    public void convert(InputStream input, OutputStream output) throws IOException {

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            firstHandler.convert(input, baos);
            var bytes = baos.toByteArray();
            try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
                secondHandler.convert(bais, output);
            }
        }
    }
}