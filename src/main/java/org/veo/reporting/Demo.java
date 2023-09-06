/*******************************************************************************
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
 ******************************************************************************/
package org.veo.reporting;

import java.io.IOException;
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
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;

import freemarker.template.TemplateException;

/**
 * Runs a demo and a template editing environment that can be used to develop new reports. This is
 * only meant to be used by developers and is nowhere near production quality, that's why it must be
 * enabled via the <code>demo</code> Spring Boot profile.
 */
@SuppressWarnings("PMD.SystemPrintln")
public class Demo {

  private static final String TARGET_ID_PLACEHOLDER = "${targetId}";
  private static final Logger logger = LoggerFactory.getLogger(Demo.class);

  static void runDemo(ConfigurableApplicationContext ctx) throws IOException {
    logger.info("Demo mode enabled");
    var reportEngine = ctx.getBean(ReportEngine.class);
    var token = ctx.getEnvironment().getRequiredProperty("veo.accesstoken");
    var scopeId = ctx.getEnvironment().getRequiredProperty("veo.demoscopeid");
    var veoClient = ctx.getBean(VeoClient.class);
    var authHeader = "Bearer " + token;
    Map<Locale, Map<String, Object>> entriesForLanguage = new HashMap<>();
    entriesForLanguage.put(Locale.GERMANY, veoClient.fetchTranslations(Locale.GERMANY, authHeader));
    entriesForLanguage.put(Locale.US, veoClient.fetchTranslations(Locale.US, authHeader));

    var objectMapper = new ObjectMapper();
    var writer = objectMapper.writerWithDefaultPrettyPrinter();

    var dpiaId = ctx.getEnvironment().getProperty("veo.demodpiaid");
    var privacyIncidentId = ctx.getEnvironment().getProperty("veo.demoincidentid");
    boolean createDPIAReports = dpiaId != null;
    boolean createDPIncidentReports = privacyIncidentId != null;

    DataProvider dataProvider =
        new DataProvider() {

          Map<String, Object> cache = new HashMap<>();

          @Override
          public Map<String, Object> resolve(Map<String, String> dataSpec) {

            ReportDataSpecification reportDataSpecification =
                new ReportDataSpecification(
                    dataSpec.entrySet().stream()
                        .filter(e -> !cache.containsKey(e.getKey()))
                        .collect(
                            Collectors.toMap(
                                Entry::getKey,
                                e -> {
                                  String key = e.getKey();
                                  String url = e.getValue();
                                  if ("dpia".equals(key)) {
                                    url = url.replace(TARGET_ID_PLACEHOLDER, dpiaId);
                                  } else if ("incident".equals(key)) {
                                    url = url.replace(TARGET_ID_PLACEHOLDER, privacyIncidentId);
                                  } else if ("scope".equals(key)) {
                                    url = url.replace(TARGET_ID_PLACEHOLDER, scopeId);
                                  } else if (url.contains("targetId")) {
                                    throw new IllegalArgumentException("Unhandled url: " + url);
                                  }

                                  return url;
                                })));
            try {
              if (!reportDataSpecification.isEmpty()) {
                Map<String, Object> data = veoClient.fetchData(reportDataSpecification, authHeader);
                for (Entry<String, Object> e : data.entrySet()) {
                  System.out.println("\n" + e.getKey() + ":");
                  System.out.println(writer.writeValueAsString(e.getValue()));
                }

                cache.putAll(data);
              }
              Map<String, Object> result = new HashMap<>(dataSpec.size());
              result.putAll(
                  dataSpec.keySet().stream()
                      .collect(Collectors.toMap(Function.identity(), cache::get)));

              return result;
            } catch (IOException e) {
              throw new RuntimeException("Error fetching data", e);
            }
          }
        };

    createReports(
        reportEngine, dataProvider, entriesForLanguage, createDPIAReports, createDPIncidentReports);
    Path template = Paths.get("src/main/resources/templates");
    try (WatchService watchService = FileSystems.getDefault().newWatchService()) {

      Files.walkFileTree(
          template,
          new SimpleFileVisitor<Path>() {
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
            createReports(
                reportEngine,
                dataProvider,
                entriesForLanguage,
                createDPIAReports,
                createDPIncidentReports);
          }
          key.reset();
        }
      } catch (InterruptedException e) {
        logger.info("Exiting ...");
      }
    }
    ctx.stop();
  }

  static void createReports(
      ReportEngine reportEngine,
      DataProvider dataProvider,
      Map<Locale, Map<String, Object>> entriesForLanguage,
      boolean createDPIAReports,
      boolean createDPIncidentReports)
      throws IOException {

    ReportCreationParameters parametersGermany = new ReportCreationParameters(Locale.GERMANY);
    ReportCreationParameters parametersUS = new ReportCreationParameters(Locale.US);

    try {
      createReport(
          reportEngine,
          "processing-activities",
          "/tmp/vvt.md",
          dataProvider,
          "text/markdown",
          parametersGermany,
          entriesForLanguage.get(Locale.GERMANY));
      createReport(
          reportEngine,
          "processing-activities",
          "/tmp/vvt.html",
          dataProvider,
          MediaType.TEXT_HTML_VALUE,
          parametersGermany,
          entriesForLanguage.get(Locale.GERMANY));
      createReport(
          reportEngine,
          "processing-activities",
          "/tmp/vvt.pdf",
          dataProvider,
          MediaType.APPLICATION_PDF_VALUE,
          parametersGermany,
          entriesForLanguage.get(Locale.GERMANY));
      createReport(
          reportEngine,
          "risk-analysis",
          "/tmp/dpra.pdf",
          dataProvider,
          MediaType.APPLICATION_PDF_VALUE,
          parametersGermany,
          entriesForLanguage.get(Locale.GERMANY));
      createReport(
          reportEngine,
          "risk-analysis",
          "/tmp/dpra.html",
          dataProvider,
          MediaType.TEXT_HTML_VALUE,
          parametersGermany,
          entriesForLanguage.get(Locale.GERMANY));
      if (createDPIAReports) {

        createReport(
            reportEngine,
            "dp-impact-assessment",
            "/tmp/dpia.pdf",
            dataProvider,
            MediaType.APPLICATION_PDF_VALUE,
            parametersGermany,
            entriesForLanguage.get(Locale.GERMANY));
        createReport(
            reportEngine,
            "dp-impact-assessment",
            "/tmp/dpia.html",
            dataProvider,
            MediaType.TEXT_HTML_VALUE,
            parametersGermany,
            entriesForLanguage.get(Locale.GERMANY));
      }
      if (createDPIncidentReports) {
        createReport(
            reportEngine,
            "dp-privacy-incident",
            "/tmp/privacy-incident.pdf",
            dataProvider,
            MediaType.APPLICATION_PDF_VALUE,
            parametersGermany,
            entriesForLanguage.get(Locale.GERMANY));
        createReport(
            reportEngine,
            "dp-privacy-incident",
            "/tmp/privacy-incident-en.pdf",
            dataProvider,
            MediaType.APPLICATION_PDF_VALUE,
            parametersUS,
            entriesForLanguage.get(Locale.US));
        createReport(
            reportEngine,
            "dp-privacy-incident",
            "/tmp/privacy-incident.html",
            dataProvider,
            MediaType.TEXT_HTML_VALUE,
            parametersGermany,
            entriesForLanguage.get(Locale.GERMANY));
      }
      createReport(
          reportEngine,
          "processing-on-behalf",
          "/tmp/av.pdf",
          dataProvider,
          MediaType.APPLICATION_PDF_VALUE,
          parametersGermany,
          entriesForLanguage.get(Locale.GERMANY));

      createReport(
          reportEngine,
          "dp-requests-from-data-subjects-overview",
          "/tmp/request-overview.pdf",
          dataProvider,
          MediaType.APPLICATION_PDF_VALUE,
          parametersUS,
          entriesForLanguage.get(Locale.US));

    } catch (IOException | TemplateException e) {
      logger.error("Error creating reports", e);
    }
  }

  private static void createReport(
      ReportEngine reportEngine,
      String reportId,
      String fileName,
      DataProvider dataProvider,
      String outputType,
      ReportCreationParameters parameters,
      Map<String, Object> dynamicBundleEntries)
      throws IOException, TemplateException {
    try (var os = Files.newOutputStream(Paths.get(fileName))) {
      reportEngine.generateReport(
          reportId, outputType, parameters, os, dataProvider, dynamicBundleEntries);
      logger.info("Report {} created at {}", reportId, fileName);
    }
  }

  private Demo() {}
}
