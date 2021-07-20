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
package org.veo.templating;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.cache.NullCacheStorage;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public class TemplateEvaluatorImpl implements TemplateEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(TemplateEvaluatorImpl.class);

    private final Configuration cfg;

    public TemplateEvaluatorImpl(TemplateLoader templateLoader, boolean useCache) {
        cfg = new Configuration(Configuration.VERSION_2_3_29);
        cfg.setTemplateLoader(templateLoader);
        // Recommended settings for new projects:
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);
        if (!useCache) {
            logger.info("Caching disabled");
            cfg.setCacheStorage(NullCacheStorage.INSTANCE);
        }
        cfg.setObjectWrapper(new VeoReportingObjectWrapper(cfg.getIncompatibleImprovements()));
    }

    public void executeTemplate(String templateName, Object data, OutputStream out)
            throws TemplateException, IOException {
        Template template = cfg.getTemplate(templateName);
        logger.info("Evaluating template {}", templateName);

        try (Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
            template.process(data, writer);
        }
    }
}
