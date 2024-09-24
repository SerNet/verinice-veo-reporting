/*******************************************************************************
 * verinice.veo reporting
 * Copyright (C) 2024  Jochen Kemnade
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
package org.veo.fileconverter.charts;

import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.data.category.DefaultCategoryDataset;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.helger.commons.collection.impl.ICommonsOrderedSet;
import com.helger.font.api.FontResourceManager;
import com.helger.font.api.IFontResource;
import com.openhtmltopdf.extend.FSObjectDrawer;
import com.openhtmltopdf.extend.OutputDevice;
import com.openhtmltopdf.render.RenderingContext;

import org.veo.reporting.exception.VeoReportingException;

public class VeoJFreeChartSpiderWebDiagramObjectDrawer implements FSObjectDrawer {

  private static final Pattern PATTERN_RGB =
      Pattern.compile("rgb *\\( *([0-9]+), *([0-9]+), *([0-9]+) *\\)");

  private static final Pattern PATTERN_HTML =
      Pattern.compile("#([0-9a-f]{2})([0-9a-f]{2})([0-9a-f]{2})", Pattern.CASE_INSENSITIVE);

  private static final Font openSansRegular;
  private static final Font openSansBold;

  static {
    var openSansFontResources = FontResourceManager.getAllResourcesOfFontType("Open Sans");
    openSansRegular = loadFont(openSansFontResources, 400);
    openSansBold = loadFont(openSansFontResources, 700);
  }

  static Map<Shape, String> buildShapeLinkMap(ChartRenderingInfo renderingInfo, int dotsPerPixel) {
    Map<Shape, String> linkShapes = null;
    AffineTransform scaleTransform = new AffineTransform();
    scaleTransform.scale(dotsPerPixel, dotsPerPixel);
    for (Object entity : renderingInfo.getEntityCollection().getEntities()) {
      if (!(entity instanceof ChartEntity)) continue;
      ChartEntity chartEntity = (ChartEntity) entity;
      Shape shape = chartEntity.getArea();
      String url = chartEntity.getURLText();
      if (url != null) {
        if (linkShapes == null) linkShapes = new HashMap<>();
        linkShapes.put(scaleTransform.createTransformedShape(shape), url);
      }
    }
    return linkShapes;
  }

  private static Font loadFont(
      ICommonsOrderedSet<IFontResource> openSansFontResources, int weight) {
    IFontResource resource =
        openSansFontResources.findFirst(
            f -> f.getFontWeight().getWeight() == weight && f.getFontStyle().isRegular());
    try (InputStream is = resource.getBufferedInputStream()) {
      return Font.createFont(Font.TRUETYPE_FONT, is);
    } catch (Exception e1) {
      throw new VeoReportingException("Error initializing chart font", e1);
    }
  }

  public static Color parseColor(String input) {
    Matcher m = PATTERN_RGB.matcher(input);
    if (m.matches()) {
      return new Color(
          Integer.parseInt(m.group(1)), // r
          Integer.parseInt(m.group(2)), // g
          Integer.parseInt(m.group(3))); // b
    }
    m = PATTERN_HTML.matcher(input);
    if (m.matches()) {
      return new Color(
          Integer.parseInt(m.group(1), 16), // r
          Integer.parseInt(m.group(2), 16), // g
          Integer.parseInt(m.group(3), 16)); // b
    }
    return null;
  }

  @Override
  public Map<Shape, String> drawObject(
      Element e,
      final double x,
      final double y,
      final double width,
      final double height,
      OutputDevice outputDevice,
      RenderingContext ctx,
      final int dotsPerPixel) {
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    NodeList childNodes = e.getChildNodes();
    final Map<String, String> urls = new HashMap<>();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node item = childNodes.item(i);
      if (!(item instanceof Element)) continue;
      Element childElement = (Element) item;
      String tagName = ((Element) item).getTagName();
      if (!"data".equals(tagName) && !"td".equals(tagName)) continue;
      String row = childElement.getAttribute("row");
      String column = childElement.getAttribute("column");
      double value = Double.parseDouble(childElement.getAttribute("value"));
      String url = childElement.getAttribute("url");
      dataset.setValue(value, row, column);
      if (!url.isEmpty()) {
        urls.put(row + ":" + column, url);
      }
    }

    VeoSpiderWebPlot plot = new VeoSpiderWebPlot(dataset);
    plot.setMaxValue(5);
    plot.setGridLineCount(5);
    String v = e.getAttribute("interiorgap");
    if (!v.isEmpty()) {
      plot.setInteriorGap(Double.parseDouble(v));
    }

    plot.setBackgroundPaint(null);
    plot.setURLGenerator(
        (dataset1, series, category) -> {
          String column = (String) dataset1.getColumnKey(category);
          String row = (String) dataset1.getRowKey(series);
          return urls.get(row + ":" + column);
        });
    JFreeChart chart = new JFreeChart(e.getAttribute("title"), plot);
    chart.setBackgroundPaint(null);
    plot.setWebFilled(1, true);
    plot.setSeriesPaint(0, Color.decode("#339966"));
    plot.setSeriesPaint(1, Color.decode("#99ccff"));

    Color defaultFontColor = Color.decode("#767676");
    plot.setOutlinePaint(defaultFontColor);
    plot.setLabelPaint(defaultFontColor);
    chart.getLegend().setItemPaint(defaultFontColor);

    chart.getTitle().setPaint(defaultFontColor);
    chart.getTitle().setFont(openSansBold.deriveFont(Font.BOLD, 20f));
    chart.getLegend().setItemFont(openSansRegular.deriveFont(Font.PLAIN, 12f));
    plot.setLabelFont(openSansRegular.deriveFont(Font.PLAIN, 9f));

    final ChartRenderingInfo renderingInfo = new ChartRenderingInfo();
    outputDevice.drawWithGraphics(
        (float) x,
        (float) y,
        (float) width / dotsPerPixel,
        (float) height / dotsPerPixel,
        graphics2D ->
            chart.draw(
                graphics2D,
                new Rectangle2D.Float(
                    0, 0, (float) (width / dotsPerPixel), (float) (height / dotsPerPixel)),
                renderingInfo));

    return buildShapeLinkMap(renderingInfo, dotsPerPixel);
  }
}
