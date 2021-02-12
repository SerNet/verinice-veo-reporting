package org.veo.templating

import groovy.transform.CompileStatic

import org.veo.templating.converters.ConversionHandler
import org.veo.templating.converters.HtmlPDFConverter
import org.veo.templating.converters.MarkdownHtmlConverter

@CompileStatic
public class FileConverter {

    Map<String, Map<String, ConversionHandler>> handlerRegistry = [:]


    public FileConverter(){
        addHandlers(MarkdownHtmlConverter, HtmlPDFConverter)
    }

    private void addHandlers(Class[] handlerClasses) {
        handlerClasses.each {
            addHandler(it)
        }
    }

    private void addHandler(Class<ConversionHandler> handlerClass) {
        addHandler(handlerClass.getDeclaredConstructor().newInstance())
    }

    private void addHandler(ConversionHandler  converter) {
        def inputType = converter.inputType
        def outputType = converter.outputType

        def entry = handlerRegistry.computeIfAbsent(inputType ,{
            [:]
        }).put(outputType, converter)
        if (entry) {
            throw new Exception("Conflicting converters found for $inputType -> $outputType")
        }
    }

    public void convert(InputStream input, String inputType,
            OutputStream output, String outputType) {
        if (inputType == outputType) {
            output << input
        } else {
            ConversionHandler converter = getHandler(inputType, outputType)
            if (!converter) {
                throw new IllegalArgumentException("Cannot convert $inputType to $outputType")
            }
            converter.convert(input, output)
        }
    }

    ConversionHandler getHandler(String inputType, String outputType) {
        Map<String, ConversionHandler> handlersFromInputType = handlerRegistry.getOrDefault(inputType, [:])
        ConversionHandler handler = handlersFromInputType.get(outputType)
        if (handler) {
            return handler
        }
        // try a two-step conversion
        // TODO: add recursion?
        for (def entry:handlersFromInputType.entrySet() ) {
            String targetType = entry.key
            ConversionHandler intermediateHandler = entry.value
            ConversionHandler targetHandler = handlerRegistry.getOrDefault(intermediateHandler.getOutputType(), [:]).get(outputType)
            if (targetHandler) {
                ConversionHandler combinedHandler = new ConversionHandler() {

                            @Override
                            public String getInputType() {
                                return inputType
                            }

                            @Override
                            public String getOutputType() {
                                return outputType
                            }

                            @Override
                            public void convert(InputStream input, OutputStream output)
                            throws IOException {
                                def bytes = new ByteArrayOutputStream().withCloseable {
                                    intermediateHandler.convert(input, it)
                                    it.toByteArray()
                                }
                                ByteArrayInputStream bais = new ByteArrayInputStream(bytes)
                                targetHandler.convert(bais, output)
                            }
                        }
                handlersFromInputType.put(outputType, combinedHandler)
                return combinedHandler
            }
        }
    }
}
