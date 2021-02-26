package org.veo.templating;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.BiFunction;

import freemarker.template.TemplateException;

public interface ReportEngine {

    void generateReport(String reportName, String outputType, OutputStream outputStream,
            BiFunction<String, String, Object> dataProvider) throws IOException, TemplateException;

    void generateReport(String templateName, Object data, String outputType, OutputStream output)
            throws IOException, TemplateException;

}