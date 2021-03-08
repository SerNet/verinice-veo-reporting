/**
 * Copyright (c) 2021 Jochen Kemnade.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.veo.fileconverter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ComposedConversionHandler implements ConversionHandler {

    private final ConversionHandler firstHandler;
    private final ConversionHandler secondHandler;
    private final String inputType;
    private final String outputType;

    public ComposedConversionHandler(ConversionHandler firstHandler,
            ConversionHandler secondHandler) {
        this.firstHandler = firstHandler;
        this.secondHandler = secondHandler;
        this.inputType = firstHandler.getInputType();
        this.outputType = secondHandler.getOutputType();
    }

    @Override
    public String getInputType() {
        return inputType;
    }

    @Override
    public String getOutputType() {
        return outputType;
    }

    @Override
    public void convert(InputStream input, OutputStream output) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            firstHandler.convert(input, baos);
            var bytes = baos.toByteArray();
            try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
                secondHandler.convert(bais, output);
            }
        }
    }
}