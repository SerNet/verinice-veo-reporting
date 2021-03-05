package org.veo.reporting;

import java.io.IOException;

public interface VeoClient {

    Object fetchData(String path, String accessToken) throws IOException;

}
