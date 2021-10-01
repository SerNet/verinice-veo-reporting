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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.veo.templating.VeoReportingObjectWrapper;

import freemarker.template.AdapterTemplateModel;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.WrappingTemplateModel;

public class VeoReportingEntityAdapter extends WrappingTemplateModel
        implements TemplateHashModel, AdapterTemplateModel {

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

    private static final class GetLinks implements TemplateMethodModelEx {
        private final Map<?, ?> m;
        private final VeoReportingObjectWrapper ow;

        public GetLinks(Map<?, ?> m, VeoReportingObjectWrapper ow) {
            this.m = m;
            this.ow = ow;
        }

        @Override
        public Object exec(List arguments) throws TemplateModelException {
            if (arguments.size() != 1) {
                throw new TemplateModelException("Expecting exactly 1 arguments");
            }
            Object typeObj = arguments.get(0);
            if (!(typeObj instanceof SimpleScalar)) {
                throw new TemplateModelException(
                        "Expecting a String argument but got " + typeObj.getClass());
            }
            Map<String, ?> links = (Map<String, ?>) m.get("links");
            return ow.wrap(links.get(((SimpleScalar) typeObj).getAsString()));
        }
    }

}