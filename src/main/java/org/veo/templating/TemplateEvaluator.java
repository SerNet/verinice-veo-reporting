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
import java.util.Map;

import org.veo.reporting.ReportCreationParameters;

import freemarker.cache.TemplateLoader;
import freemarker.template.TemplateException;

/**
 * Evaluates a template with given data
 *
 * @see TemplateLoader
 */
@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface TemplateEvaluator {
  void executeTemplate(
      String templateName,
      Map<String, Object> data,
      OutputStream out,
      ReportCreationParameters parameters)
      throws TemplateException, IOException;
}
