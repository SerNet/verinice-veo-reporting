package org.veo.templating.converters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.veo.templating.FileConverter;
import org.veo.templating.MarkdownRenderer;

public class MarkdownHtmlConverter implements FileConverter {

    @Override
    public String getInputType() {
        return "text/markdown";
    }

    @Override
    public String getOutputType() {
        return "text/html";
    }

    @Override
    public void convert(InputStream input, OutputStream output) throws IOException {
        try (Reader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8));
                Writer writer = new BufferedWriter(
                        new OutputStreamWriter(output, StandardCharsets.UTF_8))) {
            new MarkdownRenderer().renderToHTML(reader, writer);
        }
    }

}
