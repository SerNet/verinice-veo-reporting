/**
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
 */
package org.veo.fileconverter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.veo.reporting.ReportConfiguration;
import org.veo.reporting.ReportCreationParameters;
import org.veo.reporting.exception.VeoReportingException;

/**
 * A conversion handler that delegates to a pair of conversion handlers A and B
 * to support direct conversion from A's input format to B's output format by
 * applying them one after another.
 */
public class ComposedConversionHandler implements ConversionHandler {

    private final ConversionHandler firstHandler;
    private final ConversionHandler secondHandler;
    private final String inputType;
    private final String outputType;
    private final ExecutorService executorService;

    public ComposedConversionHandler(ConversionHandler firstHandler,
            ConversionHandler secondHandler, ExecutorService executorService) {
        this.firstHandler = firstHandler;
        this.secondHandler = secondHandler;
        this.executorService = executorService;
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
    public void convert(InputStream input, OutputStream output,
            ReportConfiguration reportConfiguration, ReportCreationParameters parameters)
            throws IOException {
        try (PipedInputStream pipedInputStream = new PipedInputStream();
                PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream)) {
            // start async conversion of the first
            // handler's output with the second handler
            Future<Void> future = executorService.submit(() -> {
                secondHandler.convert(pipedInputStream, output, reportConfiguration, parameters);
                return null;
            });
            try {
                firstHandler.convert(input, pipedOutputStream, reportConfiguration, parameters);
            } catch (IOException e) {
                // first handler failed, cancel the second handler task
                future.cancel(true);
                throw e;
            }
            try {
                // wait for the second handler to finish
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new VeoReportingException("Error running conversion", e);
            }
        }
    }
}