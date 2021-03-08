/**
 * Copyright (c) 2021 Jochen Kemnade.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.veo.fileconverter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.veo.fileconverter.handlers.HtmlPDFConverter;
import org.veo.fileconverter.handlers.MarkdownHtmlConverter;

public class FileConverterImpl implements FileConverter {

    private final Map<String, Map<String, ConversionHandler>> handlerRegistry = new ConcurrentHashMap<>();

    public FileConverterImpl() {
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

    /*
     * @see org.veo.fileconverter.FileConverter#convert(java.io.InputStream,
     * java.lang.String, java.io.OutputStream, java.lang.String)
     */
    @Override
    public void convert(InputStream input, String inputType, OutputStream output, String outputType)
            throws IOException {
        if (inputType.equals(outputType)) {
            input.transferTo(output);
        } else {
            ConversionHandler converter = getHandler(inputType, outputType);
            if (converter == null) {
                throw new IllegalArgumentException(
                        "Cannot convert " + inputType + " to " + outputType);
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
