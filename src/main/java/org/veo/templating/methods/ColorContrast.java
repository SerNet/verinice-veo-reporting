/**
 * verinice.veo reporting
 * Copyright (C) 2022  Jochen Kemnade
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
package org.veo.templating.methods;

import java.awt.Color;
import java.util.List;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

/**
 * Port the color-contrast CSS function as a template method
 *
 * @see <a href=
 *      "https://www.w3.org/TR/css-color-5/#colorcontrast">https://www.w3.org/TR/css-color-5/#colorcontrast</a>
 */
public class ColorContrast implements TemplateMethodModelEx {

    public static final ColorContrast INSTANCE = new ColorContrast();

    @Override
    public Object exec(List arguments) throws TemplateModelException {
        if (arguments.size() < 3) {
            throw new TemplateModelException("Method requires at least 3 arguments");
        }

        Color baseColor = parseColor(arguments.get(0));
        double l = getRelativeLuminance(baseColor);

        int idxBestMatch = -1;
        double contrastBestMatch = -1d;

        for (int i = 1; i < arguments.size(); i++) {
            Color c = parseColor(arguments.get(i));
            double lC = getRelativeLuminance(c);

            double contrastRatioToC = lC > l ? (lC + 0.05) / (l + 0.05) : (l + 0.05) / (lC + 0.05);
            if (idxBestMatch == -1 || contrastBestMatch < contrastRatioToC) {
                idxBestMatch = i;
                contrastBestMatch = contrastRatioToC;
            }
        }

        return arguments.get(idxBestMatch);
    }

    private Color parseColor(Object object) {
        if (object instanceof SimpleScalar s) {
            return Color.decode(s.getAsString());
        }
        throw new IllegalArgumentException(object + " is not a string");
    }

    private static double getRelativeLuminance(Color color) {
        double[] c = new double[] { color.getRed(), color.getGreen(), color.getBlue() };
        for (int i = 0; i <= 2; i++) {
            double col = c[i] / 255d;
            if (col <= 0.03928) {
                col = col / 12.92;
            } else {
                col = Math.pow((col + 0.055) / 1.055, 2.4);
            }
            c[i] = col;
        }
        return (0.2126 * c[0]) + (0.7152 * c[1]) + (0.0722 * c[2]);
    }
}
