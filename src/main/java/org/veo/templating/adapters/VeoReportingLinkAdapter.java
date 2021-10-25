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

import java.util.Map;
import java.util.Map.Entry;

import org.veo.templating.VeoReportingObjectWrapper;
import org.veo.templating.methods.NoArgumentsMethod;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import freemarker.template.AdapterTemplateModel;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.WrappingTemplateModel;

public class VeoReportingLinkAdapter extends WrappingTemplateModel
        implements TemplateHashModel, AdapterTemplateModel {

    private final Map<?, ?> m;
    private final VeoReportingObjectWrapper ow;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public VeoReportingLinkAdapter(Map<?, ?> m, VeoReportingObjectWrapper ow) {
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
        if ("getTarget".equals(key)) {
            return new GetTarget(m, ow);
        }

        @SuppressWarnings("unchecked")
        Map<String, ?> attributes = (Map<String, ?>) m.get("attributes");
        if (attributes != null) {
            for (Entry<String, ?> a : attributes.entrySet()) {
                if (key.equals(a.getKey())) {
                    return wrap(a.getValue());
                }
            }
        }
        return null;
    }

    @Override
    public boolean isEmpty() throws TemplateModelException {
        return false;
    }

    private static final class GetTarget extends NoArgumentsMethod {

        public GetTarget(Map<?, ?> m, VeoReportingObjectWrapper ow) {
            super(m, ow);
        }

        @Override
        public Object doExec() throws TemplateModelException {
            Map target = (Map) getProperty("target");
            logger.debug("target = {}", target);
            return resolveRef(target);
        }
    }
}