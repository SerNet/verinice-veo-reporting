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
package org.veo.templating.adapters;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.veo.templating.VeoReportingObjectWrapper;
import org.veo.templating.methods.VeoTemplateMethod;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import freemarker.template.AdapterTemplateModel;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.WrappingTemplateModel;

public class VeoReportingRiskAdapter extends WrappingTemplateModel
    implements TemplateHashModel, AdapterTemplateModel {

  private final Map<?, ?> m;
  private final VeoReportingObjectWrapper ow;

  @SuppressFBWarnings("EI_EXPOSE_REP2")
  public VeoReportingRiskAdapter(Map<?, ?> m, VeoReportingObjectWrapper ow) {
    super(ow);
    this.m = Map.copyOf(m);
    this.ow = ow;
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
    if ("getRiskValues".equals(key)) {
      return new GetRiskValues(m, ow);
    }
    return null;
  }

  @Override
  public boolean isEmpty() throws TemplateModelException {
    return false;
  }

  private static final class GetRiskValues extends VeoTemplateMethod {

    public GetRiskValues(Map<?, ?> m, VeoReportingObjectWrapper ow) {
      super(m, ow);
    }

    @Override
    protected Object doExec(List arguments) throws TemplateModelException {

      if (arguments.size() != 2) {
        throw new TemplateModelException("Expecting 2 arguments, domain ID and risk definition ID");
      }

      String domainId = asString(arguments.get(0));
      String riskDefinitionId = asString(arguments.get(1));
      Map domains = (Map) getProperty("domains");
      Map dataForDomain = (Map) domains.get(domainId);
      Map riskDefinitions = (Map) dataForDomain.get("riskDefinitions");
      Map dataForRiskDefinition = (Map) riskDefinitions.get(riskDefinitionId);
      if (dataForRiskDefinition == null) {
        return Collections.emptyMap();
      }

      Map result = new HashMap<>();

      Map probability = (Map) dataForRiskDefinition.get("probability");
      result.putAll(probability);
      List riskValues = (List) dataForRiskDefinition.get("riskValues");
      addCategorizedValues(result, riskValues);
      List impactValues = (List) dataForRiskDefinition.get("impactValues");
      addCategorizedValues(result, impactValues);
      return result;
    }

    private void addCategorizedValues(Map result, List categorizedValues) {
      for (Object object : categorizedValues) {
        Map r = (Map) object;
        String category = (String) r.get("category");
        ((Map) result.computeIfAbsent(category, (k) -> new HashMap())).putAll(r);
      }
    }
  }
}
