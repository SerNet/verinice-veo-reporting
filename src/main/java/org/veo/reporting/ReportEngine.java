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
import java.io.OutputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.veo.fileconverter.FileConverter;
import org.veo.templating.TemplateEvaluator;

import freemarker.cache.TemplateLoader;
import freemarker.template.TemplateException;

/**
 * The main entry point to create reports. <br>
 * Report creations consist of a template evaluation step and an output
 * conversion step
 * 
 * @see ReportConfiguration
 * @see TemplateEvaluator
 * @see FileConverter
 */
public interface ReportEngine {

    /**
     * Create a report
     * 
     * @param reportName
     *            the report identifier/name
     * @param outputType
     *            the desired output MIME type
     * @param locale
     *            the locale that the report is to be generated in
     * @param outputStream
     *            the target output stream
     * @param dataProvider
     *            is used to map the data keys accessible in the template to the
     *            real report data. Will be called with the key and the value
     *            that are specified in the report configuration
     * @param dynamicBundleEntries
     *            a set of additional entries that are available in the resource
     *            bundle passed to the report in addition to the ones that are
     *            defined in the report's resource bundle itself.
     */
    void generateReport(String reportName, String outputType, Locale locale,
            OutputStream outputStream, DataProvider dataProvider,
            Map<String, Object> dynamicBundleEntries) throws IOException, TemplateException;

    /**
     * Execute a template directly, bypassing the data fetching and bundle
     * loading mechanisms.
     * 
     * @param templateName
     *            the template file name (see {@link TemplateLoader}
     * @param data
     *            the data that is available for the template execution
     * @param templateType
     *            the MIME type of the template
     * @param outputType
     *            the desired output MIME type
     * @param output
     *            the target output stream
     */
    void generateReport(String templateName, Object data, String templateType, String outputType,
            OutputStream output) throws IOException, TemplateException;

    Map<String, ReportConfiguration> getReports();

    Optional<ReportConfiguration> getReport(String id);

}