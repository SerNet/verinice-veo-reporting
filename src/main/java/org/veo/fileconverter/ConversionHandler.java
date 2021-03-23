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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A handler that can convert data from one format to another one, e.g.
 * text/markdown to text/html. This is not meant to be used directly. See
 * {@link FileConverter}.
 */
public interface ConversionHandler {

    /**
     * @return the handler's input type. Must be a valid MIME type, such as
     *         <code>text/html</code>.
     */
    String getInputType();

    /**
     * @return the handler's output type. Must be a valid MIME type, such as
     *         <code>text/html</code>.
     */
    String getOutputType();

    /**
     * performs the actual conversion
     */
    void convert(InputStream input, OutputStream output) throws IOException;

}
