package org.veo.templating

import java.nio.file.Files

import org.veo.templating.converters.HtmlPDFConverter
import org.veo.templating.converters.MarkdownHtmlConverter

public class ReportEngine {

    Map<String, Map<String, FileConverter>> converterRegistry = [:]


    public ReportEngine(){
        addConverters(MarkdownHtmlConverter, HtmlPDFConverter)
    }

    private void addConverters(Class[] converterClasses) {
        converterClasses.each {
            addConverter(it)
        }
    }

    private void addConverter(Class<FileConverter> converterClass) {
        addConverter(converterClass.getDeclaredConstructor().newInstance())
    }

    private void addConverter(FileConverter  converter) {
        def inputType = converter.inputType
        def outputType = converter.outputType

        def entry = converterRegistry.computeIfAbsent(inputType ,{[:]}).put(outputType, converter)
        if (entry) {
            throw new Exception("Conflicting converters found for $inputType -> $outputType")
        }
    }

    public void generateReport(String reportName, Object data, String outputType,
            OutputStream output) {

        def templateEvaluator = new TemplateEvaluator()

        String extension = ''
        if (reportName.contains('.')) {
            extension = reportName.substring(reportName.lastIndexOf('.'))
        }
        def tempFile = Files.createTempFile("report", extension)
        tempFile.withOutputStream {
            templateEvaluator.executeTemplate(reportName, data, it)
        }
        def type = Files.probeContentType(tempFile)
        if (type == outputType) {
            output << tempFile.bytes
        } else {
            FileConverter converter = getConverter(type, outputType)
            if (!converter) {
                throw new IllegalArgumentException("Cannot convert $type to $outputType")
            }
            tempFile.withInputStream {
                converter.convert(it, output)
            }
        }
        Files.delete(tempFile)
    }

    FileConverter getConverter(String inputType, String outputType) {
        Map<String, FileConverter> convertersFromInputType = converterRegistry.getOrDefault(inputType, [:])
        FileConverter converter = convertersFromInputType.get(outputType)
        if (converter) {
            return converter
        }
        // try a two-step conversion
        // TODO: add recursion?
        for (def entry:convertersFromInputType.entrySet() ) {
            String targetType = entry.key
            FileConverter intermediateConverter = entry.value
            FileConverter targetConverter = converterRegistry.getOrDefault(intermediateConverter.getOutputType(), [:]).get(outputType)
            if (targetConverter) {
                FileConverter combinedConverter = new FileConverter() {

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
                                    intermediateConverter.convert(input, it)
                                    it.toByteArray()
                                }
                                ByteArrayInputStream bais = new ByteArrayInputStream(bytes)
                                targetConverter.convert(bais, output)
                            }
                        }
                convertersFromInputType.put(outputType, combinedConverter)
                return combinedConverter
            }
        }


    }
}
