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

import java.util.Locale;
import java.util.TimeZone;

public class ReportCreationParameters {

  public ReportCreationParameters(Locale locale, TimeZone timeZone) {
    this.locale = locale;
    this.timeZone = (TimeZone) timeZone.clone();
  }

  private final Locale locale;
  private final TimeZone timeZone;

  public Locale getLocale() {
    return locale;
  }

  public TimeZone getTimeZone() {
    return (TimeZone) timeZone.clone();
  }

  @Override
  public String toString() {
    return "ReportCreationParameters [locale=" + locale + ", timeZone=" + timeZone + "]";
  }
}
