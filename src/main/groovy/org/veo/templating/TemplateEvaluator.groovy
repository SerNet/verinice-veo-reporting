package org.veo.templating

import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler

class TemplateEvaluator {

    final Configuration cfg

    public TemplateEvaluator() {
        cfg = new Configuration(Configuration.VERSION_2_3_29)
        cfg.setClassForTemplateLoading(TemplateEvaluator, '/templates')
        // Recommended settings for new projects:
        cfg.setDefaultEncoding("UTF-8")
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER)
        cfg.setLogTemplateExceptions(false)
        cfg.setWrapUncheckedExceptions(true)
        cfg.setFallbackOnNullLoopVariable(false)
    }

    void executeTemplate(String templateName, Object data, OutputStream out) {
        def template = cfg.getTemplate(templateName)
        out.withWriter {
            template.process(data, it)
        }
    }
}
