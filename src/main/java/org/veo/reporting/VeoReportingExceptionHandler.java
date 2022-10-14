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
package org.veo.reporting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import org.veo.reporting.exception.DataFetchingException;

@ControllerAdvice
public class VeoReportingExceptionHandler {

    private static final Logger logger = LoggerFactory
            .getLogger(VeoReportingExceptionHandler.class);

    @ExceptionHandler({ DataFetchingException.class })
    protected ResponseEntity<String> handle(DataFetchingException exception) {
        return handle(exception, HttpStatus.valueOf(exception.getStatusCode()));
    }

    private ResponseEntity<String> handle(Throwable exception, HttpStatus status) {
        logger.error("Error handling request", exception);
        return ResponseEntity.status(status).body(exception.getMessage());
    }
}
