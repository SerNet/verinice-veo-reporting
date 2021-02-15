package org.veo.templating;

import java.io.IOException;
import java.io.OutputStream;

import freemarker.template.TemplateException;

public interface ReportEngine {

    void generateReport(String reportName, Object data, String outputType, OutputStream output)
            throws IOException, TemplateException;

}