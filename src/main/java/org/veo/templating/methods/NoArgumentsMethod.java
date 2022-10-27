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
package org.veo.templating.methods;

import java.util.List;
import java.util.Map;

import org.veo.templating.VeoReportingObjectWrapper;

import freemarker.template.TemplateModelException;

public abstract class NoArgumentsMethod extends VeoTemplateMethod {

  protected NoArgumentsMethod(Map<?, ?> m, VeoReportingObjectWrapper ow) {
    super(m, ow);
  }

  @Override
  public final Object doExec(List arguments) throws TemplateModelException {
    if (!arguments.isEmpty()) {
      throw new TemplateModelException("Expecting no arguments");
    }

    return doExec();
  }

  protected abstract Object doExec() throws TemplateModelException;
}
