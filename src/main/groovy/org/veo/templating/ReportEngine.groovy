package org.veo.templating

import java.nio.file.Files

import org.veo.templating.converters.MarkdownHtmlConverter
import org.veo.templating.converters.MarkdownPDFConverter

public class ReportEngine {

    Map<String, Map<String, FileConverter>> converterRegistry = [:]


    public ReportEngine(){
        addConverters(MarkdownHtmlConverter, MarkdownPDFConverter)
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
        }else {
            FileConverter converter = converterRegistry.getOrDefault(type, [:]).get(outputType)
            if (!converter) {
                throw new IllegalArgumentException("Cannot convert $type to $outputType")
            }
            tempFile.withInputStream {
                converter.convert(it, output)
            }
        }
        Files.delete(tempFile)
    }
}
