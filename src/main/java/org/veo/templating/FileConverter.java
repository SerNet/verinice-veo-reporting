package org.veo.templating;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.veo.templating.converters.ComposedConversionHandler;
import org.veo.templating.converters.ConversionHandler;
import org.veo.templating.converters.HtmlPDFConverter;
import org.veo.templating.converters.MarkdownHtmlConverter;

public class FileConverter {

    private final Map<String, Map<String, ConversionHandler>> handlerRegistry = new ConcurrentHashMap<>();

    public FileConverter() {
        addHandler(new MarkdownHtmlConverter());
        addHandler(new HtmlPDFConverter());
    }

    private void addHandler(ConversionHandler converter) {
        String inputType = converter.getInputType();
        String outputType = converter.getOutputType();

        ConversionHandler entry = handlerRegistry
                .computeIfAbsent(inputType, key -> new ConcurrentHashMap<>())
                .put(outputType, converter);
        if (entry != null) {
            throw new RuntimeException(
                    "Conflicting converters found for " + inputType + " -> " + outputType);
        }
    }

    public void convert(InputStream input, String inputType, OutputStream output, String outputType)
            throws IOException {
        if (inputType.equals(outputType)) {
            input.transferTo(output);
        } else {
            ConversionHandler converter = getHandler(inputType, outputType);
            if (converter == null) {
                throw new IllegalArgumentException(
                        "Cannot convert " + inputType + " to $outputType");
            }
            converter.convert(input, output);
        }
    }

    private ConversionHandler getHandler(String inputType, String outputType) {
        Map<String, ConversionHandler> handlersFromInputType = handlerRegistry
                .computeIfAbsent(inputType, key -> new ConcurrentHashMap<>());
        ConversionHandler handler = handlersFromInputType.get(outputType);
        if (handler != null) {
            return handler;
        }
        // try a two-step conversion
        // TODO: add recursion?
        for (var entry : handlersFromInputType.entrySet()) {
            String targetType = entry.getKey();
            ConversionHandler intermediateHandler = entry.getValue();
            ConversionHandler targetHandler = handlerRegistry
                    .computeIfAbsent(targetType, key -> new ConcurrentHashMap<>()).get(outputType);
            if (targetHandler != null) {
                ConversionHandler combinedHandler = new ComposedConversionHandler(
                        intermediateHandler, targetHandler);
                handlersFromInputType.put(outputType, combinedHandler);
                return combinedHandler;
            }
        }
        return null;
    }

}
