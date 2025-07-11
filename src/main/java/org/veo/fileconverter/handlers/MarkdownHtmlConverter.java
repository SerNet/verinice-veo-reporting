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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.parser.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.veo.fileconverter.ConversionHandler;
import org.veo.reporting.ReportConfiguration;
import org.veo.reporting.ReportCreationParameters;
import org.veo.templating.MarkdownRendererImpl;

/** Converts Markdown to HTML */
public class MarkdownHtmlConverter implements ConversionHandler {

  private static final Logger logger = LoggerFactory.getLogger(MarkdownHtmlConverter.class);

  @Override
  public String getInputType() {
    return "text/markdown";
  }

  @Override
  public String getOutputType() {
    return "text/html";
  }

  @Override
  public void convert(
      InputStream input,
      OutputStream output,
      ReportConfiguration reportConfiguration,
      ReportCreationParameters parameters)
      throws IOException {
    try (Reader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        StringWriter intermediateWriter = new StringWriter();
        Writer writer =
            new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8))) {
      new MarkdownRendererImpl().renderToHTML(reader, intermediateWriter);
      String md = intermediateWriter.toString();
      if (md.isEmpty()) {
        logger.info("Markdown input is empty, skipping HTML creation");
        return;
      }
      Parser parser = Parser.htmlParser();
      parser
          .tagSet()
          .add(Tag.valueOf("bookmark").set(Tag.SelfClose).set(Tag.Block))
          .add(Tag.valueOf("bookmarks").set(Tag.Block))
          .add(Tag.valueOf("data").set(Tag.SelfClose).set(Tag.Block))
          .add(Tag.valueOf("object").set(Tag.Block))
          // treat and format br as inline element
          .get("br", Parser.NamespaceHtml)
          .clear(Tag.Block);
      Document doc = Jsoup.parse(md, parser);
      Element html = doc.getElementsByTag("html").first();
      Locale locale = parameters.getLocale();
      if (locale.getCountry().isEmpty()) {
        html.attr("lang", locale.getLanguage());
      } else {
        html.attr("lang", locale.getLanguage() + "-" + locale.getCountry());
      }
      Element head =
          Optional.ofNullable(html.getElementsByTag("head").first())
              .orElseGet(() -> html.appendElement("head"));
      if (head.getElementsByTag("title").first() == null) {
        head.appendElement("title").text(reportConfiguration.getName().get(locale.getLanguage()));
      }
      writer.write(doc.toString());
    }
  }
}
