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

    @Override
    public final Object exec(List arguments) throws TemplateModelException {
        logger.debug("execute {} with arguments {}", getClass().getName(), arguments);
        return ow.wrap(doExec(arguments));
    }

    protected abstract Object doExec(List arguments) throws TemplateModelException;

}