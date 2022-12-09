/*******************************************************************************
 * verinice.veo reporting
 * Copyright (C) 2022  Jochen Kemnade
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

import java.util.HashMap;
import java.util.Map;

/**
 * This defines the data that the report requires. The keys are the variable names under which the
 * data is addressed in the report template, the values are the url fragments from where the data
 * should be retrieved.
 */
public class ReportDataSpecification extends HashMap<String, String> {

  public ReportDataSpecification(Map<String, String> keysAndUrls) {
    super(keysAndUrls);
  }
}
