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
package org.veo.templating.methods;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.veo.templating.VeoReportingObjectWrapper;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

abstract class VeoTemplateMethod implements TemplateMethodModelEx {
    private final Map<?, ?> m;
    private final VeoReportingObjectWrapper ow;

    protected static final Logger logger = LoggerFactory.getLogger(VeoTemplateMethod.class);

    protected VeoTemplateMethod(Map<?, ?> m, VeoReportingObjectWrapper ow) {
        this.m = m;
        this.ow = ow;
    }

    protected Object getProperty(String name) {
        return m.get(name);
    }

    protected Object resolve(String path) throws TemplateModelException {
        return ow.resolve(path);
    }

    protected String getLabel(String key) {
        return ow.getLabel(key);
    }

    protected Object resolveRef(Object objectReference) throws TemplateModelException {
        logger.debug("resolve object reference {}", objectReference);

        String targetUri = (String) ((Map) objectReference).get("targetUri");
        logger.debug("targetUri = {}", targetUri);
        if (targetUri == null) {
            throw new TemplateModelException(
                    "No targetUri property found in object reference " + objectReference);
        }
        Object targetEntity = resolve(targetUri);
        logger.debug("targetEntity = {}", targetEntity);
        if (targetEntity == null) {
            throw new TemplateModelException(
                    "Failed to resolve entity with targetUri " + targetUri);
        }
        return targetEntity;
    }

    protected Object resolveRefs(List objectReferences) throws TemplateModelException {
        logger.debug("resolve object references {}", objectReferences);

        List result = new ArrayList<>(objectReferences.size());
        for (Object ref : objectReferences) {
            result.add(resolveRef(ref));
        }
        return result;
    }

    @Override
    public final Object exec(List arguments) throws TemplateModelException {
        logger.debug("execute {} with arguments {}", getClass().getName(), arguments);
        return ow.wrap(doExec(arguments));
    }

    protected abstract Object doExec(List arguments) throws TemplateModelException;

}