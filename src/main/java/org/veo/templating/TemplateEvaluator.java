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
package org.veo.templating;

import java.io.IOException;
import java.io.OutputStream;

import freemarker.cache.TemplateLoader;
import freemarker.template.TemplateException;

/**
 * Evaluates a template with given data
 * 
 * @see TemplateLoader
 */
public interface TemplateEvaluator {
    void executeTemplate(String templateName, Object data, OutputStream out)
            throws TemplateException, IOException;
}