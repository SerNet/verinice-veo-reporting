package org.veo.reporting;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    public Object fetchData(String path, String accessToken) throws IOException {
        return fetchData(URI.create(veoUrl + path), accessToken);
    }

    private Object fetchData(URI uri, String accessToken) throws IOException {
        logger.info("Requesting data from {}", uri);
        ClientHttpRequest request = httpRequestFactory.createRequest(uri, HttpMethod.GET);
        request.getHeaders().setBearerAuth(accessToken);
        request.getHeaders().setAccept(List.of(MediaType.APPLICATION_JSON));
        try (ClientHttpResponse response = request.execute()) {
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to retrieve data from " + uri + ", status code: "
                        + response.getRawStatusCode() + ", message: " + response.getStatusText());
            }
            try (var body = response.getBody()) {
                JsonNode tree = objectMapper.readTree(body);
                if (tree.isArray()) {
                    return objectMapper.treeToValue(tree, List.class);
                } else {
                    return objectMapper.treeToValue(tree, Map.class);
                }
            }
        }
    }

}
