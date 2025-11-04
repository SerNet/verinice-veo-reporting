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
package org.veo.reporting.controllers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerErrorException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.servlet.support.RequestContextUtils;

import org.veo.reporting.CreateReport;
import org.veo.reporting.CreateReport.TargetSpecification;
import org.veo.reporting.DataProvider;
import org.veo.reporting.ReportConfiguration;
import org.veo.reporting.ReportCreationParameters;
import org.veo.reporting.ReportDataSpecification;
import org.veo.reporting.ReportEngine;
import org.veo.reporting.TypeSpecification;
import org.veo.reporting.VeoClient;
import org.veo.reporting.exception.InvalidReportParametersException;

import freemarker.template.TemplateException;

/**
 * The REST controller that serves as the API. Can be used to retrieve the available reports and
 * execute them.
 *
 * @see ReportConfiguration
 * @see CreateReport
 */
@RestController
@RequestMapping("/reports")
public class ReportController {

  private static final String TARGET_ID = "targetId";
  private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

  private final ReportEngine reportEngine;
  private final VeoClient veoClient;
  private final long buildTime;

  public ReportController(
      ReportEngine reportEngine, VeoClient veoClient, BuildProperties buildProperties) {
    this.reportEngine = reportEngine;
    this.veoClient = veoClient;
    buildTime = buildProperties.getTime().toEpochMilli();
  }

  /**
   * @return the available reports, optionally filtered by domain name
   */
  @GetMapping
  public ResponseEntity<Map<String, ReportConfiguration>> getReports(
      WebRequest request, @RequestParam(name = "domain", required = false) String domainName) {
    if (request.checkNotModified(buildTime)) {
      return null;
    }
    Map<String, ReportConfiguration> reports = reportEngine.getReports();
    if (domainName != null) {
      reports =
          reports.entrySet().stream()
              .filter(e -> domainName.equals(e.getValue().getDomainName()))
              .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
    return ResponseEntity.ok().cacheControl(CacheControl.noCache()).body(reports);
  }

  /**
   * Creates a report
   *
   * @param id the report id
   * @param createReport the report creation parameters, see {@link CreateReport}
   * @param authorizationHeader the <code>Authorization</code> request header
   * @param request the servlet request
   * @return the report
   */
  @PostMapping("/{id}")
  public ResponseEntity<StreamingResponseBody> generateReport(
      @PathVariable String id,
      @Valid @RequestBody CreateReport createReport,
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false)
          String authorizationHeader,
      HttpServletRequest request) {
    if (authorizationHeader == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    String outputType = createReport.getOutputType();
    logger.info("Create report {}, outputType {}", id, outputType);
    Optional<ReportConfiguration> configuration = reportEngine.getReport(id);
    if (!configuration.isPresent()) {
      return ResponseEntity.notFound().build();
    }
    List<String> supportedOutputTypes = configuration.get().getOutputTypes();
    if (!supportedOutputTypes.contains(outputType)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Output type "
              + outputType
              + " not supported by report "
              + id
              + ", supported output types: "
              + supportedOutputTypes);
    }

    // exactly one target entity is supported at the moment
    TargetSpecification target = createReport.getTargets().get(0);
    Set<TypeSpecification> supportedTargetTypes = configuration.get().getTargetTypes();
    if (supportedTargetTypes.stream()
        .noneMatch(typeSpecification -> typeSpecification.getModelType() == target.type())) {
      throw new InvalidReportParametersException(
          "Target type " + target.type() + " not supported by report " + id);
    }
    ReportCreationParameters parameters =
        new ReportCreationParameters(
            RequestContextUtils.getLocale(request),
            createReport.getTimeZone() != null
                    && Arrays.asList(TimeZone.getAvailableIDs())
                        .contains(createReport.getTimeZone())
                ? TimeZone.getTimeZone(createReport.getTimeZone())
                : TimeZone.getTimeZone("UTC"));
    logger.info("Request parameters = {}", parameters);

    String desiredLanguage = parameters.getLocale().getLanguage();
    Set<String> supportedLanguages = configuration.get().getName().keySet();
    if (!supportedLanguages.contains(desiredLanguage)) {
      throw new InvalidReportParametersException(
          "Language "
              + desiredLanguage
              + " not supported by report "
              + id
              + ", supported languages: "
              + supportedLanguages);
    }

    Map<String, Object> entriesForLanguage;
    try {
      entriesForLanguage = veoClient.fetchTranslations(parameters.getLocale(), authorizationHeader);
    } catch (IOException e) {
      throw new ServerErrorException(
          "Failed to fetch translations for " + parameters.getLocale(), e);
    }
    DataProvider dataProvider =
        keysAndUrls -> {
          ReportDataSpecification reportDataSpecification =
              new ReportDataSpecification(
                  keysAndUrls.entrySet().stream()
                      .collect(
                          Collectors.toMap(
                              Entry::getKey, e -> expandUrl(e.getKey(), e.getValue(), target))));

          try {
            return veoClient.fetchData(reportDataSpecification, authorizationHeader);
          } catch (IOException e) {
            throw new ServerErrorException("Failed to fetch report data", e);
          }
        };

    StreamingResponseBody stream =
        out -> {
          try {
            reportEngine.generateReport(
                id, outputType, parameters, out, dataProvider, entriesForLanguage);
            logger.info("Report generated");
          } catch (TemplateException e) {
            logger.error("Error creating report", e);
            throw new ServerErrorException("Error creating report", e);
          }
        };
    MediaType mediaType = MediaType.valueOf(outputType);
    if ("text".equals(mediaType.getType()) && mediaType.getCharset() == null) {
      mediaType = new MediaType(mediaType, StandardCharsets.UTF_8);
    }
    return ResponseEntity.ok().contentType(mediaType).body(stream);
  }

  private static String expandUrl(String key, String url, TargetSpecification target) {
    PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper("${", "}");
    return helper.replacePlaceholders(
        url,
        placeholderName -> {
          if (TARGET_ID.equals(placeholderName)) {
            return target.id();
          } else {
            throw new IllegalArgumentException(
                "Unsupported placeholder \"%s\" in url for %s (%s)"
                    .formatted(placeholderName, key, url));
          }
        });
  }
}
