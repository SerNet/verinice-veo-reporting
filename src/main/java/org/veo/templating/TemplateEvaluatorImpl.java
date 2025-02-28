/*******************************************************************************
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
 ******************************************************************************/
package org.veo.templating;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.veo.reporting.ReportCreationParameters;
import org.veo.templating.methods.Base64Encode;
import org.veo.templating.methods.ColorContrast;

import freemarker.cache.ConditionalTemplateConfigurationFactory;
import freemarker.cache.FileExtensionMatcher;
import freemarker.cache.MergingTemplateConfigurationFactory;
import freemarker.cache.NullCacheStorage;
import freemarker.cache.OrMatcher;
import freemarker.cache.TemplateLoader;
import freemarker.core.Environment;
import freemarker.core.HTMLOutputFormat;
import freemarker.core.TemplateClassResolver;
import freemarker.core.TemplateConfiguration;
import freemarker.core.XMLOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateModelException;

public class TemplateEvaluatorImpl implements TemplateEvaluator {

  private static final Logger logger = LoggerFactory.getLogger(TemplateEvaluatorImpl.class);

  private final Configuration cfg;

  public TemplateEvaluatorImpl(TemplateLoader templateLoader, boolean useCache) {
    cfg = new Configuration(Configuration.VERSION_2_3_34);
    cfg.setTemplateLoader(templateLoader);
    // Recommended settings for new projects:
    cfg.setDefaultEncoding("UTF-8");
    cfg.setNewBuiltinClassResolver(TemplateClassResolver.ALLOWS_NOTHING_RESOLVER);
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    cfg.setLogTemplateExceptions(false);
    cfg.setWrapUncheckedExceptions(true);
    cfg.setFallbackOnNullLoopVariable(false);
    if (!useCache) {
      logger.info("Caching disabled");
      cfg.setCacheStorage(NullCacheStorage.INSTANCE);
    }
    cfg.setSharedVariable("colorContrast", ColorContrast.INSTANCE);
    cfg.setSharedVariable("base64", Base64Encode.INSTANCE);

    TemplateConfiguration tcMD = new TemplateConfiguration();
    tcMD.setOutputFormat(MarkdownOutputFormat.INSTANCE);
    TemplateConfiguration tcHTML = new TemplateConfiguration();
    tcHTML.setOutputFormat(HTMLOutputFormat.INSTANCE);
    TemplateConfiguration tcXML = new TemplateConfiguration();
    tcXML.setOutputFormat(XMLOutputFormat.INSTANCE);

    cfg.setTemplateConfigurations(
        new MergingTemplateConfigurationFactory(
            new ConditionalTemplateConfigurationFactory(new FileExtensionMatcher("xml"), tcXML),
            new ConditionalTemplateConfigurationFactory(
                new OrMatcher(new FileExtensionMatcher("html"), new FileExtensionMatcher("htm")),
                tcHTML),
            new ConditionalTemplateConfigurationFactory(new FileExtensionMatcher("md"), tcMD)));
  }

  @Override
  public void executeTemplate(
      String templateName, Map data, OutputStream out, ReportCreationParameters parameters)
      throws TemplateException, IOException {
    logger.info("Evaluating template {}", templateName);
    Template template = cfg.getTemplate(templateName);

    try (Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {

      Map<String, Object> entitiesByUri = new HashMap<>();
      logger.info("Building entity lookup map");
      addRecursively(entitiesByUri, data);

      VeoReportingObjectWrapper objectWrapper =
          new VeoReportingObjectWrapper(
              cfg.getIncompatibleImprovements(),
              entitiesByUri,
              (ResourceBundle) data.get("bundle"));
      Environment env = template.createProcessingEnvironment(data, writer, objectWrapper);
      env.setLocale(parameters.getLocale());
      env.setTimeZone(parameters.getTimeZone());
      logger.info("Processing template");
      env.process();
      logger.info("Template processed");
    }
  }

  private void addRecursively(Map<String, Object> entitiesByUri, Object data)
      throws TemplateModelException {
    logger.debug("adding entities from {}", data);
    if (data instanceof Map) {
      Map<?, ?> map = (Map<?, ?>) data;
      String selfUri = (String) map.get("_self");
      if (selfUri != null) {
        logger.debug("adding {}: {}", selfUri, map);
        entitiesByUri.put(selfUri, map);
      } else {
        for (Entry<?, ?> e : map.entrySet()) {
          logger.debug("Found key {}", e.getKey());
          addRecursively(entitiesByUri, e.getValue());
        }
      }

    } else if (data instanceof Collection) {
      Collection<?> list = (Collection<?>) data;
      for (Object object : list) {
        logger.debug(" found item: {}", object);
        addRecursively(entitiesByUri, object);
      }
    }
  }
}
