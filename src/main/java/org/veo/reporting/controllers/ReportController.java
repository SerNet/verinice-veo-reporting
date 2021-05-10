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
package org.veo.reporting.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerErrorException;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import org.veo.reporting.CreateReport;
import org.veo.reporting.CreateReport.TargetSpecification;
import org.veo.reporting.DataProvider;
import org.veo.reporting.EntityType;
import org.veo.reporting.ReportConfiguration;
import org.veo.reporting.ReportEngine;
import org.veo.reporting.VeoClient;

import freemarker.template.TemplateException;

/**
 * The REST controller that serves as the API. Can be used to retrieve the
 * available reports and execute them.
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
    private final LocaleResolver localeResolver;

    public ReportController(ReportEngine reportEngine, VeoClient veoClient,
            LocaleResolver localeResolver) {
        this.reportEngine = reportEngine;
        this.veoClient = veoClient;
        this.localeResolver = localeResolver;
    }

    /**
     * @return the available reports
     */
    @GetMapping
    public Map<String, ReportConfiguration> getReports() {
        return reportEngine.getReports();
    }

    /**
     * Creates a report
     *
     * @param id
     *            the report id
     * @param createReport
     *            the report creation parameters, see {@link CreateReport}
     * @param authorizationHeader
     *            the <code>Authorization</code> request header
     * @param request
     *            the servlet request
     * @return the report
     */
    @PostMapping("/{id}")
    public ResponseEntity<StreamingResponseBody> generateReport(@PathVariable String id,
            @Valid @RequestBody CreateReport createReport,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Output type " + outputType + " not supported by report " + id
                            + ", supported output types: " + supportedOutputTypes);
        }

        // exactly one target entity is supported at the moment
        TargetSpecification target = createReport.getTargets().get(0);
        List<EntityType> supportedTargetTypes = configuration.get().getTargetTypes();
        if (!supportedTargetTypes.contains(target.type)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Target type " + target.type + " not supported by report " + id);
        }
        Locale locale = localeResolver.resolveLocale(request);
        logger.info("Request locale = {}", locale);

        String desiredLanguage = locale.getLanguage();
        Set<String> supportedLanguages = configuration.get().getName().keySet();
        if (!supportedLanguages.contains(desiredLanguage)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Language " + desiredLanguage + " not supported by report " + id
                            + ", supported languages: " + supportedLanguages);
        }

        Map<String, Object> entriesForLanguage;
        try {
            entriesForLanguage = veoClient.fetchTranslations(locale, authorizationHeader);
        } catch (IOException e) {
            throw new ServerErrorException("Failed to fetch translations for " + locale, e);
        }
        DataProvider dataProvider = (key, url) -> {
            String expandedUrl = expandUrl(key, url, target);
            try {
                return veoClient.fetchData(expandedUrl, authorizationHeader);
            } catch (IOException e) {
                throw new ServerErrorException("Failed to fetch report data from " + expandedUrl,
                        e);
            }
        };

        StreamingResponseBody stream = out -> {
            try {
                reportEngine.generateReport(id, outputType, locale, out, dataProvider,
                        entriesForLanguage);
                logger.info("Report generated");
            } catch (TemplateException e) {
                logger.error("Error creating report", e);
                throw new ServerErrorException("Error creating report", e);
            }
        };
        return ResponseEntity.ok().contentType(MediaType.valueOf(outputType)).body(stream);
    }

    private static String expandUrl(String key, String url, TargetSpecification target) {
        PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper("${", "}");
        return helper.replacePlaceholders(url, placeholderName -> {
            if (TARGET_ID.equals(placeholderName)) {
                return target.id;
            } else {
                throw new IllegalArgumentException(
                        "Unsupported placeholder in url " + key + ": " + placeholderName);
            }
        });
    }

}