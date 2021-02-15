package org.veo.templating;

import java.io.IOException;
import java.io.OutputStream;

import freemarker.template.TemplateException;

public interface TemplateEvaluator {
    void executeTemplate(String templateName, Object data, OutputStream out)
            throws TemplateException, IOException;
}