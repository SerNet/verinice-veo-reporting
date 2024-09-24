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
package org.veo.fileconverter.handlers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.EnumSet;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.helger.font.api.FontResourceManager;
import com.helger.font.api.IFontStyle;
import com.openhtmltopdf.objects.jfreechart.JFreeChartBarDiagramObjectDrawer;
import com.openhtmltopdf.objects.jfreechart.JFreeChartPieDiagramObjectDrawer;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FSFontUseCase;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FontStyle;
import com.openhtmltopdf.pdfboxout.PdfBoxRenderer;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.render.DefaultObjectDrawerFactory;

import org.veo.fileconverter.ConversionHandler;
import org.veo.fileconverter.charts.VeoJFreeChartPieDiagramObjectDrawer;
import org.veo.fileconverter.charts.VeoJFreeChartSpiderWebDiagramObjectDrawer;
import org.veo.reporting.ReportConfiguration;
import org.veo.reporting.ReportCreationParameters;

/** Converts HTML to PDF */
public class HtmlPDFConverter implements ConversionHandler {

  private static final String STYLE = "style";

  private static final Logger logger = LoggerFactory.getLogger(HtmlPDFConverter.class);

  private static final Pattern SVG_BASE_64 =
      Pattern.compile("url\\('data:image/svg\\+xml;base64,(.+)'\\)");

  @Override
  public String getInputType() {
    return "text/html";
  }

  @Override
  public String getOutputType() {
    return "application/pdf";
  }

  @Override
  public void convert(
      InputStream input,
      OutputStream output,
      ReportConfiguration reportConfiguration,
      ReportCreationParameters parameters)
      throws IOException {

    // uncomment to set optional extensions
    // options.set(TocExtension.LIST_CLASS,
    // PdfConverterExtension.DEFAULT_TOC_LIST_CLASS);

    try (Scanner s = new Scanner(input, StandardCharsets.UTF_8).useDelimiter("\\Z")) {
      String html = s.next();

      if (html.isEmpty()) {
        logger.info("HTML input is empty, skipping PDF creation");
        return;
      }
      // There are more options on the builder than shown below.
      PdfRendererBuilder builder = new PdfRendererBuilder();

      addFonts(builder);
      DefaultObjectDrawerFactory factory = new DefaultObjectDrawerFactory();
      factory.registerDrawer("jfreechart/pie", new JFreeChartPieDiagramObjectDrawer());
      factory.registerDrawer("jfreechart/bar", new JFreeChartBarDiagramObjectDrawer());
      factory.registerDrawer("jfreechart/veo-pie", new VeoJFreeChartPieDiagramObjectDrawer());
      factory.registerDrawer(
          "jfreechart/veo-spiderweb", new VeoJFreeChartSpiderWebDiagramObjectDrawer());
      builder.useObjectDrawerFactory(factory);

      org.jsoup.nodes.Document doc = Jsoup.parse(html);
      doc.forEach(
          el -> {
            String style = el.attr(STYLE);
            if (style != null && !style.isEmpty()) {
              el.attr(STYLE, replaceSvgBackgrounds(style));
            }
          });
      Document dom = new W3CDom().fromJsoup(doc);

      builder.withW3cDocument(dom, "");
      builder.usePdfUaAccessibility(true);
      builder.toStream(output);
      try (PdfBoxRenderer renderer = builder.buildPdfRenderer()) {
        renderer.layout();
        renderer.createPDF();
      }
    }
  }

  // replace inline SVG backgrounds by PNG images to work around
  // https://github.com/danfickle/openhtmltopdf/issues/750
  private static String replaceSvgBackgrounds(String str) {
    Matcher m = SVG_BASE_64.matcher(str);
    return m.replaceAll(
        r -> {
          String svgText =
              new String(
                  Base64.getDecoder().decode(r.group(1).getBytes(StandardCharsets.UTF_8)),
                  StandardCharsets.UTF_8);

          PNGTranscoder pngTranscoder = new PNGTranscoder();
          TranscodingHints transcodingHints = pngTranscoder.getTranscodingHints();
          transcodingHints.put(SVGAbstractTranscoder.KEY_ALLOW_EXTERNAL_RESOURCES, Boolean.FALSE);
          transcodingHints.remove(SVGAbstractTranscoder.KEY_ALLOWED_SCRIPT_TYPES);
          ByteArrayOutputStream os = new ByteArrayOutputStream();
          try {
            pngTranscoder.transcode(
                new TranscoderInput(new StringReader(svgText)), new TranscoderOutput(os));
            byte[] bytes = os.toByteArray();
            String encoded = Base64.getEncoder().encodeToString(bytes);
            String newUrl = "data:image/png;base64," + encoded;

            return "url('" + newUrl + "')";

          } catch (TranscoderException ex) {
            throw new RuntimeException(ex);
          }
        });
  }

  protected static void addFonts(PdfRendererBuilder builder) {
    FontResourceManager.getAllResourcesOfFontType("Open Sans")
        .forEach(
            font -> {
              builder.useFont(
                  font::getBufferedInputStream,
                  font.getFontName(),
                  font.getFontWeight().getWeight(),
                  convertFontStyle(font.getFontStyle()),
                  true);
              builder.useFont(
                  font::getBufferedInputStream,
                  font.getFontName(),
                  font.getFontWeight().getWeight(),
                  convertFontStyle(font.getFontStyle()),
                  true,
                  EnumSet.of(FSFontUseCase.FALLBACK_PRE));
            });
  }

  private static FontStyle convertFontStyle(IFontStyle fontStyle) {
    if (fontStyle.isItalic()) {
      return FontStyle.ITALIC;
    }
    return FontStyle.NORMAL;
  }
}
