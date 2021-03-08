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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public void generateReport(@PathVariable String id,
            @Valid @RequestBody CreateReport createReport, HttpServletResponse response)
            throws IOException, TemplateException {
        logger.info("Create report {}, outputType {}", id, createReport.getOutputType());
        Optional<ReportConfiguration> configuration = reportEngine.getReport(id);
        if (!configuration.isPresent()) {
            response.sendError(HttpStatus.NOT_FOUND.value());
            return;
        }

        // exactly one target entity is supported at the moment
        TargetSpecification target = createReport.getTargets().get(0);
        List<EntityType> supportedTargetTypes = configuration.get().getTargetTypes();
        if (!supportedTargetTypes.contains(target.type)) {
            response.sendError(HttpStatus.BAD_REQUEST.value(),
                    "Target type " + target.type + " not supported by report " + id);
            return;
        }

        // TODO try to get rid of additional byte[]
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            reportEngine.generateReport(id, createReport.getOutputType(), byteArrayOutputStream,
                    (key, url) -> {
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
                            return veoClient.fetchData(expandedUrl, createReport.getToken());
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to fetch report data from " + url,
                                    e);
                        }
                    });
            response.setContentType(createReport.getOutputType());
            try (OutputStream os = response.getOutputStream()) {
                os.write(byteArrayOutputStream.toByteArray());
            }
        }
    }

}