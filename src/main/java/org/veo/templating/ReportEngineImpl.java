package org.veo.templating;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import org.veo.fileconverter.FileConverter;

import freemarker.template.TemplateException;
import groovy.json.JsonSlurper;

public class ReportEngineImpl implements ReportEngine {

    private final TemplateEvaluator templateEvaluator;
    private final FileConverter converter;

    public ReportEngineImpl(TemplateEvaluator templateEvaluator, FileConverter converter) {
        this.templateEvaluator = templateEvaluator;
        this.converter = converter;
    }

    @Override
    public void generateReport(String reportName, String outputType, OutputStream outputStream,
            BiFunction<String, String, Object> dataProvider) throws IOException, TemplateException {
        try (InputStream configIs = ReportEngineImpl.class
                .getResourceAsStream("/reports/" + reportName + ".json")) {
            if (configIs == null) {
                throw new IllegalArgumentException("Unknown report " + reportName);
            }
            // TODO find non-Groovy API
            Map<String, Object> config = (Map<String, Object>) new JsonSlurper().parse(configIs);
            Map<String, Object> data = new HashMap<>();

            String templateName = (String) config.get("template");

            Map<String, String> dataFromConfig = (Map<String, String>) config.get("data");

            dataFromConfig.forEach((key, value) -> {
                data.put(key, dataProvider.apply(key, value));
            });
            generateReport(templateName, data, outputType, outputStream);
        }
    }

    @Override
    public void generateReport(String templateName, Object data, String outputType,
            OutputStream output) throws IOException, TemplateException {
        String extension = "";
        if (templateName.contains(".")) {
            extension = templateName.substring(templateName.lastIndexOf("."));
        }
        Path tempFile = Files.createTempFile("report", extension);
        try (OutputStream os = Files.newOutputStream(tempFile)) {
            templateEvaluator.executeTemplate(templateName, data, os);
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
