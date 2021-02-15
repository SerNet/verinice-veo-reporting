package org.veo.templating;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;

import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.definition.DefinitionExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

public class MarkdownRendererImpl implements MarkdownRenderer {

    public void renderToHTML(Reader reader, Writer writer) throws IOException {
        MutableDataSet options = new MutableDataSet();

        // uncomment to set optional extensions
        options.set(Parser.EXTENSIONS, Arrays.asList(DefinitionExtension.create(),
                AnchorLinkExtension.create(), TablesExtension.create()));

        // uncomment to convert soft-breaks to hard breaks
        // options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");
        options.set(AnchorLinkExtension.ANCHORLINKS_WRAP_TEXT, false);

        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        // You can re-use parser and renderer instances
        Node document = parser.parseReader(reader);
        String html = renderer.render(document);
        writer.append(html);
    }
}
