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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;

import org.veo.reporting.exception.DataFetchingException;
import org.veo.reporting.exception.VeoReportingException;

public class VeoClientImpl implements VeoClient {

  private static final Logger logger = LoggerFactory.getLogger(VeoClientImpl.class);

  private final ClientHttpRequestFactory httpRequestFactory;
  private final String veoUrl;
  private final ObjectReader objectReader;
  private final ObjectReader arrayReader;
  private final ExecutorService executorService;

  public VeoClientImpl(
      ClientHttpRequestFactory httpRequestFactory, String veoUrl, ExecutorService executorService) {
    this.httpRequestFactory = httpRequestFactory;
    this.veoUrl = veoUrl;
    this.executorService = executorService;
    ObjectMapper objectMapper = new ObjectMapper().registerModule(new BlackbirdModule());
    objectReader = objectMapper.readerFor(Map.class);
    arrayReader = objectMapper.readerFor(List.class);
  }

  @Override
  public Map<String, Object> fetchData(
      ReportDataSpecification reportDataSpecification, String authorizationHeader)
      throws IOException {

    Map<String, Future<Object>> futuresByKey =
        reportDataSpecification.entrySet().stream()
            .collect(
                Collectors.toMap(
                    Entry::getKey,
                    (e) ->
                        executorService.submit(
                            () ->
                                fetchData(
                                    URI.create(veoUrl + e.getValue()), authorizationHeader))));
    try {
      Map<String, Object> result = new HashMap<>(reportDataSpecification.size());
      for (Entry<String, Future<Object>> e : futuresByKey.entrySet()) {
        result.put(e.getKey(), e.getValue().get());
      }
      return result;
    } catch (InterruptedException | ExecutionException e1) {
      throw new VeoReportingException(e1);
    }
  }

  private Object fetchData(URI uri, String authorizationHeader) throws IOException {
    logger.info("Requesting data from {}", uri);

    ClientHttpRequest request = httpRequestFactory.createRequest(uri, HttpMethod.GET);
    request.getHeaders().add(HttpHeaders.AUTHORIZATION, authorizationHeader);
    request.getHeaders().setAccept(List.of(MediaType.APPLICATION_JSON));
    request.getHeaders().add(HttpHeaders.ACCEPT_ENCODING, "gzip");
    try (ClientHttpResponse response = request.execute()) {
      if (!response.getStatusCode().is2xxSuccessful()) {
        logger.error(
            "HTTP error {} for {}, message: {}",
            response.getStatusCode().value(),
            uri,
            response.getStatusText());
        throw new DataFetchingException(
            uri.toString(), response.getStatusCode().value(), response.getStatusText());
      }
      boolean gzip =
          response.getHeaders().getOrEmpty(HttpHeaders.CONTENT_ENCODING).contains("gzip");
      try (var s = response.getBody();
          var body =
              gzip ? new BufferedInputStream(new GZIPInputStream(s)) : new BufferedInputStream(s)) {
        body.mark(1);
        char c = (char) body.read();
        body.reset();
        if (c == '[') {
          return arrayReader.readValue(body);
        } else {
          Object value = objectReader.readValue(body);
          Map m = (Map) value;
          // add support for paged results
          if (m.containsKey("items")) {
            return (List) m.get("items");
          }
          return m;
        }
      }
    }
  }
}
