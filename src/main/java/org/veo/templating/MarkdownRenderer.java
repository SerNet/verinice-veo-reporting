package org.veo.templating;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public interface MarkdownRenderer {
    void renderToHTML(Reader reader, Writer writer) throws IOException;
}