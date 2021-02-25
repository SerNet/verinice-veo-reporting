package org.veo.fileconverter.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.w3c.dom.Document;

import com.openhtmltopdf.pdfboxout.PdfBoxRenderer;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import org.veo.fileconverter.ConversionHandler;

public class HtmlPDFConverter implements ConversionHandler {

    @Override
    public String getInputType() {
        return "text/html";
    }

    @Override
    public String getOutputType() {
        return "application/pdf";
    }

    @Override
    public void convert(InputStream input, OutputStream output) throws IOException {

        // uncomment to set optional extensions
        // options.set(TocExtension.LIST_CLASS,
        // PdfConverterExtension.DEFAULT_TOC_LIST_CLASS);

        try (Scanner s = new Scanner(input, StandardCharsets.UTF_8).useDelimiter("\\A")) {
            String html = s.hasNext() ? s.next() : "";

            // There are more options on the builder than shown below.
            PdfRendererBuilder builder = new PdfRendererBuilder();

            org.jsoup.nodes.Document doc;
            doc = Jsoup.parse(html);

            Document dom = new W3CDom().fromJsoup(doc);
            builder.withW3cDocument(dom, "");

            builder.toStream(output);
            try (PdfBoxRenderer renderer = builder.buildPdfRenderer()) {
                renderer.layout();
                renderer.createPDF();
            }
        }
    }

}
