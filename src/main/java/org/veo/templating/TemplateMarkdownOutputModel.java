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

import freemarker.core.CommonTemplateMarkupOutputModel;

/**
 * Stores Markdown markup to be printed; used with {@link MarkdownOutputFormat}.
 */
public class TemplateMarkdownOutputModel
        extends CommonTemplateMarkupOutputModel<TemplateMarkdownOutputModel> {

    /**
     * See
     * {@link CommonTemplateMarkupOutputModel#CommonTemplateMarkupOutputModel(String, String)}.
     * 
     * @since 2.3.29
     */
    protected TemplateMarkdownOutputModel(String plainTextContent, String markupContent) {
        super(plainTextContent, markupContent);
    }

    @Override
    public MarkdownOutputFormat getOutputFormat() {
        return MarkdownOutputFormat.INSTANCE;
    }

}
