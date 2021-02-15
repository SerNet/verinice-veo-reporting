package org.veo.templating;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public class TemplateEvaluatorImpl implements TemplateEvaluator {

    private final Configuration cfg;

    public TemplateEvaluatorImpl() {
        cfg = new Configuration(Configuration.VERSION_2_3_29);
        cfg.setClassForTemplateLoading(TemplateEvaluatorImpl.class, "/templates");
        // Recommended settings for new projects:
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);
    }

    public void executeTemplate(String templateName, Object data, OutputStream out)
            throws TemplateException, IOException {
        Template template = cfg.getTemplate(templateName);
        try (Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
            template.process(data, writer);
        }
    }
}
