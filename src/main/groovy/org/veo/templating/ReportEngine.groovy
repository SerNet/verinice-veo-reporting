package org.veo.templating

import groovy.transform.CompileStatic

import java.nio.file.Files

@CompileStatic
public class ReportEngine {


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
            FileConverter converter = new FileConverter()
            tempFile.withInputStream {
                converter.convert(it, type, output, outputType)
            }
        }
        Files.delete(tempFile)
    }
}
