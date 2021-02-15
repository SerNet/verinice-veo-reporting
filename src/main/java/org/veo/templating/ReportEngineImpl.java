package org.veo.templating;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.veo.fileconverter.FileConverter;

import freemarker.template.TemplateException;

public class ReportEngineImpl implements ReportEngine {

    private final TemplateEvaluator templateEvaluator;
    private final FileConverter converter;

    public ReportEngineImpl(TemplateEvaluator templateEvaluator, FileConverter converter) {
        this.templateEvaluator = templateEvaluator;
        this.converter = converter;
    }

    @Override
    public void generateReport(String reportName, Object data, String outputType,
            OutputStream output) throws IOException, TemplateException {
        String extension = "";
        if (reportName.contains(".")) {
            extension = reportName.substring(reportName.lastIndexOf("."));
        }
        Path tempFile = Files.createTempFile("report", extension);
        try (OutputStream os = Files.newOutputStream(tempFile)) {
            templateEvaluator.executeTemplate(reportName, data, os);
        }

        String type = Files.probeContentType(tempFile);
        if (outputType.equals(type)) {
            Files.copy(tempFile, output);
        } else {
            try (InputStream is = Files.newInputStream(tempFile)) {
                converter.convert(is, type, output, outputType);
            }
        }
        Files.delete(tempFile);
    }
}
