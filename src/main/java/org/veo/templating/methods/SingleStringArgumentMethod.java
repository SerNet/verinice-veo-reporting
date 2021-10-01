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

import org.veo.templating.VeoReportingObjectWrapper;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelException;

public abstract class SingleStringArgumentMethod extends VeoTemplateMethod {

    protected SingleStringArgumentMethod(Map<?, ?> m, VeoReportingObjectWrapper ow) {
        super(m, ow);
    }

    @Override
    public final Object doExec(List arguments) throws TemplateModelException {
        if (arguments.size() != 1) {
            throw new TemplateModelException("Expecting exactly 1 argument");
        }
        Object typeObj = arguments.get(0);
        if (!(typeObj instanceof SimpleScalar)) {
            throw new TemplateModelException(
                    "Expecting a String argument but got " + typeObj.getClass());
        }
        String arg = ((SimpleScalar) typeObj).getAsString();
        return doExec(arg);
    }

    protected abstract Object doExec(String arg) throws TemplateModelException;

}