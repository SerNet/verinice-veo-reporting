/**
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
 */
package org.veo.templating.adapters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.veo.templating.VeoReportingObjectWrapper;
import org.veo.templating.methods.NoArgumentsMethod;
import org.veo.templating.methods.SingleStringArgumentMethod;

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
        // for now, scopes have members, everything else has parts
        if ("scope".equals(type)) {
            if ("getMembers".equals(key)) {
                return new GetMembers(m, ow);
            }
        } else {
            if ("getParts".equals(key)) {
                return new GetParts(m, ow);
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
        case "getLinked":
            return new GetLinked(m, ow);
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
                    Map<String, ?> attributes = (Map<String, ?>) ((Map<String, ?>) ca.getValue())
                            .get("attributes");
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
            return domains != null && domains.values().stream()
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

    private static final class GetMembers extends NoArgumentsMethod {

        public GetMembers(Map<?, ?> m, VeoReportingObjectWrapper ow) {
            super(m, ow);
        }

        @Override
        public Object doExec() throws TemplateModelException {
            List<?> members = (List<?>) getProperty("members");
            logger.debug("members: {}", members);
            return resolveRefs(members);
        }
    }

    private static final class GetParts extends NoArgumentsMethod {

        public GetParts(Map<?, ?> m, VeoReportingObjectWrapper ow) {
            super(m, ow);
        }

        @Override
        public Object doExec() throws TemplateModelException {
            List<?> parts = (List<?>) getProperty("parts");
            logger.debug("parts: {}", parts);
            return resolveRefs(parts);
        }
    }

    private abstract static class LinkResolvingMethod extends SingleStringArgumentMethod {

        public LinkResolvingMethod(Map<?, ?> m, VeoReportingObjectWrapper ow) {
            super(m, ow);
        }

        protected Object resolveLink(Object link) throws TemplateModelException {
            logger.debug("Found link {}", link);
            Map target = (Map) ((Map) link).get("target");
            logger.debug("target = {}", target);
            resolveRef(target);
            return resolveRef(target);
        }

        protected Object resolveLinks(List<?> linksOfType) throws TemplateModelException {
            List result = new ArrayList<>(linksOfType.size());
            for (Object link : linksOfType) {
                Map target = (Map) ((Map) link).get("target");
                result.add(resolveRef(target));
            }
            return result;
        }
    }

    private static final class FindFirstLinked extends LinkResolvingMethod {

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
                    Object link = l.get(0);
                    return resolveLink(link);
                }
            }
            return null;
        }
    }

    private static final class GetLinked extends LinkResolvingMethod {

        public GetLinked(Map<?, ?> m, VeoReportingObjectWrapper ow) {
            super(m, ow);
        }

        @Override
        public Object doExec(String arg) throws TemplateModelException {
            Map<String, ?> links = (Map<String, ?>) getProperty("links");
            Object linksOfType = links.get(arg);
            if (linksOfType instanceof List) {
                return resolveLinks((List) linksOfType);

            }
            return null;
        }

    }

    private static final class GetImplementationStatus extends NoArgumentsMethod {

        private static final Map<String, String> statusColors = Map.of(
                "control_implementation_status_yes", "#12AE0F", "control_implementation_status_no",
                "#AE0D11", "control_implementation_status_partially", "#EDE92F",
                "control_implementation_status_notApplicable", "#49A2ED");

        public GetImplementationStatus(Map<?, ?> m, VeoReportingObjectWrapper ow) {
            super(m, ow);
        }

        @Override
        public Object doExec() throws TemplateModelException {
            // TODO: read implementation status from risk values
            Map customAspects = (Map) getProperty("customAspects");
            Map controlImplementation = (Map) customAspects.get("control_implementation");
            if (controlImplementation == null) {
                return null;
            }
            Map controlImplementationAttributes = (Map) controlImplementation.get("attributes");
            String implementationStatus = (String) controlImplementationAttributes
                    .get("control_implementation_status");
            if (implementationStatus == null) {
                return null;
            }
            String name = getLabel(implementationStatus);
            // TODO: read colors from risk configuration
            String color = statusColors.get(implementationStatus);
            return Map.of("id", implementationStatus, "label", name, "color", color);

        }
    }

}