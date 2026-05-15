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
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;

import freemarker.template.TemplateException;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.json.JsonMapper;

/**
 * Runs a demo and a template editing environment that can be used to develop new reports. This is
 * only meant to be used by developers and is nowhere near production quality, that's why it must be
 * enabled via the <code>demo</code> Spring Boot profile.
 */
@SuppressWarnings({"PMD.SystemPrintln", "PMD.AvoidDuplicateLiterals"})
public class Demo {

  private static final Logger LOGGER = LoggerFactory.getLogger(Demo.class);

  static void runDemo(ConfigurableApplicationContext ctx) throws IOException {
    LOGGER.info("Demo mode enabled");
    var reportEngine = ctx.getBean(ReportEngine.class);
    var token = ctx.getEnvironment().getRequiredProperty("veo.accesstoken");
    var scopeId = ctx.getEnvironment().getProperty("veo.demoscopeid", UUID.class);
    var printInputData = "true".equals(ctx.getEnvironment().getProperty("veo.print_report_data"));
    var requestId = ctx.getEnvironment().getProperty("veo.demorequestid", UUID.class);
    var scopeIdItgs = ctx.getEnvironment().getProperty("veo.demoscopeiditbp", UUID.class);
    var scopeIdNIS2 = ctx.getEnvironment().getProperty("veo.demoscopeidnis2", UUID.class);
    var isaId = ctx.getEnvironment().getProperty("veo.demoisaid", UUID.class);
    var scopeIdIso = ctx.getEnvironment().getProperty("veo.demoscopeidiso", UUID.class);
    var veoClient = ctx.getBean(VeoClient.class);
    var authHeader = "Bearer " + token;

    var dpiaId = ctx.getEnvironment().getProperty("veo.demodpiaid", UUID.class);
    var privacyIncidentId = ctx.getEnvironment().getProperty("veo.demoincidentid", UUID.class);
    var securityIncidentId = ctx.getEnvironment().getProperty("veo.demoincidentidnis2", UUID.class);

    var veoClientWrapper = new VeoClientWrapper(veoClient, authHeader, printInputData);

    if (scopeId != null) {
      veoClientWrapper.addReportTargetData(
          EntityType.SCOPE,
          scopeId,
          "processing-activities",
          "risk-analysis",
          "processing-on-behalf",
          "dp-requests-from-data-subjects-overview");
    }

    if (dpiaId != null) {
      veoClientWrapper.addReportTargetData(EntityType.PROCESS, dpiaId, "dp-impact-assessment");
    }

    if (privacyIncidentId != null) {
      veoClientWrapper.addReportTargetData(
          EntityType.INCIDENT, privacyIncidentId, "dp-privacy-incident");
    }

    if (requestId != null) {
      veoClientWrapper.addReportTargetData(
          EntityType.DOCUMENT, requestId, "dp-request-from-data-subject");
    }

    if (scopeIdItgs != null) {
      veoClientWrapper.addReportTargetData(
          EntityType.SCOPE,
          scopeIdItgs,
          "itbp-a1",
          "itbp-a2",
          "itbp-a3",
          "itbp-a4",
          "itbp-a5",
          "itbp-a6");
    }

    if (scopeIdNIS2 != null) {
      veoClientWrapper.addReportTargetData(EntityType.SCOPE, scopeIdNIS2, "nis2-registration-info");
    }

    if (scopeIdIso != null) {
      veoClientWrapper.addReportTargetData(
          EntityType.SCOPE, scopeIdIso, "iso-soa", "iso-inventory", "iso-risk-analysis");
    }

    if (securityIncidentId != null) {
      veoClientWrapper.addReportTargetData(
          EntityType.INCIDENT, securityIncidentId, "nis2-security-incident");
    }

    if (isaId != null) {
      veoClientWrapper.addReportTargetData(
          EntityType.SCOPE, isaId, "tisax-compact", "tisax-detailed");
    }

    createReports(reportEngine, veoClientWrapper);
    Path template = Paths.get("src/main/resources/templates");
    try (WatchService watchService = FileSystems.getDefault().newWatchService()) {

      Files.walkFileTree(
          template,
          new SimpleFileVisitor<>() {
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
            createReports(reportEngine, veoClientWrapper);
          }
          key.reset();
        }
      } catch (InterruptedException e) {
        LOGGER.info("Exiting ...");
      }
    }
    ctx.stop();
  }

  static void createReports(ReportEngine reportEngine, VeoClientWrapper veoClientWrapper)
      throws IOException {

    ReportCreationParameters parametersGermany =
        new ReportCreationParameters(Locale.GERMANY, TimeZone.getTimeZone("Europe/Berlin"));
    ReportCreationParameters parametersUS =
        new ReportCreationParameters(Locale.US, TimeZone.getTimeZone("America/New_York"));

    try {
      createReport(
          reportEngine,
          "processing-activities",
          "/tmp/vvt.md",
          veoClientWrapper,
          "text/markdown",
          parametersGermany);
      createReport(
          reportEngine,
          "processing-activities",
          "/tmp/vvt.html",
          veoClientWrapper,
          MediaType.TEXT_HTML_VALUE,
          parametersGermany);
      createReport(
          reportEngine,
          "processing-activities",
          "/tmp/vvt.pdf",
          veoClientWrapper,
          MediaType.APPLICATION_PDF_VALUE,
          parametersGermany);
      createReport(
          reportEngine,
          "risk-analysis",
          "/tmp/dpra.pdf",
          veoClientWrapper,
          MediaType.APPLICATION_PDF_VALUE,
          parametersGermany);
      createReport(
          reportEngine,
          "risk-analysis",
          "/tmp/dpra.html",
          veoClientWrapper,
          MediaType.TEXT_HTML_VALUE,
          parametersGermany);
      createReport(
          reportEngine,
          "processing-on-behalf",
          "/tmp/av.pdf",
          veoClientWrapper,
          MediaType.APPLICATION_PDF_VALUE,
          parametersGermany);

      createReport(
          reportEngine,
          "dp-requests-from-data-subjects-overview",
          "/tmp/request-overview.pdf",
          veoClientWrapper,
          MediaType.APPLICATION_PDF_VALUE,
          parametersUS);

      createReport(
          reportEngine,
          "dp-impact-assessment",
          "/tmp/dpia.pdf",
          veoClientWrapper,
          MediaType.APPLICATION_PDF_VALUE,
          parametersGermany);
      createReport(
          reportEngine,
          "dp-impact-assessment",
          "/tmp/dpia.html",
          veoClientWrapper,
          MediaType.TEXT_HTML_VALUE,
          parametersGermany);

      createReport(
          reportEngine,
          "dp-privacy-incident",
          "/tmp/privacy-incident.pdf",
          veoClientWrapper,
          MediaType.APPLICATION_PDF_VALUE,
          parametersGermany);
      createReport(
          reportEngine,
          "dp-privacy-incident",
          "/tmp/privacy-incident-en.pdf",
          veoClientWrapper,
          MediaType.APPLICATION_PDF_VALUE,
          parametersUS);
      createReport(
          reportEngine,
          "dp-privacy-incident",
          "/tmp/privacy-incident.html",
          veoClientWrapper,
          MediaType.TEXT_HTML_VALUE,
          parametersGermany);

      createReport(
          reportEngine,
          "dp-request-from-data-subject",
          "/tmp/request.pdf",
          veoClientWrapper,
          MediaType.APPLICATION_PDF_VALUE,
          parametersGermany);
      createReport(
          reportEngine,
          "dp-request-from-data-subject",
          "/tmp/request-en.pdf",
          veoClientWrapper,
          MediaType.APPLICATION_PDF_VALUE,
          parametersUS);

      createReport(
          reportEngine,
          "itbp-a1",
          "/tmp/itbp-a1.pdf",
          veoClientWrapper,
          MediaType.APPLICATION_PDF_VALUE,
          parametersGermany);
      createReport(
          reportEngine,
          "itbp-a2",
          "/tmp/itbp-a2.pdf",
          veoClientWrapper,
          MediaType.APPLICATION_PDF_VALUE,
          parametersGermany);
      createReport(
          reportEngine,
          "itbp-a3",
          "/tmp/itbp-a3.pdf",
          veoClientWrapper,
          MediaType.APPLICATION_PDF_VALUE,
          parametersGermany);
      createReport(
          reportEngine,
          "itbp-a4",
          "/tmp/itbp-a4.pdf",
          veoClientWrapper,
          MediaType.APPLICATION_PDF_VALUE,
          parametersGermany);
      createReport(
          reportEngine,
          "itbp-a5",
          "/tmp/itbp-a5.pdf",
          veoClientWrapper,
          MediaType.APPLICATION_PDF_VALUE,
          parametersGermany);
      createReport(
          reportEngine,
          "itbp-a6",
          "/tmp/itbp-a6.pdf",
          veoClientWrapper,
          MediaType.APPLICATION_PDF_VALUE,
          parametersGermany);

      createReport(
          reportEngine,
          "nis2-registration-info",
          "/tmp/nis2-registration-info.pdf",
          veoClientWrapper,
          MediaType.APPLICATION_PDF_VALUE,
          parametersGermany);
      createReport(
          reportEngine,
          "nis2-registration-info",
          "/tmp/nis2-registration-info.en.pdf",
          veoClientWrapper,
          MediaType.APPLICATION_PDF_VALUE,
          parametersUS);

      createReport(
          reportEngine,
          "nis2-security-incident",
          "/tmp/nis2-security-incident.pdf",
          veoClientWrapper,
          MediaType.APPLICATION_PDF_VALUE,
          parametersGermany);
      createReport(
          reportEngine,
          "nis2-security-incident",
          "/tmp/nis2-security-incident.en.pdf",
          veoClientWrapper,
          MediaType.APPLICATION_PDF_VALUE,
          parametersUS);

      createReport(
          reportEngine,
          "tisax-compact",
          "/tmp/tisax-compact.pdf",
          veoClientWrapper,
          MediaType.APPLICATION_PDF_VALUE,
          parametersGermany);
      createReport(
          reportEngine,
          "tisax-detailed",
          "/tmp/tisax-detailed.pdf",
          veoClientWrapper,
          MediaType.APPLICATION_PDF_VALUE,
          parametersGermany);

      createReport(
          reportEngine,
          "iso-soa",
          "/tmp/iso-soa.pdf",
          veoClientWrapper,
          MediaType.APPLICATION_PDF_VALUE,
          parametersGermany);
      createReport(
          reportEngine,
          "iso-inventory",
          "/tmp/iso-inventory.pdf",
          veoClientWrapper,
          MediaType.APPLICATION_PDF_VALUE,
          parametersGermany);
      createReport(
          reportEngine,
          "iso-risk-analysis",
          "/tmp/iso-risk-analysis.pdf",
          veoClientWrapper,
          MediaType.APPLICATION_PDF_VALUE,
          parametersGermany);

    } catch (IOException | TemplateException e) {
      LOGGER.error("Error creating reports", e);
    }
  }

  private static void createReport(
      ReportEngine reportEngine,
      String reportId,
      String fileName,
      VeoClientWrapper veoClientWrapper,
      String outputType,
      ReportCreationParameters parameters)
      throws IOException, TemplateException {
    if (!veoClientWrapper.supports(reportId)) {
      LOGGER.info("No target registered for report {}, skipping", reportId);
      return;
    }
    DataProvider dataProvider = () -> veoClientWrapper.loadData(reportId);
    Map<String, Object> dynamicBundleEntries =
        veoClientWrapper.loadTranslations(reportId, parameters.locale());
    try (var os = Files.newOutputStream(Paths.get(fileName))) {
      reportEngine.generateReport(
          reportId, outputType, parameters, os, dataProvider, dynamicBundleEntries);
      LOGGER.info("Report {} created at {}", reportId, fileName);
    } catch (Exception e) {
      LOGGER.error("Could not create report {}", reportId, e);
    }
  }

  private Demo() {}

  static class VeoClientWrapper {

    private final Map<String, Map<String, Object>> cache = new HashMap<>();
    private final Map<String, ElementInfo> reportTargets = new HashMap<>();

    private final VeoClient veoClient;
    private final String authorizationHeader;
    private final boolean printInputData;
    private final ObjectWriter writer;

    public VeoClientWrapper(
        VeoClient veoClient, String authorizationHeader, boolean printInputData) {
      this.veoClient = veoClient;
      this.authorizationHeader = authorizationHeader;
      this.printInputData = printInputData;
      var objectMapper = new JsonMapper();
      writer = objectMapper.writerWithDefaultPrettyPrinter();
    }

    public Map<String, Object> loadData(String reportId) {
      ElementInfo info =
          Objects.requireNonNull(reportTargets.get(reportId), "Unsupported report " + reportId);
      return cache.computeIfAbsent(
          reportId,
          _ -> {
            try {
              Map<String, Object> data =
                  veoClient.fetchData(
                      info.unitId, info.domainId, info.elementId, authorizationHeader);
              if (printInputData) {
                for (Map.Entry<String, Object> e : data.entrySet()) {
                  System.out.println("\n" + e.getKey() + ":");
                  System.out.println(writer.writeValueAsString(e.getValue()));
                }
              }
              return data;
            } catch (IOException e) {
              throw new RuntimeException("Error fetching data", e);
            }
          });
    }

    public void addReportTargetData(EntityType entityType, UUID elementId, String... reportIDs) {
      ElementInfo elementInfo = getElementInfo(entityType.pluralTerm, elementId);
      for (String reportId : reportIDs) {
        reportTargets.put(reportId, elementInfo);
      }
    }

    public boolean supports(String reportId) {
      return reportTargets.containsKey(reportId);
    }

    private ElementInfo getElementInfo(String elementTypePlural, UUID elementId) {
      try {
        Map<String, Object> data =
            (Map<String, Object>)
                veoClient.fetchData(
                    "/" + elementTypePlural + "/" + elementId,
                    authorizationHeader,
                    MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> owner = (Map<String, Object>) data.get("owner");

        String ownerId = (String) owner.get("id");

        Map<String, Map> domains = (Map<String, Map>) data.get(VeoReportingConstants.DOMAINS);

        if (domains.size() != 1) {
          throw new IllegalArgumentException("Expected a domain, but got " + domains.size());
        }
        String domainId = domains.entrySet().iterator().next().getKey();

        return new ElementInfo(elementId, UUID.fromString(domainId), UUID.fromString(ownerId));

      } catch (IOException e) {
        throw new RuntimeException("Error fetching data", e);
      }
    }

    public Map<String, Object> loadTranslations(String reportId, Locale locale) throws IOException {
      ElementInfo info =
          Objects.requireNonNull(reportTargets.get(reportId), "Unsupported report " + reportId);

      return veoClient.fetchTranslations(locale, info.domainId, authorizationHeader);
    }
  }

  record ElementInfo(UUID elementId, UUID domainId, UUID unitId) {}
}
