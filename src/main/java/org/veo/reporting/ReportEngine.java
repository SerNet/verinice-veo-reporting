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
package org.veo.reporting;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import freemarker.template.TemplateException;

public interface ReportEngine {

    void generateReport(String reportName, String outputType, OutputStream outputStream,
            BiFunction<String, String, Object> dataProvider) throws IOException, TemplateException;

    void generateReport(String templateName, Object data, String templateType, String outputType,
            OutputStream output) throws IOException, TemplateException;

    Map<String, ReportConfiguration> getReports();

    Optional<ReportConfiguration> getReport(String id);

}