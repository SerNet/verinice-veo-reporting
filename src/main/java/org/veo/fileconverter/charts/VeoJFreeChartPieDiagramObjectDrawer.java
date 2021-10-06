/**
 * verinice.veo reporting
 * Copyright (C) 2022 Jochen Kemnade
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
package org.veo.fileconverter.charts;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PieLabelLinkStyle;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.openhtmltopdf.extend.FSObjectDrawer;
import com.openhtmltopdf.extend.OutputDevice;
import com.openhtmltopdf.render.RenderingContext;

public class VeoJFreeChartPieDiagramObjectDrawer implements FSObjectDrawer {

    private static final Pattern PATTERN_RGB = Pattern
            .compile("rgb *\\( *([0-9]+), *([0-9]+), *([0-9]+) *\\)");

    private static final Pattern PATTERN_HTML = Pattern
            .compile("#([0-9a-f]{2})([0-9a-f]{2})([0-9a-f]{2})", Pattern.CASE_INSENSITIVE);

    static Map<Shape, String> buildShapeLinkMap(ChartRenderingInfo renderingInfo,
            int dotsPerPixel) {
        Map<Shape, String> linkShapes = null;
        AffineTransform scaleTransform = new AffineTransform();
        scaleTransform.scale(dotsPerPixel, dotsPerPixel);
        for (Object entity : renderingInfo.getEntityCollection().getEntities()) {
            if (!(entity instanceof ChartEntity))
                continue;
            ChartEntity chartEntity = (ChartEntity) entity;
            Shape shape = chartEntity.getArea();
            String url = chartEntity.getURLText();
            if (url != null) {
                if (linkShapes == null)
                    linkShapes = new HashMap<>();
                linkShapes.put(scaleTransform.createTransformedShape(shape), url);
            }
        }
        return linkShapes;
    }

    public static Color parseColor(String input) {
        Matcher m = PATTERN_RGB.matcher(input);
        if (m.matches()) {
            return new Color(Integer.parseInt(m.group(1)), // r
                    Integer.parseInt(m.group(2)), // g
                    Integer.parseInt(m.group(3))); // b
        }
        m = PATTERN_HTML.matcher(input);
        if (m.matches()) {
            return new Color(Integer.parseInt(m.group(1), 16), // r
                    Integer.parseInt(m.group(2), 16), // g
                    Integer.parseInt(m.group(3), 16)); // b
        }
        return null;
    }

    @Override
    public Map<Shape, String> drawObject(Element e, final double x, final double y,
            final double width, final double height, OutputDevice outputDevice,
            RenderingContext ctx, final int dotsPerPixel) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        NodeList childNodes = e.getChildNodes();
        final Map<String, String> urls = new HashMap<>();
        final Map<String, String> colors = new HashMap<>();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (!(item instanceof Element))
                continue;
            Element childElement = (Element) item;
            String tagName = ((Element) item).getTagName();
            if (!tagName.equals("data") && !tagName.equals("td"))
                continue;
            String name = childElement.getAttribute("name");
            double value = Double.parseDouble(childElement.getAttribute("value"));
            String url = childElement.getAttribute("url");
            String color = childElement.getAttribute("color");
            dataset.setValue(name, value);
            if (!url.isEmpty()) {
                urls.put(name, url);
            }
            if (!color.isEmpty()) {
                colors.put(name, color);
            }
        }

        final JFreeChart chart1 = ChartFactory.createPieChart(e.getAttribute("title"), dataset,
                true, false, true);
        PiePlot<String> plot = (PiePlot<String>) chart1.getPlot();
        plot.setBackgroundPaint(null);
        plot.setURLGenerator((dataset1, key, pieIndex) -> urls.get(key.toString()));
        plot.setShadowPaint(null);
        plot.setShadowGenerator(null);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}\n{1} ({2})"));
        plot.setLegendLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {2}"));

        plot.setLabelOutlinePaint(null);
        plot.setLabelBackgroundPaint(new Color(255, 255, 255, 130));
        plot.setLabelLinkStyle(PieLabelLinkStyle.QUAD_CURVE);
        plot.setLabelShadowPaint(null);
        colors.forEach((key, value) -> plot.setSectionPaint(key, parseColor(value)));
        Color defaultFontColor = Color.decode("#7c7c7b");
        plot.setOutlinePaint(defaultFontColor);
        plot.setLabelPaint(defaultFontColor);
        chart1.getLegend().setItemPaint(defaultFontColor);

        chart1.getTitle().setPaint(defaultFontColor);

        final ChartRenderingInfo renderingInfo = new ChartRenderingInfo();
        outputDevice.drawWithGraphics((float) x, (float) y, (float) width / dotsPerPixel,
                (float) height / dotsPerPixel,
                graphics2D -> chart1.draw(graphics2D, new Rectangle2D.Float(0, 0,
                        (float) (width / dotsPerPixel), (float) (height / dotsPerPixel)),
                        renderingInfo));

        return buildShapeLinkMap(renderingInfo, dotsPerPixel);
    }
}
