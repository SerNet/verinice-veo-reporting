package org.veo.reporting;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.veo.fileconverter.FileConverter;
import org.veo.templating.TemplateEvaluator;

import freemarker.template.TemplateException;

public class ReportEngineImpl implements ReportEngine {

    private final TemplateEvaluator templateEvaluator;
    private final FileConverter converter;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ResourcePatternResolver resourcePatternResolver;

    public ReportEngineImpl(TemplateEvaluator templateEvaluator, FileConverter converter,
            ResourcePatternResolver resourcePatternResolver) {
        this.templateEvaluator = templateEvaluator;
        this.converter = converter;
        this.resourcePatternResolver = resourcePatternResolver;
    }

    @Override
    public void generateReport(String reportName, String outputType, OutputStream outputStream,
            BiFunction<String, String, Object> dataProvider) throws IOException, TemplateException {

        ReportConfiguration config = getReport(reportName)
                .orElseThrow(() -> new IllegalArgumentException("Unknown report " + reportName));

        Map<String, Object> data = new HashMap<>();

        String templateName = config.getTemplateFile();

        Map<String, String> dataFromConfig = config.getData();

        dataFromConfig.forEach((key, value) -> data.put(key, dataProvider.apply(key, value)));
        generateReport(templateName, data, config.getTemplateType(), outputType, outputStream);

    }

    @Override
    public void generateReport(String templateName, Object data, String templateType,
            String outputType, OutputStream output) throws IOException, TemplateException {
        if (outputType.equals(templateType)) {
            templateEvaluator.executeTemplate(templateName, data, output);
        } else {
            try (ByteArrayOutputStream tmp = new ByteArrayOutputStream()) {
                templateEvaluator.executeTemplate(templateName, data, tmp);
                try (InputStream is = new ByteArrayInputStream(tmp.toByteArray())) {
                    converter.convert(is, templateType, output, outputType);
                }
            }
        }
    }

    @Override
    public Map<String, ReportConfiguration> getReports() {
        try {
            Resource[] resources = resourcePatternResolver
                    .getResources("classpath:/reports/*.json");
            return Arrays.stream(resources).collect(Collectors.toMap(resource -> {
                String fileName = resource.getFilename();
                if (fileName == null) {
                    throw new IllegalStateException("File name is null for " + resource);
                }
                return fileName.substring(0, fileName.length() - 5);
            }, resource -> {
                try (var is = resource.getInputStream()) {
                    return objectMapper.readValue(is, ReportConfiguration.class);
                } catch (IOException e) {
                    throw new RuntimeException("Error loading report configurations", e);
                }
            }));

        } catch (

        IOException e) {
            throw new RuntimeException("Error loading report configurations", e);
        }

    }

    @Override
    public Optional<ReportConfiguration> getReport(String id) {
        var resource = resourcePatternResolver.getResource("classpath:/reports/" + id + ".json");
        if (!resource.exists()) {
            return Optional.empty();
        }
        try (InputStream is = resource.getInputStream()) {
            var config = objectMapper.readValue(is, ReportConfiguration.class);
            return Optional.of(config);
        } catch (IOException e) {
            throw new RuntimeException("Error loading report configuration", e);
        }
    }
}
