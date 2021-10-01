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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.veo.templating.VeoReportingObjectWrapper;
import org.veo.templating.methods.SingleStringArgumentMethod;

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

    public VeoReportingEntityAdapter(Map<?, ?> m, VeoReportingObjectWrapper ow) {
        super(ow);
        this.m = m;
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
        if ("getLinks".equals(key)) {
            return new GetLinks(m, ow);
        }
        if ("getLinked".equals(key)) {
            return new GetLinked(m, ow);
        }
        if ("findFirstLinked".equals(key)) {
            return new FindFirstLinked(m, ow);
        }

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

    @Override
    public boolean isEmpty() throws TemplateModelException {
        return false;
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

    private abstract static class LinkResolvingMethod extends SingleStringArgumentMethod {

        public LinkResolvingMethod(Map<?, ?> m, VeoReportingObjectWrapper ow) {
            super(m, ow);
        }

        protected Object resolve(Object link) throws TemplateModelException {
            logger.debug("Found link {}", link);
            Map target = (Map) ((Map) link).get("target");
            logger.debug("target = {}", target);

            String targetUri = (String) ((Map) target).get("targetUri");
            logger.debug("targetUri = {}", targetUri);
            Object targetEntity = resolve(targetUri);
            logger.debug("targetEntity = {}", targetEntity);
            if (targetEntity == null) {
                throw new TemplateModelException(
                        "Failed to resolve entity with targetUri " + targetUri);
            }
            return targetEntity;
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
                    return resolve(link);
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
                List l = (List) linksOfType;
                List result = new ArrayList<>(l.size());
                for (Object object : l) {
                    result.add(resolve(object));
                }

                return result;
            }
            return null;
        }
    }

}