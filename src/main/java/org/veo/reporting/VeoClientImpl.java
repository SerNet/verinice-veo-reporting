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

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.veo.reporting.exception.VeoReportingException;

public class VeoClientImpl implements VeoClient {

    private static final Logger logger = LoggerFactory.getLogger(VeoClientImpl.class);

    private final ClientHttpRequestFactory httpRequestFactory;
    private final String veoUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public VeoClientImpl(ClientHttpRequestFactory httpRequestFactory, String veoUrl) {
        this.httpRequestFactory = httpRequestFactory;
        this.veoUrl = veoUrl;
    }

    @Override
    public Object fetchData(String path, String authorizationHeader) throws IOException {
        return fetchData(URI.create(veoUrl + path), authorizationHeader);
    }

    private Object fetchData(URI uri, String authorizationHeader) throws IOException {
        logger.info("Requesting data from {}", uri);
        ClientHttpRequest request = httpRequestFactory.createRequest(uri, HttpMethod.GET);
        request.getHeaders().add(HttpHeaders.AUTHORIZATION, authorizationHeader);
        request.getHeaders().setAccept(List.of(MediaType.APPLICATION_JSON));
        try (ClientHttpResponse response = request.execute()) {
            if (!response.getStatusCode().is2xxSuccessful()) {
                logger.error("HTTP error {} for {}, message: {}", response.getRawStatusCode(), uri,
                        response.getStatusText());
                throw new VeoReportingException("Failed to retrieve data from " + uri
                        + ", status code: " + response.getRawStatusCode() + ", message: "
                        + response.getStatusText());
            }
            try (var body = response.getBody()) {
                JsonNode tree = objectMapper.readTree(body);
                if (tree.isArray()) {
                    return objectMapper.treeToValue(tree, List.class);
                } else {
                    Map m = objectMapper.treeToValue(tree, Map.class);
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
