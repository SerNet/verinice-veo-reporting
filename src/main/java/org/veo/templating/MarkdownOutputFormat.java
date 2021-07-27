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
import java.io.Writer;

import freemarker.core.CommonMarkupOutputFormat;
import freemarker.template.TemplateModelException;
import freemarker.template.utility.StringUtil;

/**
 * Represents the Markdown output format (MIME type "text/markdown", name
 * "Markdown"). This format escapes by default (via
 * {@link StringUtil#XHTMLEnc(String)}). The {@code ?html}, {@code ?xhtml} and
 * {@code ?xml} built-ins silently bypass template output values of the type
 * produced by this output format ({@link TemplateMarkdownOutputModel}).
 */
public class MarkdownOutputFormat extends CommonMarkupOutputFormat<TemplateMarkdownOutputModel> {

    /**
     * The only instance (singleton) of this {@link OutputFormat}.
     */
    public static final MarkdownOutputFormat INSTANCE = new MarkdownOutputFormat();

    /**
     * @since 2.3.29
     */
    protected MarkdownOutputFormat() {
        // Only to decrease visibility
    }

    @Override
    public String getName() {
        return "Markdown";
    }

    @Override
    public String getMimeType() {
        return "text/markdown";
    }

    @Override
    public void output(String textToEsc, Writer out) throws IOException, TemplateModelException {
        StringUtil.XHTMLEnc(textToEsc, out);
    }

    @Override
    public String escapePlainText(String plainTextContent) {
        return StringUtil.XHTMLEnc(plainTextContent);
    }

    @Override
    public boolean isLegacyBuiltInBypassed(String builtInName) {
        return builtInName.equals("html") || builtInName.equals("xml")
                || builtInName.equals("xhtml");
    }

    @Override
    protected TemplateMarkdownOutputModel newTemplateMarkupOutputModel(String plainTextContent,
            String markupContent) {
        return new TemplateMarkdownOutputModel(plainTextContent, markupContent);
    }

}
