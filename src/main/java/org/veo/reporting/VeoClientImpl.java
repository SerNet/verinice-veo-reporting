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
import java.util.Set;
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
  private static final Set<String> RISK_AFFECTED_TYPES = Set.of("asset", "process", "scope");

  private final ClientHttpRequestFactory httpRequestFactory;
  private final String veoUrl;
  private final ObjectReader objectReader;
  private final ObjectReader arrayReader;
  private final Map<String, Object> cache;

  public VeoClientImpl(
      ClientHttpRequestFactory httpRequestFactory, String veoUrl, boolean cacheResults) {
    this.httpRequestFactory = httpRequestFactory;
    this.veoUrl = veoUrl;
    ObjectMapper objectMapper = new ObjectMapper().registerModule(new BlackbirdModule());
    objectReader = objectMapper.readerFor(Map.class);
    arrayReader = objectMapper.readerFor(List.class);
    if (cacheResults) {
      cache = new HashMap<>();
    } else {
      cache = null;
    }
  }

  @Override
  public Map<String, Object> fetchData(
      ReportDataSpecification reportDataSpecification, String authorizationHeader)
      throws IOException {

    Map<String, Object> result = new HashMap<>();
    for (Entry<String, String> e : reportDataSpecification.entrySet()) {
      Object v = fetchData(URI.create(veoUrl + e.getValue()), authorizationHeader);
      result.put(e.getKey(), v);
      if (v instanceof Map m) {
        @SuppressWarnings("unchecked")
        Map<String, Object> owner = (Map<String, Object>) m.get("owner");
        if (owner != null) {
          String ownerId = (String) owner.get("id");
          addDataForOwner(result, ownerId, authorizationHeader);
        }

      } else {
        throw new VeoReportingException("List-valued targets are not supported." + v);
      }
    }
    return result;
  }

  private void addDataForOwner(
      Map<String, Object> result, String ownerId, String authorizationHeader) throws IOException {
    @SuppressWarnings("unchecked")
    Map<String, Object> export =
        (Map<String, Object>)
            fetchData(URI.create(veoUrl + "/units/" + ownerId + "/export"), authorizationHeader);
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> elements = (List<Map<String, Object>>) export.get("elements");
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> risks = (List<Map<String, Object>>) export.get("risks");
    applyRisks(elements, risks);

    result.put("domains", export.get("domains"));
    result.put("unit", export.get("unit"));
    result.put("assets", filterElements(elements, "asset"));
    result.put("controls", filterElements(elements, "control"));
    result.put("documents", filterElements(elements, "document"));
    result.put("incidents", filterElements(elements, "incident"));
    result.put("persons", filterElements(elements, "person"));
    result.put("processes", filterElements(elements, "process"));
    result.put("scenarios", filterElements(elements, "scenario"));
    result.put("scopes", filterElements(elements, "scope"));
  }

  private static List<Map<String, Object>> filterElements(
      List<Map<String, Object>> elements, String type) {
    return elements.stream().filter(it -> it.get("type").equals(type)).toList();
  }

  private static void applyRisks(
      List<Map<String, Object>> elements, List<Map<String, Object>> risks) {
    for (Map<String, Object> element : elements) {
      String type = (String) element.get("type");
      if (RISK_AFFECTED_TYPES.contains(type)) {
        element.put(
            "risks",
            risks.stream()
                .filter(
                    r -> {
                      @SuppressWarnings("unchecked")
                      Map<String, Object> ref = (Map<String, Object>) r.get(type);
                      return ref != null && ref.get("targetUri").equals(element.get("_self"));
                    })
                .toList());
      }
    }
  }

  private Object fetchData(URI uri, String authorizationHeader) throws IOException {
    Object result;
    String cacheKey = uri.toString();
    if (cache != null) {
      result = cache.get(cacheKey);
      if (result != null) {
        logger.info("Returning cached result for {}", uri);
        return result;
      }
    }

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
          result = arrayReader.readValue(body);
        } else {
          Object value = objectReader.readValue(body);
          Map m = (Map) value;
          // add support for paged results
          if (m.containsKey("items")) {
            return (List) m.get("items");
          }
          result = m;
        }
        if (cache != null) {
          cache.put(cacheKey, result);
        }
        return result;
      }
    }
  }
}
