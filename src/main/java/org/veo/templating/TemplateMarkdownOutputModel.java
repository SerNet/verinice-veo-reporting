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
