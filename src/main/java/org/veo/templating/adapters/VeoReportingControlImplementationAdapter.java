/*
 * verinice.veo reporting
 * Copyright (C) 2026  Jochen Kemnade
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
package org.veo.templating.adapters;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import org.veo.reporting.VeoReportingConstants;
import org.veo.templating.VeoReportingObjectWrapper;

import freemarker.template.AdapterTemplateModel;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.WrappingTemplateModel;

public class VeoReportingControlImplementationAdapter extends WrappingTemplateModel
    implements TemplateHashModel, AdapterTemplateModel {

  private final Map<?, ?> m;

  public VeoReportingControlImplementationAdapter(Map<?, ?> m, VeoReportingObjectWrapper ow) {
    super(ow);
    this.m = Map.copyOf(m);
  }

  @Override
  public Object getAdaptedObject(Class<?> hint) {
    return m;
  }

  @Override
  public TemplateModel get(String key) throws TemplateModelException {
    Object val = m.get(key);
    if (val != null) {
      return wrap(val);
    }

    if ("customAspects".equals(key)) {
      return wrap(getCustomAspects(m));
    }
    Map<String, Map<String, ?>> customAspects = getCustomAspects(m);
    if (customAspects != null) {
      for (Entry<String, Map<String, ?>> ca : customAspects.entrySet()) {
        for (Entry<String, ?> a : ca.getValue().entrySet()) {
          if (key.equals(a.getKey())) {
            return wrap(a.getValue());
          }
        }
      }
    }
    return null;
  }

  private static Map<String, Map<String, ?>> getCustomAspects(Map ci) {
    var domains = (Map<String, Map<String, ?>>) ci.get(VeoReportingConstants.DOMAINS);

    if (domains != null && !domains.isEmpty()) {
      var domainAssociation = domains.values().iterator().next();

      return (Map<String, Map<String, ?>>) domainAssociation.get("customAspects");
    }
    return Collections.emptyMap();
  }

  @Override
  public boolean isEmpty() throws TemplateModelException {
    return false;
  }
}
