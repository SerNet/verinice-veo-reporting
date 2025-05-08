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
package org.veo.templating;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.veo.templating.adapters.VeoReportingEntityAdapter;
import org.veo.templating.adapters.VeoReportingLinkAdapter;
import org.veo.templating.adapters.VeoReportingRiskAdapter;
import org.veo.templating.adapters.VeoReportingRiskDefinitionAdapter;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.Version;

public class VeoReportingObjectWrapper extends DefaultObjectWrapper {

  private static final Logger logger = LoggerFactory.getLogger(VeoReportingObjectWrapper.class);

  private final Map<String, Object> entitiesByUri;

  private final ResourceBundle bundle;

  @SuppressFBWarnings("EI_EXPOSE_REP2")
  public VeoReportingObjectWrapper(
      Version incompatibleImprovements, Map<String, Object> entitiesByUri, ResourceBundle bundle) {
    super(incompatibleImprovements);
    this.bundle = bundle;
    this.entitiesByUri = Map.copyOf(entitiesByUri);
  }

  @Override
  public TemplateModel wrap(Object obj) throws TemplateModelException {
    if (obj instanceof Map) {
      Map<?, ?> m = (Map<?, ?>) obj;
      if (m.containsKey("id") && m.containsKey("customAspects")) {
        // this is probably an entity
        return new VeoReportingEntityAdapter((Map<?, ?>) obj, this);
      } else if (m.containsKey("target") && m.containsKey("attributes")) {
        // this is probably a custom link
        return new VeoReportingLinkAdapter((Map<?, ?>) obj, this);
      } else if (m.containsKey("scenario") && m.containsKey("domains")) {
        // this is probably a risk
        return new VeoReportingRiskAdapter((Map<?, ?>) obj, this);
      } else if (m.containsKey("probability") && m.containsKey("implementationStateDefinition")) {
        // this is probably a risk definition
        return new VeoReportingRiskDefinitionAdapter((Map<?, ?>) obj, this);
      } else if (m.containsKey("targetUri")) {
        // this is probably ref
        // do not try to resolve catalog items
        if (!"catalog-item".equals(m.get("type"))) {
          return wrap(resolve((String) m.get("targetUri")));
        }
      }
    }
    return super.wrap(obj);
  }

  public Collection<Map> getScopes(Object arg) {
    var memberUri = ((Map) arg).get("_self");
    return entitiesByUri.values().stream()
        .map(Map.class::cast)
        .filter(s -> "scope".equals(s.get("type")))
        .filter(
            s ->
                ((Collection<Map<String, ?>>) s.get("members"))
                    .stream().map(m -> m.get("targetUri")).anyMatch(memberUri::equals))
        .toList();
  }

  private Object resolve(String uri) throws TemplateModelException {
    Objects.requireNonNull(uri);
    logger.debug("resolve uri {}", uri);
    Object entity = entitiesByUri.get(uri);
    if (entity == null) {
      throw new TemplateModelException("Failed to resolve entity with targetUri " + uri);
    }
    return entity;
  }

  public String getLabel(String key) {
    return bundle.getString(key);
  }
}
