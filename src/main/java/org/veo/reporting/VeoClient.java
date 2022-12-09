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

/**
 * Fetches data from a veo instance.
 *
 * @see org.veo.reporting.ReportConfiguration.data
 */
public interface VeoClient {

  Map<String, Object> fetchData(
      ReportDataSpecification dataSpecification, String authorizationHeader) throws IOException;

  public default Map<String, Object> fetchTranslations(Locale locale, String authorizationHeader)
      throws IOException {
    String language = locale.getLanguage();
    String translationsUrl = "/translations?languages=" + language;
    Map<String, Map<String, Map<String, Object>>> lang =
        (Map<String, Map<String, Map<String, Object>>>)
            fetchData(
                    new ReportDataSpecification(Map.of(translationsUrl, translationsUrl)),
                    authorizationHeader)
                .values()
                .iterator()
                .next();
    Map<String, Object> entriesForLanguage = lang.get("lang").get(language);
    Objects.requireNonNull(
        entriesForLanguage, "Failed to load translations for language " + language);
    return entriesForLanguage;
  }
}
