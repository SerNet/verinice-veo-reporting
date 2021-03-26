/**
 * Copyright (c) 2021 Jochen Kemnade.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.veo.reporting.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import org.veo.reporting.CreateReport;
import org.veo.reporting.CreateReport.TargetSpecification;
import org.veo.reporting.EntityType;
import org.veo.reporting.ReportConfiguration;
import org.veo.reporting.ReportEngine;
import org.veo.reporting.VeoClient;

import freemarker.template.TemplateException;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportEngine reportEngine;
    private final VeoClient veoClient;
    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    public ReportController(ReportEngine reportEngine, VeoClient veoClient) {
        this.reportEngine = reportEngine;
        this.veoClient = veoClient;
    }

    @GetMapping
    public Map<String, ReportConfiguration> getReports() {
        return reportEngine.getReports();
    }

    @PostMapping("/{id}")
    public ResponseEntity<StreamingResponseBody> generateReport(@PathVariable String id,
            @Valid @RequestBody CreateReport createReport,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
        if (authorizationHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        logger.info("Create report {}, outputType {}", id, createReport.getOutputType());
        Optional<ReportConfiguration> configuration = reportEngine.getReport(id);
        if (!configuration.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        // exactly one target entity is supported at the moment
        TargetSpecification target = createReport.getTargets().get(0);
        List<EntityType> supportedTargetTypes = configuration.get().getTargetTypes();
        if (!supportedTargetTypes.contains(target.type)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Target type " + target.type + " not supported by report " + id);
        }

        StreamingResponseBody stream = out -> {
            try {
                reportEngine.generateReport(id, createReport.getOutputType(), out, (key, url) -> {
                    PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper("${", "}");
                    String expandedUrl = helper.replacePlaceholders(url, placeholderName -> {
                        if ("targetId".equals(placeholderName)) {
                            return target.id;
                        } else {
                            throw new IllegalArgumentException("Unsupported placeholder in url "
                                    + key + ": " + placeholderName);
                        }
                    });
                    try {
                        return veoClient.fetchData(expandedUrl, authorizationHeader);
                    } catch (IOException e) {
                        throw new ServerErrorException(
                                "Failed to fetch report data from " + expandedUrl, e);
                    }
                });
                logger.info("Report generated");
            } catch (TemplateException e) {
                logger.error("Error creating report", e);
                throw new ServerErrorException("Error creating report", e);
            }
        };
        return ResponseEntity.ok().contentType(MediaType.valueOf(createReport.getOutputType()))
                .body(stream);
    }

}