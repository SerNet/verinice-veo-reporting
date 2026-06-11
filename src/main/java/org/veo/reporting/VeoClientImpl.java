/*
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;

import org.veo.reporting.exception.DataFetchingException;

import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.module.blackbird.BlackbirdModule;

public class VeoClientImpl implements VeoClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(VeoClientImpl.class);
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
    JsonMapper objectMapper = JsonMapper.builder().addModule(new BlackbirdModule()).build();
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
      UUID unitId, UUID domainId, UUID targetId, String authorizationHeader) throws IOException {
    Map<String, Object> result = new HashMap<>();

    Map<String, Object> export =
        (Map<String, Object>)
            fetchData(
                "/units/" + unitId + "/export",
                authorizationHeader,
                "application/vnd.sernet.verinice.unit-dump.v2+json");
    List<Map<String, Object>> elements = (List<Map<String, Object>>) export.get("elements");
    List<Map<String, Object>> risks =
        (List<Map<String, Object>>) export.get(VeoReportingConstants.RISKS);
    applyRisks(elements, risks);

    elements = filterData(elements, domainId);

    var domain =
        fetchData("/domains/" + domainId, authorizationHeader, MediaType.APPLICATION_JSON_VALUE);

    result.put("domain", domain);
    result.put("unit", export.get("unit"));
    result.put("assets", filterElements(elements, "asset"));
    result.put("controls", filterElements(elements, "control"));
    result.put("documents", filterElements(elements, "document"));
    result.put("incidents", filterElements(elements, "incident"));
    result.put("persons", filterElements(elements, "person"));
    result.put("processes", filterElements(elements, "process"));
    result.put("scenarios", filterElements(elements, "scenario"));
    result.put("scopes", filterElements(elements, "scope"));
    var target =
        elements.stream()
            .filter(it -> it.get("id").equals(targetId.toString()))
            .findFirst()
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Target with id " + targetId + " not found in unit " + unitId));
    result.put("target", target);

    return result;
  }

  // TODO: remove this when #4830 is done
  private List<Map<String, Object>> filterData(List<Map<String, Object>> elements, UUID domainId) {
    String domainIdAsString = domainId.toString();

    List<Map<String, Object>> elementsInDomain = new ArrayList<>(elements.size());
    Set<String> elementURIsNotInDomain = new HashSet<>();

    for (Map<String, Object> element : elements) {
      Map<String, Map<String, Object>> domains =
          (Map<String, Map<String, Object>>) element.get(VeoReportingConstants.DOMAINS);
      if (domains.containsKey(domainIdAsString)) {
        Map<String, Object> copy = new HashMap<>(element);
        if (domains.size() > 1) {
          copy.put(
              VeoReportingConstants.DOMAINS,
              Map.of(domainIdAsString, domains.get(domainIdAsString)));
        }
        elementsInDomain.add(copy);
      } else {
        elementURIsNotInDomain.add((String) element.get("_self"));
      }
    }

    for (Map<String, Object> element : elementsInDomain) {
      Stream.of("parts", "members")
          .forEach(
              path -> {
                Optional.ofNullable(element.get(path))
                    .map(it -> (List<Map<String, Object>>) it)
                    .ifPresent(
                        items ->
                            element.put(
                                path,
                                items.stream()
                                    .filter(
                                        item ->
                                            !elementURIsNotInDomain.contains(
                                                item.get(VeoReportingConstants.TARGET_URI)))
                                    .toList()));
              });

      Optional.ofNullable(element.get(VeoReportingConstants.RISKS))
          .map(it -> (List<Map<String, Object>>) it)
          .ifPresent(
              risks ->
                  element.put(
                      VeoReportingConstants.RISKS,
                      risks.stream()
                          .filter(
                              risk -> {
                                Map<String, Object> scenario =
                                    (Map<String, Object>) risk.get("scenario");

                                if (elementURIsNotInDomain.contains(
                                    scenario.get(VeoReportingConstants.TARGET_URI))) {
                                  return false;
                                }

                                Map<String, Object> domains =
                                    (Map<String, Object>) risk.get(VeoReportingConstants.DOMAINS);

                                return domains.containsKey(domainIdAsString);
                              })
                          .map(HashMap::new)
                          .map(
                              risk -> {
                                Map<String, Object> domains =
                                    (Map<String, Object>) risk.get(VeoReportingConstants.DOMAINS);

                                if (domains.size() > 1) {
                                  risk.put(
                                      VeoReportingConstants.DOMAINS,
                                      Map.of(domainIdAsString, domains.get(domainIdAsString)));
                                }
                                Map<String, Object> mitigation =
                                    (Map<String, Object>) risk.get("mitigation");

                                if (mitigation != null
                                    && elementURIsNotInDomain.contains(
                                        mitigation.get(VeoReportingConstants.TARGET_URI))) {
                                  risk.remove("mitigation");
                                }
                                Map<String, Object> riskOwner =
                                    (Map<String, Object>) risk.get("riskOwner");

                                if (riskOwner != null
                                    && elementURIsNotInDomain.contains(
                                        riskOwner.get(VeoReportingConstants.TARGET_URI))) {
                                  risk.remove("riskOwner");
                                }
                                return risk;
                              })
                          .toList()));

      Stream.of("controlImplementations", "requirementImplementations")
          .forEach(
              path -> {
                Optional.ofNullable(element.get(path))
                    .map(it -> (List<Map<String, Object>>) it)
                    .ifPresent(
                        list -> {
                          List<Map<String, Object>> filteredList =
                              list.stream()
                                  .filter(
                                      item -> {
                                        Map<String, Object> control =
                                            (Map<String, Object>) item.get("control");
                                        return !elementURIsNotInDomain.contains(
                                            control.get(VeoReportingConstants.TARGET_URI));
                                      })
                                  .toList();
                          if (filteredList.size() != list.size()) {
                            element.put(path, filteredList);
                          }
                        });
              });
    }

    return elementsInDomain;
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
            VeoReportingConstants.RISKS,
            risks.stream()
                .filter(
                    r -> {
                      @SuppressWarnings("unchecked")
                      Map<String, Object> ref = (Map<String, Object>) r.get(type);
                      return ref != null
                          && ref.get(VeoReportingConstants.TARGET_URI).equals(element.get("_self"));
                    })
                .toList());
      }
    }
  }

  @Override
  public Object fetchData(String path, String authorizationHeader, String accept)
      throws IOException {
    Object result;
    URI uri = URI.create(veoUrl + path);
    if (cache != null) {
      result = cache.get(path);
      if (result != null) {
        LOGGER.info("Returning cached result for {}", uri);
        return result;
      }
    }

    LOGGER.info("Requesting data from {}", uri);

    ClientHttpRequest request = httpRequestFactory.createRequest(uri, HttpMethod.GET);
    request.getHeaders().add(HttpHeaders.AUTHORIZATION, authorizationHeader);
    request.getHeaders().setAccept(MediaType.parseMediaTypes(accept));
    request.getHeaders().add(HttpHeaders.ACCEPT_ENCODING, "gzip");
    try (ClientHttpResponse response = request.execute()) {
      if (!response.getStatusCode().is2xxSuccessful()) {
        LOGGER.error(
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
          cache.put(path, result);
        }
        return result;
      }
    }
  }
}
