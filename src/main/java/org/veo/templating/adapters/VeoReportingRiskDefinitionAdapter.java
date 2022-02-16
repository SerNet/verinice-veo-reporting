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
import java.util.Optional;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.veo.templating.VeoReportingObjectWrapper;
import org.veo.templating.methods.SingleNumberArgumentMethod;
import org.veo.templating.methods.VeoTemplateMethod;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import freemarker.template.AdapterTemplateModel;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.WrappingTemplateModel;

public class VeoReportingRiskDefinitionAdapter extends WrappingTemplateModel
        implements TemplateHashModel, AdapterTemplateModel {

    private static final Logger logger = LoggerFactory
            .getLogger(VeoReportingRiskDefinitionAdapter.class);

    private final Map<?, ?> m;
    private final VeoReportingObjectWrapper ow;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public VeoReportingRiskDefinitionAdapter(Map<?, ?> m, VeoReportingObjectWrapper ow) {
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
        if ("getImpact".equals(key)) {
            return new GetImpact(m, ow);
        }
        if ("getRisk".equals(key)) {
            return new GetRisk(m, ow);
        }
        if ("getProbability".equals(key)) {
            return new GetProbability(m, ow);
        }
        if ("getImplementationStatus".equals(key)) {
            return new GetImplementationStatus(m, ow);
        }
        return null;
    }

    @Override
    public boolean isEmpty() throws TemplateModelException {
        return false;
    }

    private static final class GetImpact extends VeoTemplateMethod {

        public GetImpact(Map<?, ?> m, VeoReportingObjectWrapper ow) {
            super(m, ow);
        }

        @Override
        protected Object doExec(List arguments) throws TemplateModelException {
            if (arguments.size() != 2) {
                throw new TemplateModelException(
                        "Expecting 2 arguments, category ID and impact ID");
            }
            String categoryId = asString(arguments.get(0));
            Number impactId = asNumber(arguments.get(1));

            Optional<Map> category = findByPredicate((List) getProperty("categories"),
                    c -> c.get("id").equals(categoryId));
            if (category.isEmpty()) {
                throw new TemplateModelException("Invalid " + category + " " + categoryId);
            }
            Map impact = getByOrdinalValue((List) category.get().get("potentialImpacts"), impactId,
                    "impact");
            return getMetadata(impactId, impact);
        }
    }

    private static final class GetRisk extends SingleNumberArgumentMethod {

        public GetRisk(Map<?, ?> m, VeoReportingObjectWrapper ow) {
            super(m, ow);
        }

        @Override
        protected Object doExec(Number riskId) throws TemplateModelException {
            Map riskValue = getByOrdinalValue((List) getProperty("riskValues"), riskId, "risk");
            return getMetadata(riskId, riskValue);
        }
    }

    private static final class GetProbability extends SingleNumberArgumentMethod {

        public GetProbability(Map<?, ?> m, VeoReportingObjectWrapper ow) {
            super(m, ow);
        }

        @Override
        protected Object doExec(Number probabilityId) throws TemplateModelException {
            List levels = (List) ((Map) getProperty("probability")).get("levels");
            Map level = getByOrdinalValue(levels, probabilityId, "probablilty");
            return getMetadata(probabilityId, level);
        }
    }

    private static final class GetImplementationStatus extends SingleNumberArgumentMethod {

        public GetImplementationStatus(Map<?, ?> m, VeoReportingObjectWrapper ow) {
            super(m, ow);
        }

        @Override
        protected Object doExec(Number implementationStateId) throws TemplateModelException {
            List levels = (List) ((Map) getProperty("implementationStateDefinition")).get("levels");
            Map implementationState = getByOrdinalValue(levels, implementationStateId,
                    "implementation status");
            return getMetadata(implementationStateId, implementationState);
        }
    }

    private static Map getByOrdinalValue(List list, Number ordinalValue, String valueType)
            throws TemplateModelException {
        Optional<Map> value = findByPredicate(list,
                m -> m.get("ordinalValue").equals(ordinalValue));
        if (value.isEmpty()) {
            throw new TemplateModelException("Invalid " + valueType + " " + ordinalValue);
        }
        return value.get();
    }

    private static Optional<Map> findByPredicate(List list, Predicate<Map> predicate) {
        return Optional.ofNullable(
                (Map) list.stream().filter(l -> predicate.test((Map) l)).findFirst().orElse(null));
    }

    private static Map<String, Object> getMetadata(Object key, Map level) {
        return Map.of("id", key, "label", level.get("name"), "description",
                level.get("description"), "color", level.get("htmlColor"));
    }
}