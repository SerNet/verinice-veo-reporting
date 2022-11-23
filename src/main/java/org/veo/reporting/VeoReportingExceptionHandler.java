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
package org.veo.reporting;

import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.veo.reporting.exception.DataFetchingException;
import org.veo.reporting.exception.InvalidReportParametersException;

@ControllerAdvice
public class VeoReportingExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(VeoReportingExceptionHandler.class);

  @ExceptionHandler({DataFetchingException.class})
  protected ResponseEntity<String> handle(DataFetchingException exception) {
    return handle(exception, HttpStatus.valueOf(exception.getStatusCode()));
  }

  @ExceptionHandler({MethodArgumentNotValidException.class})
  protected ResponseEntity<String> handle(MethodArgumentNotValidException exception) {
    logger.error("Error invoking method", exception);
    return handle(
        exception.getBindingResult().getAllErrors().stream()
            .map(er -> er.unwrap(ConstraintViolation.class))
            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
            .collect(Collectors.joining(" ")),
        HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler({HttpMessageNotReadableException.class})
  protected ResponseEntity<String> handle(HttpMessageNotReadableException exception) {
    logger.error("Error reading HTTP message", exception);
    Throwable cause = exception.getCause();
    if (cause instanceof JsonProcessingException jpe) {
      return handle(jpe.getOriginalMessage(), HttpStatus.BAD_REQUEST);
    }
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
  }

  @ExceptionHandler({InvalidReportParametersException.class})
  protected ResponseEntity<String> handle(InvalidReportParametersException exception) {
    return handle(exception, HttpStatus.BAD_REQUEST);
  }

  private ResponseEntity<String> handle(Throwable exception, HttpStatus status) {
    logger.error("Error handling request", exception);
    return handle(exception.getMessage(), status);
  }

  private ResponseEntity<String> handle(String responseText, HttpStatus status) {
    return ResponseEntity.status(status).body(responseText);
  }
}
