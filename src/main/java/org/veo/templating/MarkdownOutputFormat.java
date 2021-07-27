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
