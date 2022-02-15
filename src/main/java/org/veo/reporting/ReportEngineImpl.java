/**
 * verinice.veo reporting
 * Copyright (C) 2021  Jochen Kemnade
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.veo.reporting;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.veo.fileconverter.FileConverter;
import org.veo.templating.TemplateEvaluator;

import freemarker.template.TemplateException;

public class ReportEngineImpl implements ReportEngine {

    private static final Logger logger = LoggerFactory.getLogger(ReportEngineImpl.class);

    private final TemplateEvaluator templateEvaluator;
    private final FileConverter converter;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ResourcePatternResolver resourcePatternResolver;
    private final ExecutorService executorService;

    public ReportEngineImpl(TemplateEvaluator templateEvaluator, FileConverter converter,
            ResourcePatternResolver resourcePatternResolver, ExecutorService executorService) {
        this.templateEvaluator = templateEvaluator;
        this.converter = converter;
        this.resourcePatternResolver = resourcePatternResolver;
        this.executorService = executorService;
    }

    @Override
    public void generateReport(String reportName, String outputType, Locale locale,
            OutputStream outputStream, DataProvider dataProvider,
            Map<String, Object> dynamicBundleEntries) throws IOException, TemplateException {

        ReportConfiguration config = getReport(reportName)
                .orElseThrow(() -> new IllegalArgumentException("Unknown report " + reportName));

        Map<String, Object> data = new HashMap<>();

        String templateName = config.getTemplateFile();

        config.getData().forEach((key, value) -> data.put(key, dataProvider.resolve(key, value)));

        String templateBaseName = templateName.split("\\.")[0];
        String bundleName = "templates." + templateBaseName;
        logger.info("Loading resourceBundle for template {}, locale {} from {}", templateName,
                locale, bundleName);
        try {
            ResourceBundle reportBundle = ResourceBundle.getBundle(bundleName, locale);
            logger.info("Bundle loaded, locale: {}", reportBundle.getLocale());
            data.put("bundle",
                    MapResourceBundle.createMergedBundle(reportBundle, dynamicBundleEntries));
        } catch (MissingResourceException e) {
            logger.warn("No resource bundle found for template {}", templateName);
            data.put("bundle", new MapResourceBundle(dynamicBundleEntries));
        }
        generateReport(templateName, data, config.getTemplateType(), outputType, outputStream);

    }

    @Override
    public void generateReport(String templateName, Map<String, Object> data, String templateType,
            String outputType, OutputStream output) throws IOException, TemplateException {
        if (outputType.equals(templateType)) {
            templateEvaluator.executeTemplate(templateName, data, output);
        } else {
            try (PipedInputStream pipedInputStream = new PipedInputStream();
                    PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream)) {
                // start async conversion of the template output
                Future<Void> future = executorService.submit(() -> {
                    converter.convert(pipedInputStream, templateType, output, outputType);
                    return null;
                });
                try {
                    templateEvaluator.executeTemplate(templateName, data, pipedOutputStream);
                } catch (TemplateException | IOException e) {
                    // template evaluation failed, cancel the conversion task
                    future.cancel(true);
                    throw e;
                }
                try {
                    // wait for the conversion to finish
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException("Error running conversion", e);
                }
            }
        }
    }

    @Override
    public Map<String, ReportConfiguration> getReports() {
        try {
            Resource[] resources = resourcePatternResolver
                    .getResources("classpath*:/reports/*.json");
            return Arrays.stream(resources).collect(Collectors.toMap(resource -> {
                String fileName = resource.getFilename();
                if (fileName == null) {
                    throw new IllegalStateException("File name is null for " + resource);
                }
                return fileName.substring(0, fileName.length() - 5);
            }, resource -> {
                try (var is = resource.getInputStream()) {
                    ReportConfiguration reportConfiguration = objectMapper.readValue(is,
                            ReportConfiguration.class);
                    logger.info("Read report {} from {}", reportConfiguration.getName(), resource);
                    return reportConfiguration;
                } catch (IOException e) {
                    throw new RuntimeException("Error loading report configurations", e);
                }
            }));

        } catch (IOException e) {
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
