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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.veo.templating.VeoReportingObjectWrapper;
import org.veo.templating.methods.SingleStringArgumentMethod;
import org.veo.templating.methods.VeoTemplateMethod;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import freemarker.template.AdapterTemplateModel;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.WrappingTemplateModel;

public class VeoReportingEntityAdapter extends WrappingTemplateModel
    implements TemplateHashModel, AdapterTemplateModel {

  private static final Logger logger = LoggerFactory.getLogger(VeoReportingEntityAdapter.class);

  private final Map<?, ?> m;
  private final VeoReportingObjectWrapper ow;

  @SuppressFBWarnings("EI_EXPOSE_REP2")
  public VeoReportingEntityAdapter(Map<?, ?> m, VeoReportingObjectWrapper ow) {
    super(ow);
    this.m = Map.copyOf(m);
    this.ow = ow;
  }

  @Override
  @SuppressFBWarnings("EI_EXPOSE_REP")
  public Object getAdaptedObject(Class<?> hint) {
    return m;
  }

  @Override
  public TemplateModel get(String key) throws TemplateModelException {
    Object val = m.get(key);
    if (val != null) {
      return wrap(val);
    }

    Object type = m.get("type");
    if ("scope".equals(type)) {
      if ("getMembersWithType".equals(key)) {
        return new GetMembersWithType(m, ow);
      }
    }
    if ("control".equals(type)) {
      if ("getImplementationStatus".equals(key)) {
        return new GetImplementationStatus(m, ow);
      }
    }
    switch (key) {
      case "getLinks":
        return new GetLinks(m, ow);
      case "findLinked":
        return new FindLinked(m, ow);
      case "findFirstLinked":
        return new FindFirstLinked(m, ow);
      case "hasSubType":
        return new HasSubType(m, ow);
      default:
        @SuppressWarnings("unchecked")
        Map<String, ?> customAspects = (Map<String, ?>) m.get("customAspects");
        if (customAspects != null) {
          for (Entry<String, ?> ca : customAspects.entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, ?> attributes =
                (Map<String, ?>) ((Map<String, ?>) ca.getValue()).get("attributes");
            if (attributes != null) {
              for (Entry<String, ?> a : attributes.entrySet()) {
                if (key.equals(a.getKey())) {
                  return wrap(a.getValue());
                }
              }
            }
          }
        }
        return null;
    }
  }

  @Override
  public boolean isEmpty() throws TemplateModelException {
    return false;
  }

  private static final class HasSubType extends SingleStringArgumentMethod {

    public HasSubType(Map<?, ?> m, VeoReportingObjectWrapper ow) {
      super(m, ow);
    }

    @Override
    public Object doExec(String arg) throws TemplateModelException {
      var domains = (Map<String, Map<String, ?>>) getProperty("domains");

      logger.debug("domains: {}", domains);
      return domains != null
          && domains.values().stream()
              .map(domainAssociation -> (String) domainAssociation.get("subType"))
              .anyMatch(subTypeInDomain -> Objects.equals(arg, subTypeInDomain));
    }
  }

  private static final class GetLinks extends SingleStringArgumentMethod {

    public GetLinks(Map<?, ?> m, VeoReportingObjectWrapper ow) {
      super(m, ow);
    }

    @Override
    public Object doExec(String arg) throws TemplateModelException {
      Map<String, ?> links = (Map<String, ?>) getProperty("links");
      logger.debug("links: {}", links);
      return links.get(arg);
    }
  }

  private static final class FindFirstLinked extends SingleStringArgumentMethod {

    public FindFirstLinked(Map<?, ?> m, VeoReportingObjectWrapper ow) {
      super(m, ow);
    }

    @Override
    public Object doExec(String arg) throws TemplateModelException {
      Map<String, ?> links = (Map<String, ?>) getProperty("links");
      Object linksOfType = links.get(arg);
      if (linksOfType instanceof List) {
        List l = (List) linksOfType;
        if (!l.isEmpty()) {
          Map firstLink = (Map) l.get(0);
          return firstLink.get("target");
        }
      }
      return null;
    }
  }

  private static final class FindLinked extends SingleStringArgumentMethod {

    public FindLinked(Map<?, ?> m, VeoReportingObjectWrapper ow) {
      super(m, ow);
    }

    @Override
    public Object doExec(String arg) throws TemplateModelException {
      Map<String, ?> links = (Map<String, ?>) getProperty("links");
      Object linksOfType = links.get(arg);
      if (linksOfType instanceof List) {
        return ((List) linksOfType)
            .stream().map(m -> ((Map) m).get("target")).collect(Collectors.toList());
      }
      return Collections.emptyList();
    }
  }

  private static final class GetImplementationStatus extends VeoTemplateMethod {

    public GetImplementationStatus(Map<?, ?> m, VeoReportingObjectWrapper ow) {
      super(m, ow);
    }

    @Override
    protected Object doExec(List arguments) throws TemplateModelException {
      if (arguments.size() != 2) {
        throw new TemplateModelException("Expecting 2 arguments, domain ID and risk definition ID");
      }

      String domainId = asString(arguments.get(0));
      String riskDefinitionId = asString(arguments.get(1));

      return Optional.ofNullable(getProperty("domains"))
          .map(domains -> ((Map) domains).get(domainId))
          .map(dataForDomain -> ((Map) dataForDomain).get("riskValues"))
          .map(riskDefinitions -> ((Map) riskDefinitions).get(riskDefinitionId))
          .map(dataForRiskDefinition -> ((Map) dataForRiskDefinition).get("implementationStatus"))
          .orElse(null);
    }
  }

  private static final class GetMembersWithType extends SingleStringArgumentMethod {

    public GetMembersWithType(Map<?, ?> m, VeoReportingObjectWrapper ow) {
      super(m, ow);
    }

    @Override
    public Object doExec(String arg) throws TemplateModelException {
      var members = (List) getProperty("members");
      var filteredMembers =
          members.stream()
              .filter(
                  member -> {
                    // FIXME: VEO-1203: use type in idref to filter
                    return ((String) ((Map) member).get("targetUri")).contains("/" + arg);
                  })
              .collect(Collectors.toList());
      return filteredMembers;
    }
  }
}
