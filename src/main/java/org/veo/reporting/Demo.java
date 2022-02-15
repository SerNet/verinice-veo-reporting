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
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import freemarker.template.TemplateException;

/**
 * Runs a demo and a template editing environment that can be used to develop
 * new reports. This is only meant to be used by developers and is nowhere near
 * production quality, that's why it must be enabled via the <code>demo</code>
 * Spring Boot profile.
 */
public class Demo {

    private static final Logger logger = LoggerFactory.getLogger(Demo.class);

    static void runDemo(ConfigurableApplicationContext ctx) throws IOException {
        logger.info("Demo mode enabled");
        var reportEngine = ctx.getBean(ReportEngine.class);
        var token = ctx.getEnvironment().getRequiredProperty("veo.accesstoken");
        var scopeId = ctx.getEnvironment().getRequiredProperty("veo.demoscopeid");
        var veoClient = ctx.getBean(VeoClient.class);
        var authHeader = "Bearer " + token;
        var processes = veoClient.fetchData("/processes?size=2147483647", authHeader);
        var scope = veoClient.fetchData("/scopes/" + scopeId, authHeader);
        var scopes = veoClient.fetchData("/scopes?size=2147483647", authHeader);
        var persons = veoClient.fetchData("/persons?size=2147483647", authHeader);
        var controls = veoClient.fetchData("/controls?size=2147483647", authHeader);
        var assets = veoClient.fetchData("/assets?size=2147483647", authHeader);
        var scopeMembers = veoClient.fetchData("/scopes/" + scopeId + "/members", authHeader);
        Map<String, Object> entriesForLanguage = veoClient.fetchTranslations(Locale.GERMANY,
                authHeader);

        var objectMapper = new ObjectMapper();
        var writer = objectMapper.writerWithDefaultPrettyPrinter();
        System.out.println("Scope:");
        System.out.println(writer.writeValueAsString(scope));
        System.out.println("\nScopes:");
        System.out.println(writer.writeValueAsString(scopes));
        System.out.println("\nProcesses:");
        System.out.println(writer.writeValueAsString(processes));
        System.out.println("\nPersons:");
        System.out.println(writer.writeValueAsString(persons));
        System.out.println("\nControls:");
        System.out.println(writer.writeValueAsString(controls));
        System.out.println("\nAssets:");
        System.out.println(writer.writeValueAsString(assets));

        var templateInput = Map.of("scope", scope, "scopes", scopes, "processes", processes,
                "persons", persons, "controls", controls, "assets", assets, "scopeMembers",
                scopeMembers);
        createReports(reportEngine, templateInput, entriesForLanguage);
        Path template = Paths.get("src/main/resources/templates");
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {

            Files.walkFileTree(template, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                        throws IOException {
                    dir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
                    return FileVisitResult.CONTINUE;
                }
            });

            WatchKey key;
            try {
                while ((key = watchService.take()) != null) {
                    if (!key.pollEvents().isEmpty()) {
                        createReports(reportEngine, templateInput, entriesForLanguage);
                    }
                    key.reset();
                }
            } catch (InterruptedException e) {
                logger.info("Exiting ...");
            }
        }
        ctx.stop();
    }

    static void createReports(ReportEngine reportEngine, Map<String, Object> templateInput,
            Map<String, Object> entriesForLanguage) throws IOException {
        ResourceBundle bundleAV;
        ResourceBundle bundleVVT;
        try (InputStream is = Files
                .newInputStream(Paths.get("src/main/resources/templates/av_de.properties"))) {
            bundleAV = new PropertyResourceBundle(is);
        }
        try (InputStream is = Files
                .newInputStream(Paths.get("src/main/resources/templates/vvt_de.properties"))) {
            bundleVVT = new PropertyResourceBundle(is);
        }

        HashMap<String, Object> workingCopy = new HashMap<>(templateInput);
        MapResourceBundle mergedBundleAV = MapResourceBundle.createMergedBundle(bundleAV,
                entriesForLanguage);
        MapResourceBundle mergedBundleVVT = MapResourceBundle.createMergedBundle(bundleVVT,
                entriesForLanguage);

        workingCopy.put("bundle", mergedBundleVVT);
        try {
            createReport(reportEngine, "/tmp/vvt.md", "vvt.md", workingCopy, "text/markdown",
                    "text/markdown");
            createReport(reportEngine, "/tmp/vvt.html", "vvt.md", workingCopy, "text/markdown",
                    "text/html");
            createReport(reportEngine, "/tmp/vvt.pdf", "vvt.md", workingCopy, "text/markdown",
                    "application/pdf");
            workingCopy.put("bundle", mergedBundleAV);

            createReport(reportEngine, "/tmp/av.pdf", "av.md", workingCopy, "text/markdown",
                    "application/pdf");

        } catch (IOException | TemplateException e) {
            logger.error("Error creating reports", e);
        }
    }

    private static void createReport(ReportEngine reportEngine, String fileName,
            String templateName, Map<String, Object> templateInput, String templateType,
            String outputType) throws IOException, TemplateException {
        try (var os = Files.newOutputStream(Paths.get(fileName))) {
            reportEngine.generateReport(templateName, templateInput, templateType, outputType, os);
            logger.info("Report {} created at {}", templateName, fileName);
        }
    }

    private Demo() {
    }

}
