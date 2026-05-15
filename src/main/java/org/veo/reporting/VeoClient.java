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

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.http.MediaType;

/** Fetches data from a veo instance. */
public interface VeoClient {

  default Map<String, Object> fetchTranslations(
      Locale locale, UUID domainId, String authorizationHeader) throws IOException {
    String language = locale.getLanguage();
    String translationsUrl =
        "/translations?languages=" + language + "&domain=" + domainId.toString();

    Map<String, Map<String, Map<String, Object>>> translations =
        (Map) fetchData(translationsUrl, authorizationHeader, MediaType.APPLICATION_JSON_VALUE);

    Map<String, Object> entriesForLanguage = translations.get("lang").get(language);
    Objects.requireNonNull(
        entriesForLanguage, "Failed to load translations for language " + language);
    return entriesForLanguage;
  }

  Map<String, Object> fetchData(
      UUID unitId, UUID domainId, UUID targetId, String authorizationHeader) throws IOException;

  Object fetchData(String path, String authorizationHeader, String accecpt) throws IOException;
}
